package com.unimanager.app.ui.galaxy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unimanager.app.viewmodel.AppViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalaxyScreen(viewModel: AppViewModel, navController: NavController) {
    val folders by viewModel.rootFolders.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { TopAppBar(title = { Text("🌌 خريطة الملفات") }) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                val radius = size.minDimension * 0.3f

                // Draw center
                drawCircle(color = Color(0xFFF59E0B), radius = 30f, center = Offset(cx, cy))

                // Draw folders around center
                folders.forEachIndexed { index, folder ->
                    val angle = (index.toFloat() / folders.size.coerceAtLeast(1)) * 360f
                    val x = cx + radius * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat()
                    val y = cy + radius * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()

                    // Draw connection line
                    drawLine(
                        color = Color(0xFF6366F1),
                        start = Offset(cx, cy),
                        end = Offset(x, y),
                        strokeWidth = 2f
                    )

                    // Draw folder circle
                    drawCircle(color = Color(0xFF6366F1), radius = 20f, center = Offset(x, y))
                }
            }

            // Legend
            Column(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(" اسحب للتحرك • انقر على دائرة للانتقال")
            }
        }
    }
}
