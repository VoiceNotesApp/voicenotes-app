package com.voicenotes.motorcycle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var statusText: TextView
    private lateinit var infoText: TextView
    private lateinit var settingsButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var textToSpeech: TextToSpeech? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentLocation: Location? = null
    private var recordingFilePath: String? = null

    private val handler = Handler(Looper.getMainLooper())
    private val PERMISSIONS_REQUEST_CODE = 100

    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        infoText = findViewById(R.id.infoText)
        settingsButton = findViewById(R.id.settingsButton)
        progressBar = findViewById(R.id.progressBar)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        textToSpeech = TextToSpeech(this, this)

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Check if this is first run
        if (isFirstRun()) {
            showSetupDialog()
        } else {
            startRecordingProcess()
        }
    }

    private fun isFirstRun(): Boolean {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val saveDir = prefs.getString("saveDirectory", null)
        val triggerApp = prefs.getString("triggerApp", null)
        return saveDir.isNullOrEmpty() || triggerApp.isNullOrEmpty()
    }

    private fun showSetupDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.setup_required)
            .setMessage(R.string.complete_setup)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            .setCancelable(false)
            .show()
    }

    private fun startRecordingProcess() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }

        infoText.text = "Acquiring location..."
        progressBar.visibility = View.VISIBLE

        acquireLocation()
    }

    private fun checkPermissions(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startRecordingProcess()
            } else {
                AlertDialog.Builder(this)
                    .setTitle(R.string.permission_required)
                    .setMessage(R.string.grant_permissions)
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .show()
            }
        }
    }

    private fun acquireLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLocation = location
                onLocationAcquired()
            } else {
                onLocationFailed()
            }
        }.addOnFailureListener {
            onLocationFailed()
        }
    }

    private fun onLocationAcquired() {
        progressBar.visibility = View.GONE
        infoText.text = "Location acquired"

        // Speak and start recording
        speakAndRecord(getString(R.string.location_acquired))
    }

    private fun onLocationFailed() {
        progressBar.visibility = View.GONE
        infoText.text = getString(R.string.location_failed)
        Toast.makeText(this, R.string.location_failed, Toast.LENGTH_LONG).show()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.US
        }
    }

    private fun speakAndRecord(text: String) {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // Speech started
            }

            override fun onDone(utteranceId: String?) {
                // Speech finished, start recording
                runOnUiThread {
                    startRecording()
                }
            }

            override fun onError(utteranceId: String?) {
                // Error in speech, still try to record
                runOnUiThread {
                    startRecording()
                }
            }
        })

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "recording_start")
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "recording_start")
    }

    private fun startRecording() {
        try {
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val saveDir = prefs.getString("saveDirectory", null)

            if (saveDir.isNullOrEmpty()) {
                Toast.makeText(this, "Save directory not configured", Toast.LENGTH_SHORT).show()
                return
            }

            val location = currentLocation ?: run {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                return
            }

            // Create filename with coordinates and timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val lat = String.format("%.6f", location.latitude)
            val lng = String.format("%.6f", location.longitude)
            val fileName = "${lat}_${lng}_${timestamp}.3gp"

            val directory = File(saveDir)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            recordingFilePath = File(directory, fileName).absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordingFilePath)
                prepare()
                start()
            }

            infoText.text = "Recording for 10 seconds..."

            // Stop recording after 10 seconds
            handler.postDelayed({
                stopRecording()
            }, 10000)

        } catch (e: Exception) {
            Toast.makeText(this, "Recording failed: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            infoText.text = "Recording saved"

            // Speak recording stopped
            speakRecordingStopped()

        } catch (e: Exception) {
            Toast.makeText(this, "Error stopping recording: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakRecordingStopped() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                runOnUiThread {
                    launchTriggerApp()
                }
            }

            override fun onError(utteranceId: String?) {
                runOnUiThread {
                    launchTriggerApp()
                }
            }
        })

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "recording_stop")
        textToSpeech?.speak(getString(R.string.recording_stopped),
            TextToSpeech.QUEUE_FLUSH, params, "recording_stop")
    }

    private fun launchTriggerApp() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val triggerApp = prefs.getString("triggerApp", null)

        if (!triggerApp.isNullOrEmpty()) {
            try {
                val intent = packageManager.getLaunchIntentForPackage(triggerApp)
                if (intent != null) {
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Cannot launch trigger app", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error launching app: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        textToSpeech?.shutdown()
        mediaRecorder?.release()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        // Restart the process if setup is now complete
        if (!isFirstRun() && currentLocation == null) {
            startRecordingProcess()
        }
    }
}
