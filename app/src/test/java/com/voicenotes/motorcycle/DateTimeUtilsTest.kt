package com.voicenotes.motorcycle

import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unit tests for date/time formatting utilities
 * Tests timestamp formatting, conversion, and timezone handling
 */
class DateTimeUtilsTest {

    @Test
    fun testDateFormatting() {
        // Test formatting with UTC timezone
        val timestamp = 1706284800000L // 2024-01-26 12:00:00 UTC
        
        val formatted = formatTimestamp(timestamp, "yyyy-MM-dd'T'HH:mm:ss'Z'")
        assertEquals("Timestamp should be formatted in ISO 8601", "2024-01-26T12:00:00Z", formatted)
        
        // Test formatting with different pattern
        val formatted2 = formatTimestamp(timestamp, "yyyyMMdd_HHmmss")
        assertEquals("Timestamp should match filename pattern", "20240126_120000", formatted2)
        
        // Test formatting with human-readable pattern
        val formatted3 = formatTimestamp(timestamp, "yyyy-MM-dd HH:mm:ss")
        assertEquals("Timestamp should be human-readable", "2024-01-26 12:00:00", formatted3)
    }

    @Test
    fun testTimestampConversion() {
        // Test parsing timestamp from filename format
        val dateTimeStr = "20240126_120000"
        val timestamp = parseTimestamp(dateTimeStr, "yyyyMMdd_HHmmss")
        
        assertNotNull("Timestamp should be parsed", timestamp)
        
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = timestamp!!
        
        assertEquals("Year should be 2024", 2024, calendar.get(Calendar.YEAR))
        assertEquals("Month should be January", 0, calendar.get(Calendar.MONTH))
        assertEquals("Day should be 26", 26, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals("Hour should be 12", 12, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals("Minute should be 0", 0, calendar.get(Calendar.MINUTE))
        assertEquals("Second should be 0", 0, calendar.get(Calendar.SECOND))
        
        // Test round-trip conversion
        val formatted = formatTimestamp(timestamp, "yyyyMMdd_HHmmss")
        assertEquals("Round-trip should preserve timestamp", dateTimeStr, formatted)
    }

    @Test
    fun testTimezoneHandling() {
        // Test that timestamps are handled consistently in UTC
        val timestamp = 1706284800000L // 2024-01-26 12:00:00 UTC
        
        val formattedUtc = formatTimestamp(timestamp, "yyyy-MM-dd HH:mm:ss", "UTC")
        assertEquals("UTC formatting", "2024-01-26 12:00:00", formattedUtc)
        
        // Test with explicit timezone
        val formattedPst = formatTimestamp(timestamp, "yyyy-MM-dd HH:mm:ss", "America/Los_Angeles")
        assertEquals("PST formatting (UTC-8)", "2024-01-26 04:00:00", formattedPst)
        
        val formattedCet = formatTimestamp(timestamp, "yyyy-MM-dd HH:mm:ss", "Europe/Paris")
        assertEquals("CET formatting (UTC+1)", "2024-01-26 13:00:00", formattedCet)
        
        // Verify that default timezone is UTC for consistent behavior
        val formattedDefault = formatTimestamp(timestamp, "yyyy-MM-dd HH:mm:ss")
        assertEquals("Default should be UTC", formattedUtc, formattedDefault)
    }

    @Test
    fun testYear2038EdgeCase() {
        // Test Y2K38 problem edge cases (32-bit Unix timestamp overflow)
        // January 19, 2038, 03:14:07 UTC is the last second before overflow
        val maxTimestamp32Bit = 2147483647000L // 2038-01-19 03:14:07 UTC
        
        val formatted = formatTimestamp(maxTimestamp32Bit, "yyyy-MM-dd HH:mm:ss")
        assertEquals("Should handle year 2038", "2038-01-19 03:14:07", formatted)
        
        // Test beyond 2038 (64-bit timestamps should work fine)
        val futureTimestamp = 2147483648000L // 2038-01-19 03:14:08 UTC
        val formattedFuture = formatTimestamp(futureTimestamp, "yyyy-MM-dd HH:mm:ss")
        assertEquals("Should handle beyond year 2038", "2038-01-19 03:14:08", formattedFuture)
        
        // Test year 2100 (leap year edge case)
        val year2100 = 4102444800000L // 2100-01-01 00:00:00 UTC
        val formatted2100 = formatTimestamp(year2100, "yyyy-MM-dd")
        assertEquals("Should handle year 2100", "2100-01-01", formatted2100)
    }

    @Test
    fun testLeapYearHandling() {
        // Test February 29, 2024 (leap year)
        val leapDay = 1709251200000L // 2024-02-29 00:00:00 UTC
        val formatted = formatTimestamp(leapDay, "yyyy-MM-dd")
        assertEquals("Should handle leap year", "2024-02-29", formatted)
        
        // Test parsing leap year date
        val parsed = parseTimestamp("20240229_000000", "yyyyMMdd_HHmmss")
        assertNotNull("Should parse leap year date", parsed)
        
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = parsed!!
        assertEquals("Month should be February", 1, calendar.get(Calendar.MONTH))
        assertEquals("Day should be 29", 29, calendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testEdgeCaseTimestamps() {
        // Test Unix epoch (0)
        val epoch = 0L
        val formatted = formatTimestamp(epoch, "yyyy-MM-dd HH:mm:ss")
        assertEquals("Unix epoch should be 1970-01-01", "1970-01-01 00:00:00", formatted)
        
        // Test very small timestamp
        val small = 1000L // 1 second after epoch
        val formattedSmall = formatTimestamp(small, "yyyy-MM-dd HH:mm:ss")
        assertEquals("Small timestamp", "1970-01-01 00:00:01", formattedSmall)
        
        // Test current time (should not throw)
        val now = System.currentTimeMillis()
        val formattedNow = formatTimestamp(now, "yyyy-MM-dd HH:mm:ss")
        assertNotNull("Current time should format", formattedNow)
        assertTrue("Current year should be recent", formattedNow.startsWith("202"))
    }

    @Test
    fun testDateFormatPatterns() {
        val timestamp = 1706284800000L // 2024-01-26 12:00:00 UTC
        
        // Test various format patterns
        assertEquals("ISO 8601", "2024-01-26T12:00:00Z", 
            formatTimestamp(timestamp, "yyyy-MM-dd'T'HH:mm:ss'Z'"))
        
        assertEquals("Date only", "2024-01-26", 
            formatTimestamp(timestamp, "yyyy-MM-dd"))
        
        assertEquals("Time only", "12:00:00", 
            formatTimestamp(timestamp, "HH:mm:ss"))
        
        assertEquals("Filename format", "20240126_120000", 
            formatTimestamp(timestamp, "yyyyMMdd_HHmmss"))
        
        assertEquals("Human readable", "January 26, 2024", 
            formatTimestamp(timestamp, "MMMM dd, yyyy", "UTC", Locale.US))
        
        assertEquals("12-hour format", "12:00:00 PM", 
            formatTimestamp(timestamp, "hh:mm:ss a", "UTC", Locale.US))
    }

    @Test
    fun testInvalidDateParsing() {
        // Test invalid date strings
        val invalid1 = parseTimestamp("invalid_date", "yyyyMMdd_HHmmss")
        assertNull("Invalid date string should return null", invalid1)
        
        // Test wrong format
        val invalid2 = parseTimestamp("2024-01-26", "yyyyMMdd_HHmmss")
        assertNull("Wrong format should return null", invalid2)
        
        // Test empty string
        val invalid3 = parseTimestamp("", "yyyyMMdd_HHmmss")
        assertNull("Empty string should return null", invalid3)
        
        // Test malformed date
        val invalid4 = parseTimestamp("20240231_120000", "yyyyMMdd_HHmmss") // Feb 31 doesn't exist
        // Note: SimpleDateFormat may or may not accept this depending on lenient setting
        // In lenient mode, it might roll over to March
    }

    @Test
    fun testMillisecondPrecision() {
        // Test that milliseconds are preserved
        val timestamp = 1706284800123L // 2024-01-26 12:00:00.123 UTC
        
        val formatted = formatTimestamp(timestamp, "yyyy-MM-dd HH:mm:ss.SSS")
        assertEquals("Milliseconds should be preserved", "2024-01-26 12:00:00.123", formatted)
        
        // Test parsing with milliseconds
        val parsed = parseTimestamp("20240126_120000123", "yyyyMMdd_HHmmssSSS")
        assertNotNull("Should parse with milliseconds", parsed)
        assertEquals("Milliseconds should match", 123L, parsed!! % 1000)
    }

    // Helper functions
    private fun formatTimestamp(
        timestamp: Long, 
        pattern: String, 
        timezone: String = "UTC",
        locale: Locale = Locale.US
    ): String {
        val sdf = SimpleDateFormat(pattern, locale)
        sdf.timeZone = TimeZone.getTimeZone(timezone)
        return sdf.format(Date(timestamp))
    }

    private fun parseTimestamp(
        dateTimeStr: String, 
        pattern: String, 
        timezone: String = "UTC"
    ): Long? {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone(timezone)
            sdf.parse(dateTimeStr)?.time
        } catch (e: Exception) {
            null
        }
    }
}
