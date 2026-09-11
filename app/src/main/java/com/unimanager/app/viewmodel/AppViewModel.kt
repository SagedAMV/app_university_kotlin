package com.unimanager.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unimanager.app.UniApplication
import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.entity.*
import com.unimanager.app.data.repository.AppRepository
import com.unimanager.app.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = AppRepository(db)

    // ====== Data Flows (StateFlow for performance) ======

    // Folders
    val allFolders: StateFlow<List<FolderEntity>> = repo.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rootFolders: StateFlow<List<FolderEntity>> = repo.getRootFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Files
    val allFiles: StateFlow<List<FileEntity>> = repo.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteFiles: StateFlow<List<FileEntity>> = repo.getFavoriteFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fileCount: StateFlow<Int> = repo.getFileCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSize: StateFlow<Long> = repo.getTotalSize()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Lectures
    val allLectures: StateFlow<List<LectureEntity>> = repo.getAllLectures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lectureCount: StateFlow<Int> = repo.getLectureCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Tasks
    val allTasks: StateFlow<List<TaskEntity>> = repo.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<TaskEntity>> = repo.getPendingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTaskCount: StateFlow<Int> = repo.getPendingTaskCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Notes
    val allNotes: StateFlow<List<NoteEntity>> = repo.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Exams
    val allExams: StateFlow<List<ExamEntity>> = repo.getAllExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingExams: StateFlow<List<ExamEntity>> = repo.getUpcomingExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingExamCount: StateFlow<Int> = repo.getUpcomingExamCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ====== UI State for Error Handling ======

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // ====== Search ======
    fun searchFiles(query: String): Flow<List<FileEntity>> = repo.searchFiles(query)

    // ====== Folder Operations ======
    fun getChildFolders(parentId: Long?): StateFlow<List<FolderEntity>> =
        repo.getChildFolders(parentId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertFolder(folder: FolderEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.insertFolder(folder)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة المجلد بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة المجلد", e)
                _errorMessage.value = "فشل إضافة المجلد: ${e.message}"
            }
        }
    }

    fun updateFolder(folder: FolderEntity) {
        viewModelScope.launch {
            try {
                repo.updateFolder(folder)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث المجلد"
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

    // ====== File Operations ======
    fun getFilesInFolder(folderId: Long?): StateFlow<List<FileEntity>> =
        repo.getFilesInFolder(folderId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertFile(file: FileEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.insertFile(file)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة الملف بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة الملف", e)
                _errorMessage.value = "فشل إضافة الملف: ${e.message}"
            }
        }
    }

    fun updateFile(file: FileEntity) {
        viewModelScope.launch {
            try {
                repo.updateFile(file)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث الملف"
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

    fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repo.updateFavorite(id, isFavorite)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث المفضلة"
            }
        }
    }

    // ====== Lecture Operations ======
    fun getLecturesByDay(day: String): StateFlow<List<LectureEntity>> =
        repo.getLecturesByDay(day).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertLecture(lecture: LectureEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.insertLecture(lecture)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة المحاضرة بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة المحاضرة", e)
                _errorMessage.value = "فشل إضافة المحاضرة: ${e.message}"
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

    // ====== Task Operations ======
    fun insertTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.insertTask(task)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة المهمة بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة المهمة", e)
                _errorMessage.value = "فشل إضافة المهمة: ${e.message}"
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                repo.updateTask(task)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث المهمة"
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                repo.deleteTask(task)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف المهمة"
            }
        }
    }

    fun toggleTaskDone(id: Long, isDone: Boolean) {
        viewModelScope.launch {
            try {
                repo.updateTaskDone(id, isDone)
            } catch (e: Exception) {
                _errorMessage.value = "فشل تحديث المهمة"
            }
        }
    }

    // ====== Note Operations ======
    fun insertNote(note: NoteEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.insertNote(note)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة الملاحظة بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة الملاحظة", e)
                _errorMessage.value = "فشل إضافة الملاحظة: ${e.message}"
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

    // ====== Exam Operations ======
    fun insertExam(exam: ExamEntity) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repo.insertExam(exam)
                _uiState.value = UiState.Success(Unit)
                _successMessage.value = "تم إضافة الامتحان بنجاح"
            } catch (e: Exception) {
                _uiState.value = UiState.Error("فشل إضافة الامتحان", e)
                _errorMessage.value = "فشل إضافة الامتحان: ${e.message}"
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
