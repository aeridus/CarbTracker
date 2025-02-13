package com.aerobush.carbtracker.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerobush.carbtracker.data.CarbTimeItem
import com.aerobush.carbtracker.data.CarbTimeItemsRepository
import com.aerobush.carbtracker.data.CarbTrackerConstants
import com.aerobush.carbtracker.data.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.OffsetDateTime

/**
 * ViewModel to manage carb time items in the Room database.
 */
class CarbTimeItemViewModel(
    private val carbTimeItemsRepository: CarbTimeItemsRepository
) : ViewModel() {
    /**
     * Changes frequently
     */
    private val _currentTimeState = MutableStateFlow<OffsetDateTime>(TimeUtils.getCurrentTime())
    private val currentTimeState: StateFlow<OffsetDateTime> = _currentTimeState

    init {
        viewModelScope.launch {
            while (isActive) {
                _currentTimeState.value = TimeUtils.getCurrentTime()
                delay(REFRESH_MILLIS)
            }
        }
    }

    /**
     * Changes less often
     */
    private val carbTimeItemsState: StateFlow<List<CarbTimeItem>> = carbTimeItemsRepository
        .getAllItemsStream()
        .filterNotNull()
        .map { carbTimeItems ->
            carbTimeItems
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf<CarbTimeItem>()
        )

    /**
     * We want this to update with time changes or database changes
     */
    val uiState: StateFlow<CarbTrackerUiState> = currentTimeState
        .combine(carbTimeItemsState) { currentTime, carbTimeItems ->
            if (carbTimeItems.isEmpty())
            {
                CarbTrackerUiState()
            }
            else {
                val lastMealTime = TimeUtils.toOffsetDateTime(carbTimeItems.last().time)
                val dayThreshold = TimeUtils.getDayThresholdEpochMilli(
                    CarbTrackerConstants.DEFAULT_DAY_THRESHOLD_HOUR
                )

                var totalHours = 24
                var totalMinutes = 0
                TimeUtils.getDurationParts(
                    startTime = lastMealTime,
                    endTime = currentTime,
                    output =  { hours, minutes ->
                        totalHours = hours.toInt()
                        totalMinutes = minutes.toInt()
                    }
                )

                var idealMinCarbServingsPerMeal = CarbTrackerConstants.MIN_CARB_SERVINGS_PER_MEAL
                var idealMaxCarbServingsPerMeal = CarbTrackerConstants.MAX_CARB_SERVINGS_PER_MEAL

                val totalDayItems = carbTimeItems
                    .filter { it.time >= dayThreshold }
                    .size
                if (totalDayItems >= 3)
                {
                    // Any remaining meals for the day should be small
                    idealMinCarbServingsPerMeal = CarbTrackerConstants.MIN_CARB_SERVINGS_PER_SNACK
                    idealMaxCarbServingsPerMeal = CarbTrackerConstants.MAX_CARB_SERVINGS_PER_SNACK
                }

                // Calculate ideal carb servings per week
                val totalDays = Duration.between(
                    TimeUtils.toOffsetDateTime(carbTimeItems.first().time).toLocalDateTime(),
                    TimeUtils.toOffsetDateTime(carbTimeItems.last().time).toLocalDateTime()
                ).toDays().toInt() + 1
                val idealMinCarbServingsPerWeek =
                    (3 * CarbTrackerConstants.MIN_CARB_SERVINGS_PER_MEAL +
                            CarbTrackerConstants.MIN_CARB_SERVINGS_PER_SNACK) * totalDays
                val idealMaxCarbServingsPerWeek =
                    (3 * CarbTrackerConstants.MAX_CARB_SERVINGS_PER_MEAL +
                            CarbTrackerConstants.MAX_CARB_SERVINGS_PER_SNACK) * totalDays

                CarbTrackerUiState(
                    totalHours = totalHours,
                    totalMinutes = totalMinutes,
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
        private const val REFRESH_MILLIS = 15_000L
    }
}

/**
 * UI state for CarbTracker
 */
data class CarbTrackerUiState(
    val totalHours: Int = 24,
    val totalMinutes: Int = 0,
    val totalCarbServings: Int = 0,
    val idealMinCarbServingsPerMeal: Int = 2,
    val idealMaxCarbServingsPerMeal: Int = 4,
    val idealMinCarbServingsPerWeek: Int = 7,
    val idealMaxCarbServingsPerWeek: Int = 14
)