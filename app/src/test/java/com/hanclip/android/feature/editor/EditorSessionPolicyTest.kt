package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class EditorSessionPolicyTest {
    @Test
    fun `editor session starts collapsed without preview auto advance`() {
        assertFalse(DefaultClipSettingsExpanded)
        assertEquals(ClipPreviewPlaybackMode.Stop, DefaultClipPreviewPlaybackMode)
    }
}
