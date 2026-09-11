package com.unimanager.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unimanager.app.data.entity.*
import com.unimanager.app.data.repository.AppRepository
import com.unimanager.app.util.ExamNotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    application: Application,
    private val repository: AppRepository,
    private val notificationScheduler: ExamNotificationScheduler
) : AndroidViewModel(application) {

    // ====== Data Flows (StateFlow for performance) ======

    // Folders
    val rootFolders: StateFlow<List<FolderEntity>> = repository.getRootFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Files
    val allFiles: StateFlow<List<FileEntity>> = repository.getAllFiles()
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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // ====== Search ======
    // (search is done in-memory via filter on StateFlow values)

    // ====== Cached per-key flows ======
    // إعادة استخدام نفس الـ StateFlow لكل مفتاح بدل إنشاء تدفق جديد
    // عند كل إعادة تركيب (recomposition).
    private val childFoldersFlows = mutableMapOf<Long?, StateFlow<List<FolderEntity>>>()
    private val filesInFolderFlows = mutableMapOf<Long?, StateFlow<List<FileEntity>>>()
    private val lecturesByDayFlows = mutableMapOf<String, StateFlow<List<LectureEntity>>>()

    // ====== Folder Operations ======
    fun getChildFolders(parentId: Long?): StateFlow<List<FolderEntity>> =
        childFoldersFlows.getOrPut(parentId) {
            repository.getChildFolders(parentId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun insertFolder(folder: FolderEntity) {
        viewModelScope.launch {
            try {
                repository.insertFolder(folder)
                _successMessage.value = "تم إضافة المجلد بنجاح"
            } catch (e: Exception) {
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
                // احسب شجرة المجلدات الفرعية قبل الحذف لحذف الملفات الفعلية أيضًا
                // (قاعدة البيانات تحذف الصفوف عبر CASCADE لكنها لا تحذف ملفات التخزين)
                val allFolders = repository.getAllFoldersOnce()
                val subtreeIds = mutableSetOf(folder.id)
                var grew = true
                while (grew) {
                    grew = false
                    for (f in allFolders) {
                        if (f.parentId in subtreeIds && subtreeIds.add(f.id)) grew = true
                    }
                }
                repository.getAllFilesOnce()
                    .filter { it.folderId in subtreeIds }
                    .forEach { deletePhysicalFile(it.filePath) }

                repository.deleteFolder(folder)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف المجلد"
                android.util.Log.e("AppViewModel", "Failed to delete folder", e)
            }
        }
    }

    // ====== File Operations ======
    fun getFilesInFolder(folderId: Long?): StateFlow<List<FileEntity>> =
        filesInFolderFlows.getOrPut(folderId) {
            repository.getFilesInFolder(folderId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun insertFile(file: FileEntity) {
        viewModelScope.launch {
            try {
                repository.insertFile(file)
                _successMessage.value = "تم إضافة الملف بنجاح"
            } catch (e: Exception) {
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
                // احذف النسخة الفعلية المنسوخة داخل تخزين التطبيق لمنع تسرّب المساحة
                deletePhysicalFile(file.filePath)
            } catch (e: Exception) {
                _errorMessage.value = "فشل حذف الملف"
                android.util.Log.e("AppViewModel", "Failed to delete file", e)
            }
        }
    }

    /** حذف ملف فعلي من التخزين الداخلي بصمت (فشله لا يُفشل حذف السجل) */
    private fun deletePhysicalFile(path: String) {
        if (path.isBlank()) return
        runCatching { java.io.File(path).takeIf { it.isFile }?.delete() }
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
        lecturesByDayFlows.getOrPut(day) {
            repository.getLecturesByDay(day)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    fun insertLecture(lecture: LectureEntity) {
        viewModelScope.launch {
            try {
                repository.insertLecture(lecture)
                _successMessage.value = "تم إضافة المحاضرة بنجاح"
            } catch (e: Exception) {
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
                repository.insertTask(task)
                _successMessage.value = "تم إضافة المهمة بنجاح"

                // جدولة الإشعارات
                if (!task.dueDate.isNullOrBlank()) {
                    notificationScheduler.scheduleTaskNotification(task)
                }
            } catch (e: Exception) {
                _errorMessage.value = "فشل إضافة المهمة: ${e.message}"
                android.util.Log.e("AppViewModel", "Failed to insert task", e)
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                repository.updateTask(task)
                // إلغاء التذكيرات القديمة وجدولة الجديدة حسب تاريخ الاستحقاق الجديد
                notificationScheduler.cancelTaskNotifications(task.id)
                if (!task.dueDate.isNullOrBlank()) {
                    notificationScheduler.scheduleTaskNotification(task)
                }
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
                notificationScheduler.cancelTaskNotifications(task.id)
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
                repository.insertNote(note)
                _successMessage.value = "تم إضافة الملاحظة بنجاح"
            } catch (e: Exception) {
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
                repository.insertExam(exam)
                _successMessage.value = "تم إضافة الامتحان بنجاح"

                // جدولة الإشعارات
                notificationScheduler.scheduleExamNotification(exam)
            } catch (e: Exception) {
                _errorMessage.value = "فشل إضافة الامتحان: ${e.message}"
                android.util.Log.e("AppViewModel", "Failed to insert exam", e)
            }
        }
    }

    fun updateExam(exam: ExamEntity) {
        viewModelScope.launch {
            try {
                repository.updateExam(exam)
                // إلغاء التذكيرات القديمة وجدولة الأخرى حسب التاريخ/الوقت الجديد
                notificationScheduler.cancelExamNotifications(exam.id)
                notificationScheduler.scheduleExamNotification(exam)
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
                notificationScheduler.cancelExamNotifications(exam.id)
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

}
