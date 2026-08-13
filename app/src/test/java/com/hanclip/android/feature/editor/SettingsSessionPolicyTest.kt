package com.hanclip.android.feature.editor

import com.hanclip.android.core.model.EndingInfoCardTheme
import com.hanclip.android.core.model.WatermarkFontSize
import com.hanclip.android.core.model.WatermarkSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsSessionPolicyTest {
    @Test
    fun `caption opens enabled without mutating the stored snapshot`() {
        val stored = WatermarkSettings(isEnabled = false, text = "보관된 자막")

        val draft = captionSettingsDraftOnOpen(stored)

        assertFalse(stored.isEnabled)
        assertTrue(draft.isEnabled)
        assertEquals("보관된 자막", draft.text)
    }

    @Test
    fun `enabled caption keeps the original draft instance`() {
        val stored = WatermarkSettings(isEnabled = true)

        assertSame(stored, captionSettingsDraftOnOpen(stored))
    }

    @Test
    fun `ending reset restores every ending appearance field and preserves caption content`() {
        val stored = WatermarkSettings(
            isEnabled = true,
            text = "사용자 자막",
            logoEnabled = true,
            includesEndingInfoCard = true,
            endingInfoCardDuration = 8.5,
            endingInfoCardTheme = EndingInfoCardTheme.Office,
            endingInfoCardVariation = 9,
            fontName = "custom-font",
            textColorHex = "#123456",
            shadowEnabled = false,
            shadowOpacity = 0.1,
            shadowColorHex = "#654321",
            fontSize = WatermarkFontSize.Small
        )

        val reset = resetEndingSettingsDraft(stored)
        val defaults = WatermarkSettings()

        assertFalse(reset.includesEndingInfoCard)
        assertEquals(2.0, reset.endingInfoCardDuration, 0.0)
        assertEquals(EndingInfoCardTheme.Caption, reset.endingInfoCardTheme)
        assertEquals(0, reset.endingInfoCardVariation)
        assertEquals(defaults.fontName, reset.fontName)
        assertEquals(defaults.textColorHex, reset.textColorHex)
        assertEquals(defaults.shadowEnabled, reset.shadowEnabled)
        assertEquals(defaults.shadowOpacity, reset.shadowOpacity, 0.0)
        assertEquals(defaults.shadowColorHex, reset.shadowColorHex)
        assertEquals(defaults.fontSize, reset.fontSize)
        assertTrue(reset.isEnabled)
        assertEquals("사용자 자막", reset.text)
        assertTrue(reset.logoEnabled)
    }
}
