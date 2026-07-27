package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.database.entity.ChatSessionEntity
import com.example.ui.MainViewModel
import com.example.ui.components.FuturisticHudContainer
import com.example.ui.theme.ArcBorder
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcSurface
import com.example.ui.theme.ArcSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle(initialValue = emptyList())

    FuturisticHudContainer(modifier = modifier.testTag("history_screen")) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "CONVERSATION HISTORY",
                            color = ArcCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ArcCyan)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.clearCurrentChat() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Current Session", tint = ErrorRed)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ArcSurface.copy(alpha = 0.85f)
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // New Chat Button
                Button(
                    onClick = {
                        viewModel.newChatSession()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = ArcSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("new_chat_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("START NEW CONVERSATION SESSION", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "SAVED CONVERSATION SESSIONS",
                    color = ArcGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                if (sessions.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "NO CONVERSATION HISTORY YET",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sessions, key = { it.sessionId }) { session ->
                            SessionItemCard(
                                session = session,
                                isActive = session.sessionId == uiState.activeSessionId,
                                onSelect = {
                                    viewModel.loadSessionMessages(session.sessionId)
                                    onBack()
                                },
                                onDelete = { viewModel.deleteSession(session.sessionId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionItemCard(
    session: ChatSessionEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val timeStr = remember(session.lastUpdatedAt) {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        sdf.format(Date(session.lastUpdatedAt))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) ArcSurfaceVariant else ArcSurface
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) ArcCyan else ArcBorder.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onSelect() }
            .testTag("session_item_${session.sessionId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = if (isActive) ArcCyan else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isActive) "ACTIVE SESSION: ${session.title}" else session.title,
                        color = if (isActive) ArcCyan else TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Last updated: $timeStr",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Session",
                    tint = ErrorRed.copy(alpha = 0.8f)
                )
            }
        }
    }
}
