package com.example.flashmaster

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment(R.layout.home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DEBUG", "HomeFragment loaded")

        val createButton = view.findViewById<Button>(R.id.buttonCreateFlashcard)
        val studyButton = view.findViewById<Button>(R.id.buttonStudyFlashcards)

        createButton.setOnClickListener {
            Log.d("HomeFragment", "Create button clicked")
            // add jump login(fragment to fragment)
            // findNavController().navigate(R.id.action_to_create_fragment)
        }

        studyButton.setOnClickListener {
            Log.d("HomeFragment", "Study button clicked")

            // findNavController().navigate(R.id.action_to_study_fragment)
        }
    }
}