package com.unimanager.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unimanager.app.UniApplication
import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.entity.FileEntity
import com.unimanager.app.data.entity.FolderEntity
import com.unimanager.app.data.repository.AppRepository
import com.unimanager.app.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FilesViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val repo = AppRepository(db)

    // Data
    val rootFolders: StateFlow<List<FolderEntity>> = repo.getRootFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFiles: StateFlow<List<FileEntity>> = repo.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteFiles: StateFlow<List<FileEntity>> = repo.getFavoriteFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI State
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Operations
    fun insertFolder(folder: FolderEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.insertFolder(folder)
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة المجلد: ${e.message}", e)
                _errorMessage.value = "فشل إضافة المجلد"
            }
        }
    }

    fun insertFile(file: FileEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.insertFile(file)
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة الملف: ${e.message}", e)
                _errorMessage.value = "فشل إضافة الملف"
            }
        }
    }

    fun deleteFolder(folder: FolderEntity) {
        viewModelScope.launch {
            try {
                repo.deleteFolder(folder)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف المجلد"
            }
        }
    }

    fun deleteFile(file: FileEntity) {
        viewModelScope.launch {
            try {
                repo.deleteFile(file)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف الملف"
            }
        }
    }

    fun updateFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repo.updateFavorite(id, isFavorite)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث المفضلة"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Factory
    companion object {
        fun factory() = object : ViewModelProvider.AndroidViewModelFactory(UniApplication.instance) {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass == FilesViewModel::class.java) {
                    return FilesViewModel(UniApplication.instance) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
