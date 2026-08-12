package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class TodaySelectionPolicyTest {
    @Test
    fun `first today tap moves and arms selection`() {
        assertEquals(
            TodaySelectionAction.MoveAndArm,
            todaySelectionAction(isArmed = false, hasTodayItems = true)
        )
    }

    @Test
    fun `second today tap selects visible today media`() {
        assertEquals(
            TodaySelectionAction.Select,
            todaySelectionAction(isArmed = true, hasTodayItems = true)
        )
    }

    @Test
    fun `today without media moves without arming`() {
        assertEquals(
            TodaySelectionAction.MoveOnly,
            todaySelectionAction(isArmed = false, hasTodayItems = false)
        )
    }
}
