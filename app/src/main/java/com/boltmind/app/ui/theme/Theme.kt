package com.boltmind.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * BoltMind ist bewusst nur dunkel. Die Zuordnung nach Material3 deckt die Faelle
 * ab, in denen ein Material-Baustein selbst Farben zieht; die Screens arbeiten
 * direkt mit den Token aus Color.kt.
 */
private val BoltMindFarbschema = darkColorScheme(
    primary = BoltOrange,
    onPrimary = BoltAufOrange,
    primaryContainer = BoltOrangeVerlaufUnten,
    onPrimaryContainer = BoltOrangeHell,
    secondary = BoltTextSekundaer,
    onSecondary = BoltHintergrund,
    tertiary = BoltGruen,
    onTertiary = BoltAufGruen,
    background = BoltHintergrund,
    onBackground = BoltTextPrimaer,
    surface = BoltHintergrund,
    onSurface = BoltTextPrimaer,
    surfaceVariant = BoltChipFlaeche,
    onSurfaceVariant = BoltTextSchwach,
    outline = BoltRahmenDunkel,
    outlineVariant = BoltRahmenGestrichelt,
    error = BoltFehler,
    onError = BoltTextWeiss,
    errorContainer = BoltGefahrFlaeche,
    onErrorContainer = BoltFehlerText,
    scrim = BoltScrim
)

@Composable
fun BoltMindTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Der Entwurf zeichnet bis unter die Systemleisten; die Screens
            // halten den Inhalt ueber Insets frei.
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = BoltMindFarbschema,
        typography = BoltMindTypography,
        shapes = BoltMindShapes,
        content = content
    )
}
