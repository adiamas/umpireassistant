package com.adiamas.umpireassistant.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import com.adiamas.umpireassistant.data.TeamEntity
import com.adiamas.umpireassistant.ui.theme.ActionGreen
import com.adiamas.umpireassistant.viewmodel.GameViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeamsScreen(viewModel: GameViewModel) {
    val teams by viewModel.teams.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<TeamEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<TeamEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("Teams", style = MaterialTheme.typography.headlineMedium)

        LazyColumn(
            modifier = Modifier.weight(1f).padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            itemsIndexed(teams) { index, team ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { editTarget = team },
                            onLongClick = { deleteTarget = team },
                        )
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(team.name, style = MaterialTheme.typography.bodyLarge)
                }
                if (index < teams.lastIndex) HorizontalDivider()
            }
        }

        Spacer(modifier = Modifier.padding(bottom = 8.dp))
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
        ) { Text("Add Team") }
    }

    if (showAddDialog) {
        AddTeamDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name -> viewModel.addTeam(name); showAddDialog = false },
        )
    }

    editTarget?.let { team ->
        EditTeamDialog(
            team = team,
            onDismiss = { editTarget = null },
            onSave = { updated -> viewModel.updateTeam(updated); editTarget = null },
        )
    }

    deleteTarget?.let { team ->
        DeleteTeamDialog(
            team = team,
            onDismiss = { deleteTarget = null },
            onDelete = { viewModel.deleteTeam(team); deleteTarget = null },
        )
    }
}

@Composable
private fun AddTeamDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Team") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Add new team")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Team name") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onAdd(name.trim()) },
                enabled = name.isNotBlank(),
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EditTeamDialog(team: TeamEntity, onDismiss: () -> Unit, onSave: (TeamEntity) -> Unit) {
    var name by remember { mutableStateOf(team.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Team") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Edit team info")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Team name") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(team.copy(name = name.trim())) },
                enabled = name.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun DeleteTeamDialog(team: TeamEntity, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Team") },
        text = { Text("Delete team \"${team.name}\"?") },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
