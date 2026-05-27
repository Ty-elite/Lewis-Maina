package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = KenyaGreen,
    onPrimary = TextPrimaryLight,
    primaryContainer = KenyaGreenDark,
    onPrimaryContainer = TextPrimaryLight,
    secondary = GoldAccent,
    onSecondary = TextPrimaryDark,
    background = NeutralDarkBackground,
    onBackground = TextPrimaryLight,
    surface = NeutralDarkSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = NeutralDarkGrey,
    onSurfaceVariant = TextSecondaryLight,
    outline = TextSecondaryLight
)

private val LightColorScheme = lightColorScheme(
    primary = KenyaGreen,
    onPrimary = TextPrimaryLight,
    primaryContainer = KenyaGreenLight,
    onPrimaryContainer = KenyaGreenDark,
    secondary = GoldAccent,
    onSecondary = TextPrimaryDark,
    background = NeutralLightBackground,
    onBackground = TextPrimaryDark,
    surface = NeutralLightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NeutralLightGrey,
    onSurfaceVariant = TextSecondaryDark,
    outline = TextSecondaryDark
)

@Composable
fun KenyaRentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
