package com.aerobush.carbtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.aerobush.carbtracker.data.AppContainer
import com.aerobush.carbtracker.data.AppDataContainer
import com.aerobush.carbtracker.data.CarbTrackerConstants

class CarbTrackerApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        container = AppDataContainer(this)

        val normalChannel = NotificationChannel(
            CarbTrackerConstants.NORMAL_CHANNEL_ID,
            getString(R.string.normal_reminders),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        normalChannel.description = getString(R.string.normal_reminders)

        val urgentChannel = NotificationChannel(
            CarbTrackerConstants.URGENT_CHANNEL_ID,
            getString(R.string.urgent_reminders),
            NotificationManager.IMPORTANCE_HIGH
        )
        urgentChannel.description = getString(R.string.urgent_reminders)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(normalChannel)
        notificationManager.createNotificationChannel(urgentChannel)
    }
}