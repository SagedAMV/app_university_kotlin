package com.unimanager.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Common 2026 Animation Patterns for Jetpack Compose
 */

// Fade + Scale entrance animation
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
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(tween(200)) + scaleOut(tween(200)),
        modifier = modifier
    ) {
        content()
    }
}

// Slide up entrance animation
@Composable
fun SlideUpEntrance(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 400, delayMillis = delayMillis)
        ) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 3 }
        ),
        exit = fadeOut(tween(200)) + slideOutVertically(tween(200), targetOffsetY = { it / 3 }),
        modifier = modifier
    ) {
        content()
    }
}

// Staggered list entrance animation
@Composable
fun <T> StaggeredList(
    items: List<T>,
    modifier: Modifier = Modifier,
    initialDelay: Int = 0,
    delayPerItem: Int = 80,
    itemContent: @Composable (T, Int) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            AnimatedEntrance(
                visible = isVisible,
                delayMillis = initialDelay + (index * delayPerItem)
            ) {
                itemContent(item, index)
            }
        }
    }
}

// Pulse animation for emphasis
@Composable
fun Modifier.pulseEffect(
    active: Boolean = true,
    scaleRange: ClosedFloatingPointRange<Float> = 0.97f..1.03f,
    durationMillis: Int = 1500
): Modifier {
    if (!active) return this

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = scaleRange.start,
        targetValue = scaleRange.endInclusive,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    return this.then(Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    })
}

// Breathing glow effect
@Composable
fun rememberBreathingAlpha(
    active: Boolean = true,
    minAlpha: Float = 0.5f,
    maxAlpha: Float = 1f,
    durationMillis: Int = 2000
): Float {
    if (!active) return maxAlpha
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingAlpha"
    )
    return alpha
}

// Content transition for screen state changes
@Composable
fun <T> AnimatedScreenContent(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            (fadeIn(animationSpec = tween(300)) + slideInVertically { it / 3 })
                .togetherWith(
                    fadeOut(animationSpec = tween(200)) + slideOutVertically { -it / 3 }
                )
                .using(SizeTransform(clip = false))
        },
        modifier = modifier,
        label = "screenContent"
    ) { state ->
        content(state)
    }
}

// Rotation animation
@Composable
fun rememberRotation(
    active: Boolean = true,
    durationMillis: Int = 8000
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing)
        ),
        label = "rotation"
    )
    return if (active) rotation else 0f
}
