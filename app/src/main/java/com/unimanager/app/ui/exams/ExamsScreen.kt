package com.unimanager.app.ui.exams

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unimanager.app.data.entity.ExamEntity
import com.unimanager.app.ui.components.*
import com.unimanager.app.ui.theme.*
import com.unimanager.app.viewmodel.AppViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(viewModel: AppViewModel) {
    val exams by viewModel.allExams.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var screenVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { screenVisible = true }

    Scaffold(
        topBar = { TopAppBar(title = { Text("🎓 الامتحانات", fontWeight = FontWeight.Bold) }) },
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
                Icon(Icons.Filled.Add, contentDescription = "إضافة امتحان")
            }
        }
    ) { padding ->
        if (exams.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎓", style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(16.dp))
                    Text("لا توجد امتحانات بعد", style = MaterialTheme.typography.titleLarge)
                    Text("اضغط + لإضافة امتحان")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(exams) { exam ->
                    SlideUpEntrance(visible = screenVisible) {
                        ExamItem(exam = exam, onDelete = { viewModel.deleteExam(exam) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExamDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { subject, type, date, time, room ->
                viewModel.insertExam(ExamEntity(subject = subject, type = type, examDate = date, time = time, room = room))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExamItem(exam: ExamEntity, onDelete: () -> Unit) {
    val daysUntil = daysUntilExam(exam.examDate)
    val color = when {
        daysUntil < 0 -> Color.Gray
        daysUntil <= 3 -> Danger
        daysUntil <= 7 -> Warning
        else -> Primary
    }

    val breathingAlpha = rememberBreathingAlpha(
        active = daysUntil in 0..3,
        durationMillis = 1500
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateItemPlacement(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Accent strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopCenter)
                    .background(color)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(exam.subject, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    // Days badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color.copy(
                                    alpha = if (daysUntil in 0..3) breathingAlpha else 0.15f
                                )
                            )
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${exam.type} • ${exam.examDate}", style = MaterialTheme.typography.bodyMedium)
                }

                if (exam.time.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("⏰ ${exam.time}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (exam.room.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("📍 ${exam.room}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: Edit */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("️ تعديل") }

                    Button(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Danger.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🗑️ حذف", color = Danger)
                    }
                }
            }
        }
    }
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
                    listOf("نصفي", "نهائي", "فجائي", "عملي").forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) }, shape = RoundedCornerShape(12.dp))
                    }
                }
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("التاريخ (YYYY-MM-DD)") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("الوقت") }, shape = RoundedCornerShape(12.dp), singleLine = true)
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("القاعة") }, shape = RoundedCornerShape(12.dp), singleLine = true)
            }
        },
        confirmButton = {
            Button(enabled = subject.isNotBlank() && date.isNotBlank(), onClick = { onAdd(subject, type, date, time, room) }, shape = RoundedCornerShape(12.dp)) { Text("إضافة") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") }
        }
    )
}

fun daysUntilExam(dateStr: String): Int {
    return try {
        val date = LocalDate.parse(dateStr)
        java.time.Period.between(LocalDate.now(), date).days
    } catch (e: Exception) { 999 }
}
