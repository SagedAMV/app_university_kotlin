package com.unimanager.app.ui.schedule

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unimanager.app.data.entity.LectureEntity
import com.unimanager.app.ui.components.*
import com.unimanager.app.ui.theme.*
import com.unimanager.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: AppViewModel) {
    val lectures by viewModel.allLectures.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var screenVisible by remember { mutableStateOf(false) }
    val days = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    LaunchedEffect(Unit) { screenVisible = true }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                    Text("جدول المحاضرات", fontWeight = FontWeight.Bold)
                }
            })
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
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة محاضرة")
            }
        }
    ) { padding ->
        if (lectures.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📅", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("لا توجد محاضرات بعد", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                days.forEach { day ->
                    val dayLectures = lectures.filter { it.day == day }
                    if (dayLectures.isNotEmpty()) {
                        item {
                            AnimatedEntrance(visible = screenVisible) {
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
                                    Text(day, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Primary)
                                }
                            }
                        }
                        items(dayLectures) { lecture ->
                            SlideUpEntrance(visible = screenVisible) {
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

    if (showAddDialog) {
        AddLectureDialog(
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Column(
                modifier = Modifier.padding(end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    lecture.timeFrom,
                    style = MaterialTheme.typography.titleMedium,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
                if (lecture.timeTo.isNotBlank()) {
                    Text(lecture.timeTo, style = MaterialTheme.typography.bodySmall)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    lecture.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (lecture.doctor.isNotBlank()) {
                    Text(lecture.doctor, style = MaterialTheme.typography.bodySmall)
                }
                if (lecture.room.isNotBlank()) {
                    Text(" ${lecture.room}", style = MaterialTheme.typography.bodySmall)
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
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("المادة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("الدكتور") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    days.forEach { d ->
                        FilterChip(
                            selected = day == d,
                            onClick = { day = d },
                            label = { Text(d.take(3)) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = timeFrom,
                        onValueChange = { timeFrom = it },
                        label = { Text("من") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = timeTo,
                        onValueChange = { timeTo = it },
                        label = { Text("إلى") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("القاعة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                enabled = subject.isNotBlank() && timeFrom.isNotBlank(),
                onClick = { onAdd(subject, doctor, day, timeFrom, timeTo, room) },
                shape = RoundedCornerShape(12.dp)
            ) { Text("إضافة") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") }
        }
    )
}
