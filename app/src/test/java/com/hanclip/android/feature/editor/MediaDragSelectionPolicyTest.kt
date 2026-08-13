package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaDragSelectionPolicyTest {
    @Test
    fun `dragging across a row includes skipped cells`() {
        val items = listOf("a", "b", "c", "d", "e")

        assertEquals(listOf("a", "b", "c", "d"), inclusiveMediaDragRange(items, "a", "d"))
    }

    @Test
    fun `dragging backwards preserves the complete linear range`() {
        val items = listOf("a", "b", "c", "d", "e")

        assertEquals(listOf("b", "c", "d", "e"), inclusiveMediaDragRange(items, "e", "b"))
    }
}
