package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class QuickDurationPolicyTest {
    @Test
    fun redistributesTimeWhenShortVideoCannotFillItsShare() {
        val allocated = allocateQuickDurations(
            capacities = listOf(1.0, Double.POSITIVE_INFINITY),
            targetDurationSeconds = 6.0
        )

        assertEquals(1.0, allocated[0], 0.001)
        assertEquals(5.0, allocated[1], 0.001)
    }

    @Test
    fun splitsTimeEvenlyWhenEverySceneCanFillItsShare() {
        val allocated = allocateQuickDurations(
            capacities = listOf(10.0, 10.0, 10.0),
            targetDurationSeconds = 6.0
        )
        assertEquals(listOf(2.0, 2.0, 2.0), allocated)
    }
}
