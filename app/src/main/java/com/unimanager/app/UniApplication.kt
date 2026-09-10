package com.unimanager.app

import android.app.Application

class UniApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: UniApplication
            private set
    }
}
