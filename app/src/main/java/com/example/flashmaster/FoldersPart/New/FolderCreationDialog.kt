package com.example.flashmaster.FoldersPart.New

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.flashmaster.R

class FolderCreationDialog(
    private val context: Context,
    private val onFolderCreated: (String) -> Unit
) {
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_folder, null)

        val folderNameInput = dialogView.findViewById<EditText>(R.id.etFolderName)
        val shareCodeInput = dialogView.findViewById<EditText>(R.id.etShareCode)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Create New Folder")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogView.findViewById<Button>(R.id.btnCreate).setOnClickListener {
            val folderName = folderNameInput.text.toString().trim()
            if (folderName.isNotEmpty()) {
                onFolderCreated(folderName)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please enter a folder name", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<Button>(R.id.btnGo).setOnClickListener {
            val code = shareCodeInput.text.toString().trim()
            if (code.isNotEmpty()) {
                Toast.makeText(context, "Downloading folder with code: $code", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please enter a share code", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
