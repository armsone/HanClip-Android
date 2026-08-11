package com.hanclip.android.core.project

import android.content.Context

internal object ExportRecoveryStore {
    private const val PreferencesName = "hanclip_export_recovery"
    private const val ActiveProjectIdKey = "active_project_id"
    private const val ActiveTokenKey = "active_token"

    fun markStarted(context: Context, projectId: String, token: Long) {
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .putString(ActiveProjectIdKey, projectId)
            .putLong(ActiveTokenKey, token)
            .commit()
    }

    fun clear(context: Context, token: Long) {
        val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
        if (!preferences.contains(ActiveTokenKey) || preferences.getLong(ActiveTokenKey, token) != token) {
            return
        }
        preferences.edit().clear().commit()
    }

    fun consumeInterrupted(context: Context, projectId: String): Boolean {
        val preferences = context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
        if (!preferences.contains(ActiveTokenKey)) return false
        if (preferences.getString(ActiveProjectIdKey, null) != projectId) return false
        preferences.edit().clear().commit()
        return true
    }
}
