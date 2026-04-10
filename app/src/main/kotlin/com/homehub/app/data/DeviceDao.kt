package com.homehub.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY position ASC, id ASC")
    fun observeAll(): Flow<List<Device>>

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: Long): Device?

    @Upsert
    suspend fun upsert(device: Device): Long

    @Delete
    suspend fun delete(device: Device)

    @Query("UPDATE devices SET roomId = :roomId WHERE id = :deviceId")
    suspend fun moveToRoom(deviceId: Long, roomId: Long?)
}
