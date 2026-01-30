package com.voicenotes.motorcycle

import android.app.Application
import android.util.Log

/**
 * Application subclass to initialize global context for DebugLogger.
 * 
 * This ensures AppContextHolder.context is set at app startup before any
 * activities or services attempt to use DebugLogger for logging operations.
 */
class VoiceNotesApplication : Application() {
    
    companion object {
        private const val TAG = "VoiceNotesApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize global context for DebugLogger
        AppContextHolder.context = applicationContext
        
        Log.d(TAG, "Application initialized, AppContextHolder.context set")
    }
}
