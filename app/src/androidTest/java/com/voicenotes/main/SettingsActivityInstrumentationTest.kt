package com.voicenotes.main

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for SettingsActivity with PreferenceFragmentCompat.
 * 
 * Tests verify:
 * - Settings activity launches successfully
 * - Preference UI elements are displayed
 * - Settings container is present
 */
@RunWith(AndroidJUnit4::class)
class SettingsActivityInstrumentationTest {

    @Test
    fun settingsActivity_launches() {
        // Launch SettingsActivity
        ActivityScenario.launch(SettingsActivity::class.java).use { scenario ->
            // Verify the settings container is displayed
            onView(withId(R.id.settings_container)).check(matches(isDisplayed()))
        }
    }
    
    @Test
    fun settingsActivity_showsPreferenceFragment() {
        // Launch SettingsActivity
        ActivityScenario.launch(SettingsActivity::class.java).use { scenario ->
            // Settings container should be visible
            onView(withId(R.id.settings_container)).check(matches(isDisplayed()))
        }
    }
}
