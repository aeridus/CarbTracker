package com.aerobush.carbtracker.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aerobush.carbtracker.R
import com.aerobush.carbtracker.data.AppDataContainer
import com.aerobush.carbtracker.data.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "StaleWorker"

class StaleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            delay(DELAY_TIME_MILLIS)

            return@withContext try {
                val carbTimeItemsRepo = AppDataContainer(applicationContext).carbTimeItemsRepository

                carbTimeItemsRepo.deleteStaleItems(
                    TimeUtils.toEpochMilli(TimeUtils.getCurrentTime().minusDays(7)))

                Result.success()
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.failed_to_delete_stale_items),
                    throwable
                )

                Result.failure()
            }
        }
    }

    companion object {
        const val DELAY_TIME_MILLIS = 15_000L
    }
}