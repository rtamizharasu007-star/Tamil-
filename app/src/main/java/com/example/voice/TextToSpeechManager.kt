package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    private var pendingSpeed: Float = 1.0f
    private var pendingPitch: Float = 1.0f
    private var pendingEnabled: Boolean = true
    private var pendingVoiceName: String = ""

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { t ->
                val result = t.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTSManager", "US English Language is not supported")
                } else {
                    _isReady.value = true
                    applySettings(pendingSpeed, pendingPitch, pendingEnabled, pendingVoiceName)
                }

                t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        } else {
            Log.e("TTSManager", "TextToSpeech Initialization failed with status: $status")
        }
    }

    fun applySettings(speed: Float, pitch: Float, enabled: Boolean, voiceName: String = "") {
        pendingSpeed = speed
        pendingPitch = pitch
        pendingEnabled = enabled
        pendingVoiceName = voiceName

        if (_isReady.value) {
            tts?.let { t ->
                t.setSpeechRate(speed)
                t.setPitch(pitch)

                if (voiceName.isNotEmpty()) {
                    val voices = t.voices
                    val matchedVoice = voices?.firstOrNull { it.name == voiceName }
                    if (matchedVoice != null) {
                        t.voice = matchedVoice
                    }
                }
            }
        }
    }

    fun speak(text: String) {
        if (!pendingEnabled || !_isReady.value) return
        stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_UTTERANCE_${System.currentTimeMillis()}")
    }

    fun stop() {
        if (_isSpeaking.value) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun getAvailableVoices(): List<Voice> {
        return tts?.voices?.filter { !it.isNetworkConnectionRequired }?.toList() ?: emptyList()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
