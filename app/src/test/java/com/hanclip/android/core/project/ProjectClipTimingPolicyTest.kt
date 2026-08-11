package com.hanclip.android.core.project

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectClipTimingPolicyTest {
    @Test
    fun `stored video trim stays inside source duration`() {
        val timing = normalizeStoredClipTiming(
            isTimelineVideo = true,
            durationSeconds = 8.0,
            photoDurationSeconds = 3.0,
            sourceDurationSeconds = 10.0,
            trimStartSeconds = 7.0
        )

        assertEquals(8.0, timing.durationSeconds, 0.0)
        assertEquals(2.0, timing.trimStartSeconds, 0.0)
        assertEquals(10.0, timing.trimStartSeconds + timing.durationSeconds, 0.0)
    }

    @Test
    fun `invalid stored timing uses safe bounds`() {
        val video = normalizeStoredClipTiming(true, -4.0, 90.0, 0.05, -2.0)
        val photo = normalizeStoredClipTiming(false, 90.0, -1.0, null, 5.0)

        assertEquals(0.1, video.durationSeconds, 0.0)
        assertEquals(0.1, video.sourceDurationSeconds ?: 0.0, 0.0)
        assertEquals(0.0, video.trimStartSeconds, 0.0)
        assertEquals(30.0, video.photoDurationSeconds, 0.0)
        assertEquals(30.0, photo.durationSeconds, 0.0)
        assertEquals(0.1, photo.photoDurationSeconds, 0.0)
        assertEquals(0.0, photo.trimStartSeconds, 0.0)
    }
}
