package com.voicenotes.main

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility class for handling application version information.
 */
object VersionUtils {
    
    /**
     * Gets the formatted version string for display.
     * Returns "v<tag version>" if a tag is available, or "dev-<short commit hash>" if no tag is available.
     * 
     * @return Formatted version string
     */
    fun getVersionString(): String {
        // Get version from BuildConfig
        val version = BuildConfig.VERSION_NAME
        
        // Check if the version is a commit hash (7 hex characters, possibly with -dirty)
        // If it's just a commit hash, format it as dev-<hash>
        val cleanVersion = version.replace("-dirty", "")
        
        return if (cleanVersion.matches(Regex("^[0-9a-f]{7,40}$"))) {
            // No tag available, use dev-<hash> format
            "dev-$cleanVersion"
        } else {
            // Tag is available, add 'v' prefix if not present
            if (version.startsWith("v")) {
                version
            } else {
                "v$version"
            }
        }
    }
    
    /**
     * Gets the current git commit hash at runtime.
     * This is a fallback method if BuildConfig doesn't contain the commit hash.
     * 
     * @return Short commit hash or empty string if unavailable
     */
    fun getCurrentCommitHash(): String {
        return try {
            val process = Runtime.getRuntime().exec("git rev-parse --short HEAD")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()?.trim() ?: ""
            process.waitFor()
            output
        } catch (e: Exception) {
            ""
        }
    }
}
