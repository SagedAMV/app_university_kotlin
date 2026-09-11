# تقرير معالجة التدقيق العميق الثاني (UNIMANAGER_KOTLIN_AUDIT.md)

التاريخ: 2026-09-11
المنهجية: «التفكر العميق الشامل» — كل بند من التدقيق تم التحقق منه مقابل الكود الراهن (grep/قراءة) قبل الإصلاح وبعده، مع تحقق ثابت بعد كل مجموعة تغييرات (البيئة لا تحتوي Android SDK، لذا البناء/الاختبار متروكان للمستخدم — انظر النهاية).

## ملخص تنفيذي

| القسم | البند | الحالة |
|---|---|---|
| 1.1 🔴 | تكرار `Theme.App.Starting` المانع للبناء (AAPT2) | ✅ تم الحذف من `themes.xml` (بقي حصرياً في `splash_theme.xml`) |
| 1.1 | حذف `res/values/colors.xml` (7 موارد بلا أي استخدام) | ✅ تم حذف الملف |
| 2.1 🟠 | لا توجد إمكانية تعديل لأي كيان | ✅ حوارات تعديل للكيانات الخمسة في `UnifiedScreen` + تعديل/تلوين المجلد في `FilesScreen` |
| 2.2 🟠 | حذف السجل لا يحذف الملف الفعلي (تسرّب تخزين) | ✅ `deleteFile` و`deleteFolder` يحذفان النسخ الفعلية من `filesDir/documents` |
| 2.3 🟠 | لا FK بين `files.folderId` و`folders.id` | ✅ `ForeignKey(CASCADE)` + هجرة 3→4 تعيد بناء الجدول |
| 2.4 🟡 | اختصارات الرئيسية تفتح تبويب المهام دائماً | ✅ وسيط تنقل `tab` مع نمط اختياري + قراءته في `UnifiedScreen` |
| 2.5 🟡 | Dynamic Color غير قابل للتفعيل | ✅ مفتاح في الإعدادات (يظهر على Android 12+ فقط) |
| 3.1 🟡 | زر «مسح البحث» في المهام تعليق فارغ | ✅ `onClearSearch` موصول ويمسح النص فعلياً |
| 3.2 🟡 | معامل `onUndo` الميت في SwipeableItem | ✅ حُذف المعامل والتوقيع القديم بالكامل |
| 3.3 🟢 | API مهجورة SwipeToDismiss | ✅ هُجّر إلى `SwipeToDismissBox` |
| 3.4 🟢 | نبض المجرة ثابت | ✅ `rememberInfiniteTransition` يقود إعادة الرسم كل إطار |
| 3.5 🔵 | تناقض ترتيب `getAllTasksSync` | ✅ وُثّق بـ KDoc (لقطة نسخ احتياطي، ليست للواجهة) |
| 4 🧹 | كود ميت | ✅ حُذف BentoGrid DSL وSimpleBentoGrid وSlideUpEntrance وDateTimePickerRow وvalidateEmail + اختباراها ومرجع قناة backup |
| 4 🔴 كامن | إنشاء يدوي ثانٍ لـ DataStore | ✅ مصدر وحيد عبر Hilt + `ThemeViewModel` (حُذف الإنشاء اليدوي) |
| 5.1 🔵 | fallbackToDestructiveMigration | ✅ حُذف؛ 3 هجرات صريحة مسجّلة (تم في الجلسة السابقة، روجع) |
| 5.4 🔵 | لا تأكيد قبل الحذف | ✅ `ConfirmActionDialog` موحّد قبل كل حذف (سحب + أيقونة) في كل الشاشات |
| 5.2 🔵 | BackupHelper يتجاوز الـ Repository | 📝 موثّق كعمل مستقبلي (الأولوية 6، لا عطل وظيفي اليوم) |
| 5.3 🔵 | ألوان العلامة ثابتة خارج colorScheme | 📝 موثّق كعمل مستقبلي تدريجي (الأولوية 6) |

## تفاصيل الإصلاحات

### 1. الحرج المانع للبناء
- `res/values/themes.xml`: كان يعرّف `Theme.App.Starting` بينما يعرّفه `splash_theme.xml` أيضاً → AAPT2: `resource style/Theme.App.Starting is already defined` يفشل `assembleDebug`. حُذف التعريف المكرر (مع تعليق يشرح الموضع الصحيح)، وأُبقي `Theme.UniManager` لأنه مرجعي `postSplashScreenTheme` في `splash_theme.xml` وAndroidManifest.
- حُذف `res/values/colors.xml` (صفر إحالات `@color/` أو `R.color` في المشروع).

### 2.1 التعديل لكل الكيانات
- حوارات الإضافة الأربع (`AddTaskDialog`/`AddLectureDialog`/`AddNoteDialog`/`AddExamDialog`) تقبل الآن `initial: XEntity? = null` فتعمل للإنشاء والتعديل بنفس المنطق والتحقق.
- في كل تبويب: حالة `editing`/`deleting`، أيقونة تعديل (قلم) وأيقونة سلة في كل بطاقة، والسحب يفتح التأكيد.
- `updateTask`/`updateExam` الموجودان في الـ ViewModel (وفيهما إلغاء وإعادة جدولة إشعارات WorkManager) أصبحا يُستدعيان فعلياً فصار منطق إعادة جدولة التذكير حياً.
- تعديل الملاحظة يحدّث `updatedAt` ليتصدر الترتيب كما هو مصمم.
- المجلدات: `EditFolderDialog` جديد يعيد التسمية ويختار اللون من لوحة HEX ويستدعي `updateFolder`.
- بطاقة الملاحظة/الامتحان قابلة للنقر للتعديل أيضاً (سلوك مألوف).

### 2.2 حذف الملف الفعلي
- `AppViewModel.deleteFile`: بعد حذف السجل يُحذف الملف الفعلي عبر helper جديد `deletePhysicalFile(path)` داخل `runCatching` (فشل الحذف الفيزيائي لا يُفشل حذف السجل).
- `AppViewModel.deleteFolder`: يحسب شجرة المجلدات الفرعية كاملة (تكرار حتى الاستقرار) ويحذف الملفات الفعلية لكل المتحدرات قبل حذف السجل — لأن CASCADE في Room يحذف الصفوف فقط ولا يحذف ملفات التخزين.
- أُضيفت `AppRepository.getAllFoldersOnce()`/`getAllFilesOnce()` فوق دوال DAO الـ sync الموجودة أصلاً.

### 2.3 FK + الهجرة إلى v4
- `FileEntity`: `ForeignKey(entity=FolderEntity, parentColumns=["id"], childColumns=["folderId"], onDelete=CASCADE)` مع فهارسه.
- `AppDatabase` رُفع إلى الإصدار 4 مع `MIGRATION_3_4`:
  1. تُيتيم الملفات القديمة ذات `folderId` يشير لمجلد محذوف سابقاً (`UPDATE ... SET folderId=NULL`).
  2. إنشاء `files_new` بمطابقة تامة لحقول الكيان وأنواعها/NULL: `id INTEGER NOT NULL PK AUTOINCREMENT`، و`name/extension/type/mimeType/filePath TEXT NOT NULL`، و`size/createdAt INTEGER NOT NULL`، و`isFavorite INTEGER NOT NULL`، و`folderId INTEGER` قابل لل null، وFK بعبارة Room نفسها `ON UPDATE NO ACTION ON DELETE CASCADE`.
  3. نسخ البيانات، إسقاط القديم، إعادة التسمية، وإعادة الفهارس الأربعة بأسماء Room (`index_files_*`).
- تم التحقق سطراً بسطر من مطابقة الأعمدة والأنواع لتفادي فشل Room في التحقق من الهوية بعد الهجرة.

### 2.4 اختصارات التبويب
- `Routes.Unified`: `TAB_ARG="tab"`، `pattern = "unified?tab={tab}"`، `tab(index)` (موثّق: 0 مهام، 1 جدول، 2 ملاحظات، 3 امتحانات).
- `AppNavigation`: الـ composable يستخدم النمط مع `navArgument` Int افتراضيه 0.
- `UnifiedScreen` يقرأ الرقم من `backStackEntry.arguments` ويتهيأ به (`remember(initialTab)`).
- الرئيسية: «محاضرات اليوم»→1، «المهام»→0، «ملاحظاتي»→2 (تم التحقق من تطابق الأيقونة/التسمية/الرقم).

### 2.5 Dynamic Color
- `SettingsScreen`: مفتاح ثانٍ في قسم المظهر بلوحة Palette، يُعرض حصراً عند `Build.VERSION.SDK_INT >= S`.
- التوحيد المعماري (القسم 4): أُنشئ `ThemeViewModel` (@HiltViewModel) يحقن `ThemePreferenceManager` (نسخة Hilt الوحيدة عبر `@Provides @Singleton`)، وحُذف `remember { ThemePreferenceManager(context) }` اليدوي من `AppNavigation` الذي كان يمثل خطر `IllegalStateException: multiple DataStores active`.

### 3 إصلاحات وظيفية صغرى
- 3.1: `TasksTab(onClearSearch=...)` موصول وزر «مسح البحث» في حالة عدم وجود نتائج يمسح الاستعلام فعلياً.
- 3.2/3.3: `SwipeableItem` أُعيدت كتابتها على `SwipeToDismissBox/rememberSwipeToDismissBoxState`: السحب يستدعي `onSwipe` فقط ويعود العنصر لمكانه (`confirmValueChange` يعيد false دائماً)، والخلفية الحمراء تظهر أثناء السحب. لا حذف فوري ولا معامل `onUndo` ميت.
- 3.4: نبض المجرة كان يحسب `sin(System.currentTimeMillis())` داخل DrawScope بلا مشغّل إعادة رسم؛ الآن `rememberInfiniteTransition` (دورة ثانيتين، LinearEasing) ينتج `pulsePhase` تُقرأ داخل الـ Canvas فيعاد الرسم كل إطار.
- 3.5: KDoc على `getAllTasksSync` يوضح أنه لقطة للنسخ الاحتياطي بترتيب `createdAt DESC` المختلف عمداً عن استعلام الواجهة.

### 5.4 تأكيد الحذف الشامل
- مكوّن موحّد جديد: `ui/components/ConfirmDialog.kt` → `ConfirmActionDialog(title, message, icon, confirmText, onConfirm, onDismiss)` بزر تأكيد أحمر.
- موصول قبل: حذف مهمة/محاضرة/ملاحظة/امتحان (سحب + أيقونة سلة)، وحذف ملف ومجلد. رسالة المجلد تنبّه صراحةً أن كل المحتويات ستُحذف نهائياً، ورسالة الملف تذكر حذفه من الجهاز.

### 4 الكود الميت المحذوف
| المحذوف | الملف |
|---|---|
| `BentoGrid` + DSL (`BentoGridScope/BentoRowScope`/تنفيذاته) + `SimpleBentoGrid` | `components/BentoGrid.kt` (أُبقي `BentoRow` المستخدم في Dashboard) |
| `SlideUpEntrance` | `components/Animations.kt` (أُبقيت `AnimatedEntrance` المستخدمة) |
| `DateTimePickerRow` | `components/DateTimePickers.kt` (أُبقي `DatePickerField/TimePickerField` المستخدمان) |
| `Validation.validateEmail` + اختباراها الوحيدان | `util/Validation.kt`, `ValidationTest.kt` |
| فرع قناة `"backup"` غير المنشأة وغير المستخدمة | `util/NotificationWorker.kt` (القنوات الفعلية exams/tasks فقط) |
| `colors.xml` كاملاً | resources |

## عمل مستقبلي موثّق (لم يُنفّذ عمداً، الأولوية 6 في جدول التدقيق نفسه)
- **5.2**: نقل عمليات الاستيراد/التصدير الجماعي في `BackupHelper` إلى `AppRepository` لتوحيد الطبقات. لا يسبب عطلاً اليوم لعدم وجود منطق أعمال في الـ Repository بعد.
- **5.3**: استبدال ثوابت الألوان (`Primary/Secondary/...`) تدريجياً بـ`MaterialTheme.colorScheme.*` حتى تعكس Dynamic Color والوضع الليلي كل عناصر الواجهة. التبديل في 2.5 يعمل على مستوى الثيم نفسه؛ هذا البند يوسّع تغطيته داخل البطاقات.

## التحقق الثابت الذي نُفّذ
- لا مراجع متبقية لأي رمز محذوف (grep شامل على `app/src` و`app/src/test`).
- لا بقايا `onUndo`/`onSwwipe`/`fallbackToDestructiveMigration`.
- تواقيع كل العناصر (`TaskItem/LectureItem/NoteItem/ExamItem/FileItem/FolderItem`) محدّثة عند كل مستدعيها (لا مستدعين خارج الملفين المعنيين).
- مطابقة أعمدة وفهارس `MIGRATION_3_4` لما يولّده Room لـ`FileEntity` v4.
- توازن الأقواس/الأحرف في كل الملفات المعدّلة، ومراجعة بصرية كاملة للملفات المُعاد كتابتها.
- المصدر الوحيد لـ DataStore الآن هو Hilt (الـ @Provides كان موجوداً وغير مستخدم سابقاً).

## ما هو مطلوب من بيئة المستخدم (تعذّر هنا لغياب Android SDK)
1. `./gradlew :app:assembleDebug` — يجب أن يمر AAPT2 الآن بعد إزالة تكرار الـ style.
2. `./gradlew :app:testDebugUnitTest` — اختبارات Validation بعد حذف اختباري البريد.
3. `./gradlew :app:connectedDebugAndroidApp` أو فتح التطبيق يدوياً للتحقق خصوصاً من:
   - ترقية قاعدة بيانات مثبتة سابقاً (v3→v4) بلا فقد بيانات؛
   - السحب يُظهر الحوار ولا يُزيل العنصر؛
   - اختصارات الرئيسية تفتح التبويب الصحيح؛
   - مفتاح Dynamic Color يظهر على Android 12+ ويغيّر الثيم فوراً؛
  - نبض المجرة المتحرك؛
   - حذف ملف/مجلد يزيل النسخ الفعلية من `filesDir/documents`.

## الملفات المتأثرة (25)
معدّلة: AppDatabase, TaskDao, FileEntity, AppRepository, DatabaseModule, Animations, BentoGrid, DateTimePickers, SwipeableItem, DashboardScreen, FilesScreen, GalaxyScreen, AppNavigation, Routes, SettingsScreen, UnifiedScreen, NotificationWorker, Validation, AppViewModel, themes.xml, ValidationTest.
جديدة: `ui/components/ConfirmDialog.kt`, `viewmodel/ThemeViewModel.kt`.
محذوفة: `res/values/colors.xml`.
