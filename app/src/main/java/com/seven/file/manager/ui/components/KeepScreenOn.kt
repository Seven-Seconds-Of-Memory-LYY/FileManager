package com.seven.file.manager.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * Keep screen on 保持屏幕常亮
 */
@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(view) {
        // 设置屏幕常亮
        view.keepScreenOn = true

        onDispose {
            // 当组件从 UI 树移除时，恢复默认状态
            view.keepScreenOn = false
        }
    }
}