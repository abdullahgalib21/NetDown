package com.net.down.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF33147A),
    onPrimaryContainer = Color(0xFFE7DDFF),
    secondary = TealAccent,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFF9CF2E4),
    tertiary = PinkAccent,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = Color(0xFFE9E8F2),
    surface = DarkSurface,
    onSurface = Color(0xFFE9E8F2),
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = Color(0xFFB8B4CC),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = VioletDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DFFF),
    onPrimaryContainer = Color(0xFF1D006B),
    secondary = Color(0xFF007A6E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA7F2E5),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = PinkAccent,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF1C1B22),
    surface = LightSurface,
    onSurface = Color(0xFF1C1B22),
    surfaceVariant = LightSurfaceHigh,
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun NetDownTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = NetDownTypography,
        shapes = NetDownShapes,
        content = content
    )
}
