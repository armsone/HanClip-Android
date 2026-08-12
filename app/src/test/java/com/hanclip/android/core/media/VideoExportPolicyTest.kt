package com.hanclip.android.core.media

import com.hanclip.android.core.model.ClipMediaKind
import com.hanclip.android.core.model.LivePhotoMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class VideoExportPolicyTest {
    @Test
    fun `photo zoom matches iOS scale and focal bounds`() {
        repeat(100) { seed ->
            val motion = randomPhotoZoomMotion(Random(seed))
            assertTrue(motion.focalX in 0.15f..0.85f)
            assertTrue(motion.focalY in 0.15f..0.85f)
            val start = photoZoomScale(motion, 0L, 2_000_000L)
            val middle = photoZoomScale(motion, 1_000_000L, 2_000_000L)
            val end = photoZoomScale(motion, 2_000_000L, 2_000_000L)
            assertEquals(1.05f, middle, 0.0001f)
            if (motion.zoomsIn) {
                assertEquals(1.0f, start, 0.0001f)
                assertEquals(1.1f, end, 0.0001f)
            } else {
                assertEquals(1.1f, start, 0.0001f)
                assertEquals(1.0f, end, 0.0001f)
            }
        }
    }

    @Test
    fun `only video and motion Live Photo keep source audio`() {
        assertFalse(keepsSourceAudio(ClipMediaKind.Photo, LivePhotoMode.Still))
        assertTrue(keepsSourceAudio(ClipMediaKind.Video, LivePhotoMode.Still))
        assertFalse(keepsSourceAudio(ClipMediaKind.LivePhoto, LivePhotoMode.Still))
        assertTrue(keepsSourceAudio(ClipMediaKind.LivePhoto, LivePhotoMode.Motion))
        assertTrue(usesPhotoZoom(ClipMediaKind.Photo, LivePhotoMode.Still))
        assertFalse(usesPhotoZoom(ClipMediaKind.Video, LivePhotoMode.Still))
        assertTrue(usesPhotoZoom(ClipMediaKind.LivePhoto, LivePhotoMode.Still))
        assertFalse(usesPhotoZoom(ClipMediaKind.LivePhoto, LivePhotoMode.Motion))
    }

    @Test
    fun `ending hides caption but keeps copyright logo`() {
        assertEquals(
            WatermarkLayerPolicy(rendersText = true, rendersLogo = true),
            watermarkLayerPolicy(
                shouldRenderText = true,
                logoEnabled = true,
                isEndingInfoClip = false
            )
        )
        assertEquals(
            WatermarkLayerPolicy(rendersText = false, rendersLogo = true),
            watermarkLayerPolicy(
                shouldRenderText = true,
                logoEnabled = true,
                isEndingInfoClip = true
            )
        )
        assertEquals(
            WatermarkLayerPolicy(rendersText = false, rendersLogo = false),
            watermarkLayerPolicy(
                shouldRenderText = true,
                logoEnabled = false,
                isEndingInfoClip = true
            )
        )
    }
}
