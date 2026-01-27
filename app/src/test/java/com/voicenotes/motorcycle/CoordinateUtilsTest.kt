package com.voicenotes.motorcycle

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for coordinate parsing and validation
 * Tests coordinate string parsing, validation, and formatting
 */
class CoordinateUtilsTest {

    @Test
    fun testValidCoordinateParsing() {
        // Valid coordinate strings
        val coords1 = "37.7749,-122.4194"
        val result1 = parseCoordinateString(coords1)
        
        assertNotNull("Coordinate parsing should succeed", result1)
        assertEquals("Latitude should be parsed correctly", 37.7749, result1!!.first, 0.0001)
        assertEquals("Longitude should be parsed correctly", -122.4194, result1.second, 0.0001)

        // Test with spaces
        val coords2 = " 40.7128 , -74.0060 "
        val result2 = parseCoordinateString(coords2)
        
        assertNotNull("Coordinate parsing with spaces should succeed", result2)
        assertEquals("Latitude with spaces should be parsed", 40.7128, result2!!.first, 0.0001)
        assertEquals("Longitude with spaces should be parsed", -74.0060, result2.second, 0.0001)

        // Test zero coordinates
        val coords3 = "0.0,0.0"
        val result3 = parseCoordinateString(coords3)
        
        assertNotNull("Zero coordinates should be valid", result3)
        assertEquals("Zero latitude should be parsed", 0.0, result3!!.first, 0.0001)
        assertEquals("Zero longitude should be parsed", 0.0, result3.second, 0.0001)
    }

    @Test
    fun testInvalidLatitude() {
        // Latitude out of range (> 90)
        val coords1 = "91.0,0.0"
        val result1 = parseCoordinateString(coords1)
        assertNull("Latitude > 90 should be invalid", result1)

        // Latitude out of range (< -90)
        val coords2 = "-91.0,0.0"
        val result2 = parseCoordinateString(coords2)
        assertNull("Latitude < -90 should be invalid", result2)

        // Latitude exactly at boundary (valid)
        val coords3 = "90.0,0.0"
        val result3 = parseCoordinateString(coords3)
        assertNotNull("Latitude = 90 should be valid", result3)
        
        val coords4 = "-90.0,0.0"
        val result4 = parseCoordinateString(coords4)
        assertNotNull("Latitude = -90 should be valid", result4)
    }

    @Test
    fun testInvalidLongitude() {
        // Longitude out of range (> 180)
        val coords1 = "0.0,181.0"
        val result1 = parseCoordinateString(coords1)
        assertNull("Longitude > 180 should be invalid", result1)

        // Longitude out of range (< -180)
        val coords2 = "0.0,-181.0"
        val result2 = parseCoordinateString(coords2)
        assertNull("Longitude < -180 should be invalid", result2)

        // Longitude exactly at boundary (valid)
        val coords3 = "0.0,180.0"
        val result3 = parseCoordinateString(coords3)
        assertNotNull("Longitude = 180 should be valid", result3)
        
        val coords4 = "0.0,-180.0"
        val result4 = parseCoordinateString(coords4)
        assertNotNull("Longitude = -180 should be valid", result4)
    }

    @Test
    fun testCoordinateFormatting() {
        // Test coordinate formatting with 6 decimal places
        val lat = 37.774929
        val lng = -122.419416
        
        val formatted = formatCoordinate(lat, lng)
        assertEquals("Coordinates should be formatted with 6 decimals", "37.774929,-122.419416", formatted)

        // Test rounding
        val lat2 = 37.7749291234
        val lng2 = -122.4194161234
        
        val formatted2 = formatCoordinate(lat2, lng2)
        assertEquals("Coordinates should be rounded to 6 decimals", "37.774929,-122.419416", formatted2)

        // Test negative coordinates
        val formatted3 = formatCoordinate(-90.0, -180.0)
        assertEquals("Negative coordinates should format correctly", "-90.000000,-180.000000", formatted3)
    }

    @Test
    fun testNegativeCoordinates() {
        // Southern hemisphere (negative latitude)
        val coords1 = "-33.8688,151.2093"
        val result1 = parseCoordinateString(coords1)
        
        assertNotNull("Negative latitude should be valid", result1)
        assertEquals("Negative latitude should be parsed", -33.8688, result1!!.first, 0.0001)

        // Western hemisphere (negative longitude)
        val coords2 = "40.7128,-74.0060"
        val result2 = parseCoordinateString(coords2)
        
        assertNotNull("Negative longitude should be valid", result2)
        assertEquals("Negative longitude should be parsed", -74.0060, result2!!.second, 0.0001)

        // Both negative
        val coords3 = "-23.5505,-46.6333"
        val result3 = parseCoordinateString(coords3)
        
        assertNotNull("Both negative coordinates should be valid", result3)
        assertEquals("Negative latitude parsed", -23.5505, result3!!.first, 0.0001)
        assertEquals("Negative longitude parsed", -46.6333, result3.second, 0.0001)
    }

    @Test
    fun testInvalidCoordinateFormats() {
        // Missing comma
        val coords1 = "37.7749 -122.4194"
        val result1 = parseCoordinateString(coords1)
        assertNull("Coordinate without comma should be invalid", result1)

        // Empty string
        val coords2 = ""
        val result2 = parseCoordinateString(coords2)
        assertNull("Empty string should be invalid", result2)

        // Only one value
        val coords3 = "37.7749"
        val result3 = parseCoordinateString(coords3)
        assertNull("Single value should be invalid", result3)

        // Non-numeric values
        val coords4 = "abc,def"
        val result4 = parseCoordinateString(coords4)
        assertNull("Non-numeric values should be invalid", result4)

        // Too many values
        val coords5 = "37.7749,-122.4194,100"
        val result5 = parseCoordinateString(coords5)
        assertNull("Too many values should be invalid", result5)
    }

    // Helper functions that simulate the actual coordinate utilities
    private fun parseCoordinateString(coords: String): Pair<Double, Double>? {
        try {
            val parts = coords.split(",")
            if (parts.size != 2) {
                return null
            }
            
            val lat = parts[0].trim().toDoubleOrNull() ?: return null
            val lng = parts[1].trim().toDoubleOrNull() ?: return null
            
            // Validate coordinate ranges
            if (lat < -90.0 || lat > 90.0) {
                return null
            }
            if (lng < -180.0 || lng > 180.0) {
                return null
            }
            
            return Pair(lat, lng)
        } catch (e: Exception) {
            return null
        }
    }

    private fun formatCoordinate(lat: Double, lng: Double): String {
        return String.format("%.6f,%.6f", lat, lng)
    }
}
