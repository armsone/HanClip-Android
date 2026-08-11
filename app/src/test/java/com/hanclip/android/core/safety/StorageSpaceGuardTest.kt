package com.hanclip.android.core.safety

import org.junit.Assert.assertEquals
import org.junit.Test

class StorageSpaceGuardTest {
    @Test
    fun `import keeps source bytes plus fixed safety reserve`() {
        val mib = 1024L * 1024L

        assertEquals(64L * mib, StorageSpaceGuard.requiredImportBytes(null))
        assertEquals(164L * mib, StorageSpaceGuard.requiredImportBytes(100L * mib))
    }

    @Test
    fun `export estimate grows with duration resolution and frame rate`() {
        val shortStandard = StorageSpaceGuard.requiredExportBytes(1920, 1080, 30, 10.0)
        val longStandard = StorageSpaceGuard.requiredExportBytes(1920, 1080, 30, 20.0)
        val highFrameRate = StorageSpaceGuard.requiredExportBytes(1920, 1080, 60, 10.0)
        val higherResolution = StorageSpaceGuard.requiredExportBytes(3840, 2160, 30, 10.0)

        assertEquals(true, longStandard > shortStandard)
        assertEquals(true, highFrameRate > shortStandard)
        assertEquals(true, higherResolution > shortStandard)
    }
}
