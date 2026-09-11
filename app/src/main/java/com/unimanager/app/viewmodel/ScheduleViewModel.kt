package com.unimanager.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unimanager.app.UniApplication
import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.entity.LectureEntity
import com.unimanager.app.data.repository.AppRepository
import com.unimanager.app.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val repo = AppRepository(db)

    val allLectures: StateFlow<List<LectureEntity>> = repo.getAllLectures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lectureCount: StateFlow<Int> = repo.getLectureCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun insertLecture(lecture: LectureEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.insertLecture(lecture)
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة المحاضرة: ${e.message}", e)
                _errorMessage.value = "فشل إضافة المحاضرة"
            }
        }
    }

    fun updateLecture(lecture: LectureEntity) {
        viewModelScope.launch {
            try {
                repo.updateLecture(lecture)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث المحاضرة"
            }
        }
    }

    fun deleteLecture(lecture: LectureEntity) {
        viewModelScope.launch {
            try {
                repo.deleteLecture(lecture)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف المحاضرة"
            }
        }
    }

    fun getLecturesByDay(day: String): StateFlow<List<LectureEntity>> {
        return repo.getLecturesByDay(day)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        fun factory() = object : ViewModelProvider.AndroidViewModelFactory(UniApplication.instance) {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass == ScheduleViewModel::class.java) {
                    return ScheduleViewModel(UniApplication.instance) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
