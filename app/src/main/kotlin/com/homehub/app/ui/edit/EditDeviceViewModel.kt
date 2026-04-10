package com.homehub.app.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.data.Device
import com.homehub.app.data.HomeHubRepository
import com.homehub.app.data.Room
import com.homehub.app.icons.DeviceIcons
import com.homehub.app.launcher.InstalledApp
import com.homehub.app.launcher.InstalledAppsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditDeviceUiState(
    val deviceId: Long = 0L,
    val name: String = "",
    val iconKey: String = DeviceIcons.DEFAULT_KEY,
    val selectedApp: InstalledApp? = null,
    val roomId: Long? = null,
    val isEditMode: Boolean = false,
    val installedApps: List<InstalledApp> = emptyList(),
    val installedAppsLoading: Boolean = false,
)

class EditDeviceViewModel(
    private val repository: HomeHubRepository,
    private val installedAppsProvider: InstalledAppsProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(EditDeviceUiState())
    val state: StateFlow<EditDeviceUiState> = _state.asStateFlow()

    val rooms: StateFlow<List<Room>> =
        repository.observeRooms()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Call once from the screen; safe to call again — it no-ops if already loaded. */
    fun load(deviceId: Long) {
        if (_state.value.deviceId == deviceId && _state.value.isEditMode) return
        viewModelScope.launch {
            val existing = if (deviceId > 0) repository.getDevice(deviceId) else null
            val resolvedApp = existing?.let { installedAppsProvider.resolve(it.packageName) }
            _state.value = EditDeviceUiState(
                deviceId = existing?.id ?: 0L,
                name = existing?.name.orEmpty(),
                iconKey = existing?.iconKey ?: DeviceIcons.DEFAULT_KEY,
                selectedApp = resolvedApp,
                roomId = existing?.roomId,
                isEditMode = existing != null,
            )
        }
    }

    fun onNameChanged(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun onIconSelected(key: String) {
        _state.value = _state.value.copy(iconKey = key)
    }

    fun onAppSelected(app: InstalledApp) {
        _state.value = _state.value.copy(selectedApp = app)
    }

    fun onRoomSelected(roomId: Long?) {
        _state.value = _state.value.copy(roomId = roomId)
    }

    fun ensureInstalledAppsLoaded() {
        if (_state.value.installedApps.isNotEmpty() || _state.value.installedAppsLoading) return
        _state.value = _state.value.copy(installedAppsLoading = true)
        viewModelScope.launch {
            val list = installedAppsProvider.list()
            _state.value = _state.value.copy(
                installedApps = list,
                installedAppsLoading = false,
            )
        }
    }

    /** Persists the device. Returns true if save happened, false if validation failed. */
    fun save(onSaved: () -> Unit) {
        val current = _state.value
        val app = current.selectedApp ?: return
        if (current.name.isBlank()) return
        viewModelScope.launch {
            val device = Device(
                id = current.deviceId,
                name = current.name.trim(),
                packageName = app.packageName,
                appLabel = app.label,
                iconKey = current.iconKey,
                roomId = current.roomId,
                position = 0,
            )
            repository.upsertDevice(device)
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = _state.value.deviceId
        if (id <= 0L) { onDeleted(); return }
        viewModelScope.launch {
            val device = repository.getDevice(id) ?: return@launch
            repository.deleteDevice(device)
            onDeleted()
        }
    }

    suspend fun createRoom(name: String): Long {
        val nextPosition = rooms.value.size
        return repository.upsertRoom(Room(name = name, position = nextPosition))
    }
}
