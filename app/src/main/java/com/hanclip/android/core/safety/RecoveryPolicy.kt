package com.hanclip.android.core.safety

import java.io.File

internal fun <T> loadPrimaryOrBackup(
    primary: File,
    backup: File,
    decode: (String) -> T
): T? = sequenceOf(primary, backup)
    .mapNotNull { file -> runCatching { decode(file.readText()) }.getOrNull() }
    .firstOrNull()

internal fun <T> orderedCaptureValues(values: List<Pair<Long, T>>): List<T> =
    values.sortedBy(Pair<Long, T>::first).map(Pair<Long, T>::second)

internal fun steppedMediaColumnCount(current: Int, zoom: Float): Int {
    val steps = listOf(1, 3, 5, 8)
    val index = steps.indexOf(current).takeIf { it >= 0 } ?: 2
    return when {
        zoom > 1.12f -> steps[(index - 1).coerceAtLeast(0)]
        zoom < 0.88f -> steps[(index + 1).coerceAtMost(steps.lastIndex)]
        else -> steps[index]
    }
}

internal fun isDurationWithinTolerance(expectedSeconds: Double, actualSeconds: Double): Boolean {
    if (!expectedSeconds.isFinite() || !actualSeconds.isFinite() || actualSeconds <= 0.0) return false
    val tolerance = maxOf(1.0, expectedSeconds.coerceAtLeast(0.0) * 0.03)
    return kotlin.math.abs(actualSeconds - expectedSeconds) <= tolerance
}
