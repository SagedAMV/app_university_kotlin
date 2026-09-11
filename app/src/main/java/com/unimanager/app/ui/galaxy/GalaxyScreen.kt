package com.unimanager.app.ui.galaxy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unimanager.app.ui.navigation.Routes
import com.unimanager.app.viewmodel.AppViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalaxyScreen(viewModel: AppViewModel, navController: NavController) {
    val folders by viewModel.rootFolders.collectAsState(initial = emptyList())
    val allFiles by viewModel.allFiles.collectAsState(initial = emptyList())

    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🌌 خريطة الملفات",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { offset = Offset.Zero; scale = 1f }) {
                        Icon(Icons.Filled.CenterFocusWeak, contentDescription = "إعادة ضبط")
                    }
                }
            )
        }
    ) { padding ->
        // BoxWithConstraints يوفّر أبعاد الحاوية بالبكسل (Dp) بدل size الخاص بـ DrawScope
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F172A))
        ) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val minDimensionPx = minOf(widthPx, heightPx)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offset += dragAmount
                        }
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                        }
                    }
            ) {
                val centerX = size.width / 2 + offset.x
                val centerY = size.height / 2 + offset.y
                val radius = (size.minDimension * 0.3f) * scale

                // Draw center (sun)
                drawCircle(
                    color = Color(0xFFF59E0B),
                    radius = 40f * scale,
                    center = Offset(centerX, centerY)
                )

                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.3f),
                    radius = 60f * scale,
                    center = Offset(centerX, centerY)
                )

                // Draw folders around center
                folders.forEachIndexed { index, folder ->
                    val angle = (index.toFloat() / folders.size.coerceAtLeast(1)) * 360f
                    val angleRad = Math.toRadians(angle.toDouble())
                    val x = centerX + (radius * cos(angleRad)).toFloat()
                    val y = centerY + (radius * sin(angleRad)).toFloat()

                    // Draw connection line with dash effect
                    drawLine(
                        color = Color(0xFF6366F1).copy(alpha = 0.5f),
                        start = Offset(centerX, centerY),
                        end = Offset(x, y),
                        strokeWidth = 2f * scale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Draw folder circle
                    drawCircle(
                        color = Color(0xFF6366F1),
                        radius = 25f * scale,
                        center = Offset(x, y)
                    )

                    // Draw pulse effect
                    val pulseAlpha = ((sin(System.currentTimeMillis() / 500.0 + index) + 1) / 4).toFloat()
                    drawCircle(
                        color = Color(0xFF6366F1).copy(alpha = pulseAlpha),
                        radius = 35f * scale,
                        center = Offset(x, y)
                    )
                }
            }

            // Display folder names (أبعادها من BoxWithConstraints وليس من Canvas)
            folders.forEachIndexed { index, folder ->
                val angle = (index.toFloat() / folders.size.coerceAtLeast(1)) * 360f
                val angleRad = Math.toRadians(angle.toDouble())
                val baseRadius = minDimensionPx * 0.3f
                val x = widthPx / 2 + offset.x + (baseRadius * scale * cos(angleRad)).toFloat()
                val y = heightPx / 2 + offset.y + (baseRadius * scale * sin(angleRad)).toFloat()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x.roundToInt(), y.roundToInt() + 40) }
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            // الانتقال الفعلي إلى محتوى ذلك المجلد
                            navController.navigate(Routes.Files.folder(folder.id))
                        }
                        .background(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        folder.name,
                        color = Color.White,
                        fontSize = (12 * scale).sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Legend
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🌌 اسحب للتحرك • Pinch للتكبير/التصغير",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    "انقر على اسم المجلد للانتقال إليه",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }

            // Stats
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text(
                    "📁 ${folders.size} مجلد",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    " ${allFiles.size} ملف",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
