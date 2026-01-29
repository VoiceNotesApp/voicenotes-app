package com.voicenotes.motorcycle

import com.voicenotes.motorcycle.database.Recording
import com.voicenotes.motorcycle.database.V2SStatus
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
        println("\n=== Test: Download Button Visibility with Existing File ===")
        
        // Create a temporary file for testing
        val tempFile = File.createTempFile("test_recording", ".ogg")
        tempFile.deleteOnExit()
        
        println("Created temp file: ${tempFile.absolutePath}")
        println("File exists: ${tempFile.exists()}")
        
        // Create recording with different statuses but same file
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
            println("Status: $status, File exists: ${tempFile.exists()}, Should show: $shouldShow")
            
            assertTrue(
                "Download button should be visible for $status when file exists",
                shouldShow
            )
        }
        
        println("✓ All statuses with existing file show download button")
    }

    @Test
    fun testDownloadButtonVisibility_WithNonExistentFile() {
        println("\n=== Test: Download Button Visibility with Non-existent File ===")
        
        // Use a file path that doesn't exist
        val nonExistentPath = "/tmp/nonexistent_file_${System.currentTimeMillis()}.ogg"
        val file = File(nonExistentPath)
        
        println("Testing with path: $nonExistentPath")
        println("File exists: ${file.exists()}")
        
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
            println("Status: $status, File exists: false, Should show: $shouldShow")
            
            assertFalse(
                "Download button should not be visible for $status when file doesn't exist",
                shouldShow
            )
        }
        
        println("✓ All statuses without existing file hide download button")
    }

    @Test
    fun testDownloadButtonVisibility_CompletedStatusRegardlessOfTranscription() {
        println("\n=== Test: Download Button Visibility for COMPLETED Status ===")
        
        // Create a temporary file
        val tempFile = File.createTempFile("test_completed", ".ogg")
        tempFile.deleteOnExit()
        
        println("Created temp file: ${tempFile.absolutePath}")
        
        // Test COMPLETED status with transcription
        val recordingWithText = createRecording(
            filepath = tempFile.absolutePath,
            v2sStatus = V2SStatus.COMPLETED,
            v2sResult = "Transcribed text here"
        )
        
        val showWithText = shouldShowDownloadButton(recordingWithText)
        println("COMPLETED with transcription: Should show = $showWithText")
        assertTrue(
            "Download button should show for COMPLETED with transcription",
            showWithText
        )
        
        // Test COMPLETED status without transcription (empty result)
        val recordingWithoutText = createRecording(
            filepath = tempFile.absolutePath,
            v2sStatus = V2SStatus.COMPLETED,
            v2sResult = ""
        )
        
        val showWithoutText = shouldShowDownloadButton(recordingWithoutText)
        println("COMPLETED without transcription: Should show = $showWithoutText")
        assertTrue(
            "Download button should show for COMPLETED without transcription if file exists",
            showWithoutText
        )
        
        // Test COMPLETED status with null result
        val recordingWithNull = createRecording(
            filepath = tempFile.absolutePath,
            v2sStatus = V2SStatus.COMPLETED,
            v2sResult = null
        )
        
        val showWithNull = shouldShowDownloadButton(recordingWithNull)
        println("COMPLETED with null transcription: Should show = $showWithNull")
        assertTrue(
            "Download button should show for COMPLETED with null transcription if file exists",
            showWithNull
        )
        
        println("✓ COMPLETED status shows download button regardless of transcription when file exists")
    }

    @Test
    fun testProcessingAnimationCondition() {
        println("\n=== Test: Processing Animation Condition ===")
        
        // Test that PROCESSING status should trigger animation
        val processingRecording = createRecording(
            v2sStatus = V2SStatus.PROCESSING
        )
        
        val shouldAnimate = shouldStartProcessingAnimation(processingRecording)
        println("Status: PROCESSING, Should animate: $shouldAnimate")
        assertTrue(
            "Processing animation should start for PROCESSING status",
            shouldAnimate
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
            val shouldAnim = shouldStartProcessingAnimation(recording)
            println("Status: $status, Should animate: $shouldAnim")
            
            assertFalse(
                "Processing animation should not start for $status status",
                shouldAnim
            )
        }
        
        println("✓ Processing animation condition correct for all statuses")
    }

    @Test
    fun testDownloadButtonVisibility_EdgeCases() {
        println("\n=== Test: Download Button Visibility Edge Cases ===")
        
        // Test with empty filepath
        val recordingEmptyPath = createRecording(
            filepath = "",
            v2sStatus = V2SStatus.COMPLETED,
            v2sResult = "text"
        )
        val showEmpty = shouldShowDownloadButton(recordingEmptyPath)
        println("Empty filepath: Should show = $showEmpty")
        assertFalse(
            "Download button should not show for empty filepath",
            showEmpty
        )
        
        // Test with very long filepath that doesn't exist
        val longPath = "/tmp/" + "a".repeat(1000) + ".ogg"
        val recordingLongPath = createRecording(
            filepath = longPath,
            v2sStatus = V2SStatus.COMPLETED
        )
        val showLong = shouldShowDownloadButton(recordingLongPath)
        println("Long non-existent filepath: Should show = $showLong")
        assertFalse(
            "Download button should not show for non-existent long filepath",
            showLong
        )
        
        println("✓ Edge cases handled correctly")
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
    // This mirrors the logic from shouldShowDownloadButton() in RecordingManagerActivity
    private fun shouldShowDownloadButton(recording: Recording): Boolean {
        // Show download button if the recording file exists
        val file = File(recording.filepath)
        return file.exists()
    }

    // Helper function to determine if processing animation should start
    // This mirrors the logic from updateTranscriptionUI() in RecordingManagerActivity
    private fun shouldStartProcessingAnimation(recording: Recording): Boolean {
        return recording.v2sStatus == V2SStatus.PROCESSING
    }
}
