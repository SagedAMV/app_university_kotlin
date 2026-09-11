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

            // Overview title
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 100) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Primary)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "نظرة عامة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
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

            // Hero Card - Lectures
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 350) {
                    HeroBentoCard(
                        icon = "📅",
                        title = "محاضرات اليوم",
                        subtitle = "$lectureCount محاضرة مجدولة",
                        color = Primary,
                        onClick = { navController.navigate("unified") }
                    )
                }
            }

            // Quick Actions
            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 450) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Warning)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "إجراءات سريعة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        Text("⚡", fontSize = 20.sp)
                    }
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
                        ) { navController.navigate("files") }

                        QuickAction(
                            icon = Icons.Filled.CheckCircle,
                            label = "المهام",
                            color = Secondary,
                            modifier = Modifier.weight(1f)
                        ) { navController.navigate("unified") }
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
                        ) { navController.navigate("unified") }

                        QuickAction(
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Info)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "النشاط الأخير",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                AnimatedEntrance(visible = screenVisible, delayMillis = 750) {
                    ActivityCard(
                        icon = "📥",
                        title = "تم رفع ملف جديد",
                        time = "منذ 5 دقائق",
                        color = Primary
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
private fun StatCard(
    icon: ImageVector,
    number: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (isDark) 0.15f else 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                number,
                style = MaterialTheme.typography.headlineLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                Icons.Filled.ArrowForward,
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
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
