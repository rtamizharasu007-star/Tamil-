package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcSurfaceVariant
import com.example.ui.theme.OfflineYellow
import com.example.ui.theme.OnlineGreen

@Composable
fun ModeIndicatorChip(
    isOnline: Boolean,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusColor = if (isOnline) OnlineGreen else OfflineYellow
    val textLabel = if (isOnline) "🟢 ONLINE MODE" else "🟡 OFFLINE MODE"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ArcSurfaceVariant.copy(alpha = 0.9f))
            .border(1.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("mode_indicator_chip"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        ) {}

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = textLabel,
            color = statusColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.width(6.dp))

        Icon(
            imageVector = if (isOnline) Icons.Default.SignalCellularAlt else Icons.Default.SignalCellularOff,
            contentDescription = "Signal Status",
            tint = statusColor,
            modifier = Modifier.size(14.dp)
        )
    }
}
