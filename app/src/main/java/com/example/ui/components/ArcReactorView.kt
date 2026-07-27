package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcGold
import kotlin.math.cos
import kotlin.math.sin

enum class ArcReactorState {
    IDLE, LISTENING, PROCESSING, SPEAKING
}

@Composable
fun ArcReactorView(
    state: ArcReactorState = ArcReactorState.IDLE,
    size: Dp = 160.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorRotation")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    ArcReactorState.PROCESSING -> 1500
                    ArcReactorState.LISTENING -> 2500
                    ArcReactorState.SPEAKING -> 2000
                    ArcReactorState.IDLE -> 6000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(state) {
        when (state) {
            ArcReactorState.SPEAKING -> {
                pulseScale.animateTo(
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            ArcReactorState.LISTENING -> {
                pulseScale.animateTo(
                    targetValue = 1.25f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            else -> {
                pulseScale.snapTo(1f)
            }
        }
    }

    val reactorColor = when (state) {
        ArcReactorState.LISTENING -> ArcGold
        ArcReactorState.PROCESSING -> Color(0xFF00E5FF)
        ArcReactorState.SPEAKING -> ArcCyan
        ArcReactorState.IDLE -> ArcCyan.copy(alpha = 0.8f)
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("arc_reactor_view"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * pulseScale.value)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = this.size.width / 2f

            // Outer Glow Circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(reactorColor.copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            // Outer Ring
            drawCircle(
                color = reactorColor.copy(alpha = 0.6f),
                radius = maxRadius * 0.85f,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Inner Ring with Dashed HUD Segments
            rotate(rotationAngle, pivot = center) {
                drawCircle(
                    color = reactorColor,
                    radius = maxRadius * 0.72f,
                    center = center,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // 8 Triangular / Radial Arc Reactor Nodes
                val nodeCount = 8
                val nodeRadius = maxRadius * 0.65f
                for (i in 0 until nodeCount) {
                    val angle = (i * 360f / nodeCount) * (Math.PI / 180f)
                    val startX = center.x + (nodeRadius * 0.6f * cos(angle)).toFloat()
                    val startY = center.y + (nodeRadius * 0.6f * sin(angle)).toFloat()
                    val endX = center.x + (nodeRadius * cos(angle)).toFloat()
                    val endY = center.y + (nodeRadius * sin(angle)).toFloat()

                    drawLine(
                        color = reactorColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Counter-rotating Inner Ring
            rotate(-rotationAngle * 1.5f, pivot = center) {
                drawArc(
                    color = ArcGold,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - maxRadius * 0.5f, center.y - maxRadius * 0.5f),
                    size = androidx.compose.ui.geometry.Size(maxRadius, maxRadius),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawArc(
                    color = ArcGold,
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - maxRadius * 0.5f, center.y - maxRadius * 0.5f),
                    size = androidx.compose.ui.geometry.Size(maxRadius, maxRadius),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Core Energy Center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, reactorColor, ArcCyanGlow, Color.Transparent),
                    center = center,
                    radius = maxRadius * 0.35f
                ),
                radius = maxRadius * 0.35f,
                center = center
            )
        }
    }
}
