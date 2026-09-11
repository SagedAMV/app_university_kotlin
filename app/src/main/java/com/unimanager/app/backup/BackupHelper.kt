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
        private const val BACKUP_VERSION = 1
        private const val FILE_NAME = "unimanager_backup.json"
    }

    suspend fun exportBackup(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            
            val backupData = JSONObject().apply {
                put("version", BACKUP_VERSION)
                put("timestamp", System.currentTimeMillis())
                
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
            }
            
            Result.success("تم تصدير النسخة الاحتياطية بنجاح")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            
            // Read from file
            val jsonData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: throw IOException("Failed to read backup file")
            
            val backupData = JSONObject(jsonData)
            val version = backupData.optInt("version", 1)
            
            // Clear existing data
            db.folderDao().deleteAll()
            db.fileDao().deleteAll()
            db.taskDao().deleteAll()
            db.noteDao().deleteAll()
            db.examDao().deleteAll()
            db.lectureDao().deleteAll()
            
            // Import folders
            backupData.optJSONArray("folders")?.let { foldersArray ->
                for (i in 0 until foldersArray.length()) {
                    val folderObj = foldersArray.getJSONObject(i)
                    db.folderDao().insertSync(
                        com.unimanager.app.data.entity.FolderEntity(
                            id = folderObj.optLong("id", 0),
                            name = folderObj.getString("name"),
                            description = folderObj.optString("description", ""),
                            color = folderObj.optString("color", "#6366f1"),
                            parentId = if (folderObj.has("parentId") && !folderObj.isNull("parentId")) 
                                folderObj.getLong("parentId") else null,
                            createdAt = folderObj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            
            // Import files
            backupData.optJSONArray("files")?.let { filesArray ->
                for (i in 0 until filesArray.length()) {
                    val fileObj = filesArray.getJSONObject(i)
                    db.fileDao().insertSync(
                        com.unimanager.app.data.entity.FileEntity(
                            id = fileObj.optLong("id", 0),
                            name = fileObj.getString("name"),
                            extension = fileObj.getString("extension"),
                            type = fileObj.getString("type"),
                            mimeType = fileObj.getString("mimeType"),
                            size = fileObj.optLong("size", 0),
                            folderId = if (fileObj.has("folderId") && !fileObj.isNull("folderId"))
                                fileObj.getLong("folderId") else null,
                            filePath = fileObj.optString("filePath", ""),
                            isFavorite = fileObj.optBoolean("isFavorite", false),
                            createdAt = fileObj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            
            // Import tasks
            backupData.optJSONArray("tasks")?.let { tasksArray ->
                for (i in 0 until tasksArray.length()) {
                    val taskObj = tasksArray.getJSONObject(i)
                    db.taskDao().insertSync(
                        com.unimanager.app.data.entity.TaskEntity(
                            id = taskObj.optLong("id", 0),
                            title = taskObj.getString("title"),
                            description = taskObj.optString("description", ""),
                            priority = taskObj.optString("priority", "medium"),
                            dueDate = taskObj.optString("dueDate", null),
                            isDone = taskObj.optBoolean("isDone", false),
                            createdAt = taskObj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            
            // Import notes
            backupData.optJSONArray("notes")?.let { notesArray ->
                for (i in 0 until notesArray.length()) {
                    val noteObj = notesArray.getJSONObject(i)
                    db.noteDao().insertSync(
                        com.unimanager.app.data.entity.NoteEntity(
                            id = noteObj.optLong("id", 0),
                            title = noteObj.getString("title"),
                            content = noteObj.getString("content"),
                            updatedAt = noteObj.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            
            // Import exams
            backupData.optJSONArray("exams")?.let { examsArray ->
                for (i in 0 until examsArray.length()) {
                    val examObj = examsArray.getJSONObject(i)
                    db.examDao().insertSync(
                        com.unimanager.app.data.entity.ExamEntity(
                            id = examObj.optLong("id", 0),
                            subject = examObj.getString("subject"),
                            type = examObj.optString("type", "نصفي"),
                            examDate = examObj.getString("examDate"),
                            time = examObj.optString("time", ""),
                            room = examObj.optString("room", ""),
                            notes = examObj.optString("notes", "")
                        )
                    )
                }
            }
            
            // Import lectures
            backupData.optJSONArray("lectures")?.let { lecturesArray ->
                for (i in 0 until lecturesArray.length()) {
                    val lectureObj = lecturesArray.getJSONObject(i)
                    db.lectureDao().insertSync(
                        com.unimanager.app.data.entity.LectureEntity(
                            id = lectureObj.optLong("id", 0),
                            subject = lectureObj.getString("subject"),
                            doctor = lectureObj.optString("doctor", ""),
                            day = lectureObj.getString("day"),
                            timeFrom = lectureObj.getString("timeFrom"),
                            timeTo = lectureObj.optString("timeTo", ""),
                            room = lectureObj.optString("room", "")
                        )
                    )
                }
            }
            
            Result.success("تم استيراد النسخة الاحتياطية بنجاح")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
