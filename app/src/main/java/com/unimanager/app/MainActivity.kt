package com.unimanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unimanager.app.ui.navigation.AppNavigation
import com.unimanager.app.ui.theme.UniManagerTheme
import com.unimanager.app.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniManagerTheme {
                val viewModel: AppViewModel = viewModel()
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
