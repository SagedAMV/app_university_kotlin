package com.unimanager.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Swipeable Item - يدعم السحب من اليمين لليسار للحذف
 * مناسب للواجهة العربية (RTL)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableItem(
    onSwipe: () -> Unit,
    content: @Composable () -> Unit
) {
    var isDismissed by remember { mutableStateOf(false) }
    val dismissState = rememberDismissState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart) {
                isDismissed = true
                onSwipe()
                true
            } else {
                false
            }
        }
    )

    if (isDismissed) return

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            val color = Color(0xFFEF4444)
            val alpha by animateFloatAsState(
                targetValue = if (dismissState.targetValue == DismissValue.Default) 0f else 1f,
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = alpha * 0.2f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "حذف",
                    tint = color,
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .size(24.dp)
                )
            }
        },
        dismissContent = { content() }
    )
}
