package com.adiamas.umpireassistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [StoredConfigEntity::class, TeamEntity::class, AppSessionEntity::class],
    version = 5,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storedConfigDao(): StoredConfigDao
    abstract fun teamDao(): TeamDao
    abstract fun appSessionDao(): AppSessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "umpire_assistant.db")
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
