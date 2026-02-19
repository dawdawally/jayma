package com.jayma.pos

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JaymaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
