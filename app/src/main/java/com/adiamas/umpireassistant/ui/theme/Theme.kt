package com.adiamas.umpireassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = ScoreBlue,
    secondary = ActionGreen,
    error = OutRed,
    background = AppBackgroundDark,
    surface = AppBackground,
)

@Composable
fun UmpireAssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content,
    )
}
