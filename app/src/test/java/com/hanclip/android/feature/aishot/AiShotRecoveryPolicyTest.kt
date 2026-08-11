package com.hanclip.android.feature.aishot

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class AiShotRecoveryPolicyTest {
    @Test
    fun `new session removes only abandoned AiShot media`() {
        val directory = Files.createTempDirectory("aishot-recovery-test").toFile()
        try {
            directory.resolve("aishot-buffer-1720000000000.mp4").writeText("buffer")
            directory.resolve("aishot-3-1720000000001.mp4").writeText("capture")
            directory.resolve("aishot-not-a-session.mp4").writeText("keep")
            directory.resolve("other-video.mp4").writeText("keep")

            AiShotVideoTrimmer.cleanupAbandonedSessionFiles(directory)

            assertEquals(
                listOf("aishot-not-a-session.mp4", "other-video.mp4"),
                directory.listFiles().orEmpty().map { it.name }.sorted()
            )
        } finally {
            directory.deleteRecursively()
        }
    }
}
