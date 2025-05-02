package com.example.flashmaster.FoldersPart.New

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashmaster.Quizz.QuizFragment
import com.example.flashmaster.R
import com.example.flashmaster.databinding.FragmentFlashcardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FlashcardFragment : Fragment() {
    private var _binding: FragmentFlashcardBinding? = null
    private val binding get() = _binding!!
    private lateinit var folderId: String
    private lateinit var flashcardAdapter: FlashcardAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        folderId = arguments?.getString("folderId") ?: return
        setupRecyclerView()

        binding.fabQuiz.setOnClickListener {
            val bundle = Bundle().apply {
                putString("folderId", folderId)  // 传递参数 folderId
            }
            findNavController().navigate(R.id.quizFragment, bundle)  // 跳转到 QuizFragment
        }

        loadFlashcards()
        setupAddButton()
    }

    private fun setupRecyclerView() {
        flashcardAdapter = FlashcardAdapter { flashcard ->
            showEditFlashcardDialog(flashcard)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = flashcardAdapter
        }
    }

    private fun loadFlashcards() {
        db.collection("flashcards")
            .whereEqualTo("folderId", folderId)
            .get()
            .addOnSuccessListener { snapshot ->
                val flashcards = snapshot.documents.mapNotNull { doc ->
                    try {
                        Flashcard.fromMap(doc.id, doc.data ?: return@mapNotNull null)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.createdAt }
                flashcardAdapter.submitList(flashcards)
            }
            .addOnFailureListener { e ->
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "Error loading flashcards: ${e.message}",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
            }
    }

    private fun setupAddButton() {
        binding.fabAddFlashcard.setOnClickListener {
            showAddFlashcardDialog()
        }
    }

    private fun showAddFlashcardDialog() {
        val dialog = AddFlashcardDialog { frontText, backText ->
            addFlashcard(frontText, backText)
        }
        dialog.show(childFragmentManager, "AddFlashcardDialog")
    }

    private fun showEditFlashcardDialog(flashcard: Flashcard) {
        val dialog = EditFlashcardDialog(
            flashcard = flashcard,
            onSave = { frontText, backText ->
                updateFlashcard(flashcard.id, frontText, backText)
            },
            onDelete = {
                deleteFlashcard(flashcard)
            }
        )
        dialog.show(childFragmentManager, "EditFlashcardDialog")
    }

    private fun addFlashcard(frontText: String, backText: String) {
        val now = com.google.firebase.Timestamp.now()
        val flashcard = Flashcard(
            folderId = folderId,
            frontText = frontText,
            backText = backText,
            createdAt = now,
            lastModified = now
        )

        db.collection("flashcards")
            .add(flashcard.toMap())
            .addOnSuccessListener { documentReference ->
                // After successfully adding the flashcard, refresh the list
                db.collection("flashcards")
                    .whereEqualTo("folderId", folderId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val flashcards = snapshot.documents.mapNotNull { doc ->
                            Flashcard.fromMap(doc.id, doc.data ?: return@mapNotNull null)
                        }.sortedByDescending { it.createdAt }
                        flashcardAdapter.submitList(flashcards)

                        // Update folder's card count
                        db.collection("folders").document(folderId)
                            .update("cardCount", com.google.firebase.firestore.FieldValue.increment(1))
                    }
            }
    }

    private fun updateFlashcard(flashcardId: String, frontText: String, backText: String) {
        db.collection("flashcards").document(flashcardId)
            .update(
                "frontText", frontText,
                "backText", backText,
                "lastModified", com.google.firebase.Timestamp.now()
            )
            .addOnSuccessListener {
                // Refresh the list after successful update
                refreshFlashcardsList()
            }
    }

    private fun deleteFlashcard(flashcard: Flashcard) {
        db.collection("flashcards").document(flashcard.id)
            .delete()
            .addOnSuccessListener {
                // Update folder's card count
                db.collection("folders").document(folderId)
                    .update("cardCount", com.google.firebase.firestore.FieldValue.increment(-1))
                // Refresh the list after successful deletion
                refreshFlashcardsList()
            }
    }

    private fun refreshFlashcardsList() {
        db.collection("flashcards")
            .whereEqualTo("folderId", folderId)
            .get()
            .addOnSuccessListener { snapshot ->
                val flashcards = snapshot.documents.mapNotNull { doc ->
                    try {
                        Flashcard.fromMap(doc.id, doc.data ?: return@mapNotNull null)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.createdAt }
                flashcardAdapter.submitList(flashcards)
            }
            .addOnFailureListener { e ->
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "Error refreshing flashcards: ${e.message}",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(folderId: String): FlashcardFragment {
            return FlashcardFragment().apply {
                arguments = Bundle().apply {
                    putString("folderId", folderId)
                }
            }
        }
    }
}
