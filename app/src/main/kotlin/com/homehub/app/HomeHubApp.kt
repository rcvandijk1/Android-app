package com.homehub.app

import android.app.Application
import com.homehub.app.data.DeviceDatabase
import com.homehub.app.data.HomeHubRepository
import com.homehub.app.launcher.InstalledAppsProvider

/**
 * Application class acts as a minimal service locator so the whole app can
 * share a single Room database, repository and PackageManager wrapper
 * without pulling in a DI framework.
 */
class HomeHubApp : Application() {

    val database: DeviceDatabase by lazy { DeviceDatabase.get(this) }

    val repository: HomeHubRepository by lazy {
        HomeHubRepository(
            deviceDao = database.deviceDao(),
            roomDao = database.roomDao(),
        )
    }

    val installedAppsProvider: InstalledAppsProvider by lazy {
        InstalledAppsProvider(this)
    }
}
