package com.example.flashmaster.data

import com.example.flashmaster.data.local.FlashcardEntity
import com.example.flashmaster.data.local.FolderEntity
import com.example.flashmaster.data.local.LocalRepository
import com.example.flashmaster.data.remote.FirebaseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SyncManager(
    private val localRepository: LocalRepository,
    private val firebaseRepository: FirebaseRepository
) {
    suspend fun syncFolders(userId: String) {
        try {
            // Get folders from Firebase
            val remoteFolders = firebaseRepository.getFolders()
            
            // Convert to entities
            val folderEntities = remoteFolders.map { data ->
                FolderEntity(
                    id = data["id"] as String,
                    name = data["name"] as String,
                    userId = data["userId"] as String,
                    lastSyncTime = (data["lastSyncTime"] as? Long) ?: System.currentTimeMillis()
                )
            }
            
            // Update local database
            localRepository.insertFolders(folderEntities)
        } catch (e: Exception) {
            // Handle sync error
            e.printStackTrace()
        }
    }

    suspend fun syncFlashcards(folderId: String) {
        try {
            // Get flashcards from Firebase
            val remoteFlashcards = firebaseRepository.getFlashcards(folderId)
            
            // Convert to entities
            val flashcardEntities = remoteFlashcards.map { data ->
                FlashcardEntity(
                    id = data["id"] as String,
                    folderId = data["folderId"] as String,
                    frontText = data["frontText"] as String,
                    backText = data["backText"] as String,
                    lastSyncTime = (data["lastSyncTime"] as? Long) ?: System.currentTimeMillis()
                )
            }
            
            // Update local database
            localRepository.insertFlashcards(flashcardEntities)
        } catch (e: Exception) {
            // Handle sync error
            e.printStackTrace()
        }
    }

    suspend fun syncAllData(userId: String) {
        syncFolders(userId)
        
        // Get all folders and sync their flashcards
        val folders = localRepository.getFolders(userId).first()
        folders.forEach { folder ->
            syncFlashcards(folder.id)
        }
    }
} 