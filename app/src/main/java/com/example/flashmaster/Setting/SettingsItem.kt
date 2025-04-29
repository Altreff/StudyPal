package com.example.flashmaster.Setting

data class SettingsItem(
    val id: Int,
    val title: String,
    val description: String? = null,
    val type: SettingsType
)

enum class SettingsType {
    THEME,
    NOTIFICATION,
    SHARE
} 