package com.jayma.pos.util

import android.content.Context
import android.widget.Toast
import com.jayma.pos.R
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized error handling utility
 */
object ErrorHandler {
    
    /**
     * Handle network errors and return user-friendly messages
     */
    fun handleNetworkError(context: Context, throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> {
                context.getString(R.string.no_internet)
            }
            is SocketTimeoutException -> {
                "Connection timeout. Please check your internet connection."
            }
            is IOException -> {
                "Network error. Please try again."
            }
            else -> {
                throwable.message ?: "An unexpected error occurred"
            }
        }
    }
    
    /**
     * Show error toast message
     */
    fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        Logger.e("ErrorHandler", message)
    }
    
    /**
     * Handle API error responses
     */
    fun handleApiError(context: Context, errorCode: Int, errorMessage: String?): String {
        return when (errorCode) {
            400 -> "Invalid request. Please check your input."
            401 -> "Authentication required."
            403 -> "Access denied."
            404 -> "Resource not found."
            500, 502, 503 -> "Server error. Please try again later."
            else -> errorMessage ?: "An error occurred (Code: $errorCode)"
        }
    }
}
