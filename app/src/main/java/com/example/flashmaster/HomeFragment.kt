package com.example.flashmaster

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.flashmaster.FoldersPart.New.FolderAdapter
import com.example.flashmaster.FoldersPart.New.FolderCreationDialog

class HomeFragment : Fragment(R.layout.home) {

    private lateinit var adapter: FolderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DEBUG", "HomeFragment loaded")

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
    }
}
