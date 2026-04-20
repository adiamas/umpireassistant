package com.adiamas.umpireassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adiamas.umpireassistant.ui.screens.ClickerScreen
import com.adiamas.umpireassistant.ui.screens.SettingsScreen
import com.adiamas.umpireassistant.ui.screens.TeamsScreen
import com.adiamas.umpireassistant.ui.theme.UmpireAssistantTheme
import com.adiamas.umpireassistant.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UmpireAssistantTheme {
                MainNavigation(viewModel)
            }
        }
    }
}

@Composable
fun MainNavigation(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = "Clicker") },
                    label = { Text("Clicker") },
                    selected = currentRoute == "clicker",
                    onClick = { navController.navigate("clicker") { launchSingleTop = true } },
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Teams") },
                    label = { Text("Teams") },
                    selected = currentRoute == "teams",
                    onClick = { navController.navigate("teams") { launchSingleTop = true } },
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") { launchSingleTop = true } },
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
}
