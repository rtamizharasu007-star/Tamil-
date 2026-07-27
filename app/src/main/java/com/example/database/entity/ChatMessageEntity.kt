package com.example.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String = "default_session",
    val sender: String, // "USER" or "JARVIS"
    val text: String,
    val isOfflineMode: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
