package com.hanclip.android.core.safety

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class UserAssetFileTransactionTest {
    @Test
    fun `cold start cleanup removes only known user asset staging files`() {
        val filesDirectory = Files.createTempDirectory("hanclip-assets").toFile()
        try {
            val fontDirectory = filesDirectory.resolve("imported-fonts").apply { mkdirs() }
            val musicDirectory = filesDirectory.resolve("background-music").apply { mkdirs() }
            val iconDirectory = filesDirectory.resolve("copyright-icons").apply { mkdirs() }
            val savedFont = fontDirectory.resolve("saved.ttf").apply { writeText("font") }
            val savedMusic = musicDirectory.resolve("hanclip-music-1.mp3").apply { writeText("music") }
            val savedIcon = iconDirectory.resolve("custom-icon").apply { writeText("icon") }
            val fontStaging = fontDirectory.resolve(".font-staging-dead.ttf").apply { writeText("partial") }
            val musicStaging = musicDirectory.resolve(".music-staging-dead.mp3").apply { writeText("partial") }
            val iconStaging = iconDirectory.resolve(".icon-staging-dead").apply { writeText("partial") }

            UserAssetFileTransaction.cleanupInterrupted(filesDirectory)

            assertTrue(savedFont.isFile)
            assertTrue(savedMusic.isFile)
            assertTrue(savedIcon.isFile)
            assertFalse(fontStaging.exists())
            assertFalse(musicStaging.exists())
            assertFalse(iconStaging.exists())
        } finally {
            filesDirectory.deleteRecursively()
        }
    }
}
