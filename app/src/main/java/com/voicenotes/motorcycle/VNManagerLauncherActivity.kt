package com.voicenotes.motorcycle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * VN Manager Launcher Activity
 * 
 * This activity serves as an alternative launcher entry point that directly opens
 * the SettingsActivity (Voice Notes Manager). It is initially disabled in the manifest
 * and can be enabled programmatically after user consent.
 */
class VNManagerLauncherActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Forward to SettingsActivity
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        
        // Finish this activity immediately
        finish()
    }
}
