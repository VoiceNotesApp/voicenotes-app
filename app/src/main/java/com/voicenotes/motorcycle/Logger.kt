package com.voicenotes.motorcycle

import android.util.Log

/**
 * Centralized logging wrapper that forwards to both DebugLogger and android.util.Log.
 * 
 * This ensures that when debug logging is enabled, all important messages are written to
 * debug_log.txt via DebugLogger, while also being available in Android logcat.
 * 
 * Usage:
 *   Logger.e(TAG, "Error message", exception)  // For errors
 *   Logger.i(TAG, "Info message")              // For informational messages
 *   Logger.d(TAG, "Debug message")             // For debug messages
 */
object Logger {
    
    /**
     * Log an error message.
     * Forwards to both DebugLogger.logError() and Log.e()
     * 
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     * @param throwable An optional throwable to log
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        // Log to Android logcat
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
        
        // Log to DebugLogger (writes to debug_log.txt when enabled)
        // skipLogcat=true to avoid double-logging to logcat
        DebugLogger.logError(tag, message, throwable, skipLogcat = true)
    }
    
    /**
     * Log an informational message.
     * Forwards to both DebugLogger.logInfo() and Log.i()
     * 
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     */
    fun i(tag: String, message: String) {
        // Log to Android logcat
        Log.i(tag, message)
        
        // Log to DebugLogger (writes to debug_log.txt when enabled)
        // skipLogcat=true to avoid double-logging to logcat
        DebugLogger.logInfo(tag, message, skipLogcat = true)
    }
    
    /**
     * Log a debug message.
     * Forwards to both DebugLogger.logDebug() and Log.d()
     * 
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     */
    fun d(tag: String, message: String) {
        // Log to Android logcat
        Log.d(tag, message)
        
        // Log to DebugLogger (writes to debug_log.txt when enabled)
        // skipLogcat=true to avoid double-logging to logcat
        DebugLogger.logDebug(tag, message, skipLogcat = true)
    }
    
    /**
     * Log a warning message.
     * Forwards to both DebugLogger and Log.w()
     * 
     * @param tag Used to identify the source of a log message
     * @param message The message to log
     * @param t An optional throwable to log
     */
    // Accept optional throwable to match existing call sites that pass exceptions.
    // When a throwable is provided we persist the error and stack trace to DebugLogger.
    fun w(tag: String, message: String, t: Throwable? = null) {
        try {
            if (t != null) {
                // treat as error to persist stack trace
                DebugLogger.logError(tag, message, t)
            } else {
                DebugLogger.logInfo(tag, message)
            }
        } catch (ex: Exception) {
            Log.w(tag, "Failed to write warning to DebugLogger", ex)
        }
        if (t != null) Log.w(tag, message, t) else Log.w(tag, message)
    }
        
        // Log to DebugLogger (writes to debug_log.txt when enabled)
        // skipLogcat=true to avoid double-logging to logcat
        try {
            if (t != null) {
                DebugLogger.logError(tag, message, t, skipLogcat = true)
            } else {
                DebugLogger.logInfo(tag, message, skipLogcat = true)
            }
        } catch (e: Exception) {
            // Ensure logging never throws - fallback to basic logcat only
            Log.e("Logger", "DebugLogger failed in w(): ${e.message}", e)
        }
    }
}
