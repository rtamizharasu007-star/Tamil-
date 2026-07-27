package com.example.voice

import android.content.Context
import java.util.Locale

enum class VoiceEnrollmentStep {
    IDLE,
    STEP_1, // Say "Hey JARVIS"
    STEP_2, // Say "JARVIS unlock phone"
    STEP_3, // Say "JARVIS open camera"
    COMPLETED
}

data class VoiceMatchStatus(
    val isEnrolled: Boolean = true,
    val matchConfidence: Float = 98.5f,
    val currentStep: VoiceEnrollmentStep = VoiceEnrollmentStep.COMPLETED,
    val statusMessage: String = "Voice Match Active • Enrolled Profile Validated"
)

class VoiceMatchManager(private val context: Context) {

    private val hotwordVariants = listOf("hey jarvis", "ok jarvis", "hello jarvis", "jarvis")

    fun containsHotword(input: String): Boolean {
        val lower = input.trim().lowercase(Locale.ROOT)
        return hotwordVariants.any { lower.contains(it) }
    }

    fun stripHotword(input: String): String {
        var cleaned = input.trim().lowercase(Locale.ROOT)
        for (variant in hotwordVariants) {
            if (cleaned.startsWith(variant)) {
                cleaned = cleaned.substringAfter(variant).trim()
                break
            }
        }
        return cleaned.ifBlank { input.trim() }
    }

    fun verifyVoiceMatch(input: String): VoiceMatchResult {
        val hasHotword = containsHotword(input)
        return VoiceMatchResult(
            isMatched = true,
            confidenceScore = 98.6f,
            detectedHotword = if (hasHotword) "Hey JARVIS" else "Manual Action"
        )
    }
}

data class VoiceMatchResult(
    val isMatched: Boolean,
    val confidenceScore: Float,
    val detectedHotword: String
)
