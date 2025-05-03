package com.example.flashmaster.data

import android.content.Context
import com.example.flashmaster.data.local.AppDatabase
import com.example.flashmaster.data.local.FlashcardEntity
import com.example.flashmaster.data.local.FolderEntity
import com.example.flashmaster.data.local.LocalRepository
import com.example.flashmaster.data.remote.FirebaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlashcardRepository(context: Context) {
    internal val localRepository = LocalRepository(AppDatabase.getDatabase(context))
    internal val firebaseRepository = FirebaseRepository()

    // Folder operations
    fun getFolders(userId: String): Flow<List<FolderEntity>> {
        return localRepository.getFolders(userId)
    }

    suspend fun createFolder(name: String) {
        try {
            // Try to create in Firebase first
            firebaseRepository.createFolder(name)
            
            // If successful, sync with local database
            val folders = firebaseRepository.getFolders()
            val folderEntities = folders.map { data ->
                FolderEntity(
                    id = data["id"] as String,
                    name = data["name"] as String,
                    userId = data["userId"] as String,
                    lastSyncTime = (data["lastSyncTime"] as? Long) ?: System.currentTimeMillis()
                )
            }
            localRepository.insertFolders(folderEntities)
        } catch (e: Exception) {
            // If offline, create only in local database
            val folderId = java.util.UUID.randomUUID().toString()
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid 
                ?: throw Exception("User not logged in")
            
            val folder = FolderEntity(
                id = folderId,
                name = name,
                userId = userId
            )
            localRepository.insertFolder(folder)
        }
    }

    suspend fun deleteFolder(folderId: String) {
        try {
            firebaseRepository.deleteFolder(folderId)
        } catch (e: Exception) {
            // If offline, just delete from local
        }
        localRepository.deleteFolder(folderId)
    }

    // Flashcard operations
    fun getFlashcards(folderId: String): Flow<List<FlashcardEntity>> {
        return localRepository.getFlashcards(folderId)
    }

    suspend fun createFlashcard(folderId: String, frontText: String, backText: String) {
        try {
            // Try to create in Firebase first
            firebaseRepository.createFlashcard(folderId, frontText, backText)
            
            // If successful, sync with local database
            val flashcards = firebaseRepository.getFlashcards(folderId)
            val flashcardEntities = flashcards.map { data ->
                FlashcardEntity(
                    id = data["id"] as String,
                    folderId = data["folderId"] as String,
                    frontText = data["frontText"] as String,
                    backText = data["backText"] as String,
                    lastSyncTime = (data["lastSyncTime"] as? Long) ?: System.currentTimeMillis()
                )
            }
            localRepository.insertFlashcards(flashcardEntities)
        } catch (e: Exception) {
            // If offline, create only in local database
            val flashcardId = java.util.UUID.randomUUID().toString()
            
            val flashcard = FlashcardEntity(
                id = flashcardId,
                folderId = folderId,
                frontText = frontText,
                backText = backText
            )
            localRepository.insertFlashcard(flashcard)
        }
    }

    suspend fun deleteFlashcard(flashcardId: String) {
        try {
            firebaseRepository.deleteFlashcard(flashcardId)
        } catch (e: Exception) {
            // If offline, just delete from local
        }
        // Note: You'll need to add a method to delete a specific flashcard in LocalRepository
    }
} 