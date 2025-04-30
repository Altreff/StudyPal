package com.example.flashmaster

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.flashmaster.FoldersPart.New.FolderAdapter
import com.example.flashmaster.FoldersPart.New.FolderCreationDialog
import com.example.flashmaster.auth.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.home) {

    private lateinit var adapter: FolderAdapter
    private lateinit var authViewModel: AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DEBUG", "HomeFragment loaded")

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        adapter = FolderAdapter(mutableListOf())

        // Set up settings button click listener
        view.findViewById<Button>(R.id.btnSettings)?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }

        val newButton = view.findViewById<LinearLayout>(R.id.btnNewFolder)
        newButton.setOnClickListener {
            FolderCreationDialog(
                requireContext(),
                onFolderCreated = { newFolder ->
                    adapter.addFolder(newFolder)
                }
            ).show()
        }

        val recyclerView: RecyclerView = requireView().findViewById(R.id.cardsRecyclerView)
        recyclerView.adapter = adapter

        // Set up auth button
        val authButton = view.findViewById<MaterialButton>(R.id.btnAuth)
        authButton.setOnClickListener {
            if (authViewModel.currentUser.value == null) {
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            } else {
                authViewModel.signOut()
                updateAuthButton(authButton)
            }
        }

        // Observe auth state using lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.currentUser.collect { user: FirebaseUser? ->
                updateAuthButton(authButton)
            }
        }
    }

    private fun updateAuthButton(button: MaterialButton) {
        if (authViewModel.currentUser.value == null) {
            button.text = "Login"
        } else {
            button.text = "Logout"
        }
    }
}
