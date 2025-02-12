package com.aerobush.carbtracker.ui.item

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
import java.time.Duration
import java.time.OffsetDateTime

/**
 * ViewModel to validate and insert items in the Room database.
 */
class CarbTimeItemViewModel(
    private val carbTimeItemsRepository: CarbTimeItemsRepository
) : ViewModel() {
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
                var idealMinCarbServingsPerMeal = CarbTrackerConstants.MIN_CARB_SERVINGS_PER_MEAL
                var idealMaxCarbServingsPerMeal = CarbTrackerConstants.MAX_CARB_SERVINGS_PER_MEAL

                val dayThreshold = TimeUtils.getDayThreshold(
                    CarbTrackerConstants.DEFAULT_DAY_THRESHOLD_HOUR
                )

                val totalDayItems = carbTimeItems
                    .filter { TimeUtils.toOffsetDataTime(it.time) >= dayThreshold }
                    .size
                if (totalDayItems >= 3)
                {
                    // Any remaining meals for the day should be small
                    idealMinCarbServingsPerMeal = CarbTrackerConstants.MIN_CARB_SERVINGS_PER_SNACK
                    idealMaxCarbServingsPerMeal = CarbTrackerConstants.MAX_CARB_SERVINGS_PER_SNACK
                }

                val totalDays = Duration.between(
                    TimeUtils.toOffsetDataTime(carbTimeItems.first().time).toLocalDateTime(),
                    TimeUtils.toOffsetDataTime(carbTimeItems.last().time).toLocalDateTime()
                ).toDays().toInt() + 1
                val idealMinCarbServingsPerWeek =
                    (3 * CarbTrackerConstants.MIN_CARB_SERVINGS_PER_MEAL +
                    CarbTrackerConstants.MIN_CARB_SERVINGS_PER_SNACK) * totalDays
                val idealMaxCarbServingsPerWeek =
                    (3 * CarbTrackerConstants.MAX_CARB_SERVINGS_PER_MEAL +
                            CarbTrackerConstants.MAX_CARB_SERVINGS_PER_SNACK) * totalDays

                CarbTrackerUiState(
                    lastMealTime = TimeUtils.toOffsetDataTime(carbTimeItems.last().time),
                    totalCarbServings = carbTimeItems.sumOf { it.carbServings },
                    idealMinCarbServingsPerMeal = idealMinCarbServingsPerMeal,
                    idealMaxCarbServingsPerMeal = idealMaxCarbServingsPerMeal,
                    idealMinCarbServingsPerWeek = idealMinCarbServingsPerWeek,
                    idealMaxCarbServingsPerWeek = idealMaxCarbServingsPerWeek
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
    val lastMealTime: OffsetDateTime = OffsetDateTime.MIN,
    val totalCarbServings: Int = 0,
    val idealMinCarbServingsPerMeal: Int = 2,
    val idealMaxCarbServingsPerMeal: Int = 4,
    val idealMinCarbServingsPerWeek: Int = 7,
    val idealMaxCarbServingsPerWeek: Int = 14
)