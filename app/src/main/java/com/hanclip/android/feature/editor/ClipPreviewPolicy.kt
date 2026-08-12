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
    return if (currentIndex + 1 < clipCount) currentIndex + 1 else 0
}
