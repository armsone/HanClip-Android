package com.hanclip.android.feature.editor

import org.junit.Assert.assertTrue
import org.junit.Test

class EditorDestructiveActionPolicyTest {
    @Test
    fun `removal messages describe the actual data boundary`() {
        assertTrue(clipRemovalConfirmationMessage(ClipRemovalKind.Standard).contains("원본 미디어는 삭제되지"))
        assertTrue(clipRemovalConfirmationMessage(ClipRemovalKind.VideoWithSegments).contains("자동 컷"))
        assertTrue(clipRemovalConfirmationMessage(ClipRemovalKind.SegmentChild).contains("이 자동 컷만"))
        assertTrue(clipRemovalConfirmationMessage(ClipRemovalKind.SimilarPhotoRepresentative).contains("남은 사진"))
    }
}
