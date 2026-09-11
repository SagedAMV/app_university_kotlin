package com.unimanager.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unimanager.app.UniApplication
import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.entity.ExamEntity
import com.unimanager.app.data.repository.AppRepository
import com.unimanager.app.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExamsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val repo = AppRepository(db)

    val allExams: StateFlow<List<ExamEntity>> = repo.getAllExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingExams: StateFlow<List<ExamEntity>> = repo.getUpcomingExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingExamCount: StateFlow<Int> = repo.getUpcomingExamCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun insertExam(exam: ExamEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.insertExam(exam)
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة الامتحان: ${e.message}", e)
                _errorMessage.value = "فشل إضافة الامتحان"
            }
        }
    }

    fun updateExam(exam: ExamEntity) {
        viewModelScope.launch {
            try {
                repo.updateExam(exam)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث الامتحان"
            }
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            try {
                repo.deleteExam(exam)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف الامتحان"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        fun factory() = object : ViewModelProvider.AndroidViewModelFactory(UniApplication.instance) {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass == ExamsViewModel::class.java) {
                    return ExamsViewModel(UniApplication.instance) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
