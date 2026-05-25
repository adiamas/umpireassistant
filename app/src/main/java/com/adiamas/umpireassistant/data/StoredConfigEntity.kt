package com.adiamas.umpireassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stored_configs")
data class StoredConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isDefault: Boolean = false,
    val strikesPerOut: Int = 3,
    val ballsPerWalk: Int = 4,
    val outsPerInning: Int = 3,
    val inningsPerGame: Int = 7,
    val foulMode: String = "ALWAYS_STRIKES",
    val maxFoulCount: Int = 2,
    val foulsPerOut: Int = 3,
    val volumeUp: String = "OFF",
    val volumeDown: String = "OFF",
    val volumeUpLong: String = "OFF",
    val volumeDownLong: String = "OFF",
    val gameLengthMinutes: Int = 45,
    val scrollTeamNames: Boolean = true,
    val largeButtonLayout: Boolean = false,
)
