package com.hanclip.android.core.safety

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDurationStepPolicyTest {
    @Test
    fun `steps across every duration interval boundary`() {
        assertEquals(0.9, steppedDefaultDuration(1.0, increase = false), 0.0)
        assertEquals(1.5, steppedDefaultDuration(1.0, increase = true), 0.0)
        assertEquals(10.0, steppedDefaultDuration(9.5, increase = true), 0.0)
        assertEquals(11.0, steppedDefaultDuration(10.0, increase = true), 0.0)
        assertEquals(10.0, steppedDefaultDuration(11.0, increase = false), 0.0)
    }

    @Test
    fun `keeps minimum and maximum stable`() {
        assertEquals(0.1, steppedDefaultDuration(0.1, increase = false), 0.0)
        assertEquals(30.0, steppedDefaultDuration(30.0, increase = true), 0.0)
    }

    @Test
    fun `moves an old off-grid value to the nearest allowed neighbor`() {
        assertEquals(3.5, steppedDefaultDuration(3.7, increase = false), 0.0)
        assertEquals(4.0, steppedDefaultDuration(3.7, increase = true), 0.0)
    }

    @Test
    fun `photo duration never leaves the supported range`() {
        assertEquals(0.1, normalizedPhotoDuration(-0.9), 0.0)
        assertEquals(12.0, normalizedPhotoDuration(12.0), 0.0)
        assertEquals(1.1, normalizedPhotoDuration(1.06), 0.0)
        assertEquals(30.0, normalizedPhotoDuration(31.0), 0.0)
    }
}
