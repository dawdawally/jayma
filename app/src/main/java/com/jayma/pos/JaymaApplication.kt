package com.jayma.pos

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JaymaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Sync initialization will be handled by SyncInitializer
        // which is called from MainActivity after Hilt injection is ready
    }
}
