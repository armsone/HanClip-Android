package com.hanclip.android.core.safety

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Files

class RecoveryPolicyTest {
    @Test
    fun `valid primary wins over backup and corrupt primary falls back`() {
        val directory = Files.createTempDirectory("hanclip-recovery").toFile()
        try {
            val primary = directory.resolve("project.json")
            val backup = directory.resolve("project.json.bak")
            primary.writeText("current")
            backup.writeText("previous")

            assertEquals("current", loadPrimaryOrBackup(primary, backup, ::decodeProject))
            primary.writeText("corrupt")
            assertEquals("previous", loadPrimaryOrBackup(primary, backup, ::decodeProject))
            backup.writeText("corrupt")
            assertNull(loadPrimaryOrBackup(primary, backup, ::decodeProject))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun `capture values keep trigger order when completion order differs`() {
        val completionOrder = listOf(2L to "third", 0L to "first", 1L to "second")
        assertEquals(listOf("first", "second", "third"), orderedCaptureValues(completionOrder))
    }

    @Test
    fun `pinch column steps are bounded to one three five and eight`() {
        assertEquals(3, steppedMediaColumnCount(5, 1.2f))
        assertEquals(8, steppedMediaColumnCount(5, 0.8f))
        assertEquals(1, steppedMediaColumnCount(1, 1.2f))
        assertEquals(8, steppedMediaColumnCount(8, 0.8f))
        assertEquals(5, steppedMediaColumnCount(5, 1f))
    }

    @Test
    fun `duration validation accepts muxer rounding but rejects truncated output`() {
        assertEquals(true, isDurationWithinTolerance(30.0, 29.4))
        assertEquals(true, isDurationWithinTolerance(120.0, 117.0))
        assertEquals(false, isDurationWithinTolerance(30.0, 25.0))
        assertEquals(false, isDurationWithinTolerance(30.0, 0.0))
    }

    private fun decodeProject(raw: String): String {
        require(raw != "corrupt")
        return raw
    }
}
