package com.boltmind.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BoltMindColorScheme = darkColorScheme(
    primary = BoltPrimary,
    onPrimary = BoltOnPrimary,
    primaryContainer = BoltPrimaryContainer,
    onPrimaryContainer = BoltOnPrimaryContainer,
    secondary = BoltSecondary,
    onSecondary = BoltOnSecondary,
    secondaryContainer = BoltSecondaryContainer,
    onSecondaryContainer = BoltOnSecondaryContainer,
    tertiary = BoltTertiary,
    onTertiary = BoltOnTertiary,
    background = BoltBackground,
    onBackground = BoltOnBackground,
    surface = BoltSurface,
    onSurface = BoltOnSurface,
    surfaceVariant = BoltSurfaceVariant,
    onSurfaceVariant = BoltOnSurfaceVariant,
    surfaceContainerHigh = BoltSurfaceContainerHigh,
    surfaceBright = BoltSurfaceBright,
    surfaceContainer = BoltSurfaceContainer,
    outline = BoltOutline,
    outlineVariant = BoltOutlineVariant,
    error = BoltError,
    onError = BoltOnError,
    errorContainer = BoltErrorContainer,
    onErrorContainer = BoltOnErrorContainer,
    inverseSurface = BoltInverseSurface,
    inverseOnSurface = BoltInverseOnSurface,
    inversePrimary = BoltInversePrimary,
    scrim = BoltScrim,
)

@Composable
fun BoltMindTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = BoltBackground.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = BoltBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = BoltMindColorScheme,
        typography = BoltMindTypography,
        shapes = BoltMindShapes,
        content = content,
    )
}
