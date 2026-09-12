package com.unimanager.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Advanced Animation Patterns for Jetpack Compose - 2026 Edition
 * Inspired by modern Material Design 3 principles
 */

// =============== Motion Constants ===============
object AppMotion {
    const val STAGGER_STEP_MS = 70
    const val FIELD_STAGGER_MS = 80
    
    val quick = tween<Float>(220, easing = FastOutSlowInEasing)
    val springBounce = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val springGentle = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val springPress = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val elasticOutStrong = CubicBezierEasing(0.25f, 1.8f, 0.4f, 1f)
    val enter = tween<Float>(340, easing = LinearOutSlowInEasing)
    val exit = tween<Float>(280, easing = FastOutLinearInEasing)
    val enterOffset = tween<Int>(340, easing = LinearOutSlowInEasing)
    val exitOffset = tween<Int>(280, easing = FastOutLinearInEasing)
    
    val shimmerColor = Color.White.copy(alpha = 0.28f)
}

// =============== Utility Functions ===============
@Composable
fun rememberReduceMotion(): Boolean {
    // In production, this would check system accessibility settings
    return false
}

// =============== Click & Press Effects ===============

/**
 * 💫 Bounce Click Effect - Enhanced 2026
 * When pressed: element scales down with a white flash overlay
 * When released: bounces back with spring animation
 */
fun Modifier.bounceClick(): Modifier = composed {
    val reduce = rememberReduceMotion()
    val scale = remember { Animatable(1f) }
    val press = remember { Animatable(0f) }
    val pressY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            translationY = pressY.value
        }
        .drawWithContent {
            drawContent()
            if (press.value > 0f) {
                drawRect(Color.White.copy(alpha = press.value * 0.48f))
            }
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (!reduce) {
                    scope.launch { scale.animateTo(0.92f, tween(160, easing = FastOutSlowInEasing)) }
                    scope.launch { press.animateTo(1f, tween(140)) }
                    scope.launch { pressY.animateTo(2f, tween(120, easing = FastOutSlowInEasing)) }
                }
                
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (change.changedToUpIgnoreConsumed() || !change.pressed) break
                }
                
                if (!reduce) {
                    scope.launch { scale.animateTo(1f, AppMotion.springPress) }
                    scope.launch { press.animateTo(0f, tween(380, easing = LinearOutSlowInEasing)) }
                    scope.launch { pressY.animateTo(0f, AppMotion.springPress) }
                }
            }
        }
}

// =============== Entrance Animations ===============

/**
 * 🎯 Elastic Entrance - Material 3 Style
 * Element overshoots then settles with elastic easing
 */
@Composable
fun ElasticEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val reduce = rememberReduceMotion()
    val t = remember { Animatable(if (reduce) 1f else 0f) }
    
    LaunchedEffect(Unit) {
        if (reduce) {
            t.snapTo(1f)
            return@LaunchedEffect
        }
        delay((index * AppMotion.STAGGER_STEP_MS).toLong())
        t.animateTo(1f, tween(480, easing = AppMotion.elasticOutStrong))
    }
    
    val v = t.value
    Box(
        modifier.graphicsLayer {
            val s = 0.5f + 0.5f * v
            scaleX = s
            scaleY = s
            translationY = 18f * (1f - v.coerceAtMost(1.2f))
            alpha = v.coerceIn(0f, 1f)
        }
    ) { content() }
}

/**
 * 🕰️ Swing Card Entrance - Clock Pendulum Effect
 * Card enters with a swinging rotation like a clock hand
 */
@Composable
fun SwingCardEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val reduce = rememberReduceMotion()
    val rotation = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(Unit) {
        if (reduce) {
            alpha.snapTo(1f)
            return@LaunchedEffect
        }
        delay((index * AppMotion.STAGGER_STEP_MS).toLong())
        launch { alpha.animateTo(1f, tween(380, easing = LinearOutSlowInEasing)) }
        
        rotation.snapTo(-12f)
        rotation.animateTo(8f, tween(350, easing = LinearOutSlowInEasing))
        rotation.animateTo(-3f, tween(250, easing = LinearOutSlowInEasing))
        rotation.animateTo(0f, AppMotion.springGentle)
        
        scale.snapTo(0.95f)
        launch { scale.animateTo(1f, AppMotion.springGentle) }
    }
    
    Box(
        modifier.graphicsLayer {
            rotationZ = rotation.value
            this.alpha = alpha.value
            scaleX = scale.value
            scaleY = scale.value
        }
    ) { content() }
}

/**
 * 📋 Sheet Field Entrance - Staggered Form Fields
 * Each field in dialogs appears after the previous one
 */
@Composable
fun SheetFieldEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val reduce = rememberReduceMotion()
    val duration = if (reduce) 0 else 520
    val delayMs = if (reduce) 0 else 120 + index * AppMotion.FIELD_STAGGER_MS
    
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(duration, delayMillis = delayMs, easing = LinearOutSlowInEasing)) +
            slideInVertically(tween(duration, delayMillis = delayMs, easing = LinearOutSlowInEasing)) { it / 5 },
        modifier = modifier
    ) { content() }
}

/**
 * Fade + Scale entrance animation
 * Used across all screens to show content
 */
@Composable
fun AnimatedEntrance(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            )
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(tween(200)) + scaleOut(tween(200)),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Slide from bottom with bounce
 */
@Composable
fun SlideFromBottom(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(300, delayMillis = delayMillis)
        ) + slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(tween(200)) + slideOutVertically { it },
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Slide from right (for RTL support)
 */
@Composable
fun SlideFromRight(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(300, delayMillis = delayMillis)
        ) + slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(tween(200)) + slideOutHorizontally { it },
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Pulse animation for attention
 */
@Composable
fun PulseEffect(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    Box(modifier = modifier.scale(scale)) {
        content()
    }
}

/**
 * Shimmer loading effect
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    
    Box(modifier = modifier.alpha(alpha)) {
        content()
    }
}

// =============== Visual Effects ===============

/**
 * 🌈 Animated Gradient - Living gradient that breathes
 * Smoothly transitions between colors with rotating direction
 */
@Composable
fun animatedGradient(primary: Color, secondary: Color): Brush {
    val reduce = rememberReduceMotion()
    if (reduce) return Brush.linearGradient(listOf(primary, secondary))
    
    val inf = rememberInfiniteTransition(label = "gradient")
    val phase by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(9000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "gradientPhase"
    )
    
    val mid = lerp(primary, secondary, 0.5f)
    return Brush.linearGradient(
        colors = listOf(
            lerp(primary, mid, phase),
            lerp(mid, secondary, phase),
            lerp(secondary, primary, phase)
        ),
        start = Offset.Zero,
        end = Offset(900f * phase, 900f * (1f - phase))
    )
}

/**
 * 🕊️ Floating Icon - Gently floats up and down
 * Creates an elliptical floating motion
 */
@Composable
fun FloatingIcon(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val reduce = rememberReduceMotion()
    val inf = rememberInfiniteTransition(label = "float")
    
    val offsetY by inf.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "floatY"
    )
    
    val offsetX by inf.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            tween(2800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "floatX"
    )
    
    Box(modifier.graphicsLayer {
        translationY = if (reduce) 0f else offsetY
        translationX = if (reduce) 0f else offsetX
    }) { content() }
}

/**
 * 🔢 Animated Number - Counts up to target value
 * Dynamic duration based on difference size
 */
@Composable
fun AnimatedNumber(
    target: Double,
    format: (Double) -> String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle? = null,
    color: Color = Color.Unspecified,
    hidden: Boolean = false
) {
    val reduceMotion = rememberReduceMotion()
    val animated = remember { Animatable(target.toFloat()) }
    val flash = remember { Animatable(0f) }
    var counting by remember { mutableStateOf(false) }
    var lastTarget by remember { mutableStateOf(target) }
    
    LaunchedEffect(target) {
        val diff = kotlin.math.abs(target.toFloat() - animated.value)
        val duration = (200 + (diff / 100f).coerceIn(0f, 300f)).toInt()
        
        if (!reduceMotion && diff > 0.001) counting = true
        animated.animateTo(
            targetValue = target.toFloat(),
            animationSpec = if (reduceMotion) snap() else tween(duration, easing = FastOutSlowInEasing)
        )
        counting = false
        
        if (!reduceMotion && target > lastTarget) {
            flash.snapTo(0.18f)
            flash.animateTo(0f, tween(700, easing = LinearOutSlowInEasing))
        }
        lastTarget = target
    }
    
    Box(modifier) {
        androidx.compose.material3.Text(
            text = if (hidden) "•••••" else format(animated.value.toDouble()),
            style = style ?: MaterialTheme.typography.headlineMedium,
            color = color
        )
        if (flash.value > 0f) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color(0xFF00B894).copy(alpha = flash.value))
            )
        }
    }
}

/**
 * Rotate continuously
 */
@Composable
fun RotateAnimation(
    modifier: Modifier = Modifier,
    durationMillis: Int = 2000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(modifier = modifier.graphicsLayer { rotationZ = rotation }) {
        content()
    }
}

/**
 * Bounce animation on appear
 */
@Composable
fun BounceOnAppear(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(200, delayMillis = delayMillis)
        ) + scaleIn(
            initialScale = 0.3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.3f),
        modifier = modifier
    ) {
        content()
    }
}
