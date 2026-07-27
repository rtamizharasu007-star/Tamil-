package com.example.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_memory")
data class UserMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyCategory: String, // e.g. "project", "preference", "note", "general"
    val factText: String,
    val createdAt: Long = System.currentTimeMillis()
)
