package com.hanclip.android.core.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubUpdatePolicyTest {
    @Test
    fun `release tag uses the exact Android version code format`() {
        assertEquals(544, GitHubUpdatePolicy.versionCode("android-v544"))
        assertNull(GitHubUpdatePolicy.versionCode("v544"))
        assertNull(GitHubUpdatePolicy.versionCode("android-v0"))
        assertNull(GitHubUpdatePolicy.versionCode("android-v544-beta"))
    }

    @Test
    fun `APK asset must come from the matching HanClip release`() {
        assertTrue(
            GitHubUpdatePolicy.isApprovedApkAsset(
                "HanClip-Android-v544.apk",
                "https://github.com/armsone/HanClip-Android/releases/download/android-v544/HanClip-Android-v544.apk",
                544,
                68_000_000L
            )
        )
        assertFalse(
            GitHubUpdatePolicy.isApprovedApkAsset(
                "HanClip-Android-v544.apk",
                "https://example.com/HanClip-Android-v544.apk",
                544,
                68_000_000L
            )
        )
        assertFalse(
            GitHubUpdatePolicy.isApprovedApkAsset(
                "other.apk",
                "https://github.com/armsone/HanClip-Android/releases/download/android-v544/other.apk",
                544,
                68_000_000L
            )
        )
    }
}
