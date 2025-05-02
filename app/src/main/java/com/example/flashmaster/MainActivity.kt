package com.example.flashmaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import com.example.flashmaster.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.example.flashmaster.FoldersPart.New.FlashcardFolder
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var pendingDeepLink: Intent? = null
    private var navController: NavController? = null
    private var pendingFolderId: String? = null

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

    private fun copySharedFolder(originalFolderId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        Log.d("MainActivity", "Starting to copy folder: $originalFolderId for user: $currentUserId")

        // First check if the user already has this folder
        db.collection("folders")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("originalFolderId", originalFolderId)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("MainActivity", "Check for existing folder result: ${snapshot.documents.size} documents found")
                if (snapshot.isEmpty) {
                    // User doesn't have this folder yet, let's copy it
                    Log.d("MainActivity", "Folder not found in user's collection, proceeding with copy")
                    db.collection("folders").document(originalFolderId)
                        .get()
                        .addOnSuccessListener { originalFolderDoc ->
                            if (!originalFolderDoc.exists()) {
                                Log.d("MainActivity", "Original folder not found in database")
                                Snackbar.make(binding.root, "Folder not found", Snackbar.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

                            Log.d("MainActivity", "Original folder found, creating copy")
                            val originalFolder = FlashcardFolder.fromMap(
                                originalFolderDoc.id,
                                originalFolderDoc.data ?: return@addOnSuccessListener
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

                            // Add the new folder
                            db.collection("folders")
                                .add(newFolder.toMap())
                                .addOnSuccessListener { newFolderRef ->
                                    Log.d("MainActivity", "New folder created with ID: ${newFolderRef.id}")
                                    // Copy all flashcards from the original folder
                                    db.collection("flashcards")
                                        .whereEqualTo("folderId", originalFolderId)
                                        .get()
                                        .addOnSuccessListener { flashcardsSnapshot ->
                                            Log.d("MainActivity", "Found ${flashcardsSnapshot.documents.size} flashcards to copy")
                                            val batch = db.batch()
                                            for (flashcardDoc in flashcardsSnapshot.documents) {
                                                val flashcardData = flashcardDoc.data ?: continue
                                                val newFlashcardRef = db.collection("flashcards").document()
                                                batch.set(newFlashcardRef, flashcardData.apply {
                                                    put("folderId", newFolderRef.id)
                                                })
                                            }
                                            batch.commit()
                                                .addOnSuccessListener {
                                                    Log.d("MainActivity", "Successfully copied all flashcards")
                                                    Snackbar.make(binding.root, "Folder copied successfully", Snackbar.LENGTH_SHORT).show()
                                                    
                                                    // Find the HomeFragment and refresh the folders list
                                                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                                                    if (navHostFragment is NavHostFragment) {
                                                        val homeFragment = navHostFragment.childFragmentManager.fragments.firstOrNull { 
                                                            it is HomeFragment 
                                                        } as? HomeFragment
                                                        homeFragment?.loadFolders()
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("MainActivity", "Error copying flashcards", e)
                                                    Snackbar.make(binding.root, "Error copying flashcards: ${e.message}", Snackbar.LENGTH_SHORT).show()
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("MainActivity", "Error getting flashcards", e)
                                            Snackbar.make(binding.root, "Error copying flashcards: ${e.message}", Snackbar.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("MainActivity", "Error creating new folder", e)
                                    Snackbar.make(binding.root, "Error copying folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("MainActivity", "Error getting original folder", e)
                            Snackbar.make(binding.root, "Error accessing shared folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
                        }
                } else {
                    Log.d("MainActivity", "User already has this folder")
                    Snackbar.make(binding.root, "You already have this folder", Snackbar.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error checking for existing folder", e)
                Snackbar.make(binding.root, "Error checking for existing folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
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