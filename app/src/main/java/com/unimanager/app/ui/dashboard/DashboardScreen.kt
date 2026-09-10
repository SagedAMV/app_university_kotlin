package com.unimanager.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unimanager.app.ui.components.*
import com.unimanager.app.ui.theme.*
import com.unimanager.app.viewmodel.AppViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AppViewModel, navController: NavController) {
    val fileCount by viewModel.fileCount.collectAsState(initial = 0)
    val lectureCount by viewModel.lectureCount.collectAsState(initial = 0)
    val pendingTasks by viewModel.pendingTaskCount.collectAsState(initial = 0)
    val upcomingExams by viewModel.upcomingExamCount.collectAsState(initial = 0)
    val totalSize by viewModel.totalSize.collectAsState(initial = 0L)

    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { screenVisible = true }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏠 الرئيسية", style = MaterialTheme.typography.headlineMedium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { /* TODO: Backup */ }) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = "نسخ احتياطي")
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
            // Welcome Card with gradient
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 0) {
                    WelcomeCard()
                }
            }

            // Bento Grid - Stats
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 100) {
                    Text(
                        "نظرة عامة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 150) {
                    BentoRow {
                        GlassStatCard(
                            icon = "📄",
                            number = "$fileCount",
                            label = "ملف",
                            accentColor = Primary,
                            modifier = Modifier.weight(1f)
                        )
                        GlassStatCard(
                            icon = "📁",
                            number = "${totalSize / (1024 * 1024)}MB",
                            label = "الحجم",
                            accentColor = Secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 250) {
                    BentoRow {
                        GlassStatCard(
                            icon = "✅",
                            number = "$pendingTasks",
                            label = "مهمةPending",
                            accentColor = Warning,
                            modifier = Modifier.weight(1f)
                        )
                        GlassStatCard(
                            icon = "🎓",
                            number = "$upcomingExams",
                            label = "امتحان قادم",
                            accentColor = Danger,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Hero Bento Card - Lectures
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 350) {
                    HeroBentoCard(
                        icon = "📅",
                        title = "محاضرات اليوم",
                        subtitle = "$lectureCount محاضرة مجدولة",
                        accentColor = Primary,
                        onClick = { navController.navigate("schedule") }
                    )
                }
            }

            // Quick Actions
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 450) {
                    Text(
                        "⚡ إجراءات سريعة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 500) {
                    BentoRow {
                        QuickActionButton(
                            icon = Icons.Filled.Folder,
                            label = "الملفات",
                            color = Primary,
                            modifier = Modifier.weight(1f)
                        ) { navController.navigate("files") }

                        QuickActionButton(
                            icon = Icons.Filled.CheckCircle,
                            label = "المهام",
                            color = Secondary,
                            modifier = Modifier.weight(1f)
                        ) { navController.navigate("tasks") }
                    }
                }
            }

            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 600) {
                    BentoRow {
                        QuickActionButton(
                            icon = Icons.Filled.Note,
                            label = "ملاحظاتي",
                            color = Info,
                            modifier = Modifier.weight(1f)
                        ) { navController.navigate("notes") }

                        QuickActionButton(
                            icon = Icons.Filled.Star,
                            label = "المجرة",
                            color = Warning,
                            modifier = Modifier.weight(1f)
                        ) { navController.navigate("galaxy") }
                    }
                }
            }

            // Recent Activity
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 700) {
                    Text(
                        "النشاط الأخير",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 750) {
                    ActivityCard(
                        icon = "📥",
                        title = "تم رفع ملف جديد",
                        time = "منذ 5 دقائق",
                        accentColor = Primary
                    )
                }
            }
        }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(colors = colors)
                )
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-30).dp, y = 20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "📚 مرحباً بك",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                val todayName = getTodayName()
                Text(
                    "$todayName — يوم مثالي للتعلم",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun HeroBentoCard(
    icon: String,
    title: String,
    subtitle: String,
    accentColor: Color,
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
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = if (isDark) 0.15f else 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp)
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
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (isDark) 0.12f else 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ActivityCard(
    icon: String,
    title: String,
    time: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

fun getTodayName(): String {
    val days = listOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
    return days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]
}
