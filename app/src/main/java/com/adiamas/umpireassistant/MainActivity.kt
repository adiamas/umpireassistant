package com.adiamas.umpireassistant

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adiamas.umpireassistant.R
import com.adiamas.umpireassistant.ui.screens.ClickerScreen
import com.adiamas.umpireassistant.ui.screens.SettingsScreen
import com.adiamas.umpireassistant.ui.screens.TeamsScreen
import androidx.compose.ui.unit.dp
import com.adiamas.umpireassistant.ui.theme.ActionGreen
import com.adiamas.umpireassistant.ui.theme.UmpireAssistantTheme
import com.adiamas.umpireassistant.ui.shareText
import com.adiamas.umpireassistant.model.VolumeAction
import com.adiamas.umpireassistant.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            WindowInsetsControllerCompat(window, window.decorView)
                .show(WindowInsetsCompat.Type.statusBars())
        }
    }

    private var volumeUpLongPressed = false
    private var volumeDownLongPressed = false

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val (short, long) = when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> Pair(viewModel.config.value.volumeUp, viewModel.config.value.volumeUpLong)
            KeyEvent.KEYCODE_VOLUME_DOWN -> Pair(viewModel.config.value.volumeDown, viewModel.config.value.volumeDownLong)
            else -> return super.onKeyDown(keyCode, event)
        }
        if (short == VolumeAction.OFF && long == VolumeAction.OFF)
            return super.onKeyDown(keyCode, event)
        event?.startTracking()
        return true
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        val action = when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> { volumeUpLongPressed = true; viewModel.config.value.volumeUpLong }
            KeyEvent.KEYCODE_VOLUME_DOWN -> { volumeDownLongPressed = true; viewModel.config.value.volumeDownLong }
            else -> return super.onKeyLongPress(keyCode, event)
        }
        viewModel.dispatchVolumeAction(action)
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (!volumeUpLongPressed) viewModel.dispatchVolumeAction(viewModel.config.value.volumeUp)
                volumeUpLongPressed = false
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (!volumeDownLongPressed) viewModel.dispatchVolumeAction(viewModel.config.value.volumeDown)
                volumeDownLongPressed = false
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            UmpireAssistantTheme {
                MainNavigation(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainNavigation(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val config by viewModel.config.collectAsState()
    var showFinalizeGameDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val shareGameScore = {
        context.shareText("${config.awayTeamName} ${state.awayScore}, ${config.homeTeamName} ${state.homeScore}")
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val rowScope = this
                val clickerInteractionSource = remember { MutableInteractionSource() }
                Box(modifier = Modifier.weight(1f)) {
                    rowScope.NavigationBarItem(
                        icon = { Icon(painterResource(R.drawable.ic_clicker), contentDescription = "Clicker") },
                        label = { Text("Clicker") },
                        selected = currentRoute == "clicker",
                        onClick = {},
                        interactionSource = clickerInteractionSource,
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .combinedClickable(
                                interactionSource = clickerInteractionSource,
                                indication = null,
                                onClick = { if (currentRoute != "clicker") navController.navigate("clicker") { launchSingleTop = true } },
                                onLongClick = { showFinalizeGameDialog = true },
                            )
                    )
                }
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Teams") },
                    label = { Text("Teams") },
                    selected = currentRoute == "teams",
                    onClick = { if (currentRoute != "teams") navController.navigate("teams") { launchSingleTop = true } },
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = { if (currentRoute != "settings") navController.navigate("settings") { launchSingleTop = true } },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "clicker",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("clicker") { ClickerScreen(viewModel) }
            composable("teams") { TeamsScreen(viewModel) }
            composable("settings") { SettingsScreen(viewModel) }
        }
    }

    if (showFinalizeGameDialog) {
        AlertDialog(
            onDismissRequest = { showFinalizeGameDialog = false },
            title = { Text("Game Over?") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { showFinalizeGameDialog = false; shareGameScore() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ActionGreen),
                    ) { Text("Share Game Scores") }
                    Button(
                        onClick = { showFinalizeGameDialog = false; showResetConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) { Text("Reset Clicker") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFinalizeGameDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Clicker") },
            text = { Text("This will reset the score, pitch count, game clock, and team assignments. Continue?") },
            confirmButton = {
                TextButton(onClick = { viewModel.resetGame(); showResetConfirm = false }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            },
        )
    }
}
