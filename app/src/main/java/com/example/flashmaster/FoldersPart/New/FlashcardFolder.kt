package com.example.flashmaster.FoldersPart.New

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class FlashcardFolder(
    val id: String,
    val name: String,
    val userId: String,
    val createdAt: Timestamp,
    val cardCount: Int,
    val originalFolderId: String? = null
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "userId" to userId,
            "createdAt" to createdAt,
            "cardCount" to cardCount
        ).let { map ->
            if (originalFolderId != null) {
                map + ("originalFolderId" to originalFolderId)
            } else {
                map
            }
        }
    }

    companion object {
        fun fromMap(id: String, data: Map<String, Any>): FlashcardFolder {
            return FlashcardFolder(
                id = id,
                name = data["name"] as String,
                userId = data["userId"] as String,
                createdAt = data["createdAt"] as Timestamp,
                cardCount = (data["cardCount"] as? Number)?.toInt() ?: 0,
                originalFolderId = data["originalFolderId"] as? String
            )
        }
    }
}
