package com.example.flashmaster.FoldersPart.New

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.flashmaster.R
import com.google.firebase.firestore.FirebaseFirestore

class EditFlashcardDialog(
    private val flashcard: Flashcard,
    private val onSave: (String, String) -> Unit,
    private val onDelete: () -> Unit
) : DialogFragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_flashcard, null)

        val editFrontText = view.findViewById<EditText>(R.id.editFrontText)
        val editBackText = view.findViewById<EditText>(R.id.editBackText)
        val buttonDelete = view.findViewById<Button>(R.id.buttonDelete)
        val buttonSave = view.findViewById<Button>(R.id.buttonSave)

        editFrontText.setText(flashcard.frontText)
        editBackText.setText(flashcard.backText)

        buttonSave.setOnClickListener {
            val frontText = editFrontText.text.toString()
            val backText = editBackText.text.toString()

            if (frontText.isNotBlank() && backText.isNotBlank()) {
                onSave(frontText, backText)
                dismiss()
            }
        }

        buttonDelete.setOnClickListener {
            onDelete()
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Edit Flashcard")
            .setView(view)
            .create()
    }
} 