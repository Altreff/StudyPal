package com.example.flashmaster.Setting

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NotificationHelperTest {
    private lateinit var notificationHelper: NotificationHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        notificationHelper = NotificationHelper(context)
    }

    @Test
    fun testNotificationToggle() {
        notificationHelper.setNotificationEnabled(true)
        assertTrue(notificationHelper.isNotificationEnabled())
        notificationHelper.setNotificationEnabled(false)
        assertFalse(notificationHelper.isNotificationEnabled())
    }
} 