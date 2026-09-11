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
    val rootFolders by viewModel.rootFolders.collectAsState()
    val allFiles by viewModel.allFiles.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

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

    var showAddDialog by remember { mutableStateOf(false) }
    var screenVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { screenVisible = true }

    // Filter files and folders based on search
    val filteredFolders = if (searchQuery.isBlank()) rootFolders
    else rootFolders.filter { it.name.contains(searchQuery, ignoreCase = true) }

    val filteredFiles = if (searchQuery.isBlank()) allFiles
    else allFiles.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📁", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                        Text("الملفات", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "مسح البحث")
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
                shape = RoundedCornerShape(16.dp)
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
            // Search Bar
            if (screenVisible) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { -it / 2 }
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onClear = { searchQuery = "" },
                        placeholder = "بحث في الملفات والمجلدات...",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredFolders.isNotEmpty()) {
                    item {
                        SectionTitle("📂 المجلدات", color = Primary)
                    }
                    items(filteredFolders, key = { it.id }) { folder ->
                        SwipeableItem(
                            onSwipe = { viewModel.deleteFolder(folder) }
                        ) {
                            FolderItem(
                                folder = folder,
                                fileCount = filteredFiles.count { it.folderId == folder.id },
                                onFolderClick = { /* TODO */ }
                            )
                        }
                    }
                }

                if (filteredFiles.isNotEmpty()) {
                    item {
                        SectionTitle("📄 الملفات", color = Secondary)
                    }
                    items(filteredFiles, key = { it.id }) { file ->
                        SwipeableItem(
                            onSwipe = { viewModel.deleteFile(file) }
                        ) {
                            FileItem(
                                file = file,
                                onFileClick = { /* TODO */ },
                                onFavoriteToggle = {
                                    viewModel.toggleFavorite(file.id, !file.isFavorite)
                                }
                            )
                        }
                    }
                }

                if (filteredFolders.isEmpty() && filteredFiles.isEmpty()) {
                    if (searchQuery.isNotBlank()) {
                        item {
                            EmptyStateEnhanced(
                                icon = "🔍",
                                title = "لا توجد نتائج",
                                subtitle = "جرب البحث بكلمات أخرى",
                                actionText = "مسح البحث",
                                onActionClick = { searchQuery = "" }
                            )
                        }
                    } else {
                        item {
                            EmptyStateEnhanced(
                                icon = "📂",
                                title = "لا توجد ملفات بعد",
                                subtitle = "اضغط + لإنشاء مجلد أو رفع ملف",
                                actionText = "إضافة مجلد",
                                onActionClick = { showAddDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFileDialog(
            onDismiss = { showAddDialog = false },
            onAddFile = { name, ext ->
                viewModel.insertFile(
                    FileEntity(
                        name = name,
                        extension = ext,
                        type = determineFileType(ext),
                        mimeType = getMimeType(ext),
                        size = 0L,
                        folderId = null,
                        filePath = ""
                    )
                )
            },
            onAddFolder = { name ->
                viewModel.insertFolder(
                    FolderEntity(name = name, parentId = null)
                )
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String, color: Color) {
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
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

fun determineFileType(extension: String): String {
    return when (extension.lowercase()) {
        "pdf" -> "pdf"
        "doc", "docx" -> "doc"
        "xls", "xlsx" -> "doc"
        "ppt", "pptx" -> "doc"
        "jpg", "jpeg", "png", "gif", "webp" -> "img"
        "mp4", "avi", "mkv", "mov" -> "video"
        "mp3", "wav", "m4a", "ogg" -> "audio"
        "zip", "rar", "7z" -> "archive"
        "txt", "md" -> "code"
        else -> "other"
    }
}

fun getMimeType(extension: String): String {
    return when (extension.lowercase()) {
        "pdf" -> "application/pdf"
        "doc", "docx" -> "application/msword"
        "xls", "xlsx" -> "application/vnd.ms-excel"
        "ppt", "pptx" -> "application/vnd.ms-powerpoint"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "mp4" -> "video/mp4"
        "mp3" -> "audio/mpeg"
        "txt" -> "text/plain"
        else -> "*/*"
    }
}
