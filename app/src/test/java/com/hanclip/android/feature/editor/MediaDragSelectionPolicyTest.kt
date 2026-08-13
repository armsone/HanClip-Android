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

    @Test
    fun `retreating during a selecting drag restores cells beyond the current point`() {
        val items = listOf("a", "b", "c", "d", "e")
        val initial = listOf("e")

        assertEquals(
            listOf("e", "a", "b", "c", "d"),
            dragSelectionFromAnchor(items, initial, anchor = "a", current = "d", selects = true)
        )
        assertEquals(
            listOf("e", "a", "b"),
            dragSelectionFromAnchor(items, initial, anchor = "a", current = "b", selects = true)
        )
    }

    @Test
    fun `retreating during a deselecting drag restores the retreated cells`() {
        val items = listOf("a", "b", "c", "d", "e")
        val initial = items

        assertEquals(
            listOf("e"),
            dragSelectionFromAnchor(items, initial, anchor = "a", current = "d", selects = false)
        )
        assertEquals(
            listOf("c", "d", "e"),
            dragSelectionFromAnchor(items, initial, anchor = "a", current = "b", selects = false)
        )
    }

    @Test
    fun `drag selection preserves selections outside the active range`() {
        val items = listOf("a", "b", "c", "d", "e")

        assertEquals(
            listOf("e", "b", "c"),
            dragSelectionFromAnchor(
                items,
                initialSelection = listOf("e"),
                anchor = "b",
                current = "c",
                selects = true
            )
        )
    }

    @Test
    fun `bottom auto scroll reaches full speed above the overlaid action bar`() {
        val viewportHeight = 1_000f
        val edgeSize = 100f
        val bottomOcclusion = 80f

        assertEquals(
            1f,
            mediaDragEdgeProgress(
                pointerY = 920f,
                viewportHeight = viewportHeight,
                edgeSize = edgeSize,
                bottomOcclusion = bottomOcclusion,
                direction = 1
            )
        )
        assertEquals(
            1f,
            mediaDragEdgeProgress(
                pointerY = 0f,
                viewportHeight = viewportHeight,
                edgeSize = edgeSize,
                bottomOcclusion = bottomOcclusion,
                direction = -1
            )
        )
    }

    @Test
    fun `bottom action area keeps selecting from the last unobscured grid position`() {
        assertEquals(
            919f,
            mediaDragProbeY(
                pointerY = 990f,
                viewportHeight = 1_000f,
                bottomOcclusion = 80f
            )
        )
        assertEquals(
            600f,
            mediaDragProbeY(
                pointerY = 600f,
                viewportHeight = 1_000f,
                bottomOcclusion = 80f
            )
        )
    }
}
