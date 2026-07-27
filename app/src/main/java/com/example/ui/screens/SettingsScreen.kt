package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.speech.tts.Voice
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.FuturisticHudContainer
import com.example.ui.theme.ArcBorder
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcGold
import com.example.ui.theme.ArcSurface
import com.example.ui.theme.ArcSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var apiKeyInput by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }

    val savedSpeed by viewModel.settingsManager.speechSpeed.collectAsStateWithLifecycle(initialValue = 1.0f)
    val savedPitch by viewModel.settingsManager.voicePitch.collectAsStateWithLifecycle(initialValue = 1.0f)
    val savedVoiceEnabled by viewModel.settingsManager.isVoiceOutputEnabled.collectAsStateWithLifecycle(initialValue = true)
    val savedVoiceName by viewModel.settingsManager.selectedVoiceName.collectAsStateWithLifecycle(initialValue = "")

    val wakeWordEnabled by viewModel.settingsManager.isWakeWordEnabled.collectAsStateWithLifecycle(initialValue = true)
    val showOnLockScreen by viewModel.settingsManager.showOnLockScreen.collectAsStateWithLifecycle(initialValue = true)
    val unlockOnVoice by viewModel.settingsManager.unlockOnVoice.collectAsStateWithLifecycle(initialValue = true)
    val autoOpenOnWake by viewModel.settingsManager.autoOpenOnWake.collectAsStateWithLifecycle(initialValue = true)
    val isVoiceMatchEnrolled by viewModel.settingsManager.isVoiceMatchEnrolled.collectAsStateWithLifecycle(initialValue = true)
    val voiceMatchScore by viewModel.settingsManager.voiceMatchScore.collectAsStateWithLifecycle(initialValue = 98.5f)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var currentSpeed by remember { mutableFloatStateOf(1.0f) }
    var currentPitch by remember { mutableFloatStateOf(1.0f) }
    var voiceEnabled by remember { mutableStateOf(true) }

    val availableVoices = remember { viewModel.ttsManager.getAvailableVoices() }
    var voiceDropdownExpanded by remember { mutableStateOf(false) }
    var selectedVoiceText by remember { mutableStateOf("Default System Voice") }

    LaunchedEffect(Unit) {
        apiKeyInput = viewModel.settingsManager.customApiKey.first()
        currentSpeed = savedSpeed
        currentPitch = savedPitch
        voiceEnabled = savedVoiceEnabled
    }

    LaunchedEffect(savedVoiceName, availableVoices) {
        if (savedVoiceName.isNotEmpty()) {
            val matched = availableVoices.firstOrNull { it.name == savedVoiceName }
            if (matched != null) {
                selectedVoiceText = matched.name
            }
        }
    }

    // Permissions check
    val hasMicPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    val hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    FuturisticHudContainer(modifier = modifier.testTag("settings_screen")) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "SYSTEM CONFIGURATION",
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 1. API KEY SETTINGS CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArcSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ArcBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = "API Key", tint = ArcGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GEMINI AI API KEY",
                                color = ArcGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Configure a custom Gemini API key for Online Mode. If left blank, JARVIS will use the default system environment API key.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("api_key_input"),
                            label = { Text("Gemini API Key", color = TextSecondary) },
                            singleLine = true,
                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = "Toggle Key Visibility",
                                        tint = if (isKeyVisible) ArcCyan else TextSecondary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = ArcBorder.copy(alpha = 0.5f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.settingsManager.saveCustomApiKey(apiKeyInput.trim())
                                    Toast.makeText(context, "API Key updated successfully, sir.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan, contentColor = ArcSurface),
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("save_api_key_button")
                        ) {
                            Text("SAVE KEY", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 2. VOICE SETTINGS CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArcSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ArcBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = "Voice Settings", tint = ArcCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VOCAL SYNTHESIZER SETTINGS",
                                color = ArcCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Enable/Disable Voice Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Enable Voice Output", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("JARVIS speaks responses aloud", color = TextSecondary, fontSize = 12.sp)
                            }
                            Switch(
                                checked = voiceEnabled,
                                onCheckedChange = { checked ->
                                    voiceEnabled = checked
                                    scope.launch {
                                        viewModel.settingsManager.saveVoiceEnabled(checked)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ArcCyan,
                                    checkedTrackColor = ArcSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Speech Speed Slider
                        Text(
                            text = "Speech Speed: ${String.format("%.1fx", currentSpeed)}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = currentSpeed,
                            onValueChange = { currentSpeed = it },
                            onValueChangeFinished = {
                                scope.launch { viewModel.settingsManager.saveSpeechSpeed(currentSpeed) }
                            },
                            valueRange = 0.5f..2.0f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = ArcCyan,
                                activeTrackColor = ArcCyan
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Voice Pitch Slider
                        Text(
                            text = "Voice Pitch: ${String.format("%.1fx", currentPitch)}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = currentPitch,
                            onValueChange = { currentPitch = it },
                            onValueChangeFinished = {
                                scope.launch { viewModel.settingsManager.saveVoicePitch(currentPitch) }
                            },
                            valueRange = 0.5f..2.0f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = ArcGold,
                                activeTrackColor = ArcGold
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Test Voice Button
                        Button(
                            onClick = {
                                viewModel.ttsManager.speak("Good day, sir. Vocal synthesizer test complete.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcSurfaceVariant, contentColor = ArcCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_voice_button")
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TEST JARVIS VOICE")
                        }
                    }
                }

                // 3. LOCK SCREEN & VOICE MATCH CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArcSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ArcBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhonelinkLock, contentDescription = "Lock Screen & Voice Match", tint = ArcGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LOCK SCREEN & VOICE MATCH",
                                color = ArcGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Voice Match Profile Status
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ArcSurfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .border(1.dp, if (isVoiceMatchEnrolled) OnlineGreen else ArcGold, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = if (isVoiceMatchEnrolled) OnlineGreen else ArcGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isVoiceMatchEnrolled) "VOICE PRINT ENROLLED" else "NOT ENROLLED",
                                            color = if (isVoiceMatchEnrolled) OnlineGreen else ArcGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Match Confidence: ${String.format("%.1f%%", voiceMatchScore)}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.startVoiceMatchEnrollment() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isVoiceMatchEnrolled) ArcSurfaceVariant else ArcGold,
                                        contentColor = if (isVoiceMatchEnrolled) ArcCyan else ArcSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("train_voice_match_button")
                                ) {
                                    Text(
                                        text = if (isVoiceMatchEnrolled) "RE-TRAIN" else "TRAIN VOICE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // "Hey JARVIS" Wake Word Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("\"Hey JARVIS\" Wake Word", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Auto wake on hotword detection", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = wakeWordEnabled,
                                onCheckedChange = { checked ->
                                    scope.launch { viewModel.settingsManager.saveWakeWordEnabled(checked) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ArcGold, checkedTrackColor = ArcSurfaceVariant),
                                modifier = Modifier.testTag("wake_word_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Show Over Phone Lock Screen Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Show On Lock Screen", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Display JARVIS UI on keyguard lock screen", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = showOnLockScreen,
                                onCheckedChange = { checked ->
                                    scope.launch { viewModel.settingsManager.saveShowOnLockScreen(checked) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ArcCyan, checkedTrackColor = ArcSurfaceVariant),
                                modifier = Modifier.testTag("show_lock_screen_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Unlock Device Keyguard on Voice Match Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Unlock Phone on Voice Match", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Dismiss keyguard when enrolled voice is verified", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = unlockOnVoice,
                                onCheckedChange = { checked ->
                                    scope.launch { viewModel.settingsManager.saveUnlockOnVoice(checked) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = OnlineGreen, checkedTrackColor = ArcSurfaceVariant),
                                modifier = Modifier.testTag("unlock_on_voice_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Auto-Open App on "Hey JARVIS"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto Open on \"Hey JARVIS\"", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Bring app to foreground automatically", color = TextSecondary, fontSize = 11.sp)
                            }
                            Switch(
                                checked = autoOpenOnWake,
                                onCheckedChange = { checked ->
                                    scope.launch { viewModel.settingsManager.saveAutoOpenOnWake(checked) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = ArcCyan, checkedTrackColor = ArcSurfaceVariant),
                                modifier = Modifier.testTag("auto_open_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Test Lock Screen Unlock Action
                        Button(
                            onClick = {
                                viewModel.simulateHeyJarvisHotword("Hey JARVIS unlock phone")
                                Toast.makeText(context, "Executing 'Hey JARVIS unlock phone' & Keyguard Dismiss!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcSurfaceVariant, contentColor = ArcGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_lock_unlock_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SIMULATE 'HEY JARVIS UNLOCK PHONE'")
                        }
                    }
                }

                // 3. HARDWARE PERMISSIONS STATUS CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArcSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ArcBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = "Permissions", tint = ArcCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM PERMISSIONS STATUS",
                                color = ArcCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        PermissionRow(
                            label = "Microphone Access (Voice Input)",
                            isGranted = hasMicPermission
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        PermissionRow(
                            label = "Camera Hardware (Flashlight)",
                            isGranted = hasCameraPermission
                        )
                    }
                }
            }
        }

        if (uiState.isVoiceMatchEnrolling) {
            val stepProgress = when (uiState.enrollmentStep) {
                com.example.voice.VoiceEnrollmentStep.STEP_1 -> 0.33f
                com.example.voice.VoiceEnrollmentStep.STEP_2 -> 0.66f
                com.example.voice.VoiceEnrollmentStep.STEP_3 -> 1.0f
                else -> 0.0f
            }

            val stepPhrase = when (uiState.enrollmentStep) {
                com.example.voice.VoiceEnrollmentStep.STEP_1 -> "\"Hey JARVIS\""
                com.example.voice.VoiceEnrollmentStep.STEP_2 -> "\"JARVIS unlock phone\""
                com.example.voice.VoiceEnrollmentStep.STEP_3 -> "\"JARVIS open camera\""
                else -> ""
            }

            AlertDialog(
                onDismissRequest = { viewModel.cancelVoiceMatchEnrollment() },
                containerColor = ArcSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = ArcGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VOICE MATCH CALIBRATION",
                            color = ArcGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "To enroll your acoustic voice print profile for keyguard lock screen unlock and hotword activation, please speak the requested phrase clearly into your microphone:",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        LinearProgressIndicator(
                            progress = { stepProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = ArcGold,
                            trackColor = ArcSurfaceVariant,
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ArcSurfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, ArcGold, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "PHRASE TO SPEAK:",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stepPhrase,
                                    color = ArcGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.advanceEnrollmentStep() },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcGold, contentColor = ArcSurface)
                    ) {
                        Text("SPEAK / VERIFY STEP", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.cancelVoiceMatchEnrollment() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("CANCEL")
                    }
                }
            )
        }
    }
}

@Composable
private fun PermissionRow(label: String, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ArcSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextPrimary, fontSize = 12.sp)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isGranted) OnlineGreen else ErrorRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isGranted) "GRANTED" else "MISSING",
                color = if (isGranted) OnlineGreen else ErrorRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
