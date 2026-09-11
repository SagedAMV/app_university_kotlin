package com.unimanager.app.ui.navigation

/**
 * مسارات التنقل.
 * كل مسار كائن يحمل [route] لتتوافق مع استدعاءات Routes.X.route في التطبيق.
 */
object Routes {
    object Dashboard { const val route = "dashboard" }
    object Files {
        const val route = "files"
        const val FOLDER_ID_ARG = "folderId"

        /** النمط الكامل مع وسيط اختياري لمعرّف المجلد */
        const val pattern = "files?$FOLDER_ID_ARG={$FOLDER_ID_ARG}"

        /** مسار فتح شاشة الملفات داخل مجلد معيّن (أو الجذر عند null) */
        fun folder(folderId: Long?): String =
            if (folderId == null) route else "files?$FOLDER_ID_ARG=$folderId"
    }
    object Unified { const val route = "unified" }
    object Galaxy { const val route = "galaxy" }
    object Settings { const val route = "settings" }
    object Backup { const val route = "backup" }
}
