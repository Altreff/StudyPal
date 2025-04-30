package com.example.flashmaster.FoldersPart.New

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Flashcard(
    @DocumentId
    val id: String = "",
    val folderId: String = "",
    val frontText: String = "",
    val backText: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val lastModified: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "folderId" to folderId,
            "frontText" to frontText,
            "backText" to backText,
            "createdAt" to createdAt,
            "lastModified" to lastModified
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): Flashcard {
            return Flashcard(
                id = id,
                folderId = map["folderId"] as String,
                frontText = map["frontText"] as String,
                backText = map["backText"] as String,
                createdAt = map["createdAt"] as Timestamp,
                lastModified = map["lastModified"] as Timestamp
            )
        }
    }
} 