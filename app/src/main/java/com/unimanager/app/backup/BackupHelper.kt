package com.unimanager.app.backup

import android.content.Context
import android.net.Uri
import com.unimanager.app.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

class BackupHelper(private val context: Context) {

    companion object {
        private const val BACKUP_VERSION = 2
        private const val FILE_NAME = "unimanager_backup.json"
    }

    /**
     * تصدير النسخة الاحتياطية
     */
    suspend fun exportBackup(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)

            val backupData = JSONObject().apply {
                put("version", BACKUP_VERSION)
                put("timestamp", System.currentTimeMillis())
                put("appVersion", context.packageName)

                // Export folders
                val foldersArray = JSONArray()
                db.folderDao().getAllFoldersSync().forEach { folder ->
                    foldersArray.put(JSONObject().apply {
                        put("id", folder.id)
                        put("name", folder.name)
                        put("description", folder.description)
                        put("color", folder.color)
                        put("parentId", folder.parentId)
                        put("createdAt", folder.createdAt)
                    })
                }
                put("folders", foldersArray)

                // Export files
                val filesArray = JSONArray()
                db.fileDao().getAllFilesSync().forEach { file ->
                    filesArray.put(JSONObject().apply {
                        put("id", file.id)
                        put("name", file.name)
                        put("extension", file.extension)
                        put("type", file.type)
                        put("mimeType", file.mimeType)
                        put("size", file.size)
                        put("folderId", file.folderId)
                        put("filePath", file.filePath)
                        put("isFavorite", file.isFavorite)
                        put("createdAt", file.createdAt)
                    })
                }
                put("files", filesArray)

                // Export tasks
                val tasksArray = JSONArray()
                db.taskDao().getAllTasksSync().forEach { task ->
                    tasksArray.put(JSONObject().apply {
                        put("id", task.id)
                        put("title", task.title)
                        put("description", task.description)
                        put("priority", task.priority)
                        put("dueDate", task.dueDate)
                        put("isDone", task.isDone)
                        put("createdAt", task.createdAt)
                    })
                }
                put("tasks", tasksArray)

                // Export notes
                val notesArray = JSONArray()
                db.noteDao().getAllNotesSync().forEach { note ->
                    notesArray.put(JSONObject().apply {
                        put("id", note.id)
                        put("title", note.title)
                        put("content", note.content)
                        put("updatedAt", note.updatedAt)
                    })
                }
                put("notes", notesArray)

                // Export exams
                val examsArray = JSONArray()
                db.examDao().getAllExamsSync().forEach { exam ->
                    examsArray.put(JSONObject().apply {
                        put("id", exam.id)
                        put("subject", exam.subject)
                        put("type", exam.type)
                        put("examDate", exam.examDate)
                        put("time", exam.time)
                        put("room", exam.room)
                        put("notes", exam.notes)
                    })
                }
                put("exams", examsArray)

                // Export lectures
                val lecturesArray = JSONArray()
                db.lectureDao().getAllLecturesSync().forEach { lecture ->
                    lecturesArray.put(JSONObject().apply {
                        put("id", lecture.id)
                        put("subject", lecture.subject)
                        put("doctor", lecture.doctor)
                        put("day", lecture.day)
                        put("timeFrom", lecture.timeFrom)
                        put("timeTo", lecture.timeTo)
                        put("room", lecture.room)
                    })
                }
                put("lectures", lecturesArray)
            }

            // Write to file
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(backupData.toString(2).toByteArray())
            } ?: throw IOException("Cannot open output stream")

            Result.success("تم تصدير النسخة الاحتياطية بنجاح")
        } catch (e: Exception) {
            android.util.Log.e("BackupHelper", "Export failed", e)
            Result.failure(e)
        }
    }

    /**
     * استيراد النسخة الاحتياطية
     * الإصلاح: التحقق من صحة البيانات أولاً ثم الاستيراد في transaction
     */
    suspend fun importBackup(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)

            // Step 1: قراءة البيانات أولاً
            val jsonData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: throw IOException("فشل في قراءة ملف النسخة الاحتياطية")

            // Step 2: التحقق من صحة JSON
            val backupData = try {
                JSONObject(jsonData)
            } catch (e: Exception) {
                throw IOException("ملف النسخة الاحتياطية تالف أو غير صالح")
            }

            val version = backupData.optInt("version", 1)
            if (version > BACKUP_VERSION) {
                throw IOException("إصدار النسخة الاحتياطية ($version) أحدث من التطبيق ($BACKUP_VERSION)")
            }

            // Step 3: Pre-parse all data قبل الحذف
            val folders = parseFolders(backupData.optJSONArray("folders"))
            val files = parseFiles(backupData.optJSONArray("files"))
            val tasks = parseTasks(backupData.optJSONArray("tasks"))
            val notes = parseNotes(backupData.optJSONArray("notes"))
            val exams = parseExams(backupData.optJSONArray("exams"))
            val lectures = parseLectures(backupData.optJSONArray("lectures"))

            // Step 4: الآن نحذف ونستورد في transaction
            db.runInTransaction {
                try {
                    // Clear existing data
                    db.folderDao().deleteAll()
                    db.fileDao().deleteAll()
                    db.taskDao().deleteAll()
                    db.noteDao().deleteAll()
                    db.examDao().deleteAll()
                    db.lectureDao().deleteAll()

                    // Import folders أولاً (لأن الملفات تعتمد عليها)
                    folders.forEach { db.folderDao().insertSync(it) }

                    // Import files
                    files.forEach { db.fileDao().insertSync(it) }

                    // Import tasks
                    tasks.forEach { db.taskDao().insertSync(it) }

                    // Import notes
                    notes.forEach { db.noteDao().insertSync(it) }

                    // Import exams
                    exams.forEach { db.examDao().insertSync(it) }

                    // Import lectures
                    lectures.forEach { db.lectureDao().insertSync(it) }
                } catch (e: Exception) {
                    throw IOException("فشل استيراد البيانات: ${e.message}")
                }
            }

            Result.success("تم استيراد النسخة الاحتياطية بنجاح")
        } catch (e: Exception) {
            android.util.Log.e("BackupHelper", "Import failed", e)
            Result.failure(e)
        }
    }

    // ====== Parse functions ======

    private fun parseFolders(array: JSONArray?): List<com.unimanager.app.data.entity.FolderEntity> {
        if (array == null) return emptyList()
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            com.unimanager.app.data.entity.FolderEntity(
                id = obj.optLong("id", 0),
                name = obj.getString("name"),
                description = obj.optString("description", ""),
                color = obj.optString("color", "#6366f1"),
                parentId = if (obj.has("parentId") && !obj.isNull("parentId"))
                    obj.getLong("parentId") else null,
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseFiles(array: JSONArray?): List<com.unimanager.app.data.entity.FileEntity> {
        if (array == null) return emptyList()
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            com.unimanager.app.data.entity.FileEntity(
                id = obj.optLong("id", 0),
                name = obj.getString("name"),
                extension = obj.getString("extension"),
                type = obj.getString("type"),
                mimeType = obj.getString("mimeType"),
                size = obj.optLong("size", 0),
                folderId = if (obj.has("folderId") && !obj.isNull("folderId"))
                    obj.getLong("folderId") else null,
                filePath = obj.optString("filePath", ""),
                isFavorite = obj.optBoolean("isFavorite", false),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseTasks(array: JSONArray?): List<com.unimanager.app.data.entity.TaskEntity> {
        if (array == null) return emptyList()
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            com.unimanager.app.data.entity.TaskEntity(
                id = obj.optLong("id", 0),
                title = obj.getString("title"),
                description = obj.optString("description", ""),
                priority = obj.optString("priority", "medium"),
                dueDate = if (obj.has("dueDate") && !obj.isNull("dueDate"))
                    obj.getString("dueDate") else null,
                isDone = obj.optBoolean("isDone", false),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseNotes(array: JSONArray?): List<com.unimanager.app.data.entity.NoteEntity> {
        if (array == null) return emptyList()
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            com.unimanager.app.data.entity.NoteEntity(
                id = obj.optLong("id", 0),
                title = obj.getString("title"),
                content = obj.getString("content"),
                updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseExams(array: JSONArray?): List<com.unimanager.app.data.entity.ExamEntity> {
        if (array == null) return emptyList()
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            com.unimanager.app.data.entity.ExamEntity(
                id = obj.optLong("id", 0),
                subject = obj.getString("subject"),
                type = obj.optString("type", "نصفي"),
                examDate = obj.getString("examDate"),
                time = obj.optString("time", ""),
                room = obj.optString("room", ""),
                notes = obj.optString("notes", "")
            )
        }
    }

    private fun parseLectures(array: JSONArray?): List<com.unimanager.app.data.entity.LectureEntity> {
        if (array == null) return emptyList()
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            com.unimanager.app.data.entity.LectureEntity(
                id = obj.optLong("id", 0),
                subject = obj.getString("subject"),
                doctor = obj.optString("doctor", ""),
                day = obj.getString("day"),
                timeFrom = obj.getString("timeFrom"),
                timeTo = obj.optString("timeTo", ""),
                room = obj.optString("room", "")
            )
        }
    }
}
