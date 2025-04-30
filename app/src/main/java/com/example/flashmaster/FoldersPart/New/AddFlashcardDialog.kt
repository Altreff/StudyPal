package com.example.flashmaster.FoldersPart.New

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.flashmaster.R

class AddFlashcardDialog(
    private val onSave: (String, String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_flashcard, null)

        val editFrontText = view.findViewById<EditText>(R.id.editFrontText)
        val editBackText = view.findViewById<EditText>(R.id.editBackText)
        val buttonSave = view.findViewById<Button>(R.id.buttonSave)

        buttonSave.setOnClickListener {
            val frontText = editFrontText.text.toString()
            val backText = editBackText.text.toString()

            if (frontText.isNotBlank() && backText.isNotBlank()) {
                onSave(frontText, backText)
                dismiss()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Add New Flashcard")
            .setView(view)
            .create()
    }
}