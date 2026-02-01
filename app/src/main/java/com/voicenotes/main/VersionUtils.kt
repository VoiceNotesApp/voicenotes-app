package com.voicenotes.main

/**
 * Utility class for handling application version information.
 */
object VersionUtils {
    
    /**
     * Gets the formatted version string for display.
     * Returns "v<tag version>" if a tag is available, or "dev-<short commit hash>" if no tag is available.
     * 
     * The gradle build system (build.gradle) strips the 'v' prefix from git tags for VERSION_NAME,
     * so this function adds it back for display purposes.
     * 
     * Note: The current build.gradle configuration requires git tags to be present to build successfully.
     * If no tags are available, the build will fail. This function includes logic to handle commit hashes
     * for potential future modifications to the build system.
     * 
     * @return Formatted version string (e.g., "v1.0.0" or "dev-83343ed")
     */
    fun getVersionString(): String {
        // Get version from BuildConfig (set during gradle build)
        val version = BuildConfig.VERSION_NAME
        
        // Check if the version is a commit hash (7-40 hex characters, possibly with -dirty)
        // This would happen if gradle is modified to allow building without tags
        val cleanVersion = version.replace("-dirty", "")
        
        return if (cleanVersion.matches(Regex("^[0-9a-f]{7,40}$"))) {
            // No tag available, use dev-<hash> format
            "dev-$cleanVersion"
        } else {
            // Tag is available, add 'v' prefix if not already present
            if (version.startsWith("v")) {
                version
            } else {
                "v$version"
            }
        }
    }
}
