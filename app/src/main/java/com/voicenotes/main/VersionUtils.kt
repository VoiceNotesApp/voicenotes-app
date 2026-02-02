package com.voicenotes.main

/**
 * Utility class for handling application version information.
 */
object VersionUtils {
    
    /**
     * Gets the formatted version string for display.
     * 
     * Returns:
     * - "Version <version>" for tagged commits (e.g., "Version 0.0.15")
     * - "Version dev-<hash>" for untagged commits (e.g., "Version dev-83343ed")
     * - "Version unknown" if git is not available or no .git directory
     * 
     * @return Formatted version string
     */
    fun getVersionString(): String {
        val version = BuildConfig.VERSION_NAME
        return "Version $version"
    }
}
