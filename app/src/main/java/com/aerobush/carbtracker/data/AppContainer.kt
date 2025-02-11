package com.aerobush.carbtracker.data

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val carbTimeItemsRepository: CarbTimeItemsRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineCarbTimeItemsRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [CarbTimeItemsRepository]
     */
    override val carbTimeItemsRepository: CarbTimeItemsRepository by lazy {
        OfflineCarbTimeItemsRepository(CarbTrackerDatabase.getDatabase(context).carbTimeItemDao())
    }
}