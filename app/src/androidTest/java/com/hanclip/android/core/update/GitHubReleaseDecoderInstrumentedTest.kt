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
              "tag_name": "android-v2.1.0",
              "draft": false,
              "prerelease": false,
              "body": "Android-Version-Code: 340033\nBuild-Number: 202608250313",
              "assets": [{
                "name": "HanClip-Android-2.1.0.apk",
                "size": 68000000,
                "digest": "sha256:${"ab".repeat(32)}",
                "browser_download_url": "https://github.com/armsone/HanClip-Android/releases/download/android-v2.1.0/HanClip-Android-2.1.0.apk"
              }]
            }
            """.trimIndent()
        )

        assertEquals(340033, release?.versionCode)
        assertEquals("HanClip-Android-2.1.0.apk", release?.assetName)
    }

    @Test
    fun draftAndPrereleaseAreRejected() {
        assertNull(
            GitHubReleaseDecoder.decode(
                """{"tag_name":"android-v2.1.0","draft":true,"prerelease":false,"assets":[]}"""
            )
        )
        assertNull(
            GitHubReleaseDecoder.decode(
                """{"tag_name":"android-v2.1.0","draft":false,"prerelease":true,"assets":[]}"""
            )
        )
    }
}
