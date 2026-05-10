package com.adiamas.umpireassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adiamas.umpireassistant.model.FoulMode
import com.adiamas.umpireassistant.ui.theme.ActionGreen
import com.adiamas.umpireassistant.model.VolumeAction
import com.adiamas.umpireassistant.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val config by viewModel.config.collectAsState()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showResetConfirm by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Button(
            onClick = {
                val date = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
                val text = "$date\n${config.awayTeamName} - ${state.awayScore}\n${config.homeTeamName} - ${state.homeScore}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(intent, null))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
        ) {
            Text("Share Game Score")
        }

        Button(
            onClick = { showResetConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
            Text("Reset Clicker")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Game Settings", style = MaterialTheme.typography.titleMedium)

        StepperRow(
            label = "Innings per game",
            value = config.inningsPerGame,
            onDecrement = { viewModel.updateInningsPerGame(config.inningsPerGame - 1) },
            onIncrement = { viewModel.updateInningsPerGame(config.inningsPerGame + 1) },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Pitch Count Settings", style = MaterialTheme.typography.titleMedium)
        Text(
            "Set any option to Off to disable it in game view.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        StepperRow(
            label = "Outs per inning",
            value = config.outsPerInning,
            onDecrement = { viewModel.updateOutsPerInning(config.outsPerInning - 1) },
            onIncrement = { viewModel.updateOutsPerInning(config.outsPerInning + 1) },
        )

        StepperRow(
            label = "Strikes per out",
            value = config.strikesPerOut,
            onDecrement = { viewModel.updateStrikesPerOut(config.strikesPerOut - 1) },
            onIncrement = { viewModel.updateStrikesPerOut(config.strikesPerOut + 1) },
            showOffAtZero = true,
        )

        StepperRow(
            label = "Balls per walk",
            value = config.ballsPerWalk,
            onDecrement = { viewModel.updateBallsPerWalk(config.ballsPerWalk - 1) },
            onIncrement = { viewModel.updateBallsPerWalk(config.ballsPerWalk + 1) },
            showOffAtZero = true,
        )

        Text("Fouls are:")
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it },
        ) {
            OutlinedTextField(
                value = config.foulMode.label(),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
            ) {
                FoulMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode.label()) },
                        onClick = { viewModel.updateFoulMode(mode); dropdownExpanded = false },
                        enabled = when (mode) {
                            FoulMode.ALWAYS_STRIKES -> config.strikesPerOut > 0
                            FoulMode.STRIKE_CAP -> config.strikesPerOut > 1
                            else -> true
                        },
                    )
                }
            }
        }
        if (config.foulMode == FoulMode.STRIKE_CAP) {
            StepperRow(
                label = "Max foul strikes",
                value = config.maxFoulCount,
                onDecrement = { viewModel.updateMaxFoulCount(config.maxFoulCount - 1) },
                onIncrement = { viewModel.updateMaxFoulCount(config.maxFoulCount + 1) },
            )
        }
        if (config.foulMode == FoulMode.INDEPENDENT) {
            StepperRow(
                label = "Fouls per out",
                value = config.foulsPerOut,
                onDecrement = { viewModel.updateFoulsPerOut(config.foulsPerOut - 1) },
                onIncrement = { viewModel.updateFoulsPerOut(config.foulsPerOut + 1) },
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Volume Button Assignments", style = MaterialTheme.typography.titleMedium)

        Text("Volume up")
        VolumeActionDropdown(
            selected = config.volumeUp,
            excluded = config.volumeDown.takeIf { it != VolumeAction.OFF },
            onSelect = { viewModel.updateVolumeUp(it) },
        )
        Text("Volume down")
        VolumeActionDropdown(
            selected = config.volumeDown,
            excluded = config.volumeUp.takeIf { it != VolumeAction.OFF },
            onSelect = { viewModel.updateVolumeDown(it) },
        )

    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Clicker") },
            text = { Text("Would you like to start a new game?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetGame()
                    showResetConfirm = false
                }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolumeActionDropdown(
    selected: VolumeAction,
    excluded: VolumeAction?,
    onSelect: (VolumeAction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            VolumeAction.entries.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.label()) },
                    onClick = { onSelect(action); expanded = false },
                    enabled = action != excluded,
                )
            }
        }
    }
}

private fun VolumeAction.label() = when (this) {
    VolumeAction.OFF -> "Off"
    VolumeAction.BALL -> "Ball"
    VolumeAction.STRIKE -> "Strike"
    VolumeAction.FOUL -> "Foul"
    VolumeAction.OUT -> "Out"
    VolumeAction.RUN_SCORED -> "Run scored"
    VolumeAction.NEW_AT_BAT -> "New at-bat"
}

private fun FoulMode.label() = when (this) {
    FoulMode.NOT_COUNTED -> "Off"
    FoulMode.ALWAYS_STRIKES -> "Strikes"
    FoulMode.STRIKE_CAP -> "Strikes, no foul out"
    FoulMode.INDEPENDENT -> "Fouls"
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    startPadding: Dp = 0.dp,
    showOffAtZero: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement) {
                Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = if (showOffAtZero && value == 0) "Off" else "$value",
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onIncrement) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
