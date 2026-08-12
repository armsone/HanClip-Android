package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentMediaFilterPolicyTest {
    @Test
    fun `individual media filters can be combined and removed`() {
        val photoAndVideo = RecentMediaFilter.Photo.toggled(RecentMediaFilter.Video)

        assertEquals(RecentMediaFilter.PhotoAndVideo, photoAndVideo)
        assertTrue(photoAndVideo.includes(RecentMediaFilter.Photo))
        assertTrue(photoAndVideo.includes(RecentMediaFilter.Video))
        assertEquals(RecentMediaFilter.Video, photoAndVideo.toggled(RecentMediaFilter.Photo))
    }

    @Test
    fun `last selected media kind cannot be removed`() {
        assertEquals(
            RecentMediaFilter.LivePhoto,
            RecentMediaFilter.LivePhoto.toggled(RecentMediaFilter.LivePhoto)
        )
    }

    @Test
    fun `all restores every media kind`() {
        assertEquals(RecentMediaFilter.All, RecentMediaFilter.Video.toggled(RecentMediaFilter.All))
        assertTrue(RecentMediaFilter.All.includes(RecentMediaFilter.Photo))
        assertTrue(RecentMediaFilter.All.includes(RecentMediaFilter.LivePhoto))
        assertTrue(RecentMediaFilter.All.includes(RecentMediaFilter.Video))
    }
}
