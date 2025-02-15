package com.aerobush.carbtracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val carbTimeItemsRepository: CarbTimeItemsRepository
    val userPreferencesRepository: UserPreferencesRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineCarbTimeItemsRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [CarbTimeItemsRepository]
     */
    override val carbTimeItemsRepository: CarbTimeItemsRepository by lazy {
        val carbTimeItemsRepo = OfflineCarbTimeItemsRepository(
            context,
            CarbTrackerDatabase.getDatabase(context).carbTimeItemDao()
        )

        carbTimeItemsRepo.startHungryWorker()

        carbTimeItemsRepo
    }

    /**
     * Implementation for [UserPreferencesRepository]
     */
    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        val userPreferencesRepo = UserPreferencesRepository(
            context.dataStore
        )

        userPreferencesRepo
    }
}