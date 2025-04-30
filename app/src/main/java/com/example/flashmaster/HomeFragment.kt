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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp

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
    }

    private fun setupRecyclerView() {
        folderAdapter = FolderAdapter { folder ->
            // Navigate to FlashcardFragment with folder ID
            val action = HomeFragmentDirections.actionHomeFragmentToFlashcardFragment(folder.id)
            findNavController().navigate(action)
        }
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

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }
    }

    private fun setupAuthObserver() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _binding?.let { binding ->
                binding.btnAuth.text = if (user != null) "Logout" else "Login"
                if (user != null) {
                    loadFolders()
                } else {
                    folderAdapter.submitList(emptyList())
                }
            }
        }
        auth.addAuthStateListener(authStateListener!!)
    }

    private fun loadFolders() {
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
        dialog.show()
    }

    private fun createNewFolder(folderName: String) {
        val userId = auth.currentUser?.uid ?: return
        val folder = FlashcardFolder(
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
    }

    override fun onDestroyView() {
        authStateListener?.let {
            auth.removeAuthStateListener(it)
        }
        _binding = null
        super.onDestroyView()
    }
}
