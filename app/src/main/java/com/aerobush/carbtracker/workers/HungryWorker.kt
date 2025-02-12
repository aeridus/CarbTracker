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

private const val TAG = "HungryWorker"

class HungryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            delay(DELAY_TIME_MILLIS)

            return@withContext try {
                val carbTimeItemsRepo = AppDataContainer(applicationContext).carbTimeItemsRepository

                val lastCarbTimeItem = carbTimeItemsRepo.getLastItem()
                if (lastCarbTimeItem != null)
                {
                    val lastCarbTime = TimeUtils.toOffsetDateTime(lastCarbTimeItem.time)

                    var totalHours = 24L
                    TimeUtils.getDurationParts(
                        startTime = lastCarbTime,
                        endTime = TimeUtils.getCurrentTime(),
                        output =  { hours, _ ->
                            totalHours = hours
                        }
                    )

                    if ((totalHours >= 4) && (!lastCarbTimeItem.sentSecondReminder))
                    {
                        WorkerUtils.makeUrgentNotification(
                            title = applicationContext.resources.getString(R.string.time_to_eat_title),
                            message = applicationContext.resources.getString(R.string.time_to_eat),
                            context = applicationContext
                        )

                        lastCarbTimeItem.sentFirstReminder = true
                        lastCarbTimeItem.sentSecondReminder = true

                        carbTimeItemsRepo.updateItem(lastCarbTimeItem)
                    }
                    else if ((totalHours >= 3) && (!lastCarbTimeItem.sentFirstReminder))
                    {
                        WorkerUtils.makeNormalNotification(
                            title = applicationContext.resources.getString(R.string.safe_to_eat_title),
                            message = applicationContext.resources.getString(R.string.safe_to_eat),
                            context = applicationContext
                        )

                        lastCarbTimeItem.sentFirstReminder = true

                        carbTimeItemsRepo.updateItem(lastCarbTimeItem)
                    }
                }

                Result.success()
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.failed_to_notify_user),
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