package com.voicenotes.motorcycle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Launcher activity for VN Manager icon.
 * This activity simply forwards to SettingsActivity and finishes immediately.
 * 
 * This provides a guaranteed launcher entry that works across all launchers,
 * even those that hide activity-alias entries.
 */
class VNManagerLauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Forward to SettingsActivity
        val intent = Intent(this, SettingsActivity::class.java)
        // Pass along any extras if present
        intent.putExtras(this.intent.extras ?: Bundle())
        startActivity(intent)
        
        // Finish immediately
        finish()
    }
}
