package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class MediaPickerSavedStatePolicyTest {
    @Test
    fun `month and selected dates survive saved state round trip`() {
        val month = YearMonth.of(2026, 8)
        val dates = linkedSetOf(LocalDate.of(2026, 8, 12), LocalDate.of(2026, 8, 3))

        assertEquals(month, decodeSavedYearMonth(encodeSavedYearMonth(month)))
        assertEquals(dates, decodeSavedDateSet(encodeSavedDateSet(dates)))
        assertNull(decodeSavedYearMonth("not-a-month"))
    }

    @Test
    fun `duration filter restores only complete positive values`() {
        assertEquals(
            "AtMost" to 90,
            decodeSavedVideoFilter(encodeSavedVideoFilter("AtMost", 90))
        )
        assertNull(decodeSavedVideoFilter(encodeSavedVideoFilter(null, null)))
        assertNull(decodeSavedVideoFilter("AtLeast|0"))
        assertNull(decodeSavedVideoFilter("corrupt"))
    }
}
