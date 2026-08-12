package com.hanclip.android.feature.preview

import org.junit.Assert.assertEquals
import org.junit.Test

class FullscreenPlaybackPolicyTest {
    @Test
    fun `single tap pauses active playback`() {
        assertEquals(
            FullscreenPlaybackTapAction.Pause,
            fullscreenPlaybackTapAction(isPlaying = true, hasEnded = false)
        )
    }

    @Test
    fun `single tap resumes paused playback`() {
        assertEquals(
            FullscreenPlaybackTapAction.Play,
            fullscreenPlaybackTapAction(isPlaying = false, hasEnded = false)
        )
    }

    @Test
    fun `single tap restarts ended playback`() {
        assertEquals(
            FullscreenPlaybackTapAction.ReplayFromStart,
            fullscreenPlaybackTapAction(isPlaying = false, hasEnded = true)
        )
    }

    @Test
    fun `aspect toggle is only shown in landscape`() {
        assertEquals(true, shouldShowFullscreenAspectToggle(800f, 400f))
        assertEquals(false, shouldShowFullscreenAspectToggle(400f, 800f))
        assertEquals(false, shouldShowFullscreenAspectToggle(600f, 600f))
    }
}
