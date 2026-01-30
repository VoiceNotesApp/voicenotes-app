package com.voicenotes.motorcycle

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import java.io.File

class SettingsActivity : AppCompatActivity() {

    companion object {
        private const val FALLBACK_VERSION = "Version 0.0.0-unknown"
        const val TAG = "SettingsActivity"
        private const val SHORTCUT_ADDED_ACTION = "com.voicenotes.motorcycle.SHORTCUT_ADDED"
        
        // Component name constant to avoid hardcoding throughout codebase
        const val VN_MANAGER_COMPONENT_NAME = "com.voicenotes.motorcycle.VNManagerLauncherActivity"
    }

    /**
     * BroadcastReceiver for pinned shortcut callback.
     * 
     * This receiver is invoked when the user confirms adding the pinned shortcut
     * to their home screen. It updates SharedPreferences to mark the manager icon
     * as successfully added.
     */
    class ShortcutAddedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == SHORTCUT_ADDED_ACTION) {
                Log.d(TAG, "Pinned shortcut callback received - shortcut was added successfully")
                // Store success in SharedPreferences
                val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("managerIconPresent", true).apply()
                
                // Show toast confirmation on main thread
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        context.getString(R.string.manager_icon_added_toast),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private lateinit var durationValueText: TextView
    private lateinit var durationNumberPicker: NumberPicker
    private lateinit var requestPermissionsButton: Button
    private lateinit var permissionStatusList: TextView
    private lateinit var quitButton: Button
    private lateinit var openFolderButton: Button
    private lateinit var buttonDebugLog: Button
    private lateinit var appVersionText: TextView

    private val PERMISSIONS_REQUEST_CODE = 200
    private val OVERLAY_PERMISSION_REQUEST_CODE = 201
    private val BATTERY_OPTIMIZATION_REQUEST_CODE = 202

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        // Enable Up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        durationValueText = findViewById(R.id.durationValueText)
        durationNumberPicker = findViewById(R.id.durationNumberPicker)
        requestPermissionsButton = findViewById(R.id.requestPermissionsButton)
        permissionStatusList = findViewById(R.id.permissionStatusList)
        quitButton = findViewById(R.id.quitButton)
        openFolderButton = findViewById(R.id.openFolderButton)
        buttonDebugLog = findViewById(R.id.buttonDebugLog)
        appVersionText = findViewById(R.id.appVersionText)

        // Configure NumberPicker
        durationNumberPicker.minValue = 1
        durationNumberPicker.maxValue = 99
        durationNumberPicker.wrapSelectorWheel = false
        
        // Auto-save when value changes
        durationNumberPicker.setOnValueChangedListener { _, _, newVal ->
            saveDuration()
        }
        
        // Display app version
        appVersionText.text = getAppVersion()

        loadCurrentSettings()

        requestPermissionsButton.setOnClickListener {
            requestAllPermissions()
        }

        quitButton.setOnClickListener {
            finishAffinity()
        }
        
        openFolderButton.setOnClickListener {
            val intent = Intent(this, RecordingManagerActivity::class.java)
            startActivity(intent)
        }
        
        buttonDebugLog.setOnClickListener {
            showDebugLog()
        }
    }

    private fun getAppVersion(): String {
        return try {
            // Try BuildConfig first
            val buildConfigVersion = BuildConfig.VERSION_NAME
            
            if (!buildConfigVersion.isNullOrEmpty() && buildConfigVersion != "null") {
                Log.d("SettingsActivity", "Version source: BuildConfig.VERSION_NAME = $buildConfigVersion")
                return "Version $buildConfigVersion"
            }
            
            // Fallback: Try to get from PackageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            
            if (!versionName.isNullOrEmpty() && versionName != "null") {
                Log.d("SettingsActivity", "Version source: PackageManager.versionName = $versionName")
                return "Version $versionName"
            }
            
            // Final fallback
            Log.d("SettingsActivity", "Version source: FALLBACK_VERSION = $FALLBACK_VERSION")
            FALLBACK_VERSION
            
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Error getting version", e)
            Log.d("SettingsActivity", "Version source: Exception fallback = $FALLBACK_VERSION")
            FALLBACK_VERSION
        }
    }

    private fun getDefaultSavePath(): String {
        // Use app-specific external files directory (doesn't require storage permissions)
        // This directory is cleared when the app is uninstalled
        val externalDir = getExternalFilesDir(null)
        return if (externalDir != null) {
            "${externalDir.absolutePath}/VoiceNotes"
        } else {
            // Fallback to internal files directory if external is not available
            "${filesDir.absolutePath}/VoiceNotes"
        }
    }
    
    private fun showDebugLog() {
        val intent = Intent(this, DebugLogActivity::class.java)
        startActivity(intent)
    }

    private fun loadCurrentSettings() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val recordingDuration = prefs.getInt("recordingDuration", 10)
        
        // Always use fixed internal storage path
        val defaultPath = getDefaultSavePath()
        val saveDir = prefs.getString("saveDirectory", null)
        
        // If no directory configured, set it now
        if (saveDir.isNullOrEmpty()) {
            prefs.edit().putString("saveDirectory", defaultPath).apply()
        }
        
        durationValueText.text = "$recordingDuration seconds"
        durationNumberPicker.value = recordingDuration
        
        // Update permission status list
        updatePermissionStatusList()
    }
    
    private fun updatePermissionStatusList() {
        val statusLines = mutableListOf<String>()
        
        // Check microphone permission
        val hasMicrophone = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        statusLines.add(if (hasMicrophone) {
            getString(R.string.permission_granted, getString(R.string.permission_microphone))
        } else {
            getString(R.string.permission_not_granted, getString(R.string.permission_microphone))
        })
        
        // Check location permission
        val hasLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        statusLines.add(if (hasLocation) {
            getString(R.string.permission_granted, getString(R.string.permission_location))
        } else {
            getString(R.string.permission_not_granted, getString(R.string.permission_location))
        })
        
        // Check Bluetooth permission
        val hasBluetooth = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        statusLines.add(if (hasBluetooth) {
            getString(R.string.permission_granted, getString(R.string.permission_bluetooth))
        } else {
            getString(R.string.permission_not_granted, getString(R.string.permission_bluetooth))
        })
        
        // Check overlay permission
        val hasOverlay = Settings.canDrawOverlays(this)
        statusLines.add(if (hasOverlay) {
            getString(R.string.permission_granted, getString(R.string.permission_overlay))
        } else {
            getString(R.string.permission_not_granted, getString(R.string.permission_overlay))
        })

        // Check battery optimization
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val hasBatteryExemption = powerManager.isIgnoringBatteryOptimizations(packageName)
        statusLines.add(if (hasBatteryExemption) {
            "✓ Battery optimization (disabled)"
        } else {
            "✗ Battery optimization (enabled - may interrupt recording)"
        })

        permissionStatusList.text = statusLines.joinToString("\n")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            updatePermissionStatusList()
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show()
                // Continue permission flow - check battery optimization
                checkAndRequestBatteryOptimization()
            } else {
                Toast.makeText(this, "Overlay permission is required for the app to work", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == BATTERY_OPTIMIZATION_REQUEST_CODE) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Toast.makeText(this, "Battery optimization disabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Background recording may be interrupted", Toast.LENGTH_LONG).show()
            }
            updatePermissionStatusList()
            // Continue to manager icon consent
            checkAndRequestManagerIcon()
        }
    }

    private fun saveDuration() {
        val duration = durationNumberPicker.value

        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().putInt("recordingDuration", duration).apply()

        durationValueText.text = "$duration seconds"
        Toast.makeText(this, getString(R.string.duration_saved, duration), Toast.LENGTH_SHORT).show()
    }

    private fun requestAllPermissions() {
        // First, request runtime permissions
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSIONS_REQUEST_CODE)
        } else {
            // All runtime permissions granted, check overlay and storage permissions
            checkAndRequestOverlayPermission()
        }
    }
    
    private fun checkAndRequestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.overlay_permission_required)
                .setMessage(R.string.overlay_permission_message)
                .setPositiveButton("Grant") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
                }
                .setNegativeButton("Cancel") { _, _ ->
                    updatePermissionStatusList()
                }
                .show()
        } else {
            // Check battery optimization permission next
            checkAndRequestBatteryOptimization()
        }
    }

    private fun checkAndRequestBatteryOptimization() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = packageName

        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            AlertDialog.Builder(this)
                .setTitle("Battery Optimization")
                .setMessage("To ensure reliable background recording, this app needs unrestricted battery access.\n\n" +
                        "This will prevent Android from stopping the recording when the screen is off or the app is in the background.")
                .setPositiveButton("Grant") { _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivityForResult(intent, BATTERY_OPTIMIZATION_REQUEST_CODE)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Unable to open battery settings", Toast.LENGTH_SHORT).show()
                        // Continue to manager icon consent
                        checkAndRequestManagerIcon()
                    }
                }
                .setNegativeButton("Skip") { _, _ ->
                    Toast.makeText(this, "Background recording may be interrupted", Toast.LENGTH_LONG).show()
                    updatePermissionStatusList()
                    // Continue to manager icon consent
                    checkAndRequestManagerIcon()
                }
                .show()
        } else {
            // Battery optimization already granted, continue to manager icon
            checkAndRequestManagerIcon()
        }
    }

    /**
     * Checks if the manager icon consent has been requested and prompts the user if not.
     * 
     * This dialog is shown after the battery optimization flow completes. It asks users
     * if they want to add a VN Manager icon to their home screen.
     * 
     * The consent is tracked via the "managerIconRequested" SharedPreferences flag to
     * avoid repeated prompts. If the user declines, they can still proceed with using
     * the app, and the component will be enabled anyway to prevent blocking app usage.
     */
    private fun checkAndRequestManagerIcon() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val iconRequested = prefs.getBoolean("managerIconRequested", false)
        
        // If already requested (either granted or declined), don't ask again
        if (iconRequested) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            updatePermissionStatusList()
            return
        }
        
        // Show consent dialog
        AlertDialog.Builder(this)
            .setTitle(R.string.add_vn_manager_permission_title)
            .setMessage(R.string.add_vn_manager_permission_message)
            .setPositiveButton("Add Icon") { _, _ ->
                addManagerIcon()
            }
            .setNegativeButton("No Thanks") { _, _ ->
                handleManagerIconDeclined()
            }
            .setOnCancelListener {
                // Treat cancellation same as declining
                handleManagerIconDeclined()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Handles the case where user declines the manager icon.
     * 
     * Even though the user declined, we still enable the component to prevent
     * blocking app usage. The pinned shortcut request is skipped, but the
     * launcher icon will still appear on supported launchers.
     */
    private fun handleManagerIconDeclined() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        
        try {
            // Still enable component to allow app to proceed
            val componentName = ComponentName(this, VN_MANAGER_COMPONENT_NAME)
            packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            
            // Mark as requested and present (so app can proceed)
            prefs.edit().apply {
                putBoolean("managerIconRequested", true)
                putBoolean("managerIconPresent", true)
                apply()
            }
            
            Toast.makeText(this, "You can access the manager via your launcher", Toast.LENGTH_LONG).show()
            updatePermissionStatusList()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable component after decline", e)
            // Even if component enabling fails, mark as complete to avoid blocking
            prefs.edit().apply {
                putBoolean("managerIconRequested", true)
                putBoolean("managerIconPresent", true)
                apply()
            }
            updatePermissionStatusList()
        }
    }

    /**
     * Enables the VN Manager launcher icon and requests a pinned shortcut.
     * 
     * This method implements a two-tier approach:
     * 1. Enables VNManagerLauncherActivity component via PackageManager
     *    - Component starts disabled in manifest (android:enabled="false")
     *    - Enabling makes it appear in launchers that honor component state
     *    - Uses DONT_KILL_APP to avoid interrupting the app
     * 
     * 2. Requests pinned shortcut as fallback via ShortcutManagerCompat
     *    - Creates a ShortcutInfoCompat targeting SettingsActivity
     *    - Provides callback PendingIntent to detect successful addition
     *    - Shortcut works on launchers that don't honor component state
     * 
     * SharedPreferences flags:
     * - "managerIconRequested" = true: User has been prompted (avoid repeat prompts)
     * - "managerIconPresent" = true: Set when component enabled OR callback confirms shortcut added
     * 
     * Success scenarios:
     * - Component enabled + shortcut added = both icons may appear
     * - Component enabled + shortcut declined = icon appears in supported launchers
     * - Component enabled + shortcut not supported = icon appears in supported launchers
     * 
     * Failure scenario:
     * - Component enabling fails = app proceeds anyway to avoid blocking usage
     */
    private fun addManagerIcon() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        
        try {
            // 1. Enable the VNManagerLauncherActivity component
            val componentName = ComponentName(this, VN_MANAGER_COMPONENT_NAME)
            
            packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            
            Log.d(TAG, "VNManagerLauncherActivity component enabled")
            
            // Mark as requested
            prefs.edit().putBoolean("managerIconRequested", true).apply()
            
            // 2. Request pinned shortcut as fallback
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
                Log.d(TAG, "Requesting pinned shortcut")
                
                // Create intent for the shortcut
                val shortcutIntent = Intent(this, SettingsActivity::class.java)
                shortcutIntent.action = Intent.ACTION_VIEW
                
                // Create the shortcut info
                val shortcutInfo = ShortcutInfoCompat.Builder(this, "vn_manager_shortcut")
                    .setShortLabel(getString(R.string.shortcut_settings_short))
                    .setLongLabel(getString(R.string.shortcut_settings_long))
                    .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher_settings))
                    .setIntent(shortcutIntent)
                    .build()
                
                // Create callback intent for when shortcut is added
                val callbackIntent = Intent(SHORTCUT_ADDED_ACTION)
                val callbackPendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    callbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Request the pinned shortcut
                val success = ShortcutManagerCompat.requestPinShortcut(
                    this,
                    shortcutInfo,
                    callbackPendingIntent.intentSender
                )
                
                if (success) {
                    Toast.makeText(this, R.string.manager_icon_requested_toast, Toast.LENGTH_LONG).show()
                } else {
                    Log.d(TAG, "Pinned shortcut request returned false, but component is enabled")
                    // Component is still enabled, so consider it a success
                    prefs.edit().putBoolean("managerIconPresent", true).apply()
                    Toast.makeText(this, R.string.manager_icon_added_toast, Toast.LENGTH_LONG).show()
                }
            } else {
                Log.d(TAG, "Pinned shortcuts not supported, but component is enabled")
                // Component is enabled, mark as present
                prefs.edit().putBoolean("managerIconPresent", true).apply()
                Toast.makeText(this, R.string.manager_icon_added_toast, Toast.LENGTH_LONG).show()
            }
            
            updatePermissionStatusList()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add manager icon", e)
            // Still mark as complete to avoid blocking the app
            prefs.edit().apply {
                putBoolean("managerIconRequested", true)
                putBoolean("managerIconPresent", true)
                apply()
            }
            Toast.makeText(this, R.string.manager_icon_failed_to_add, Toast.LENGTH_LONG).show()
            updatePermissionStatusList()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            updatePermissionStatusList()
            
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!allGranted) {
                Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_LONG).show()
            }
            
            // Always check overlay permission after runtime permissions are handled
            checkAndRequestOverlayPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        loadCurrentSettings()
    }
    
    // Handle Up navigation
    // Note: This handles the Up/back button in the action bar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    // Handle home/up button in options menu
    // Note: This provides additional compatibility for older Android versions
    // where onSupportNavigateUp() might not be called
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
