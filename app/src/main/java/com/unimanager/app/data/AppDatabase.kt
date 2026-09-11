package com.unimanager.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.unimanager.app.data.dao.*
import com.unimanager.app.data.entity.*

@Database(
    entities = [
        FolderEntity::class,
        FileEntity::class,
        LectureEntity::class,
        TaskEntity::class,
        NoteEntity::class,
        ExamEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun folderDao(): FolderDao
    abstract fun fileDao(): FileDao
    abstract fun lectureDao(): LectureDao
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun examDao(): ExamDao

    companion object {
        // Migration 1 → 2: إضافة indices للملفات والمجلدات
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_files_name ON files(name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_files_folderId ON files(folderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_files_createdAt ON files(createdAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_files_isFavorite ON files(isFavorite)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_folders_parentId ON folders(parentId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_folders_createdAt ON folders(createdAt)")
            }
        }

        // Migration 2 → 3: إضافة indices للمهام والامتحانات والمحاضرات
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Tasks indices
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_isDone ON tasks(isDone)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_priority ON tasks(priority)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_dueDate ON tasks(dueDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_createdAt ON tasks(createdAt)")

                // Exams indices
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exams_examDate ON exams(examDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exams_type ON exams(type)")

                // Lectures indices
                db.execSQL("CREATE INDEX IF NOT EXISTS index_lectures_day ON lectures(day)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_lectures_timeFrom ON lectures(timeFrom)")
            }
        }
    }
}
