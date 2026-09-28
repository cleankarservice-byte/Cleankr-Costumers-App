package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CleankrTeal,
    onPrimary = CleankrCardSurface,
    primaryContainer = CleankrTealLight,
    onPrimaryContainer = CleankrTealDark,
    secondary = CleankrNavy,
    onSecondary = CleankrCardSurface,
    background = CleankrBackground,
    onBackground = CleankrNavy,
    surface = CleankrCardSurface,
    onSurface = CleankrNavy,
    surfaceVariant = CleankrBackground,
    onSurfaceVariant = CleankrSlate,
    outline = CleankrBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = CleankrTeal,
    onPrimary = CleankrCardSurface,
    secondary = CleankrTealLight,
    onSecondary = CleankrNavy,
    background = CleankrNavy,
    onBackground = CleankrBackground,
    surface = Color(0xFF1E293B),
    onSurface = CleankrBackground
)

@Composable
fun CleankrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
