package com.unimanager.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Common Animation Patterns for Jetpack Compose
 */

/**
 * Fade + Scale entrance animation
 * يُستخدم في جميع الشاشات لإظهار المحتوى
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

/**
 * Slide up entrance animation
 * يُستخدم لإظهار عناصر القوائم
 */
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
