package com.hanclip.android.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportForegroundPolicyTest {
    @Test
    fun `notification progress is bounded`() {
        assertEquals(0, normalizedExportNotificationProgress(-1))
        assertEquals(42, normalizedExportNotificationProgress(42))
        assertEquals(100, normalizedExportNotificationProgress(101))
    }

    @Test
    fun `notification cancel only reaches matching active export`() {
        var cancellationCount = 0
        ExportForegroundTaskBridge.register(7L) { cancellationCount += 1 }

        assertFalse(ExportForegroundTaskBridge.cancel(6L))
        assertEquals(0, cancellationCount)
        assertTrue(ExportForegroundTaskBridge.cancel(7L))
        assertEquals(1, cancellationCount)
        assertTrue(ExportForegroundTaskBridge.cancelActive())
        assertEquals(2, cancellationCount)

        assertEquals(false, ExportForegroundTaskBridge.requestStop(6L))
        assertEquals(false, ExportForegroundTaskBridge.requestStop(7L))
        assertEquals(true, ExportForegroundTaskBridge.markForegroundStarted(7L))
        assertEquals(true, ExportForegroundTaskBridge.isForegroundStarted(7L))
        ExportForegroundTaskBridge.clear(7L)
        assertFalse(ExportForegroundTaskBridge.cancel(7L))
    }
}
