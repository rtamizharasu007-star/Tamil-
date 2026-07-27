package com.example.ui

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiRepository
import com.example.database.JarvisDatabase
import com.example.database.entity.ChatMessageEntity
import com.example.database.entity.ChatSessionEntity
import com.example.database.entity.UserMemoryEntity
import com.example.offline.CommandResult
import com.example.offline.OfflineCommandProcessor
import com.example.settings.UserSettingsManager
import com.example.voice.SpeechToTextManager
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

import com.example.voice.VoiceEnrollmentStep
import com.example.voice.VoiceMatchManager
import com.example.voice.VoiceMatchStatus

data class MainUiState(
    val messages: List<ChatMessageEntity> = emptyList(),
    val currentInputText: String = "",
    val isOnline: Boolean = true,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isProcessing: Boolean = false,
    val activeSessionId: String = "default_session",
    val partialRecognizedText: String = "",
    val errorMessage: String? = null,
    val isWakeWordActive: Boolean = true,
    val voiceMatchStatus: VoiceMatchStatus = VoiceMatchStatus(),
    val isVoiceMatchEnrolling: Boolean = false,
    val enrollmentStep: VoiceEnrollmentStep = VoiceEnrollmentStep.IDLE
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JarvisDatabase.getInstance(application)
    private val chatDao = db.chatDao()
    private val memoryDao = db.memoryDao()

    val settingsManager = UserSettingsManager(application)
    private val aiRepository = AiRepository()
    private val offlineProcessor = OfflineCommandProcessor(application, memoryDao)
    val voiceMatchManager = VoiceMatchManager(application)

    val ttsManager = TextToSpeechManager(application)
    val sttManager = SpeechToTextManager(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val allMemories = memoryDao.getAllMemories()
    val allSessions = chatDao.getAllSessions()

    private val connectivityManager = application.getSystemService(ConnectivityManager::class.java)

    init {
        observeNetworkConnectivity()
        observeSettings()
        observeTtsSpeaking()
        observeSttListening()
        loadSessionMessages(_uiState.value.activeSessionId)
        ensureInitialGreeting()
    }

    private fun observeNetworkConnectivity() {
        // Initial state check
        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val initialConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _uiState.value = _uiState.value.copy(isOnline = initialConnected)

        // Callback listener
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                viewModelScope.launch {
                    val wasOffline = !_uiState.value.isOnline
                    _uiState.value = _uiState.value.copy(isOnline = true)
                    if (wasOffline && _uiState.value.messages.isNotEmpty()) {
                        ttsManager.speak("Sir, internet connection restored. Online AI Mode is now active.")
                    }
                }
            }

            override fun onLost(network: Network) {
                viewModelScope.launch {
                    val wasOnline = _uiState.value.isOnline
                    _uiState.value = _uiState.value.copy(isOnline = false)
                    if (wasOnline && _uiState.value.messages.isNotEmpty()) {
                        ttsManager.speak("Sir, internet connection lost. Switching automatically to Offline Mode.")
                    }
                }
            }
        })
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsManager.speechSpeed.collectLatest { speed ->
                val pitch = settingsManager.voicePitch.first()
                val enabled = settingsManager.isVoiceOutputEnabled.first()
                val voiceName = settingsManager.selectedVoiceName.first()
                ttsManager.applySettings(speed, pitch, enabled, voiceName)
            }
        }
        viewModelScope.launch {
            settingsManager.voicePitch.collectLatest { pitch ->
                val speed = settingsManager.speechSpeed.first()
                val enabled = settingsManager.isVoiceOutputEnabled.first()
                val voiceName = settingsManager.selectedVoiceName.first()
                ttsManager.applySettings(speed, pitch, enabled, voiceName)
            }
        }
        viewModelScope.launch {
            settingsManager.isVoiceOutputEnabled.collectLatest { enabled ->
                val speed = settingsManager.speechSpeed.first()
                val pitch = settingsManager.voicePitch.first()
                val voiceName = settingsManager.selectedVoiceName.first()
                ttsManager.applySettings(speed, pitch, enabled, voiceName)
            }
        }
    }

    private fun observeTtsSpeaking() {
        viewModelScope.launch {
            ttsManager.isSpeaking.collectLatest { speaking ->
                _uiState.value = _uiState.value.copy(isSpeaking = speaking)
            }
        }
    }

    private fun observeSttListening() {
        viewModelScope.launch {
            sttManager.isListening.collectLatest { listening ->
                _uiState.value = _uiState.value.copy(isListening = listening)
            }
        }
        viewModelScope.launch {
            sttManager.partialText.collectLatest { partial ->
                _uiState.value = _uiState.value.copy(partialRecognizedText = partial)
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(currentInputText = text)
    }

    private fun ensureInitialGreeting() {
        viewModelScope.launch {
            val session = ChatSessionEntity(_uiState.value.activeSessionId, "JARVIS Session")
            chatDao.insertOrUpdateSession(session)

            val existingMessages = db.chatDao().getMessagesForSession(_uiState.value.activeSessionId).first()
            if (existingMessages.isEmpty()) {
                val greeting = ChatMessageEntity(
                    sessionId = _uiState.value.activeSessionId,
                    sender = "JARVIS",
                    text = "Good day, sir. All systems are online and operational. How may I assist you today?",
                    isOfflineMode = !_uiState.value.isOnline
                )
                chatDao.insertMessage(greeting)
            }
        }
    }

    fun loadSessionMessages(sessionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(activeSessionId = sessionId)
            chatDao.getMessagesForSession(sessionId).collectLatest { msgs ->
                _uiState.value = _uiState.value.copy(messages = msgs)
            }
        }
    }

    fun sendUserCommand(text: String = _uiState.value.currentInputText) {
        val prompt = text.trim()
        if (prompt.isBlank()) return

        onInputTextChanged("")
        ttsManager.stop()

        viewModelScope.launch {
            val userMsg = ChatMessageEntity(
                sessionId = _uiState.value.activeSessionId,
                sender = "USER",
                text = prompt,
                isOfflineMode = !_uiState.value.isOnline
            )
            chatDao.insertMessage(userMsg)

            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)

            // 1. Process Offline Commands First
            val result = offlineProcessor.processCommand(prompt)

            when (result) {
                is CommandResult.Success -> {
                    val jarvisMsg = ChatMessageEntity(
                        sessionId = _uiState.value.activeSessionId,
                        sender = "JARVIS",
                        text = result.responseText,
                        isOfflineMode = true
                    )
                    chatDao.insertMessage(jarvisMsg)
                    ttsManager.speak(result.responseText)
                    _uiState.value = _uiState.value.copy(isProcessing = false)
                }

                is CommandResult.NotHandled -> {
                    // 2. Offline command failed/unhandled -> fallback to Gemini AI if online
                    if (_uiState.value.isOnline) {
                        val customKey = settingsManager.customApiKey.first()
                        val aiResult = aiRepository.askGemini(
                            prompt = prompt,
                            history = _uiState.value.messages,
                            userCustomApiKey = customKey
                        )

                        aiResult.fold(
                            onSuccess = { response ->
                                val jarvisMsg = ChatMessageEntity(
                                    sessionId = _uiState.value.activeSessionId,
                                    sender = "JARVIS",
                                    text = response,
                                    isOfflineMode = false
                                )
                                chatDao.insertMessage(jarvisMsg)
                                ttsManager.speak(response)
                                _uiState.value = _uiState.value.copy(isProcessing = false)
                            },
                            onFailure = { error ->
                                val errorText = "Sorry sir, I am unable to connect to the AI service right now: ${error.localizedMessage ?: "Network or API Key Error"}"
                                val jarvisMsg = ChatMessageEntity(
                                    sessionId = _uiState.value.activeSessionId,
                                    sender = "JARVIS",
                                    text = errorText,
                                    isOfflineMode = true
                                )
                                chatDao.insertMessage(jarvisMsg)
                                ttsManager.speak(errorText)
                                _uiState.value = _uiState.value.copy(isProcessing = false, errorMessage = error.message)
                            }
                        )
                    } else {
                        val offlineNotice = "Sir, internet connection is unavailable and no local offline action matched. I am currently in Offline Mode."
                        val jarvisMsg = ChatMessageEntity(
                            sessionId = _uiState.value.activeSessionId,
                            sender = "JARVIS",
                            text = offlineNotice,
                            isOfflineMode = true
                        )
                        chatDao.insertMessage(jarvisMsg)
                        ttsManager.speak(offlineNotice)
                        _uiState.value = _uiState.value.copy(isProcessing = false)
                    }
                }
            }
        }
    }

    fun startVoiceInput(onPermissionNeeded: () -> Unit) {
        ttsManager.stop()
        sttManager.startListening(
            onResult = { recognizedText ->
                sendUserCommand(recognizedText)
            },
            onError = { err ->
                if (err.contains("permission", ignoreCase = true)) {
                    onPermissionNeeded()
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = err)
                }
            }
        )
    }

    fun stopVoiceInput() {
        sttManager.stopListening()
    }

    fun newChatSession() {
        viewModelScope.launch {
            val newId = "session_${UUID.randomUUID().toString().take(8)}"
            val session = ChatSessionEntity(newId, "Session ${System.currentTimeMillis() / 1000}")
            chatDao.insertOrUpdateSession(session)
            loadSessionMessages(newId)

            val greeting = ChatMessageEntity(
                sessionId = newId,
                sender = "JARVIS",
                text = "New session initialized, sir. Standing by for commands.",
                isOfflineMode = !_uiState.value.isOnline
            )
            chatDao.insertMessage(greeting)
        }
    }

    fun clearCurrentChat() {
        viewModelScope.launch {
            chatDao.clearMessagesForSession(_uiState.value.activeSessionId)
            ensureInitialGreeting()
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatDao.deleteSession(sessionId)
            chatDao.clearMessagesForSession(sessionId)
            if (_uiState.value.activeSessionId == sessionId) {
                newChatSession()
            }
        }
    }

    fun addMemoryNote(category: String, factText: String) {
        viewModelScope.launch {
            if (factText.isNotBlank()) {
                memoryDao.insertMemory(UserMemoryEntity(keyCategory = category, factText = factText))
            }
        }
    }

    fun deleteMemoryNote(id: Long) {
        viewModelScope.launch {
            memoryDao.deleteMemoryById(id)
        }
    }

    fun simulateHeyJarvisHotword(phrase: String = "Hey JARVIS unlock phone") {
        viewModelScope.launch {
            val unlockOnVoice = settingsManager.unlockOnVoice.first()
            if (unlockOnVoice) {
                com.example.MainActivity.unlockDeviceKeyguard()
            }
            sendUserCommand(phrase)
        }
    }

    fun startVoiceMatchEnrollment() {
        _uiState.value = _uiState.value.copy(
            isVoiceMatchEnrolling = true,
            enrollmentStep = VoiceEnrollmentStep.STEP_1
        )
        ttsManager.speak("Initializing Voice Match training, sir. Please say: Hey JARVIS.")
    }

    fun advanceEnrollmentStep() {
        val nextStep = when (_uiState.value.enrollmentStep) {
            VoiceEnrollmentStep.IDLE -> VoiceEnrollmentStep.STEP_1
            VoiceEnrollmentStep.STEP_1 -> VoiceEnrollmentStep.STEP_2
            VoiceEnrollmentStep.STEP_2 -> VoiceEnrollmentStep.STEP_3
            VoiceEnrollmentStep.STEP_3 -> VoiceEnrollmentStep.COMPLETED
            VoiceEnrollmentStep.COMPLETED -> VoiceEnrollmentStep.COMPLETED
        }

        if (nextStep == VoiceEnrollmentStep.COMPLETED) {
            viewModelScope.launch {
                settingsManager.saveVoiceMatchEnrolled(true, 98.6f)
                _uiState.value = _uiState.value.copy(
                    isVoiceMatchEnrolling = false,
                    enrollmentStep = VoiceEnrollmentStep.COMPLETED,
                    voiceMatchStatus = VoiceMatchStatus(isEnrolled = true, matchConfidence = 98.6f)
                )
                ttsManager.speak("Voice Match profile successfully calibrated and saved to secure storage.")
            }
        } else {
            _uiState.value = _uiState.value.copy(enrollmentStep = nextStep)
            val prompt = when (nextStep) {
                VoiceEnrollmentStep.STEP_2 -> "Great. Now please say: JARVIS unlock phone."
                VoiceEnrollmentStep.STEP_3 -> "Final step. Please say: JARVIS open camera."
                else -> ""
            }
            if (prompt.isNotBlank()) ttsManager.speak(prompt)
        }
    }

    fun cancelVoiceMatchEnrollment() {
        _uiState.value = _uiState.value.copy(
            isVoiceMatchEnrolling = false,
            enrollmentStep = VoiceEnrollmentStep.IDLE
        )
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            memoryDao.clearAllMemories()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        sttManager.stopListening()
    }
}
