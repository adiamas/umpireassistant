package com.adiamas.umpireassistant.viewmodel

import androidx.lifecycle.ViewModel
import com.adiamas.umpireassistant.model.GameConfig
import com.adiamas.umpireassistant.model.GameState
import com.adiamas.umpireassistant.model.Sport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {
    private val _config = MutableStateFlow(GameConfig())
    val config: StateFlow<GameConfig> = _config.asStateFlow()

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private fun update(block: GameState.() -> GameState) {
        _state.value = _state.value.block()
    }

    private fun updateConfig(block: GameConfig.() -> GameConfig) {
        _config.value = _config.value.block()
    }

    fun incrementBalls() {
        val newBalls = _state.value.balls + 1
        if (newBalls >= 4) {
            resetPitchCount()
        } else {
            update { copy(balls = newBalls) }
        }
    }

    fun incrementStrikes() {
        val newStrikes = _state.value.strikes + 1
        if (newStrikes >= 3) {
            incrementOuts()
        } else {
            update { copy(strikes = newStrikes) }
        }
    }

    fun incrementFouls() {
        val newFouls = _state.value.fouls + 1
        if (newFouls >= 4) {
            incrementOuts()
        } else {
            update { copy(fouls = newFouls) }
        }
    }

    fun incrementOuts() {
        val newOuts = _state.value.outs + 1
        if (newOuts >= 3) {
            advanceHalf()
        } else {
            update { copy(outs = newOuts, balls = 0, strikes = 0, fouls = 0) }
        }
    }

    fun addRun() {
        if (_state.value.isTopHalf) {
            update { copy(awayScore = awayScore + 1) }
        } else {
            update { copy(homeScore = homeScore + 1) }
        }
        resetPitchCount()
    }

    fun resetPitchCount() = update { copy(balls = 0, strikes = 0, fouls = 0) }

    fun advanceHalf() {
        if (_state.value.isTopHalf) {
            update { copy(isTopHalf = false, balls = 0, strikes = 0, fouls = 0, outs = 0) }
        } else {
            update { copy(isTopHalf = true, inning = inning + 1, balls = 0, strikes = 0, fouls = 0, outs = 0) }
        }
    }

    fun updateTeamNames(homeName: String, awayName: String) =
        updateConfig {
            copy(
                homeTeamName = homeName.ifBlank { "Home" },
                awayTeamName = awayName.ifBlank { "Away" },
            )
        }

    fun updateSport(sport: Sport) = updateConfig { copy(sport = sport) }

    fun updateStrikesPerOut(value: Int) = updateConfig {
        val newValue = value.coerceIn(0, 5)
        copy(
            strikesPerOut = newValue,
            countFoulsAsStrikes = if (newValue == 0) false else countFoulsAsStrikes,
            foulsCanCauseOut = if (newValue == 1 && countFoulsAsStrikes) true else foulsCanCauseOut,
            maxFoulCount = maxFoulCount.coerceIn(1, (newValue - 1).coerceAtLeast(1)),
        )
    }

    fun updateBallsPerWalk(value: Int) = updateConfig { copy(ballsPerWalk = value.coerceIn(0, 5)) }

    fun updateOutsPerInning(value: Int) = updateConfig { copy(outsPerInning = value.coerceIn(1, 5)) }

    fun updateCountFouls(value: Boolean) = updateConfig { copy(countFouls = value) }

    fun updateFoulsCanCauseOut(value: Boolean) = updateConfig {
        if (!value && strikesPerOut == 1 && countFoulsAsStrikes) return@updateConfig this
        copy(foulsCanCauseOut = value)
    }

    fun updateCountFoulsAsStrikes(value: Boolean) = updateConfig {
        copy(
            countFoulsAsStrikes = value,
            foulsCanCauseOut = if (value && strikesPerOut == 1) true else foulsCanCauseOut,
        )
    }

    fun updateMaxFoulCount(value: Int) = updateConfig {
        copy(maxFoulCount = value.coerceIn(1, (strikesPerOut - 1).coerceAtLeast(1)))
    }

    fun updateFoulsPerOut(value: Int) = updateConfig { copy(foulsPerOut = value.coerceIn(1, 5)) }

    fun resetGame() {
        _state.value = GameState()
    }
}
