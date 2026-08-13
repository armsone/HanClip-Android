package com.hanclip.android.feature.editor

import java.time.LocalDate
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

    @Test
    fun `previous day without a selection targets yesterday`() {
        val today = LocalDate.of(2026, 8, 14)

        assertEquals(
            LocalDate.of(2026, 8, 13),
            previousDaySelectionTarget(emptyList(), today)
        )
    }

    @Test
    fun `previous day uses the day before the earliest selected media date`() {
        val today = LocalDate.of(2026, 8, 14)
        val selectedDates = listOf(
            LocalDate.of(2026, 8, 12),
            LocalDate.of(2026, 8, 8),
            LocalDate.of(2026, 8, 10)
        )

        assertEquals(
            LocalDate.of(2026, 8, 7),
            previousDaySelectionTarget(selectedDates, today)
        )
    }
}
