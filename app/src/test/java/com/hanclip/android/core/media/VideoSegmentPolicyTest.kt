package com.hanclip.android.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoSegmentPolicyTest {
    @Test
    fun doesNotInventExtraPeaksWhenAnalysisFindsOnlyOne() {
        assertEquals(
            listOf(5.0),
            VideoSegmentPolicy.nonOverlappingPeaks(
                rawPeaks = listOf(5.0),
                fallbackPeak = 5.0,
                sourceDuration = 12.0,
                selectedDuration = 3.0
            )
        )
    }

    @Test
    fun rejectsWindowsThatOverlapEvenWhenPeakDistanceLooksLargeEnough() {
        assertEquals(
            listOf(3.0, 7.0),
            VideoSegmentPolicy.nonOverlappingPeaks(
                rawPeaks = listOf(3.0, 5.5, 7.0),
                fallbackPeak = 5.0,
                sourceDuration = 10.0,
                selectedDuration = 4.0
            )
        )
    }

    @Test
    fun deduplicatesPeaksWithinFiftyMilliseconds() {
        assertEquals(
            listOf(1.0, 2.0),
            VideoSegmentPolicy.nonOverlappingPeaks(
                rawPeaks = listOf(1.0, 1.049, 2.0),
                fallbackPeak = 1.0,
                sourceDuration = 3.0,
                selectedDuration = 0.5
            )
        )
    }
}
