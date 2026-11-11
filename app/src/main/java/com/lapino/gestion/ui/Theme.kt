package com.lapino.gestion.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Couleur de marque (indigo-violet)
val BrandPrimary = Color(0xFF5B4FE9)
val BrandLight = Color(0xFF8B7CF6)
val BrandContainer = Color(0xFFECEAFE)

// Couleurs par phase du cycle
val PinkSaillie = Color(0xFFDB2777)
val PinkContainer = Color(0xFFFCE7F3)
val BlueGestante = Color(0xFF0369A1)
val BlueContainer = Color(0xFFE0F2FE)
val MintNursing = Color(0xFF047857)
val MintContainer = Color(0xFFD1FAE5)
val OrangeAlert = Color(0xFFB45309)
val OrangeContainer = Color(0xFFFEF3C7)
val RedCritical = Color(0xFFDC2626)
val RedContainer = Color(0xFFFEE2E2)

val HeroGradient = Brush.linearGradient(
    listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFFA855F7))
)

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandContainer,
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF2E1065),
    tertiary = MintNursing,
    background = Color(0xFFF4F5FB),
    onBackground = Color(0xFF12131A),
    surface = Color.White,
    onSurface = Color(0xFF12131A),
    surfaceVariant = Color(0xFFEDEEF6),
    onSurfaceVariant = Color(0xFF5B5E70),
    outline = Color(0xFFC9CBDA),
    outlineVariant = Color(0xFFE4E5EF),
    error = RedCritical
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5A0FF),
    onPrimary = Color(0xFF16124A),
    primaryContainer = Color(0xFF322C8A),
    onPrimaryContainer = Color(0xFFE4E1FF),
    secondary = Color(0xFFC4B5FD),
    onSecondary = Color(0xFF2E1065),
    secondaryContainer = Color(0xFF3B2A7A),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = Color(0xFF6EE7B7),
    background = Color(0xFF0D0E14),
    onBackground = Color(0xFFECEDF5),
    surface = Color(0xFF181A23),
    onSurface = Color(0xFFECEDF5),
    surfaceVariant = Color(0xFF232632),
    onSurfaceVariant = Color(0xFFA9ADC0),
    outline = Color(0xFF4A4E60),
    outlineVariant = Color(0xFF2C2F3D),
    error = Color(0xFFFCA5A5)
)

private val LapinoShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val base = Typography()
private val LapinoTypography = Typography(
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Bold),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.1.sp)
)

@Composable
fun LapinoTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = LapinoShapes,
        typography = LapinoTypography,
        content = content
    )
}
