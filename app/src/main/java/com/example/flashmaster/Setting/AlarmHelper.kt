package com.example.flashmaster.Setting

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import java.util.Calendar

class AlarmHelper(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
    private val ALARM_KEY = "alarm_enabled"
    private val ALARM_TIME_KEY = "alarm_time"
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun isAlarmEnabled(): Boolean {
        return sharedPreferences.getBoolean(ALARM_KEY, false)
    }

    fun setAlarmEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(ALARM_KEY, enabled).apply()
        if (enabled) {
            setAlarm()
        } else {
            cancelAlarm()
        }
    }

    fun getAlarmTime(): String {
        return sharedPreferences.getString(ALARM_TIME_KEY, "08:00") ?: "08:00"
    }

    fun setAlarmTime(time: String) {
        sharedPreferences.edit().putString(ALARM_TIME_KEY, time).apply()
        if (isAlarmEnabled()) {
            setAlarm()
        }
    }

    fun setAlarm() {
        val time = getAlarmTime()
        val timeParts = time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, set it for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.flashmaster.ALARM_ACTION"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
} 