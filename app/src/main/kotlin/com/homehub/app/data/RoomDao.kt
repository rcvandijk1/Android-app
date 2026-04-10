package com.homehub.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY position ASC, id ASC")
    fun observeAll(): Flow<List<Room>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    suspend fun getById(id: Long): Room?

    @Upsert
    suspend fun upsert(room: Room): Long

    @Delete
    suspend fun delete(room: Room)

    @Query("UPDATE rooms SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Int)
}
