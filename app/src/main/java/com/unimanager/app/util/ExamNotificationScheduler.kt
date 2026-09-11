package com.unimanager.app.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.unimanager.app.data.entity.ExamEntity
import com.unimanager.app.data.entity.TaskEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Exam & Task Notification Scheduler
 * يستخدم WorkManager لجدولة الإشعارات
 */
class ExamNotificationScheduler(private val context: Context) {

    companion object {
        private const val WORK_TAG_EXAM = "exam_notification"
        private const val WORK_TAG_TASK = "task_notification"
        private const val KEY_EXAM_SUBJECT = "exam_subject"
        private const val KEY_EXAM_DATE = "exam_date"
        private const val KEY_EXAM_ID = "exam_id"
        private const val KEY_TASK_TITLE = "task_title"
        private const val KEY_TASK_ID = "task_id"
    }

    /**
     * جدولة إشعار للامتحان
     */
    fun scheduleExamNotification(exam: ExamEntity) {
        try {
            val examDateTime = parseExamDateTime(exam.examDate, exam.time)
            if (examDateTime.isBefore(LocalDateTime.now())) {
                return // الامتحان في الماضي
            }

            // إشعار قبل يوم واحد
            val oneDayBefore = examDateTime.minusDays(1)
            if (oneDayBefore.isAfter(LocalDateTime.now())) {
                scheduleNotification(
                    workTag = "${WORK_TAG_EXAM}_${exam.id}_1day",
                    title = "تذكير بالامتحان",
                    message = "${exam.subject} غداً",
                    triggerTime = oneDayBefore,
                    notificationId = exam.id.toInt() * 10 + 1
                )
            }

            // إشعار قبل ساعة واحدة
            val oneHourBefore = examDateTime.minusHours(1)
            if (oneHourBefore.isAfter(LocalDateTime.now())) {
                scheduleNotification(
                    workTag = "${WORK_TAG_EXAM}_${exam.id}_1hour",
                    title = "تذكير بالامتحان",
                    message = "${exam.subject} بعد ساعة",
                    triggerTime = oneHourBefore,
                    notificationId = exam.id.toInt() * 10 + 2
                )
            }

            // إشعار في وقت الامتحان
            if (examDateTime.isAfter(LocalDateTime.now())) {
                scheduleNotification(
                    workTag = "${WORK_TAG_EXAM}_${exam.id}_now",
                    title = "الامتحان الآن",
                    message = "${exam.subject} - ${exam.room}",
                    triggerTime = examDateTime,
                    notificationId = exam.id.toInt() * 10 + 3
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ExamScheduler", "Failed to schedule exam notification", e)
        }
    }

    /**
     * جدولة إشعار للمهمة
     */
    fun scheduleTaskNotification(task: TaskEntity) {
        try {
            if (task.dueDate.isNullOrBlank()) return

            val dueDate = LocalDate.parse(task.dueDate)
            val dueDateTime = dueDate.atTime(9, 0) // 9 صباحاً

            if (dueDateTime.isBefore(LocalDateTime.now())) return

            // إشعار في يوم الاستحقاق
            scheduleNotification(
                workTag = "${WORK_TAG_TASK}_${task.id}",
                title = "تذكير بالمهمة",
                message = task.title,
                triggerTime = dueDateTime,
                notificationId = task.id.toInt() + 1000
            )
        } catch (e: Exception) {
            android.util.Log.e("TaskScheduler", "Failed to schedule task notification", e)
        }
    }

    /**
     * إلغاء إشعارات الامتحان
     */
    fun cancelExamNotifications(examId: Long) {
        val workManager = WorkManager.getInstance(context)
        for (i in 1..3) {
            workManager.cancelUniqueWork("${WORK_TAG_EXAM}_${examId}_${i}day")
            workManager.cancelUniqueWork("${WORK_TAG_EXAM}_${examId}_${i}hour")
            workManager.cancelUniqueWork("${WORK_TAG_EXAM}_${examId}_now")
        }
    }

    /**
     * إلغاء إشعارات المهمة
     */
    fun cancelTaskNotifications(taskId: Long) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork("${WORK_TAG_TASK}_${taskId}")
    }

    /**
     * جدولة إشعار عام
     */
    private fun scheduleNotification(
        workTag: String,
        title: String,
        message: String,
        triggerTime: LocalDateTime,
        notificationId: Int
    ) {
        val workManager = WorkManager.getInstance(context)

        val data = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .putInt("notificationId", notificationId)
            .build()

        val delay = java.time.Duration.between(LocalDateTime.now(), triggerTime).toMillis()
        if (delay <= 0) return

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(workTag)
            .build()

        workManager.enqueueUniqueWork(
            workTag,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun parseExamDateTime(dateStr: String, timeStr: String?): LocalDateTime {
        val date = LocalDate.parse(dateStr)
        val time = if (!timeStr.isNullOrBlank()) {
            LocalTime.parse(timeStr)
        } else {
            LocalTime.of(9, 0)
        }
        return date.atTime(time)
    }
}
