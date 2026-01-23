package com.voicenotes.motorcycle

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.voicenotes.motorcycle.database.RecordingDatabase
import com.voicenotes.motorcycle.database.Recording
import com.voicenotes.motorcycle.database.V2SStatus
import com.voicenotes.motorcycle.database.OsmStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import java.io.File

class BatchProcessingService : LifecycleService() {
    
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        val recordingId = intent?.getLongExtra("recordingId", -1L) ?: -1L
        
        lifecycleScope.launch {
            if (recordingId > 0) {
                // Process single recording
                processSingleRecording(recordingId)
            } else {
                // Process all pending recordings
                processAllFiles()
            }
        }
        
        return START_NOT_STICKY
    }
    
    private suspend fun processSingleRecording(recordingId: Long) {
        val db = RecordingDatabase.getDatabase(this@BatchProcessingService)
        val recording = withContext(Dispatchers.IO) {
            db.recordingDao().getRecordingById(recordingId)
        }
        
        if (recording == null) {
            Log.e("BatchProcessing", "Recording not found: $recordingId")
            stopSelf()
            return
        }
        
        DebugLogger.logInfo(
            service = "BatchProcessingService",
            message = "Processing single recording: ${recording.filename}"
        )
        
        processRecording(recording, 1, 1)
        stopSelf()
    }
    
    private suspend fun processAllFiles() {
        DebugLogger.logInfo(
            service = "BatchProcessingService",
            message = "Starting batch processing from database"
        )
        
        // Get all recordings that need processing from database
        val db = RecordingDatabase.getDatabase(this@BatchProcessingService)
        val recordings = withContext(Dispatchers.IO) {
            // Get recordings that haven't been transcribed yet
            db.recordingDao().getRecordingsByV2SStatus(V2SStatus.NOT_STARTED)
        }
        
        Log.d("BatchProcessing", "Found ${recordings.size} recordings to process")
        DebugLogger.logInfo(
            service = "BatchProcessingService",
            message = "Found ${recordings.size} recordings to process from database"
        )
        
        val totalFiles = recordings.size
        
        for ((index, recording) in recordings.withIndex()) {
            val currentFile = index + 1
            Log.d("BatchProcessing", "Processing recording $currentFile/$totalFiles: ${recording.filename}")
            DebugLogger.logInfo(
                service = "BatchProcessingService",
                message = "Processing recording $currentFile/$totalFiles: ${recording.filename}"
            )
            
            try {
                withTimeout(120000) { // 2 minute timeout per file
                    processRecording(recording, currentFile, totalFiles)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("BatchProcessingService", "Timeout processing recording: ${recording.filename}")
                DebugLogger.logError(
                    service = "BatchProcessingService",
                    error = "Recording processing timeout: ${recording.filename}",
                    exception = e
                )
                
                // Update recording status to ERROR
                withContext(Dispatchers.IO) {
                    val updated = recording.copy(
                        v2sStatus = V2SStatus.ERROR,
                        errorMsg = "Processing timeout",
                        updatedAt = System.currentTimeMillis()
                    )
                    db.recordingDao().updateRecording(updated)
                }
                
                // Send error status
                val errorProgressIntent = Intent("com.voicenotes.motorcycle.BATCH_PROGRESS")
                errorProgressIntent.putExtra("filename", recording.filename)
                errorProgressIntent.putExtra("status", "timeout")
                errorProgressIntent.putExtra("current", currentFile)
                errorProgressIntent.putExtra("total", totalFiles)
                sendBroadcast(errorProgressIntent)
                
            } catch (e: Exception) {
                Log.e("BatchProcessingService", "Error processing recording: ${recording.filename}", e)
                DebugLogger.logError(
                    service = "BatchProcessingService",
                    error = "Error processing recording: ${recording.filename}",
                    exception = e
                )
                
                // Update recording status to ERROR
                withContext(Dispatchers.IO) {
                    val updated = recording.copy(
                        v2sStatus = V2SStatus.ERROR,
                        errorMsg = e.message ?: "Unknown error",
                        updatedAt = System.currentTimeMillis()
                    )
                    db.recordingDao().updateRecording(updated)
                }
                
                // Send error status
                val errorProgressIntent = Intent("com.voicenotes.motorcycle.BATCH_PROGRESS")
                errorProgressIntent.putExtra("filename", recording.filename)
                errorProgressIntent.putExtra("status", "error")
                errorProgressIntent.putExtra("current", currentFile)
                errorProgressIntent.putExtra("total", totalFiles)
                sendBroadcast(errorProgressIntent)
            }
        }
        
        // Broadcast completion
        DebugLogger.logInfo(
            service = "BatchProcessingService",
            message = "Batch processing complete. Processed $totalFiles recordings."
        )
        sendBroadcast(Intent("com.voicenotes.motorcycle.BATCH_COMPLETE"))
        stopSelf()
    }
    
    private suspend fun processRecording(
        recording: Recording,
        currentFile: Int,
        totalFiles: Int
    ) {
            val db = RecordingDatabase.getDatabase(this@BatchProcessingService)
            val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val addOsmNote = prefs.getBoolean("addOsmNote", false)
            
            // Update status to PROCESSING
            withContext(Dispatchers.IO) {
                val updated = recording.copy(
                    v2sStatus = V2SStatus.PROCESSING,
                    updatedAt = System.currentTimeMillis()
                )
                db.recordingDao().updateRecording(updated)
            }
            
            // Broadcast progress with detailed status
            val progressIntent = Intent("com.voicenotes.motorcycle.BATCH_PROGRESS")
            progressIntent.putExtra("filename", recording.filename)
            progressIntent.putExtra("status", "transcribing")
            progressIntent.putExtra("current", currentFile)
            progressIntent.putExtra("total", totalFiles)
            sendBroadcast(progressIntent)
            
            // Transcribe file
            val transcriptionService = TranscriptionService(this)
            val result = transcriptionService.transcribeAudioFile(recording.filepath)
            
            result.onSuccess { transcribedText ->
                DebugLogger.logInfo(
                    service = "BatchProcessingService",
                    message = "Transcription successful for ${recording.filename}: $transcribedText"
                )
                
                // Update recording with transcription result
                withContext(Dispatchers.IO) {
                    val updated = recording.copy(
                        v2sStatus = V2SStatus.COMPLETED,
                        v2sResult = transcribedText,
                        v2sFallback = transcribedText.isBlank(),
                        updatedAt = System.currentTimeMillis()
                    )
                    db.recordingDao().updateRecording(updated)
                }
                
                // Create OSM note if enabled
                if (addOsmNote) {
                    val oauthManager = OsmOAuthManager(this)
                    if (oauthManager.isAuthenticated()) {
                        val osmProgressIntent = Intent("com.voicenotes.motorcycle.BATCH_PROGRESS")
                        osmProgressIntent.putExtra("filename", recording.filename)
                        osmProgressIntent.putExtra("status", "creating OSM note")
                        osmProgressIntent.putExtra("current", currentFile)
                        osmProgressIntent.putExtra("total", totalFiles)
                        sendBroadcast(osmProgressIntent)
                        
                        try {
                            // Update OSM status to PROCESSING
                            withContext(Dispatchers.IO) {
                                val updated = recording.copy(
                                    osmStatus = OsmStatus.PROCESSING,
                                    updatedAt = System.currentTimeMillis()
                                )
                                db.recordingDao().updateRecording(updated)
                            }
                            
                            val accessToken = oauthManager.getAccessToken()!!
                            val finalText = if (transcribedText.isBlank()) 
                                "${recording.latitude},${recording.longitude} (no text)" 
                            else 
                                transcribedText
                            
                            DebugLogger.logInfo(
                                service = "BatchProcessingService",
                                message = "Creating OSM note for ${recording.filename} at ${recording.latitude},${recording.longitude}"
                            )
                            
                            val osmService = OsmNotesService()
                            val osmResult = osmService.createNote(recording.latitude, recording.longitude, finalText, accessToken)
                            
                            osmResult.onSuccess {
                                // Update OSM status to COMPLETED
                                withContext(Dispatchers.IO) {
                                    val updated = recording.copy(
                                        osmStatus = OsmStatus.COMPLETED,
                                        osmResult = "Note created at ${recording.latitude},${recording.longitude}",
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    db.recordingDao().updateRecording(updated)
                                }
                            }.onFailure { osmError ->
                                // Update OSM status to ERROR
                                withContext(Dispatchers.IO) {
                                    val updated = recording.copy(
                                        osmStatus = OsmStatus.ERROR,
                                        errorMsg = "OSM: ${osmError.message}",
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    db.recordingDao().updateRecording(updated)
                                }
                            }
                        } else {
                        // Update OSM status to DISABLED (not authenticated)
                        withContext(Dispatchers.IO) {
                            val updated = recording.copy(
                                osmStatus = OsmStatus.DISABLED,
                                updatedAt = System.currentTimeMillis()
                            )
                            db.recordingDao().updateRecording(updated)
                        }
                    }
                }
                
                // Send completion status for this file
                val doneProgressIntent = Intent("com.voicenotes.motorcycle.BATCH_PROGRESS")
                doneProgressIntent.putExtra("filename", recording.filename)
                doneProgressIntent.putExtra("status", "complete")
                doneProgressIntent.putExtra("current", currentFile)
                doneProgressIntent.putExtra("total", totalFiles)
                sendBroadcast(doneProgressIntent)
                
            }.onFailure { error ->
                Log.e("BatchProcessing", "Failed to transcribe ${recording.filename}", error)
                DebugLogger.logError(
                    service = "BatchProcessingService",
                    error = "Failed to transcribe ${recording.filename}",
                    exception = error
                )
                
                // Update recording status to ERROR
                withContext(Dispatchers.IO) {
                    val updated = recording.copy(
                        v2sStatus = V2SStatus.ERROR,
                        errorMsg = error.message ?: "Transcription failed",
                        updatedAt = System.currentTimeMillis()
                    )
                    db.recordingDao().updateRecording(updated)
                }
                
                // Send error status
                val errorProgressIntent = Intent("com.voicenotes.motorcycle.BATCH_PROGRESS")
                errorProgressIntent.putExtra("filename", recording.filename)
                errorProgressIntent.putExtra("status", "error")
                errorProgressIntent.putExtra("current", currentFile)
                errorProgressIntent.putExtra("total", totalFiles)
                sendBroadcast(errorProgressIntent)
            }
    }
}
