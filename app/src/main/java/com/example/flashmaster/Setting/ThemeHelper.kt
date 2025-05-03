package com.example.flashmaster.Setting

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemeHelper(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    private val THEME_KEY = "theme_mode"

    init {
        // Always use the last user choice, or system default if never set
        val savedMode = prefs.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedMode)
    }

    fun setTheme(mode: Int) {
        prefs.edit().putInt(THEME_KEY, mode).apply()
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun isDarkMode(): Boolean {
        val currentMode = prefs.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        return currentMode == AppCompatDelegate.MODE_NIGHT_YES
    }
} 