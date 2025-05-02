package com.example.flashmaster.FoldersPart.New

import android.view.View
import android.widget.Button
import android.widget.EditText
import com.example.flashmaster.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.snackbar.Snackbar

class AddFolderHandler(
    private val view: View,
    private val onFolderAdded: () -> Unit
) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val etFolderName = view.findViewById<EditText>(R.id.etFolderName)
    private val btnCreate = view.findViewById<Button>(R.id.btnCreate)

    init {
        btnCreate.setOnClickListener {
            val name = etFolderName.text.toString().trim()
            if (name.isNotEmpty()) {
                addFolder(name)
                etFolderName.text.clear()
            }
        }
    }

    fun addFolder(folderName: String) {
        val userId = auth.currentUser?.uid ?: return
        val folder = FlashcardFolder(
            id = "", // The ID will be set by Firestore when we add the document
            name = folderName,
            userId = userId,
            createdAt = Timestamp.now(),
            cardCount = 0
        )
        db.collection("folders")
            .add(folder.toMap())
            .addOnSuccessListener {
                onFolderAdded()
            }
            .addOnFailureListener { e ->
                Snackbar.make(view, "Error creating folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }
}
