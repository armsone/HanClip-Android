package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClipPreviewPolicyTest {
    @Test
    fun `auto next advances and loops to first clip`() {
        assertEquals(1, nextClipIndexOnPlaybackEnded(ClipPreviewPlaybackMode.AutoNext, 0, 3))
        assertEquals(2, nextClipIndexOnPlaybackEnded(ClipPreviewPlaybackMode.AutoNext, 1, 3))
        assertEquals(0, nextClipIndexOnPlaybackEnded(ClipPreviewPlaybackMode.AutoNext, 2, 3))
    }

    @Test
    fun `stop and loop never request another clip`() {
        assertNull(nextClipIndexOnPlaybackEnded(ClipPreviewPlaybackMode.Stop, 0, 3))
        assertNull(nextClipIndexOnPlaybackEnded(ClipPreviewPlaybackMode.Loop, 0, 3))
        assertNull(nextClipIndexOnPlaybackEnded(ClipPreviewPlaybackMode.AutoNext, -1, 3))
    }
}
