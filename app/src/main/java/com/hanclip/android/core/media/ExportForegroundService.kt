package com.hanclip.android.core.media

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.hanclip.android.MainActivity

class ExportForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        notificationManager().createNotificationChannel(
            NotificationChannel(
                ChannelId,
                "영화 만들기",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "HanClip 완성본 제작 진행 상태"
                setShowBadge(false)
            }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ActionCancel -> {
                ExportForegroundTaskBridge.cancel(intent.getLongExtra(ExtraToken, InvalidToken))
                return START_NOT_STICKY
            }

        }

        val token = intent?.getLongExtra(ExtraToken, InvalidToken) ?: InvalidToken
        val progress = normalizedExportNotificationProgress(intent?.getIntExtra(ExtraProgress, 0) ?: 0)
        val message = intent?.getStringExtra(ExtraMessage).orEmpty()
            .ifBlank { "완성본을 만드는 중입니다." }
        ServiceCompat.startForeground(
            this,
            NotificationId,
            buildNotification(token, progress, message),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
        if (ExportForegroundTaskBridge.markForegroundStarted(token)) {
            stopForegroundTask()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTimeout(startId: Int, fgsType: Int) {
        ExportForegroundTaskBridge.cancelActive()
        stopForegroundTask()
    }

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
        ExportForegroundTaskBridge.clear()
    }

    private fun buildNotification(token: Long, progress: Int, message: String) =
        NotificationCompat.Builder(this, ChannelId)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("HanClip · 개봉 준비 중")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(100, progress, progress <= 0)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "제작 취소",
                PendingIntent.getService(
                    this,
                    token.hashCode(),
                    Intent(this, ExportForegroundService::class.java).apply {
                        action = ActionCancel
                        putExtra(ExtraToken, token)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    private fun stopForegroundTask() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun notificationManager(): NotificationManager =
        getSystemService(NotificationManager::class.java)

    companion object {
        private const val ChannelId = "hanclip_export"
        private const val NotificationId = 4102
        private const val InvalidToken = Long.MIN_VALUE
        private const val ActionStart = "com.hanclip.android.action.EXPORT_START"
        private const val ActionUpdate = "com.hanclip.android.action.EXPORT_UPDATE"
        private const val ActionCancel = "com.hanclip.android.action.EXPORT_CANCEL"
        private const val ExtraToken = "export_token"
        private const val ExtraProgress = "export_progress"
        private const val ExtraMessage = "export_message"

        fun start(context: Context, token: Long, message: String, onCancel: () -> Unit) {
            ExportForegroundTaskBridge.register(token, onCancel)
            ContextCompat.startForegroundService(
                context,
                serviceIntent(context, ActionStart, token, 0, message)
            )
        }

        fun update(context: Context, token: Long, progress: Int, message: String) {
            context.startService(serviceIntent(context, ActionUpdate, token, progress, message))
        }

        fun stop(context: Context, token: Long) {
            if (ExportForegroundTaskBridge.requestStop(token)) {
                context.stopService(Intent(context, ExportForegroundService::class.java))
            }
        }

        private fun serviceIntent(
            context: Context,
            actionValue: String,
            token: Long,
            progress: Int,
            message: String
        ) = Intent(context, ExportForegroundService::class.java).apply {
            action = actionValue
            putExtra(ExtraToken, token)
            putExtra(ExtraProgress, normalizedExportNotificationProgress(progress))
            putExtra(ExtraMessage, message)
        }
    }
}

internal object ExportForegroundTaskBridge {
    private var activeToken: Long? = null
    private var cancelAction: (() -> Unit)? = null
    private var foregroundStarted: Boolean = false
    private var stopRequested: Boolean = false

    @Synchronized
    fun register(token: Long, onCancel: () -> Unit) {
        activeToken = token
        cancelAction = onCancel
        foregroundStarted = false
        stopRequested = false
    }

    @Synchronized
    fun cancel(token: Long): Boolean {
        if (token != activeToken) return false
        cancelAction?.invoke() ?: return false
        return true
    }

    @Synchronized
    fun cancelActive(): Boolean {
        cancelAction?.invoke() ?: return false
        return true
    }

    @Synchronized
    fun requestStop(token: Long): Boolean {
        if (token != activeToken) return false
        stopRequested = true
        cancelAction = null
        return foregroundStarted
    }

    @Synchronized
    fun markForegroundStarted(token: Long): Boolean {
        if (token != activeToken) return false
        foregroundStarted = true
        return stopRequested
    }

    @Synchronized
    fun isForegroundStarted(token: Long): Boolean =
        token == activeToken && foregroundStarted

    @Synchronized
    fun clear(token: Long? = null) {
        if (token != null && token != activeToken) return
        activeToken = null
        cancelAction = null
        foregroundStarted = false
        stopRequested = false
    }
}

internal fun normalizedExportNotificationProgress(progress: Int): Int = progress.coerceIn(0, 100)
