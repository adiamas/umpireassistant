package com.adiamas.umpireassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_session")
data class AppSessionEntity(
    @PrimaryKey val id: Int = 1,
    val activeConfigId: Int = 0,
    val homeTeamId: Int? = null,
    val homeTeamName: String = "Home",
    val homeTeamColor: Int? = null,
    val awayTeamId: Int? = null,
    val awayTeamName: String = "Away",
    val awayTeamColor: Int? = null,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val inning: Int = 1,
    val isTopHalf: Boolean = true,
    val balls: Int = 0,
    val strikes: Int = 0,
    val fouls: Int = 0,
    val outs: Int = 0,
    val timerSeconds: Int = -1,
)
