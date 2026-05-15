package com.adiamas.umpireassistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AppSessionEntity::class],
    version = 1,
)
abstract class SessionDatabase : RoomDatabase() {
    abstract fun appSessionDao(): AppSessionDao

    companion object {
        @Volatile private var INSTANCE: SessionDatabase? = null

        fun getInstance(context: Context): SessionDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, SessionDatabase::class.java, "umpire_assistant_session.db")
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
