package com.example.ai

import com.example.BuildConfig
import com.example.database.entity.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiRepository {

    suspend fun askGemini(
        prompt: String,
        history: List<ChatMessageEntity>,
        userCustomApiKey: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = when {
            userCustomApiKey.isNotBlank() -> userCustomApiKey
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> BuildConfig.GEMINI_API_KEY
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Please enter your API key in Settings."))
        }

        val systemInstruction = GeminiContent(
            parts = listOf(
                GeminiPart("You are JARVIS, an advanced, highly intelligent AI assistant inspired by Iron Man. Be polite, concise, and helpful. Address the user as 'sir' appropriately. Answer questions on programming (Python, Kotlin, Web), engineering, mathematics, translations, and general knowledge accurately.")
            )
        )

        // Build recent conversation turns (up to 6 turns for context)
        val conversationTurns = mutableListOf<GeminiContent>()
        val recentHistory = history.takeLast(6)
        recentHistory.forEach { msg ->
            val role = if (msg.sender == "USER") "user" else "model"
            conversationTurns.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(msg.text))
                )
            )
        }

        // Add current prompt
        conversationTurns.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(prompt))
            )
        )

        val request = GeminiRequest(
            contents = conversationTurns,
            systemInstruction = systemInstruction
        )

        return@withContext try {
            val response = GeminiClient.service.generateContent(apiKey, request)
            val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!candidateText.isNullOrBlank()) {
                Result.success(candidateText)
            } else {
                Result.failure(Exception("Empty response received from AI service."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
