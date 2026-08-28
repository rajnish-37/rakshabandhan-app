package com.rajnish.rakshabandhan.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = RakhiRose,
    onPrimary = RakhiInk,
    primaryContainer = RakhiMaroonDark,
    onPrimaryContainer = RakhiCream,
    secondary = RakhiGold,
    onSecondary = RakhiInk,
    secondaryContainer = RakhiMaroonDark,
    onSecondaryContainer = RakhiCream,
    tertiary = RakhiRose,
    background = Color(0xFF1A1114),
    onBackground = RakhiCream,
    surface = Color(0xFF24181C),
    onSurface = RakhiCream,
    surfaceVariant = Color(0xFF3A292E),
    onSurfaceVariant = Color(0xFFE2D1D6),
    outline = Color(0xFF9F858D),
)

private val LightColorScheme = lightColorScheme(
    primary = RakhiMaroon,
    onPrimary = Color.White,
    primaryContainer = RakhiBlush,
    onPrimaryContainer = RakhiMaroonDark,
    secondary = RakhiGold,
    onSecondary = RakhiInk,
    secondaryContainer = RakhiBlush,
    onSecondaryContainer = RakhiMaroonDark,
    tertiary = RakhiRose,
    background = RakhiCream,
    onBackground = RakhiInk,
    surface = Color.White,
    onSurface = RakhiInk,
    surfaceVariant = RakhiBlush,
    onSurfaceVariant = RakhiMuted,
    outline = Color(0xFF8C777D),
)

@Composable
fun RakshaBandhanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // Opt-in remains available for future use, while the default preserves
            // the intentional Rakhi visual identity across devices.
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}