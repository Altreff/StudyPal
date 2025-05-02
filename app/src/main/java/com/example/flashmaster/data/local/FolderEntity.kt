package com.example.flashmaster.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val userId: String,
    val lastSyncTime: Long = System.currentTimeMillis()
) 