package com.unimanager.app.backup

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.entity.FolderEntity
import com.unimanager.app.data.entity.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented test يثبت أن التصدير ثم الاستيراد عبر BackupHelper
 * ينعكس فعليًا على تدفقات Room المراقَبة (نفس قاعدة البيانات المحقونة).
 */
@RunWith(AndroidJUnit4::class)
class BackupHelperTest {

    private lateinit var database: AppDatabase
    private lateinit var helper: BackupHelper
    private lateinit var backupFile: File

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        helper = BackupHelper(context, database)
        backupFile = File.createTempFile("backup_test", ".json", context.cacheDir)
    }

    @After
    fun teardown() {
        database.close()
        backupFile.delete()
    }

    @Test
    fun exportThenImport_reflectsInObservedFlows() = runTest {
        // Seed data
        database.folderDao().insert(FolderEntity(name = "Folder A"))
        database.taskDao().insert(TaskEntity(title = "Task A"))
        val uri = Uri.fromFile(backupFile)

        // Export
        val exportResult = helper.exportBackup(uri)
        assertTrue(exportResult.isSuccess)
        assertTrue(backupFile.length() > 0)

        // Wipe database
        database.folderDao().deleteAll()
        database.taskDao().deleteAll()
        assertEquals(0, database.folderDao().getAllFolders().first().size)

        // Re-import
        val importResult = helper.importBackup(uri)
        assertTrue(importResult.exceptionOrNull()?.message ?: "import failed", importResult.isSuccess)

        // Changes must be visible through the same DB instance the UI observes
        val folders = database.folderDao().getAllFolders().first()
        val tasks = database.taskDao().getAllTasks().first()
        assertEquals(1, folders.size)
        assertEquals("Folder A", folders[0].name)
        assertEquals(1, tasks.size)
        assertEquals("Task A", tasks[0].title)
    }

    @Test
    fun importCorruptFile_failsGracefully() = runTest {
        backupFile.writeText("{ this is not valid json")
        val result = helper.importBackup(Uri.fromFile(backupFile))
        assertTrue(result.isFailure)
    }
}
