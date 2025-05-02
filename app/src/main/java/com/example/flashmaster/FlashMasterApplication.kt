package com.example.flashmaster

import android.app.Application
import com.example.flashmaster.data.FlashcardRepository
import com.example.flashmaster.data.SyncManager
import com.example.flashmaster.utils.NetworkUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FlashMasterApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    lateinit var flashcardRepository: FlashcardRepository
        private set
    
    lateinit var networkUtils: NetworkUtils
        private set
    
    lateinit var syncManager: SyncManager
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Initialize components
        flashcardRepository = FlashcardRepository(this)
        networkUtils = NetworkUtils(this)
        syncManager = SyncManager(
            flashcardRepository.localRepository,
            flashcardRepository.firebaseRepository
        )
        
        // Observe network state and sync when online
        networkUtils.networkState
            .onEach { isOnline ->
                if (isOnline) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        syncManager.syncAllData(userId)
                    }
                }
            }
            .launchIn(applicationScope)
    }
} 