package com.adiamas.umpireassistant.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adiamas.umpireassistant.data.TeamEntity
import com.adiamas.umpireassistant.ui.theme.ActionGreen
import com.adiamas.umpireassistant.ui.theme.AppBackground
import com.adiamas.umpireassistant.viewmodel.GameViewModel

private val teamColors = listOf(
    Color(0xFFE53935), Color(0xFFE67E22), Color(0xFFF1C40F), Color(0xFF43A047),
    Color(0xFF00897B), Color(0xFF1E88E5), Color(0xFF3949AB), Color(0xFF8E24AA),
    Color(0xFFD81B60), Color(0xFF6D4C41), Color(0xFF757575), Color(0xFF212121),
)

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
            .background(AppBackground)
            .padding(16.dp),
    ) {
        Text("Teams", style = MaterialTheme.typography.headlineMedium)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)

        LazyColumn(
            modifier = Modifier.weight(1f).padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            itemsIndexed(teams) { index, team ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "${index + 1}.",
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val swatchColor = team.color?.let { Color(it) } ?: AppBackground
                    val textColor = if (swatchColor.luminance() > 0.5f) Color.Black else Color.White
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(50))
                            .background(swatchColor),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = team.name,
                            fontSize = 18.sp,
                            color = textColor,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 16.dp).basicMarquee(animationMode = MarqueeAnimationMode.Immediately, initialDelayMillis = 2400, repeatDelayMillis = 2400),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                        IconButton(onClick = { editTarget = team }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit team")
                        }
                        IconButton(onClick = { deleteTarget = team }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete team")
                        }
                    }
                }
                if (index < teams.lastIndex) HorizontalDivider(color = Color.DarkGray)
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
            onAdd = { name, color -> viewModel.addTeam(name, color); showAddDialog = false },
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
private fun AddTeamDialog(onDismiss: () -> Unit, onAdd: (String, Int?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<Color?>(null) }
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
                Text("Team color", style = MaterialTheme.typography.bodyMedium)
                TeamColorPicker(
                    selected = selectedColor,
                    onSelect = { selectedColor = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onAdd(name.trim(), selectedColor?.toArgb()) },
                enabled = name.isNotBlank(),
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EditTeamDialog(team: TeamEntity, onDismiss: () -> Unit, onSave: (TeamEntity) -> Unit) {
    var name by remember { mutableStateOf(team.name) }
    var selectedColor by remember { mutableStateOf(team.color?.let { Color(it) }) }
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
                Text("Team color", style = MaterialTheme.typography.bodyMedium)
                TeamColorPicker(
                    selected = selectedColor,
                    onSelect = { selectedColor = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(team.copy(name = name.trim(), color = selectedColor?.toArgb())) },
                enabled = name.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TeamColorPicker(selected: Color?, onSelect: (Color) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        teamColors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onSelect(color) },
                contentAlignment = Alignment.Center,
            ) {
                if (color == selected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
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
