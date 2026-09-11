package com.unimanager.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.unimanager.app.ui.backup.BackupScreen
import com.unimanager.app.ui.components.AnimatedBottomNavBar
import com.unimanager.app.ui.dashboard.DashboardScreen
import com.unimanager.app.ui.files.FilesScreen
import com.unimanager.app.ui.galaxy.GalaxyScreen
import com.unimanager.app.ui.settings.SettingsScreen
import com.unimanager.app.ui.theme.UniManagerTheme
import com.unimanager.app.ui.unified.UnifiedScreen
import com.unimanager.app.viewmodel.AppViewModel
import com.unimanager.app.viewmodel.ThemeViewModel

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val navItems = listOf(
    NavItem(Routes.Dashboard.route, "الرئيسية", Icons.Filled.Home),
    NavItem(Routes.Files.route, "الملفات", Icons.Filled.Folder),
    NavItem(Routes.Unified.route, "الأقسام", Icons.Filled.Apps),
    NavItem(Routes.Galaxy.route, "المجرة", Icons.Filled.Star)
)

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val themeViewModel: ThemeViewModel = hiltViewModel()

    // Theme state
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val useDynamicColor by themeViewModel.useDynamicColor.collectAsState()

    // Apply theme
    UniManagerTheme(
        darkTheme = isDarkTheme,
        dynamicColor = useDynamicColor
    ) {
        Scaffold(
            bottomBar = {
                // Hide bottom bar on settings/backup screens
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route
                val hiddenRoutes = listOf(Routes.Settings.route, Routes.Backup.route)
                if (currentRoute !in hiddenRoutes) {
                    AnimatedBottomNavBar(
                        navController = navController,
                        navItems = navItems
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.Dashboard.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(
                    Routes.Dashboard.route,
                    enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it } },
                    exitTransition = { fadeOut(tween(200)) },
                    popEnterTransition = { fadeIn(tween(300)) },
                    popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it } }
                ) { DashboardScreen(viewModel = viewModel, navController = navController) }

                composable(
                    Routes.Files.pattern,
                    arguments = listOf(
                        navArgument(Routes.Files.FOLDER_ID_ARG) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    ),
                    enterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it } },
                    exitTransition = { fadeOut(tween(200)) },
                    popEnterTransition = { fadeIn(tween(300)) },
                    popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { -it } }
                ) { FilesScreen(viewModel = viewModel, navController = navController) }

                composable(
                    Routes.Unified.pattern,
                    arguments = listOf(
                        navArgument(Routes.Unified.TAB_ARG) {
                            type = NavType.IntType
                            defaultValue = 0
                        }
                    ),
                    enterTransition = { fadeIn(tween(300)) + slideInVertically { it / 3 } },
                    exitTransition = { fadeOut(tween(200)) },
                    popEnterTransition = { fadeIn(tween(300)) },
                    popExitTransition = { fadeOut(tween(200)) }
                ) { UnifiedScreen(viewModel = viewModel, navController = navController) }

                composable(
                    Routes.Galaxy.route,
                    enterTransition = { fadeIn(tween(400)) + scaleIn(initialScale = 0.9f) },
                    exitTransition = { fadeOut(tween(200)) },
                    popEnterTransition = { fadeIn(tween(400)) + scaleIn(initialScale = 0.9f) },
                    popExitTransition = { fadeOut(tween(200)) + scaleOut(targetScale = 0.9f) }
                ) { GalaxyScreen(viewModel = viewModel, navController = navController) }

                composable(
                    Routes.Settings.route,
                    enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it } },
                    exitTransition = { fadeOut(tween(200)) },
                    popEnterTransition = { fadeIn(tween(300)) },
                    popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it } }
                ) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onBackupClick = { navController.navigate(Routes.Backup.route) },
                        isDarkTheme = isDarkTheme,
                        onThemeChange = themeViewModel::setDarkTheme,
                        useDynamicColor = useDynamicColor,
                        onDynamicColorChange = themeViewModel::setDynamicColor
                    )
                }

                composable(
                    Routes.Backup.route,
                    enterTransition = { fadeIn(tween(300)) + slideInVertically { it / 2 } },
                    exitTransition = { fadeOut(tween(200)) },
                    popEnterTransition = { fadeIn(tween(300)) },
                    popExitTransition = { fadeOut(tween(200)) + slideOutVertically { it / 2 } }
                ) {
                    BackupScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
