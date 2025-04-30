package com.example.flashmaster.FoldersPart.New

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class FlashcardFolder(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String,
    val createdAt: Timestamp = Timestamp.now(),
    val cardCount: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "name" to name,
            "createdAt" to createdAt,
            "cardCount" to cardCount
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): FlashcardFolder {
            return FlashcardFolder(
                id = id,
                userId = map["userId"] as String,
                name = map["name"] as String,
                createdAt = map["createdAt"] as Timestamp,
                cardCount = (map["cardCount"] as Long).toInt()
            )
        }
    }
}
