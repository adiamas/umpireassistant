package com.adiamas.umpireassistant.model

enum class Sport { BASEBALL, SOFTBALL, KICKBALL }

enum class FoulMode { NOT_COUNTED, ALWAYS_STRIKES, STRIKE_CAP, INDEPENDENT }

enum class VolumeAction { OFF, BALL, STRIKE, FOUL, OUT, RUN_SCORED, NEW_AT_BAT }

data class GameConfig(
    val homeTeamName: String = "Home",
    val awayTeamName: String = "Away",
    val sport: Sport = Sport.BASEBALL,
    val strikesPerOut: Int = 3,
    val ballsPerWalk: Int = 4,
    val outsPerInning: Int = 3,
    val inningsPerGame: Int = 7,
    val foulMode: FoulMode = FoulMode.ALWAYS_STRIKES,
    val maxFoulCount: Int = 2,
    val foulsPerOut: Int = 3,
    val volumeUp: VolumeAction = VolumeAction.OFF,
    val volumeDown: VolumeAction = VolumeAction.OFF,
    val gameLengthMinutes: Int = 45,
)

data class GameState(
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val inning: Int = 1,
    val isTopHalf: Boolean = true,
    val balls: Int = 0,
    val strikes: Int = 0,
    val fouls: Int = 0,
    val outs: Int = 0,
)
