package com.jayma.pos.util

import android.util.Log
import com.jayma.pos.BuildConfig

/**
 * Secure logging utility that prevents logging sensitive data in production
 */
object Logger {
    
    private const val TAG_PREFIX = "JaymaPOS"
    private val isDebug = BuildConfig.DEBUG
    
    fun d(tag: String, message: String) {
        if (isDebug) {
            Log.d("$TAG_PREFIX:$tag", sanitizeMessage(message))
        }
    }
    
    fun i(tag: String, message: String) {
        if (isDebug) {
            Log.i("$TAG_PREFIX:$tag", sanitizeMessage(message))
        }
    }
    
    fun w(tag: String, message: String) {
        if (isDebug) {
            Log.w("$TAG_PREFIX:$tag", sanitizeMessage(message))
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        // Always log errors, but sanitize in production
        if (isDebug) {
            Log.e("$TAG_PREFIX:$tag", sanitizeMessage(message), throwable)
        } else {
            // In production, log errors without sensitive data
            Log.e("$TAG_PREFIX:$tag", sanitizeMessage(message))
        }
    }
    
    /**
     * Sanitize log messages to remove sensitive data
     */
    private fun sanitizeMessage(message: String): String {
        if (isDebug) {
            return message
        }
        
        // Remove potential sensitive data patterns in production
        return message
            .replace(Regex("(password|pwd|token|api[_-]?key|secret)=[^\\s&]+", RegexOption.IGNORE_CASE), "$1=***")
            .replace(Regex("(client[_-]?id|warehouse[_-]?id)=\\d+", RegexOption.IGNORE_CASE), "$1=***")
    }
}
