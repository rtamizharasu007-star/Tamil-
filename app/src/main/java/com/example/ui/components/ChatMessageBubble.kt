package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.entity.ChatMessageEntity
import com.example.ui.theme.ArcBorder
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcSurface
import com.example.ui.theme.ArcSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUser = message.sender == "USER"

    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) ArcSurfaceVariant else ArcSurface
    val borderColor = if (isUser) ArcGold.copy(alpha = 0.5f) else ArcBorder.copy(alpha = 0.6f)
    val accentColor = if (isUser) ArcGold else ArcCyan

    val timeStr = rememberFormattedTime(message.timestamp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "JARVIS",
                    tint = ArcCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "JARVIS HUD",
                    color = ArcCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Text(
                    text = "USER COMMAND",
                    color = ArcGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = ArcGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    )
                )
                .padding(12.dp)
                .testTag("chat_message_bubble_${message.id}")
        ) {
            Column {
                Text(
                    text = message.text,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$timeStr ${if (message.isOfflineMode) "• OFFLINE" else "• ONLINE"}",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("JARVIS Message", message.text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard, sir.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Text",
                            tint = accentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberFormattedTime(timestamp: Long): String {
    return androidx.compose.runtime.remember(timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    }
}
