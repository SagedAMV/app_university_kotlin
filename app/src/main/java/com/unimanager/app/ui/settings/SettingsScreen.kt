package com.unimanager.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import com.unimanager.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onBackupClick: () -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    useDynamicColor: Boolean = false,
    onDynamicColorChange: (Boolean) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚙️", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                        Text("الإعدادات", fontWeight = FontWeight.Bold)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance section
            SettingsSection(title = "🎨 المظهر") {
                ThemeToggleItem(
                    icon = if (isDarkTheme) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    title = "الوضع الليلي",
                    subtitle = if (isDarkTheme) "مفعّل" else "معطّل",
                    isChecked = isDarkTheme,
                    onCheckedChange = onThemeChange
                )
                // Dynamic Color متاح فقط على Android 12+ (API 31)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ThemeToggleItem(
                        icon = Icons.Filled.Palette,
                        title = "الألوان الديناميكية",
                        subtitle = if (useDynamicColor) "مطابقة لألوان النظام" else "معطّل",
                        isChecked = useDynamicColor,
                        onCheckedChange = onDynamicColorChange
                    )
                }
            }

            // Backup section
            SettingsSection(title = "🔒 النسخ الاحتياطي") {
                SettingsItem(
                    icon = Icons.Filled.Backup,
                    title = "النسخ الاحتياطي والاستيراد",
                    subtitle = "احفظ واستعد بياناتك",
                    onClick = onBackupClick
                )
            }

            // About section
            SettingsSection(title = "ℹ️ حول التطبيق") {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "مدير الجامعة",
                    subtitle = "الإصدار 3.0.0",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Filled.Code,
                    title = "التقنيات المستخدمة",
                    subtitle = "Kotlin, Compose, Room, Hilt",
                    onClick = { }
                )
            }

            // Developer section
            SettingsSection(title = "👨‍💻 المطور") {
                SettingsItem(
                    icon = Icons.Filled.Person,
                    title = "SagedAMV",
                    subtitle = "github.com/SagedAMV",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ThemeToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        )
    }
}
