package com.unimanager.app.data.repository

import android.util.Log
import com.unimanager.app.data.dao.*
import com.unimanager.app.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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

    /**
     * إصلاح كراش (التدقيق الرابع): تدفقات Room (Flow) تُشغَّل داخل `stateIn` في الـ ViewModel، وأي
     * استثناء غير متوقع أثناء تنفيذ الاستعلام (اتصال DB مقطوع، قيد صار غير صالح، إلخ) كان يُسقط
     * الكوروتين المُجمِّعة للتدفق بصمت خارج أي try/catch في الواجهة، مما يُظهر للمستخدم كراش فوري
     * عند فتح الشاشة (تحديداً تبويب الملفات الذي يستمع لعدة تدفقات دفعة واحدة). كل تدفقات القوائم
     * والعدادات هنا محمية الآن بـ [catch] تُسجّل الخطأ وتُصدر قيمة آمنة (قائمة فارغة/صفر) بدل إسقاط
     * الشاشة بالكامل.
     */
    private fun <T> Flow<T>.safe(tag: String, fallback: T): Flow<T> = catch { e ->
        Log.e("AppRepository", "تدفق $tag فشل، سيُستخدم قيمة احتياطية", e)
        emit(fallback)
    }

    // Folders
    fun getRootFolders(): Flow<List<FolderEntity>> = folderDao.getRootFolders().safe("getRootFolders", emptyList())
    fun getChildFolders(parentId: Long?): Flow<List<FolderEntity>> =
        folderDao.getChildFolders(parentId).safe("getChildFolders", emptyList())
    suspend fun getAllFoldersOnce(): List<FolderEntity> = folderDao.getAllFoldersSync()
    suspend fun insertFolder(folder: FolderEntity): Long = folderDao.insert(folder)
    suspend fun updateFolder(folder: FolderEntity) = folderDao.update(folder)
    suspend fun deleteFolder(folder: FolderEntity) = folderDao.delete(folder)

    // Files
    fun getAllFiles(): Flow<List<FileEntity>> = fileDao.getAllFiles().safe("getAllFiles", emptyList())
    fun getFilesInFolder(folderId: Long?): Flow<List<FileEntity>> =
        fileDao.getFilesInFolder(folderId).safe("getFilesInFolder", emptyList())
    suspend fun getAllFilesOnce(): List<FileEntity> = fileDao.getAllFilesSync()
    fun getFavoriteFiles(): Flow<List<FileEntity>> = fileDao.getFavoriteFiles().safe("getFavoriteFiles", emptyList())
    suspend fun insertFile(file: FileEntity): Long = fileDao.insert(file)
    suspend fun updateFile(file: FileEntity) = fileDao.update(file)
    suspend fun deleteFile(file: FileEntity) = fileDao.delete(file)
    suspend fun updateFavorite(id: Long, isFavorite: Boolean) = fileDao.updateFavorite(id, isFavorite)
    fun getFileCount(): Flow<Int> = fileDao.getFileCount().safe("getFileCount", 0)
    fun getTotalSize(): Flow<Long> = fileDao.getTotalSize().safe("getTotalSize", 0L)

    // Lectures
    fun getAllLectures(): Flow<List<LectureEntity>> = lectureDao.getAllLectures().safe("getAllLectures", emptyList())
    fun getLecturesByDay(day: String): Flow<List<LectureEntity>> = lectureDao.getLecturesByDay(day).safe("getLecturesByDay", emptyList())
    suspend fun insertLecture(lecture: LectureEntity): Long = lectureDao.insert(lecture)
    suspend fun updateLecture(lecture: LectureEntity) = lectureDao.update(lecture)
    suspend fun deleteLecture(lecture: LectureEntity) = lectureDao.delete(lecture)
    fun getLectureCount(): Flow<Int> = lectureDao.getLectureCount().safe("getLectureCount", 0)

    // Tasks
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks().safe("getAllTasks", emptyList())
    fun getPendingTasks(): Flow<List<TaskEntity>> = taskDao.getPendingTasks().safe("getPendingTasks", emptyList())
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insert(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.update(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.delete(task)
    suspend fun updateTaskDone(id: Long, isDone: Boolean) = taskDao.updateDone(id, isDone)
    fun getPendingTaskCount(): Flow<Int> = taskDao.getPendingCount().safe("getPendingTaskCount", 0)

    // Notes
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes().safe("getAllNotes", emptyList())
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insert(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.update(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.delete(note)

    // Exams
    fun getAllExams(): Flow<List<ExamEntity>> = examDao.getAllExams().safe("getAllExams", emptyList())
    fun getUpcomingExams(): Flow<List<ExamEntity>> = examDao.getUpcomingExams().safe("getUpcomingExams", emptyList())
    suspend fun insertExam(exam: ExamEntity): Long = examDao.insert(exam)
    suspend fun updateExam(exam: ExamEntity) = examDao.update(exam)
    suspend fun deleteExam(exam: ExamEntity) = examDao.delete(exam)
    fun getUpcomingExamCount(): Flow<Int> = examDao.getUpcomingExamCount().safe("getUpcomingExamCount", 0)
}
