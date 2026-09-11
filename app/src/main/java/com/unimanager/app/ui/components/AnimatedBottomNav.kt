package com.unimanager.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.unimanager.app.ui.navigation.NavItem

@Composable
fun AnimatedBottomNavBar(
    navController: NavController,
    navItems: List<NavItem>
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    NavigationBar(
        modifier = Modifier.height(76.dp),
        tonalElevation = 2.dp,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        navItems.forEachIndexed { index, item ->
            val selected = currentBackStackEntry?.destination?.hierarchy?.any {
                it.route == item.route || it.route?.startsWith("${item.route}?") == true
            } == true

            val scale by animateFloatAsState(
                targetValue = if (selected) 1.15f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "navScale_$index"
            )

            val iconRotation by animateFloatAsState(
                targetValue = if (selected) 360f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "navRotation_$index"
            )

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Box(modifier = Modifier.scale(scale)) {
                        androidx.compose.animation.AnimatedContent(
                            targetState = selected,
                            transitionSpec = {
                                fadeIn(tween(300)) + scaleIn(initialScale = 0.8f) togetherWith
                                        fadeOut(tween(200)) + scaleOut(targetScale = 0.8f)
                            },
                            label = "iconAnim_$index"
                        ) { isSelected ->
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(if (isSelected) 26.dp else 24.dp)
                            )
                        }
                    }
                },
                label = {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn(tween(250)) + expandVertically(tween(250)) + 
                                slideInVertically { -it / 2 },
                        exit = fadeOut(tween(200)) + shrinkVertically() + 
                               slideOutVertically { -it / 2 }
                    ) {
                        Text(
                            item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
