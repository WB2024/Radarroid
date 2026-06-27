package com.radarrtv.androidtv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RadarrBlue,
    onPrimary = RadarrBg,
    primaryContainer = RadarrBlueDark,
    onPrimaryContainer = RadarrWhite,
    secondary = RadarrOrange,
    onSecondary = RadarrBg,
    secondaryContainer = Color(0xFF5A3010),
    onSecondaryContainer = RadarrWhite,
    tertiary = RadarrGreen,
    onTertiary = RadarrBg,
    background = RadarrBg,
    onBackground = RadarrWhite,
    surface = RadarrSurface,
    onSurface = RadarrWhite,
    surfaceVariant = RadarrSurfaceVariant,
    onSurfaceVariant = RadarrMuted,
    outline = RadarrBorder,
    error = RadarrRed,
    onError = RadarrWhite
)

@Composable
fun RadarrTVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = RadarrTypography,
        content = content
    )
}
