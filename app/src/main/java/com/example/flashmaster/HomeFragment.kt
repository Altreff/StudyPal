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
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            binding.btnAuth.text = if (user != null) "Logout" else "Login"
            if (user != null) {
                loadFolders()
            } else {
                folderAdapter.submitList(emptyList())
            }
        }
    }

    private fun loadFolders() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("folders")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Snackbar.make(binding.root, "Error loading folders: ${e.message}", Snackbar.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val folders = snapshot?.documents?.mapNotNull { doc ->
                    FlashcardFolder.fromMap(doc.id, doc.data ?: return@mapNotNull null)
                } ?: emptyList()
                folderAdapter.submitList(folders)
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
                Snackbar.make(binding.root, "Folder created successfully", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root, "Error creating folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
