package com.example.flashmaster.Setting

import android.content.Context
import android.content.SharedPreferences

class NotificationHelper(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
    private val NOTIFICATION_KEY = "notification_enabled"

    fun isNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(NOTIFICATION_KEY, false)
    }

    fun setNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(NOTIFICATION_KEY, enabled).apply()
    }
} 