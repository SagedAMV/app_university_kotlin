package com.unimanager.app.backup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.unimanager.app.MainActivity
import com.unimanager.app.R

class BackupService : Service() {

    companion object {
        const val CHANNEL_ID = "backup_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_EXPORT = "ACTION_EXPORT"
        const val ACTION_IMPORT = "ACTION_IMPORT"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("جاري النسخ الاحتياطي...")
        startForeground(NOTIFICATION_ID, notification)

        when (intent?.action) {
            ACTION_EXPORT -> {
                // Perform export backup
                // TODO: Implement export logic
            }
            ACTION_IMPORT -> {
                // Perform import backup
                // TODO: Implement import logic
            }
        }

        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "النسخ الاحتياطي",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعارات النسخ الاحتياطي"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(message: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("مدير الجامعة")
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .build()
}
