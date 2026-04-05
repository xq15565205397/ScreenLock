package com.screenlock.app

import android.content.Context
import android.content.SharedPreferences
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityService

class AntiUninstallService : AccessibilityService() {

    private var isTimerActive = false

    companion object {
        private const val PREFS_NAME = "LockConfig"
        private const val KEY_IS_TIMER_ACTIVE = "is_timer_active"

        private val KEYWORDS = listOf("卸载", "Uninstall", "确认", "确定", "删除")
        private val BLOCKED_PACKAGES = listOf("packageinstaller", "settings")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        updateTimerStatus()
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

    private fun updateTimerStatus() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isTimerActive = prefs.getBoolean(KEY_IS_TIMER_ACTIVE, false)
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
        val text = node.text?.toString() ?: ""
        val contentDescription = node.contentDescription?.toString() ?: ""
        if (KEYWORDS.any { text.contains(it) || contentDescription.contains(it) }) {
            return true
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
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
}
