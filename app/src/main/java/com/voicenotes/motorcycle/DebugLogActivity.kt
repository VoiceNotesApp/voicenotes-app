package com.voicenotes.motorcycle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class DebugLogActivity : AppCompatActivity() {

    private lateinit var switchEnableLogging: SwitchCompat
    private lateinit var buttonRunTests: Button
    private lateinit var buttonClearLog: Button
    private lateinit var textViewLog: TextView
    private lateinit var scrollViewLog: ScrollView
    
    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L // Update every second
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_log)
        
        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Debug Log"
        
        // Initialize views
        switchEnableLogging = findViewById(R.id.switchEnableLogging)
        buttonRunTests = findViewById(R.id.buttonRunTests)
        buttonClearLog = findViewById(R.id.buttonClearLog)
        textViewLog = findViewById(R.id.textViewLog)
        scrollViewLog = findViewById(R.id.scrollViewLog)
        
        // Load current logging preference
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val loggingEnabled = prefs.getBoolean("debug_logging_enabled", false)
        switchEnableLogging.isChecked = loggingEnabled
        
        // Set up switch listener
        switchEnableLogging.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("debug_logging_enabled", isChecked).apply()
            Toast.makeText(
                this, 
                if (isChecked) "Debug logging enabled" else "Debug logging disabled", 
                Toast.LENGTH_SHORT
            ).show()
        }
        
        // Set up run tests button
        buttonRunTests.setOnClickListener {
            runTests()
        }
        
        // Set up clear log button
        buttonClearLog.setOnClickListener {
            DebugLogger.clearLog(this)
            updateLogDisplay()
            Toast.makeText(this, "Log cleared", Toast.LENGTH_SHORT).show()
        }
        
        // Initial log display
        updateLogDisplay()
    }
    
    override fun onResume() {
        super.onResume()
        startAutoUpdate()
    }
    
    override fun onPause() {
        super.onPause()
        stopAutoUpdate()
    }
    
    private fun startAutoUpdate() {
        updateHandler.post(object : Runnable {
            override fun run() {
                updateLogDisplay()
                updateHandler.postDelayed(this, updateInterval)
            }
        })
    }
    
    private fun stopAutoUpdate() {
        updateHandler.removeCallbacksAndMessages(null)
    }
    
    private fun updateLogDisplay() {
        val logContent = DebugLogger.getLogContent(this)
        textViewLog.text = logContent
        
        // Auto-scroll to bottom if we're already near the bottom
        scrollViewLog.post {
            val scrollY = scrollViewLog.scrollY
            val height = scrollViewLog.height
            val contentHeight = textViewLog.height
            
            // If we're within 100px of the bottom, scroll to the new bottom
            if (contentHeight - scrollY - height < 100) {
                scrollViewLog.fullScroll(View.FOCUS_DOWN)
            }
        }
    }
    
    private fun runTests() {
        // Disable button during test run
        buttonRunTests.isEnabled = false
        buttonRunTests.text = "Running Tests..."
        
        // Run tests in background thread
        Thread {
            val testSuite = TestSuite(this)
            testSuite.runAllTests()
            
            // Re-enable button on main thread
            runOnUiThread {
                buttonRunTests.isEnabled = true
                buttonRunTests.text = "Run Tests"
                updateLogDisplay()
                Toast.makeText(this, "Tests complete", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
