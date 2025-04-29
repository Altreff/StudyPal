package com.example.flashmaster

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemeHelper(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    private val THEME_KEY = "theme_mode"

    init {
        // Apply the saved theme or use light theme by default
        val savedMode = prefs.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(savedMode)
    }

    fun setTheme(mode: Int) {
        prefs.edit().putInt(THEME_KEY, mode).apply()
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun isDarkMode(): Boolean {
        val currentMode = prefs.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_NO)
        return currentMode == AppCompatDelegate.MODE_NIGHT_YES
    }
} 