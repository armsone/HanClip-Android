package com.hanclip.android.core.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubUpdatePolicyTest {
    @Test
    fun `release identity separates product version and internal code`() {
        assertEquals("2.1.0", GitHubUpdatePolicy.productVersion("android-v2.1.0"))
        assertNull(GitHubUpdatePolicy.productVersion("android-v340033"))
        assertEquals(340033, GitHubUpdatePolicy.versionCode("Android-Version-Code: 340033\nBuild-Number: 202608250313"))
        assertNull(GitHubUpdatePolicy.versionCode("versionCode: 340033"))
    }

    @Test
    fun `APK asset must come from the matching HanClip release`() {
        assertTrue(
            GitHubUpdatePolicy.isApprovedApkAsset(
                "HanClip-Android-2.1.0.apk",
                "https://github.com/armsone/HanClip-Android/releases/download/android-v2.1.0/HanClip-Android-2.1.0.apk",
                "2.1.0",
                68_000_000L
            )
        )
        assertFalse(
            GitHubUpdatePolicy.isApprovedApkAsset(
                "HanClip-Android-2.1.0.apk",
                "https://example.com/HanClip-Android-2.1.0.apk",
                "2.1.0",
                68_000_000L
            )
        )
        assertFalse(
            GitHubUpdatePolicy.isApprovedApkAsset(
                "other.apk",
                "https://github.com/armsone/HanClip-Android/releases/download/android-v2.1.0/other.apk",
                "2.1.0",
                68_000_000L
            )
        )
    }
}
