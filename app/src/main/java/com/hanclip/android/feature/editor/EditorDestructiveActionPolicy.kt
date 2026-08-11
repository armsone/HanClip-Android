package com.hanclip.android.feature.editor

internal enum class ClipRemovalKind {
    Standard,
    VideoWithSegments,
    SegmentChild,
    SimilarPhotoRepresentative
}

internal fun clipRemovalConfirmationMessage(kind: ClipRemovalKind): String = when (kind) {
    ClipRemovalKind.Standard ->
        "이 클립을 완성본에서 제외할까요? 원본 미디어는 삭제되지 않으며 바로 되돌릴 수 있습니다."
    ClipRemovalKind.VideoWithSegments ->
        "이 원본 영상과 자동 컷을 완성본에서 제외할까요? 원본 미디어는 삭제되지 않으며 바로 되돌릴 수 있습니다."
    ClipRemovalKind.SegmentChild ->
        "이 자동 컷만 완성본에서 제외할까요? 원본 영상과 다른 자동 컷은 유지되며 바로 되돌릴 수 있습니다."
    ClipRemovalKind.SimilarPhotoRepresentative ->
        "이 대표 사진을 완성본에서 제외할까요? 같은 묶음은 남은 사진으로 다시 정리되며 바로 되돌릴 수 있습니다."
}
