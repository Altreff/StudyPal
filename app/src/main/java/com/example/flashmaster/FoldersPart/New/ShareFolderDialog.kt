package com.example.flashmaster.FoldersPart.New

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.flashmaster.R

class ShareFolderDialog(private val folderId: String, private val folderName: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_share_folder)

        val tvShareMessage = dialog.findViewById<TextView>(R.id.tvShareMessage)
        val btnCopyLink = dialog.findViewById<Button>(R.id.btnCopyLink)
        val btnShareLink = dialog.findViewById<Button>(R.id.btnShareLink)

        val shareMessage = "Check out my folder \"$folderName\" on StudyPal!\n\n" +
                "Click here to open: https://studypal.com/folder/$folderId\n\n" +
                "Or use the code \"$folderId\" to add the folder"

        tvShareMessage.text = shareMessage

        btnCopyLink.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("StudyPal Folder Link", shareMessage)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Link copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        btnShareLink.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Folder"))
        }

        return dialog
    }
} 