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
    fun `previous day without a selection chooses yesterday when it has media`() {
        val today = LocalDate.of(2026, 8, 14)

        assertEquals(
            LocalDate.of(2026, 8, 13),
            previousAvailableMediaDate(
                availableDates = listOf(
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 13),
                    LocalDate.of(2026, 8, 14)
                ),
                selectedDates = emptyList(),
                today = today
            )
        )
    }

    @Test
    fun `previous day skips empty dates before the earliest selection`() {
        val today = LocalDate.of(2026, 8, 14)
        val selectedDates = listOf(
            LocalDate.of(2026, 8, 12),
            LocalDate.of(2026, 8, 8),
            LocalDate.of(2026, 8, 10)
        )

        assertEquals(
            LocalDate.of(2026, 8, 5),
            previousAvailableMediaDate(
                availableDates = listOf(
                    LocalDate.of(2026, 8, 5),
                    LocalDate.of(2026, 8, 12),
                    LocalDate.of(2026, 8, 14)
                ),
                selectedDates = selectedDates,
                today = today
            )
        )
    }

    @Test
    fun `without a selection chooses the closest media date to yesterday`() {
        val today = LocalDate.of(2026, 8, 14)

        assertEquals(
            LocalDate.of(2026, 8, 11),
            previousAvailableMediaDate(
                availableDates = listOf(
                    LocalDate.of(2026, 8, 11),
                    LocalDate.of(2026, 8, 15)
                ),
                selectedDates = emptyList(),
                today = today
            )
        )
    }

    @Test
    fun `closest media date prefers the earlier day when distances tie`() {
        val today = LocalDate.of(2026, 8, 14)

        assertEquals(
            LocalDate.of(2026, 8, 11),
            previousAvailableMediaDate(
                availableDates = listOf(
                    LocalDate.of(2026, 8, 15),
                    LocalDate.of(2026, 8, 11)
                ),
                selectedDates = emptyList(),
                today = today
            )
        )
    }

    @Test
    fun `previous day does nothing when no earlier media exists`() {
        assertEquals(
            null,
            previousAvailableMediaDate(
                availableDates = listOf(LocalDate.of(2026, 8, 14)),
                selectedDates = listOf(LocalDate.of(2026, 8, 10)),
                today = LocalDate.of(2026, 8, 14)
            )
        )
    }
}
