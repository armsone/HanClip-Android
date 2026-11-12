package com.hanclip.android.core.media

import com.hanclip.android.core.model.ClipProtectedAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtectedActionSegmenterTest {
    @Test
    fun keepsExistingSegmentationWithoutEvidence() {
        assertNull(
            ProtectedActionSegmenter.ranges(
                peaks = listOf(5.0),
                protectedActions = emptyList(),
                sourceDurationSeconds = 20.0,
                segmentDurationSeconds = 4.0
            )
        )
    }

    @Test
    fun followWindowDependsOnLoudAndSharpImpact() {
        val confirmed = ProtectedActionSegmenter.ranges(
            peaks = emptyList(),
            protectedActions = listOf(ClipProtectedAction(10.0, isLoud = true, isSharpTransient = true)),
            sourceDurationSeconds = 60.0,
            segmentDurationSeconds = 4.0
        )!!.single()
        assertEquals(7.5, confirmed.startSeconds, 1e-9)
        assertEquals(6.0, confirmed.durationSeconds, 1e-9)
        assertTrue(confirmed.isProtectedAction)

        // A quiet sound may precede a silent real stroke: keep 6 s after it.
        val uncertain = ProtectedActionSegmenter.ranges(
            peaks = emptyList(),
            protectedActions = listOf(ClipProtectedAction(10.0, isLoud = false, isSharpTransient = true)),
            sourceDurationSeconds = 60.0,
            segmentDurationSeconds = 4.0
        )!!.single()
        assertEquals(16.0, uncertain.startSeconds + uncertain.durationSeconds, 1e-9)
    }

    @Test
    fun ordinaryPeaksKeepCenteredWindowsAndInsidePeaksAreAbsorbed() {
        val ranges = ProtectedActionSegmenter.ranges(
            peaks = listOf(11.0, 40.0),
            protectedActions = listOf(ClipProtectedAction(10.0, isLoud = false, isSharpTransient = false)),
            sourceDurationSeconds = 60.0,
            segmentDurationSeconds = 4.0
        )!!
        assertEquals(2, ranges.size)
        assertTrue(ranges[0].isProtectedAction)
        assertEquals(38.0, ranges[1].startSeconds, 1e-9)
        assertEquals(4.0, ranges[1].durationSeconds, 1e-9)
    }

    @Test
    fun protectedRangesSurviveBeyondTheTwelvePeakLimit() {
        val actions = (0 until 14).map { ClipProtectedAction(10.0 + it * 20.0, isLoud = true, isSharpTransient = true) }
        val ranges = ProtectedActionSegmenter.ranges(
            peaks = emptyList(),
            protectedActions = actions,
            sourceDurationSeconds = 300.0,
            segmentDurationSeconds = 4.0
        )!!
        assertEquals(14, ranges.count { it.isProtectedAction })
    }
}
