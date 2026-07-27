package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.entity.ChatMessageEntity
import com.example.database.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearMessagesForSession(sessionId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()

    @Query("SELECT * FROM chat_sessions ORDER BY lastUpdatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: ChatSessionEntity)

    @Query("DELETE FROM chat_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<ChatMessageEntity>>
}
