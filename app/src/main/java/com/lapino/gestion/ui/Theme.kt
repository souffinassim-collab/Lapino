package com.lapino.gestion.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GreenPrimary = Color(0xFF2E7D32)
val GreenLight = Color(0xFF4CAF50)
val GreenContainer = Color(0xFFE8F5E9)
val PinkSaillie = Color(0xFFE91E63)
val PinkContainer = Color(0xFFFCE4EC)
val BlueGestante = Color(0xFF1976D2)
val BlueContainer = Color(0xFFE3F2FD)
val OrangeAlert = Color(0xFFEF6C00)
val OrangeContainer = Color(0xFFFFF3E0)
val RedCritical = Color(0xFFC62828)
val RedContainer = Color(0xFFFFEBEE)

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF388E3C),
    background = Color(0xFFF6F8F6),
    surface = Color.White,
    onSurface = Color(0xFF1E1E1E),
    error = RedCritical
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003300),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFA5D6A7),
    background = Color(0xFF121212),
    surface = Color(0xFF1E221E),
    onSurface = Color(0xFFE8ECE8),
    error = Color(0xFFEF9A9A)
)

@Composable
fun LapinoTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

