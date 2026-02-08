package com.boltmind.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    // Orange: Buttons, FAB, active indicators
    primary = Orange50,
    onPrimary = Color.White,
    primaryContainer = Navy30,
    onPrimaryContainer = Color.White,

    // Blue: Tabs, secondary elements
    secondary = Navy50,
    onSecondary = Color.White,
    secondaryContainer = Navy80,
    onSecondaryContainer = Navy20,

    // Green: Success states
    tertiary = Green50,
    onTertiary = Color.White,
    tertiaryContainer = Green80,
    onTertiaryContainer = Green30,

    // Error
    error = Red50,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Red40,

    // Backgrounds
    background = Neutral99,
    onBackground = Neutral10,
    surface = Color.White,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral30,

    // Borders
    outline = Neutral60,
    outlineVariant = Neutral80,

    // Inverse
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    inversePrimary = Orange60
)

private val DarkColorScheme = darkColorScheme(
    // Orange: lighter for dark backgrounds
    primary = Orange60,
    onPrimary = Orange30,
    primaryContainer = Navy40,
    onPrimaryContainer = Navy80,

    // Blue
    secondary = Navy60,
    onSecondary = Navy10,
    secondaryContainer = Navy30,
    onSecondaryContainer = Navy80,

    // Green
    tertiary = Green80,
    onTertiary = Green30,
    tertiaryContainer = Green50,
    onTertiaryContainer = Green80,

    // Error
    error = Red80,
    onError = Red20,
    errorContainer = Red40,
    onErrorContainer = Red80,

    // Backgrounds
    background = Neutral04,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral30,
    onSurfaceVariant = Neutral80,

    // Borders
    outline = Neutral60,
    outlineVariant = Neutral40,

    // Inverse
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    inversePrimary = Orange50
)

@Composable
fun BoltMindTheme(
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
