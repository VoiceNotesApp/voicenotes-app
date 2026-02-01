package com.voicenotes.main

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for date/time formatting and parsing
 * Provides consistent timestamp handling across the application
 */
object DateTimeUtils {
    
    /**
     * Format a timestamp to a string using the specified pattern
     * 
     * @param timestamp The timestamp in milliseconds since epoch
     * @param pattern The SimpleDateFormat pattern to use
     * @param timezone The timezone to use (default: UTC)
     * @param locale The locale to use (default: US)
     * @return The formatted date/time string
     */
    fun formatTimestamp(
        timestamp: Long, 
        pattern: String, 
        timezone: String = "UTC",
        locale: Locale = Locale.US
    ): String {
        val sdf = SimpleDateFormat(pattern, locale)
        sdf.timeZone = TimeZone.getTimeZone(timezone)
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Parse a date/time string to a timestamp
     * 
     * @param dateTimeStr The date/time string to parse
     * @param pattern The SimpleDateFormat pattern to use
     * @param timezone The timezone to use (default: UTC)
     * @return The timestamp in milliseconds since epoch, or null if parsing fails
     */
    fun parseTimestamp(
        dateTimeStr: String, 
        pattern: String, 
        timezone: String = "UTC"
    ): Long? {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone(timezone)
            sdf.isLenient = false
            sdf.parse(dateTimeStr)?.time
        } catch (e: Exception) {
            null
        }
    }
}
