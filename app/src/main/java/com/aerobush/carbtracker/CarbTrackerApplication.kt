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

        val defaultChannel = NotificationChannel(
            CarbTrackerConstants.normalChannelId,
            "Normal priority notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val highChannel = NotificationChannel(
            CarbTrackerConstants.highChannelId,
            "High priority notifications",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(defaultChannel)
        notificationManager.createNotificationChannel(highChannel)
    }
}