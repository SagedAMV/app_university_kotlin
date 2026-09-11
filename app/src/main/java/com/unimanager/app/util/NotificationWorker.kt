package com.unimanager.app.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.unimanager.app.MainActivity
import com.unimanager.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Notification Worker - تنفيذ الإشعارات في الخلفية
 * يستخدم القنوات المخصصة (exams/tasks) بدلاً من general
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_NOTIFICATION_ID = "notificationId"
        const val KEY_CHANNEL = "channel"

        // قناة افتراضية
        const val DEFAULT_CHANNEL = "general"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.Main) {
        try {
            val title = inputData.getString(KEY_TITLE) ?: "إشعار"
            val message = inputData.getString(KEY_MESSAGE) ?: ""
            val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, 0)
            val channel = inputData.getString(KEY_CHANNEL) ?: DEFAULT_CHANNEL

            if (notificationId == 0) {
                return@withContext Result.failure()
            }

            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // استخدام الأولوية المناسبة للقناة
            val priority = when (channel) {
                "exams" -> NotificationCompat.PRIORITY_HIGH
                "tasks" -> NotificationCompat.PRIORITY_DEFAULT
                "backup" -> NotificationCompat.PRIORITY_LOW
                else -> NotificationCompat.PRIORITY_DEFAULT
            }

            val notification = NotificationCompat.Builder(applicationContext, channel)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(priority)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("NotificationWorker", "Failed to send notification", e)
            Result.failure()
        }
    }
}
