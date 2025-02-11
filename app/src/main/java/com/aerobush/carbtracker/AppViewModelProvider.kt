package com.aerobush.carbtracker

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.aerobush.carbtracker.ui.item.CarbTimeItemViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Inventory app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for CarbTimeItemViewModel
        initializer {
            CarbTimeItemViewModel(
                carbTrackerApplication().container.carbTimeItemsRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [CarbTrackerApplication].
 */
fun CreationExtras.carbTrackerApplication(): CarbTrackerApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as CarbTrackerApplication)