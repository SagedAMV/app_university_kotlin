package com.unimanager.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unimanager.app.ui.components.*
import com.unimanager.app.ui.navigation.Routes
import com.unimanager.app.ui.theme.*
import com.unimanager.app.viewmodel.AppViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AppViewModel, navController: NavController) {
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

    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }

    // Show success messages
    LaunchedEffect(successMessage) {
        successMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }

    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { screenVisible = true }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏠", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            "الرئيسية",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.Settings.route) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "الإعدادات")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Welcome Card
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 0) {
                    WelcomeCard()
                }
            }

            // Overview title
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 100) {
                    SectionTitle("نظرة عامة", color = Primary)
                }
            }

            // Stats - Row 1
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 150) {
                    BentoRow {
                        StatCard(
                            icon = Icons.Filled.Description,
                            number = "$fileCount",
                            label = "ملف",
                            color = Primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Filled.Folder,
                            number = "${totalSize / (1024 * 1024)}MB",
                            label = "الحجم",
                            color = Secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Stats - Row 2
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 250) {
                    BentoRow {
                        StatCard(
                            icon = Icons.Filled.CheckCircle,
                            number = "$pendingTasks",
                            label = "مهام معلقة",
                            color = Warning,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            icon = Icons.Filled.School,
                            number = "$upcomingExams",
                            label = "امتحان قادم",
                            color = Danger,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Hero Card
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 350) {
                    HeroBentoCard(
                        icon = "📅",
                        title = "محاضرات اليوم",
                        subtitle = "$lectureCount محاضرة مجدولة",
                        color = Primary,
                        onClick = { navController.navigate(Routes.Unified.tab(1)) }
                    )
                }
            }

            // Quick Actions
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 450) {
                    SectionTitle("إجراءات سريعة", color = Warning, icon = "⚡")
                }
            }

            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 500) {
                    BentoRow {
                        QuickAction(
                            icon = Icons.Filled.Folder,
                            label = "الملفات",
                            color = Primary,
                            modifier = Modifier.weight(1f)
                        ) { navController.navigate(Routes.Files.route) }

                        QuickAction(
                            icon = Icons.Filled.CheckCircle,
                            label = "المهام",
                            color = Secondary,
                            modifier = Modifier.weight(1f)
                        ) { navController.navigate(Routes.Unified.tab(0)) }
                    }
                }
            }

            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 600) {
                    BentoRow {
                        QuickAction(
                            icon = Icons.Filled.Note,
                            label = "ملاحظاتي",
                            color = Info,
                            modifier = Modifier.weight(1f)
                        ) { navController.navigate(Routes.Unified.tab(2)) }

                        QuickAction(
                            icon = Icons.Filled.Star,
                            label = "المجرة",
                            color = Warning,
                            modifier = Modifier.weight(1f)
                        ) { navController.navigate(Routes.Galaxy.route) }
                    }
                }
            }

            // Recent Activity section
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 700) {
                    SectionTitle("النشاط القادم", color = Info)
                }
            }

            // Upcoming exams
            if (upcomingExamList.isNotEmpty()) {
                items(upcomingExamList.take(3)) { exam ->
                    AnimatedEntrance(visible = screenVisible, delayMillis = 750) {
                        ActivityCard(
                            icon = "🎓",
                            title = exam.subject,
                            time = "${exam.type} • ${exam.examDate}",
                            color = if (exam.type == "نهائي") Danger else Info
                        )
                    }
                }
            }

            // Pending tasks
            if (pendingTaskList.isNotEmpty()) {
                items(pendingTaskList.take(3)) { task ->
                    AnimatedEntrance(visible = screenVisible, delayMillis = 800) {
                        ActivityCard(
                            icon = "✅",
                            title = task.title,
                            time = when (task.priority) {
                                "high" -> "أولوية عالية 🔴"
                                "medium" -> "أولوية متوسطة 🟡"
                                else -> "أولوية منخفضة"
                            },
                            color = when (task.priority) {
                                "high" -> Danger
                                "medium" -> Warning
                                else -> Info
                            }
                        )
                    }
                }
            }

            // Empty state
            if (upcomingExamList.isEmpty() && pendingTaskList.isEmpty()) {
                item {
                    AnimatedEntrance(visible = screenVisible, delayMillis = 750) {
                        ActivityCard(
                            icon = "✨",
                            title = "لا يوجد نشاط قادم",
                            time = "أضف مهام أو امتحانات لتظهر هنا",
                            color = Info
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, color: Color, icon: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (icon != null) "$icon $title" else title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WelcomeCard() {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val colors = if (isDark) {
        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFA855F7))
    } else {
        listOf(Color(0xFF6366F1), Color(0xFF7C3AED), Color(0xFF8B5CF6))
    }

    // Animated shimmer effect
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(32.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = colors,
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        ) {
            // Animated decorative circles
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f * shimmerAlpha))
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-20).dp, y = 30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f * shimmerAlpha))
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (-10).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "👋",
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "مرحباً بك",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(10.dp))
                val todayName = getTodayName()
                Text(
                    "$todayName — يوم مثالي للتعلم ✨",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    number: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    // Pulse animation for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "statPulse")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (isDark) 0.18f else 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(iconScale)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                number,
                style = MaterialTheme.typography.headlineLarge,
                color = color,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun HeroBentoCard(
    icon: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isPressed = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed.value) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )

    // Reset isPressed after animation
    LaunchedEffect(isPressed.value) {
        if (isPressed.value) {
            kotlinx.coroutines.delay(150)
            isPressed.value = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = {
                    isPressed.value = true
                    onClick()
                }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (isDark) 0.15f else 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 28.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isPressed = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed.value) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )

    // Reset isPressed after animation
    LaunchedEffect(isPressed.value) {
        if (isPressed.value) {
            kotlinx.coroutines.delay(150)
            isPressed.value = false
        }
    }

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                onClick = {
                    isPressed.value = true
                    onClick()
                }
            )
            .aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (isDark) 0.12f else 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ActivityCard(
    icon: String,
    title: String,
    time: String,
    color: Color
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    time, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

fun getTodayName(): String {
    val days = listOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
    return days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]
}
