package com.seven.file.manager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seven.file.manager.tool.MMKVTool

@Composable
fun FileManagerTheme(content: @Composable () -> Unit) {
    val isPreview = LocalInspectionMode.current
    // 1. 订阅 MMKV 中的主题类型流 (0:跟随系统, 1:强制浅色, -1:强制深色)
    val themeType = if (isPreview) {
        remember { mutableIntStateOf(0) }
    } else {
        MMKVTool.themeTypeProperty.asFlow()
            .collectAsStateWithLifecycle(initialValue = MMKVTool.themeType)
    }
    // 2. 核心逻辑判断：最终是否为暗黑模式
    val isDarkTheme = when (themeType.value) {
        1 -> false     // 强制浅色
        -1 -> true     // 强制深色
        else -> isSystemInDarkTheme() // 0 或其他情况，跟随系统
    }
    // 3. 选取对应的官方色彩方案
    val colorScheme = if (isDarkTheme) MonochromeDarkScheme else MonochromeLightScheme

    // 4. 处理系统状态栏与导航栏的动态变色 (SystemBarColorEffect 融合)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)

            // 如果是浅色模式，状态栏图标变黑 (true)；如果是深色模式，状态栏图标变白 (false)
            controller.isAppearanceLightStatusBars = !isDarkTheme
            controller.isAppearanceLightNavigationBars = !isDarkTheme

            // 顺便让系统栏的背景色与 App 的大背景完美融为一体（实现沉浸式）
            window.isNavigationBarContrastEnforced = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}