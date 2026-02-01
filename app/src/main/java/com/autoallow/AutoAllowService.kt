package com.autoallow

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class AutoAllowService : AccessibilityService() {

    companion object {
        private const val TAG = "AutoAllow"
        private const val TARGET_PACKAGE = "com.hihonor.linktowindowsservice"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or 
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            packageNames = arrayOf(TARGET_PACKAGE)
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }
        serviceInfo = info
        Log.d(TAG, "AutoAllow 无障碍服务已启动")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        val packageName = event.packageName?.toString() ?: return
        if (packageName != TARGET_PACKAGE) return

        val rootNode = rootInActiveWindow ?: return
        findAndClickAllow(rootNode)
        rootNode.recycle()
    }

    private fun findAndClickAllow(node: AccessibilityNodeInfo): Boolean {
        // 查找"允许"按钮
        val allowButtons = node.findAccessibilityNodeInfosByText("允许")
        for (button in allowButtons) {
            if (button.isClickable) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "已自动点击「允许」按钮")
                button.recycle()
                return true
            }
            button.recycle()
        }

        // 也尝试通过 resource-id 查找
        val buttonById = node.findAccessibilityNodeInfosByViewId("$TARGET_PACKAGE:id/positive_button")
        for (button in buttonById) {
            if (button.isClickable) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "已自动点击「允许」按钮 (by id)")
                button.recycle()
                return true
            }
            button.recycle()
        }

        return false
    }

    override fun onInterrupt() {
        Log.d(TAG, "AutoAllow 无障碍服务中断")
    }
}
