package com.unimanager.app.data.repository

import com.unimanager.app.data.dao.*
import com.unimanager.app.data.entity.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val folderDao: FolderDao,
    private val fileDao: FileDao,
    private val lectureDao: LectureDao,
    private val taskDao: TaskDao,
    private val noteDao: NoteDao,
    private val examDao: ExamDao
) {

    // Folders
    fun getAllFolders(): Flow<List<FolderEntity>> = folderDao.getAllFolders()
    fun getRootFolders(): Flow<List<FolderEntity>> = folderDao.getRootFolders()
    fun getChildFolders(parentId: Long?): Flow<List<FolderEntity>> = folderDao.getChildFolders(parentId)
    suspend fun insertFolder(folder: FolderEntity): Long = folderDao.insert(folder)
    suspend fun updateFolder(folder: FolderEntity) = folderDao.update(folder)
    suspend fun deleteFolder(folder: FolderEntity) = folderDao.delete(folder)
    suspend fun getAllFoldersSync(): List<FolderEntity> = folderDao.getAllFoldersSync()

    // Files
    fun getAllFiles(): Flow<List<FileEntity>> = fileDao.getAllFiles()
    fun getFilesInFolder(folderId: Long?): Flow<List<FileEntity>> = fileDao.getFilesInFolder(folderId)
    fun getFavoriteFiles(): Flow<List<FileEntity>> = fileDao.getFavoriteFiles()
    fun searchFiles(query: String): Flow<List<FileEntity>> = fileDao.searchFiles(query)
    suspend fun insertFile(file: FileEntity): Long = fileDao.insert(file)
    suspend fun updateFile(file: FileEntity) = fileDao.update(file)
    suspend fun deleteFile(file: FileEntity) = fileDao.delete(file)
    suspend fun updateFavorite(id: Long, isFavorite: Boolean) = fileDao.updateFavorite(id, isFavorite)
    fun getFileCount(): Flow<Int> = fileDao.getFileCount()
    fun getTotalSize(): Flow<Long> = fileDao.getTotalSize()
    suspend fun getAllFilesSync(): List<FileEntity> = fileDao.getAllFilesSync()

    // Lectures
    fun getAllLectures(): Flow<List<LectureEntity>> = lectureDao.getAllLectures()
    fun getLecturesByDay(day: String): Flow<List<LectureEntity>> = lectureDao.getLecturesByDay(day)
    suspend fun insertLecture(lecture: LectureEntity): Long = lectureDao.insert(lecture)
    suspend fun updateLecture(lecture: LectureEntity) = lectureDao.update(lecture)
    suspend fun deleteLecture(lecture: LectureEntity) = lectureDao.delete(lecture)
    fun getLectureCount(): Flow<Int> = lectureDao.getLectureCount()
    suspend fun getAllLecturesSync(): List<LectureEntity> = lectureDao.getAllLecturesSync()

    // Tasks
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    fun getPendingTasks(): Flow<List<TaskEntity>> = taskDao.getPendingTasks()
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insert(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.update(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.delete(task)
    suspend fun updateTaskDone(id: Long, isDone: Boolean) = taskDao.updateDone(id, isDone)
    fun getPendingTaskCount(): Flow<Int> = taskDao.getPendingCount()
    suspend fun getAllTasksSync(): List<TaskEntity> = taskDao.getAllTasksSync()

    // Notes
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insert(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.update(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.delete(note)
    fun getNoteCount(): Flow<Int> = noteDao.getNoteCount()
    suspend fun getAllNotesSync(): List<NoteEntity> = noteDao.getAllNotesSync()

    // Exams
    fun getAllExams(): Flow<List<ExamEntity>> = examDao.getAllExams()
    fun getUpcomingExams(): Flow<List<ExamEntity>> = examDao.getUpcomingExams()
    suspend fun insertExam(exam: ExamEntity): Long = examDao.insert(exam)
    suspend fun updateExam(exam: ExamEntity) = examDao.update(exam)
    suspend fun deleteExam(exam: ExamEntity) = examDao.delete(exam)
    fun getUpcomingExamCount(): Flow<Int> = examDao.getUpcomingExamCount()
    suspend fun getAllExamsSync(): List<ExamEntity> = examDao.getAllExamsSync()
}
