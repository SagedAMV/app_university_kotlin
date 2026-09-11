package com.unimanager.app.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration Tests for DAOs
 * These tests require Android instrumentation
 */
@RunWith(AndroidJUnit4::class)
class DaoIntegrationTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveTask() = runTest {
        val taskDao = database.taskDao()
        val task = TaskEntity(
            title = "Test Task",
            description = "Test Description",
            priority = "high"
        )

        val taskId = taskDao.insert(task)
        assertTrue(taskId > 0)

        val tasks = taskDao.getAllTasks().first()
        assertTrue(tasks.isNotEmpty())
        assertEquals("Test Task", tasks[0].title)
    }

    @Test
    fun insertAndRetrieveExam() = runTest {
        val examDao = database.examDao()
        val exam = ExamEntity(
            subject = "Mathematics",
            type = "نصفي",
            examDate = "2026-06-15",
            time = "10:00",
            room = "A101"
        )

        val examId = examDao.insert(exam)
        assertTrue(examId > 0)

        val exams = examDao.getAllExams().first()
        assertTrue(exams.isNotEmpty())
        assertEquals("Mathematics", exams[0].subject)
    }

    @Test
    fun insertAndRetrieveNote() = runTest {
        val noteDao = database.noteDao()
        val note = NoteEntity(
            title = "Test Note",
            content = "This is a test note"
        )

        val noteId = noteDao.insert(note)
        assertTrue(noteId > 0)

        val notes = noteDao.getAllNotes().first()
        assertTrue(notes.isNotEmpty())
        assertEquals("Test Note", notes[0].title)
    }

    @Test
    fun insertAndRetrieveFolder() = runTest {
        val folderDao = database.folderDao()
        val folder = FolderEntity(
            name = "Test Folder",
            description = "A test folder",
            color = "#6366f1"
        )

        val folderId = folderDao.insert(folder)
        assertTrue(folderId > 0)

        val folders = folderDao.getAllFolders().first()
        assertTrue(folders.isNotEmpty())
        assertEquals("Test Folder", folders[0].name)
    }

    @Test
    fun insertAndRetrieveFile() = runTest {
        val fileDao = database.fileDao()
        val file = FileEntity(
            name = "test.pdf",
            extension = "pdf",
            type = "pdf",
            mimeType = "application/pdf",
            size = 1024,
            folderId = null,
            filePath = "/path/to/file"
        )

        val fileId = fileDao.insert(file)
        assertTrue(fileId > 0)

        val files = fileDao.getAllFiles().first()
        assertTrue(files.isNotEmpty())
        assertEquals("test.pdf", files[0].name)
    }

    @Test
    fun toggleTaskDone() = runTest {
        val taskDao = database.taskDao()
        val task = TaskEntity(title = "Toggle Test")

        val taskId = taskDao.insert(task)
        val tasks = taskDao.getAllTasks().first()
        assertFalse(tasks[0].isDone)

        taskDao.updateDone(taskId, true)
        val updatedTasks = taskDao.getAllTasks().first()
        assertTrue(updatedTasks[0].isDone)
    }

    @Test
    fun toggleFileFavorite() = runTest {
        val fileDao = database.fileDao()
        val file = FileEntity(
            name = "fav.pdf",
            extension = "pdf",
            type = "pdf",
            mimeType = "application/pdf",
            size = 512,
            folderId = null,
            filePath = ""
        )

        val fileId = fileDao.insert(file)
        fileDao.updateFavorite(fileId, true)

        val favorites = fileDao.getFavoriteFiles().first()
        assertTrue(favorites.isNotEmpty())
        assertEquals("fav.pdf", favorites[0].name)
    }

    @Test
    fun searchFiles() = runTest {
        val fileDao = database.fileDao()
        fileDao.insert(FileEntity(name = "math notes.pdf", extension = "pdf", type = "pdf", mimeType = "application/pdf", size = 100, folderId = null, filePath = ""))
        fileDao.insert(FileEntity(name = "physics notes.pdf", extension = "pdf", type = "pdf", mimeType = "application/pdf", size = 200, folderId = null, filePath = ""))
        fileDao.insert(FileEntity(name = "math exam.doc", extension = "doc", type = "doc", mimeType = "application/msword", size = 300, folderId = null, filePath = ""))

        val results = fileDao.searchFiles("math").first()
        assertEquals(2, results.size)
    }

    @Test
    fun deleteAllClearsDatabase() = runTest {
        val taskDao = database.taskDao()
        taskDao.insert(TaskEntity(title = "Task 1"))
        taskDao.insert(TaskEntity(title = "Task 2"))

        var tasks = taskDao.getAllTasks().first()
        assertEquals(2, tasks.size)

        taskDao.deleteAll()
        tasks = taskDao.getAllTasks().first()
        assertEquals(0, tasks.size)
    }

    @Test
    fun nestedFolders() = runTest {
        val folderDao = database.folderDao()
        val parent = FolderEntity(name = "Parent")
        val parentId = folderDao.insert(parent)

        val child = FolderEntity(name = "Child", parentId = parentId)
        folderDao.insert(child)

        val children = folderDao.getChildFolders(parentId).first()
        assertEquals(1, children.size)
        assertEquals("Child", children[0].name)
    }
}
