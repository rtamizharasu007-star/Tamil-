package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.ArcReactorState
import com.example.ui.components.ArcReactorView
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.FuturisticHudContainer
import com.example.ui.components.ModeIndicatorChip
import com.example.ui.theme.ArcBlueDark
import com.example.ui.theme.ArcBorder
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcSurface
import com.example.ui.theme.ArcSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAssistantScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Permission launcher for Microphone
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput(onPermissionNeeded = {})
        } else {
            Toast.makeText(context, "Microphone permission is required for voice commands.", Toast.LENGTH_LONG).show()
        }
    }

    // Scroll to latest message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Determine Arc Reactor animation state
    val reactorState = when {
        uiState.isListening -> ArcReactorState.LISTENING
        uiState.isProcessing -> ArcReactorState.PROCESSING
        uiState.isSpeaking -> ArcReactorState.SPEAKING
        else -> ArcReactorState.IDLE
    }

    FuturisticHudContainer(modifier = modifier.testTag("main_assistant_screen")) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "J.A.R.V.I.S.",
                                color = ArcCyan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "MARK VII PROTOCOL",
                                color = ArcGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onNavigateToMemory,
                            modifier = Modifier.testTag("memory_nav_button")
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = "Memory System", tint = ArcCyan)
                        }

                        IconButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.testTag("history_nav_button")
                        ) {
                            Icon(Icons.Default.History, contentDescription = "Chat History", tint = ArcCyan)
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.testTag("settings_nav_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ArcCyan)
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
                    .padding(horizontal = 12.dp)
            ) {
                // Header Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ModeIndicatorChip(
                            isOnline = uiState.isOnline,
                            onClick = {
                                val statusMsg = if (uiState.isOnline) "ONLINE MODE: Connected to Gemini AI Service." else "OFFLINE MODE: Running local hardware commands."
                                Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                            }
                        )

                        // Voice Match & Hotword Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ArcSurface)
                                .border(1.dp, ArcGold, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.testTag("hotword_wake_chip")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Voice Match Active",
                                    tint = ArcGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "HEY JARVIS",
                                    color = ArcGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    if (uiState.isProcessing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ArcCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ANALYZING...",
                                color = ArcCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Arc Reactor Visual Animation HUD Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ArcSurface.copy(alpha = 0.6f))
                        .border(1.dp, ArcBorder.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ArcReactorView(
                            state = reactorState,
                            size = 120.dp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val stateLabel = when (reactorState) {
                            ArcReactorState.LISTENING -> "LISTENING TO VOICE COMMAND..."
                            ArcReactorState.PROCESSING -> "PROCESSING QUERY VIA ROUTER..."
                            ArcReactorState.SPEAKING -> "JARVIS VOCALIZING RESPONSE..."
                            ArcReactorState.IDLE -> "SYSTEMS NOMINAL • STANDING BY"
                        }

                        Text(
                            text = stateLabel,
                            color = when (reactorState) {
                                ArcReactorState.LISTENING -> ArcGold
                                ArcReactorState.PROCESSING -> ArcCyan
                                ArcReactorState.SPEAKING -> Color(0xFF00FF88)
                                ArcReactorState.IDLE -> TextSecondary
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Partial voice output display
                AnimatedVisibility(
                    visible = uiState.isListening && uiState.partialRecognizedText.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(ArcGold.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, ArcGold, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Hearing: \"${uiState.partialRecognizedText}\"",
                            color = ArcGold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Messages Feed
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { msg ->
                        ChatMessageBubble(message = msg)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Command Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.currentInputText,
                        onValueChange = { viewModel.onInputTextChanged(it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("command_input_field"),
                        placeholder = {
                            Text(
                                text = "Enter command or ask JARVIS...",
                                color = TextSecondary.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        },
                        trailingIcon = {
                            if (uiState.currentInputText.isNotBlank()) {
                                IconButton(
                                    onClick = { viewModel.sendUserCommand() },
                                    modifier = Modifier.testTag("send_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send Command",
                                        tint = ArcCyan
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.sendUserCommand() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcCyan,
                            unfocusedBorderColor = ArcBorder.copy(alpha = 0.5f),
                            focusedContainerColor = ArcSurfaceVariant,
                            unfocusedContainerColor = ArcSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Microphone Floating Voice Command Button
                    FloatingActionButton(
                        onClick = {
                            if (uiState.isListening) {
                                viewModel.stopVoiceInput()
                            } else {
                                val hasMicPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasMicPermission) {
                                    viewModel.startVoiceInput(onPermissionNeeded = {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    })
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        containerColor = if (uiState.isListening) ArcGold else ArcCyan,
                        contentColor = ArcBlueDark,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("microphone_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Microphone Voice Command",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
