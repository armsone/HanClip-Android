package com.hanclip.android.core.project

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class ImportedFontPolicyTest {
    @Test
    fun `font limit rejects next import without pruning existing entries`() {
        assertTrue(ImportedFontStore.canImportMoreFonts(29))
        assertFalse(ImportedFontStore.canImportMoreFonts(30))
        assertFalse(ImportedFontStore.canImportMoreFonts(31))
    }

    @Test
    fun `font structure rejects arbitrary files and truncated table directories`() {
        val arbitrary = Files.createTempFile("hanclip-font", ".ttf").toFile()
        val truncated = Files.createTempFile("hanclip-font-truncated", ".ttf").toFile()
        try {
            arbitrary.writeText("not-a-font")
            truncated.writeBytes(
                byteArrayOf(
                    0x00, 0x01, 0x00, 0x00,
                    0x00, 0x02, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00
                )
            )

            assertFalse(ImportedFontStore.hasSupportedFontStructure(arbitrary))
            assertFalse(ImportedFontStore.hasSupportedFontStructure(truncated))
        } finally {
            arbitrary.delete()
            truncated.delete()
        }
    }
}
