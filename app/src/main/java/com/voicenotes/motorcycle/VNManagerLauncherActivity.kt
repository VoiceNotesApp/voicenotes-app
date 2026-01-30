package com.voicenotes.motorcycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * VN Manager Launcher Activity
 * 
 * This activity serves as an alternative launcher entry point that directly opens
 * the SettingsActivity (Voice Notes Manager). It is initially disabled in the manifest
 * and can be enabled programmatically after user consent.
 */
class VNManagerLauncherActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "VNManagerLauncher"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Forward to SettingsActivity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start SettingsActivity", e)
        } finally {
            // Finish this activity
            finish()
        }
    }
}
