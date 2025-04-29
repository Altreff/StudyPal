package com.example.flashmaster.FoldersPart.New

import android.view.View
import android.widget.Button
import android.widget.EditText
import com.example.flashmaster.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddFolderHandler(
    private val rootView: View,
    private val onFolderCreated: (FlashcardFolder) -> Unit
) {
    private val etFolderName = rootView.findViewById<EditText>(R.id.etFolderName)
    private val btnCreate = rootView.findViewById<Button>(R.id.btnCreate)

    init {
        btnCreate.setOnClickListener {
            val name = etFolderName.text.toString().trim()
            if (name.isNotEmpty()) {
                val newFolder = FlashcardFolder(
                    name = name,
                    createdAt = getCurrentTimestamp(),
                    cardCount = 0
                )
                onFolderCreated(newFolder)
                etFolderName.text.clear()
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }
}
