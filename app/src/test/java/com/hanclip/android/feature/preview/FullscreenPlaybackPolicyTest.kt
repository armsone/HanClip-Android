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
}
