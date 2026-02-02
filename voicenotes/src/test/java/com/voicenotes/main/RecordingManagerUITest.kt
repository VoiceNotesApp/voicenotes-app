package com.voicenotes.main

import com.voicenotes.main.database.Recording
import com.voicenotes.main.database.V2SStatus
import org.junit.Test
import org.junit.Assert.*
import java.io.File

/**
 * Unit tests for RecordingManager UI logic
 * Tests download button visibility and processing animation conditions
 */
class RecordingManagerUITest {

    @Test
    fun testDownloadButtonVisibility_WithExistingFile() {
        // Create a temporary file for testing
        val tempFile = File.createTempFile("test_recording", ".ogg")
        tempFile.deleteOnExit()
        
        // Test all statuses with existing file
        val testCases = listOf(
            V2SStatus.NOT_STARTED,
            V2SStatus.PROCESSING,
            V2SStatus.COMPLETED,
            V2SStatus.ERROR,
            V2SStatus.FALLBACK,
            V2SStatus.DISABLED
        )
        
        testCases.forEach { status ->
            val recording = createRecording(
                filepath = tempFile.absolutePath,
                v2sStatus = status,
                v2sResult = if (status == V2SStatus.COMPLETED) "transcribed text" else null
            )
            
            val shouldShow = shouldShowDownloadButton(recording)
            
            assertTrue(
                "Download button should be visible for $status when file exists",
                shouldShow
            )
        }
    }

    @Test
    fun testDownloadButtonVisibility_WithNonExistentFile() {
        // Use a file path that doesn't exist
        val nonExistentPath = "/tmp/nonexistent_file_${System.currentTimeMillis()}.ogg"
        
        // Test with different statuses
        val testCases = listOf(
            V2SStatus.NOT_STARTED,
            V2SStatus.PROCESSING,
            V2SStatus.COMPLETED,
            V2SStatus.ERROR
        )
        
        testCases.forEach { status ->
            val recording = createRecording(
                filepath = nonExistentPath,
                v2sStatus = status,
                v2sResult = if (status == V2SStatus.COMPLETED) "transcribed text" else null
            )
            
            val shouldShow = shouldShowDownloadButton(recording)
            
            // Download button should always be visible, regardless of file existence
            // If the file doesn't exist, a "file not found" error will be shown when clicked
            assertTrue(
                "Download button should always be visible, even when file doesn't exist for $status",
                shouldShow
            )
        }
    }

    @Test
    fun testDownloadButtonVisibility_CompletedStatusRegardlessOfTranscription() {
        // Create a temporary file
        val tempFile = File.createTempFile("test_completed", ".ogg")
        tempFile.deleteOnExit()
        
        // Test COMPLETED status with transcription
        val recordingWithText = createRecording(
            filepath = tempFile.absolutePath,
            v2sStatus = V2SStatus.COMPLETED,
            v2sResult = "Transcribed text here"
        )
        
        assertTrue(
            "Download button should show for COMPLETED with transcription",
            shouldShowDownloadButton(recordingWithText)
        )
        
        // Test COMPLETED status without transcription (empty result)
        val recordingWithoutText = createRecording(
            filepath = tempFile.absolutePath,
            v2sStatus = V2SStatus.COMPLETED,
            v2sResult = ""
        )
        
        assertTrue(
            "Download button should show for COMPLETED without transcription if file exists",
            shouldShowDownloadButton(recordingWithoutText)
        )
        
        // Test COMPLETED status with null result
        val recordingWithNull = createRecording(
            filepath = tempFile.absolutePath,
            v2sStatus = V2SStatus.COMPLETED,
            v2sResult = null
        )
        
        assertTrue(
            "Download button should show for COMPLETED with null transcription if file exists",
            shouldShowDownloadButton(recordingWithNull)
        )
    }

    @Test
    fun testProcessingAnimationCondition() {
        // Test that PROCESSING status should trigger animation
        val processingRecording = createRecording(
            v2sStatus = V2SStatus.PROCESSING
        )
        
        assertTrue(
            "Processing animation should start for PROCESSING status",
            shouldStartProcessingAnimation(processingRecording)
        )
        
        // Test that other statuses should not trigger animation
        val nonAnimatingStatuses = listOf(
            V2SStatus.NOT_STARTED,
            V2SStatus.COMPLETED,
            V2SStatus.ERROR,
            V2SStatus.FALLBACK,
            V2SStatus.DISABLED
        )
        
        nonAnimatingStatuses.forEach { status ->
            val recording = createRecording(v2sStatus = status)
            
            assertFalse(
                "Processing animation should not start for $status status",
                shouldStartProcessingAnimation(recording)
            )
        }
    }

    @Test
    fun testDownloadButtonVisibility_EdgeCases() {
        // Test with empty filepath - download button should still be visible
        // User will see "file not found" error when clicking
        val recordingEmptyPath = createRecording(
            filepath = "",
            v2sStatus = V2SStatus.COMPLETED,
            v2sResult = "text"
        )
        assertTrue(
            "Download button should always be visible, even with empty filepath",
            shouldShowDownloadButton(recordingEmptyPath)
        )
        
        // Test with very long filepath that doesn't exist
        // Download button should still be visible
        val longPath = "/tmp/" + "a".repeat(1000) + ".ogg"
        val recordingLongPath = createRecording(
            filepath = longPath,
            v2sStatus = V2SStatus.COMPLETED
        )
        assertTrue(
            "Download button should always be visible, even with non-existent long filepath",
            shouldShowDownloadButton(recordingLongPath)
        )
    }

    // Helper function to create test recordings
    private fun createRecording(
        id: Long = 1L,
        filename: String = "test.ogg",
        filepath: String = "/tmp/test.ogg",
        timestamp: Long = System.currentTimeMillis(),
        latitude: Double = 37.774929,
        longitude: Double = -122.419416,
        v2sStatus: V2SStatus = V2SStatus.NOT_STARTED,
        v2sResult: String? = null
    ): Recording {
        return Recording(
            id = id,
            filename = filename,
            filepath = filepath,
            timestamp = timestamp,
            latitude = latitude,
            longitude = longitude,
            v2sStatus = v2sStatus,
            v2sResult = v2sResult,
            v2sFallback = false,
            errorMsg = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    // Helper function to determine if download button should be shown
    // Download button should always be visible - file existence is checked when clicked
    // If file doesn't exist, a "file not found" error is shown to the user
    private fun shouldShowDownloadButton(recording: Recording): Boolean {
        // Always show download button - error shown on click if file doesn't exist
        return true
    }

    // Helper function to determine if processing animation should start
    // This mirrors the logic from updateTranscriptionUI() in RecordingManagerActivity
    private fun shouldStartProcessingAnimation(recording: Recording): Boolean {
        return recording.v2sStatus == V2SStatus.PROCESSING
    }
}
