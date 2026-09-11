package com.unimanager.app.di

import android.content.Context
import androidx.room.Room
import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.dao.*
import com.unimanager.app.data.repository.AppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "uni_manager_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideFolderDao(database: AppDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    fun provideFileDao(database: AppDatabase): FileDao {
        return database.fileDao()
    }

    @Provides
    fun provideLectureDao(database: AppDatabase): LectureDao {
        return database.lectureDao()
    }

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideExamDao(database: AppDatabase): ExamDao {
        return database.examDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRepository(
        folderDao: FolderDao,
        fileDao: FileDao,
        lectureDao: LectureDao,
        taskDao: TaskDao,
        noteDao: NoteDao,
        examDao: ExamDao
    ): AppRepository {
        return AppRepository(
            folderDao, fileDao, lectureDao,
            taskDao, noteDao, examDao
        )
    }
}
