package com.hanclip.android.core.safety

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class ImportFileTransactionTest {
    @Test
    fun `cold start cleanup removes staging and preserves imported source`() {
        val directory = Files.createTempDirectory("hanclip-import").toFile()
        try {
            val importedSource = directory.resolve("hanclip-source.jpg").apply { writeText("source") }
            val interruptedCopy = directory.resolve("hanclip-next.jpg.tmp").apply { writeText("partial") }
            val interruptedMotion = directory.resolve("motion-source.mp4.tmp").apply { writeText("partial") }

            ImportFileTransaction.cleanupInterrupted(directory)

            assertTrue(importedSource.isFile)
            assertFalse(interruptedCopy.exists())
            assertFalse(interruptedMotion.exists())
        } finally {
            directory.deleteRecursively()
        }
    }
}
