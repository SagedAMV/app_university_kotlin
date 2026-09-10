package com.unimanager.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unimanager.app.UniApplication
import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.entity.*
import com.unimanager.app.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = AppRepository(db)

    // Folders
    val allFolders: Flow<List<FolderEntity>> = repo.getAllFolders()
    val rootFolders: Flow<List<FolderEntity>> = repo.getRootFolders()

    // Files
    val allFiles: Flow<List<FileEntity>> = repo.getAllFiles()
    val favoriteFiles: Flow<List<FileEntity>> = repo.getFavoriteFiles()
    val fileCount: Flow<Int> = repo.getFileCount()
    val totalSize: Flow<Long> = repo.getTotalSize()

    // Lectures
    val allLectures: Flow<List<LectureEntity>> = repo.getAllLectures()
    val lectureCount: Flow<Int> = repo.getLectureCount()

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = repo.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = repo.getPendingTasks()
    val pendingTaskCount: Flow<Int> = repo.getPendingTaskCount()

    // Notes
    val allNotes: Flow<List<NoteEntity>> = repo.getAllNotes()

    // Exams
    val allExams: Flow<List<ExamEntity>> = repo.getAllExams()
    val upcomingExams: Flow<List<ExamEntity>> = repo.getUpcomingExams()
    val upcomingExamCount: Flow<Int> = repo.getUpcomingExamCount()

    // Search
    fun searchFiles(query: String): Flow<List<FileEntity>> = repo.searchFiles(query)

    // Folder operations
    fun getChildFolders(parentId: Long?): Flow<List<FolderEntity>> = repo.getChildFolders(parentId)
    fun insertFolder(folder: FolderEntity) = viewModelScope.launch { repo.insertFolder(folder) }
    fun updateFolder(folder: FolderEntity) = viewModelScope.launch { repo.updateFolder(folder) }
    fun deleteFolder(folder: FolderEntity) = viewModelScope.launch { repo.deleteFolder(folder) }

    // File operations
    fun getFilesInFolder(folderId: Long?): Flow<List<FileEntity>> = repo.getFilesInFolder(folderId)
    fun insertFile(file: FileEntity) = viewModelScope.launch { repo.insertFile(file) }
    fun updateFile(file: FileEntity) = viewModelScope.launch { repo.updateFile(file) }
    fun deleteFile(file: FileEntity) = viewModelScope.launch { repo.deleteFile(file) }
    fun toggleFavorite(id: Long, isFavorite: Boolean) = viewModelScope.launch { repo.updateFavorite(id, isFavorite) }

    // Lecture operations
    fun getLecturesByDay(day: String): Flow<List<LectureEntity>> = repo.getLecturesByDay(day)
    fun insertLecture(lecture: LectureEntity) = viewModelScope.launch { repo.insertLecture(lecture) }
    fun updateLecture(lecture: LectureEntity) = viewModelScope.launch { repo.updateLecture(lecture) }
    fun deleteLecture(lecture: LectureEntity) = viewModelScope.launch { repo.deleteLecture(lecture) }

    // Task operations
    fun insertTask(task: TaskEntity) = viewModelScope.launch { repo.insertTask(task) }
    fun updateTask(task: TaskEntity) = viewModelScope.launch { repo.updateTask(task) }
    fun deleteTask(task: TaskEntity) = viewModelScope.launch { repo.deleteTask(task) }
    fun toggleTaskDone(id: Long, isDone: Boolean) = viewModelScope.launch { repo.updateTaskDone(id, isDone) }

    // Note operations
    fun insertNote(note: NoteEntity) = viewModelScope.launch { repo.insertNote(note) }
    fun updateNote(note: NoteEntity) = viewModelScope.launch { repo.updateNote(note) }
    fun deleteNote(note: NoteEntity) = viewModelScope.launch { repo.deleteNote(note) }

    // Exam operations
    fun insertExam(exam: ExamEntity) = viewModelScope.launch { repo.insertExam(exam) }
    fun updateExam(exam: ExamEntity) = viewModelScope.launch { repo.updateExam(exam) }
    fun deleteExam(exam: ExamEntity) = viewModelScope.launch { repo.deleteExam(exam) }

    // Dashboard data
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
