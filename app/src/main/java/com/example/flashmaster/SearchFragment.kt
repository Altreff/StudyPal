package com.example.flashmaster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashmaster.databinding.FragmentSearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.flashmaster.FoldersPart.New.FlashcardFolder
import com.example.flashmaster.FoldersPart.New.FolderAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var folderAdapter: FolderAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearchInput()
        setupBottomNavigation()
        binding.bottomNavigation.selectedItemId = R.id.navigation_settings

    }

    private fun setupRecyclerView() {
        folderAdapter = FolderAdapter(
            onFolderClick = { folder ->
                val action = SearchFragmentDirections.actionSearchFragmentToFlashcardFragment(folder.id)
                findNavController().navigate(action)
            },
            onShareClick = { folder ->
                // TODO: Implement share functionality
            },
            onFolderUpdated = {
                // Refresh search results
                performSearch(binding.searchEditText.text.toString())
            }
        )
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = folderAdapter
        }
    }

    private fun setupSearchInput() {
        binding.searchEditText.setOnEditorActionListener { _, _, _ ->
            performSearch(binding.searchEditText.text.toString())
            true
        }
    }

    private fun performSearch(query: String) {
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
                }.filter { folder ->
                    folder.name.contains(query, ignoreCase = true)
                }.sortedByDescending { it.createdAt }
                
                folderAdapter.submitList(folders)
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root, "Error searching folders: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_folders -> {
                    findNavController().navigate(R.id.homeFragment)
                    true
                }
                R.id.navigation_search -> {
                    // Already in search view
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
} 