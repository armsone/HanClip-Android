package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptionDateInsertionTest {
    @Test
    fun `basic caption is replaced by date`() {
        val result = insertCaptionDateText("기본", 2, 2, "2026. 8. 13.", replaceBasicText = true)

        assertEquals("2026. 8. 13.", result.text)
        assertEquals(result.text.length, result.cursor)
        assertTrue(result.replacedBasicText)
    }

    @Test
    fun `date replaces current selection in edited caption`() {
        val result = insertCaptionDateText("여행 기록", 3, 5, "8. 13.", replaceBasicText = false)

        assertEquals("여행 8. 13.", result.text)
        assertEquals(9, result.cursor)
        assertFalse(result.replacedBasicText)
    }
}
