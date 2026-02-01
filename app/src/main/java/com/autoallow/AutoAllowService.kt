package com.autoallow

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class AutoAllowService : AccessibilityService() {

    companion object {
        private const val TAG = "AutoAllow"
        private const val CHANNEL_ID = "auto_allow_channel"
        private const val NOTIFICATION_ID = 1
        // 目标包名列表
        private val TARGET_PACKAGES = setOf(
            "com.hihonor.linktowindowsservice",
            "com.microsoft.appmanager"
        )
        // 弹窗关键词
        private val DIALOG_KEYWORDS = listOf(
            "连接至 Windows",
            "连接至Windows", 
            "录制或投放内容"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or 
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            // 不限制包名，监听所有应用
            packageNames = null
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        serviceInfo = info
        
        // 启动前台服务
        startForegroundNotification()
        
        Log.d(TAG, "AutoAllow 无障碍服务已启动")
    }

    private fun startForegroundNotification() {
        createNotificationChannel()
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }.apply {
            setContentTitle("自动允许")
            setContentText("正在监控「连接至Windows」弹窗")
            setSmallIcon(android.R.drawable.ic_menu_preferences)
            setContentIntent(pendingIntent)
            setOngoing(true)
        }.build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "自动允许服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持服务在后台运行"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        val rootNode = rootInActiveWindow ?: return
        
        try {
            // 检查是否包含目标弹窗的关键词
            if (isTargetDialog(rootNode)) {
                findAndClickAllow(rootNode)
            }
        } finally {
            rootNode.recycle()
        }
    }

    private fun isTargetDialog(node: AccessibilityNodeInfo): Boolean {
        for (keyword in DIALOG_KEYWORDS) {
            val nodes = node.findAccessibilityNodeInfosByText(keyword)
            if (nodes.isNotEmpty()) {
                nodes.forEach { it.recycle() }
                return true
            }
        }
        return false
    }

    private fun findAndClickAllow(node: AccessibilityNodeInfo): Boolean {
        // 查找"允许"按钮
        val allowButtons = node.findAccessibilityNodeInfosByText("允许")
        for (button in allowButtons) {
            // 确保是"允许"而不是"不允许"
            val text = button.text?.toString() ?: ""
            if (button.isClickable && text == "允许") {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "已自动点击「允许」按钮")
                button.recycle()
                return true
            }
            button.recycle()
        }

        // 也尝试通过 resource-id 查找
        for (pkg in TARGET_PACKAGES) {
            val buttonById = node.findAccessibilityNodeInfosByViewId("$pkg:id/positive_button")
            for (button in buttonById) {
                if (button.isClickable) {
                    button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d(TAG, "已自动点击「允许」按钮 (by id)")
                    button.recycle()
                    return true
                }
                button.recycle()
            }
        }

        return false
    }

    override fun onInterrupt() {
        Log.d(TAG, "AutoAllow 无障碍服务中断")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
