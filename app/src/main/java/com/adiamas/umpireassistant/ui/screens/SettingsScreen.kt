package com.adiamas.umpireassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adiamas.umpireassistant.viewmodel.GameViewModel

private val INDENT_1 = 32.dp
private val INDENT_2 = 64.dp

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val config by viewModel.config.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Pitch Count Rules", style = MaterialTheme.typography.titleMedium)

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
        )

        StepperRow(
            label = "Balls per walk",
            value = config.ballsPerWalk,
            onDecrement = { viewModel.updateBallsPerWalk(config.ballsPerWalk - 1) },
            onIncrement = { viewModel.updateBallsPerWalk(config.ballsPerWalk + 1) },
            enabled = config.ballsPerWalk > 0,
        )

        StepperRow(
            label = "Balls per walk",
            value = config.ballsPerWalk,
            onDecrement = { viewModel.updateBallsPerWalk(config.ballsPerWalk - 1) },
            onIncrement = { viewModel.updateBallsPerWalk(config.ballsPerWalk + 1) },
            suffixLabel = if (config.ballsPerWalk == 0) "(Inactive)" else null,
        )

        CheckboxRow(
            label = "Count fouls?",
            checked = config.countFouls,
            onCheckedChange = { viewModel.updateCountFouls(it) },
        )

        if (config.countFouls) {
            if (config.strikesPerOut > 0) {
                CheckboxRow(
                    label = "Count fouls as strikes?",
                    checked = config.countFoulsAsStrikes,
                    onCheckedChange = { viewModel.updateCountFoulsAsStrikes(it) },
                    startPadding = INDENT_1,
                )
            }
            if (!config.countFoulsAsStrikes) {
                CheckboxRow(
                    label = "Allow foul outs?",
                    checked = config.foulsCanCauseOut,
                    onCheckedChange = { viewModel.updateFoulsCanCauseOut(it) },
                    startPadding = INDENT_1,
                )
                if (config.foulsCanCauseOut) {
                    StepperRow(
                        label = "Fouls per out",
                        value = config.foulsPerOut,
                        onDecrement = { viewModel.updateFoulsPerOut(config.foulsPerOut - 1) },
                        onIncrement = { viewModel.updateFoulsPerOut(config.foulsPerOut + 1) },
                        startPadding = INDENT_2,
                    )
                }
            } else {
                CheckboxRow(
                    label = "Allow foul outs?",
                    checked = config.foulsCanCauseOut,
                    onCheckedChange = { viewModel.updateFoulsCanCauseOut(it) },
                    startPadding = INDENT_2,
                    enabled = !(config.strikesPerOut == 1 && config.countFoulsAsStrikes),
                )
                if (!config.foulsCanCauseOut) {
                    StepperRow(
                        label = "Max foul count",
                        value = config.maxFoulCount,
                        onDecrement = { viewModel.updateMaxFoulCount(config.maxFoulCount - 1) },
                        onIncrement = { viewModel.updateMaxFoulCount(config.maxFoulCount + 1) },
                        startPadding = INDENT_2,
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Button(
            onClick = { showResetConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
            Text("Reset Game")
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Game") },
            text = { Text("Reset all scores and counts? Team names and sport will be kept.") },
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

@Composable
private fun CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    startPadding: Dp = 0.dp,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    startPadding: Dp = 0.dp,
    enabled: Boolean = true,
    suffixLabel: String? = null,
) {
    val labelColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = labelColor)
            if (suffixLabel != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(suffixLabel, color = Color(0xFFE57373), fontSize = 14.sp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement) {
                Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "$value",
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
