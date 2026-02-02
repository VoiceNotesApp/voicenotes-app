package com.voicenotes.main

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for VersionUtils.
 * 
 * Tests cover:
 * - Version string format: "Version <version>"
 * - Expected formats: "Version X.Y.Z", "Version dev-<hash>", "Version unknown"
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class VersionUtilsTest {
    
    @Test
    fun testGetVersionString_returnsNonEmptyString() {
        // When: Getting the version string
        val version = VersionUtils.getVersionString()
        
        // Then: Should not be empty
        assertNotNull("Version string should not be null", version)
        assertTrue("Version string should not be empty", version.isNotEmpty())
    }
    
    @Test
    fun testGetVersionString_startsWithVersion() {
        // When: Getting the version string
        val version = VersionUtils.getVersionString()
        
        // Then: Should start with "Version "
        assertTrue(
            "Version string should start with 'Version ', got: $version",
            version.startsWith("Version ")
        )
    }
    
    @Test
    fun testGetVersionString_hasValidFormat() {
        // When: Getting the version string
        val version = VersionUtils.getVersionString()
        
        // Then: Should match one of the expected formats:
        // - "Version X.Y.Z" (tagged release)
        // - "Version dev-<hash>" (untagged commit)
        // - "Version unknown" (no git)
        val versionPart = version.removePrefix("Version ")
        
        val isTaggedVersion = versionPart.matches(Regex("^[0-9]+\\.[0-9]+\\.[0-9]+.*$"))
        val isDevVersion = versionPart.startsWith("dev-")
        val isUnknown = versionPart == "unknown"
        
        assertTrue(
            "Version should be tagged (X.Y.Z), dev-<hash>, or unknown, got: $versionPart",
            isTaggedVersion || isDevVersion || isUnknown
        )
    }
    
    @Test
    fun testGetVersionString_devVersionHasValidHash() {
        // When: Getting the version string
        val version = VersionUtils.getVersionString()
        val versionPart = version.removePrefix("Version ")
        
        // Then: If it's a dev version, the hash should be valid hex
        if (versionPart.startsWith("dev-")) {
            val hashPart = versionPart.substring(4) // Remove "dev-" prefix
            assertTrue(
                "Commit hash should be 7+ hex characters, got: $hashPart",
                hashPart.matches(Regex("^[0-9a-f]{7,}$"))
            )
        }
    }
}
