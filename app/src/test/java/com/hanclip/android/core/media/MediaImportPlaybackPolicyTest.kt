package com.hanclip.android.core.media

import com.hanclip.android.core.model.LivePhotoMode
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaImportPlaybackPolicyTest {
    @Test
    fun extractedMotionPhotoStartsInMotionModeAtMotionDuration() {
        val playback = initialImportedClipPlayback(
            motionDurationSeconds = 2.75,
            fallbackDurationSeconds = 4.0
        )

        assertEquals(LivePhotoMode.Motion, playback.livePhotoMode)
        assertEquals(2.75, playback.durationSeconds, 0.0)
    }

    @Test
    fun missingOrInvalidMotionUsesStillFallbackWithPositiveDuration() {
        val missing = initialImportedClipPlayback(null, 3.0)
        val invalid = initialImportedClipPlayback(Double.NaN, -2.0)

        assertEquals(LivePhotoMode.Still, missing.livePhotoMode)
        assertEquals(3.0, missing.durationSeconds, 0.0)
        assertEquals(LivePhotoMode.Still, invalid.livePhotoMode)
        assertEquals(0.1, invalid.durationSeconds, 0.0)
    }
}
