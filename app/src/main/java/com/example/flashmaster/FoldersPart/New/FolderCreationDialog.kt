package com.example.flashmaster.FoldersPart.New

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.flashmaster.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FolderCreationDialog(
    private val context: Context,
    private val onFolderCreated: (FlashcardFolder) -> Unit
) {
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_folder, null)

        val folderNameInput = dialogView.findViewById<EditText>(R.id.etFolderName)
        val shareCodeInput = dialogView.findViewById<EditText>(R.id.etShareCode)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Creation Options")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<Button>(R.id.btnCreate).setOnClickListener {
            val folderName = folderNameInput.text.toString().trim()
            if (folderName.isNotEmpty()) {
                val newFolder = FlashcardFolder(
                    name = folderName,
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                    cardCount = 0
                )
                onFolderCreated(newFolder)
            }
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnGo).setOnClickListener {
            val code = shareCodeInput.text.toString().trim()
            Toast.makeText(context, "Downloading folder with code: $code", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
