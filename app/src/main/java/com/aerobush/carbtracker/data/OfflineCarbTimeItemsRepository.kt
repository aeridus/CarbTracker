package com.aerobush.carbtracker.data

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aerobush.carbtracker.workers.HungryWorker
import com.aerobush.carbtracker.workers.StaleWorker
import kotlinx.coroutines.flow.Flow
import java.time.Duration

class OfflineCarbTimeItemsRepository(
    context: Context,
    private val carbTimeItemDao: CarbTimeItemDao
) : CarbTimeItemsRepository {
    override suspend fun insertItem(item: CarbTimeItem) = carbTimeItemDao.insert(item)

    override suspend fun updateItem(item: CarbTimeItem) = carbTimeItemDao.update(item)

    override suspend fun deleteItem(item: CarbTimeItem) = carbTimeItemDao.delete(item)

    override fun getItemStream(id: Long): Flow<CarbTimeItem?> = carbTimeItemDao.getItem(id)

    override fun getAllItemsStream(): Flow<List<CarbTimeItem>> = carbTimeItemDao.getAllItems()

    override fun getRecentItemsStream(time: Long): Flow<List<CarbTimeItem>> = carbTimeItemDao.getRecentItems(time)

    override suspend fun getLastItem() = carbTimeItemDao.getLastItem()

    override suspend fun deleteStaleItems(time: Long) = carbTimeItemDao.deleteStaleItems(time)

    // WorkManager
    private val workManager = WorkManager.getInstance(context)

    override fun startHungryWorker() {
        val hungryBuilder = PeriodicWorkRequestBuilder<HungryWorker>(
            repeatInterval = Duration.ofMinutes(5L)
        )
        workManager.enqueue(hungryBuilder.build())
    }

    override fun startStaleWorker() {
        val staleBuilder = PeriodicWorkRequestBuilder<StaleWorker>(
            repeatInterval = Duration.ofHours(5L)
        )
        workManager.enqueue(staleBuilder.build())
    }
}