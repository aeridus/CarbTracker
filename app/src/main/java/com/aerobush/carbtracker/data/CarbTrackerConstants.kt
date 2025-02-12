package com.aerobush.carbtracker.data

class CarbTrackerConstants {
    companion object {
        // Notifications
        const val NORMAL_CHANNEL_ID = "Normal"
        const val URGENT_CHANNEL_ID = "Urgent"

        // Carb Servings
        const val MIN_CARB_SERVINGS_PER_MEAL = 2
        const val MAX_CARB_SERVINGS_PER_MEAL = 4
        const val MIN_CARB_SERVINGS_PER_SNACK = 1
        const val MAX_CARB_SERVINGS_PER_SNACK = 2

        // Potential User Preferences
        const val DEFAULT_DAY_THRESHOLD_HOUR = 4L
    }
}