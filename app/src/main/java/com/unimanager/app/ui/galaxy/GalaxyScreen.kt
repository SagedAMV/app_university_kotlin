package com.unimanager.app.ui.galaxy

import androidx.compose.animation.core.*
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
import kotlin.random.Random

// Data class for stars
data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val phase: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalaxyScreen(viewModel: AppViewModel, navController: NavController) {
    val folders by viewModel.rootFolders.collectAsState(initial = emptyList())
    val allFiles by viewModel.allFiles.collectAsState(initial = emptyList())

    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }

    // Generate random stars once
    val stars = remember {
        List(150) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 2f + 0.5f,
                speed = Random.nextFloat() * 0.5f + 0.3f,
                phase = Random.nextFloat() * 2f * Math.PI.toFloat()
            )
        }
    }

    // Continuous pulse animation for nodes
    val infiniteTransition = rememberInfiniteTransition(label = "galaxyPulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulsePhase"
    )

    // Animation for twinkling stars
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starTwinkle"
    )

    // Animation for flowing lines (dash offset)
    val lineFlowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lineFlow"
    )

    // Animation for rotating glow around sun
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRotation"
    )

    // Entrance animation for folders
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val entranceProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "entrance"
    )

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

                // Draw twinkling stars background
                stars.forEach { star ->
                    val starX = star.x * size.width
                    val starY = star.y * size.height
                    val twinkleValue = sin(starTwinkle * star.speed + star.phase)
                    val alpha = ((twinkleValue + 1f) / 2f) * 0.8f + 0.2f
                    
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = star.size * scale,
                        center = Offset(starX, starY)
                    )
                    
                    // Add glow effect for some stars
                    if (star.size > 1.5f) {
                        drawCircle(
                            color = Color(0xFF6366F1).copy(alpha = alpha * 0.3f),
                            radius = star.size * 2f * scale,
                            center = Offset(starX, starY)
                        )
                    }
                }

                // Draw center sun with rotating glow
                val sunRotationRad = Math.toRadians(sunRotation.toDouble())
                
                // Rotating outer glow layers
                for (i in 0..2) {
                    val angle = sunRotationRad + (i * Math.PI * 2 / 3)
                    val glowOffsetX = cos(angle).toFloat() * 15f * scale
                    val glowOffsetY = sin(angle).toFloat() * 15f * scale
                    
                    drawCircle(
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        radius = 90f * scale,
                        center = Offset(centerX + glowOffsetX, centerY + glowOffsetY)
                    )
                }
                
                // Static glow layers
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                    radius = 70f * scale,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.4f),
                    radius = 50f * scale,
                    center = Offset(centerX, centerY)
                )
                
                // Pulsing core
                val corePulse = ((sin(pulsePhase * 2) + 1f) / 2f) * 0.1f + 0.9f
                drawCircle(
                    color = Color(0xFFF59E0B),
                    radius = 35f * scale * corePulse,
                    center = Offset(centerX, centerY)
                )
                
                // Inner highlight
                drawCircle(
                    color = Color(0xFFFBBF24).copy(alpha = 0.8f),
                    radius = 25f * scale,
                    center = Offset(centerX - 5f * scale, centerY - 5f * scale)
                )

                // Draw folders around center with entrance animation
                folders.forEachIndexed { index, folder ->
                    val angle = (index.toFloat() / folders.size.coerceAtLeast(1)) * 360f
                    val angleRad = Math.toRadians(angle.toDouble())
                    
                    // Apply entrance animation (start from center and expand)
                    val animatedRadius = radius * entranceProgress
                    val x = centerX + (animatedRadius * cos(angleRad)).toFloat()
                    val y = centerY + (animatedRadius * sin(angleRad)).toFloat()

                    // Draw flowing connection line with animated dash
                    drawLine(
                        color = Color(0xFF6366F1).copy(alpha = 0.6f * entranceProgress),
                        start = Offset(centerX, centerY),
                        end = Offset(x, y),
                        strokeWidth = 3f * scale,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(10f, 10f),
                            lineFlowOffset
                        )
                    )

                    // Draw glowing line underneath
                    drawLine(
                        color = Color(0xFF818CF8).copy(alpha = 0.3f * entranceProgress),
                        start = Offset(centerX, centerY),
                        end = Offset(x, y),
                        strokeWidth = 8f * scale
                    )

                    // Draw folder circle with entrance scale
                    val nodeScale = entranceProgress
                    drawCircle(
                        color = Color(0xFF6366F1),
                        radius = 25f * scale * nodeScale,
                        center = Offset(x, y)
                    )

                    // Draw multi-layer pulse effect
                    val pulseAlpha = ((sin(pulsePhase + index) + 1f) / 4f) * entranceProgress
                    drawCircle(
                        color = Color(0xFF6366F1).copy(alpha = pulseAlpha),
                        radius = 40f * scale,
                        center = Offset(x, y)
                    )
                    
                    // Secondary pulse ring
                    val pulseAlpha2 = ((sin(pulsePhase * 1.5f + index + 1f) + 1f) / 5f) * entranceProgress
                    drawCircle(
                        color = Color(0xFF818CF8).copy(alpha = pulseAlpha2),
                        radius = 55f * scale,
                        center = Offset(x, y)
                    )
                    
                    // Inner glow
                    drawCircle(
                        color = Color(0xFF818CF8).copy(alpha = 0.6f * entranceProgress),
                        radius = 18f * scale,
                        center = Offset(x, y)
                    )
                    
                    // Highlight
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f * entranceProgress),
                        radius = 10f * scale,
                        center = Offset(x - 5f * scale, y - 5f * scale)
                    )
                }
            }

            // Display folder names
            folders.forEachIndexed { index, folder ->
                val angle = (index.toFloat() / folders.size.coerceAtLeast(1)) * 360f
                val angleRad = Math.toRadians(angle.toDouble())
                val baseRadius = minDimensionPx * 0.3f
                val animatedRadius = baseRadius * scale * entranceProgress
                val x = widthPx / 2 + offset.x + (animatedRadius * cos(angleRad)).toFloat()
                val y = heightPx / 2 + offset.y + (animatedRadius * sin(angleRad)).toFloat()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x.roundToInt(), y.roundToInt() + 40) }
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            navController.navigate(Routes.Files.folder(folder.id))
                        }
                        .background(
                            color = Color.Black.copy(alpha = 0.7f * entranceProgress),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        folder.name,
                        color = Color.White.copy(alpha = entranceProgress),
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
                    "📄 ${allFiles.size} ملف",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
