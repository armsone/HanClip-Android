package com.hanclip.android.core.media

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal object VideoSegmentPolicy {
    fun nonOverlappingPeaks(
        rawPeaks: List<Double>,
        fallbackPeak: Double,
        sourceDuration: Double,
        selectedDuration: Double,
        limit: Int = 12
    ): List<Double> {
        val safeSourceDuration = sourceDuration.coerceAtLeast(0.1)
        val safeDuration = min(max(0.1, selectedDuration), safeSourceDuration)
        val ranked = rawPeaks.ifEmpty { listOf(fallbackPeak) }
            .mapNotNull { it.takeIf(Double::isFinite) }
            .map { it.coerceIn(0.0, safeSourceDuration) }
            .fold(emptyList<Double>()) { result, peak ->
                if (result.any { abs(it - peak) < 0.05 }) result else result + peak
            }
        val selected = mutableListOf<Triple<Double, Double, Double>>()
        val safeLimit = limit.coerceIn(1, 12)
        val minimumGap = max(0.75, safeDuration * 0.5)
        ranked.ifEmpty { listOf(safeSourceDuration / 2.0) }.forEach { peak ->
            if (selected.size >= safeLimit) return@forEach
            val start = max(
                0.0,
                min(safeSourceDuration - safeDuration, peak - safeDuration / 2.0)
            )
            val end = start + safeDuration
            if (selected.all { (_, selectedStart, selectedEnd) ->
                    end + minimumGap <= selectedStart + 0.001 ||
                        start >= selectedEnd + minimumGap - 0.001
                }
            ) {
                selected += Triple(peak, start, end)
            }
        }
        return selected.map { it.first }.sorted()
    }
}
