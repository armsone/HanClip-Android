package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportProgressPolicyTest {
    @Test
    fun `cancel invalidates late progress and completion callbacks`() {
        val gate = ExportOperationGate()
        val exportToken = gate.begin()

        assertTrue(gate.isCurrent(exportToken))
        val cancellationToken = gate.invalidate()

        assertEquals(false, gate.isCurrent(exportToken))
        assertTrue(gate.isCurrent(cancellationToken))
    }

    @Test
    fun `progress message reports elapsed and estimated remaining time`() {
        val message = exportProgressMessage(
            progress = 0.25f,
            elapsedMillis = 10_000,
            attemptLabel = "3개 클립 · 1080x1920"
        )

        assertTrue(message.contains("25%"))
        assertTrue(message.contains("처리 00:10"))
        assertTrue(message.contains("예상 00:30 남음"))
        assertTrue(message.endsWith("3개 클립 · 1080x1920"))
    }

    @Test
    fun `duration formatting clamps negative and very long values`() {
        assertEquals("00:00", formatExportDuration(-1))
        assertEquals("01:05", formatExportDuration(65_999))
        assertEquals("99:59", formatExportDuration(Long.MAX_VALUE))
    }
}
