package com.example.flashmaster.Setting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashmaster.R
import android.app.TimePickerDialog
import com.example.flashmaster.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var themeHelper: ThemeHelper
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmHelper: AlarmHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var settingsAdapter: SettingsAdapter
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DEBUG", "SettingsFragment loaded")

        themeHelper = ThemeHelper(requireContext())
        notificationHelper = NotificationHelper(requireContext())
        alarmHelper = AlarmHelper(requireContext())
        
        // Set up back button
        view.findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            findNavController().navigateUp()
        }
        
        recyclerView = view.findViewById(R.id.rvSettings)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        setupAdapter()
        setupBottomNavigation()
    }

    private fun setupAdapter() {
        settingsAdapter = SettingsAdapter({ item ->
            when (item.type) {
                SettingsType.THEME -> {
                    // Toggle theme
                    val newMode = if (themeHelper.isDarkMode()) {
                        AppCompatDelegate.MODE_NIGHT_NO
                    } else {
                        AppCompatDelegate.MODE_NIGHT_YES
                    }
                    themeHelper.setTheme(newMode)
                    // Restart activity to apply theme
                    requireActivity().recreate()
                }
                SettingsType.NOTIFICATION -> {
                    // Check if we need to request notification permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Permission already granted, toggle notification state
                            toggleNotificationState()
                        } else {
                            // Request notification permission
                            requestNotificationPermission()
                        }
                    } else {
                        // For Android 12 and below, no permission needed
                        toggleNotificationState()
                    }
                }
                SettingsType.ALARM -> {
                    // Toggle alarm state
                    val currentState = alarmHelper.isAlarmEnabled()
                    alarmHelper.setAlarmEnabled(!currentState)
                    if (!currentState) {
                        // If enabling alarm, show time picker
                        showTimePicker()
                    }
                    // Refresh the adapter to update the switch state
                    setupAdapter()
                }
                SettingsType.SHARE -> {
                    // Share the app
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome Flash Master app!")
                    startActivity(Intent.createChooser(shareIntent, "Share via"))
                }
            }
        }, themeHelper.isDarkMode(), notificationHelper.isNotificationEnabled(), alarmHelper.isAlarmEnabled())
        
        recyclerView.adapter = settingsAdapter
    }

    private fun toggleNotificationState() {
        val currentState = notificationHelper.isNotificationEnabled()
        notificationHelper.setNotificationEnabled(!currentState)
        // Refresh the adapter to update the switch state
        setupAdapter()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showTimePicker() {
        val currentTime = alarmHelper.getAlarmTime()
        val timeParts = currentTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                alarmHelper.setAlarmTime(formattedTime)
                // TODO: Set up the actual alarm with AlarmManager
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable notifications
                toggleNotificationState()
            } else {
                // Permission denied
                Toast.makeText(
                    requireContext(),
                    "Notification permission is required for this feature",
                    Toast.LENGTH_SHORT
                ).show()
                // Reset the switch state
                notificationHelper.setNotificationEnabled(false)
                setupAdapter()
            }
        }
    }
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_folders -> {
                    findNavController().navigate(R.id.homeFragment)
                    true
                }
                R.id.navigation_search -> {
                    findNavController().navigate(R.id.searchFragment)
                    true
                }
                R.id.navigation_settings -> {
                    // Already in settings view
                    true
                }
                else -> false
            }
        }
    }
} 