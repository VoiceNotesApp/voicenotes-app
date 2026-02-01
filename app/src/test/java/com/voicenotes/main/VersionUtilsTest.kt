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
 * - Version string formatting with tag
 * - Version string formatting without tag (commit hash)
 * - Proper 'v' prefix handling
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
    fun testGetVersionString_hasCorrectFormat() {
        // When: Getting the version string
        val version = VersionUtils.getVersionString()
        
        // Then: Should start with 'v' or 'dev-'
        assertTrue(
            "Version string should start with 'v' or 'dev-', got: $version",
            version.startsWith("v") || version.startsWith("dev-")
        )
    }
    
    @Test
    fun testGetVersionString_commitHashFormat() {
        // When: Version is a commit hash (like "83343ed")
        val version = VersionUtils.getVersionString()
        
        // Then: If it starts with 'dev-', it should have the commit hash
        if (version.startsWith("dev-")) {
            val hashPart = version.replace("-dirty", "").substring(4) // Remove "dev-" prefix and -dirty if present
            assertTrue(
                "Commit hash should be 7-40 hex characters, got: $hashPart",
                hashPart.matches(Regex("^[0-9a-f]{7,40}$"))
            )
        }
    }
    
    @Test
    fun testGetVersionString_tagFormat() {
        // When: Getting the version string
        val version = VersionUtils.getVersionString()
        
        // Then: If it starts with 'v' and is not a commit hash
        if (version.startsWith("v") && !version.startsWith("dev-")) {
            val versionPart = version.substring(1) // Remove 'v' prefix
            
            // Should contain version-like content (numbers, dots, dashes)
            // e.g., "1.0.0", "1.0.0-beta", "1.0.0-5-g83343ed", "1.0.0-dirty"
            assertTrue(
                "Version tag should contain valid characters, got: $versionPart",
                versionPart.matches(Regex("^[0-9a-zA-Z._-]+$"))
            )
        }
    }
    
    @Test
    fun testGetVersionString_stripsVPrefixAndAddsItBack() {
        // When: Getting the version string
        val version = VersionUtils.getVersionString()
        
        // Then: If it's a tag version, it should start with 'v'
        // (gradle strips 'v' from git tags, this function adds it back)
        if (!version.startsWith("dev-")) {
            assertTrue(
                "Tag version should start with 'v', got: $version",
                version.startsWith("v")
            )
        }
    }
    
    @Test
    fun testGetVersionString_dirtySuffixDocumentation() {
        // Note: This test documents the expected -dirty suffix preservation behavior.
        // Since BuildConfig.VERSION_NAME is set at build time and cannot be easily mocked
        // without additional dependencies, this test validates the current build state.
        //
        // Expected behavior (validated manually):
        // - Tag versions: "1.0.0-dirty" -> "v1.0.0-dirty"
        // - Commit hashes: "83343ed-dirty" -> "dev-83343ed-dirty"
        // - Without dirty: "1.0.0" -> "v1.0.0", "83343ed" -> "dev-83343ed"
        //
        // Manual testing: Build with uncommitted changes to verify -dirty is preserved
        
        val version = VersionUtils.getVersionString()
        
        // If the current build has -dirty, verify it's at the end
        if (version.contains("-dirty")) {
            assertTrue(
                "Version with -dirty suffix must end with -dirty, got: $version",
                version.endsWith("-dirty")
            )
        }
        
        // Verify version always starts with expected prefix
        assertTrue(
            "Version must start with 'v' or 'dev-', got: $version",
            version.startsWith("v") || version.startsWith("dev-")
        )
    }
}
