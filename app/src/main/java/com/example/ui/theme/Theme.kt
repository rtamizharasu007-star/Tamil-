package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
    primary = ArcCyan,
    onPrimary = ArcBlueDark,
    primaryContainer = ArcSurfaceVariant,
    onPrimaryContainer = TextCyan,
    secondary = ArcGold,
    onSecondary = ArcBlueDark,
    background = ArcBlueDark,
    onBackground = TextPrimary,
    surface = ArcSurface,
    onSurface = TextPrimary,
    surfaceVariant = ArcSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = ArcBorder,
    error = ErrorRed
)

@Composable
fun JarvisTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
