package com.unimanager.app.ui.unified

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unimanager.app.data.entity.ExamEntity
import com.unimanager.app.data.entity.LectureEntity
import com.unimanager.app.data.entity.NoteEntity
import com.unimanager.app.data.entity.TaskEntity
import com.unimanager.app.ui.components.SlideUpEntrance
import com.unimanager.app.ui.theme.*
import com.unimanager.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf<String?>(null) }

    val tabs = listOf(
        TabData("المهام", Icons.Filled.CheckCircle, Color(0xFF10B981)),
        TabData("الجدول", Icons.Filled.CalendarToday, Color(0xFF6366F1)),
        TabData("ملاحظاتي", Icons.Filled.Note, Color(0xFF3B82F6)),
        TabData("الامتحانات", Icons.Filled.School, Color(0xFFF59E0B))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        " الأقسام",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = tabs[selectedTab].name },
                shape = RoundedCornerShape(16.dp),
                containerColor = tabs[selectedTab].color
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    TabButton(
                        icon = tab.icon,
                        label = tab.label,
                        color = tab.color,
                        selected = selectedTab == index,
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedTab = index
                    }
                }
            }

            // Tab content
            when (selectedTab) {
                0 -> TasksTab(viewModel = viewModel, showAddDialog = showAddDialog)
                1 -> ScheduleTab(viewModel = viewModel, showAddDialog = showAddDialog)
                2 -> NotesTab(viewModel = viewModel, showAddDialog = showAddDialog)
                3 -> ExamsTab(viewModel = viewModel, showAddDialog = showAddDialog)
            }
        }
    }
}

@Composable
fun TabButton(
    icon: ImageVector,
    label: String,
    color: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tabScale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.2f)
            else if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TasksTab(viewModel: AppViewModel, showAddDialog: String?) {
    val tasks by viewModel.allTasks.collectAsState(initial = emptyList())

    if (tasks.isEmpty()) {
        EmptyState("✅", "لا توجد مهام", "اضغط + لإضافة مهمة")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskItem(task = task) {
                    viewModel.toggleTaskDone(task.id, !task.isDone)
                }
            }
        }
    }
}

@Composable
fun ScheduleTab(viewModel: AppViewModel, showAddDialog: String?) {
    val lectures by viewModel.allLectures.collectAsState(initial = emptyList())
    val days = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    if (lectures.isEmpty()) {
        EmptyState("📅", "لا توجد محاضرات", "اضغط + لإضافة محاضرة")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            days.forEach { day ->
                val dayLectures = lectures.filter { it.day == day }
                if (dayLectures.isNotEmpty()) {
                    item {
                        Text(
                            day,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                    items(dayLectures) { lecture ->
                        LectureItem(lecture)
                    }
                }
            }
        }
    }
}

@Composable
fun NotesTab(viewModel: AppViewModel, showAddDialog: String?) {
    val notes by viewModel.allNotes.collectAsState(initial = emptyList())

    if (notes.isEmpty()) {
        EmptyState("📝", "لا توجد ملاحظات", "اضغط + لإضافة ملاحظة")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notes, key = { it.id }) { note ->
                NoteItem(note)
            }
        }
    }
}

@Composable
fun ExamsTab(viewModel: AppViewModel, showAddDialog: String?) {
    val exams by viewModel.allExams.collectAsState(initial = emptyList())

    if (exams.isEmpty()) {
        EmptyState("🎓", "لا توجد امتحانات", "اضغط + لإضافة امتحان")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exams) { exam ->
                ExamItem(exam)
            }
        }
    }
}

@Composable
fun EmptyState(icon: String, title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TaskItem(task: TaskEntity, onToggle: () -> Unit) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val priorityColor = when (task.priority) {
        "high" -> Danger
        "medium" -> Warning
        else -> Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isDone) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (task.isDone) Success else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isDone) {
                        Icon(Icons.Filled.Check, contentDescription = "مكتمل", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotBlank()) {
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
            )
        }
    }
}

@Composable
fun LectureItem(lecture: LectureEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(end = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(lecture.timeFrom, style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)
                if (lecture.timeTo.isNotBlank()) {
                    Text(lecture.timeTo, style = MaterialTheme.typography.bodySmall)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(lecture.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (lecture.doctor.isNotBlank()) {
                    Text(lecture.doctor, style = MaterialTheme.typography.bodySmall)
                }
                if (lecture.room.isNotBlank()) {
                    Text("📍 ${lecture.room}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun NoteItem(note: NoteEntity) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val colors = listOf(Info, Secondary, Warning, Primary)
    val noteColor = colors[note.title.hashCode().mod(colors.size)]

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = noteColor.copy(alpha = if (isDark) 0.12f else 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                note.content.take(120) + if (note.content.length > 120) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 3
            )
        }
    }
}

@Composable
fun ExamItem(exam: ExamEntity) {
    val daysUntil = try {
        val date = java.time.LocalDate.parse(exam.examDate)
        java.time.Period.between(java.time.LocalDate.now(), date).days
    } catch (e: Exception) { 999 }

    val color = when {
        daysUntil < 0 -> Color.Gray
        daysUntil <= 3 -> Danger
        daysUntil <= 7 -> Warning
        else -> Primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(exam.subject, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.15f))
                ) {
                    Text(
                        when {
                            daysUntil < 0 -> "انتهى"
                            daysUntil == 0 -> "اليوم!"
                            else -> "$daysUntil يوم"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("${exam.type} • ${exam.examDate}", style = MaterialTheme.typography.bodyMedium)
            if (exam.time.isNotBlank()) Text("⏰ ${exam.time}", style = MaterialTheme.typography.bodySmall)
            if (exam.room.isNotBlank()) Text("📍 ${exam.room}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

data class TabData(
    val label: String,
    val icon: ImageVector,
    val color: Color
)
