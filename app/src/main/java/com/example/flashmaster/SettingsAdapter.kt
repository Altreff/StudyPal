package com.example.flashmaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

class SettingsAdapter(
    private val onItemClick: (SettingsItem) -> Unit,
    private val isDarkMode: Boolean,
    private val isNotificationEnabled: Boolean
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    private val items = listOf(
        SettingsItem(
            1,
            "Dark Mode",
            "Switch between light and dark mode",
            SettingsType.THEME
        ),
        SettingsItem(
            2,
            "Notifications",
            "Manage notification preferences",
            SettingsType.NOTIFICATION
        ),
        SettingsItem(
            3,
            "Share App",
            "Share this app with friends",
            SettingsType.SHARE
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_setting, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescription)
        private val switchSetting: SwitchCompat = itemView.findViewById(R.id.switch_setting)

        fun bind(item: SettingsItem) {
            titleTextView.text = item.title
            descriptionTextView.text = item.description

            // Remove previous listeners to avoid duplicates
            switchSetting.setOnCheckedChangeListener(null)

            when (item.type) {
                SettingsType.THEME -> {
                    switchSetting.visibility = View.VISIBLE
                    switchSetting.isChecked = isDarkMode
                    switchSetting.setOnCheckedChangeListener { _, _ ->
                        onItemClick(item)
                    }
                }
                SettingsType.NOTIFICATION -> {
                    switchSetting.visibility = View.VISIBLE
                    switchSetting.isChecked = isNotificationEnabled
                    switchSetting.setOnCheckedChangeListener { _, _ ->
                        onItemClick(item)
                    }
                }
                SettingsType.SHARE -> {
                    switchSetting.visibility = View.GONE
                    itemView.setOnClickListener {
                        onItemClick(item)
                    }
                }
            }

            // Only set click listener for non-switch items
            if (item.type == SettingsType.SHARE) {
                itemView.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }
} 