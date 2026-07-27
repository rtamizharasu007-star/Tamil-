package com.example.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "jarvis_settings")

class UserSettingsManager(private val context: Context) {

    companion object {
        private val KEY_CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        private val KEY_SPEECH_SPEED = floatPreferencesKey("speech_speed")
        private val KEY_VOICE_PITCH = floatPreferencesKey("voice_pitch")
        private val KEY_VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        private val KEY_SELECTED_VOICE = stringPreferencesKey("selected_voice")
        private val KEY_WAKE_WORD_ENABLED = booleanPreferencesKey("wake_word_enabled")
        private val KEY_VOICE_MATCH_ENROLLED = booleanPreferencesKey("voice_match_enrolled")
        private val KEY_VOICE_MATCH_SCORE = floatPreferencesKey("voice_match_score")
        private val KEY_SHOW_ON_LOCK_SCREEN = booleanPreferencesKey("show_on_lock_screen")
        private val KEY_UNLOCK_ON_VOICE = booleanPreferencesKey("unlock_on_voice")
        private val KEY_AUTO_OPEN_ON_WAKE = booleanPreferencesKey("auto_open_on_wake")
    }

    val customApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_API_KEY] ?: ""
    }

    val speechSpeed: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_SPEECH_SPEED] ?: 1.0f
    }

    val voicePitch: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_PITCH] ?: 1.0f
    }

    val isVoiceOutputEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_ENABLED] ?: true
    }

    val selectedVoiceName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_VOICE] ?: ""
    }

    val isWakeWordEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WAKE_WORD_ENABLED] ?: true
    }

    val isVoiceMatchEnrolled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_MATCH_ENROLLED] ?: true
    }

    val voiceMatchScore: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOICE_MATCH_SCORE] ?: 98.5f
    }

    val showOnLockScreen: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_ON_LOCK_SCREEN] ?: true
    }

    val unlockOnVoice: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_UNLOCK_ON_VOICE] ?: true
    }

    val autoOpenOnWake: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_OPEN_ON_WAKE] ?: true
    }

    suspend fun saveCustomApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_API_KEY] = key
        }
    }

    suspend fun saveSpeechSpeed(speed: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SPEECH_SPEED] = speed
        }
    }

    suspend fun saveVoicePitch(pitch: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VOICE_PITCH] = pitch
        }
    }

    suspend fun saveVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VOICE_ENABLED] = enabled
        }
    }

    suspend fun saveSelectedVoice(voiceName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_VOICE] = voiceName
        }
    }

    suspend fun saveWakeWordEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WAKE_WORD_ENABLED] = enabled
        }
    }

    suspend fun saveVoiceMatchEnrolled(enrolled: Boolean, score: Float = 98.5f) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VOICE_MATCH_ENROLLED] = enrolled
            prefs[KEY_VOICE_MATCH_SCORE] = score
        }
    }

    suspend fun saveShowOnLockScreen(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_ON_LOCK_SCREEN] = enabled
        }
    }

    suspend fun saveUnlockOnVoice(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_UNLOCK_ON_VOICE] = enabled
        }
    }

    suspend fun saveAutoOpenOnWake(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_OPEN_ON_WAKE] = enabled
        }
    }
}
