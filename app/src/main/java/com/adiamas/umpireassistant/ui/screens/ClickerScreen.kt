package com.adiamas.umpireassistant.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adiamas.umpireassistant.model.FoulMode
import com.adiamas.umpireassistant.model.GameConfig
import com.adiamas.umpireassistant.model.GameState
import com.adiamas.umpireassistant.model.Sport
import com.adiamas.umpireassistant.ui.theme.ActionGreen
import com.adiamas.umpireassistant.ui.theme.AppBackground
import com.adiamas.umpireassistant.ui.theme.CountDark
import com.adiamas.umpireassistant.ui.theme.OutRed
import com.adiamas.umpireassistant.ui.theme.ScoreBlue
import com.adiamas.umpireassistant.ui.theme.ScoreBlueInactive
import com.adiamas.umpireassistant.viewmodel.GameViewModel

@Composable
fun ClickerScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()
    val config by viewModel.config.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val timerRunning by viewModel.timerRunning.collectAsState()
    val teams by viewModel.teams.collectAsState()
    var showClockResetConfirm by remember { mutableStateOf(false) }
    var showHomeSelector by remember { mutableStateOf(false) }
    var showAwaySelector by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ScoreRow(
            state = state,
            config = config,
            scrollTeamNames = config.scrollTeamNames,
            onAddRun = { viewModel.addRun() },
            onSelectAwayTeam = { showAwaySelector = true },
            onSelectHomeTeam = { showHomeSelector = true },
        )
        CountRow(
            state = state,
            config = config,
            onBall = { viewModel.incrementBalls() },
            onStrike = { viewModel.incrementStrikes() },
            onFoul = { viewModel.incrementFouls() },
            onOut = { viewModel.incrementOuts() },
        )
        Spacer(modifier = Modifier.weight(1f))
        ActionButtons(
            sport = config.sport,
            onRunScored = { viewModel.addRun() },
            onNewAtBat = { viewModel.resetPitchCount() },
        )
        Spacer(modifier = Modifier.weight(1f))
        BottomRow(
            gameLengthMinutes = config.gameLengthMinutes,
            timerSeconds = timerSeconds,
            timerRunning = timerRunning,
            onTimerTap = { viewModel.toggleTimer() },
            onTimerLongPress = { showClockResetConfirm = true },
            canUndo = canUndo,
            canRedo = canRedo,
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
        )
    }

    Box(modifier = Modifier.align(Alignment.TopEnd)) {
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text("Share Game Score") }, onClick = { showMenu = false })
            DropdownMenuItem(text = { Text("Reset Clicker") }, onClick = { showMenu = false })
        }
    }
    } // end outer Box

    if (showClockResetConfirm) {
        AlertDialog(
            onDismissRequest = { showClockResetConfirm = false },
            title = { Text("Reset Game Clock") },
            text = { Text("Would you like to reset the game clock?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetTimer()
                    showClockResetConfirm = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showClockResetConfirm = false }) { Text("Cancel") }
            },
        )
    }

    if (showAwaySelector) {
        TeamSelectorDialog(
            label = "Away",
            teams = teams,
            onDismiss = { showAwaySelector = false },
            onSelect = { id, name, color -> viewModel.selectAwayTeam(id, name, color); showAwaySelector = false },
        )
    }

    if (showHomeSelector) {
        TeamSelectorDialog(
            label = "Home",
            teams = teams,
            onDismiss = { showHomeSelector = false },
            onSelect = { id, name, color -> viewModel.selectHomeTeam(id, name, color); showHomeSelector = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamSelectorDialog(
    label: String,
    teams: List<com.adiamas.umpireassistant.data.TeamEntity>,
    onDismiss: () -> Unit,
    onSelect: (Int, String, Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(teams.firstOrNull()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select $label team") },
        text = {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selected?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    teams.forEach { team ->
                        DropdownMenuItem(
                            text = { Text(team.name) },
                            onClick = { selected = team; expanded = false },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { selected?.let { onSelect(it.id, it.name, it.color) } }, enabled = selected != null) {
                Text("Select")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ScoreRow(
    state: GameState,
    config: GameConfig,
    scrollTeamNames: Boolean,
    onAddRun: () -> Unit,
    onSelectAwayTeam: () -> Unit,
    onSelectHomeTeam: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TeamScoreBox(
            name = config.awayTeamName,
            teamColor = config.awayTeamColor,
            score = state.awayScore,
            isBatting = state.isTopHalf,
            scrollName = scrollTeamNames,
            onClick = onAddRun,
            onLongClick = onSelectAwayTeam,
            modifier = Modifier.weight(1f),
        )
        TeamScoreBox(
            name = config.homeTeamName,
            teamColor = config.homeTeamColor,
            score = state.homeScore,
            isBatting = !state.isTopHalf,
            scrollName = scrollTeamNames,
            onClick = onAddRun,
            onLongClick = onSelectHomeTeam,
            modifier = Modifier.weight(1f),
        )
        InningBox(
            inning = state.inning,
            isTopHalf = state.isTopHalf,
            modifier = Modifier.weight(0.65f),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TeamScoreBox(
    name: String,
    teamColor: Int?,
    score: Int,
    isBatting: Boolean,
    scrollName: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer { alpha = if (isBatting) 1f else 0.5f }
            .then(if (isBatting) Modifier.border(3.dp, Color(0xFF0D47A1), RoundedCornerShape(8.dp)) else Modifier)
            .clip(RoundedCornerShape(8.dp))
            .background(ScoreBlue)
            .combinedClickable(
                onClick = { if (isBatting) onClick() },
                onLongClick = onLongClick,
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (teamColor != null) {
                val pillColor = Color(teamColor)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(pillColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = name,
                        color = if (pillColor.luminance() > 0.5f) Color.Black else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 12.dp)
                            .then(if (scrollName) Modifier.basicMarquee(animationMode = MarqueeAnimationMode.Immediately, initialDelayMillis = 2400, repeatDelayMillis = 2400) else Modifier),
                    )
                }
            } else {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 12.dp)
                        .then(if (scrollName) Modifier.basicMarquee(animationMode = MarqueeAnimationMode.Immediately, initialDelayMillis = 2400, repeatDelayMillis = 2400) else Modifier),
                )
            }
            Text(
                text = "$score",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 56.sp,
            )
        }
    }
}

@Composable
private fun InningBox(
    inning: Int,
    isTopHalf: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(ScoreBlue),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isTopHalf) "Top" else "Bottom",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = inning.toOrdinal(),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CountRow(
    state: GameState,
    config: GameConfig,
    onBall: () -> Unit,
    onStrike: () -> Unit,
    onFoul: () -> Unit,
    onOut: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(CountDark),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CountCell(label = "Ball", value = state.balls, enabled = config.ballsPerWalk > 0, onClick = onBall)
            CountCell(label = "Strike", value = state.strikes, enabled = config.strikesPerOut > 0, onClick = onStrike)
            CountCell(label = "Foul", value = state.fouls, enabled = config.foulMode != FoulMode.NOT_COUNTED, onClick = onFoul)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(OutRed)
                .clickable { onOut() },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Out", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${state.outs}", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CountCell(label: String, value: Int, enabled: Boolean = true, onClick: () -> Unit) {
    val contentColor = if (enabled) Color.White else Color.White.copy(alpha = 0.35f)
    Column(
        modifier = Modifier
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, color = contentColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            text = if (!enabled) "Off" else "$value",
            color = contentColor,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 42.sp,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomRow(
    gameLengthMinutes: Int,
    timerSeconds: Int,
    timerRunning: Boolean,
    onTimerTap: () -> Unit,
    onTimerLongPress: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val clockEnabled = gameLengthMinutes > 0
        val clockColor = if (clockEnabled) Color.White else Color.White.copy(alpha = 0.35f)
        Column(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CountDark)
                .then(if (clockEnabled) Modifier.combinedClickable(onClick = onTimerTap, onLongClick = onTimerLongPress) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Game Clock", color = clockColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                text = if (clockEnabled) "%d:%02d".format(timerSeconds / 60, timerSeconds % 60) else "Off",
                color = clockColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CountDark),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val undoColor = if (canUndo) Color.White else Color.White.copy(alpha = 0.35f)
            val redoColor = if (canRedo) Color.White else Color.White.copy(alpha = 0.35f)
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .then(if (canUndo) Modifier.clickable { onUndo() } else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Undo", color = undoColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, tint = undoColor, modifier = Modifier.size(36.dp).graphicsLayer { scaleX = 1.4f; scaleY = 1.4f })
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .then(if (canRedo) Modifier.clickable { onRedo() } else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Redo", color = redoColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = null, tint = redoColor, modifier = Modifier.size(36.dp).graphicsLayer { scaleX = 1.4f; scaleY = 1.4f })
            }
        }
    }
}

@Composable
private fun ActionButtons(sport: Sport, onRunScored: () -> Unit, onNewAtBat: () -> Unit) {
    val runLabel = if (sport == Sport.KICKBALL) "Runner scored!" else "Run scored!"

    Button(
        onClick = onRunScored,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
    ) {
        Text(runLabel, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = onNewAtBat,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
    ) {
        Text("New at-bat", fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

private fun Int.toOrdinal(): String = when {
    this % 100 in 11..13 -> "${this}th"
    this % 10 == 1 -> "${this}st"
    this % 10 == 2 -> "${this}nd"
    this % 10 == 3 -> "${this}rd"
    else -> "${this}th"
}
