package com.homehub.app.ui.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homehub.app.data.HomeHubRepository
import com.homehub.app.data.Room
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ManageRoomsViewModel(
    private val repository: HomeHubRepository,
) : ViewModel() {

    val rooms: StateFlow<List<Room>> =
        repository.observeRooms()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createRoom(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val nextPosition = rooms.value.size
            repository.upsertRoom(Room(name = trimmed, position = nextPosition))
        }
    }

    fun renameRoom(room: Room, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed == room.name) return
        viewModelScope.launch {
            repository.upsertRoom(room.copy(name = trimmed))
        }
    }

    fun deleteRoom(room: Room) {
        viewModelScope.launch { repository.deleteRoom(room) }
    }

    /**
     * Swaps a room with its neighbour at [delta] (−1 = up, +1 = down) and
     * persists the new positions. Simple and sufficient for a handful of rooms;
     * full drag-reorder can be added later.
     */
    fun move(room: Room, delta: Int) {
        val current = rooms.value
        val idx = current.indexOfFirst { it.id == room.id }
        val targetIdx = idx + delta
        if (idx < 0 || targetIdx !in current.indices) return
        val other = current[targetIdx]
        viewModelScope.launch {
            repository.updateRoomPosition(room.id, targetIdx)
            repository.updateRoomPosition(other.id, idx)
        }
    }
}
