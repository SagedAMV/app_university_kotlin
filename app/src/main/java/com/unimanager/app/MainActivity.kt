package com.unimanager.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.unimanager.app.ui.navigation.AppNavigation
import com.unimanager.app.ui.theme.UniManagerTheme
import com.unimanager.app.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Log.w(TAG, "Some permissions were denied: $permissions")
            Toast.makeText(
                this,
                "بعض الأذونات مرفوضة. قد لا تعمل بعض الميزات.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Log.i(TAG, "All permissions granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission (Android 13+)
        requestNotificationPermission()

        setContent {
            // Theme is now applied inside AppNavigation via ThemePreferenceManager
            val viewModel: AppViewModel = hiltViewModel()
            AppNavigation(viewModel = viewModel)
        }
    }

    private fun requestNotificationPermission() {
        // إذن الإشعارات مطلوب فقط من Android 13 (TIRAMISU) فما فوق.
        // الملفات تُختار عبر SAF وتُنسخ للتخزين الداخلي، فلا حاجة لأذونات التخزين.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS")
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            } else {
                Log.d(TAG, "Notification permission already granted")
            }
        }
    }
}
