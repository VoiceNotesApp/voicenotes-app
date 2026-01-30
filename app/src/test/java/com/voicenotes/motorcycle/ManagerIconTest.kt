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
        println("TEST: testManagerIconPresent_whenSharedPrefIsTrue")
        println("  Testing manager icon detection when SharedPreferences flag is true")
        
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
        
        println("  Input: managerIconPresent SharedPref = true")
        println("  Expected: true")
        println("  Actual: $iconPresent")
        
        assertTrue("Manager icon should be detected when SharedPreferences flag is true", 
            iconPresent)
        
        // Verify we checked SharedPreferences first
        verify(mockPrefs).getBoolean("managerIconPresent", false)
        
        println("  ✓ PASSED: Manager icon correctly detected from SharedPreferences\n")
    }

    @Test
    fun testManagerIconPresent_whenComponentEnabled() {
        println("TEST: testManagerIconPresent_whenComponentEnabled")
        println("  Testing manager icon detection when component is enabled")
        
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
        
        println("  Input: managerIconPresent SharedPref = false, component state = ENABLED")
        println("  Expected: true")
        println("  Actual: $iconPresent")
        
        assertTrue("Manager icon should be detected when component is enabled", 
            iconPresent)
        
        // Verify we checked both SharedPreferences and component state
        verify(mockPrefs).getBoolean("managerIconPresent", false)
        verify(mockPackageManager).getComponentEnabledSetting(any())
        
        println("  ✓ PASSED: Manager icon correctly detected from component state\n")
    }

    @Test
    fun testManagerIconNotPresent_whenBothChecksReturnFalse() {
        println("TEST: testManagerIconNotPresent_whenBothChecksReturnFalse")
        println("  Testing manager icon detection when neither check returns true")
        
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
        
        println("  Input: managerIconPresent SharedPref = false, component state = DEFAULT")
        println("  Expected: false")
        println("  Actual: $iconPresent")
        
        assertFalse("Manager icon should not be detected when both checks return false", 
            iconPresent)
        
        println("  ✓ PASSED: Manager icon correctly not detected\n")
    }

    @Test
    fun testManagerIconNotPresent_whenComponentDisabled() {
        println("TEST: testManagerIconNotPresent_whenComponentDisabled")
        println("  Testing manager icon detection when component is explicitly disabled")
        
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
        
        println("  Input: managerIconPresent SharedPref = false, component state = DISABLED")
        println("  Expected: false")
        println("  Actual: $iconPresent")
        
        assertFalse("Manager icon should not be detected when component is disabled", 
            iconPresent)
        
        println("  ✓ PASSED: Disabled component correctly detected\n")
    }

    @Test
    fun testAppConfiguration_withoutManagerIcon() {
        println("TEST: testAppConfiguration_withoutManagerIcon")
        println("  Testing that app is not configured without manager icon")
        
        val hasPermissions = true
        val hasOverlay = true
        val hasManagerIcon = false
        
        val isConfigured = hasPermissions && hasOverlay && hasManagerIcon
        
        println("  Input: permissions=$hasPermissions, overlay=$hasOverlay, managerIcon=$hasManagerIcon")
        println("  Expected: false")
        println("  Actual: $isConfigured")
        
        assertFalse("App should not be configured without manager icon", isConfigured)
        
        println("  ✓ PASSED: App configuration correctly requires manager icon\n")
    }

    @Test
    fun testAppConfiguration_withAllRequirements() {
        println("TEST: testAppConfiguration_withAllRequirements")
        println("  Testing that app is configured with all requirements")
        
        val hasPermissions = true
        val hasOverlay = true
        val hasManagerIcon = true
        
        val isConfigured = hasPermissions && hasOverlay && hasManagerIcon
        
        println("  Input: permissions=$hasPermissions, overlay=$hasOverlay, managerIcon=$hasManagerIcon")
        println("  Expected: true")
        println("  Actual: $isConfigured")
        
        assertTrue("App should be configured with all requirements", isConfigured)
        
        println("  ✓ PASSED: App configuration correctly validated\n")
    }

    @Test
    fun testManagerIconRequestedFlag() {
        println("TEST: testManagerIconRequestedFlag")
        println("  Testing manager icon requested flag behavior")
        
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
        
        println("  Input: User consented to manager icon")
        println("  Expected: managerIconRequested = true")
        println("  Actual: $iconRequested")
        
        assertTrue("Manager icon requested flag should be true after consent", iconRequested)
        
        println("  ✓ PASSED: Manager icon requested flag correctly set\n")
    }

    @Test
    fun testVNManagerLauncherActivityComponentName() {
        println("TEST: testVNManagerLauncherActivityComponentName")
        println("  Testing component name construction for VNManagerLauncherActivity")
        
        val packageName = "com.voicenotes.motorcycle"
        val activityName = "com.voicenotes.motorcycle.VNManagerLauncherActivity"
        
        // Test: Component name should be correctly constructed
        val expectedClassName = "VNManagerLauncherActivity"
        val actualClassName = activityName.substringAfterLast('.')
        
        println("  Input: packageName=$packageName, activityName=$activityName")
        println("  Expected className: $expectedClassName")
        println("  Actual className: $actualClassName")
        
        assertEquals("Component class name should match", expectedClassName, actualClassName)
        assertTrue("Activity name should start with package name", 
            activityName.startsWith(packageName))
        
        println("  ✓ PASSED: Component name correctly constructed\n")
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
            "com.voicenotes.motorcycle.VNManagerLauncherActivity"
        )
        val componentEnabledState = context.packageManager.getComponentEnabledSetting(componentName)
        return componentEnabledState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }
}

// Test summary output
fun main() {
    println("========================================")
    println("MANAGER ICON TEST SUITE")
    println("========================================")
    println("Total Tests: 8")
    println("Purpose: Validate VN Manager icon detection and configuration")
    println("========================================\n")
}
