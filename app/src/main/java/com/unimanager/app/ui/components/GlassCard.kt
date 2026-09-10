package com.unimanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    cornerRadius: Dp = 24.dp,
    alpha: Float = 0.12f,
    borderWidth: Dp = 1.dp,
    borderAlpha: Float = 0.25f,
    blurRadius: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    // Glass gradient colors
    val glassColor = if (isDark) Color.White else Color.White
    val borderColor = if (isDark) Color.White else Color.White

    Card(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        glassColor.copy(alpha = alpha + 0.08f),
                        glassColor.copy(alpha = alpha - 0.04f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor.copy(alpha = borderAlpha + 0.1f),
                        borderColor.copy(alpha = 0.05f),
                        borderColor.copy(alpha = borderAlpha)
                    )
                ),
                shape = shape
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
fun GlassStatCard(
    icon: String,
    number: String,
    label: String,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = if (isDark) 0.15f else 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                icon,
                style = MaterialTheme.typography.headlineMedium
            )
            androidx.compose.material3.Text(
                number,
                style = MaterialTheme.typography.headlineLarge,
                color = accentColor
            )
            androidx.compose.material3.Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)
            )
        }
    }
}
