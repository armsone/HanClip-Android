package com.hanclip.android.core.safety

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class ExportFileTransactionTest {
    @Test
    fun `interrupted partial is cleaned and verified file is promoted`() {
        val directory = Files.createTempDirectory("hanclip-export").toFile()
        try {
            val completed = directory.resolve("hanclip-preview-old.mp4").apply { writeText("old") }
            val interrupted = directory.resolve("hanclip-preview-dead.partial.mp4").apply {
                writeText("partial")
            }

            ExportFileTransaction.cleanupInterrupted(directory)

            assertFalse(interrupted.exists())
            assertTrue(completed.isFile)

            val finalFile = directory.resolve("hanclip-preview-new.mp4")
            val staging = ExportFileTransaction.stagingFile(finalFile).apply { writeText("verified") }
            val promoted = ExportFileTransaction.promote(staging, finalFile)

            assertFalse(staging.exists())
            assertEquals("verified", promoted.readText())
            assertEquals("hanclip-preview-new.mp4", promoted.name)
        } finally {
            directory.deleteRecursively()
        }
    }
}
