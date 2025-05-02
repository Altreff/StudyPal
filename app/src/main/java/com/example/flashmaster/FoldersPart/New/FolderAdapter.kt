package com.example.flashmaster.FoldersPart.New

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashmaster.databinding.ItemFlashcardFolderBinding

class FolderAdapter(
    private val onFolderClick: (FlashcardFolder) -> Unit,
    private val onShareClick: (FlashcardFolder) -> Unit,
    private val onFolderUpdated: () -> Unit
) : ListAdapter<FlashcardFolder, FolderAdapter.FolderViewHolder>(FolderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFlashcardFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FolderViewHolder(
        private val binding: ItemFlashcardFolderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: FlashcardFolder) {
            binding.tvFolderName.text = folder.name
            binding.tvCardCount.text = "${folder.cardCount} cards"

            binding.root.setOnClickListener {
                onFolderClick(folder)
            }

            binding.btnShare.setOnClickListener {
                onShareClick(folder)
            }

            binding.btnSettings.setOnClickListener {
                val dialog = FolderSettingsDialog(folder, onFolderUpdated)
                dialog.show((binding.root.context as androidx.fragment.app.FragmentActivity).supportFragmentManager, "FolderSettingsDialog")
            }
        }
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
