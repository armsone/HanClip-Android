package com.hanclip.android.core.safety

import kotlin.math.roundToInt

private val AllowedDefaultDurationTenths =
    (1..10).toList() +
        (15..100 step 5).toList() +
        (110..300 step 10).toList()

internal fun steppedDefaultDuration(currentSeconds: Double, increase: Boolean): Double {
    val currentTenths = (currentSeconds * 10.0).roundToInt()
    val adjustedTenths = if (increase) {
        AllowedDefaultDurationTenths.firstOrNull { it > currentTenths }
    } else {
        AllowedDefaultDurationTenths.lastOrNull { it < currentTenths }
    }
    return (adjustedTenths ?: currentTenths.coerceIn(1, 300)) / 10.0
}
