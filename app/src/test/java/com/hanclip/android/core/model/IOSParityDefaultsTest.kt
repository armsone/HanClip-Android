package com.hanclip.android.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSParityDefaultsTest {
    @Test
    fun `new watermark and Live Photo labels match iOS`() {
        val settings = WatermarkSettings()

        assertFalse(settings.isEnabled)
        assertTrue(settings.logoEnabled)
        assertEquals(CaptionDateFormatter.single(), settings.text)
        assertEquals("poppins", settings.fontName)
        assertEquals("#FFE45C", settings.textColorHex)
        assertEquals(0.75, settings.shadowOpacity, 0.0)
        assertEquals("#642BFF", settings.shadowColorHex)
        assertEquals(WatermarkFontSize.ExtraLarge, settings.fontSize)
        assertEquals(WatermarkPosition.UpperCenter, settings.position)
        assertEquals("Live", LivePhotoMode.Motion.title)
        assertEquals(listOf(OutputQualityPreset.Standard), OutputQualityPreset.entries)
    }

    @Test
    fun `ending duration rounds to nearest half second`() {
        assertEquals(
            2.0,
            WatermarkSettings(endingInfoCardDuration = 1.76).normalizedEndingInfoCardDuration,
            0.0
        )
    }
}
