package com.unimanager.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unimanager.app.data.entity.*
import com.unimanager.app.data.repository.AppRepository
import com.unimanager.app.ui.components.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // ====== Data Flows (StateFlow for performance) ======

    // Folders
    val allFolders: StateFlow<List<FolderEntity>> = repository.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rootFolders: StateFlow<List<FolderEntity>> = repository.getRootFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Files
    val allFiles: StateFlow<List<FileEntity>> = repository.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteFiles: StateFlow<List<FileEntity>> = repository.getFavoriteFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fileCount: StateFlow<Int> = repository.getFileCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSize: StateFlow<Long> = repository.getTotalSize()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Lectures
    val allLectures: StateFlow<List<LectureEntity>> = repository.getAllLectures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lectureCount: StateFlow<Int> = repository.getLectureCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Tasks
    val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<TaskEntity>> = repository.getPendingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTaskCount: StateFlow<Int> = repository.getPendingTaskCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Notes
    val allNotes: StateFlow<List<NoteEntity>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Exams
    val allExams: StateFlow<List<ExamEntity>> = repository.getAllExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingExams: StateFlow<List<ExamEntity>> = repository.getUpcomingExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingExamCount: StateFlow<Int> = repository.getUpcomingExamCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ====== UI State for Error Handling ======

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // ====== Search ======
    fun searchFiles(query: String): Flow<List<FileEntity>> = repository.searchFiles(query)

    // ====== Folder Operations ======
    fun getChildFolders(parentId: Long?): StateFlow<List<FolderEntity>> =
        repository.getChildFolders(parentId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertFolder(folder: FolderEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.insertFolder(folder)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة المجلد بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة المجلد", e)
                _errorMessage.value = "فشل إضافة المجلد: ${e.message}"
                android.util.Log.e("AppViewModel", "Failed to insert folder", e)
            }
        }
    }

    fun updateFolder(folder: FolderEntity) {
        viewModelScope.launch {
            try {
                repository.updateFolder(folder)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث المجلد"
                android.util.Log.e("AppViewModel", "Failed to update folder", e)
            }
        }
    }

    fun deleteFolder(folder: FolderEntity) {
        viewModelScope.launch {
            try {
                repository.deleteFolder(folder)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف المجلد"
                android.util.Log.e("AppViewModel", "Failed to delete folder", e)
            }
        }
    }

    // ====== File Operations ======
    fun getFilesInFolder(folderId: Long?): StateFlow<List<FileEntity>> =
        repository.getFilesInFolder(folderId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertFile(file: FileEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.insertFile(file)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة الملف بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة الملف", e)
                _errorMessage.value = "فشل إضافة الملف: ${e.message}"
                android.util.Log.e("AppViewModel", "Failed to insert file", e)
            }
        }
    }

    fun updateFile(file: FileEntity) {
        viewModelScope.launch {
            try {
                repository.updateFile(file)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث الملف"
                android.util.Log.e("AppViewModel", "Failed to update file", e)
            }
        }
    }

    fun deleteFile(file: FileEntity) {
        viewModelScope.launch {
            try {
                repository.deleteFile(file)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف الملف"
                android.util.Log.e("AppViewModel", "Failed to delete file", e)
            }
        }
    }

    fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateFavorite(id, isFavorite)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث المفضلة"
                android.util.Log.e("AppViewModel", "Failed to toggle favorite", e)
            }
        }
    }

    // ====== Lecture Operations ======
    fun getLecturesByDay(day: String): StateFlow<List<LectureEntity>> =
        repository.getLecturesByDay(day).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertLecture(lecture: LectureEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.insertLecture(lecture)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة المحاضرة بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة المحاضرة", e)
                _errorMessage.value = "فشل إضافة المحاضرة: ${e.message}"
                android.util.Log.e("AppViewModel", "Failed to insert lecture", e)
            }
        }
    }

    fun updateLecture(lecture: LectureEntity) {
        viewModelScope.launch {
            try {
                repository.updateLecture(lecture)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث المحاضرة"
                android.util.Log.e("AppViewModel", "Failed to update lecture", e)
            }
        }
    }

    fun deleteLecture(lecture: LectureEntity) {
        viewModelScope.launch {
            try {
                repository.deleteLecture(lecture)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف المحاضرة"
                android.util.Log.e("AppViewModel", "Failed to delete lecture", e)
            }
        }
    }

    // ====== Task Operations ======
    fun insertTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.insertTask(task)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة المهمة بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة المهمة", e)
                _errorMessage.value = "فشل إضافة المهمة: ${e.message}"
                android.util.Log.e("AppViewModel", "Failed to insert task", e)
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                repository.updateTask(task)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث المهمة"
                android.util.Log.e("AppViewModel", "Failed to update task", e)
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                repository.deleteTask(task)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف المهمة"
                android.util.Log.e("AppViewModel", "Failed to delete task", e)
            }
        }
    }

    fun toggleTaskDone(id: Long, isDone: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateTaskDone(id, isDone)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث المهمة"
                android.util.Log.e("AppViewModel", "Failed to toggle task", e)
            }
        }
    }

    // ====== Note Operations ======
    fun insertNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.insertNote(note)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة الملاحظة بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة الملاحظة", e)
                _errorMessage.value = "فشل إضافة الملاحظة: ${e.message}"
                android.util.Log.e("AppViewModel", "Failed to insert note", e)
            }
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                repository.updateNote(note)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث الملاحظة"
                android.util.Log.e("AppViewModel", "Failed to update note", e)
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                repository.deleteNote(note)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف الملاحظة"
                android.util.Log.e("AppViewModel", "Failed to delete note", e)
            }
        }
    }

    // ====== Exam Operations ======
    fun insertExam(exam: ExamEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.insertExam(exam)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة الامتحان بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة الامتحان", e)
                _errorMessage.value = "فشل إضافة الامتحان: ${e.message}"
                android.util.Log.e("AppViewModel", "Failed to insert exam", e)
            }
        }
    }

    fun updateExam(exam: ExamEntity) {
        viewModelScope.launch {
            try {
                repository.updateExam(exam)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث الامتحان"
                android.util.Log.e("AppViewModel", "Failed to update exam", e)
            }
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            try {
                repository.deleteExam(exam)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف الامتحان"
                android.util.Log.e("AppViewModel", "Failed to delete exam", e)
            }
        }
    }

    // ====== Clear Messages ======
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // ====== Dashboard Data ======
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
}
