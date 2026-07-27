package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.entity.UserMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM user_memory ORDER BY createdAt DESC")
    fun getAllMemories(): Flow<List<UserMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: UserMemoryEntity): Long

    @Query("SELECT * FROM user_memory WHERE factText LIKE '%' || :query || '%' OR keyCategory LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<UserMemoryEntity>

    @Query("DELETE FROM user_memory WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM user_memory")
    suspend fun clearAllMemories()
}
