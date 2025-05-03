package com.example.flashmaster.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE folderId = :folderId")
    fun getFlashcardsByFolder(folderId: String): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>)

    @Query("DELETE FROM flashcards WHERE folderId = :folderId")
    suspend fun deleteFlashcardsByFolder(folderId: String)

    @Query("DELETE FROM flashcards WHERE id = :flashcardId")
    suspend fun deleteFlashcard(flashcardId: String)
} 