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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.unimanager.app.ui.components.AnimatedBottomNavBar
import com.unimanager.app.ui.dashboard.DashboardScreen
import com.unimanager.app.ui.exams.ExamsScreen
import com.unimanager.app.ui.files.FilesScreen
import com.unimanager.app.ui.galaxy.GalaxyScreen
import com.unimanager.app.ui.notes.NotesScreen
import com.unimanager.app.ui.schedule.ScheduleScreen
import com.unimanager.app.ui.tasks.TasksScreen
import com.unimanager.app.viewmodel.AppViewModel

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val navItems = listOf(
    NavItem("dashboard", "الرئيسية", Icons.Filled.Home),
    NavItem("files", "الملفات", Icons.Filled.Folder),
    NavItem("tasks", "المهام", Icons.Filled.CheckCircle),
    NavItem("notes", "ملاحظاتي", Icons.Filled.Note),
    NavItem("exams", "الامتحانات", Icons.Filled.School),
    NavItem("schedule", "الجدول", Icons.Filled.CalendarToday),
    NavItem("galaxy", "المجرة", Icons.Filled.Star)
)

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            AnimatedBottomNavBar(
                navController = navController,
                navItems = navItems
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable(
                "dashboard",
                enterTransition = { fadeIn(tween(300)) + slideInHorizontally { it } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { it } }
            ) { DashboardScreen(viewModel = viewModel, navController = navController) }

            composable(
                "files",
                enterTransition = { fadeIn(tween(300)) + slideInHorizontally { -it } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally { -it } }
            ) { FilesScreen(viewModel = viewModel, navController = navController) }

            composable(
                "tasks",
                enterTransition = { fadeIn(tween(300)) + slideInVertically { it / 3 } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(200)) }
            ) { TasksScreen(viewModel = viewModel) }

            composable(
                "notes",
                enterTransition = { fadeIn(tween(300)) + slideInVertically { it / 3 } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(200)) }
            ) { NotesScreen(viewModel = viewModel) }

            composable(
                "exams",
                enterTransition = { fadeIn(tween(300)) + slideInVertically { it / 3 } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(200)) }
            ) { ExamsScreen(viewModel = viewModel) }

            composable(
                "schedule",
                enterTransition = { fadeIn(tween(300)) + slideInVertically { it / 3 } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(200)) }
            ) { ScheduleScreen(viewModel = viewModel) }

            composable(
                "galaxy",
                enterTransition = { fadeIn(tween(400)) + scaleIn(initialScale = 0.9f) },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(400)) + scaleIn(initialScale = 0.9f) },
                popExitTransition = { fadeOut(tween(200)) + scaleOut(targetScale = 0.9f) }
            ) { GalaxyScreen(viewModel = viewModel, navController = navController) }
        }
    }
}
