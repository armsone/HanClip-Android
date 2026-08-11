package com.hanclip.android.feature.editor

internal enum class ClipPreviewPlaybackMode {
    Stop,
    Loop,
    AutoNext
}

internal fun nextClipIndexOnPlaybackEnded(
    mode: ClipPreviewPlaybackMode,
    currentIndex: Int,
    clipCount: Int
): Int? {
    if (mode != ClipPreviewPlaybackMode.AutoNext) return null
    if (currentIndex !in 0 until clipCount) return null
    return (currentIndex + 1).takeIf { it < clipCount }
}
