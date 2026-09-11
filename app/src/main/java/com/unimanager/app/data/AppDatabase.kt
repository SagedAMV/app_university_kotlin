package com.unimanager.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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
    version = 2,
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
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2: Add indices
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create indices for files table
                db.execSQL("CREATE INDEX IF NOT EXISTS index_files_name ON files(name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_files_folderId ON files(folderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_files_createdAt ON files(createdAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_files_isFavorite ON files(isFavorite)")

                // Create indices for folders table
                db.execSQL("CREATE INDEX IF NOT EXISTS index_folders_parentId ON folders(parentId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_folders_createdAt ON folders(createdAt)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "uni_manager_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
