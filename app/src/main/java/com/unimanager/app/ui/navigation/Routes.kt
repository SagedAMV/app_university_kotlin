package com.unimanager.app.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation Routes
 * استخدام sealed class للـ routes لتجنب أخطاء الـ strings
 */
sealed class Routes(val route: String) {
    data object Dashboard : Routes("dashboard")
    data object Files : Routes("files")
    data object Unified : Routes("unified")
    data object Galaxy : Routes("galaxy")
    data object Settings : Routes("settings")
    data object Backup : Routes("backup")
    data object FolderDetail : Routes("folder/{folderId}") {
        fun createRoute(folderId: Long) = "folder/$folderId"
    }
}
