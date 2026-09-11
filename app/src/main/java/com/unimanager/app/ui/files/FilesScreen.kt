package com.unimanager.app.ui.files

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.unimanager.app.data.entity.FileEntity
import com.unimanager.app.data.entity.FolderEntity
import com.unimanager.app.ui.components.*
import com.unimanager.app.ui.navigation.Routes
import com.unimanager.app.ui.theme.*
import com.unimanager.app.util.Validation
import com.unimanager.app.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(viewModel: AppViewModel, navController: NavController) {
    val rootFolders by viewModel.rootFolders.collectAsState()
    val allFiles by viewModel.allFiles.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

    // رسائل محلية (فشل اختيار/فتح الملف)
    var localMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(localMessage) {
        localMessage?.let {
            snackbarHostState.showSnackbar(it)
            localMessage = null
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var screenVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // حالات التعديل/الحذف (الحذف يمرّ عبر حوار تأكيد بدل الحذف الفوري)
    var editingFolder by remember { mutableStateOf<FolderEntity?>(null) }
    var deletingFolder by remember { mutableStateOf<FolderEntity?>(null) }
    var deletingFile by remember { mutableStateOf<FileEntity?>(null) }

    // Folder navigation state (يدعم فتح مجلد قادم من شاشة المجرة عبر وسيط التنقل)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val initialFolderId = navBackStackEntry
        ?.arguments?.getString(Routes.Files.FOLDER_ID_ARG)?.toLongOrNull()
    var currentFolderId by remember(initialFolderId) { mutableStateOf(initialFolderId) }

    val childFolders by viewModel.getChildFolders(currentFolderId).collectAsState()
    val filesInFolder by viewModel.getFilesInFolder(currentFolderId).collectAsState()

    LaunchedEffect(Unit) { screenVisible = true }

    // منتقي ملفات حقيقي (Storage Access Framework)
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = runCatching {
                    importDocument(context, uri, currentFolderId)
                }
                result.onSuccess { entity ->
                    viewModel.insertFile(entity)
                }.onFailure { e ->
                    localMessage = e.message ?: "تعذّرت إضافة الملف"
                }
            }
        }
    }

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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
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
                            onSwipe = { deletingFolder = folder }
                        ) {
                            FolderItem(
                                folder = folder,
                                fileCount = displayFiles.count { it.folderId == folder.id },
                                onFolderClick = {
                                    currentFolderId = folder.id
                                },
                                onEdit = { editingFolder = folder },
                                onDelete = { deletingFolder = folder }
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
                            onSwipe = { deletingFile = file }
                        ) {
                            FileItem(
                                file = file,
                                onFileClick = {
                                    openFile(context, file) { msg -> localMessage = msg }
                                },
                                onFavoriteToggle = {
                                    viewModel.toggleFavorite(file.id, !file.isFavorite)
                                },
                                onDelete = { deletingFile = file }
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
                                subtitle = "اضغط + لإضافة ملف حقيقي من جهازك أو إنشاء مجلد",
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
            onPickFile = {
                showAddDialog = false
                // SAF: */* يتيح اختيار أي نوع ملف
                runCatching { pickFileLauncher.launch(arrayOf("*/*")) }
                    .onFailure { localMessage = "تعذّر فتح منتقي الملفات على هذا الجهاز" }
            },
            onAddFolder = { name ->
                viewModel.insertFolder(
                    FolderEntity(name = name, parentId = currentFolderId)
                )
            }
        )
    }

    // تعديل المجلد (إعادة التسمية/اللون)
    editingFolder?.let { folder ->
        EditFolderDialog(
            folder = folder,
            onDismiss = { editingFolder = null },
            onConfirm = { name, color ->
                viewModel.updateFolder(folder.copy(name = name, color = color))
                editingFolder = null
            }
        )
    }

    // تأكيد حذف مجلد (يشمل المجلدات الفرعية وملفاتها — تتالي في Room + حذف فيزيائي)
    deletingFolder?.let { folder ->
        ConfirmActionDialog(
            title = "حذف المجلد",
            message = "سيتم حذف المجلد «${folder.name}» وكل ما بداخله من مجلدات وملفات نهائياً. هل أنت متأكد؟",
            icon = Icons.Filled.FolderDelete,
            confirmText = "حذف",
            onConfirm = { viewModel.deleteFolder(folder); deletingFolder = null },
            onDismiss = { deletingFolder = null }
        )
    }

    // تأكيد حذف ملف (يحذف النسخة الفعلية من تخزين التطبيق)
    deletingFile?.let { file ->
        ConfirmActionDialog(
            title = "حذف الملف",
            message = "سيتم حذف الملف «${file.name}» نهائياً من جهازك. هل أنت متأكد؟",
            icon = Icons.Filled.Delete,
            confirmText = "حذف",
            onConfirm = { viewModel.deleteFile(file); deletingFile = null },
            onDismiss = { deletingFile = null }
        )
    }
}

/** لوحة الألوان المتاحة للمجلدات (HEX) */
private val FOLDER_COLOR_PALETTE = listOf(
    "#6366f1", // بنفسجي
    "#0ea5e9", // أزرق
    "#10b981", // أخضر
    "#f59e0b", // كهرماني
    "#ef4444", // أحمر
    "#ec4899"  // وردي
)

/** حوار تعديل مجلد: إعادة التسمية واختيار اللون */
@Composable
fun EditFolderDialog(
    folder: FolderEntity,
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf(folder.name) }
    var color by remember { mutableStateOf(folder.color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text("تعديل المجلد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المجلد") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Text(
                    "لون المجلد",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FOLDER_COLOR_PALETTE.forEach { hex ->
                        val tint = folderTint(hex)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (color == hex) tint else tint.copy(alpha = 0.35f)
                                )
                                .clickable { color = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == hex) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim(), color) },
                shape = RoundedCornerShape(12.dp)
            ) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("إلغاء") }
        }
    )
}

/**
 * فتح ملف عبر تطبيقات النظام باستخدام FileProvider.
 * يُبلغ المستخدم عبر [onMessage] عند غياب الملف أو عدم وجود تطبيق قادر على فتحه.
 */
private fun openFile(
    context: Context,
    file: FileEntity,
    onMessage: (String) -> Unit
) {
    val physicalFile = File(file.filePath)
    if (file.filePath.isBlank() || !physicalFile.exists()) {
        onMessage("الملف غير موجود على الجهاز")
        return
    }
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            physicalFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, file.mimeType.ifBlank { "*/*" })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        onMessage("لا يوجد تطبيق على جهازك قادر على فتح هذا النوع من الملفات")
    } catch (e: Exception) {
        android.util.Log.e("FilesScreen", "Cannot open file: ${e.message}", e)
        onMessage("تعذّر فتح الملف")
    }
}

/**
 * نسخ مستند اختاره المستخدم عبر SAF إلى تخزين التطبيق الداخلي،
 * وبناء كيان FileEntity ببيانات حقيقية (الاسم، النوع، الحجم، المسار).
 */
private suspend fun importDocument(
    context: Context,
    uri: Uri,
    folderId: Long?
): FileEntity = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver

    val displayName = queryDisplayName(context, uri)
        ?: "document_${System.currentTimeMillis()}"

    val ext = displayName.substringAfterLast('.', "").lowercase()
    if (ext.isBlank() || ext !in Validation.ALLOWED_EXTENSIONS) {
        throw IOException("نوع الملف غير مدعوم (.${ext.ifBlank { "؟" }})")
    }

    val baseName = displayName.substringBeforeLast('.', displayName)
    val mimeType = resolver.getType(uri)?.takeIf { it.isNotBlank() } ?: getMimeType(ext)

    val dir = File(context.filesDir, "documents").apply { mkdirs() }
    val safeName = sanitizeFileName(displayName)
    var destination = File(dir, safeName)
    var counter = 1
    while (destination.exists()) {
        val stem = safeName.substringBeforeLast('.', safeName)
        destination = File(dir, "${stem}_$counter.$ext")
        counter++
    }

    resolver.openInputStream(uri)?.use { input ->
        FileOutputStream(destination).use { output ->
            input.copyTo(output)
        }
    } ?: throw IOException("تعذّر قراءة الملف المختار")

    FileEntity(
        name = baseName,
        extension = ext,
        type = determineFileType(ext),
        mimeType = mimeType,
        size = destination.length(),
        folderId = folderId,
        filePath = destination.absolutePath
    )
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
    }
    return uri.lastPathSegment?.substringAfterLast('/')
}

private fun sanitizeFileName(name: String): String =
    name.replace(Regex("[\\\\/:*?\"<>|]"), "_").ifBlank { "file" }

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

/** تحويل لون المجلد المخزّن كسلسلة HEX إلى Color مع قيمة احتياطية */
fun folderTint(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Primary)

/**
 * Folder Item - يعرض مجلد مع عدد الملفات
 */
@Composable
fun FolderItem(
    folder: FolderEntity,
    fileCount: Int,
    onFolderClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tint = folderTint(folder.color)

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Folder,
                    contentDescription = folder.name,
                    tint = tint,
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

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "تعديل المجلد",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "حذف المجلد",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
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
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit
) {
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

            // Delete button
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "حذف الملف",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
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
    onPickFile: () -> Unit,
    onAddFolder: (name: String) -> Unit
) {
    var selectedMode by remember { mutableStateOf("file") }
    var folderName by remember { mutableStateOf("") }
    var folderError by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()

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
                // اختيار ملف حقيقي من الجهاز
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.UploadFile,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "اختر ملفاً من جهازك (PDF، مستندات Office، صور، فيديو، صوت...)",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "سيتم نسخ الملف داخل التطبيق ليظل متاحاً للفتح لاحقاً.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onPickFile,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("اختيار ملف من الجهاز")
                    }
                }
            } else {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it; folderError = null },
                    label = { Text("اسم المجلد") },
                    isError = folderError != null,
                    supportingText = folderError?.let { msg ->
                        { Text(msg, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Folder, contentDescription = null) }
                )

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
                            val result = Validation.validateName(folderName)
                            val failure = result.exceptionOrNull()?.message
                            if (failure != null) {
                                folderError = failure
                            } else {
                                onAddFolder(Validation.sanitizeInput(folderName))
                                onDismiss()
                            }
                        },
                        enabled = folderName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("إضافة") }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// Utility functions
fun determineFileType(extension: String): String {
    return when (extension.lowercase()) {
        "pdf" -> "pdf"
        "doc", "docx", "rtf" -> "doc"
        "xls", "xlsx", "csv" -> "doc"
        "ppt", "pptx" -> "doc"
        "jpg", "jpeg", "png", "gif", "webp", "bmp" -> "img"
        "mp4", "avi", "mkv", "mov", "wmv" -> "video"
        "mp3", "wav", "m4a", "ogg", "flac" -> "audio"
        "zip", "rar", "7z", "tar", "gz" -> "archive"
        "txt", "md" -> "code"
        else -> "other"
    }
}

fun getMimeType(extension: String): String {
    return when (extension.lowercase()) {
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "ppt" -> "application/vnd.ms-powerpoint"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "rtf" -> "application/rtf"
        "csv" -> "text/csv"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "bmp" -> "image/bmp"
        "mp4" -> "video/mp4"
        "avi" -> "video/x-msvideo"
        "mkv" -> "video/x-matroska"
        "mov" -> "video/quicktime"
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "m4a" -> "audio/mp4"
        "ogg" -> "audio/ogg"
        "flac" -> "audio/flac"
        "zip" -> "application/zip"
        "rar" -> "application/vnd.rar"
        "7z" -> "application/x-7z-compressed"
        "txt", "md" -> "text/plain"
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
