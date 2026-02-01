package com.autoallow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
            text = "\n检测到「连接至Windows」弹窗时\n自动点击「允许」按钮\n\n请开启无障碍服务\n"
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val button = Button(this).apply {
            text = "打开无障碍设置"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(title)
        layout.addView(desc)
        layout.addView(button)
        setContentView(layout)
    }
}
