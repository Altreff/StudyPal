package com.example.flashmaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashmaster.databinding.HomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.flashmaster.FoldersPart.New.FlashcardFolder
import com.example.flashmaster.FoldersPart.New.FolderAdapter
import com.example.flashmaster.FoldersPart.New.FolderCreationDialog
import com.example.flashmaster.FoldersPart.New.ShareFolderDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {
    private var _binding: HomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var folderAdapter: FolderAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupButtons()
        setupAuthObserver()
        loadFolders()
        setupBottomNavigation()

        // Set the selected item
        binding.bottomNavigation.selectedItemId = R.id.navigation_folders

        // Setup bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_folders -> {
                    // Already in folders view
                    true
                }
                R.id.navigation_search -> {
                    findNavController().navigate(R.id.searchFragment)
                    true
                }
                R.id.navigation_settings -> {
                    findNavController().navigate(R.id.settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        folderAdapter = FolderAdapter(
            onFolderClick = { folder ->
                // Navigate to FlashcardFragment with folder ID
                val action = HomeFragmentDirections.actionHomeFragmentToFlashcardFragment(folder.id)
                findNavController().navigate(action)
            },
            onShareClick = { folder ->
                showShareFolderDialog(folder)
            },
            onFolderUpdated = {
                loadFolders() // Refresh the list when a folder is updated or deleted
            }
        )
        binding.cardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = folderAdapter
        }
    }

    private fun setupButtons() {
        binding.btnNewFolder.setOnClickListener {
            if (auth.currentUser != null) {
                showNewFolderDialog()
            } else {
                Snackbar.make(binding.root, "Please log in to create folders", Snackbar.LENGTH_SHORT).show()
            }
        }
        binding.btnAuth.setOnClickListener {
            if (auth.currentUser != null) {
                auth.signOut()
            } else {
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            }
        }
        binding.btnAuthText.setOnClickListener {
            if (auth.currentUser != null) {
                auth.signOut()
            } else {
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            }
        }
    }





    private fun setupAuthObserver() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _binding?.let { binding ->
                binding.btnAuthText.text = if (user != null) "Logout" else "Login"
                if (user != null) {
                    loadFolders()
                } else {
                    folderAdapter.submitList(emptyList())
                }
            }
        }
        auth.addAuthStateListener(authStateListener!!)
    }

    fun loadFolders() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("folders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val folders = snapshot.documents.mapNotNull { doc ->
                    try {
                        FlashcardFolder.fromMap(doc.id, doc.data ?: return@mapNotNull null)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.createdAt }
                _binding?.let {
                    folderAdapter.submitList(folders)
                }
            }
            .addOnFailureListener { e ->
                _binding?.let {
                    Snackbar.make(it.root, "Error loading folders: ${e.message}", Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    private fun showNewFolderDialog() {
        val dialog = FolderCreationDialog(
            requireContext(),
            onFolderCreated = { folderName ->
                createNewFolder(folderName)
            }
        )
        dialog.show(childFragmentManager, "FolderCreationDialog")
    }

    private fun createNewFolder(folderName: String) {
        val userId = auth.currentUser?.uid ?: return
        
        // First check if a folder with this name already exists
        db.collection("folders")
            .whereEqualTo("userId", userId)
            .whereEqualTo("name", folderName)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    // Only create a new folder if one with this name doesn't exist
                    val folder = FlashcardFolder(
                        id = "", // The ID will be set by Firestore when we add the document
                        name = folderName,
                        userId = userId,
                        createdAt = Timestamp.now(),
                        cardCount = 0
                    )
                    db.collection("folders")
                        .add(folder.toMap())
                        .addOnSuccessListener { documentReference ->
                            _binding?.let {
                                Snackbar.make(it.root, "Folder created successfully", Snackbar.LENGTH_SHORT).show()
                                loadFolders() // Refresh the list after creating a folder
                            }
                        }
                        .addOnFailureListener { e ->
                            _binding?.let {
                                Snackbar.make(it.root, "Error creating folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // Just refresh the list if the folder already exists
                    loadFolders()
                }
            }
            .addOnFailureListener { e ->
                _binding?.let {
                    Snackbar.make(it.root, "Error checking for existing folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    private fun showShareFolderDialog(folder: FlashcardFolder) {
        val dialog = ShareFolderDialog(folder.id, folder.name)
        dialog.show(childFragmentManager, "ShareFolderDialog")
    }

    // Add this method to handle folder updates from code
    fun addFolderByCode(folderName: String) {
        if (auth.currentUser == null) {
            Snackbar.make(binding.root, "Please log in to create folders", Snackbar.LENGTH_SHORT).show()
            return
        }
        createNewFolder(folderName)
    }

    private fun setupBottomNavigation() {
        // Implementation of setupBottomNavigation method
    }

    override fun onDestroyView() {
        authStateListener?.let {
            auth.removeAuthStateListener(it)
        }
        _binding = null
        super.onDestroyView()
    }
}
