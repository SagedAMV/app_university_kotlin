package com.unimanager.app.ui.files

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.unimanager.app.data.entity.FileEntity
import com.unimanager.app.data.entity.FolderEntity
import com.unimanager.app.ui.components.*
import com.unimanager.app.ui.theme.*
import com.unimanager.app.viewmodel.AppViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(viewModel: AppViewModel, navController: NavController) {
    val rootFolders by viewModel.rootFolders.collectAsState()
    val allFiles by viewModel.allFiles.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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

    // Folder navigation state
    var currentFolderId by remember { mutableStateOf<Long?>(null) }
    val childFolders by viewModel.getChildFolders(currentFolderId).collectAsState()
    val filesInFolder by viewModel.getFilesInFolder(currentFolderId).collectAsState()

    LaunchedEffect(Unit) { screenVisible = true }

    // Display folders and files based on current folder
    val displayFolders = if (currentFolderId == null) rootFolders else childFolders
    val displayFiles = if (currentFolderId == null) allFiles else filesInFolder

    // Filter based on search
    val filteredFolders = if (searchQuery.isBlank()) displayFolders
    else displayFolders.filter { it.name.contains(searchQuery, ignoreCase = true) }

    val filteredFiles = if (searchQuery.isBlank()) displayFiles
    else displayFiles.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📁", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            if (currentFolderId != null) "داخل المجلد" else "الملفات",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    if (currentFolderId != null) {
                        IconButton(onClick = { currentFolderId = null }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع")
                        }
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
                        FilesSectionTitle("📂 المجلدات", color = Primary)
                    }
                    items(filteredFolders, key = { it.id }) { folder ->
                        SwipeableItem(
                            onSwipe = { viewModel.deleteFolder(folder) }
                        ) {
                            FolderItem(
                                folder = folder,
                                fileCount = displayFiles.count { it.folderId == folder.id },
                                onFolderClick = {
                                    currentFolderId = folder.id
                                }
                            )
                        }
                    }
                }

                if (filteredFiles.isNotEmpty()) {
                    item {
                        FilesSectionTitle("📄 الملفات", color = Secondary)
                    }
                    items(filteredFiles, key = { it.id }) { file ->
                        SwipeableItem(
                            onSwipe = { viewModel.deleteFile(file) }
                        ) {
                            FileItem(
                                file = file,
                                onFileClick = {
                                    // Open file if it exists
                                    if (file.filePath.isNotBlank()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    File(file.filePath)
                                                )
                                                setDataAndType(uri, file.mimeType)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            android.util.Log.e("FilesScreen", "Cannot open file: ${e.message}")
                                        }
                                    }
                                },
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
                                subtitle = "اضغط + لإنشاء مجلد أو إضافة ملف",
                                actionText = "إضافة",
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
                        folderId = currentFolderId,
                        filePath = ""
                    )
                )
            },
            onAddFolder = { name ->
                viewModel.insertFolder(
                    FolderEntity(name = name, parentId = currentFolderId)
                )
            }
        )
    }
}

@Composable
private fun FilesSectionTitle(title: String, color: Color) {
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

/**
 * Folder Item - يعرض مجلد مع عدد الملفات
 */
@Composable
fun FolderItem(
    folder: FolderEntity,
    fileCount: Int,
    onFolderClick: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val pressScale by animateFloatAsState(
        targetValue = 1f,
        label = "folderScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pressScale)
            .clickable(onClick = onFolderClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Folder icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(folder.color.hashCode()).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Folder,
                    contentDescription = folder.name,
                    tint = Color(folder.color.hashCode()),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "$fileCount ملف",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = "فتح",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * File Item - يعرض ملف مع نوعه وأيقونته
 */
@Composable
fun FileItem(
    file: FileEntity,
    onFileClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val fileColor = getFileColor(file.type)
    val fileIcon = getFileIcon(file.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFileClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(fileColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    fileIcon,
                    contentDescription = file.name,
                    tint = fileColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        ".${file.extension.uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (file.size > 0) {
                        Text(
                            " • ${formatFileSize(file.size)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Favorite button
            IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (file.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "مفضلة",
                    tint = if (file.isFavorite) Warning else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Add File/Folder Dialog - Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFileDialog(
    onDismiss: () -> Unit,
    onAddFile: (name: String, extension: String) -> Unit,
    onAddFolder: (name: String) -> Unit
) {
    var selectedMode by remember { mutableStateOf("file") }
    var fileName by remember { mutableStateOf("") }
    var fileExt by remember { mutableStateOf("pdf") }
    var folderName by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    val extensions = listOf("pdf", "doc", "ppt", "jpg", "png", "mp4", "mp3", "zip", "txt")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Handle bar
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "إضافة جديد",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            // Mode selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f).clickable { selectedMode = "file" },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedMode == "file") Primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (selectedMode == "file") androidx.compose.foundation.BorderStroke(2.dp, Primary) else null
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = if (selectedMode == "file") Primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("ملف", fontWeight = if (selectedMode == "file") FontWeight.Bold else FontWeight.Normal)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f).clickable { selectedMode = "folder" },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedMode == "folder") Secondary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (selectedMode == "folder") androidx.compose.foundation.BorderStroke(2.dp, Secondary) else null
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Folder, contentDescription = null, tint = if (selectedMode == "folder") Secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("مجلد", fontWeight = if (selectedMode == "folder") FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (selectedMode == "file") {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("اسم الملف") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) }
                )

                Spacer(Modifier.height(16.dp))

                Text("نوع الملف", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))

                // Extension selector - scrollable
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    extensions.take(4).forEach { ext ->
                        FilterChip(
                            selected = fileExt == ext,
                            onClick = { fileExt = ext },
                            label = { Text(ext.uppercase()) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    extensions.drop(4).forEach { ext ->
                        FilterChip(
                            selected = fileExt == ext,
                            onClick = { fileExt = ext },
                            label = { Text(ext.uppercase()) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("اسم المجلد") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Folder, contentDescription = null) }
                )
            }

            Spacer(Modifier.height(32.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("إلغاء") }

                Button(
                    onClick = {
                        when (selectedMode) {
                            "file" -> {
                                if (fileName.isNotBlank()) {
                                    onAddFile(fileName, fileExt)
                                    onDismiss()
                                }
                            }
                            "folder" -> {
                                if (folderName.isNotBlank()) {
                                    onAddFolder(folderName)
                                    onDismiss()
                                }
                            }
                        }
                    },
                    enabled = when (selectedMode) {
                        "file" -> fileName.isNotBlank()
                        "folder" -> folderName.isNotBlank()
                        else -> false
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("إضافة") }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// Utility functions
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

fun getFileColor(type: String): Color {
    return when (type) {
        "pdf" -> Color(0xFFEF4444)
        "doc" -> Color(0xFF3B82F6)
        "img" -> Color(0xFF10B981)
        "video" -> Color(0xFF8B5CF6)
        "audio" -> Color(0xFFF59E0B)
        "archive" -> Color(0xFF6B7280)
        "code" -> Color(0xFF06B6D4)
        else -> Color(0xFF9CA3AF)
    }
}

fun getFileIcon(type: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        "pdf" -> Icons.Filled.PictureAsPdf
        "doc" -> Icons.Filled.Description
        "img" -> Icons.Filled.Image
        "video" -> Icons.Filled.Videocam
        "audio" -> Icons.Filled.MusicNote
        "archive" -> Icons.Filled.FolderZip
        "code" -> Icons.Filled.Code
        else -> Icons.Filled.InsertDriveFile
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
