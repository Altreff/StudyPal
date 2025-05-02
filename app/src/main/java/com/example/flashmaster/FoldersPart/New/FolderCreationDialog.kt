package com.example.flashmaster.FoldersPart.New

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.flashmaster.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.android.material.snackbar.Snackbar

class FolderCreationDialog(
    private val context: Context,
    private val onFolderCreated: (String) -> Unit
) : DialogFragment() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_create_folder)

        val etFolderName = dialog.findViewById<EditText>(R.id.etFolderName)
        val etFolderCode = dialog.findViewById<EditText>(R.id.etFolderCode)
        val btnCreate = dialog.findViewById<Button>(R.id.btnCreate)
        val btnAddByCode = dialog.findViewById<Button>(R.id.btnAddByCode)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)

        btnCreate.setOnClickListener {
            val name = etFolderName.text.toString().trim()
            if (name.isNotEmpty()) {
                onFolderCreated(name)
                dialog.dismiss()
            }
        }

        btnAddByCode.setOnClickListener {
            val code = etFolderCode.text.toString().trim()
            if (code.isNotEmpty()) {
                addFolderByCode(code, dialog)
            }
        }

        return dialog
    }

    private fun addFolderByCode(folderId: String, dialog: Dialog) {
        val currentUserId = auth.currentUser?.uid ?: return

        // First check if the user already has this folder
        db.collection("folders")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("originalFolderId", folderId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    // User doesn't have this folder yet, let's copy it
                    db.collection("folders").document(folderId)
                        .get()
                        .addOnSuccessListener { originalFolderDoc ->
                            if (!originalFolderDoc.exists()) {
                                Snackbar.make(dialog.findViewById(R.id.root), "Invalid folder code", Snackbar.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

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
                                originalFolderId = folderId
                            )

                            // Add the new folder
                            db.collection("folders")
                                .add(newFolder.toMap())
                                .addOnSuccessListener { newFolderRef ->
                                    // Copy all flashcards from the original folder
                                    db.collection("flashcards")
                                        .whereEqualTo("folderId", folderId)
                                        .get()
                                        .addOnSuccessListener { flashcardsSnapshot ->
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
                                                    Snackbar.make(dialog.findViewById(R.id.root), "Folder added successfully", Snackbar.LENGTH_SHORT).show()
                                                    dialog.dismiss()
                                                    // Call onFolderCreated to trigger list update
                                                    onFolderCreated(originalFolder.name)
                                                }
                                                .addOnFailureListener { e ->
                                                    Snackbar.make(dialog.findViewById(R.id.root), "Error copying flashcards: ${e.message}", Snackbar.LENGTH_SHORT).show()
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Snackbar.make(dialog.findViewById(R.id.root), "Error copying flashcards: ${e.message}", Snackbar.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Snackbar.make(dialog.findViewById(R.id.root), "Error copying folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Snackbar.make(dialog.findViewById(R.id.root), "Error accessing shared folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
                        }
                } else {
                    Snackbar.make(dialog.findViewById(R.id.root), "You already have this folder", Snackbar.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Snackbar.make(dialog.findViewById(R.id.root), "Error checking for existing folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }
}
