package com.homehub.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.homehub.app.HomeHubApp
import com.homehub.app.ui.edit.EditDeviceViewModel
import com.homehub.app.ui.home.HomeViewModel
import com.homehub.app.ui.rooms.ManageRoomsViewModel

/**
 * Single place that builds all ViewModels from the application-scoped
 * repository + InstalledAppsProvider. Using [viewModelFactory] keeps the
 * wiring explicit without dragging in Hilt/Koin.
 */
object HomeHubViewModels {

    private fun CreationExtras.app(): HomeHubApp =
        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HomeHubApp

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                repository = app().repository,
                installedAppsProvider = app().installedAppsProvider,
            )
        }
        initializer {
            EditDeviceViewModel(
                repository = app().repository,
                installedAppsProvider = app().installedAppsProvider,
            )
        }
        initializer {
            ManageRoomsViewModel(app().repository)
        }
    }
}
