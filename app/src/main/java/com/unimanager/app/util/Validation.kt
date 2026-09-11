package com.unimanager.app.util

/**
 * Input Validation Utilities
 */
object Validation {

    // Allowed file extensions
    val ALLOWED_EXTENSIONS = setOf(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "jpg", "jpeg", "png", "gif", "webp", "bmp",
        "mp4", "avi", "mkv", "mov", "wmv",
        "mp3", "wav", "m4a", "ogg", "flac",
        "zip", "rar", "7z", "tar", "gz",
        "txt", "md", "rtf", "csv",
        "apk", "exe", "dmg"
    )

    val MAX_NAME_LENGTH = 255
    val MAX_DESCRIPTION_LENGTH = 1000

    /**
     * Validate file/folder name
     */
    fun validateName(name: String): Result<String> {
        return when {
            name.isBlank() -> Result.failure(Exception("الاسم مطلوب"))
            name.length > MAX_NAME_LENGTH -> Result.failure(Exception("الاسم طويل جداً (حد أقصى $MAX_NAME_LENGTH حرف)"))
            !name.matches(Regex("^[a-zA-Z0-9\\s\\u0600-\\u06FF._-]+$")) ->
                Result.failure(Exception("الاسم يحتوي على أحرف غير صالحة"))
            else -> Result.success(name.trim())
        }
    }

    /**
     * Validate file extension
     */
    fun validateExtension(ext: String): Result<String> {
        return when {
            ext.isBlank() -> Result.failure(Exception("الامتداد مطلوب"))
            ext !in ALLOWED_EXTENSIONS -> Result.failure(Exception("امتداد غير مدعوم: .$ext"))
            else -> Result.success(ext.lowercase())
        }
    }

    /**
     * Validate date format (YYYY-MM-DD)
     */
    fun validateDate(date: String): Result<String> {
        return when {
            date.isBlank() -> Result.failure(Exception("التاريخ مطلوب"))
            !date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) -> Result.failure(Exception("صيغة التاريخ غير صحيحة (YYYY-MM-DD)"))
            else -> {
                try {
                    java.time.LocalDate.parse(date)
                    Result.success(date)
                } catch (e: Exception) {
                    Result.failure(Exception("تاريخ غير صالح"))
                }
            }
        }
    }

    /**
     * Validate time format (HH:MM)
     */
    fun validateTime(time: String): Result<String> {
        return when {
            time.isBlank() -> Result.failure(Exception("الوقت مطلوب"))
            !time.matches(Regex("^\\d{2}:\\d{2}$")) -> Result.failure(Exception("صيغة الوقت غير صحيحة (HH:MM)"))
            else -> {
                val parts = time.split(":")
                val hour = parts[0].toIntOrNull() ?: -1
                val minute = parts[1].toIntOrNull() ?: -1
                when {
                    hour !in 0..23 -> Result.failure(Exception("الساعة يجب أن تكون بين 00 و 23"))
                    minute !in 0..59 -> Result.failure(Exception("الدقيقة يجب أن تكون بين 00 و 59"))
                    else -> Result.success(time)
                }
            }
        }
    }

    /**
     * Sanitize input - إزالة الأحرف الخطيرة فقط من الحقول القصيرة (الأسماء/العناوين).
     * إصلاح: لا نحذف الأحرف العربية أو الأكاديمية الشرعية
     */
    fun sanitizeInput(input: String): String {
        return input
            .trim()
            .take(MAX_NAME_LENGTH)
            .replace(Regex("[<>]"), "") // Remove HTML/XML brackets only
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "") // Remove control characters
    }

    /**
     * تنظيف الحقول النصية الطويلة (الأوصاف/المحتوى) بلا حد للطول،
     * مع الإبقاء على الأسطر الجديدة وعلامات التبويب.
     */
    fun sanitizeText(input: String): String {
        return input
            .trim()
            .replace(Regex("[<>]"), "")
            .replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "")
    }

    /**
     * Validate priority
     */
    fun validatePriority(priority: String): Result<String> {
        return when (priority) {
            "high", "medium", "low" -> Result.success(priority)
            else -> Result.failure(Exception("أولوية غير صالحة"))
        }
    }

    /**
     * Validate exam type
     */
    fun validateExamType(type: String): Result<String> {
        val allowedTypes = setOf("نصفي", "نهائي", "فجائي", "عملي")
        return when {
            type !in allowedTypes -> Result.failure(Exception("نوع امتحان غير صالح"))
            else -> Result.success(type)
        }
    }
}
