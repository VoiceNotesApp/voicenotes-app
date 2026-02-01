package com.voicenotes.main

import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unit tests for filename generation and parsing
 * Tests filename pattern: latitude,longitude_YYYYMMDD_HHmmss.ext
 */
class FilenameUtilsTest {

    @Test
    fun testFilenameGeneration() {
        // Test filename generation with specific coordinates and timestamp
        val lat = 37.774929
        val lng = -122.419416
        val timestamp = 1706284800000L // 2024-01-26 12:00:00 UTC
        
        val filename = generateFilename(lat, lng, timestamp, "ogg")
        
        assertTrue("Filename should contain coordinates", filename.contains("37.774929,-122.419416"))
        assertTrue("Filename should contain timestamp", filename.matches(Regex(".*_\\d{8}_\\d{6}\\.ogg")))
        
        // Test with AMR extension
        val filenameAmr = generateFilename(lat, lng, timestamp, "amr")
        assertTrue("Filename should end with .amr", filenameAmr.endsWith(".amr"))
    }

    @Test
    fun testFilenamePatternMatching() {
        // Valid filenames
        val validFilename1 = "37.774929,-122.419416_20240126_120000.ogg"
        assertTrue("Valid filename should match pattern", isValidFilename(validFilename1))
        
        val validFilename2 = "-33.868800,151.209300_20240126_235959.amr"
        assertTrue("Valid filename with negative coords should match", isValidFilename(validFilename2))
        
        val validFilename3 = "0.000000,0.000000_20000101_000000.ogg"
        assertTrue("Valid filename with zero coords should match", isValidFilename(validFilename3))
        
        // Invalid filenames
        val invalidFilename1 = "invalid_filename.ogg"
        assertFalse("Invalid filename should not match pattern", isValidFilename(invalidFilename1))
        
        val invalidFilename2 = "37.774929_20240126_120000.ogg"
        assertFalse("Filename missing longitude should not match", isValidFilename(invalidFilename2))
        
        val invalidFilename3 = "37.774929,-122.419416.ogg"
        assertFalse("Filename missing timestamp should not match", isValidFilename(invalidFilename3))
    }

    @Test
    fun testCoordinateExtraction() {
        // Extract coordinates from valid filename
        val filename1 = "37.774929,-122.419416_20240126_120000.ogg"
        val coords1 = extractCoordinates(filename1)
        
        assertNotNull("Coordinates should be extracted", coords1)
        assertEquals("Latitude should match", 37.774929, coords1!!.first, 0.000001)
        assertEquals("Longitude should match", -122.419416, coords1.second, 0.000001)
        
        // Extract negative coordinates
        val filename2 = "-33.868800,151.209300_20240126_235959.amr"
        val coords2 = extractCoordinates(filename2)
        
        assertNotNull("Negative coordinates should be extracted", coords2)
        assertEquals("Negative latitude should match", -33.868800, coords2!!.first, 0.000001)
        assertEquals("Positive longitude should match", 151.209300, coords2.second, 0.000001)
        
        // Test with both negative
        val filename3 = "-23.550500,-46.633300_20240126_120000.ogg"
        val coords3 = extractCoordinates(filename3)
        
        assertNotNull("Both negative coordinates should be extracted", coords3)
        assertEquals("Negative latitude", -23.550500, coords3!!.first, 0.000001)
        assertEquals("Negative longitude", -46.633300, coords3.second, 0.000001)
    }

    @Test
    fun testTimestampParsing() {
        // Parse timestamp from valid filename
        val filename1 = "37.774929,-122.419416_20240126_120000.ogg"
        val timestamp1 = extractTimestamp(filename1)
        
        assertNotNull("Timestamp should be extracted", timestamp1)
        
        // Verify the parsed timestamp components
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = timestamp1!!
        
        assertEquals("Year should be 2024", 2024, calendar.get(Calendar.YEAR))
        assertEquals("Month should be January (0-indexed)", 0, calendar.get(Calendar.MONTH))
        assertEquals("Day should be 26", 26, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals("Hour should be 12", 12, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals("Minute should be 0", 0, calendar.get(Calendar.MINUTE))
        assertEquals("Second should be 0", 0, calendar.get(Calendar.SECOND))
        
        // Parse edge case: New Year's Eve
        val filename2 = "0.000000,0.000000_20231231_235959.ogg"
        val timestamp2 = extractTimestamp(filename2)
        
        assertNotNull("Edge case timestamp should be extracted", timestamp2)
        
        val calendar2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar2.timeInMillis = timestamp2!!
        
        assertEquals("Year should be 2023", 2023, calendar2.get(Calendar.YEAR))
        assertEquals("Month should be December", 11, calendar2.get(Calendar.MONTH))
        assertEquals("Day should be 31", 31, calendar2.get(Calendar.DAY_OF_MONTH))
        assertEquals("Hour should be 23", 23, calendar2.get(Calendar.HOUR_OF_DAY))
        assertEquals("Minute should be 59", 59, calendar2.get(Calendar.MINUTE))
        assertEquals("Second should be 59", 59, calendar2.get(Calendar.SECOND))
    }

    @Test
    fun testInvalidFilenamePattern() {
        // Missing coordinates
        val filename1 = "20240126_120000.ogg"
        assertNull("Filename without coordinates should return null", extractCoordinates(filename1))
        assertNull("Filename without coordinates should return null timestamp", extractTimestamp(filename1))
        
        // Malformed coordinates
        val filename2 = "invalid,coords_20240126_120000.ogg"
        assertNull("Filename with malformed coordinates should return null", extractCoordinates(filename2))
        
        // Malformed timestamp
        val filename3 = "37.774929,-122.419416_invalid_time.ogg"
        assertNull("Filename with malformed timestamp should return null", extractTimestamp(filename3))
        
        // Empty filename
        val filename4 = ""
        assertNull("Empty filename should return null", extractCoordinates(filename4))
        assertNull("Empty filename should return null timestamp", extractTimestamp(filename4))
        
        // Invalid coordinate range
        val filename5 = "91.000000,0.000000_20240126_120000.ogg"
        assertNull("Filename with invalid latitude should return null", extractCoordinates(filename5))
        
        val filename6 = "0.000000,181.000000_20240126_120000.ogg"
        assertNull("Filename with invalid longitude should return null", extractCoordinates(filename6))
    }

    // Helper functions
    private fun generateFilename(lat: Double, lng: Double, timestamp: Long, extension: String): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val timestampStr = sdf.format(Date(timestamp))
        val latStr = String.format("%.6f", lat)
        val lngStr = String.format("%.6f", lng)
        return "${latStr},${lngStr}_${timestampStr}.${extension}"
    }

    private fun isValidFilename(filename: String): Boolean {
        val pattern = Regex("""^-?\d+\.\d+,-?\d+\.\d+_\d{8}_\d{6}\.(ogg|amr)$""")
        return pattern.matches(filename)
    }

    private fun extractCoordinates(filename: String): Pair<Double, Double>? {
        try {
            // Remove extension
            val nameWithoutExt = filename.substringBeforeLast(".")
            
            // Split by underscore
            val parts = nameWithoutExt.split("_")
            if (parts.size < 3) return null
            
            // Parse coordinates
            val coords = parts[0].split(",")
            if (coords.size != 2) return null
            
            val lat = coords[0].toDoubleOrNull() ?: return null
            val lng = coords[1].toDoubleOrNull() ?: return null
            
            // Validate ranges
            if (lat < -90.0 || lat > 90.0) return null
            if (lng < -180.0 || lng > 180.0) return null
            
            return Pair(lat, lng)
        } catch (e: Exception) {
            return null
        }
    }

    private fun extractTimestamp(filename: String): Long? {
        try {
            // Remove extension
            val nameWithoutExt = filename.substringBeforeLast(".")
            
            // Split by underscore
            val parts = nameWithoutExt.split("_")
            if (parts.size < 3) return null
            
            // Combine date and time
            val dateTimeStr = "${parts[1]}_${parts[2]}"
            
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            
            return sdf.parse(dateTimeStr)?.time
        } catch (e: Exception) {
            return null
        }
    }
}
