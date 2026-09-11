package com.unimanager.app.ui.unified

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.unimanager.app.ui.components.*
import com.unimanager.app.ui.theme.*
import com.unimanager.app.viewmodel.AppViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var screenVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { screenVisible = true }

    val tabs = listOf(
        TabData("المهام", Icons.Filled.CheckCircle, Success),
        TabData("الجدول", Icons.Filled.CalendarToday, Primary),
        TabData("ملاحظاتي", Icons.Filled.Note, Info),
        TabData("الامتحانات", Icons.Filled.School, Warning)
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
                ),
                actions = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "مسح")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            val fabScale by animateFloatAsState(
                targetValue = if (screenVisible) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "fabScale"
            )
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.scale(fabScale),
                shape = RoundedCornerShape(16.dp),
                containerColor = tabs[selectedTab].color
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة ${tabs[selectedTab].label}")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = { searchQuery = "" },
                placeholder = "بحث في ${tabs[selectedTab].label}...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

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
                        searchQuery = ""
                    }
                }
            }

            // Tab content
            when (selectedTab) {
                0 -> TasksTab(viewModel = viewModel, searchQuery = searchQuery)
                1 -> ScheduleTab(viewModel = viewModel, searchQuery = searchQuery)
                2 -> NotesTab(viewModel = viewModel, searchQuery = searchQuery)
                3 -> ExamsTab(viewModel = viewModel, searchQuery = searchQuery)
            }
        }
    }

    // Add Dialogs
    if (showAddDialog) {
        when (selectedTab) {
            0 -> AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, desc, priority ->
                    viewModel.insertTask(TaskEntity(title = title, description = desc, priority = priority))
                    showAddDialog = false
                }
            )
            1 -> AddLectureDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { subject, doctor, day, timeFrom, timeTo, room ->
                    viewModel.insertLecture(LectureEntity(
                        subject = subject,
                        doctor = doctor,
                        day = day,
                        timeFrom = timeFrom,
                        timeTo = timeTo,
                        room = room
                    ))
                    showAddDialog = false
                }
            )
            2 -> AddNoteDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, content ->
                    viewModel.insertNote(NoteEntity(title = title, content = content))
                    showAddDialog = false
                }
            )
            3 -> AddExamDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { subject, type, date, time, room ->
                    viewModel.insertExam(ExamEntity(
                        subject = subject,
                        type = type,
                        examDate = date,
                        time = time,
                        room = room
                    ))
                    showAddDialog = false
                }
            )
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
fun TasksTab(viewModel: AppViewModel, searchQuery: String) {
    val tasks by viewModel.allTasks.collectAsState()
    val filteredTasks = if (searchQuery.isBlank()) tasks
    else tasks.filter { it.title.contains(searchQuery, ignoreCase = true) }

    if (filteredTasks.isEmpty()) {
        if (searchQuery.isNotBlank()) {
            EmptyStateEnhanced(
                icon = "🔍",
                title = "لا توجد نتائج",
                subtitle = "جرب كلمات أخرى",
                actionText = "مسح البحث",
                onActionClick = { /* Clear search */ }
            )
        } else {
            EmptyStateEnhanced(
                icon = "✅",
                title = "لا توجد مهام",
                subtitle = "اضغط + لإضافة مهمة",
                actionText = "إضافة مهمة",
                onActionClick = { /* TODO */ }
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredTasks, key = { it.id }) { task ->
                SwipeableItem(
                    onSwipe = { viewModel.deleteTask(task) }
                ) {
                    TaskItem(
                        task = task,
                        onToggle = { viewModel.toggleTaskDone(task.id, !task.isDone) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleTab(viewModel: AppViewModel, searchQuery: String) {
    val lectures by viewModel.allLectures.collectAsState()
    val days = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    val filteredLectures = if (searchQuery.isBlank()) lectures
    else lectures.filter {
        it.subject.contains(searchQuery, ignoreCase = true) ||
        it.doctor.contains(searchQuery, ignoreCase = true)
    }

    if (filteredLectures.isEmpty()) {
        EmptyStateEnhanced(
            icon = "🔍",
            title = if (searchQuery.isNotBlank()) "لا توجد نتائج" else "لا توجد محاضرات",
            subtitle = if (searchQuery.isNotBlank()) "جرب كلمات أخرى" else "اضغط + لإضافة محاضرة",
            actionText = if (searchQuery.isBlank()) "إضافة محاضرة" else null,
            onActionClick = if (searchQuery.isBlank()) { /* TODO */ } else null
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            days.forEach { day ->
                val dayLectures = filteredLectures.filter { it.day == day }
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
                        SwipeableItem(
                            onSwipe = { viewModel.deleteLecture(lecture) }
                        ) {
                            LectureItem(
                                lecture = lecture,
                                onDelete = { viewModel.deleteLecture(lecture) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesTab(viewModel: AppViewModel, searchQuery: String) {
    val notes by viewModel.allNotes.collectAsState()
    val filteredNotes = if (searchQuery.isBlank()) notes
    else notes.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.content.contains(searchQuery, ignoreCase = true)
    }

    if (filteredNotes.isEmpty()) {
        EmptyStateEnhanced(
            icon = "📝",
            title = if (searchQuery.isNotBlank()) "لا توجد نتائج" else "لا توجد ملاحظات",
            subtitle = if (searchQuery.isNotBlank()) "جرب كلمات أخرى" else "اضغط + لإضافة ملاحظة",
            actionText = if (searchQuery.isBlank()) "إضافة ملاحظة" else null,
            onActionClick = if (searchQuery.isBlank()) { /* TODO */ } else null
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredNotes, key = { it.id }) { note ->
                SwipeableItem(
                    onSwipe = { viewModel.deleteNote(note) }
                ) {
                    NoteItem(
                        note = note,
                        onDelete = { viewModel.deleteNote(note) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExamsTab(viewModel: AppViewModel, searchQuery: String) {
    val exams by viewModel.allExams.collectAsState()
    val filteredExams = if (searchQuery.isBlank()) exams
    else exams.filter { it.subject.contains(searchQuery, ignoreCase = true) }

    if (filteredExams.isEmpty()) {
        EmptyStateEnhanced(
            icon = "🔍",
            title = if (searchQuery.isNotBlank()) "لا توجد نتائج" else "لا توجد امتحانات",
            subtitle = if (searchQuery.isNotBlank()) "جرب كلمات أخرى" else "اضغط + لإضافة امتحان",
            actionText = if (searchQuery.isBlank()) "إضافة امتحان" else null,
            onActionClick = if (searchQuery.isBlank()) { /* TODO */ } else null
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredExams) { exam ->
                SwipeableItem(
                    onSwipe = { viewModel.deleteExam(exam) }
                ) {
                    ExamItem(
                        exam = exam,
                        onDelete = { viewModel.deleteExam(exam) }
                    )
                }
            }
        }
    }
}

// ... (keep existing TaskItem, LectureItem, NoteItem, ExamItem, and Dialog composables)
@Composable
fun TaskItem(task: TaskEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
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

            Spacer(Modifier.width(4.dp))

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun LectureItem(lecture: LectureEntity, onDelete: () -> Unit) {
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

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun NoteItem(note: NoteEntity, onDelete: () -> Unit) {
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
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(noteColor)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
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
fun ExamItem(exam: ExamEntity, onDelete: () -> Unit) {
    val daysUntil = try {
        if (exam.examDate.isBlank()) 999
        else {
            val date = LocalDate.parse(exam.examDate)
            // استخدام ChronoUnit.DAYS.between للحصول على الفرق الكلي بالأيام
            java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date).toInt()
        }
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
                            daysUntil == 999 -> "غير محدد"
                            daysUntil < 0 -> "انتهى"
                            daysUntil == 0 -> "اليوم!"
                            daysUntil == 1 -> "غداً"
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

// Dialogs
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("مهمة جديدة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("العنوان") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف") }, shape = RoundedCornerShape(12.dp), maxLines = 3)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("🔴 عالية" to "high", "🟡 متوسطة" to "medium", " منخفضة" to "low").forEach { (label, value) ->
                        FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(label) }, shape = RoundedCornerShape(12.dp))
                    }
                }
            }
        },
        confirmButton = { Button(enabled = title.isNotBlank(), onClick = { onAdd(title, desc, priority) }, shape = RoundedCornerShape(12.dp)) { Text("إضافة") } },
        dismissButton = { TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") } }
    )
}

@Composable
fun AddLectureDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String, String) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("السبت") }
    var timeFrom by remember { mutableStateOf("") }
    var timeTo by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    val days = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("محاضرة جديدة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("المادة") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = doctor, onValueChange = { doctor = it }, label = { Text("الدكتور") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    days.forEach { d -> FilterChip(selected = day == d, onClick = { day = d }, label = { Text(d.take(3)) }, shape = RoundedCornerShape(12.dp)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = timeFrom, onValueChange = { timeFrom = it }, label = { Text("من") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                    OutlinedTextField(value = timeTo, onValueChange = { timeTo = it }, label = { Text("إلى") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                }
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("القاعة") }, shape = RoundedCornerShape(12.dp), singleLine = true)
            }
        },
        confirmButton = { Button(enabled = subject.isNotBlank() && timeFrom.isNotBlank(), onClick = { onAdd(subject, doctor, day, timeFrom, timeTo, room) }, shape = RoundedCornerShape(12.dp)) { Text("إضافة") } },
        dismissButton = { TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") } }
    )
}

@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("ملاحظة جديدة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("العنوان") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("المحتوى") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = { Button(enabled = title.isNotBlank(), onClick = { onAdd(title, content) }, shape = RoundedCornerShape(12.dp)) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") } }
    )
}

@Composable
fun AddExamDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("نصفي") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("امتحان جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("المادة") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("نصفي", "نهائي", "فجائي", "عملي").forEach { t -> FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) }, shape = RoundedCornerShape(12.dp)) }
                }
                // DatePicker و TimePicker
                com.unimanager.app.ui.components.DatePickerField(
                    value = date,
                    onValueChange = { date = it },
                    label = "تاريخ الامتحان"
                )
                com.unimanager.app.ui.components.TimePickerField(
                    value = time,
                    onValueChange = { time = it },
                    label = "وقت الامتحان"
                )
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("القاعة") }, shape = RoundedCornerShape(12.dp), singleLine = true)
            }
        },
        confirmButton = { Button(enabled = subject.isNotBlank() && date.isNotBlank(), onClick = { onAdd(subject, type, date, time, room) }, shape = RoundedCornerShape(12.dp)) { Text("إضافة") } },
        dismissButton = { TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") } }
    )
}

data class TabData(
    val label: String,
    val icon: ImageVector,
    val color: Color
)
