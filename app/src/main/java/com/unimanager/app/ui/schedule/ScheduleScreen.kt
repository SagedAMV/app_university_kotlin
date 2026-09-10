package com.unimanager.app.ui.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unimanager.app.data.entity.LectureEntity
import com.unimanager.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: AppViewModel) {
    val lectures by viewModel.allLectures.collectAsState(initial = emptyList())
    val days = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    Scaffold(
        topBar = { TopAppBar(title = { Text("📅 جدول المحاضرات") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Add lecture */ }) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة محاضرة")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (lectures.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📅", style = MaterialTheme.typography.displayLarge)
                            Text("لا توجد محاضرات بعد")
                        }
                    }
                }
            } else {
                days.forEach { day ->
                    val dayLectures = lectures.filter { it.day == day }
                    if (dayLectures.isNotEmpty()) {
                        item {
                            Text(day, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        items(dayLectures) { lecture ->
                            LectureItem(lecture = lecture, onDelete = { viewModel.deleteLecture(lecture) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LectureItem(lecture: LectureEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(end = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(lecture.timeFrom, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                if (lecture.timeTo.isNotBlank()) {
                    Text(lecture.timeTo, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(lecture.subject, style = MaterialTheme.typography.titleMedium)
                Text(lecture.doctor, style = MaterialTheme.typography.bodySmall)
                if (lecture.room.isNotBlank()) {
                    Text("📍 ${lecture.room}", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
