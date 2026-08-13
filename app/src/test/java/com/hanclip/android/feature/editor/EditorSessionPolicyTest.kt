package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorSessionPolicyTest {
    @Test
    fun `editor session starts collapsed without preview auto advance`() {
        assertFalse(DefaultClipSettingsExpanded)
        assertEquals(ClipPreviewPlaybackMode.Stop, DefaultClipPreviewPlaybackMode)
    }

    @Test
    fun `project is saved before preview export starts`() {
        val calls = mutableListOf<String>()

        val started = saveBeforePreviewExport(
            save = { calls += "save" },
            startExport = { calls += "export" },
            onSaveFailure = { calls += "failure" }
        )

        assertTrue(started)
        assertEquals(listOf("save", "export"), calls)
    }

    @Test
    fun `save failure blocks preview export`() {
        val calls = mutableListOf<String>()

        val started = saveBeforePreviewExport(
            save = {
                calls += "save"
                error("disk full")
            },
            startExport = { calls += "export" },
            onSaveFailure = { calls += "failure" }
        )

        assertFalse(started)
        assertEquals(listOf("save", "failure"), calls)
    }
}
