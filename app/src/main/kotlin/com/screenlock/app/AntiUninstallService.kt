package com.screenlock.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityService
import androidx.core.app.NotificationCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

class AntiUninstallService : AccessibilityService() {

    private var isTimerActive = false
    private val handler = Handler(Looper.getMainLooper())
    private val checkRunnable = object : Runnable {
        override fun run() {
            updateTimerStatus()
            handler.postDelayed(this, 1000)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "anti_uninstall_channel"
        private const val PREFS_NAME = "LockConfig"
        private const val KEY_IS_TIMER_ACTIVE = "is_timer_active"

        private val KEYWORDS = listOf("卸载", "Uninstall", "确认", "确定", "删除")
        private val BLOCKED_PACKAGES = listOf("packageinstaller", "settings")
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        handler.post(checkRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(checkRunnable)
        super.onDestroy()
    }

    private fun updateTimerStatus() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isTimerActive = prefs.getBoolean(KEY_IS_TIMER_ACTIVE, false)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isTimerActive) return
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChange(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleWindowContentChange(event)
            }
        }
    }

    private fun handleWindowStateChange(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        if (packageName == "com.android.systemui") {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun handleWindowContentChange(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        if (BLOCKED_PACKAGES.any { packageName.contains(it, ignoreCase = true) }) {
            val rootNode = rootInActiveWindow ?: return
            try {
                if (hasUninstallKeywords(rootNode)) {
                    lockScreen()
                }
            } finally {
                rootNode.recycle()
            }
        }
    }

    private fun hasUninstallKeywords(node: AccessibilityNodeInfo): Boolean {
        val nodeCompat = AccessibilityNodeInfoCompat.wrap(node)
        val text = nodeCompat.text?.toString() ?: ""
        val contentDescription = nodeCompat.contentDescription?.toString() ?: ""
        if (KEYWORDS.any { text.contains(it) || contentDescription.contains(it) }) {
            return true
        }
        for (i in 0 until nodeCompat.childCount) {
            val child = nodeCompat.getChild(i)
            if (child != null) {
                try {
                    if (hasUninstallKeywords(child)) {
                        return true
                    }
                } finally {
                    child.recycle()
                }
            }
        }
        return false
    }

    private fun lockScreen() {
        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }

    override fun onInterrupt() {}

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "防卸载服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "防止在定时期间卸载应用"
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("系统守护运行中")
            .setContentText("防卸载保护已激活")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
