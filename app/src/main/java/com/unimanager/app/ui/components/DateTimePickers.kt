package com.unimanager.app.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Date Picker Dialog - Material 3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "التاريخ",
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "اختر التاريخ")
            }
        }
    )

    // سبب الإصلاح: كان استدعاء DatePickerDialog(...).show() ثم "showDialog = false" يحدث
    // مباشرة داخل جسم الدالة القابلة للتركيب (Composable) وليس داخل أثر جانبي (Side Effect).
    // هذا مخالف لعقد Compose الذي يفترض أن تكون الدوال القابلة للتركيب خالية من الآثار
    // الجانبية وقابلة لإعادة الاستدعاء عدة مرات لكل إطار (Recomposition)؛ فأي إعادة تركيب
    // إضافية لسبب آخر بينما showDialog = true كانت قد تُعيد إنشاء وإظهار حوار Android نظامي
    // إضافي فوق نفسه. استخدام LaunchedEffect(showDialog) يضمن أن الحوار يُبنى ويُعرض مرة
    // واحدة فقط عند تحوّل showDialog إلى true، وأن إعادة ضبطه تحدث كأثر جانبي منضبط لا كتأثير
    // مباشر أثناء التركيب.
    val context = LocalContext.current
    LaunchedEffect(showDialog) {
        if (showDialog) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val date = LocalDate.of(year, month + 1, dayOfMonth)
                    onValueChange(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setOnDismissListener { showDialog = false }
            }.show()
        }
    }
}

/**
 * Time Picker Dialog - Material 3
 */
@Composable
fun TimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "الوقت",
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.AccessTime, contentDescription = "اختر الوقت")
            }
        }
    )

    // نفس إصلاح DatePickerField أعلاه: إظهار الحوار داخل LaunchedEffect بدل جسم composable
    // مباشرة، مع إعادة ضبط showDialog عبر مستمع الإغلاق الفعلي للحوار.
    val context = LocalContext.current
    LaunchedEffect(showDialog) {
        if (showDialog) {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    onValueChange(String.format("%02d:%02d", hourOfDay, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // 24h format
            ).apply {
                setOnDismissListener { showDialog = false }
            }.show()
        }
    }
}
