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
     * كل الترقيات مغطّاة بهجرات صريحة تحفظ بيانات المستخدم (انظر AppDatabase.MIGRATION_*).
     *
     * تحديث (التدقيق الرابع): أُضيفت fallbackToDestructiveMigration() كشبكة أمان أخيرة فقط،
     * لأن هذا تطبيق شخصي للتجربة غير منشور للعموم (لا مستخدمين حقيقيين قد تُفقد بياناتهم
     * صامتاً). في تطبيق إنتاجي حقيقي بمستخدمين فعليين يجب حذف هذا السطر والاكتفاء بكتابة
     * هجرة صريحة لكل تغيير في المخطط. راجع تقرير_التدقيق_الرابع.md لتفاصيل سبب الإضافة
     * (كانت مصدر كراش فعلي عند إضافة مجلد/فتح تبويب الملفات بسبب عدم تطابق مخطط قديم).
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            // إصلاح كراش حرج (التدقيق الرابع): كانت قاعدة البيانات تحمل الاسم القديم "uni_manager_db"
            // الذي مرّ بعدة تنقيحات للمخطط عبر جلسات تطوير متتالية (v1→v4: فهارس ثم مفتاح أجنبي
            // للملفات). أي تثبيت سابق على جهاز الاختبار خلال هذه الجلسات قد يحمل بنية جدول لا
            // تطابق حرفيًا ما يتوقعه Room من التعريفات الحالية، فيرمي Room عند فتح القاعدة
            // IllegalStateException: "Migration didn't properly handle ..." — وهذا بالضبط ما يظهر
            // للمستخدم كـ"كراش عند فتح تبويب الملفات" أو "كراش عند إضافة مجلد"، لأنهما أول استعلام
            // يلمس جدولي folders/files بعد فتح القاعدة. بما أن هذا تطبيق شخصي للتجربة (غير مخصص
            // للنشر) فلا قيمة لحفظ بيانات اختبار قديمة قد تكون غير متوافقة؛ لذلك غُيّر اسم الملف
            // ليبدأ كل مستخدم بقاعدة بيانات نظيفة تُبنى مباشرة من التعريفات الحالية (مطابقة تامة،
            // بلا أي مخاطرة ترقية).
            "uni_manager_db_v2"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4
            )
            // شبكة أمان إضافية: إن ظهر تعارض مخطط مستقبلي (نسيان كتابة هجرة جديدة) فلن يتجمّد
            // التطبيق في حلقة كراش عند كل فتح؛ يُعاد بناء القاعدة نظيفة بدل تعطّل كامل. مقبول هنا
            // تحديدًا لأن التطبيق شخصي غير منشور؛ في تطبيق إنتاجي حقيقي يجب كتابة هجرة صريحة بدلاً
            // من هذا السقوط الآمن.
            .fallbackToDestructiveMigration()
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
