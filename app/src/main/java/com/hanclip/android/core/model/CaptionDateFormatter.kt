package com.hanclip.android.core.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object CaptionDateFormatter {
    private val singleDateFormatter = DateTimeFormatter.ofPattern("yy. M. d. (EEE)", Locale.KOREAN)

    fun single(date: LocalDate = LocalDate.now()): String = date.format(singleDateFormatter)

    fun range(
        createdAtMillis: List<Long>,
        fallbackDate: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val dates = createdAtMillis
            .map { millis -> Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate() }
            .sorted()
        val first = dates.firstOrNull() ?: fallbackDate
        val last = dates.lastOrNull() ?: first
        val firstText = single(first).replace(". (", ".(")
        if (first == last) return firstText

        val nonBreakingRecentDate = single(last)
            .replace(". (", ".(")
            .replace(" ", "\u00A0")
        return "$firstText ~\u00A0$nonBreakingRecentDate"
    }
}
