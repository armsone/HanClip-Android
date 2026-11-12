package com.hanclip.android.feature.editor

internal const val DefaultClipSettingsExpanded: Boolean = false
internal val DefaultClipPreviewPlaybackMode: ClipPreviewPlaybackMode = ClipPreviewPlaybackMode.Stop

internal fun saveBeforePreviewExport(
    save: () -> Unit,
    startExport: () -> Unit,
    onSaveFailure: (Throwable) -> Unit
): Boolean = runCatching(save).fold(
    onSuccess = {
        startExport()
        true
    },
    onFailure = {
        onSaveFailure(it)
        false
    }
)
