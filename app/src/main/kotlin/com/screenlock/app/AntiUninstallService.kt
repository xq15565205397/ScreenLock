package com.screenlock.app

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat

/**
 * 核心逻辑：监听系统事件，防止应用在定时期间被卸载
 */
class AntiUninstallService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
        // 启动前台服务以提高 vivo 系统的存活率
        startForeground(1, createNotification())
    }

    private fun createNotification(): Notification {
        val channelId = "system_guard"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "系统守护中", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("系统守护运行中")
            .setContentText("正在保护定时锁定状态...")
            .setSmallIcon(android.R.drawable.ic_secure) // 修正：使用更稳定的系统图标
            .setOngoing(true)
            .build()
    }

    // 修正：添加了 AccessibilityEvent? 的问号，确保匹配基类签名
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 1. 读取定时器状态
        val prefs = getSharedPreferences("LockConfig", Context.MODE_PRIVATE)
        val isTimerActive = prefs.getBoolean("is_timer_active", false)
        
        // 只有在定时器激活且事件不为空时才进行拦截
        if (!isTimerActive || event == null) return

        val packageName = event.packageName?.toString() ?: ""

        // 2. 拦截多任务键 (防止划掉卡片)
        if (packageName == "com.android.systemui") {
            performGlobalAction(GLOBAL_ACTION_HOME)
            return
        }

        // 3. 拦截卸载/设置操作
        if (packageName.contains("packageinstaller") || packageName.contains("settings")) {
            val rootNode = rootInActiveWindow ?: return
            // 检查界面中是否存在敏感文字
            if (findKeyword(rootNode, listOf("卸载", "Uninstall", "确认", "确定", "删除"))) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                }
            }
            rootNode.recycle()
        }
    }

    private fun findKeyword(node: AccessibilityNodeInfo, words: List<String>): Boolean {
        for (word in words) {
            val list = node.findAccessibilityNodeInfosByText(word)
            if (list != null && list.isNotEmpty()) {
                return true
            }
        }
        return false
    }

    override fun onInterrupt() {
        // 服务中断回调，必须保留
    }
}