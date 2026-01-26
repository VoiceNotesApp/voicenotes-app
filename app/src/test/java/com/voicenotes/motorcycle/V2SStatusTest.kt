package com.voicenotes.motorcycle

import com.voicenotes.motorcycle.database.V2SStatus
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for V2SStatus enum
 * Tests enum conversions, string representations, and status transitions
 */
class V2SStatusTest {

    @Test
    fun testEnumConversion() {
        // Test all enum values can be created from their names
        val notStarted = V2SStatus.valueOf("NOT_STARTED")
        assertEquals("NOT_STARTED enum should match", V2SStatus.NOT_STARTED, notStarted)
        
        val processing = V2SStatus.valueOf("PROCESSING")
        assertEquals("PROCESSING enum should match", V2SStatus.PROCESSING, processing)
        
        val completed = V2SStatus.valueOf("COMPLETED")
        assertEquals("COMPLETED enum should match", V2SStatus.COMPLETED, completed)
        
        val fallback = V2SStatus.valueOf("FALLBACK")
        assertEquals("FALLBACK enum should match", V2SStatus.FALLBACK, fallback)
        
        val error = V2SStatus.valueOf("ERROR")
        assertEquals("ERROR enum should match", V2SStatus.ERROR, error)
        
        val disabled = V2SStatus.valueOf("DISABLED")
        assertEquals("DISABLED enum should match", V2SStatus.DISABLED, disabled)
    }

    @Test
    fun testAllStatusValues() {
        // Ensure all expected values exist
        val values = V2SStatus.values()
        
        assertEquals("Should have 6 status values", 6, values.size)
        assertTrue("Should contain NOT_STARTED", values.contains(V2SStatus.NOT_STARTED))
        assertTrue("Should contain PROCESSING", values.contains(V2SStatus.PROCESSING))
        assertTrue("Should contain COMPLETED", values.contains(V2SStatus.COMPLETED))
        assertTrue("Should contain FALLBACK", values.contains(V2SStatus.FALLBACK))
        assertTrue("Should contain ERROR", values.contains(V2SStatus.ERROR))
        assertTrue("Should contain DISABLED", values.contains(V2SStatus.DISABLED))
    }

    @Test
    fun testStatusTransitions() {
        // Test typical status transition: NOT_STARTED -> PROCESSING -> COMPLETED
        var status = V2SStatus.NOT_STARTED
        assertTrue("Initial status should be NOT_STARTED", status == V2SStatus.NOT_STARTED)
        assertFalse("NOT_STARTED should not be final", isFinalStatus(status))
        
        status = V2SStatus.PROCESSING
        assertTrue("Status should transition to PROCESSING", status == V2SStatus.PROCESSING)
        assertFalse("PROCESSING should not be final", isFinalStatus(status))
        
        status = V2SStatus.COMPLETED
        assertTrue("Status should transition to COMPLETED", status == V2SStatus.COMPLETED)
        assertTrue("COMPLETED should be final", isFinalStatus(status))
        
        // Test error flow: NOT_STARTED -> PROCESSING -> ERROR
        status = V2SStatus.NOT_STARTED
        status = V2SStatus.PROCESSING
        status = V2SStatus.ERROR
        assertTrue("Status should be ERROR", status == V2SStatus.ERROR)
        assertTrue("ERROR should be final", isFinalStatus(status))
        
        // Test fallback flow: NOT_STARTED -> PROCESSING -> FALLBACK
        status = V2SStatus.NOT_STARTED
        status = V2SStatus.PROCESSING
        status = V2SStatus.FALLBACK
        assertTrue("Status should be FALLBACK", status == V2SStatus.FALLBACK)
        assertTrue("FALLBACK should be final", isFinalStatus(status))
        
        // Test disabled status
        status = V2SStatus.DISABLED
        assertTrue("Status should be DISABLED", status == V2SStatus.DISABLED)
        assertTrue("DISABLED should be final", isFinalStatus(status))
    }

    @Test
    fun testStatusProperties() {
        // Test NOT_STARTED properties
        assertFalse("NOT_STARTED should not be processing", isProcessing(V2SStatus.NOT_STARTED))
        assertFalse("NOT_STARTED should not be complete", isComplete(V2SStatus.NOT_STARTED))
        assertFalse("NOT_STARTED should not be error", isError(V2SStatus.NOT_STARTED))
        
        // Test PROCESSING properties
        assertTrue("PROCESSING should be processing", isProcessing(V2SStatus.PROCESSING))
        assertFalse("PROCESSING should not be complete", isComplete(V2SStatus.PROCESSING))
        assertFalse("PROCESSING should not be error", isError(V2SStatus.PROCESSING))
        
        // Test COMPLETED properties
        assertFalse("COMPLETED should not be processing", isProcessing(V2SStatus.COMPLETED))
        assertTrue("COMPLETED should be complete", isComplete(V2SStatus.COMPLETED))
        assertFalse("COMPLETED should not be error", isError(V2SStatus.COMPLETED))
        
        // Test FALLBACK properties
        assertFalse("FALLBACK should not be processing", isProcessing(V2SStatus.FALLBACK))
        assertTrue("FALLBACK should be complete", isComplete(V2SStatus.FALLBACK))
        assertFalse("FALLBACK should not be error", isError(V2SStatus.FALLBACK))
        
        // Test ERROR properties
        assertFalse("ERROR should not be processing", isProcessing(V2SStatus.ERROR))
        assertFalse("ERROR should not be complete", isComplete(V2SStatus.ERROR))
        assertTrue("ERROR should be error", isError(V2SStatus.ERROR))
        
        // Test DISABLED properties
        assertFalse("DISABLED should not be processing", isProcessing(V2SStatus.DISABLED))
        assertFalse("DISABLED should not be complete", isComplete(V2SStatus.DISABLED))
        assertFalse("DISABLED should not be error", isError(V2SStatus.DISABLED))
    }

    @Test
    fun testStatusOrdering() {
        // Test enum ordinal values are consistent
        val values = V2SStatus.values()
        
        assertEquals("NOT_STARTED should be first", 0, V2SStatus.NOT_STARTED.ordinal)
        assertEquals("PROCESSING should be second", 1, V2SStatus.PROCESSING.ordinal)
        assertEquals("COMPLETED should be third", 2, V2SStatus.COMPLETED.ordinal)
        assertEquals("FALLBACK should be fourth", 3, V2SStatus.FALLBACK.ordinal)
        assertEquals("ERROR should be fifth", 4, V2SStatus.ERROR.ordinal)
        assertEquals("DISABLED should be sixth", 5, V2SStatus.DISABLED.ordinal)
    }

    @Test
    fun testStatusToString() {
        // Test string representations
        assertEquals("NOT_STARTED toString", "NOT_STARTED", V2SStatus.NOT_STARTED.toString())
        assertEquals("PROCESSING toString", "PROCESSING", V2SStatus.PROCESSING.toString())
        assertEquals("COMPLETED toString", "COMPLETED", V2SStatus.COMPLETED.toString())
        assertEquals("FALLBACK toString", "FALLBACK", V2SStatus.FALLBACK.toString())
        assertEquals("ERROR toString", "ERROR", V2SStatus.ERROR.toString())
        assertEquals("DISABLED toString", "DISABLED", V2SStatus.DISABLED.toString())
    }

    @Test
    fun testStatusEquality() {
        // Test enum equality
        assertTrue("Same enum should be equal", V2SStatus.NOT_STARTED == V2SStatus.NOT_STARTED)
        assertFalse("Different enums should not be equal", V2SStatus.NOT_STARTED == V2SStatus.PROCESSING)
        
        // Test with valueOf
        val status1 = V2SStatus.valueOf("COMPLETED")
        val status2 = V2SStatus.COMPLETED
        assertTrue("valueOf should create equal enum", status1 == status2)
        
        // Test reference equality
        assertTrue("Enum should have reference equality", V2SStatus.NOT_STARTED === V2SStatus.NOT_STARTED)
    }

    // Helper functions
    private fun isFinalStatus(status: V2SStatus): Boolean {
        return when (status) {
            V2SStatus.COMPLETED, V2SStatus.FALLBACK, V2SStatus.ERROR, V2SStatus.DISABLED -> true
            else -> false
        }
    }

    private fun isProcessing(status: V2SStatus): Boolean {
        return status == V2SStatus.PROCESSING
    }

    private fun isComplete(status: V2SStatus): Boolean {
        return status == V2SStatus.COMPLETED || status == V2SStatus.FALLBACK
    }

    private fun isError(status: V2SStatus): Boolean {
        return status == V2SStatus.ERROR
    }
}
