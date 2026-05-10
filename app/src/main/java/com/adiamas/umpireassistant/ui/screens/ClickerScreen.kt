package com.adiamas.umpireassistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ScoreRow(state = state, config = config, onAddRun = { viewModel.addRun() })
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
        UndoRedoRow(
            canUndo = canUndo,
            canRedo = canRedo,
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
        )
    }
}

@Composable
private fun ScoreRow(state: GameState, config: GameConfig, onAddRun: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TeamScoreBox(
            name = config.awayTeamName,
            score = state.awayScore,
            isBatting = state.isTopHalf,
            onClick = onAddRun,
            modifier = Modifier.weight(1f),
        )
        TeamScoreBox(
            name = config.homeTeamName,
            score = state.homeScore,
            isBatting = !state.isTopHalf,
            onClick = onAddRun,
            modifier = Modifier.weight(1f),
        )
        InningBox(
            inning = state.inning,
            isTopHalf = state.isTopHalf,
            modifier = Modifier.weight(0.65f),
        )
    }
}

@Composable
private fun TeamScoreBox(
    name: String,
    score: Int,
    isBatting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isBatting) ScoreBlue else ScoreBlueInactive)
            .then(if (isBatting) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "▼",
                color = if (isBatting) Color.White else Color.Transparent,
                fontSize = 20.sp,
            )
            Text(text = name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "$score",
                color = Color.White,
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
            .height(110.dp),
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
                Text("Out", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
        Text(label, color = contentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(
            text = if (!enabled) "Off" else "$value",
            color = contentColor,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 42.sp,
        )
    }
}

@Composable
private fun UndoRedoRow(canUndo: Boolean, canRedo: Boolean, onUndo: () -> Unit, onRedo: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.5f)
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
                Text("Undo", color = undoColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.Undo, contentDescription = null, tint = undoColor, modifier = Modifier.size(36.dp).graphicsLayer { scaleX = 1.4f; scaleY = 1.4f })
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .then(if (canRedo) Modifier.clickable { onRedo() } else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Redo", color = redoColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.Redo, contentDescription = null, tint = redoColor, modifier = Modifier.size(36.dp).graphicsLayer { scaleX = 1.4f; scaleY = 1.4f })
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
