package com.homehub.app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.data.Device
import com.homehub.app.data.HomeHubRepository
import com.homehub.app.data.Room
import com.homehub.app.data.RoomGroup
import com.homehub.app.launcher.AppLauncher
import com.homehub.app.launcher.InstalledApp
import com.homehub.app.launcher.InstalledAppsProvider
import com.homehub.app.launcher.LaunchResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

sealed interface HomeEvent {
    data class LaunchFailed(val deviceId: Long) : HomeEvent
}

class HomeViewModel(
    private val repository: HomeHubRepository,
    private val installedAppsProvider: InstalledAppsProvider,
) : ViewModel() {

    val groups: StateFlow<List<RoomGroup>> =
        repository.observeGrouped()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val rooms: StateFlow<List<Room>> =
        repository.observeRooms()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Cache of resolved linked-app metadata (label + icon) keyed by package.
     * Populated lazily as tiles appear, so we don't block the list on
     * PackageManager I/O for every device at startup.
     */
    private val _appMetadata = MutableStateFlow<Map<String, InstalledApp?>>(emptyMap())
    val appMetadata: StateFlow<Map<String, InstalledApp?>> = _appMetadata.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    fun ensureAppMetadata(packageName: String) {
        if (_appMetadata.value.containsKey(packageName)) return
        viewModelScope.launch {
            val resolved = installedAppsProvider.resolve(packageName)
            _appMetadata.value = _appMetadata.value + (packageName to resolved)
        }
    }

    fun onDeviceTapped(context: Context, device: Device) {
        when (AppLauncher.launch(context, device.packageName)) {
            LaunchResult.Success -> Unit
            LaunchResult.NotFound -> {
                viewModelScope.launch { _events.emit(HomeEvent.LaunchFailed(device.id)) }
            }
        }
    }

    fun deleteDevice(device: Device) {
        viewModelScope.launch { repository.deleteDevice(device) }
    }

    fun moveDeviceToRoom(device: Device, roomId: Long?) {
        viewModelScope.launch { repository.moveDeviceToRoom(device.id, roomId) }
    }

    /**
     * Creates a new room and returns its generated id, so callers (e.g. the
     * RoomPickerSheet) can immediately select it.
     */
    suspend fun createRoom(name: String): Long = withContext(Dispatchers.Default) {
        val nextPosition = rooms.value.size
        repository.upsertRoom(Room(name = name, position = nextPosition))
    }
}
