package com.unimanager.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class UniApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    "exams",
                    "تذكير بالامتحانات",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "إشعارات الامتحانات القادمة"
                    enableVibration(true)
                },
                NotificationChannel(
                    "tasks",
                    "المهام",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "إشعارات المهام"
                    enableVibration(false)
                }
            )

            val manager = getSystemService(NotificationManager::class.java)
            channels.forEach { channel ->
                manager.createNotificationChannel(channel)
            }
        }
    }
}
