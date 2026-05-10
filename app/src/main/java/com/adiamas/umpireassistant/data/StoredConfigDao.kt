package com.adiamas.umpireassistant.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StoredConfigDao {
    @Query("SELECT * FROM stored_configs ORDER BY name ASC")
    fun getAllConfigs(): Flow<List<StoredConfigEntity>>

    @Query("SELECT * FROM stored_configs WHERE id = :id")
    suspend fun getById(id: Int): StoredConfigEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(config: StoredConfigEntity): Long

    @Update
    suspend fun update(config: StoredConfigEntity)

    @Delete
    suspend fun delete(config: StoredConfigEntity)

    @Query("SELECT COUNT(*) FROM stored_configs")
    suspend fun count(): Int
}
