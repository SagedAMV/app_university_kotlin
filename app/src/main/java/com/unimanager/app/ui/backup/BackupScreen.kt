package com.unimanager.app.ui.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unimanager.app.ui.theme.*
import com.unimanager.app.viewmodel.BackupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onBack: () -> Unit) {
    val viewModel: BackupViewModel = hiltViewModel()
    val isExporting by viewModel.isExporting.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val message by viewModel.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.export(it) }
    }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.import(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔒", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                        Text("النسخ الاحتياطي", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        " النسخ الاحتياطي",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "احفظ بياناتك في ملف JSON واستعدها في أي وقت",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Export button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                onClick = { exportLauncher.launch("unimanager_backup.json") },
                enabled = !isExporting && !isImporting,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Success.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Success,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.Upload,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "تصدير البيانات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "احفظ نسخة من جميع بياناتك",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Import button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                enabled = !isExporting && !isImporting,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Info.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Info.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Info,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.Download,
                                contentDescription = null,
                                tint = Info,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "استيراد البيانات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "استعد بياناتك من ملف نسخة احتياطية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Warning card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "⚠️ استيراد البيانات سيحذف جميع البيانات الحالية ويستبدلها بالبيانات المستوردة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Info
            Text(
                "💡 نصيحة: قم بعمل نسخة احتياطية دورياً للحفاظ على بياناتك آمنة",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
