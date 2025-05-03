package com.example.flashmaster.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Folder operations
    suspend fun createFolder(name: String) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val folderId = UUID.randomUUID().toString()
        
        val folder = hashMapOf(
            "id" to folderId,
            "name" to name,
            "userId" to userId,
            "lastSyncTime" to System.currentTimeMillis()
        )
        
        db.collection("folders")
            .document(folderId)
            .set(folder)
            .await()
    }

    suspend fun getFolders(): List<Map<String, Any>> {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        
        return db.collection("folders")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .map { it.data ?: emptyMap() }
    }

    suspend fun deleteFolder(folderId: String) {
        db.collection("folders")
            .document(folderId)
            .delete()
            .await()
    }

    // Flashcard operations
    suspend fun createFlashcard(folderId: String, frontText: String, backText: String) {
        val flashcardId = UUID.randomUUID().toString()
        
        val flashcard = hashMapOf(
            "id" to flashcardId,
            "folderId" to folderId,
            "frontText" to frontText,
            "backText" to backText,
            "lastSyncTime" to System.currentTimeMillis()
        )
        
        db.collection("flashcards")
            .document(flashcardId)
            .set(flashcard)
            .await()
    }

    suspend fun getFlashcards(folderId: String): List<Map<String, Any>> {
        return db.collection("flashcards")
            .whereEqualTo("folderId", folderId)
            .get()
            .await()
            .documents
            .map { it.data ?: emptyMap() }
    }

    suspend fun deleteFlashcard(flashcardId: String) {
        db.collection("flashcards")
            .document(flashcardId)
            .delete()
            .await()
    }
} 