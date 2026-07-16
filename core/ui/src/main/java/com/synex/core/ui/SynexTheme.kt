package com.synex.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SynexColors = lightColorScheme(
    primary = SynexInk,
    onPrimary = Color.White,
    primaryContainer = SynexGreenSoft,
    onPrimaryContainer = SynexInk,
    secondary = SynexGreen,
    onSecondary = Color.White,
    background = SynexPaper,
    onBackground = SynexInk,
    surface = SynexCanvas,
    onSurface = SynexInk,
    surfaceVariant = Color.White,
    onSurfaceVariant = SynexMuted,
    outline = SynexLine,
    error = SynexRed,
)

@Composable
fun SynexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SynexColors,
        typography = SynexTypography,
        content = content,
    )
}
