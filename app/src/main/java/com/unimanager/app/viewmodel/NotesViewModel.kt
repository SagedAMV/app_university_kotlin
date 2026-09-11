package com.unimanager.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unimanager.app.UniApplication
import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.entity.NoteEntity
import com.unimanager.app.data.repository.AppRepository
import com.unimanager.app.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val repo = AppRepository(db)

    val allNotes: StateFlow<List<NoteEntity>> = repo.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun insertNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.insertNote(note)
                _uiState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة الملاحظة: ${e.message}", e)
                _errorMessage.value = "فشل إضافة الملاحظة"
            }
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                repo.updateNote(note)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث الملاحظة"
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                repo.deleteNote(note)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف الملاحظة"
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
                if (modelClass == NotesViewModel::class.java) {
                    return NotesViewModel(UniApplication.instance) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
