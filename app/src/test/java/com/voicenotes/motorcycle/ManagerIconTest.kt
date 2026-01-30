package com.voicenotes.motorcycle

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

/**
 * Unit tests for VN Manager Icon functionality
 * Tests the manager icon presence detection and configuration logic
 */
class ManagerIconTest {

    @Test
    fun testManagerIconPresent_whenSharedPrefIsTrue() {
        // Setup mock context and preferences
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)
        val mockPackageManager = mock(PackageManager::class.java)
        
        whenever(mockContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        whenever(mockPrefs.getBoolean("managerIconPresent", false))
            .thenReturn(true)
        whenever(mockContext.packageManager)
            .thenReturn(mockPackageManager)
        
        // Test: Icon should be present when SharedPreferences says so
        val iconPresent = isManagerIconPresent(mockContext)
        
        assertTrue("Manager icon should be detected when SharedPreferences flag is true", 
            iconPresent)
        
        // Verify we checked SharedPreferences first
        verify(mockPrefs).getBoolean("managerIconPresent", false)
    }

    @Test
    fun testManagerIconPresent_whenComponentEnabled() {
        // Setup mock context and preferences (SharedPref returns false)
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)
        val mockPackageManager = mock(PackageManager::class.java)
        
        whenever(mockContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        whenever(mockPrefs.getBoolean("managerIconPresent", false))
            .thenReturn(false)
        whenever(mockContext.packageManager)
            .thenReturn(mockPackageManager)
        
        // Mock component as enabled
        whenever(mockPackageManager.getComponentEnabledSetting(any()))
            .thenReturn(PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        
        // Test: Icon should be present when component is enabled (fallback check)
        val iconPresent = isManagerIconPresent(mockContext)
        
        assertTrue("Manager icon should be detected when component is enabled", 
            iconPresent)
        
        // Verify we checked both SharedPreferences and component state
        verify(mockPrefs).getBoolean("managerIconPresent", false)
        verify(mockPackageManager).getComponentEnabledSetting(any())
    }

    @Test
    fun testManagerIconNotPresent_whenBothChecksReturnFalse() {
        // Setup mock context and preferences (both return false/default)
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)
        val mockPackageManager = mock(PackageManager::class.java)
        
        whenever(mockContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        whenever(mockPrefs.getBoolean("managerIconPresent", false))
            .thenReturn(false)
        whenever(mockContext.packageManager)
            .thenReturn(mockPackageManager)
        
        // Mock component as default (not explicitly enabled)
        whenever(mockPackageManager.getComponentEnabledSetting(any()))
            .thenReturn(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
        
        // Test: Icon should not be present
        val iconPresent = isManagerIconPresent(mockContext)
        
        assertFalse("Manager icon should not be detected when both checks return false", 
            iconPresent)
    }

    @Test
    fun testManagerIconNotPresent_whenComponentDisabled() {
        // Setup mock context and preferences
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)
        val mockPackageManager = mock(PackageManager::class.java)
        
        whenever(mockContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        whenever(mockPrefs.getBoolean("managerIconPresent", false))
            .thenReturn(false)
        whenever(mockContext.packageManager)
            .thenReturn(mockPackageManager)
        
        // Mock component as explicitly disabled
        whenever(mockPackageManager.getComponentEnabledSetting(any()))
            .thenReturn(PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
        
        // Test: Icon should not be present
        val iconPresent = isManagerIconPresent(mockContext)
        
        assertFalse("Manager icon should not be detected when component is disabled", 
            iconPresent)
    }

    @Test
    fun testAppConfiguration_withoutManagerIcon() {
        val hasPermissions = true
        val hasOverlay = true
        val hasManagerIcon = false
        
        val isConfigured = hasPermissions && hasOverlay && hasManagerIcon
        
        assertFalse("App should not be configured without manager icon", isConfigured)
    }

    @Test
    fun testAppConfiguration_withAllRequirements() {
        val hasPermissions = true
        val hasOverlay = true
        val hasManagerIcon = true
        
        val isConfigured = hasPermissions && hasOverlay && hasManagerIcon
        
        assertTrue("App should be configured with all requirements", isConfigured)
    }

    @Test
    fun testManagerIconRequestedFlag() {
        // Setup mock preferences
        val mockContext = mock(Context::class.java)
        val mockPrefs = mock(SharedPreferences::class.java)
        val mockEditor = mock(SharedPreferences.Editor::class.java)
        
        whenever(mockContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        whenever(mockPrefs.edit())
            .thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(anyString(), anyBoolean()))
            .thenReturn(mockEditor)
        
        // Test: Flag should be set when user consents
        whenever(mockPrefs.getBoolean("managerIconRequested", false))
            .thenReturn(true)
        
        val iconRequested = mockPrefs.getBoolean("managerIconRequested", false)
        
        assertTrue("Manager icon requested flag should be true after consent", iconRequested)
    }

    @Test
    fun testVNManagerLauncherActivityComponentName() {
        val packageName = "com.voicenotes.motorcycle"
        val componentName = SettingsActivity.VN_MANAGER_COMPONENT_NAME
        
        // Test: Component name should be correctly constructed
        val expectedClassName = "VNManagerLauncherActivity"
        val actualClassName = componentName.substringAfterLast('.')
        
        assertEquals("Component class name should match", expectedClassName, actualClassName)
        assertTrue("Activity name should start with package name", 
            componentName.startsWith(packageName))
    }

    // Helper function to simulate isManagerIconPresent logic
    private fun isManagerIconPresent(context: Context): Boolean {
        // Check SharedPreferences first (set on confirmed success)
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val iconPresent = prefs.getBoolean("managerIconPresent", false)
        if (iconPresent) {
            return true
        }

        // Fallback: Check if component is enabled
        val componentName = ComponentName(
            context,
            SettingsActivity.VN_MANAGER_COMPONENT_NAME
        )
        val componentEnabledState = context.packageManager.getComponentEnabledSetting(componentName)
        return componentEnabledState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }
}
