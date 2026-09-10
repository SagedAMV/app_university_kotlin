# Keep Room entities and DAOs
-keep class com.unimanager.app.data.entity.** { *; }
-keep class com.unimanager.app.data.dao.** { *; }

# Keep Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
