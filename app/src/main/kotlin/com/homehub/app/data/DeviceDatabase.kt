package com.homehub.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room as RoomKt
import androidx.room.RoomDatabase

@Database(
    entities = [Device::class, Room::class],
    version = 1,
    exportSchema = false,
)
abstract class DeviceDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun roomDao(): RoomDao

    companion object {
        @Volatile private var instance: DeviceDatabase? = null

        fun get(context: Context): DeviceDatabase =
            instance ?: synchronized(this) {
                instance ?: RoomKt.databaseBuilder(
                    context.applicationContext,
                    DeviceDatabase::class.java,
                    "homehub.db",
                ).build().also { instance = it }
            }
    }
}
