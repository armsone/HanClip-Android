package com.hanclip.android.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class CaptionDateFormatterTest {
    @Test
    fun `single date follows the iOS Korean caption format`() {
        assertEquals("26. 8. 12. (수)", CaptionDateFormatter.single(LocalDate.of(2026, 8, 12)))
    }

    @Test
    fun `date range keeps the second date together`() {
        val first = LocalDate.of(2026, 8, 10).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val last = LocalDate.of(2026, 8, 12).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        assertEquals(
            "26. 8. 10.(월) ~\u00A026.\u00A08.\u00A012.(수)",
            CaptionDateFormatter.range(listOf(last, first), zoneId = ZoneOffset.UTC)
        )
    }
}
