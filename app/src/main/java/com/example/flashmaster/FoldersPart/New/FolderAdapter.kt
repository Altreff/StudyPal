package com.example.flashmaster.FoldersPart.New

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashmaster.R
import java.text.SimpleDateFormat
import java.util.Locale

class FolderAdapter(
    private val onFolderClick: (FlashcardFolder) -> Unit
) : ListAdapter<FlashcardFolder, FolderAdapter.FolderViewHolder>(FolderDiffCallback()) {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dragHandle: ImageView = itemView.findViewById(R.id.ivDragHandle)
        val tvName: TextView = itemView.findViewById(R.id.tvFolderName)
        val tvDate: TextView = itemView.findViewById(R.id.tvCreatedDate)
        val tvCardCount: TextView = itemView.findViewById(R.id.tvCardCount)
        val btnShare: ImageView = itemView.findViewById(R.id.ivShare)
        val btnOption: ImageView = itemView.findViewById(R.id.ivOptions)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFolderClick(getItem(position))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flashcard_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = getItem(position)
        holder.tvName.text = folder.name
        holder.tvDate.text = dateFormat.format(folder.createdAt.toDate())
        holder.tvCardCount.text = "${folder.cardCount} Cards"
    }

    private class FolderDiffCallback : DiffUtil.ItemCallback<FlashcardFolder>() {
        override fun areItemsTheSame(oldItem: FlashcardFolder, newItem: FlashcardFolder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FlashcardFolder, newItem: FlashcardFolder): Boolean {
            return oldItem == newItem
        }
    }
}
