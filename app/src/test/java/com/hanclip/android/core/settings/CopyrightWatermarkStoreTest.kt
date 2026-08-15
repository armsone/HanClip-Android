package com.hanclip.android.core.settings

import com.hanclip.android.core.model.CopyrightIconColorMode
import com.hanclip.android.core.model.WatermarkFontSize
import com.hanclip.android.core.model.WatermarkPlatform
import com.hanclip.android.core.model.WatermarkPosition
import com.hanclip.android.core.model.WatermarkSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CopyrightWatermarkStoreTest {
    @Test
    fun `copyright settings merge without replacing project caption settings`() {
        val caption = WatermarkSettings(
            isEnabled = true,
            text = "프로젝트 자막",
            position = WatermarkPosition.TopLeading,
            fontName = "do_hyeon",
            fontSize = WatermarkFontSize.Small,
            logoEnabled = false
        )
        val copyright = WatermarkSettings(
            isEnabled = false,
            text = "전역 값이 자막을 덮으면 안 됨",
            logoEnabled = true,
            address = "@hanclip",
            platform = WatermarkPlatform.Instagram,
            copyrightPosition = WatermarkPosition.BottomCenter,
            copyrightIconColorMode = CopyrightIconColorMode.Tint,
            copyrightIconColorHex = "#123456"
        )

        val merged = caption.withCopyrightWatermark(copyright)

        assertTrue(merged.isEnabled)
        assertEquals("프로젝트 자막", merged.text)
        assertEquals(WatermarkPosition.TopLeading, merged.position)
        assertEquals("do_hyeon", merged.fontName)
        assertEquals(WatermarkFontSize.Small, merged.fontSize)
        assertTrue(merged.logoEnabled)
        assertEquals("@hanclip", merged.address)
        assertEquals(WatermarkPlatform.Instagram, merged.platform)
        assertEquals(WatermarkPosition.BottomCenter, merged.copyrightPosition)
        assertEquals(CopyrightIconColorMode.Tint, merged.copyrightIconColorMode)
        assertEquals("#123456", merged.copyrightIconColorHex)
        assertFalse(merged.includesEndingInfoCard)
    }

    @Test
    fun `copyright reset preserves caption and selected platform`() {
        val current = WatermarkSettings(
            isEnabled = true,
            text = "보존할 자막",
            fontName = "do_hyeon",
            platform = WatermarkPlatform.YouTube,
            logoEnabled = false,
            address = "사용자 주소",
            copyrightPosition = WatermarkPosition.TopLeading,
            logoShadowOpacity = 0.9
        )

        val reset = current.resetCopyrightWatermark(
            storedAddress = "@saved",
            defaultTextColorHex = "#FF0000",
            defaultShadowColorHex = "#00FFFF"
        )

        assertTrue(reset.isEnabled)
        assertEquals("보존할 자막", reset.text)
        assertEquals("do_hyeon", reset.fontName)
        assertEquals(WatermarkPlatform.YouTube, reset.platform)
        assertTrue(reset.logoEnabled)
        assertEquals("@saved", reset.address)
        assertEquals(WatermarkPosition.BottomTrailing, reset.copyrightPosition)
        assertEquals("#FF0000", reset.logoColorHex)
        assertEquals("#00FFFF", reset.logoShadowColorHex)
        assertEquals(0.5, reset.logoShadowOpacity, 0.0)
        assertEquals(CopyrightIconColorMode.Original, reset.copyrightIconColorMode)
    }
}
