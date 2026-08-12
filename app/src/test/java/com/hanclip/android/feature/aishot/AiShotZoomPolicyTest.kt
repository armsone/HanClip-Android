package com.hanclip.android.feature.aishot

import org.junit.Assert.assertEquals
import org.junit.Test

class AiShotZoomPolicyTest {
    @Test
    fun logarithmicControlKeepsLensStopsEvenlySpaced() {
        val minimum = 0.5f
        val maximum = 8f

        assertEquals(0f, zoomRatioToControlPosition(0.5f, minimum, maximum), 0.0001f)
        assertEquals(0.25f, zoomRatioToControlPosition(1f, minimum, maximum), 0.0001f)
        assertEquals(0.5f, zoomRatioToControlPosition(2f, minimum, maximum), 0.0001f)
        assertEquals(4f, zoomControlPositionToRatio(0.75f, minimum, maximum), 0.0001f)
        assertEquals(8f, zoomControlPositionToRatio(1f, minimum, maximum), 0.0001f)
    }
}
