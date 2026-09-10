package com.unimanager.app.ui.files

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unimanager.app.data.entity.FileEntity
import com.unimanager.app.data.entity.FolderEntity
import com.unimanager.app.ui.components.*
import com.unimanager.app.ui.theme.*
import com.unimanager.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(viewModel: AppViewModel, navController: NavController) {
    val rootFolders by viewModel.rootFolders.collectAsState(initial = emptyList())
    val allFiles by viewModel.allFiles.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var screenVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { screenVisible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📁", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                        Text("الملفات", fontWeight = FontWeight.Bold)
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
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Folders section
            if (rootFolders.isNotEmpty()) {
                item {
                    AnimatedEntrance(visible = screenVisible, delayMillis = 0) {
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
                            Text("📂 المجلدات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                items(rootFolders) { folder ->
                    SlideUpEntrance(visible = screenVisible) {
                        FolderItem(folder = folder, fileCount = allFiles.count { it.folderId == folder.id })
                    }
                }
            }

            // Files section
            if (allFiles.isNotEmpty()) {
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
                                    .background(Secondary)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("📄 الملفات", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                items(allFiles.take(10)) { file ->
                    SlideUpEntrance(visible = screenVisible) {
                        FileItem(file = file)
                    }
                }
            }

            if (rootFolders.isEmpty() && allFiles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("", fontSize = 64.sp)
                            Spacer(Modifier.height(16.dp))
                            Text("لا توجد ملفات بعد", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("اضغط + لإنشاء مجلد أو رفع ملف")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFileDialog(
            onDismiss = { showAddDialog = false },
            onAddFile = { name, ext -> /* TODO */ showAddDialog = false },
            onAddFolder = { name -> /* TODO */ showAddDialog = false }
        )
    }
}

@Composable
fun FolderItem(folder: FolderEntity, fileCount: Int) {
    val isPressed = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed.value) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { isPressed.value = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = if (isDark) 0.12f else 0.08f)
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
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("📁", fontSize = 24.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(folder.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "$fileCount ملف",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = Primary)
        }
    }
}

@Composable
fun FileItem(file: FileEntity) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val fileColor = when (file.type) {
        "pdf" -> Danger
        "doc" -> Info
        "img" -> Success
        "video" -> Warning
        "audio" -> Primary
        else -> Secondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(16.dp),
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(fileColor.copy(alpha = if (isDark) 0.15f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when (file.type) {
                        "pdf" -> "📄"
                        "doc" -> ""
                        "img" -> "️"
                        "video" -> ""
                        "audio" -> "🎵"
                        else -> "📎"
                    },
                    fontSize = 22.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    "${file.extension.uppercase()} • ${formatFileSize(file.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (file.isFavorite) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "مفضل",
                    tint = Danger,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddFileDialog(
    onDismiss: () -> Unit,
    onAddFile: (String, String) -> Unit,
    onAddFolder: (String) -> Unit
) {
    var isFolder by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var extension by remember { mutableStateOf("pdf") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("إضافة جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isFolder,
                        onClick = { isFolder = true },
                        label = { Text("📁 مجلد") },
                        shape = RoundedCornerShape(12.dp)
                    )
                    FilterChip(
                        selected = !isFolder,
                        onClick = { isFolder = false },
                        label = { Text(" ملف") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (!isFolder) {
                    OutlinedTextField(
                        value = extension,
                        onValueChange = { extension = it },
                        label = { Text("الامتداد") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    if (isFolder) onAddFolder(name)
                    else onAddFile(name, extension)
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text("إضافة") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") }
        }
    )
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
