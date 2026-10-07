package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val YouTubeColorScheme = darkColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = YouTubeCardSurface,
    onPrimaryContainer = Color.White,
    secondary = YouTubeTextPrimary,
    onSecondary = YouTubeBlack,
    secondaryContainer = YouTubePillBg,
    onSecondaryContainer = YouTubeTextPrimary,
    tertiary = YouTubeBlue,
    onTertiary = Color.White,
    background = YouTubeBlack,
    onBackground = YouTubeTextPrimary,
    surface = YouTubeBlack,
    onSurface = YouTubeTextPrimary,
    surfaceVariant = YouTubeCardSurface,
    onSurfaceVariant = YouTubeTextSecondary,
    outline = YouTubeBorder,
    outlineVariant = YouTubePillBg
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = YouTubeColorScheme,
        typography = Typography,
        content = content
    )
}
