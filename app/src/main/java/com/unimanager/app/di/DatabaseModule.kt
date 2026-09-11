package com.unimanager.app.di

import android.content.Context
import androidx.room.Room
import com.unimanager.app.data.AppDatabase
import com.unimanager.app.data.dao.*
import com.unimanager.app.util.ThemePreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * المصدر الوحيد لقاعدة البيانات عبر التطبيق كله (بما فيه BackupHelper).
     * كل الترقيات مغطّاة بهجرات صريحة تحفظ بيانات المستخدم.
     * لا نستخدم fallbackToDestructiveMigration في الإنتاج حتى لا تُحذف بيانات
     * المستخدم المحلية بصمت عند نسيان كتابة هجرة لأي إصدار جديد.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "uni_manager_db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4
            )
            .build()
    }

    @Provides
    fun provideFolderDao(database: AppDatabase): FolderDao = database.folderDao()

    @Provides
    fun provideFileDao(database: AppDatabase): FileDao = database.fileDao()

    @Provides
    fun provideLectureDao(database: AppDatabase): LectureDao = database.lectureDao()

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideExamDao(database: AppDatabase): ExamDao = database.examDao()

    // ملاحظة إصلاح حرجة (يمنع البناء): لا يوجد @Provides لـ AppRepository هنا عن قصد.
    // AppRepository معرَّفة بـ "@Inject constructor" في ملفها (data/repository/AppRepository.kt)
    // وكل الـ DAOs التي تحتاجها متوفرة أعلاه عبر @Provides. وجود @Provides يدوي إضافي لنفس
    // النوع AppRepository (كما كان في هذا الملف سابقًا عبر provideRepository) بينما الفئة نفسها
    // تملك "@Inject constructor" يتسبب حتميًا في خطأ ترجمة من Dagger/Hilt:
    // "[Dagger/DuplicateBindings] AppRepository is bound multiple times" لأن Dagger يجد رابطين
    // لنفس النوع (constructor injection + module provider) ولا يستطيع أن يقرر أيهما يستخدم.
    // حُذفت دالة provideRepository المكرِّرة لإزالة هذا التعارض والسماح للمشروع بالترجمة أصلاً.

    @Provides
    @Singleton
    fun provideThemePreferenceManager(
        @ApplicationContext context: android.content.Context
    ): ThemePreferenceManager {
        return ThemePreferenceManager(context)
    }
}
