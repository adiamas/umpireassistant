package com.adiamas.umpireassistant.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams WHERE configId = :configId ORDER BY name ASC")
    fun getTeamsForConfig(configId: Int): Flow<List<TeamEntity>>

    @Insert
    suspend fun insert(team: TeamEntity)

    @Update
    suspend fun update(team: TeamEntity)

    @Delete
    suspend fun delete(team: TeamEntity)
}
