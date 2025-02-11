package com.aerobush.carbtracker.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [CarbTimeItem] from a given data source.
 */
interface CarbTimeItemsRepository {
    /**
     * Retrieve all the items from the the given data source.
     */
    fun getAllItemsStream(): Flow<List<CarbTimeItem>>

    /**
     * Retrieve all the stale items from the the given data source.
     */
    fun getStaleItemsStream(time: Long): Flow<List<CarbTimeItem>>

    /**
     * Retrieve all the recent items from the the given data source.
     */
    fun getRecentItemsStream(time: Long): Flow<List<CarbTimeItem>>

    /**
     * Retrieve an item from the given data source that matches with the [id].
     */
    fun getItemStream(id: Long): Flow<CarbTimeItem?>

    /**
     * Insert item in the data source
     */
    suspend fun insertItem(item: CarbTimeItem)

    /**
     * Delete item from the data source
     */
    suspend fun deleteItem(item: CarbTimeItem)

    /**
     * Update item in the data source
     */
    suspend fun updateItem(item: CarbTimeItem)
}