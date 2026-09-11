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
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.unimanager.app.data.entity.ExamEntity
import com.unimanager.app.data.entity.LectureEntity
import com.unimanager.app.data.entity.NoteEntity
import com.unimanager.app.data.entity.TaskEntity
import com.unimanager.app.ui.components.*
import com.unimanager.app.ui.navigation.Routes
import com.unimanager.app.ui.theme.*
import com.unimanager.app.util.Validation
import com.unimanager.app.viewmodel.AppViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedScreen(viewModel: AppViewModel, navController: NavController) {
    var showAddDialog by remember { mutableStateOf(false) }
    var screenVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // رقم التبويب الابتدائي القادم من اختصارات الشاشة الرئيسية
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val initialTab = navBackStackEntry?.arguments?.getInt(Routes.Unified.TAB_ARG, 0) ?: 0
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }

    LaunchedEffect(Unit) { screenVisible = true }

    // رسائل الخطأ/النجاح القادمة من الـ ViewModel
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    val tabs = listOf(
        TabData("المهام", Icons.Filled.CheckCircle, Success),
        TabData("الجدول", Icons.Filled.CalendarToday, Primary),
        TabData("ملاحظاتي", Icons.Filled.Note, Info),
        TabData("الامتحانات", Icons.Filled.School, Warning)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    containerColor = MaterialTheme.colorScheme.background
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

            // Tab content — يجب أن يملأ باقي المساحة المتبقية أسفل التبويبات
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> TasksTab(
                        viewModel = viewModel,
                        searchQuery = searchQuery,
                        onAdd = { showAddDialog = true },
                        onClearSearch = { searchQuery = "" }
                    )
                    1 -> ScheduleTab(
                        viewModel = viewModel,
                        searchQuery = searchQuery,
                        onAdd = { showAddDialog = true }
                    )
                    2 -> NotesTab(
                        viewModel = viewModel,
                        searchQuery = searchQuery,
                        onAdd = { showAddDialog = true }
                    )
                    3 -> ExamsTab(
                        viewModel = viewModel,
                        searchQuery = searchQuery,
                        onAdd = { showAddDialog = true }
                    )
                }
            }
        }
    }

    // Add Dialogs (إنشاء جديد)
    if (showAddDialog) {
        when (selectedTab) {
            0 -> AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onSubmit = { title, desc, priority ->
                    viewModel.insertTask(TaskEntity(title = title, description = desc, priority = priority))
                    showAddDialog = false
                }
            )
            1 -> AddLectureDialog(
                onDismiss = { showAddDialog = false },
                onSubmit = { subject, doctor, day, timeFrom, timeTo, room ->
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
                onSubmit = { title, content ->
                    viewModel.insertNote(NoteEntity(title = title, content = content))
                    showAddDialog = false
                }
            )
            3 -> AddExamDialog(
                onDismiss = { showAddDialog = false },
                onSubmit = { subject, type, date, time, room ->
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
            else MaterialTheme.colorScheme.surfaceVariant
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
fun TasksTab(
    viewModel: AppViewModel,
    searchQuery: String,
    onAdd: () -> Unit,
    onClearSearch: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()
    var editing by remember { mutableStateOf<TaskEntity?>(null) }
    var deleting by remember { mutableStateOf<TaskEntity?>(null) }

    val filteredTasks = if (searchQuery.isBlank()) tasks
    else tasks.filter { it.title.contains(searchQuery, ignoreCase = true) }

    if (filteredTasks.isEmpty()) {
        if (searchQuery.isNotBlank()) {
            EmptyStateEnhanced(
                icon = "🔍",
                title = "لا توجد نتائج",
                subtitle = "جرب كلمات أخرى",
                actionText = "مسح البحث",
                onActionClick = onClearSearch
            )
        } else {
            EmptyStateEnhanced(
                icon = "✅",
                title = "لا توجد مهام",
                subtitle = "اضغط + لإضافة مهمة",
                actionText = "إضافة مهمة",
                onActionClick = onAdd
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
                    onSwipe = { deleting = task }
                ) {
                    TaskItem(
                        task = task,
                        onToggle = { viewModel.toggleTaskDone(task.id, !task.isDone) },
                        onEdit = { editing = task },
                        onDelete = { deleting = task }
                    )
                }
            }
        }
    }

    editing?.let { task ->
        AddTaskDialog(
            initial = task,
            onDismiss = { editing = null },
            onSubmit = { title, desc, priority ->
                viewModel.updateTask(task.copy(title = title, description = desc, priority = priority))
                editing = null
            }
        )
    }
    deleting?.let { task ->
        ConfirmActionDialog(
            title = "حذف المهمة",
            message = "هل أنت متأكد من حذف المهمة «${task.title}»؟",
            icon = Icons.Filled.Delete,
            onConfirm = { viewModel.deleteTask(task); deleting = null },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
fun ScheduleTab(viewModel: AppViewModel, searchQuery: String, onAdd: () -> Unit) {
    val lectures by viewModel.allLectures.collectAsState()
    var editing by remember { mutableStateOf<LectureEntity?>(null) }
    var deleting by remember { mutableStateOf<LectureEntity?>(null) }

    val days = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    val filteredLectures = if (searchQuery.isBlank()) lectures
    else lectures.filter {
        it.subject.contains(searchQuery, ignoreCase = true) ||
        it.doctor.contains(searchQuery, ignoreCase = true)
    }

    if (filteredLectures.isEmpty()) {
        EmptyStateEnhanced(
            icon = if (searchQuery.isNotBlank()) "🔍" else "📅",
            title = if (searchQuery.isNotBlank()) "لا توجد نتائج" else "لا توجد محاضرات",
            subtitle = if (searchQuery.isNotBlank()) "جرب كلمات أخرى" else "اضغط + لإضافة محاضرة",
            actionText = if (searchQuery.isBlank()) "إضافة محاضرة" else null,
            onActionClick = if (searchQuery.isBlank()) onAdd else null
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
                            onSwipe = { deleting = lecture }
                        ) {
                            LectureItem(
                                lecture = lecture,
                                onEdit = { editing = lecture },
                                onDelete = { deleting = lecture }
                            )
                        }
                    }
                }
            }
        }
    }

    editing?.let { lecture ->
        AddLectureDialog(
            initial = lecture,
            onDismiss = { editing = null },
            onSubmit = { subject, doctor, day, timeFrom, timeTo, room ->
                viewModel.updateLecture(
                    lecture.copy(
                        subject = subject,
                        doctor = doctor,
                        day = day,
                        timeFrom = timeFrom,
                        timeTo = timeTo,
                        room = room
                    )
                )
                editing = null
            }
        )
    }
    deleting?.let { lecture ->
        ConfirmActionDialog(
            title = "حذف المحاضرة",
            message = "هل أنت متأكد من حذف محاضرة «${lecture.subject}»؟",
            icon = Icons.Filled.Delete,
            onConfirm = { viewModel.deleteLecture(lecture); deleting = null },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
fun NotesTab(viewModel: AppViewModel, searchQuery: String, onAdd: () -> Unit) {
    val notes by viewModel.allNotes.collectAsState()
    var editing by remember { mutableStateOf<NoteEntity?>(null) }
    var deleting by remember { mutableStateOf<NoteEntity?>(null) }

    val filteredNotes = if (searchQuery.isBlank()) notes
    else notes.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.content.contains(searchQuery, ignoreCase = true)
    }

    if (filteredNotes.isEmpty()) {
        EmptyStateEnhanced(
            icon = if (searchQuery.isNotBlank()) "🔍" else "📝",
            title = if (searchQuery.isNotBlank()) "لا توجد نتائج" else "لا توجد ملاحظات",
            subtitle = if (searchQuery.isNotBlank()) "جرب كلمات أخرى" else "اضغط + لإضافة ملاحظة",
            actionText = if (searchQuery.isBlank()) "إضافة ملاحظة" else null,
            onActionClick = if (searchQuery.isBlank()) onAdd else null
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredNotes, key = { it.id }) { note ->
                SwipeableItem(
                    onSwipe = { deleting = note }
                ) {
                    NoteItem(
                        note = note,
                        onEdit = { editing = note },
                        onDelete = { deleting = note }
                    )
                }
            }
        }
    }

    editing?.let { note ->
        AddNoteDialog(
            initial = note,
            onDismiss = { editing = null },
            onSubmit = { title, content ->
                viewModel.updateNote(
                    note.copy(title = title, content = content, updatedAt = System.currentTimeMillis())
                )
                editing = null
            }
        )
    }
    deleting?.let { note ->
        ConfirmActionDialog(
            title = "حذف الملاحظة",
            message = "هل أنت متأكد من حذف ملاحظة «${note.title}»؟",
            icon = Icons.Filled.Delete,
            onConfirm = { viewModel.deleteNote(note); deleting = null },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
fun ExamsTab(viewModel: AppViewModel, searchQuery: String, onAdd: () -> Unit) {
    val exams by viewModel.allExams.collectAsState()
    var editing by remember { mutableStateOf<ExamEntity?>(null) }
    var deleting by remember { mutableStateOf<ExamEntity?>(null) }

    val filteredExams = if (searchQuery.isBlank()) exams
    else exams.filter { it.subject.contains(searchQuery, ignoreCase = true) }

    if (filteredExams.isEmpty()) {
        EmptyStateEnhanced(
            icon = if (searchQuery.isNotBlank()) "🔍" else "🎓",
            title = if (searchQuery.isNotBlank()) "لا توجد نتائج" else "لا توجد امتحانات",
            subtitle = if (searchQuery.isNotBlank()) "جرب كلمات أخرى" else "اضغط + لإضافة امتحان",
            actionText = if (searchQuery.isBlank()) "إضافة امتحان" else null,
            onActionClick = if (searchQuery.isBlank()) onAdd else null
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredExams) { exam ->
                SwipeableItem(
                    onSwipe = { deleting = exam }
                ) {
                    ExamItem(
                        exam = exam,
                        onEdit = { editing = exam },
                        onDelete = { deleting = exam }
                    )
                }
            }
        }
    }

    editing?.let { exam ->
        AddExamDialog(
            initial = exam,
            onDismiss = { editing = null },
            onSubmit = { subject, type, date, time, room ->
                viewModel.updateExam(exam.copy(subject = subject, type = type, examDate = date, time = time, room = room))
                editing = null
            }
        )
    }
    deleting?.let { exam ->
        ConfirmActionDialog(
            title = "حذف الامتحان",
            message = "هل أنت متأكد من حذف امتحان «${exam.subject}»؟",
            icon = Icons.Filled.Delete,
            onConfirm = { viewModel.deleteExam(exam); deleting = null },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
fun TaskItem(task: TaskEntity, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
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

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "تعديل",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
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
fun LectureItem(lecture: LectureEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
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

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "تعديل",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
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
fun NoteItem(note: NoteEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val colors = listOf(Info, Secondary, Warning, Primary)
    val noteColor = colors[note.title.hashCode().mod(colors.size)]

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
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
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "تعديل",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
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
fun ExamItem(exam: ExamEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    val daysUntil = try {
        if (exam.examDate.isBlank()) 999
        else {
            val date = LocalDate.parse(exam.examDate)
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
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}

// ====== Dialogs (تُستخدم للإنشاء initial=null وللتعديل initial=الكيان) ======

@Composable
fun AddTaskDialog(
    initial: TaskEntity? = null,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var priority by remember { mutableStateOf(initial?.priority ?: "medium") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(if (initial == null) "مهمة جديدة" else "تعديل المهمة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("العنوان") },
                    isError = error != null,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف") }, shape = RoundedCornerShape(12.dp), maxLines = 3)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("🔴 عالية" to "high", "🟡 متوسطة" to "medium", "🟢 منخفضة" to "low").forEach { (label, value) ->
                        FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(label) }, shape = RoundedCornerShape(12.dp))
                    }
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    val nameResult = Validation.validateName(title)
                    val priorityResult = Validation.validatePriority(priority)
                    val failure = nameResult.exceptionOrNull()?.message
                        ?: priorityResult.exceptionOrNull()?.message
                    if (failure != null) {
                        error = failure
                    } else {
                        onSubmit(
                            Validation.sanitizeInput(title),
                            Validation.sanitizeText(desc),
                            priorityResult.getOrThrow()
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (initial == null) "إضافة" else "حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") } }
    )
}

@Composable
fun AddLectureDialog(
    initial: LectureEntity? = null,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, String) -> Unit
) {
    var subject by remember { mutableStateOf(initial?.subject ?: "") }
    var doctor by remember { mutableStateOf(initial?.doctor ?: "") }
    var day by remember { mutableStateOf(initial?.day ?: "السبت") }
    var timeFrom by remember { mutableStateOf(initial?.timeFrom ?: "") }
    var timeTo by remember { mutableStateOf(initial?.timeTo ?: "") }
    var room by remember { mutableStateOf(initial?.room ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    val days = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(if (initial == null) "محاضرة جديدة" else "تعديل المحاضرة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it; error = null },
                    label = { Text("المادة") },
                    isError = error != null,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(value = doctor, onValueChange = { doctor = it }, label = { Text("الدكتور") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    days.forEach { d -> FilterChip(selected = day == d, onClick = { day = d }, label = { Text(d.take(3)) }, shape = RoundedCornerShape(12.dp)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = timeFrom,
                        onValueChange = { timeFrom = it; error = null },
                        label = { Text("من") },
                        isError = error != null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(value = timeTo, onValueChange = { timeTo = it; error = null }, label = { Text("إلى (اختياري)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                }
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("القاعة") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = subject.isNotBlank() && timeFrom.isNotBlank(),
                onClick = {
                    val subjectResult = Validation.validateName(subject)
                    val fromResult = Validation.validateTime(timeFrom)
                    val toResult = if (timeTo.isBlank()) Result.success(timeTo) else Validation.validateTime(timeTo)
                    val failure = subjectResult.exceptionOrNull()?.message
                        ?: fromResult.exceptionOrNull()?.message
                        ?: toResult.exceptionOrNull()?.message
                    if (failure != null) {
                        error = failure
                    } else {
                        onSubmit(
                            Validation.sanitizeInput(subject),
                            Validation.sanitizeText(doctor),
                            day,
                            fromResult.getOrThrow(),
                            toResult.getOrThrow(),
                            Validation.sanitizeText(room)
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (initial == null) "إضافة" else "حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") } }
    )
}

@Composable
fun AddNoteDialog(
    initial: NoteEntity? = null,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(if (initial == null) "ملاحظة جديدة" else "تعديل الملاحظة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("العنوان") },
                    isError = error != null,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it; error = null },
                    label = { Text("المحتوى") },
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    val titleResult = Validation.validateName(title)
                    val failure = titleResult.exceptionOrNull()?.message
                        ?: if (content.isBlank()) "المحتوى مطلوب" else null
                    if (failure != null) {
                        error = failure
                    } else {
                        onSubmit(Validation.sanitizeInput(title), Validation.sanitizeText(content))
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (initial == null) "حفظ" else "حفظ التعديل") }
        },
        dismissButton = { TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") } }
    )
}

@Composable
fun AddExamDialog(
    initial: ExamEntity? = null,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit
) {
    var subject by remember { mutableStateOf(initial?.subject ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "نصفي") }
    var date by remember { mutableStateOf(initial?.examDate ?: "") }
    var time by remember { mutableStateOf(initial?.time ?: "") }
    var room by remember { mutableStateOf(initial?.room ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(if (initial == null) "امتحان جديد" else "تعديل الامتحان", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it; error = null },
                    label = { Text("المادة") },
                    isError = error != null,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("نصفي", "نهائي", "فجائي", "عملي").forEach { t -> FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) }, shape = RoundedCornerShape(12.dp)) }
                }
                com.unimanager.app.ui.components.DatePickerField(
                    value = date,
                    onValueChange = { date = it; error = null },
                    label = "تاريخ الامتحان"
                )
                com.unimanager.app.ui.components.TimePickerField(
                    value = time,
                    onValueChange = { time = it; error = null },
                    label = "وقت الامتحان (اختياري)"
                )
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("القاعة") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = subject.isNotBlank() && date.isNotBlank(),
                onClick = {
                    val subjectResult = Validation.validateName(subject)
                    val dateResult = Validation.validateDate(date)
                    val timeResult = if (time.isBlank()) Result.success(time) else Validation.validateTime(time)
                    val typeResult = Validation.validateExamType(type)
                    val failure = subjectResult.exceptionOrNull()?.message
                        ?: dateResult.exceptionOrNull()?.message
                        ?: timeResult.exceptionOrNull()?.message
                        ?: typeResult.exceptionOrNull()?.message
                    if (failure != null) {
                        error = failure
                    } else {
                        onSubmit(
                            Validation.sanitizeInput(subject),
                            typeResult.getOrThrow(),
                            dateResult.getOrThrow(),
                            timeResult.getOrThrow(),
                            Validation.sanitizeText(room)
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (initial == null) "إضافة" else "حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") } }
    )
}

data class TabData(
    val label: String,
    val icon: ImageVector,
    val color: Color
)
