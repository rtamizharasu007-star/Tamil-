package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcBlueDark
import com.example.ui.theme.ArcBorder
import com.example.ui.theme.ArcCyan

@Composable
fun FuturisticHudContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArcBlueDark)
    ) {
        // Draw HUD Corner Accents
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val bracketLen = 24.dp.toPx()
            val strokeWidth = 2.5.dp.toPx()
            val accentColor = ArcCyan.copy(alpha = 0.8f)

            // Top-Left Corner
            drawLine(accentColor, Offset(0f, 0f), Offset(bracketLen, 0f), strokeWidth)
            drawLine(accentColor, Offset(0f, 0f), Offset(0f, bracketLen), strokeWidth)

            // Top-Right Corner
            drawLine(accentColor, Offset(w - bracketLen, 0f), Offset(w, 0f), strokeWidth)
            drawLine(accentColor, Offset(w, 0f), Offset(w, bracketLen), strokeWidth)

            // Bottom-Left Corner
            drawLine(accentColor, Offset(0f, h - bracketLen), Offset(0f, h), strokeWidth)
            drawLine(accentColor, Offset(0f, h), Offset(bracketLen, h), strokeWidth)

            // Bottom-Right Corner
            drawLine(accentColor, Offset(w - bracketLen, h), Offset(w, h), strokeWidth)
            drawLine(accentColor, Offset(w, h - bracketLen), Offset(w, h), strokeWidth)
        }

        content()
    }
}
