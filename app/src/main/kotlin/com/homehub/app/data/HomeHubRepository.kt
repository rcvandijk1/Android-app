package com.homehub.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * A group of devices rendered as a section on the home screen.
 * A null [room] represents the "Unassigned" bucket.
 */
data class RoomGroup(
    val room: Room?,
    val devices: List<Device>,
)

class HomeHubRepository(
    private val deviceDao: DeviceDao,
    private val roomDao: RoomDao,
) {
    // Rooms --------------------------------------------------------------
    fun observeRooms(): Flow<List<Room>> = roomDao.observeAll()
    suspend fun getRoom(id: Long): Room? = roomDao.getById(id)
    suspend fun upsertRoom(room: Room): Long = roomDao.upsert(room)
    suspend fun deleteRoom(room: Room) = roomDao.delete(room)
    suspend fun updateRoomPosition(id: Long, position: Int) =
        roomDao.updatePosition(id, position)

    // Devices ------------------------------------------------------------
    fun observeDevices(): Flow<List<Device>> = deviceDao.observeAll()
    suspend fun getDevice(id: Long): Device? = deviceDao.getById(id)
    suspend fun upsertDevice(device: Device): Long = deviceDao.upsert(device)
    suspend fun deleteDevice(device: Device) = deviceDao.delete(device)
    suspend fun moveDeviceToRoom(deviceId: Long, roomId: Long?) =
        deviceDao.moveToRoom(deviceId, roomId)

    /**
     * Combines rooms and devices into an ordered list of [RoomGroup]s ready to
     * render. Rooms appear in their user-defined order, followed by an
     * "Unassigned" group (null room) if any devices lack a room.
     */
    fun observeGrouped(): Flow<List<RoomGroup>> =
        combine(roomDao.observeAll(), deviceDao.observeAll()) { rooms, devices ->
            val byRoom: Map<Long?, List<Device>> = devices.groupBy { it.roomId }
            buildList {
                for (room in rooms) {
                    add(
                        RoomGroup(
                            room = room,
                            devices = byRoom[room.id].orEmpty().sortedBy { it.position },
                        ),
                    )
                }
                val unassigned = byRoom[null].orEmpty().sortedBy { it.position }
                if (unassigned.isNotEmpty()) {
                    add(RoomGroup(room = null, devices = unassigned))
                }
            }
        }
}
