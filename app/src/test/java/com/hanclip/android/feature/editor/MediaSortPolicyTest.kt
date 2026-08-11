package com.hanclip.android.feature.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaSortPolicyTest {
    @Test
    fun `added order follows modification date with stable fallbacks`() {
        assertEquals(3_000L, resolveAddedMillis(3L, 2L, 1_000L))
        assertEquals(2_000L, resolveAddedMillis(0L, 2L, 1_000L))
        assertEquals(1_000L, resolveAddedMillis(0L, 0L, 1_000L))
    }
}
