package com.unimanager.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * عنصر قابل للسحب من اليمين لليسار (RTL) لكشف زر الحذف.
 *
 * السحب لا يحذف مباشرة: يستدعي [onSwipe] فقط ثم يعود العنصر لمكانه،
 * ليترك للشاشة الأم قرار الحذف عبر حوار تأكيد (شبكة أمان ضد الضغط الخاطئ).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableItem(
    onSwipe: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onSwipe()
            }
            // نعيد دائمًا false حتى لا يُزال العنصر دون تأكيد
            false
        }
    )

    val alpha by animateFloatAsState(
        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0f else 1f,
        label = "swipeAlpha"
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEF4444).copy(alpha = alpha * 0.2f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "حذف",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .size(24.dp)
                )
            }
        }
    ) {
        content()
    }
}
