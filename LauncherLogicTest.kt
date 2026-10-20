package com.crystal.launcher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherLogicTest {
    @Test
    fun blankSearchMatchesEveryApp() {
        assertTrue(appMatchesSearch("Camera", "com.example.camera", ""))
        assertTrue(appMatchesSearch("Camera", "com.example.camera", "   "))
    }

    @Test
    fun labelSearchIsCaseInsensitive() {
        assertTrue(appMatchesSearch("Google Camera", "com.google.camera", "camera"))
        assertTrue(appMatchesSearch("Google Camera", "com.google.camera", "GOOGLE"))
    }

    @Test
    fun packageNameCanBeSearched() {
        assertTrue(appMatchesSearch("Settings", "com.android.settings", "android.settings"))
    }

    @Test
    fun unrelatedQueryDoesNotMatch() {
        assertFalse(appMatchesSearch("Settings", "com.android.settings", "calculator"))
    }
}
