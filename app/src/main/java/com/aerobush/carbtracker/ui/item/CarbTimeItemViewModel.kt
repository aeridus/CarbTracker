package com.aerobush.carbtracker.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerobush.carbtracker.data.CarbTimeItem
import com.aerobush.carbtracker.data.CarbTimeItemsRepository
import com.aerobush.carbtracker.data.CarbTrackerConstants
import com.aerobush.carbtracker.data.TimeUtils
import com.aerobush.carbtracker.data.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.OffsetDateTime

/**
 * ViewModel to manage carb time items in the Room database.
 */
class CarbTimeItemViewModel(
    private val carbTimeItemsRepository: CarbTimeItemsRepository,
    private val userPreferencesRepository: UserPreferencesRepository
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
     * Changes based on user input
     */
    private val dayThresholdHourState: StateFlow<Int> =
        userPreferencesRepository.dayThresholdHour.map { dayThresholdHour ->
            dayThresholdHour
        }.stateIn(
            scope = viewModelScope,
            // Flow is set to emits value for when app is on the foreground
            // 5 seconds stop delay is added to ensure it flows continuously
            // for cases such as configuration change
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = runBlocking {
                userPreferencesRepository.dayThresholdHour.first()
            }
        )

    fun updateDayThresholdHour(newHour : Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveDayThresholdHour(newHour)
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
     * We want this to update with time changes, user input, or database changes
     */
    val uiState: StateFlow<CarbTrackerUiState> = combine(
        currentTimeState, dayThresholdHourState, carbTimeItemsState
    ) { currentTime, dayThresholdHour, carbTimeItems ->
            val dayThreshold = TimeUtils.getDayThresholdEpochMilli(
                dayThresholdHour
            )
            val weekAgoThreshold = dayThreshold - Duration.ofDays(7).toMillis()
            val carbWeekItems = carbTimeItems.filter { it.time >= weekAgoThreshold }

            if (carbWeekItems.isEmpty())
            {
                CarbTrackerUiState()
            }
            else {
                val lastMealTime = TimeUtils.toOffsetDateTime(carbWeekItems.last().time)

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

                val totalDayItems = carbWeekItems
                    .filter { it.time >= dayThreshold }
                    .size
                if (totalDayItems >= 3)
                {
                    // Any remaining meals for the day should be small
                    idealMinCarbServingsPerMeal = CarbTrackerConstants.MIN_CARB_SERVINGS_PER_SNACK
                    idealMaxCarbServingsPerMeal = CarbTrackerConstants.MAX_CARB_SERVINGS_PER_SNACK
                }

                // Calculate ideal carb servings per week
                val firstCarbTime = carbWeekItems.first().time
                val millisPerDay = Duration.ofDays(1).toMillis()
                val totalEmptyDays = (firstCarbTime - weekAgoThreshold) / millisPerDay
                val totalDays = 7 - totalEmptyDays.toInt() + 1

                val idealMinCarbServingsPerWeek =
                    (3 * CarbTrackerConstants.MIN_CARB_SERVINGS_PER_MEAL +
                            CarbTrackerConstants.MIN_CARB_SERVINGS_PER_SNACK) * totalDays
                val idealMaxCarbServingsPerWeek =
                    (3 * CarbTrackerConstants.MAX_CARB_SERVINGS_PER_MEAL +
                            CarbTrackerConstants.MAX_CARB_SERVINGS_PER_SNACK) * totalDays

                CarbTrackerUiState(
                    dayThresholdHour = dayThresholdHour,
                    totalHours = totalHours,
                    totalMinutes = totalMinutes,
                    totalCarbServings = carbWeekItems.sumOf { it.carbServings },
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
    val dayThresholdHour: Int = CarbTrackerConstants.DEFAULT_DAY_THRESHOLD_HOUR,
    val totalHours: Int = 24,
    val totalMinutes: Int = 0,
    val totalCarbServings: Int = 0,
    val idealMinCarbServingsPerMeal: Int = 2,
    val idealMaxCarbServingsPerMeal: Int = 4,
    val idealMinCarbServingsPerWeek: Int = 7,
    val idealMaxCarbServingsPerWeek: Int = 14
)