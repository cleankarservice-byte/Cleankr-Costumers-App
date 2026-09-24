package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CleankrTealLight,
    onPrimary = CleankrNavyDark,
    primaryContainer = CleankrTealDark,
    onPrimaryContainer = Color.White,
    secondary = CleankrOrange,
    onSecondary = Color.White,
    secondaryContainer = CleankrOrangeDark,
    onSecondaryContainer = Color.White,
    background = Color(0xFF090D16),
    surface = Color(0xFF131D2E),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = CleankrTeal,
    onPrimary = Color.White,
    primaryContainer = CleankrTealContainer,
    onPrimaryContainer = CleankrTealDark,
    secondary = CleankrOrange,
    onSecondary = Color.White,
    secondaryContainer = CleankrOrangeContainer,
    onSecondaryContainer = CleankrOrangeDark,
    background = CleankrBackground,
    surface = CleankrSurface,
    onBackground = CleankrNavyDark,
    onSurface = CleankrNavyDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = CleankrSlate,
    outline = CleankrBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent Cleankr brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
