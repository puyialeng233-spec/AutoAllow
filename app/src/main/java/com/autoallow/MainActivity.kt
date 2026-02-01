package com.autoallow

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.Gravity

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "自动允许"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val desc = TextView(this).apply {
            text = "\n检测到「连接至Windows」弹窗时\n自动点击「允许」按钮\n"
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val accessibilityButton = Button(this).apply {
            text = "1. 打开无障碍设置"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        val batteryButton = Button(this).apply {
            text = "2. 关闭电池优化"
            setOnClickListener {
                requestIgnoreBatteryOptimization()
            }
        }

        val statusText = TextView(this).apply {
            text = getStatusText()
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }

        layout.addView(title)
        layout.addView(desc)
        layout.addView(accessibilityButton)
        layout.addView(batteryButton)
        layout.addView(statusText)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        // 刷新状态
        val layout = window.decorView.findViewById<LinearLayout>(android.R.id.content)?.getChildAt(0) as? LinearLayout
        layout?.let {
            val statusText = it.getChildAt(it.childCount - 1) as? TextView
            statusText?.text = getStatusText()
        }
    }

    private fun getStatusText(): String {
        val batteryOptimized = !isIgnoringBatteryOptimizations()
        return buildString {
            append("\n状态:\n")
            append("• 电池优化: ")
            append(if (batteryOptimized) "❌ 未关闭" else "✓ 已关闭")
            append("\n\n请确保已开启无障碍服务")
        }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun requestIgnoreBatteryOptimization() {
        if (isIgnoringBatteryOptimizations()) {
            Toast.makeText(this, "已关闭电池优化", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            // 部分手机不支持，跳转到电池设置页面
            try {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (e2: Exception) {
                Toast.makeText(this, "请手动在设置中关闭电池优化", Toast.LENGTH_LONG).show()
            }
        }
    }
}
