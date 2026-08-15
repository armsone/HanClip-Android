package com.hanclip.android.core.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepPreventionPolicyTest {
    @Test
    fun `AiShot always keeps the screen on regardless of selected mode`() {
        SleepPreventionMode.entries.forEach { mode ->
            assertTrue(shouldKeepScreenOn(mode, isAiShotActive = true, isWorkActive = false))
        }
    }

    @Test
    fun `outside AiShot selected mode controls work based screen prevention`() {
        assertTrue(shouldKeepScreenOn(SleepPreventionMode.AlwaysOn, false, false))
        assertFalse(shouldKeepScreenOn(SleepPreventionMode.AlwaysOff, false, true))
        assertTrue(shouldKeepScreenOn(SleepPreventionMode.Automatic, false, true))
        assertFalse(shouldKeepScreenOn(SleepPreventionMode.Automatic, false, false))
    }
}
