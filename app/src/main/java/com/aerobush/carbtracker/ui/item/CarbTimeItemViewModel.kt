package com.aerobush.carbtracker.ui.item

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerobush.carbtracker.data.CarbTimeItem
import com.aerobush.carbtracker.data.CarbTimeItemsRepository
import com.aerobush.carbtracker.data.CarbTrackerConstants
import com.aerobush.carbtracker.data.TimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.OffsetDateTime

/**
 * ViewModel to validate and insert items in the Room database.
 */
class CarbTimeItemViewModel(
    private val carbTimeItemsRepository: CarbTimeItemsRepository
) : ViewModel() {
    private val dayThresholdHour = 4

/*    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val normalBuilder =
        NotificationCompat.Builder(this, CarbTrackerConstants.normalChannelId)
            .setContentTitle("Getting Hungry")
            .setContentText("It's safe to eat now.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)*/

    /**
     * The data is retrieved from [CarbTimeItemsRepository] and mapped to the UI state.
     */
    val uiState: StateFlow<CarbTrackerUiState> = carbTimeItemsRepository
        .getRecentItemsStream(TimeUtils.toEpochMilli(TimeUtils.getCurrentTime().minusDays(7)))
        .filterNotNull()
        .map { carbTimeItems ->
            if (carbTimeItems.isEmpty())
            {
                CarbTrackerUiState()
            }
            else {
                var idealMinCarbServingsPerMeal = 2
                var idealMaxCarbServingsPerMeal = 4

                var dayThreshold = TimeUtils.getCurrentTime()
                if (dayThreshold.hour < dayThresholdHour)
                {
                    dayThreshold = dayThreshold.minusDays(1)
                }
                dayThreshold = dayThreshold
                    .withHour(dayThresholdHour)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)

                val totalDayItems = carbTimeItems
                    .filter { TimeUtils.toOffsetDataTime(it.time) >= dayThreshold }
                    .size
                if (totalDayItems >= 3)
                {
                    // Any remaining meals for the day should be small
                    idealMinCarbServingsPerMeal = 1
                    idealMaxCarbServingsPerMeal = 2
                }

                CarbTrackerUiState(
                    lastTime = TimeUtils.toOffsetDataTime(carbTimeItems.last().time),
                    totalCarbServings = carbTimeItems.sumOf { it.carbServings },
                    idealMinCarbServingsPerMeal = idealMinCarbServingsPerMeal,
                    idealMaxCarbServingsPerMeal = idealMaxCarbServingsPerMeal
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = CarbTrackerUiState()
        )

    /**
     * Inserts a [CarbTimeItem] in the Room database
     */
    suspend fun saveCarbTimeItem(carbServings: Int) {
        val carbTimeItem = CarbTimeItem(
            time = TimeUtils.toEpochMilli(TimeUtils.getCurrentTime()),
            carbServings = carbServings,
            sentFirstReminder = false,
            sentSecondReminder = false
        )
        carbTimeItemsRepository.insertItem(carbTimeItem)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * UI state for CarbTracker
 */
data class CarbTrackerUiState(
    val lastTime: OffsetDateTime = OffsetDateTime.MIN,
    val totalCarbServings: Int = 0,
    val idealMinCarbServingsPerMeal: Int = 2,
    val idealMaxCarbServingsPerMeal: Int = 4,
)