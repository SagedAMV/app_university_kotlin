package com.unimanager.app.data.repository

import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.dao.*
import com.unimanager.app.data.entity.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

    // Folders
    fun getAllFolders(): Flow<List<FolderEntity>> = db.folderDao().getAllFolders()
    fun getRootFolders(): Flow<List<FolderEntity>> = db.folderDao().getRootFolders()
    fun getChildFolders(parentId: Long?): Flow<List<FolderEntity>> = db.folderDao().getChildFolders(parentId)
    suspend fun insertFolder(folder: FolderEntity): Long = db.folderDao().insert(folder)
    suspend fun updateFolder(folder: FolderEntity) = db.folderDao().update(folder)
    suspend fun deleteFolder(folder: FolderEntity) = db.folderDao().delete(folder)

    // Files
    fun getAllFiles(): Flow<List<FileEntity>> = db.fileDao().getAllFiles()
    fun getFilesInFolder(folderId: Long?): Flow<List<FileEntity>> = db.fileDao().getFilesInFolder(folderId)
    fun getFavoriteFiles(): Flow<List<FileEntity>> = db.fileDao().getFavoriteFiles()
    fun searchFiles(query: String): Flow<List<FileEntity>> = db.fileDao().searchFiles(query)
    suspend fun insertFile(file: FileEntity): Long = db.fileDao().insert(file)
    suspend fun updateFile(file: FileEntity) = db.fileDao().update(file)
    suspend fun deleteFile(file: FileEntity) = db.fileDao().delete(file)
    suspend fun updateFavorite(id: Long, isFavorite: Boolean) = db.fileDao().updateFavorite(id, isFavorite)
    fun getFileCount(): Flow<Int> = db.fileDao().getFileCount()
    fun getTotalSize(): Flow<Long> = db.fileDao().getTotalSize()

    // Lectures
    fun getAllLectures(): Flow<List<LectureEntity>> = db.lectureDao().getAllLectures()
    fun getLecturesByDay(day: String): Flow<List<LectureEntity>> = db.lectureDao().getLecturesByDay(day)
    suspend fun insertLecture(lecture: LectureEntity): Long = db.lectureDao().insert(lecture)
    suspend fun updateLecture(lecture: LectureEntity) = db.lectureDao().update(lecture)
    suspend fun deleteLecture(lecture: LectureEntity) = db.lectureDao().delete(lecture)
    fun getLectureCount(): Flow<Int> = db.lectureDao().getLectureCount()

    // Tasks
    fun getAllTasks(): Flow<List<TaskEntity>> = db.taskDao().getAllTasks()
    fun getPendingTasks(): Flow<List<TaskEntity>> = db.taskDao().getPendingTasks()
    suspend fun insertTask(task: TaskEntity): Long = db.taskDao().insert(task)
    suspend fun updateTask(task: TaskEntity) = db.taskDao().update(task)
    suspend fun deleteTask(task: TaskEntity) = db.taskDao().delete(task)
    suspend fun updateTaskDone(id: Long, isDone: Boolean) = db.taskDao().updateDone(id, isDone)
    fun getPendingTaskCount(): Flow<Int> = db.taskDao().getPendingCount()

    // Notes
    fun getAllNotes(): Flow<List<NoteEntity>> = db.noteDao().getAllNotes()
    suspend fun insertNote(note: NoteEntity): Long = db.noteDao().insert(note)
    suspend fun updateNote(note: NoteEntity) = db.noteDao().update(note)
    suspend fun deleteNote(note: NoteEntity) = db.noteDao().delete(note)

    // Exams
    fun getAllExams(): Flow<List<ExamEntity>> = db.examDao().getAllExams()
    fun getUpcomingExams(): Flow<List<ExamEntity>> = db.examDao().getUpcomingExams()
    suspend fun insertExam(exam: ExamEntity): Long = db.examDao().insert(exam)
    suspend fun updateExam(exam: ExamEntity) = db.examDao().update(exam)
    suspend fun deleteExam(exam: ExamEntity) = db.examDao().delete(exam)
    fun getUpcomingExamCount(): Flow<Int> = db.examDao().getUpcomingCount()
}
