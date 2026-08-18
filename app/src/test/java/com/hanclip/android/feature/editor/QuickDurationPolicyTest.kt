package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class QuickDurationPolicyTest {
    @Test
    fun recommendedTimeUsesPointSevenSecondsPerScene() {
        assertEquals(4.9, quickRecommendedDuration(sceneCount = 7), 0.001)
    }

    @Test
    fun minimumTimeUsesPointTwoSecondsPerScene() {
        assertEquals(1.4, quickMinimumDuration(sceneCount = 7), 0.001)
    }

    @Test
    fun musicMatchingStaysDisabledUntilDurationIsKnown() {
        assertEquals(
            null,
            quickMusicTargetDuration(
                null,
                endingDurationSeconds = 0.0,
                availableContentCapacitySeconds = Double.POSITIVE_INFINITY,
                sceneCount = 5
            )
        )
        assertEquals(
            null,
            quickMusicTargetDuration(
                0.0,
                endingDurationSeconds = 0.0,
                availableContentCapacitySeconds = Double.POSITIVE_INFINITY,
                sceneCount = 5
            )
        )
    }

    @Test
    fun musicMatchingUsesConfirmedDurationWhenEndingIsDisabled() {
        assertEquals(
            42.5,
            quickMusicTargetDuration(
                42.5,
                endingDurationSeconds = 0.0,
                availableContentCapacitySeconds = Double.POSITIVE_INFINITY,
                sceneCount = 5
            )!!,
            0.001
        )
    }

    @Test
    fun musicMatchingSubtractsEnabledEndingDurationFromContentTarget() {
        assertEquals(
            40.0,
            quickMusicTargetDuration(
                42.5,
                endingDurationSeconds = 2.5,
                availableContentCapacitySeconds = Double.POSITIVE_INFINITY,
                sceneCount = 5
            )!!,
            0.001
        )
    }

    @Test
    fun musicMatchingDisabledWhenEndingConsumesAllMusicTime() {
        assertEquals(
            null,
            quickMusicTargetDuration(
                2.0,
                endingDurationSeconds = 5.0,
                availableContentCapacitySeconds = Double.POSITIVE_INFINITY,
                sceneCount = 5
            )
        )
    }

    @Test
    fun musicMatchingDisabledWhenRemainingTimeIsBelowMinimumSceneDuration() {
        assertEquals(
            null,
            quickMusicTargetDuration(
                1.0,
                endingDurationSeconds = 0.0,
                availableContentCapacitySeconds = Double.POSITIVE_INFINITY,
                sceneCount = 10
            )
        )
    }

    @Test
    fun musicMatchingDisabledWhenAvailableContentCapacityIsInsufficient() {
        assertEquals(
            null,
            quickMusicTargetDuration(
                10.0,
                endingDurationSeconds = 0.0,
                availableContentCapacitySeconds = 4.0,
                sceneCount = 5
            )
        )
    }

    @Test
    fun musicMatchingEnabledWhenAvailableContentCapacityExactlyCoversContentTarget() {
        assertEquals(
            8.0,
            quickMusicTargetDuration(
                10.0,
                endingDurationSeconds = 2.0,
                availableContentCapacitySeconds = 8.0,
                sceneCount = 5
            )!!,
            0.001
        )
    }

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
