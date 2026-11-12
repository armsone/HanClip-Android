package com.hanclip.android.feature.editor

internal const val DefaultClipSettingsExpanded: Boolean = false
internal val DefaultClipPreviewPlaybackMode: ClipPreviewPlaybackMode = ClipPreviewPlaybackMode.Stop
internal const val ProjectSaveFailureMessage: String =
    "프로젝트를 저장하지 못했습니다. 저장 공간을 확인한 뒤 다시 시도해 주세요."

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
