package com.jayma.pos.util

import android.os.SystemClock
import com.jayma.pos.BuildConfig

/**
 * Performance monitoring utility for tracking operation times
 * Only active in debug builds
 */
object PerformanceMonitor {
    
    private val isDebug = BuildConfig.DEBUG
    
    /**
     * Measure execution time of a block
     */
    inline fun <T> measureTime(operation: String, block: () -> T): T {
        if (!isDebug) {
            return block()
        }
        
        val startTime = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            val duration = SystemClock.elapsedRealtime() - startTime
            Logger.d("Performance", "$operation took ${duration}ms")
        }
    }
    
    /**
     * Measure execution time of a suspend block
     */
    suspend inline fun <T> measureTimeSuspend(operation: String, block: () -> T): T {
        if (!isDebug) {
            return block()
        }
        
        val startTime = SystemClock.elapsedRealtime()
        return try {
            block()
        } finally {
            val duration = SystemClock.elapsedRealtime() - startTime
            Logger.d("Performance", "$operation took ${duration}ms")
        }
    }
}
