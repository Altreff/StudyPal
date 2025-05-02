package com.example.flashmaster.FoldersPart.New

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.flashmaster.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.snackbar.Snackbar

class FolderSettingsDialog(
    private val folder: FlashcardFolder,
    private val onFolderUpdated: () -> Unit
) : DialogFragment() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_folder_settings)

        val tvFolderName = dialog.findViewById<TextView>(R.id.tvFolderName)
        val btnEditName = dialog.findViewById<Button>(R.id.btnEditName)
        val btnDelete = dialog.findViewById<Button>(R.id.btnDelete)

        tvFolderName.text = folder.name

        btnEditName.setOnClickListener {
            showEditNameDialog(dialog)
        }

        btnDelete.setOnClickListener {
            deleteFolder(dialog)
        }

        return dialog
    }

    private fun showEditNameDialog(parentDialog: Dialog) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_edit_name)

        val etNewName = dialog.findViewById<EditText>(R.id.etNewName)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        etNewName.setText(folder.name)

        btnSave.setOnClickListener {
            val newName = etNewName.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateFolderName(newName, dialog, parentDialog)
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateFolderName(newName: String, editDialog: Dialog, parentDialog: Dialog) {
        db.collection("folders").document(folder.id)
            .update("name", newName)
            .addOnSuccessListener {
                Snackbar.make(parentDialog.window?.decorView ?: return@addOnSuccessListener, "Folder name updated", Snackbar.LENGTH_SHORT).show()
                editDialog.dismiss()
                parentDialog.dismiss()
                onFolderUpdated()
            }
            .addOnFailureListener { e ->
                Snackbar.make(parentDialog.window?.decorView ?: return@addOnFailureListener, "Error updating folder name: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun deleteFolder(dialog: Dialog) {
        // First delete all flashcards in the folder
        db.collection("flashcards")
            .whereEqualTo("folderId", folder.id)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        // Then delete the folder itself
                        db.collection("folders").document(folder.id)
                            .delete()
                            .addOnSuccessListener {
                                Snackbar.make(dialog.window?.decorView ?: return@addOnSuccessListener, "Folder deleted", Snackbar.LENGTH_SHORT).show()
                                dialog.dismiss()
                                onFolderUpdated()
                            }
                            .addOnFailureListener { e ->
                                Snackbar.make(dialog.window?.decorView ?: return@addOnFailureListener, "Error deleting folder: ${e.message}", Snackbar.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Snackbar.make(dialog.window?.decorView ?: return@addOnFailureListener, "Error deleting flashcards: ${e.message}", Snackbar.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Snackbar.make(dialog.window?.decorView ?: return@addOnFailureListener, "Error getting flashcards: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }
} 