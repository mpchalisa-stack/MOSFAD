package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MosfadDarkColorScheme = darkColorScheme(
    primary = TacticalGreen,
    onPrimary = TacticalBgDeep,
    primaryContainer = TacticalGreenDark,
    onPrimaryContainer = TacticalGreen,
    secondary = TacticalCyan,
    onSecondary = TacticalBgDeep,
    secondaryContainer = TacticalCyanDark,
    onSecondaryContainer = TacticalCyan,
    tertiary = TacticalAmber,
    onTertiary = TacticalBgDeep,
    tertiaryContainer = TacticalAmberDark,
    onTertiaryContainer = TacticalAmber,
    error = TacticalRed,
    onError = TacticalTextPrimary,
    errorContainer = TacticalRedDark,
    onErrorContainer = TacticalRed,
    background = TacticalBgDeep,
    onBackground = TacticalTextPrimary,
    surface = TacticalSurface,
    onSurface = TacticalTextPrimary,
    surfaceVariant = TacticalSurfaceElevated,
    onSurfaceVariant = TacticalTextSecondary,
    outline = TacticalBorder,
    outlineVariant = TacticalBorderBright
)

@Composable
fun MosfadTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MosfadDarkColorScheme,
        typography = TacticalTypography,
        content = content
    )
}
