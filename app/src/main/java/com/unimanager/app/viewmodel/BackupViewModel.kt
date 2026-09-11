package com.unimanager.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unimanager.app.backup.BackupHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel خاص بشاشة النسخ الاحتياطي.
 * BackupHelper مُحقون بالكامل عبر Hilt (نفس قاعدة بيانات التطبيق).
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupHelper: BackupHelper
) : ViewModel() {

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun export(uri: Uri) {
        viewModelScope.launch {
            _isExporting.value = true
            val result = backupHelper.exportBackup(uri)
            _isExporting.value = false
            result.fold(
                onSuccess = { _message.value = it },
                onFailure = { _message.value = "فشل التصدير: ${it.message}" }
            )
        }
    }

    fun import(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            val result = backupHelper.importBackup(uri)
            _isImporting.value = false
            result.fold(
                onSuccess = { _message.value = it },
                onFailure = { _message.value = "فشل الاستيراد: ${it.message}" }
            )
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
