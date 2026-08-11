package com.hanclip.android.core.media

import android.app.ActivityManager
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExportForegroundServiceInstrumentedTest {
    @Test
    fun serviceStartsCancelsMatchingTokenAndStops() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val token = 4_102L
        var cancelled = false

        ExportForegroundService.start(context, token, "계측 시험 제작 20%") {
            cancelled = true
        }
        waitForForegroundStart(token)

        assertFalse(ExportForegroundTaskBridge.cancel(token - 1))
        assertFalse(cancelled)
        assertTrue(ExportForegroundTaskBridge.cancel(token))
        assertTrue(cancelled)

        ExportForegroundService.stop(context, token)
        waitForServiceState(context, expectedRunning = false)
    }

    private fun waitForForegroundStart(token: Long) {
        repeat(20) {
            if (ExportForegroundTaskBridge.isForegroundStarted(token)) return
            Thread.sleep(50)
        }
        assertTrue("foreground service did not promote in time", false)
    }

    private fun waitForServiceState(context: Context, expectedRunning: Boolean) {
        val manager = context.getSystemService(ActivityManager::class.java)
        repeat(20) {
            val running = manager.getRunningServices(Int.MAX_VALUE).any { service ->
                service.service.className == ExportForegroundService::class.java.name
            }
            if (running == expectedRunning) return
            Thread.sleep(50)
        }
        val running = manager.getRunningServices(Int.MAX_VALUE).any { service ->
            service.service.className == ExportForegroundService::class.java.name
        }
        assertTrue("foreground service running=$running expected=$expectedRunning", running == expectedRunning)
    }
}
