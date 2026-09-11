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
    fun getRootFolders(): Flow<List<FolderEntity>> = folderDao.getRootFolders()
    fun getChildFolders(parentId: Long?): Flow<List<FolderEntity>> = folderDao.getChildFolders(parentId)
    suspend fun getAllFoldersOnce(): List<FolderEntity> = folderDao.getAllFoldersSync()
    suspend fun insertFolder(folder: FolderEntity): Long = folderDao.insert(folder)
    suspend fun updateFolder(folder: FolderEntity) = folderDao.update(folder)
    suspend fun deleteFolder(folder: FolderEntity) = folderDao.delete(folder)

    // Files
    fun getAllFiles(): Flow<List<FileEntity>> = fileDao.getAllFiles()
    fun getFilesInFolder(folderId: Long?): Flow<List<FileEntity>> = fileDao.getFilesInFolder(folderId)
    suspend fun getAllFilesOnce(): List<FileEntity> = fileDao.getAllFilesSync()
    fun getFavoriteFiles(): Flow<List<FileEntity>> = fileDao.getFavoriteFiles()
    suspend fun insertFile(file: FileEntity): Long = fileDao.insert(file)
    suspend fun updateFile(file: FileEntity) = fileDao.update(file)
    suspend fun deleteFile(file: FileEntity) = fileDao.delete(file)
    suspend fun updateFavorite(id: Long, isFavorite: Boolean) = fileDao.updateFavorite(id, isFavorite)
    fun getFileCount(): Flow<Int> = fileDao.getFileCount()
    fun getTotalSize(): Flow<Long> = fileDao.getTotalSize()

    // Lectures
    fun getAllLectures(): Flow<List<LectureEntity>> = lectureDao.getAllLectures()
    fun getLecturesByDay(day: String): Flow<List<LectureEntity>> = lectureDao.getLecturesByDay(day)
    suspend fun insertLecture(lecture: LectureEntity): Long = lectureDao.insert(lecture)
    suspend fun updateLecture(lecture: LectureEntity) = lectureDao.update(lecture)
    suspend fun deleteLecture(lecture: LectureEntity) = lectureDao.delete(lecture)
    fun getLectureCount(): Flow<Int> = lectureDao.getLectureCount()

    // Tasks
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    fun getPendingTasks(): Flow<List<TaskEntity>> = taskDao.getPendingTasks()
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insert(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.update(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.delete(task)
    suspend fun updateTaskDone(id: Long, isDone: Boolean) = taskDao.updateDone(id, isDone)
    fun getPendingTaskCount(): Flow<Int> = taskDao.getPendingCount()

    // Notes
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insert(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.update(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.delete(note)

    // Exams
    fun getAllExams(): Flow<List<ExamEntity>> = examDao.getAllExams()
    fun getUpcomingExams(): Flow<List<ExamEntity>> = examDao.getUpcomingExams()
    suspend fun insertExam(exam: ExamEntity): Long = examDao.insert(exam)
    suspend fun updateExam(exam: ExamEntity) = examDao.update(exam)
    suspend fun deleteExam(exam: ExamEntity) = examDao.delete(exam)
    fun getUpcomingExamCount(): Flow<Int> = examDao.getUpcomingExamCount()
}
