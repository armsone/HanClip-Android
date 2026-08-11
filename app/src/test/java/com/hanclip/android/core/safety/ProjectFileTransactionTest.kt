package com.hanclip.android.core.safety

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class ProjectFileTransactionTest {
    @Test
    fun `cold start cleanup preserves committed project and media files`() {
        val filesDirectory = Files.createTempDirectory("hanclip-projects").toFile()
        try {
            val projectDirectory = filesDirectory
                .resolve("editable-projects/project-1")
                .apply { mkdirs() }
            val mediaDirectory = projectDirectory.resolve("media").apply { mkdirs() }
            val primary = projectDirectory.resolve("project.json").apply { writeText("primary") }
            val backup = projectDirectory.resolve("project.json.bak").apply { writeText("backup") }
            val source = mediaDirectory.resolve("source-clip.jpg").apply { writeText("source") }
            val metadataStaging = projectDirectory.resolve("project.json.tmp").apply { writeText("partial") }
            val mediaStaging = mediaDirectory.resolve("source-next.jpg.tmp-dead").apply {
                writeText("partial")
            }

            ProjectFileTransaction.cleanupInterrupted(filesDirectory)

            assertTrue(primary.isFile)
            assertTrue(backup.isFile)
            assertTrue(source.isFile)
            assertFalse(metadataStaging.exists())
            assertFalse(mediaStaging.exists())
        } finally {
            filesDirectory.deleteRecursively()
        }
    }
}
