package com.aerobush.carbtracker.data

import kotlinx.coroutines.flow.Flow

class OfflineCarbTimeItemsRepository(private val carbTimeItemDao: CarbTimeItemDao) : CarbTimeItemsRepository {
    override fun getAllItemsStream(): Flow<List<CarbTimeItem>> = carbTimeItemDao.getAllItems()

    override fun getStaleItemsStream(time: Long): Flow<List<CarbTimeItem>> = carbTimeItemDao.getStaleItems(time)

    override fun getRecentItemsStream(time: Long): Flow<List<CarbTimeItem>> = carbTimeItemDao.getRecentItems(time)

    override fun getItemStream(id: Long): Flow<CarbTimeItem?> = carbTimeItemDao.getItem(id)

    override suspend fun insertItem(item: CarbTimeItem) = carbTimeItemDao.insert(item)

    override suspend fun deleteItem(item: CarbTimeItem) = carbTimeItemDao.delete(item)

    override suspend fun updateItem(item: CarbTimeItem) = carbTimeItemDao.update(item)
}