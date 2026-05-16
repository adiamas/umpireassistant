package com.adiamas.umpireassistant.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adiamas.umpireassistant.data.StoredConfigEntity
import com.adiamas.umpireassistant.model.FoulMode
import com.adiamas.umpireassistant.ui.theme.ActionGreen
import com.adiamas.umpireassistant.ui.theme.AppBackground
import com.adiamas.umpireassistant.model.VolumeAction
import com.adiamas.umpireassistant.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val config by viewModel.config.collectAsState()
    val state by viewModel.state.collectAsState()
    val storedConfigs by viewModel.storedConfigs.collectAsState()
    val activeConfigId by viewModel.activeConfigId.collectAsState()
    val isDirty by viewModel.isDirty.collectAsState()
    val context = LocalContext.current
    var showResetConfirm by remember { mutableStateOf(false) }
    var showSaveConfigDialog by remember { mutableStateOf(false) }
    var showSaveMessage by remember { mutableStateOf(false) }
    LaunchedEffect(showSaveMessage) {
        if (showSaveMessage) { delay(3000); showSaveMessage = false }
    }
    var configDropdownExpanded by remember { mutableStateOf(false) }
    var pendingConfigId by remember { mutableStateOf<Int?>(null) }
    var showDeleteConfigConfirm by remember { mutableStateOf(false) }
    val hasChanges = isDirty || state.homeScore > 0 || state.awayScore > 0 || state.inning > 1
    var dropdownExpanded by remember { mutableStateOf(false) }
    val activeConfig = storedConfigs.find { it.id == activeConfigId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
        Text("Game Status", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
            ) {
                Text("Share Game Score")
            }
            Button(
                onClick = { showResetConfirm = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Text("Reset Clicker")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
        Text("Stored Settings", style = MaterialTheme.typography.titleMedium)
        AnimatedContent(
            targetState = showSaveMessage,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "StoredSettingsArea",
        ) { isSaved ->
            if (isSaved) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = ActionGreen)
                        Text(
                            "Settings have been saved.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ActionGreen,
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExposedDropdownMenuBox(
                            expanded = configDropdownExpanded,
                            onExpandedChange = { configDropdownExpanded = it },
                            modifier = Modifier.weight(1f),
                        ) {
                            OutlinedTextField(
                                value = activeConfig?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = configDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            )
                            ExposedDropdownMenu(
                                expanded = configDropdownExpanded,
                                onDismissRequest = { configDropdownExpanded = false },
                            ) {
                                storedConfigs.forEach { sc ->
                                    DropdownMenuItem(
                                        text = { Text(sc.name) },
                                        onClick = {
                                            configDropdownExpanded = false
                                            if (hasChanges) pendingConfigId = sc.id
                                            else viewModel.switchConfig(sc.id)
                                        },
                                    )
                                }
                            }
                        }
                        val canDelete = activeConfig != null && !activeConfig.name.equals("Default", ignoreCase = true)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "+",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { showSaveConfigDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                            Text(
                                text = "−",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canDelete) Color.Unspecified else Color.Gray,
                                modifier = Modifier
                                    .then(if (canDelete) Modifier.clickable { showDeleteConfigConfirm = true } else Modifier)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.saveCurrentConfig(activeConfig!!.name)
                            showSaveMessage = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isDirty && activeConfig != null && !activeConfig.name.equals("Default", ignoreCase = true),
                        colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
                    ) { Text("Save Settings") }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
        Text("Game Settings", style = MaterialTheme.typography.titleMedium)

        StepperRow(
            label = "Innings per game",
            value = config.inningsPerGame,
            onDecrement = { viewModel.updateInningsPerGame(config.inningsPerGame - 1) },
            onIncrement = { viewModel.updateInningsPerGame(config.inningsPerGame + 1) },
        )

        StepperRow(
            label = "Game length (min)",
            value = config.gameLengthMinutes,
            onDecrement = {
                val next = when {
                    config.gameLengthMinutes <= 1 -> 0
                    config.gameLengthMinutes <= 5 -> 1
                    else -> config.gameLengthMinutes - 5
                }
                viewModel.updateGameLengthMinutes(next)
            },
            onIncrement = {
                val next = when {
                    config.gameLengthMinutes == 0 -> 1
                    config.gameLengthMinutes < 5 -> 5
                    else -> config.gameLengthMinutes + 5
                }
                viewModel.updateGameLengthMinutes(next)
            },
            showOffAtZero = true,
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
        Text("Pitch Count Settings", style = MaterialTheme.typography.titleMedium)

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

        Text("Foul Rules:")
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
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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
                label = "Foul strike limit",
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
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
        Text("Volume up - Long press")
        VolumeActionDropdown(
            selected = config.volumeUpLong,
            excluded = config.volumeDownLong.takeIf { it != VolumeAction.OFF },
            onSelect = { viewModel.updateVolumeUpLong(it) },
        )
        Text("Volume down - Long press")
        VolumeActionDropdown(
            selected = config.volumeDownLong,
            excluded = config.volumeUpLong.takeIf { it != VolumeAction.OFF },
            onSelect = { viewModel.updateVolumeDownLong(it) },
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)
        Text("UI Settings", style = MaterialTheme.typography.titleMedium)

        ToggleStepperRow(
            label = "Scroll team names",
            value = config.scrollTeamNames,
            onToggle = { viewModel.updateScrollTeamNames(it) },
        )

    }

    pendingConfigId?.let { configId ->
        AlertDialog(
            onDismissRequest = { pendingConfigId = null },
            title = { Text("Switch Settings") },
            text = { Text("Selecting a stored setting will reset any clicker, teams, or settings changes.") },
            confirmButton = {
                TextButton(onClick = { viewModel.switchConfig(configId); pendingConfigId = null }) {
                    Text("Switch")
                }
            },
            dismissButton = { TextButton(onClick = { pendingConfigId = null }) { Text("Cancel") } },
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

    if (showDeleteConfigConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfigConfirm = false },
            title = { Text("Delete Settings") },
            text = { Text("Delete \"${activeConfig?.name}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    activeConfig?.let { viewModel.deleteStoredConfig(it.id) }
                    showDeleteConfigConfirm = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfigConfirm = false }) { Text("Cancel") }
            },
        )
    }

    if (showSaveConfigDialog) {
        SaveConfigDialog(
            onDismiss = { showSaveConfigDialog = false },
            onSave = { name ->
                viewModel.saveCurrentConfig(name)
                showSaveConfigDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveConfigDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var showDefaultWarning by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showDefaultWarning = false },
                    label = { Text("Configuration name") },
                    singleLine = true,
                )
                if (showDefaultWarning) {
                    Text(
                        "Cannot overwrite Default settings.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.trim().equals("Default", ignoreCase = true)) {
                    showDefaultWarning = true
                } else {
                    onSave(name.trim())
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
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
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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
    FoulMode.ALWAYS_STRIKES -> "Fouls = Strikes"
    FoulMode.STRIKE_CAP -> "Fouls = Strikes (Limited)"
    FoulMode.INDEPENDENT -> "Allow Foul Outs"
    FoulMode.TRACK_ONLY -> "Track Only"
}

@Composable
private fun ToggleStepperRow(label: String, value: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onToggle(false) }, enabled = value) {
                Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = if (value) "On" else "Off",
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = { onToggle(true) }, enabled = !value) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    showOffAtZero: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
