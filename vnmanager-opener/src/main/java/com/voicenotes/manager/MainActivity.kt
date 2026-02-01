package com.voicenotes.manager

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Minimal launcher activity that opens the SettingsActivity of the main Voice Notes app.
 * 
 * This activity serves as a companion launcher that directly launches the Voice Notes Manager
 * (SettingsActivity) from the main app.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TARGET_PACKAGE = "com.voicenotes.main"
        private const val TARGET_ACTIVITY = "com.voicenotes.main.SettingsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Build the explicit intent to launch the main app's SettingsActivity
        val intent = Intent().apply {
            component = ComponentName(TARGET_PACKAGE, TARGET_ACTIVITY)
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Main app or target activity not installed — inform user and finish
            Toast.makeText(this, R.string.error_app_not_found, Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            // Not allowed to start target activity — inform user and finish
            Toast.makeText(this, R.string.error_permission_denied, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // Fallback for any other unexpected exception
            Toast.makeText(this, R.string.error_app_not_found, Toast.LENGTH_LONG).show()
        } finally {
            // Ensure opener always finishes and does not remain in the foreground
            finish()
        }
    }
}
