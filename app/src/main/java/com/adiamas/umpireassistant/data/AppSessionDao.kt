package com.adiamas.umpireassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSessionDao {
    @Query("SELECT * FROM app_session WHERE id = 1")
    fun getSession(): Flow<AppSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(session: AppSessionEntity)
}
