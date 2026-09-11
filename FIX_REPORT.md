# تقرير التحقق والإصلاح — تطبيق UniManager

> التاريخ: 2026-09-11
> المنهجية: التحقق من كل بند في `unimanager_analysis.md` مقابل الكود الفعلي، وإصلاح الأخطاء الحقيقية فقط.
> النتيجة: **التقرير المرفق صحيح في غالب بنوده الحرجة، لكنه أغفل 4 أخطاء ترجمة قاتلة إضافية.**

---

## أ) أخطاء ترجمة مؤكدة ومُصلَحة

| # | الملف | المشكلة | المصدر |
|---|-------|---------|--------|
| 1 | `util/ThemePreferenceManager.kt` | استدعاء دالة غير موجودة `booleanPreferencesName`؛ الصحيح `booleanPreferencesKey` (استيراد + استخدامان) | التقرير |
| 2 | `ui/galaxy/GalaxyScreen.kt` | استخدام `size.width/height/minDimension` خارج نطاق `Canvas` (لا تتوفر إلا في DrawScope) | التقرير |
| 3 | `backup/BackupHelper.kt` | `runInTransaction { }` يستقبل Runnable عادياً بينما بداخله دوال DAO من نوع `suspend` → لا يُترجم. استُبدل بـ `db.withTransaction { }` من room-ktx | التقرير |
| 4 | `data/dao/*.kt` (الـ 6) | تعليقتان `@Query` معلّقتان بلا دالة (بقايا تنظيف كود سابق): واحدة كانت تُلصق على `insert()` مع `@Insert`، وأخرى تكرّر `@Query` على `deleteAll()` → خطأ Room/KSP وخطأ "تعليق غير قابل للتكرار" | **لم يذكرها التقرير** |
| 5 | `data/repository/AppRepository.kt` | استدعاء `examDao.getUpcomingExamCount()` بينما اسم الدالة في الـ DAO هو `getUpcomingCount()` → مرجع غير موجود | **لم يذكرها التقرير** |
| 6 | `ui/navigation/Routes.kt` + `AppNavigation.kt` + `DashboardScreen.kt` | الكود يستخدم `Routes.X.route` بينما `Routes.X` سلسلة نصية لا تملك خاصية `.route` (20 موضعاً). أُعيد تعريف المسارات ككائنات تحمل `route` | **لم يذكرها التقرير** |
| 7 | `ui/dashboard/DashboardScreen.kt` | تعليق `@Composable` مكرر على `ActivityCard` (غير قابل للتكرار) | **لم يذكرها التقرير** |
| 8 | `ui/theme/Theme.kt` | دالة `@Composable` اسمها `Shapes()` يتعارض مع صنف Material3 وتُعاد في كل تركيبة؛ استُبدلت بقيمة ثابتة `AppShapes` | **لم يذكرها التقرير** |

## ب) أخطاء وظيفية مؤكدة ومُصلَحة

| # | الملف | المشكلة والحل |
|---|-------|----------------|
| 9 | `di/DatabaseModule.kt` + `data/AppDatabase.kt` | نسختان من قاعدة البيانات: واحدة من Hilt (بلا migrations) وأخرى من `getInstance` (يستخدمها BackupHelper) → عدم تحديث الواجهة بعد الاستيراد وخطر فقدان بيانات. الآن Hilt يفوّض إلى `AppDatabase.getInstance` (نسخة وحيدة + migrations) |
| 10 | `ui/files/FilesScreen.kt` | لا يوجد منتقي ملفات حقيقي: كانت تُنشأ سجلات بـ `filePath=""` فلا يُفتح شيء. أُضيف منتقي نظام حقيقي (SAF `OpenDocument`) ينسخ الملف إلى `filesDir/documents` ويسجل الاسم الحقيقي والامتداد والحجم والمسار وMIME، مع رسائل خطأ عند الفشل/غياب عارض |
| 11 | نفس الملف | فتح الملف الآن يفحص وجوده فعلياً، ويلتقط `ActivityNotFoundException`، ويصحح أنواع MIME لملفات Office الحديثة (docx/xlsx/pptx) |
| 12 | `viewmodel/AppViewModel.kt` | `getChildFolders/getFilesInFolder/getLecturesByDay` كانت تُنشئ StateFlow جديداً عند كل إعادة تركيب → تُخزَّن الآن في خرائط وتُعاد |
| 13 | نفس الملف | تحديث المهمة/الامتحان لم يُلغِ التذكير القديم ولا يجدول الجديد → تمت إعادة الجدولة (والإلغاء عند إفراغ التاريخ) |
| 14 | `util/ExamNotificationScheduler.kt` | معرّفات الإشعارات كانت تتصادم بين المهام والامتحانات (`id*10+1` مقابل `id+1000`) وقد تفيض سعة Int؛ تُشتق الآن من workTag الفريد (`hashCode` مع ضمان عدم الصفر) |
| 15 | `backup/BackupHelper.kt` | استيراد المجلدات المتداخلة كان قد يفشل لانتهاك المفتاح الأجنبي (الابن يُستورد قبل أبيه بسبب ترتيب التنازل)؛ يُرتب الآن تصاعدياً بالمعرّف |
| 16 | `ui/galaxy/GalaxyScreen.kt` | أسماء المجرات كانت غير قابلة للنقر رغم النص الإرشادي؛ أصبحت تنقل إلى ذلك المجلد فعلياً (وسيط `folderId` في مسار الملفات) |
| 17 | `ui/files/FilesScreen.kt` | لون المجلد كان يُشتق من `hashCode` لنص HEX (لون عشوائي)؛ يُحلَّل الآن من القيمة السداسية مع لون احتياطي |
| 18 | `ui/backup/BackupScreen.kt` | `CoroutineScope(Dispatchers.Main)` يدوي غير مرتبط بدورة الحياة ومتغير `isSuccess` غير مستخدم → `rememberCoroutineScope()` وحذف المتغير |
| 19 | `util/NotificationWorker.kt` | غير `Dispatchers.Main` إلى `Dispatchers.Default` (العامل خلفي أصلاً) |
| 20 | `MainActivity.kt` | حذف فرع ميت: فحص TIRAMISU داخل فرع Android R لا يُنفذ أبداً |
| 21 | `util/Validation.kt` | تقوية regex البريد الإلكتروني (يطلب نطاقاً بنقطة) |
| 22 | `README.md` | كان يدّعي استخدام Apache POI / Media3 / Coil وهي غير موجودة؛ الوثيقة تصف الآن الواقع: فتح الملفات عبر تطبيقات النظام `ACTION_VIEW`، وتوضح حدود النسخ الاحتياطي |

## ج) بنود فُحصت واتُّخذ فيها قرار عدم التعديل

- **إضافة POI/ExoPlayer/Coil**: ليست لازمة — الفتح يتم بتفويض النظام عبر `ACTION_VIEW` (الباب الذي كان معطلاً هو عدم وجود ملف حقيقي، وقد أُصلح). إضافة POI ستضخم التطبيق دون داعٍ (مبدأ KISS).
- **تضمين محتوى الملفات في النسخة الاحتياطية**: النسخة الحالية JSON للبيانات الوصفية فقط. تضمين الملفات يتطلب تغيير الصيغة إلى ZIP — يُقترح كتحسين مستقبلي موثّق في README وليس خطأً.
- **`android.util.Log` والنصوص والإيموجي داخل الكود**: أسلوبية وليست أخطاء.
- **ملاحظة التقرير عن "استعلامات بلا نوع إرجاع" في DAOs**: لم تكن أداة عرض مشوّهة، بل نفس التعليقات المعلّقة في البند (4) وأُصلحت.

## د) التحقق

- فحص آلي لجميع تعليقات Room: كل `@Query` الآن معلق على دالة، ولا توجد تعليقات مكررة.
- فحص المراجع المكسورة بالبحث الشامل (`booleanPreferencesName`, `runInTransaction`, التوقيعات القديمة).
- اختبار regex البريد الجديد لحالات الصحة/الرفض (يلبي اختباري الوحدة الحاليين).
- لم يتم بناء APK داخل هذه البيئة (لا يوجد Android SDK/Gradle wrapper)؛ يُنصح بفتح المشروع في Android Studio 2024.1+ وتشغيل `./gradlew assembleDebug` واختبارات DAO للتأكيد النهائي.
