package com.example.flashmaster.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey
    val id: String,
    val folderId: String,
    val frontText: String,
    val backText: String,
    val lastSyncTime: Long = System.currentTimeMillis()
) 