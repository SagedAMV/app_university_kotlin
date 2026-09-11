package com.unimanager.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unimanager.app.UniApplication
import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.entity.LectureEntity
import com.unimanager.app.data.entity.TaskEntity
import com.unimanager.app.data.entity.FileEntity
import com.unimanager.app.data.entity.ExamEntity
import com.unimanager.app.data.repository.AppRepository
import com.unimanager.app.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardData(
    val fileCount: Int = 0,
    val folderCount: Int = 0,
    val lectureCount: Int = 0,
    val pendingTasks: Int = 0,
    val upcomingExams: Int = 0,
    val totalSize: Long = 0,
    val todayLectures: List<LectureEntity> = emptyList(),
    val recentFiles: List<FileEntity> = emptyList(),
    val upcomingExamList: List<ExamEntity> = emptyList(),
    val urgentTasks: List<TaskEntity> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val repo = AppRepository(db)

    val fileCount: StateFlow<Int> = repo.getFileCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lectureCount: StateFlow<Int> = repo.getLectureCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingTaskCount: StateFlow<Int> = repo.getPendingTaskCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val upcomingExamCount: StateFlow<Int> = repo.getUpcomingExamCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSize: StateFlow<Long> = repo.getTotalSize()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        fun factory() = object : ViewModelProvider.AndroidViewModelFactory(UniApplication.instance) {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass == DashboardViewModel::class.java) {
                    return DashboardViewModel(UniApplication.instance) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
