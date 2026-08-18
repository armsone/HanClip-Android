package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class MusicDurationTextTest {
    @Test
    fun `formats imported music shorter than one hour`() {
        assertEquals("3:07", formatMusicDuration(187_000L))
    }

    @Test
    fun `formats imported music longer than one hour`() {
        assertEquals("1:02:09", formatMusicDuration(3_729_000L))
    }

    @Test
    fun `positive subsecond music does not display zero duration`() {
        assertEquals("0:01", formatMusicDuration(500L))
    }

    @Test
    fun `rounds down when under the half second boundary`() {
        assertEquals("0:01", formatMusicDuration(1_400L))
    }

    @Test
    fun `rounds up when at or past the half second boundary`() {
        assertEquals("0:02", formatMusicDuration(1_500L))
        assertEquals("0:02", formatMusicDuration(1_900L))
    }

    @Test
    fun `rounding up a full second carries into minutes`() {
        assertEquals("1:00", formatMusicDuration(59_500L))
    }
}
