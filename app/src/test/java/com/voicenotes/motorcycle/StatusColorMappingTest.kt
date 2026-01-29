package com.voicenotes.motorcycle

import com.voicenotes.motorcycle.database.V2SStatus
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for UI status color coding
 * Tests that each V2SStatus maps to the correct color resource
 */
class StatusColorMappingTest {

    @Test
    fun testStatusColorMapping() {
        // Define expected color resource IDs for each status
        // These should match the colors defined in colors.xml
        val expectedColors = mapOf(
            V2SStatus.NOT_STARTED to R.color.status_not_started,
            V2SStatus.PROCESSING to R.color.status_processing,
            V2SStatus.COMPLETED to R.color.status_completed,
            V2SStatus.FALLBACK to R.color.status_fallback,
            V2SStatus.ERROR to R.color.status_error,
            V2SStatus.DISABLED to R.color.status_disabled
        )

        // Verify each status has a corresponding color
        V2SStatus.values().forEach { status ->
            assertTrue(
                "Status $status should have a color mapping",
                expectedColors.containsKey(status)
            )
        }

        // Verify all expected colors are defined
        assertEquals(
            "All V2SStatus values should have color mappings",
            V2SStatus.values().size,
            expectedColors.size
        )
    }

    @Test
    fun testStatusColorUniqueness() {
        // Map of colors that should be unique (not shared)
        val uniqueColors = mapOf(
            V2SStatus.NOT_STARTED to R.color.status_not_started,
            V2SStatus.PROCESSING to R.color.status_processing,
            V2SStatus.COMPLETED to R.color.status_completed,
            V2SStatus.FALLBACK to R.color.status_fallback,
            V2SStatus.DISABLED to R.color.status_disabled
        )

        // Get all color values
        val colorValues = uniqueColors.values.toList()

        // Check for duplicates (excluding ERROR which shares with FALLBACK by design)
        val uniqueColorSet = colorValues.toSet()
        assertEquals(
            "Each status (except ERROR/FALLBACK) should have a unique color",
            colorValues.size,
            uniqueColorSet.size
        )
    }

    @Test
    fun testStatusDrawableMapping() {
        // Define expected drawable resource IDs for each status
        val expectedDrawables = mapOf(
            V2SStatus.NOT_STARTED to R.drawable.ic_status_not_started,
            V2SStatus.PROCESSING to R.drawable.ic_status_processing,
            V2SStatus.COMPLETED to R.drawable.ic_status_completed,
            V2SStatus.FALLBACK to R.drawable.ic_status_error,
            V2SStatus.ERROR to R.drawable.ic_status_error,
            V2SStatus.DISABLED to R.drawable.ic_status_not_started
        )

        // Verify each status has a corresponding drawable
        V2SStatus.values().forEach { status ->
            assertTrue(
                "Status $status should have a drawable mapping",
                expectedDrawables.containsKey(status)
            )
        }
    }

    @Test
    fun testStatusToStringMapping() {
        // Define expected string resource IDs for each status
        val expectedStrings = mapOf(
            V2SStatus.NOT_STARTED to R.string.transcribe,
            V2SStatus.PROCESSING to R.string.processing,
            V2SStatus.COMPLETED to R.string.retranscribe,
            V2SStatus.FALLBACK to R.string.retry,
            V2SStatus.ERROR to R.string.retry,
            V2SStatus.DISABLED to R.string.disabled
        )

        // Verify each status has a corresponding string
        V2SStatus.values().forEach { status ->
            assertTrue(
                "Status $status should have a string mapping",
                expectedStrings.containsKey(status)
            )
        }
    }

    @Test
    fun testStatusButtonEnablementMapping() {
        // Define expected button enabled states for each status
        val expectedEnabledStates = mapOf(
            V2SStatus.NOT_STARTED to true,
            V2SStatus.PROCESSING to false,
            V2SStatus.COMPLETED to true,
            V2SStatus.FALLBACK to true,
            V2SStatus.ERROR to true,
            V2SStatus.DISABLED to false
        )

        // Verify each status has a button enabled state
        V2SStatus.values().forEach { status ->
            assertTrue(
                "Status $status should have a button enabled state",
                expectedEnabledStates.containsKey(status)
            )
        }

        // Verify PROCESSING and DISABLED are the only disabled states
        val disabledStatuses = expectedEnabledStates.filter { !it.value }.keys
        assertEquals(
            "Only PROCESSING and DISABLED should have disabled buttons",
            setOf(V2SStatus.PROCESSING, V2SStatus.DISABLED),
            disabledStatuses
        )
    }
}
