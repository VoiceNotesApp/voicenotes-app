package com.voicenotes.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class DebugLogActivity : AppCompatActivity() {

    private lateinit var buttonRefreshLog: MaterialButton
    private lateinit var buttonCopyLog: MaterialButton
    private lateinit var buttonShareLog: MaterialButton
    private lateinit var buttonClearLog: MaterialButton
    private lateinit var textViewLog: TextView
    private lateinit var scrollViewLog: ScrollView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_log)
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Debug Log"
        
        // Initialize views
        buttonRefreshLog = findViewById(R.id.buttonRefreshLog)
        buttonCopyLog = findViewById(R.id.buttonCopyLog)
        buttonShareLog = findViewById(R.id.buttonShareLog)
        buttonClearLog = findViewById(R.id.buttonClearLog)
        textViewLog = findViewById(R.id.textViewLog)
        scrollViewLog = findViewById(R.id.scrollViewLog)
        
        // Set up refresh log button
        buttonRefreshLog.setOnClickListener {
            updateLogDisplay()
            Toast.makeText(this, "Log refreshed", Toast.LENGTH_SHORT).show()
        }
        
        // Set up copy log button
        buttonCopyLog.setOnClickListener {
            val logContent = DebugLogger.getLogContent(this)
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Debug Log", logContent)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        
        // Set up share log button
        buttonShareLog.setOnClickListener {
            try {
                val logContent = DebugLogger.getLogContent(this)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Motorcycle Voice Notes Debug Log")
                    putExtra(Intent.EXTRA_TEXT, logContent)
                }
                startActivity(Intent.createChooser(shareIntent, "Share log via..."))
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to share log: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Set up clear log button
        buttonClearLog.setOnClickListener {
            DebugLogger.clearLog(this)
            updateLogDisplay()
            Toast.makeText(this, "Log cleared", Toast.LENGTH_SHORT).show()
        }
        
        // Initial log display (scroll to top)
        updateLogDisplay()
    }
    
    private fun updateLogDisplay() {
        val logContent = DebugLogger.getLogContent(this)
        textViewLog.text = logContent
        
        // Scroll to top to show newest entries (if log is reversed) or oldest entries first
        scrollViewLog.post {
            scrollViewLog.scrollTo(0, 0)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
