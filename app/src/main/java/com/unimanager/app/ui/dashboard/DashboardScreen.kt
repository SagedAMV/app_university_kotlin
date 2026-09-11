package com.unimanager.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unimanager.app.ui.components.*
import com.unimanager.app.ui.navigation.Routes
import com.unimanager.app.ui.theme.*
import com.unimanager.app.viewmodel.AppViewModel
import java.util.*
import kotlin.math.sin

/**
 * شاشة الرئيسية المُعاد تصميمها بالكامل - 2026
 * تصميم عصري بأنيميشنات سلسة ومبتكرة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AppViewModel, navController: NavController) {
    // State collection
    val fileCount by viewModel.fileCount.collectAsState()
    val lectureCount by viewModel.lectureCount.collectAsState()
    val pendingTasks by viewModel.pendingTaskCount.collectAsState()
    val upcomingExams by viewModel.upcomingExamCount.collectAsState()
    val totalSize by viewModel.totalSize.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val upcomingExamList by viewModel.upcomingExams.collectAsState()
    val pendingTaskList by viewModel.pendingTasks.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var screenVisible by remember { mutableStateOf(false) }

    // Animation trigger
    LaunchedEffect(Unit) { 
        kotlinx.coroutines.delay(50)
        screenVisible = true 
    }

    // Messages handling
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ModernTopBar(navController, screenVisible)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Hero Welcome Section with Morphing Background
            item {
                MorphingWelcomeHero(screenVisible)
            }

            // Quick Stats Grid with Parallax Effect
            item {
                AnimatedStatsGrid(
                    visible = screenVisible,
                    fileCount = fileCount,
                    totalSize = totalSize,
                    pendingTasks = pendingTasks,
                    upcomingExams = upcomingExams
                )
            }

            // Today's Lectures Card with Glassmorphism
            item {
                GlassmorphicLectureCard(
                    visible = screenVisible,
                    lectureCount = lectureCount,
                    onClick = { navController.navigate(Routes.Unified.tab(1)) }
                )
            }

            // Quick Actions with Staggered Animation
            item {
                StaggeredQuickActions(
                    visible = screenVisible,
                    navController = navController
                )
            }

            // Upcoming Activity Section
            item {
                FloatingSectionHeader(
                    visible = screenVisible,
                    title = "النشاط القادم",
                    icon = "📅",
                    color = Info
                )
            }

            // Upcoming Exams with Slide Animation
            if (upcomingExamList.isNotEmpty()) {
                itemsIndexed(upcomingExamList.take(3)) { index, exam ->
                    SlideInActivityCard(
                        visible = screenVisible,
                        delay = 700 + (index * 80),
                        icon = "🎓",
                        title = exam.subject,
                        subtitle = "${exam.type} • ${exam.examDate}",
                        color = if (exam.type == "نهائي") Danger else Info,
                        accentEmoji = if (exam.type == "نهائي") "🔥" else "📝"
                    )
                }
            }

            // Pending Tasks with Bounce Animation
            if (pendingTaskList.isNotEmpty()) {
                itemsIndexed(pendingTaskList.take(3)) { index, task ->
                    BounceInTaskCard(
                        visible = screenVisible,
                        delay = 900 + (index * 80),
                        title = task.title,
                        priority = task.priority,
                        dueDate = task.dueDate ?: ""
                    )
                }
            }

            // Empty State with Floating Animation
            if (upcomingExamList.isEmpty() && pendingTaskList.isEmpty()) {
                item {
                    FloatingEmptyState(visible = screenVisible)
                }
            }

            // Bottom Spacer
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

// =============== Modern Top Bar ===============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(navController: NavController, visible: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "topBarScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label = "topBarAlpha"
    )

    TopAppBar(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RotatingIcon()
                Text(
                    "الرئيسية",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        actions = {
            PulsingSettingsButton { navController.navigate(Routes.Settings.route) }
        }
    )
}

@Composable
private fun RotatingIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "homeRotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconRotation"
    )

    Text(
        "🏠",
        fontSize = 28.sp,
        modifier = Modifier
            .graphicsLayer { rotationZ = rotation }
            .padding(end = 4.dp)
    )
}

@Composable
private fun PulsingSettingsButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "settingsPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "settingsScale"
    )

    IconButton(onClick = onClick) {
        Icon(
            Icons.Filled.Settings,
            contentDescription = "الإعدادات",
            modifier = Modifier.scale(scale),
            tint = Primary
        )
    }
}

// =============== Morphing Welcome Hero ===============
@Composable
private fun MorphingWelcomeHero(visible: Boolean) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    val infiniteTransition = rememberInfiniteTransition(label = "heroMorph")
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "morphOffsetX"
    )

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "morphOffsetY"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heroScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .scale(scale),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6),
                                Color(0xFFEC4899)
                            )
                        } else {
                            listOf(
                                Color(0xFF4F46E5),
                                Color(0xFF7C3AED),
                                Color(0xFFDB2777)
                            )
                        },
                        start = Offset(offsetX, offsetY),
                        end = Offset(1000f + offsetX, 800f + offsetY)
                    )
                )
        ) {
            // Floating Orbs
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-30).dp)
                    .blur(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-20).dp, y = 30.dp + offsetY.dp / 3)
                    .blur(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "مرحباً بك 👋",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "${getTodayName()} — لنحقق إنجازات اليوم",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "⭐ ${getMotivationalQuote()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// =============== Animated Stats Grid ===============
@Composable
private fun AnimatedStatsGrid(
    visible: Boolean,
    fileCount: Int,
    totalSize: Long,
    pendingTasks: Int,
    upcomingExams: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ParallaxStatCard(
                visible = visible,
                delay = 200,
                icon = Icons.Filled.Description,
                number = "$fileCount",
                label = "ملف",
                color = Primary,
                modifier = Modifier.weight(1f)
            )
            
            ParallaxStatCard(
                visible = visible,
                delay = 280,
                icon = Icons.Filled.Storage,
                number = "${totalSize / (1024 * 1024)}MB",
                label = "الحجم الكلي",
                color = Secondary,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ParallaxStatCard(
                visible = visible,
                delay = 360,
                icon = Icons.Filled.CheckCircle,
                number = "$pendingTasks",
                label = "مهام معلقة",
                color = Warning,
                modifier = Modifier.weight(1f),
                pulse = pendingTasks > 0
            )
            
            ParallaxStatCard(
                visible = visible,
                delay = 440,
                icon = Icons.Filled.School,
                number = "$upcomingExams",
                label = "امتحان قادم",
                color = Danger,
                modifier = Modifier.weight(1f),
                pulse = upcomingExams > 0
            )
        }
    }
}

@Composable
private fun ParallaxStatCard(
    visible: Boolean,
    delay: Int,
    icon: ImageVector,
    number: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    pulse: Boolean = false
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
            visibilityThreshold = 0.01f
        ).also { kotlinx.coroutines.delay(delay.toLong()) },
        label = "statScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = delay),
        label = "statAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "statPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (pulse) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = modifier
            .scale(scale * pulseScale)
            .alpha(alpha),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (isDark) 0.16f else 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                number,
                style = MaterialTheme.typography.headlineLarge,
                color = color,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =============== Glassmorphic Lecture Card ===============
@Composable
private fun GlassmorphicLectureCard(
    visible: Boolean,
    lectureCount: Int,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ).also { kotlinx.coroutines.delay(520) },
        label = "glassScale"
    )

    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "pressScale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale * pressScale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Primary, Primary.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("📅", fontSize = 32.sp)
            }
            
            Spacer(Modifier.width(18.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "محاضرات اليوم",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$lectureCount محاضرة مجدولة",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// =============== Staggered Quick Actions ===============
@Composable
private fun StaggeredQuickActions(visible: Boolean, navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FloatingSectionHeader(
            visible = visible,
            title = "إجراءات سريعة",
            icon = "⚡",
            color = Warning
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StaggeredActionCard(
                visible = visible,
                delay = 600,
                icon = Icons.Filled.Folder,
                label = "الملفات",
                emoji = "📁",
                color = Primary,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.Files.route) }
            
            StaggeredActionCard(
                visible = visible,
                delay = 680,
                icon = Icons.Filled.CheckCircle,
                label = "المهام",
                emoji = "✅",
                color = Secondary,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.Unified.tab(0)) }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StaggeredActionCard(
                visible = visible,
                delay = 760,
                icon = Icons.Filled.Note,
                label = "ملاحظاتي",
                emoji = "📝",
                color = Info,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.Unified.tab(2)) }
            
            StaggeredActionCard(
                visible = visible,
                delay = 840,
                icon = Icons.Filled.Star,
                label = "المجرة",
                emoji = "🌟",
                color = Warning,
                modifier = Modifier.weight(1f)
            ) { navController.navigate(Routes.Galaxy.route) }
        }
    }
}

@Composable
private fun StaggeredActionCard(
    visible: Boolean,
    delay: Int,
    icon: ImageVector,
    label: String,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    val slideOffset by animateIntAsState(
        targetValue = if (visible) 0 else 100,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ).also { kotlinx.coroutines.delay(delay.toLong()) },
        label = "actionSlide"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = delay),
        label = "actionAlpha"
    )

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "actionPress"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(120)
            isPressed = false
        }
    }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                translationY = slideOffset.toFloat()
            }
            .scale(scale)
            .alpha(alpha)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (isDark) 0.13f else 0.09f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(emoji, fontSize = 42.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// =============== Floating Section Header ===============
@Composable
private fun FloatingSectionHeader(
    visible: Boolean,
    title: String,
    icon: String,
    color: Color
) {
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else -20f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ).also { kotlinx.coroutines.delay(650) },
        label = "headerFloat"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(500, delayMillis = 650),
        label = "headerAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = offsetY }
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(icon, fontSize = 24.sp)
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// =============== Activity Cards ===============
@Composable
private fun SlideInActivityCard(
    visible: Boolean,
    delay: Int,
    icon: String,
    title: String,
    subtitle: String,
    color: Color,
    accentEmoji: String
) {
    val slideOffset by animateIntAsState(
        targetValue = if (visible) 0 else 150,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ).also { kotlinx.coroutines.delay(delay.toLong()) },
        label = "activitySlide"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, delayMillis = delay),
        label = "activityAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = slideOffset.toFloat() }
            .alpha(alpha),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 26.sp)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(accentEmoji, fontSize = 22.sp)
        }
    }
}

@Composable
private fun BounceInTaskCard(
    visible: Boolean,
    delay: Int,
    title: String,
    priority: String,
    dueDate: String
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ).also { kotlinx.coroutines.delay(delay.toLong()) },
        label = "taskBounce"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300, delayMillis = delay),
        label = "taskAlpha"
    )

    val color = when (priority) {
        "high" -> Danger
        "medium" -> Warning
        else -> Info
    }

    val priorityText = when (priority) {
        "high" -> "🔴 أولوية عالية"
        "medium" -> "🟡 أولوية متوسطة"
        else -> "🟢 أولوية منخفضة"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text("✅", fontSize = 26.sp)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    priorityText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
                if (dueDate.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "📅 $dueDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =============== Floating Empty State ===============
@Composable
private fun FloatingEmptyState(visible: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "emptyFloat")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emptyFloatY"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ).also { kotlinx.coroutines.delay(750) },
        label = "emptyScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer { translationY = offsetY },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Info.copy(alpha = 0.10f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("✨", fontSize = 52.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "لا يوجد نشاط قادم",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "أضف مهام أو امتحانات لتظهر هنا",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =============== Helper Functions ===============
private fun getTodayName(): String {
    val days = listOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
    return days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]
}

private fun getMotivationalQuote(): String {
    val quotes = listOf(
        "النجاح رحلة وليس وجهة",
        "كل يوم فرصة جديدة",
        "التعلم هو الاستثمار الأفضل",
        "المثابرة طريق النجاح",
        "ابدأ من حيث أنت"
    )
    return quotes.random()
}
