package com.voicenotes.motorcycle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Broadcast receiver for handling pinned shortcut callbacks.
 * 
 * This receiver is invoked when the system confirms that a shortcut has been pinned
 * to the home screen. It sets the managerIconPresent preference to true.
 */
class VNManagerIconReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "VNManagerIconReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received shortcut pinned callback")
        
        // Mark the manager icon as present
        // Use commit() instead of apply() to ensure synchronous write
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val success = prefs.edit().apply {
            putBoolean("managerIconPresent", true)
        }.commit()
        
        if (success) {
            Log.d(TAG, "managerIconPresent preference set to true")
        } else {
            Log.e(TAG, "Failed to set managerIconPresent preference")
        }
    }
}
