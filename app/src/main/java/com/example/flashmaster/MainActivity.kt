package com.example.flashmaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import com.example.flashmaster.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.example.flashmaster.FoldersPart.New.FlashcardFolder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.flashmaster.data.local.FolderEntity
import com.example.flashmaster.data.local.FlashcardEntity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var pendingDeepLink: Intent? = null
    private var navController: NavController? = null
    private var pendingFolderId: String? = null

    // Offline support components
    private val repository by lazy { (application as FlashMasterApp).flashcardRepository }
    private val networkUtils by lazy { (application as FlashMasterApp).networkUtils }
    private val syncManager by lazy { (application as FlashMasterApp).syncManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Store the intent for later processing
        pendingDeepLink = intent
        Log.d("MainActivity", "Initial intent: ${intent?.data}")

        // Get NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                as? NavHostFragment ?: return

        navController = navHostFragment.navController

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Observe network state
        observeNetworkState()

        // Process deep link after NavController is ready
        navController?.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("MainActivity", "Destination changed to: ${destination.id}")
            when (destination.id) {
                R.id.homeFragment -> {
                    Log.d("MainActivity", "Reached home fragment, pendingFolderId: $pendingFolderId")
                    // We've reached the home fragment, now we can navigate to the flashcard
                    pendingFolderId?.let { folderId ->
                        if (auth.currentUser != null) {
                            Log.d("MainActivity", "User is logged in, copying folder: $folderId")
                            copySharedFolder(folderId)
                            pendingFolderId = null // Clear the pending folder ID after processing
                        } else {
                            Log.d("MainActivity", "User is not logged in")
                        }
                    }
                }
            }
        }

        // Handle initial deep link
        handleDeepLink(intent)
    }

    private fun observeNetworkState() {
        networkUtils.networkState
            .onEach { isOnline ->
                if (isOnline) {
                    // Show online indicator
                    Snackbar.make(binding.root, "Online - Syncing data...", Snackbar.LENGTH_SHORT).show()
                    // Sync data when online
                    auth.currentUser?.uid?.let { userId ->
                        lifecycleScope.launch {
                            try {
                                syncManager.syncAllData(userId)
                                Snackbar.make(binding.root, "Sync completed", Snackbar.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Snackbar.make(binding.root, "Sync failed: ${e.message}", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // Show offline indicator
                    Snackbar.make(binding.root, "Offline - Using local data", Snackbar.LENGTH_LONG).show()
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun copySharedFolder(originalFolderId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        Log.d("MainActivity", "Starting to copy folder: $originalFolderId for user: $currentUserId")

        lifecycleScope.launch {
            try {
                // First check if the user already has this folder
                val existingFolders = repository.getFolders(currentUserId). first()
                val alreadyHasFolder = existingFolders.any { folder -> folder.id == originalFolderId }

                if (alreadyHasFolder) {
                    Log.d("MainActivity", "User already has this folder")
                    Snackbar.make(binding.root, "You already have this folder", Snackbar.LENGTH_SHORT).show()
                    return@launch
                }

                // Get the original folder from Firebase
                val originalFolderDoc = db.collection("folders")
                    .document(originalFolderId)
                    .get()
                    .await()

                if (!originalFolderDoc.exists()) {
                    Log.d("MainActivity", "Original folder not found in database")
                    Snackbar.make(binding.root, "Folder not found", Snackbar.LENGTH_SHORT).show()
                    return@launch
                }

                Log.d("MainActivity", "Original folder found, creating copy")
                val originalFolder = FlashcardFolder.fromMap(
                    originalFolderDoc.id,
                    originalFolderDoc.data ?: return@launch
                )

                // Create a new folder with the same content but new owner
                val newFolder = FlashcardFolder(
                    id = "", // The ID will be set by Firestore when we add the document
                    name = originalFolder.name,
                    userId = currentUserId,
                    createdAt = Timestamp.now(),
                    cardCount = originalFolder.cardCount,
                    originalFolderId = originalFolderId
                )

                // Add the new folder to both Firebase and local database
                val newFolderRef = db.collection("folders")
                    .add(newFolder.toMap())
                    .await()

                // Create local folder entity
                val localFolder = FolderEntity(
                    id = newFolderRef.id,
                    name = newFolder.name,
                    userId = currentUserId
                )
                repository.localRepository.insertFolder(localFolder)

                // Copy all flashcards from the original folder
                val flashcardsSnapshot = db.collection("flashcards")
                    .whereEqualTo("folderId", originalFolderId)
                    .get()
                    .await()

                Log.d("MainActivity", "Found ${flashcardsSnapshot.documents.size} flashcards to copy")

                // Create local flashcard entities
                val localFlashcards = flashcardsSnapshot.documents.map { flashcardDoc ->
                    val flashcardData = flashcardDoc.data ?: return@map null
                    FlashcardEntity(
                        id = flashcardDoc.id,
                        folderId = newFolderRef.id,
                        frontText = flashcardData["frontText"] as String,
                        backText = flashcardData["backText"] as String
                    )
                }.filterNotNull()

                // Insert flashcards into local database
                repository.localRepository.insertFlashcards(localFlashcards)

                // Show success message
                Snackbar.make(binding.root, "Folder copied successfully", Snackbar.LENGTH_SHORT).show()

                // Find the HomeFragment and refresh the folders list
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                if (navHostFragment is NavHostFragment) {
                    val homeFragment = navHostFragment.childFragmentManager.fragments.firstOrNull {
                        it is HomeFragment
                    } as? HomeFragment
                    homeFragment?.loadFolders()
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Error copying folder", e)
                Snackbar.make(binding.root, "Error copying folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called with: ${intent.data}")
        pendingDeepLink = intent
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        Log.d("MainActivity", "handleDeepLink called with intent: ${intent?.data}")
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data ?: return
            Log.d("MainActivity", "Processing URI: $uri")

            val folderId = when {
                uri.scheme == "studypal" && uri.host == "folder" -> {
                    uri.pathSegments.firstOrNull()
                }
                uri.scheme == "https" && uri.host == "studypal.com" && uri.pathSegments.firstOrNull() == "folder" -> {
                    uri.pathSegments.getOrNull(1)
                }
                else -> null
            }

            Log.d("MainActivity", "Extracted folderId: $folderId")

            if (folderId != null) {
                try {
                    if (auth.currentUser != null) {
                        Log.d("MainActivity", "User is logged in, storing folderId: $folderId")
                        // Store the folder ID to navigate after reaching home fragment
                        pendingFolderId = folderId
                        // If we're not already on the home fragment, navigate there
                        if (navController?.currentDestination?.id != R.id.homeFragment) {
                            navController?.navigate(R.id.homeFragment)
                        }
                    } else {
                        Log.d("MainActivity", "User needs to log in first, storing folderId: $folderId")
                        // User needs to log in first
                        navController?.navigate(R.id.loginFragment)
                        // Store the folder ID to navigate after login
                        pendingFolderId = folderId
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error handling deep link", e)
                }
            } else {
                Log.d("MainActivity", "No valid folderId found in URI")
            }
        } else {
            Log.d("MainActivity", "Intent action is not ACTION_VIEW: ${intent?.action}")
        }
    }
}