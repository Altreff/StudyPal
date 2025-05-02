package com.example.flashmaster.Quizz

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flashmaster.FoldersPart.New.Flashcard
import com.example.flashmaster.R
import com.google.firebase.firestore.FirebaseFirestore

class QuizFragment : Fragment() {

    private lateinit var folderId: String
    private val db = FirebaseFirestore.getInstance()
    private var flashcards: List<Flashcard> = emptyList()
    private var currentIndex = 0
    private var showingFront = true

    private lateinit var tvCardText: TextView
    private lateinit var btnToggle: Button
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        folderId = arguments?.getString("folderId") ?: ""
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val folderId = arguments?.getString("folderId")

        val view = inflater.inflate(R.layout.fragment_quiz, container, false)

        tvCardText = view.findViewById(R.id.tvCardText)
        btnToggle = view.findViewById(R.id.btnToggle)
        btnNext = view.findViewById(R.id.btnNext)

        btnToggle.setOnClickListener {
            toggleCardSide()
        }

        btnNext.setOnClickListener {
            showNextCard()
        }

        loadFlashcards()

        return view
    }

    private fun loadFlashcards() {
        db.collection("flashcards")
            .whereEqualTo("folderId", folderId)
            .get()
            .addOnSuccessListener { snapshot ->
                flashcards = snapshot.documents.mapNotNull {
                    Flashcard.fromMap(it.id, it.data ?: return@mapNotNull null)
                }
                currentIndex = 0
                if (flashcards.isNotEmpty()) {
                    showingFront = true
                    tvCardText.text = flashcards[0].frontText
                } else {
                    tvCardText.text = "No Cards"
                }
            }
    }

    private fun toggleCardSide() {
        if (flashcards.isEmpty()) return
        val currentCard = flashcards[currentIndex]
        if (showingFront) {
            tvCardText.text = currentCard.backText
            showingFront = false
        } else {
            tvCardText.text = currentCard.frontText
            showingFront = true
        }
    }

    private fun showNextCard() {
        if (flashcards.isEmpty()) return
        currentIndex = (currentIndex + 1) % flashcards.size
        showingFront = true
        tvCardText.text = flashcards[currentIndex].frontText
    }

    companion object {
        fun newInstance(folderId: String): QuizFragment {
            return QuizFragment().apply {
                arguments = Bundle().apply {
                    putString("folderId", folderId)
                }
            }
        }
    }
}
