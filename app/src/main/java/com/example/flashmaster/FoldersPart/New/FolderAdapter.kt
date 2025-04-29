package com.example.flashmaster.FoldersPart.New

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flashmaster.R

class FolderAdapter(private val folders: MutableList<FlashcardFolder>) :
    RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dragHandle: ImageView = itemView.findViewById(R.id.ivDragHandle)
        val tvName: TextView = itemView.findViewById(R.id.tvFolderName)
        val tvDate: TextView = itemView.findViewById(R.id.tvCreatedDate)
        val tvCardCount: TextView = itemView.findViewById(R.id.tvCardCount)
        val btnShare: ImageView = itemView.findViewById(R.id.ivShare)
        val btnOption: ImageView = itemView.findViewById(R.id.ivOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flashcard_folder, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.tvName.text = folder.name
        holder.tvDate.text = folder.createdAt
        holder.tvCardCount.text = "${folder.cardCount} Cards"
    }

    override fun getItemCount() = folders.size

    fun addFolder(folder: FlashcardFolder) {
        folders.add(0, folder)
        notifyItemInserted(0)
    }
}
