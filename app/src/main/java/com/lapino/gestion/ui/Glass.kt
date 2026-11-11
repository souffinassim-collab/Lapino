package com.lapino.gestion.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/** Fond « aurora » : halos de couleur flous qui dérivent lentement derrière les surfaces en verre. */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val dark = isDarkTheme()
    val base = MaterialTheme.colorScheme.background
    val transition = rememberInfiniteTransition(label = "aurora")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )
    val a = if (dark) 0.34f else 0.42f
    Box(modifier = modifier.fillMaxSize().background(base)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            fun halo(color: Color, cx: Float, cy: Float, r: Float) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = a), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = r
                    ),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }
            halo(Color(0xFF8B5CF6), w * (0.05f + 0.25f * drift), h * 0.08f, w * 0.85f)
            halo(Color(0xFFEC4899), w * (1.0f - 0.2f * drift), h * (0.32f + 0.08f * drift), w * 0.7f)
            halo(Color(0xFF22D3EE), w * (0.15f + 0.3f * drift), h * (0.78f - 0.1f * drift), w * 0.8f)
            halo(Color(0xFF6366F1), w * 0.9f, h * (0.95f - 0.1f * drift), w * 0.6f)
        }
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            content()
        }
    }
}

@Composable
fun glassBrush(): Brush {
    val dark = isDarkTheme()
    return if (dark) {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.13f), Color.White.copy(alpha = 0.06f)))
    } else {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.82f), Color.White.copy(alpha = 0.56f)))
    }
}

@Composable
fun glassBorderBrush(): Brush {
    val dark = isDarkTheme()
    return if (dark) {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.04f)))
    } else {
        Brush.linearGradient(listOf(Color.White, Color.White.copy(alpha = 0.25f)))
    }
}

/** Couleurs « verre » pour les Card Material existantes. */
@Composable
fun glassCardColors(): CardColors {
    val dark = isDarkTheme()
    return CardDefaults.cardColors(
        containerColor = if (dark) Color.White.copy(alpha = 0.09f) else Color.White.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun glassBorder(): BorderStroke = BorderStroke(1.dp, glassBorderBrush())

/** Surface translucide bordée d'un reflet, à poser sur [AuroraBackground]. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val base = modifier
        .then(if (onClick != null) Modifier.pressScale(onClick) else Modifier)
        .clip(shape)
        .background(glassBrush())
        .border(1.dp, glassBorderBrush(), shape)
    Box(modifier = base) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            content()
        }
    }
}

/** Clic avec effet de pression élastique (léger rétrécissement). */
fun Modifier.pressScale(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(interactionSource = interaction, indication = LocalIndication.current, onClick = onClick)
}

/** Apparition décalée (fondu + glissement vers le haut) pour les éléments de liste et les tuiles. */
fun Modifier.staggeredAppear(index: Int): Modifier = composed {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(40L * index.coerceAtMost(12))
        shown = true
    }
    val p by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "appear"
    )
    graphicsLayer {
        alpha = p
        translationY = (1f - p) * 36f
    }
}

/** Halo pulsé (pastille de notification). */
@Composable
fun pulse(): Float {
    val t = rememberInfiniteTransition(label = "pulse")
    val v by t.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "pulseV"
    )
    return v
}
