package com.adiamas.umpireassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adiamas.umpireassistant.model.FoulMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.adiamas.umpireassistant.model.GameConfig
import com.adiamas.umpireassistant.model.GameState
import com.adiamas.umpireassistant.model.Sport
import com.adiamas.umpireassistant.model.VolumeAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {
    private val _config = MutableStateFlow(GameConfig())
    val config: StateFlow<GameConfig> = _config.asStateFlow()

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private val _timerSeconds = MutableStateFlow(_config.value.gameLengthMinutes * 60)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()
    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _undoStack = mutableListOf<GameState>()
    private val _redoStack = mutableListOf<GameState>()
    private var _actionDepth = 0
    private var _actionSnapshot: GameState? = null

    private val _canUndo = MutableStateFlow(false)
    private val _canRedo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private inline fun action(block: () -> Unit) {
        if (_actionDepth == 0) _actionSnapshot = _state.value
        _actionDepth++
        try {
            block()
        } finally {
            _actionDepth--
            if (_actionDepth == 0) {
                _actionSnapshot?.let { snapshot ->
                    if (snapshot != _state.value) {
                        _undoStack.add(snapshot)
                        _redoStack.clear()
                        _canUndo.value = true
                        _canRedo.value = false
                    }
                    _actionSnapshot = null
                }
            }
        }
    }

    private fun update(block: GameState.() -> GameState) {
        _state.value = _state.value.block()
    }

    private fun updateConfig(block: GameConfig.() -> GameConfig) {
        _config.value = _config.value.block()
    }

    fun incrementBalls() = action {
        val config = _config.value
        if (config.ballsPerWalk == 0) return@action
        val newBalls = _state.value.balls + 1
        if (newBalls >= config.ballsPerWalk) resetPitchCount()
        else update { copy(balls = newBalls) }
    }

    fun incrementStrikes() = action {
        val config = _config.value
        if (config.strikesPerOut == 0) return@action
        val newStrikes = _state.value.strikes + 1
        if (newStrikes >= config.strikesPerOut) incrementOuts()
        else update { copy(strikes = newStrikes) }
    }

    fun incrementFouls() = action {
        val config = _config.value
        val state = _state.value
        when (config.foulMode) {
            FoulMode.NOT_COUNTED -> Unit
            FoulMode.ALWAYS_STRIKES -> {
                update { copy(fouls = fouls + 1) }
                incrementStrikes()
            }
            FoulMode.STRIKE_CAP -> {
                if (state.fouls < config.maxFoulCount) {
                    update { copy(fouls = fouls + 1) }
                    incrementStrikes()
                }
            }
            FoulMode.INDEPENDENT -> {
                val newFouls = state.fouls + 1
                if (newFouls >= config.foulsPerOut) incrementOuts()
                else update { copy(fouls = newFouls) }
            }
        }
    }

    fun incrementOuts() = action {
        val newOuts = _state.value.outs + 1
        if (newOuts >= _config.value.outsPerInning) advanceHalf()
        else update { copy(outs = newOuts, balls = 0, strikes = 0, fouls = 0) }
    }

    fun addRun() = action {
        if (_state.value.isTopHalf) update { copy(awayScore = awayScore + 1) }
        else update { copy(homeScore = homeScore + 1) }
        resetPitchCount()
    }

    fun resetPitchCount() = action { update { copy(balls = 0, strikes = 0, fouls = 0) } }

    fun advanceHalf() = action {
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
        val newFoulMode = if (newValue == 0 && (foulMode == FoulMode.ALWAYS_STRIKES || foulMode == FoulMode.STRIKE_CAP))
            FoulMode.NOT_COUNTED else foulMode
        copy(
            strikesPerOut = newValue,
            foulMode = newFoulMode,
            maxFoulCount = maxFoulCount.coerceIn(1, (newValue - 1).coerceAtLeast(1)),
        )
    }

    fun updateBallsPerWalk(value: Int) = updateConfig { copy(ballsPerWalk = value.coerceIn(0, 5)) }

    fun updateOutsPerInning(value: Int) = updateConfig { copy(outsPerInning = value.coerceIn(1, 5)) }

    fun updateInningsPerGame(value: Int) = updateConfig { copy(inningsPerGame = value.coerceIn(1, 20)) }

    fun updateFoulMode(mode: FoulMode) = updateConfig { copy(foulMode = mode) }

    fun updateMaxFoulCount(value: Int) = updateConfig {
        copy(maxFoulCount = value.coerceIn(1, (strikesPerOut - 1).coerceAtLeast(1)))
    }

    fun updateFoulsPerOut(value: Int) = updateConfig { copy(foulsPerOut = value.coerceIn(1, 5)) }

    fun updateVolumeUp(action: VolumeAction) = updateConfig { copy(volumeUp = action) }
    fun updateVolumeDown(action: VolumeAction) = updateConfig { copy(volumeDown = action) }

    fun updateGameLengthMinutes(value: Int) {
        updateConfig { copy(gameLengthMinutes = value.coerceIn(0, 120)) }
        if (!_timerRunning.value) _timerSeconds.value = _config.value.gameLengthMinutes * 60
    }

    fun toggleTimer() {
        if (_timerRunning.value) {
            timerJob?.cancel()
            _timerRunning.value = false
        } else {
            if (_timerSeconds.value == 0) _timerSeconds.value = _config.value.gameLengthMinutes * 60
            _timerRunning.value = true
            timerJob = viewModelScope.launch {
                while (_timerSeconds.value > 0) {
                    delay(1000)
                    _timerSeconds.value--
                }
                _timerRunning.value = false
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerRunning.value = false
        _timerSeconds.value = _config.value.gameLengthMinutes * 60
    }

    fun dispatchVolumeAction(action: VolumeAction): Boolean {
        when (action) {
            VolumeAction.OFF -> return false
            VolumeAction.BALL -> incrementBalls()
            VolumeAction.STRIKE -> incrementStrikes()
            VolumeAction.FOUL -> incrementFouls()
            VolumeAction.OUT -> incrementOuts()
            VolumeAction.RUN_SCORED -> addRun()
            VolumeAction.NEW_AT_BAT -> resetPitchCount()
        }
        return true
    }

    fun undo() {
        if (_undoStack.isEmpty()) return
        _redoStack.add(_state.value)
        _state.value = _undoStack.removeLast()
        _canUndo.value = _undoStack.isNotEmpty()
        _canRedo.value = true
    }

    fun redo() {
        if (_redoStack.isEmpty()) return
        _undoStack.add(_state.value)
        _state.value = _redoStack.removeLast()
        _canUndo.value = true
        _canRedo.value = _redoStack.isNotEmpty()
    }

    fun resetGame() = action {
        _state.value = GameState()
        resetTimer()
    }
}
