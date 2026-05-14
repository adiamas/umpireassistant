package com.adiamas.umpireassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adiamas.umpireassistant.data.AppDatabase
import com.adiamas.umpireassistant.data.AppRepository
import com.adiamas.umpireassistant.data.SessionDatabase
import com.adiamas.umpireassistant.data.AppSessionEntity
import com.adiamas.umpireassistant.data.StoredConfigEntity
import com.adiamas.umpireassistant.data.TeamEntity
import com.adiamas.umpireassistant.model.FoulMode
import com.adiamas.umpireassistant.model.GameConfig
import com.adiamas.umpireassistant.model.GameState
import com.adiamas.umpireassistant.model.Sport
import com.adiamas.umpireassistant.model.VolumeAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AppRepository(AppDatabase.getInstance(application), SessionDatabase.getInstance(application))

    private val _config = MutableStateFlow(GameConfig())
    val config: StateFlow<GameConfig> = _config.asStateFlow()

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private val _timerSeconds = MutableStateFlow(_config.value.gameLengthMinutes * 60)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()
    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()
    private val _timerExpired = MutableStateFlow(false)
    val timerExpired: StateFlow<Boolean> = _timerExpired.asStateFlow()

    private val _undoStack = mutableListOf<GameState>()
    private val _redoStack = mutableListOf<GameState>()
    private var _actionDepth = 0
    private var _actionSnapshot: GameState? = null

    private val _canUndo = MutableStateFlow(false)
    private val _canRedo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _activeConfigId = MutableStateFlow(0)
    val activeConfigId: StateFlow<Int> = _activeConfigId.asStateFlow()

    private val _isDirty = MutableStateFlow(false)
    val isDirty: StateFlow<Boolean> = _isDirty.asStateFlow()

    private val _storedConfigs = MutableStateFlow<List<StoredConfigEntity>>(emptyList())
    val storedConfigs: StateFlow<List<StoredConfigEntity>> = _storedConfigs.asStateFlow()

    private val _teams = MutableStateFlow<List<TeamEntity>>(emptyList())
    val teams: StateFlow<List<TeamEntity>> = _teams.asStateFlow()

    private var homeTeamId: Int? = null
    private var awayTeamId: Int? = null

    private var sessionSaveJob: Job? = null

    init {
        viewModelScope.launch {
            val defaultId = repo.ensureDefaultConfig()
            val session = repo.session.first()
            val configId = session?.activeConfigId?.takeIf { it > 0 } ?: defaultId
            val storedConfig = repo.getConfigById(configId) ?: repo.getDefaultConfig() ?: return@launch
            applyStoredConfig(storedConfig, session)
        }
        viewModelScope.launch {
            repo.configs.collect { _storedConfigs.value = it }
        }
        viewModelScope.launch {
            _activeConfigId.flatMapLatest { id ->
                if (id > 0) repo.getTeamsForConfig(id) else flowOf(emptyList())
            }.collect { teams ->
                _teams.value = teams.sortedBy { it.name.lowercase() }
                val home = homeTeamId?.let { id -> teams.find { it.id == id } }
                val away = awayTeamId?.let { id -> teams.find { it.id == id } }
                var updated = _config.value
                var changed = false
                if (home != null) {
                    updated = updated.copy(homeTeamName = home.name, homeTeamColor = home.color); changed = true
                } else if (homeTeamId != null) {
                    homeTeamId = null; updated = updated.copy(homeTeamName = "Home", homeTeamColor = null); changed = true
                }
                if (away != null) {
                    updated = updated.copy(awayTeamName = away.name, awayTeamColor = away.color); changed = true
                } else if (awayTeamId != null) {
                    awayTeamId = null; updated = updated.copy(awayTeamName = "Away", awayTeamColor = null); changed = true
                }
                if (changed) _config.value = updated
            }
        }
    }

    private fun applyStoredConfig(storedConfig: StoredConfigEntity, session: AppSessionEntity?) {
        _activeConfigId.value = storedConfig.id
        homeTeamId = session?.homeTeamId
        awayTeamId = session?.awayTeamId
        _config.value = storedConfig.toGameConfig(
            homeTeamName = session?.homeTeamName ?: "Home",
            homeTeamColor = session?.homeTeamColor,
            awayTeamName = session?.awayTeamName ?: "Away",
            awayTeamColor = session?.awayTeamColor,
        )
        if (session != null) {
            _state.value = GameState(
                homeScore = session.homeScore,
                awayScore = session.awayScore,
                inning = session.inning,
                isTopHalf = session.isTopHalf,
                balls = session.balls,
                strikes = session.strikes,
                fouls = session.fouls,
                outs = session.outs,
            )
            _timerSeconds.value = if (session.timerSeconds >= 0) session.timerSeconds
                                   else storedConfig.gameLengthMinutes * 60
        } else {
            _timerSeconds.value = storedConfig.gameLengthMinutes * 60
        }
    }

    // ── state helpers ─────────────────────────────────────────────────────────

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
        scheduleSessionSave()
    }

    private fun updateConfig(block: GameConfig.() -> GameConfig) {
        _config.value = _config.value.block()
        _isDirty.value = true
    }

    private fun scheduleSessionSave() {
        sessionSaveJob?.cancel()
        sessionSaveJob = viewModelScope.launch {
            delay(500)
            persistSession()
        }
    }

    private suspend fun persistSession() {
        if (_activeConfigId.value == 0) return
        val s = _state.value
        val c = _config.value
        repo.saveSession(AppSessionEntity(
            activeConfigId = _activeConfigId.value,
            homeTeamId = homeTeamId,
            homeTeamName = c.homeTeamName,
            homeTeamColor = c.homeTeamColor,
            awayTeamId = awayTeamId,
            awayTeamName = c.awayTeamName,
            awayTeamColor = c.awayTeamColor,
            homeScore = s.homeScore,
            awayScore = s.awayScore,
            inning = s.inning,
            isTopHalf = s.isTopHalf,
            balls = s.balls,
            strikes = s.strikes,
            fouls = s.fouls,
            outs = s.outs,
            timerSeconds = _timerSeconds.value,
        ))
    }

    // ── game actions ──────────────────────────────────────────────────────────

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
            FoulMode.TRACK_ONLY -> {
                if (state.fouls < 10) update { copy(fouls = fouls + 1) }
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

    // ── team names (session state, not config dirty) ──────────────────────────

    fun selectHomeTeam(id: Int, name: String, color: Int?) {
        homeTeamId = id
        _config.value = _config.value.copy(homeTeamName = name, homeTeamColor = color)
        scheduleSessionSave()
    }

    fun selectAwayTeam(id: Int, name: String, color: Int?) {
        awayTeamId = id
        _config.value = _config.value.copy(awayTeamName = name, awayTeamColor = color)
        scheduleSessionSave()
    }

    // ── config settings ───────────────────────────────────────────────────────

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

    fun updateScrollTeamNames(value: Boolean) = updateConfig { copy(scrollTeamNames = value) }

    // ── stored config management ──────────────────────────────────────────────

    fun switchConfig(id: Int) {
        viewModelScope.launch {
            val storedConfig = repo.getConfigById(id) ?: return@launch
            val currentHome = _config.value.homeTeamName
            val currentAway = _config.value.awayTeamName
            _state.value = GameState()
            _undoStack.clear(); _redoStack.clear()
            _canUndo.value = false; _canRedo.value = false
            _activeConfigId.value = storedConfig.id
            _config.value = storedConfig.toGameConfig(homeTeamName = currentHome, awayTeamName = currentAway)
            _timerSeconds.value = storedConfig.gameLengthMinutes * 60
            _isDirty.value = false
            scheduleSessionSave()
        }
    }

    fun saveCurrentConfig(name: String) {
        viewModelScope.launch {
            val c = _config.value
            val existing = repo.getConfigByName(name)
            val entity = StoredConfigEntity(
                id = existing?.id ?: 0,
                name = name,
                isDefault = existing?.isDefault ?: false,
                sport = c.sport.name,
                strikesPerOut = c.strikesPerOut,
                ballsPerWalk = c.ballsPerWalk,
                outsPerInning = c.outsPerInning,
                inningsPerGame = c.inningsPerGame,
                foulMode = c.foulMode.name,
                maxFoulCount = c.maxFoulCount,
                foulsPerOut = c.foulsPerOut,
                volumeUp = c.volumeUp.name,
                volumeDown = c.volumeDown.name,
                gameLengthMinutes = c.gameLengthMinutes,
                scrollTeamNames = c.scrollTeamNames,
            )
            val savedId = if (existing != null) {
                repo.updateConfig(entity); existing.id
            } else {
                repo.insertConfig(entity).toInt()
            }
            _activeConfigId.value = savedId
            _isDirty.value = false
            scheduleSessionSave()
        }
    }

    fun deleteStoredConfig(id: Int) {
        viewModelScope.launch {
            val toDelete = repo.getConfigById(id) ?: return@launch
            if (toDelete.isDefault) return@launch
            repo.deleteConfig(toDelete)
            if (_activeConfigId.value == id) {
                val default = repo.getDefaultConfig() ?: return@launch
                switchConfig(default.id)
            }
        }
    }

    // ── team management ───────────────────────────────────────────────────────

    fun addTeam(name: String, color: Int? = null) {
        if (_activeConfigId.value == 0) return
        viewModelScope.launch { repo.addTeam(TeamEntity(configId = _activeConfigId.value, name = name, color = color)) }
    }

    fun updateTeam(team: TeamEntity) { viewModelScope.launch { repo.updateTeam(team) } }
    fun deleteTeam(team: TeamEntity) { viewModelScope.launch { repo.deleteTeam(team) } }

    // ── timer ─────────────────────────────────────────────────────────────────

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
                _timerExpired.value = true
            }
        }
    }

    fun clearTimerExpired() {
        _timerExpired.value = false
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerRunning.value = false
        _timerSeconds.value = _config.value.gameLengthMinutes * 60
    }

    // ── undo / redo ───────────────────────────────────────────────────────────

    fun undo() {
        if (_undoStack.isEmpty()) return
        _redoStack.add(_state.value)
        _state.value = _undoStack.removeLast()
        _canUndo.value = _undoStack.isNotEmpty()
        _canRedo.value = true
        scheduleSessionSave()
    }

    fun redo() {
        if (_redoStack.isEmpty()) return
        _undoStack.add(_state.value)
        _state.value = _redoStack.removeLast()
        _canUndo.value = true
        _canRedo.value = _redoStack.isNotEmpty()
        scheduleSessionSave()
    }

    // ── volume buttons ────────────────────────────────────────────────────────

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

    // ── team names (TeamsScreen compat) ───────────────────────────────────────

    fun updateTeamNames(homeName: String, awayName: String) {
        _config.value = _config.value.copy(
            homeTeamName = homeName.ifBlank { "Home" },
            awayTeamName = awayName.ifBlank { "Away" },
        )
        scheduleSessionSave()
    }

    // ── reset ─────────────────────────────────────────────────────────────────

    fun resetGame() {
        _state.value = GameState()
        _undoStack.clear(); _redoStack.clear()
        _canUndo.value = false; _canRedo.value = false
        homeTeamId = null
        awayTeamId = null
        _config.value = _config.value.copy(
            homeTeamName = "Home", homeTeamColor = null,
            awayTeamName = "Away", awayTeamColor = null,
        )
        resetTimer()
        scheduleSessionSave()
    }
}

// ── mapping helpers ───────────────────────────────────────────────────────────

private fun StoredConfigEntity.toGameConfig(homeTeamName: String, homeTeamColor: Int? = null, awayTeamName: String, awayTeamColor: Int? = null) = GameConfig(
    homeTeamName = homeTeamName,
    homeTeamColor = homeTeamColor,
    awayTeamName = awayTeamName,
    awayTeamColor = awayTeamColor,
    sport = Sport.valueOf(sport),
    strikesPerOut = strikesPerOut,
    ballsPerWalk = ballsPerWalk,
    outsPerInning = outsPerInning,
    inningsPerGame = inningsPerGame,
    foulMode = FoulMode.valueOf(foulMode),
    maxFoulCount = maxFoulCount,
    foulsPerOut = foulsPerOut,
    volumeUp = VolumeAction.valueOf(volumeUp),
    volumeDown = VolumeAction.valueOf(volumeDown),
    gameLengthMinutes = gameLengthMinutes,
    scrollTeamNames = scrollTeamNames,
)
