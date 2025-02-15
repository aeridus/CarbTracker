package com.aerobush.carbtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.aerobush.carbtracker.data.AppContainer
import com.aerobush.carbtracker.data.AppDataContainer
import com.aerobush.carbtracker.data.CarbTrackerConstants
import com.aerobush.carbtracker.data.UserPreferencesRepository

private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

class CarbTrackerApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()

        container = AppDataContainer(this)

        userPreferencesRepository = UserPreferencesRepository(dataStore)

        val normalChannel = NotificationChannel(
            CarbTrackerConstants.NORMAL_CHANNEL_ID,
            getString(R.string.normal_reminders_title),
            NotificationManager.IMPORTANCE_HIGH
        )
        normalChannel.description = getString(R.string.normal_reminders)

        val urgentChannel = NotificationChannel(
            CarbTrackerConstants.URGENT_CHANNEL_ID,
            getString(R.string.urgent_reminders_title),
            NotificationManager.IMPORTANCE_HIGH
        )
        urgentChannel.description = getString(R.string.urgent_reminders)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(normalChannel)
        notificationManager.createNotificationChannel(urgentChannel)
    }
}