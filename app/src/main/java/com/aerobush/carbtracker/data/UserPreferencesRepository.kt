package com.aerobush.carbtracker.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/*
 * Concrete class implementation to access data store
 */
class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val DAY_THRESHOLD_HOUR_KEY = intPreferencesKey("day_threshold_hour")
        const val TAG = "UserPreferencesRepo"
    }

    val themeMode: Flow<ThemeMode> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            val currentThemeString = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.toString()

            when (currentThemeString) {
                ThemeMode.DARK.toString() -> ThemeMode.DARK
                ThemeMode.LIGHT.toString() -> ThemeMode.LIGHT
                else -> ThemeMode.SYSTEM
            }
        }

    val dayThresholdHour: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[DAY_THRESHOLD_HOUR_KEY] ?: CarbTrackerConstants.DEFAULT_DAY_THRESHOLD_HOUR
        }

    suspend fun saveThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.toString()
        }
    }

    suspend fun saveDayThresholdHour(dayThresholdHour: Int) {
        dataStore.edit { preferences ->
            preferences[DAY_THRESHOLD_HOUR_KEY] = dayThresholdHour
        }
    }
}
