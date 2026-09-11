# Keep Room entities
-keep class com.unimanager.app.data.entity.** { *; }

# Keep DAOs
-keep class com.unimanager.app.data.dao.** { *; }

# Keep Room Database
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep ViewModels
-keep class com.unimanager.app.viewmodel.** { *; }

# Keep Repository
-keep class com.unimanager.app.data.repository.** { *; }

# Keep Backup classes
-keep class com.unimanager.app.backup.** { *; }

# Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn schemasMicrosoftComVml.**

# Media3 ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Kotlin serialization plugin will generate the serializers for following classes
-keepclassmembers class com.unimanager.app.data.entity.** {
    *** Companion;
}
-keepclasseswithmembers class com.unimanager.app.data.entity.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Generic keep rules
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
