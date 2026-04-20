package com.adiamas.umpireassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.adiamas.umpireassistant.viewmodel.GameViewModel

@Composable
fun TeamsScreen(viewModel: GameViewModel) {
    val config by viewModel.config.collectAsState()
    var awayName by remember(config.awayTeamName) { mutableStateOf(config.awayTeamName) }
    var homeName by remember(config.homeTeamName) { mutableStateOf(config.homeTeamName) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Teams", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = awayName,
            onValueChange = { awayName = it },
            label = { Text("Away Team Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
        )

        OutlinedTextField(
            value = homeName,
            onValueChange = { homeName = it },
            label = { Text("Home Team Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    viewModel.updateTeamNames(homeName, awayName)
                },
            ),
        )

        Button(
            onClick = { viewModel.updateTeamNames(homeName, awayName) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save Teams")
        }
    }
}
