package com.voicenotes.main

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Minimal instrumentation test: Launch SettingsActivity and assert that the
 * request permissions button is displayed. This is a deterministic smoke test
 * useful for verifying that instrumentation test plumbing and Firebase Test
 * Lab integration are working.
 */
@RunWith(AndroidJUnit4::class)
class SettingsActivityInstrumentationTest {

    @Test
    fun settingsActivity_showsRequestPermissionsButton() {
        ActivityScenario.launch(SettingsActivity::class.java).use { scenario ->
            // Wait for activity to be in resumed state and assert the view is visible
            onView(withId(R.id.requestPermissionsButton)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun settingsActivity_debugCard_visibilityBasedOnBuildType() {
        ActivityScenario.launch(SettingsActivity::class.java).use { scenario ->
            // Check that debug card visibility matches build type
            // In debug builds, it should be visible (VISIBLE)
            // In release builds, it should be gone (GONE)
            val expectedVisibility = if (BuildConfig.DEBUG) {
                Visibility.VISIBLE
            } else {
                Visibility.GONE
            }
            
            onView(withId(R.id.debugCard))
                .check(matches(withEffectiveVisibility(expectedVisibility)))
        }
    }
}
