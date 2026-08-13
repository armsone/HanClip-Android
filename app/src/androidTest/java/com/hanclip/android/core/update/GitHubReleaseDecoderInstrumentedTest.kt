package com.hanclip.android.core.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubReleaseDecoderInstrumentedTest {
    @Test
    fun stableReleaseAcceptsOnlyTheExactApprovedApk() {
        val release = GitHubReleaseDecoder.decode(
            """
            {
              "tag_name": "android-v544",
              "draft": false,
              "prerelease": false,
              "assets": [{
                "name": "HanClip-Android-v544.apk",
                "size": 68000000,
                "browser_download_url": "https://github.com/armsone/HanClip-Android/releases/download/android-v544/HanClip-Android-v544.apk"
              }]
            }
            """.trimIndent()
        )

        assertEquals(544, release?.versionCode)
        assertEquals("HanClip-Android-v544.apk", release?.assetName)
    }

    @Test
    fun draftAndPrereleaseAreRejected() {
        assertNull(
            GitHubReleaseDecoder.decode(
                """{"tag_name":"android-v544","draft":true,"prerelease":false,"assets":[]}"""
            )
        )
        assertNull(
            GitHubReleaseDecoder.decode(
                """{"tag_name":"android-v544","draft":false,"prerelease":true,"assets":[]}"""
            )
        )
    }
}
