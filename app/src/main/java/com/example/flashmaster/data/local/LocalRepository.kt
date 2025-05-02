package com.example.flashmaster.data.local

import kotlinx.coroutines.flow.Flow

class LocalRepository(private val database: AppDatabase) {
    private val folderDao = database.folderDao()
    private val flashcardDao = database.flashcardDao()

    // Folder operations
    fun getFolders(userId: String): Flow<List<FolderEntity>> {
        return folderDao.getFoldersByUser(userId)
    }

    suspend fun insertFolder(folder: FolderEntity) {
        folderDao.insertFolder(folder)
    }

    suspend fun insertFolders(folders: List<FolderEntity>) {
        folderDao.insertFolders(folders)
    }

    suspend fun deleteFolder(folderId: String) {
        folderDao.deleteFolder(folderId)
    }

    // Flashcard operations
    fun getFlashcards(folderId: String): Flow<List<FlashcardEntity>> {
        return flashcardDao.getFlashcardsByFolder(folderId)
    }

    suspend fun insertFlashcard(flashcard: FlashcardEntity) {
        flashcardDao.insertFlashcard(flashcard)
    }

    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>) {
        flashcardDao.insertFlashcards(flashcards)
    }

    suspend fun deleteFlashcardsByFolder(folderId: String) {
        flashcardDao.deleteFlashcardsByFolder(folderId)
    }

    suspend fun deleteFlashcard(flashcardId: String) {
        flashcardDao.deleteFlashcard(flashcardId)
    }
} 