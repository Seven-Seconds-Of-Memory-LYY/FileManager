package com.seven.file.manager.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Color_Blue = Color(0xFF4285F4)

// --- 基础灰阶原子值 ---
val MonoWhite = Color(0xFFFFFFFF)       // 纯白
val MonoGray50 = Color(0xFFF5F5F5)      // 极浅灰（大背景）
val MonoGray100 = Color(0xFFE5E5E5)     // 浅灰（分割线/容器）
val MonoGray400 = Color(0xFF999999)     // 中灰（提示文字）
val MonoGray600 = Color(0xFF666666)     // 深灰（次级文字）
val MonoGray800 = Color(0xFF333333)     // 极深灰（主文字/浅色模式主色）
val MonoGray900 = Color(0xFF1A1A1A)     // 接近黑（深色模式卡片）
val MonoBlack = Color(0xFF000000)       // 纯黑（深色模式大背景）

// --- 💡 浅色模式（纯白背景 + 深灰/黑色文字） ---
val MonochromeLightScheme = lightColorScheme(
    primary = MonoBlack,                 // 主按钮/核心高亮：纯黑
    onPrimary = MonoWhite,               // 黑色按钮上的文字：纯白
    primaryContainer = MonoGray100,      // 重点区域容器：浅灰
    onPrimaryContainer = MonoGray800,    // 重点容器上的文字：极深灰

    background = MonoGray50,             // 页面大背景：极浅灰（产生悬浮层级）
    onBackground = MonoGray800,          // 大背景上的主文字：#333

    surface = MonoWhite,                 // 卡片/列表项背景：纯白
    onSurface = MonoGray800,             // 卡片上的主文字：#333
    onSurfaceVariant = MonoGray600,      // 次级文字/图标：#666

    outline = MonoGray400,               // 分割线 / 提示文字：#999
    outlineVariant = MonoGray100         // 更弱的分割线
)

// --- 💡 深色模式（纯黑背景 + 纯白/浅灰文字） ---
val MonochromeDarkScheme = darkColorScheme(
    primary = MonoWhite,                 // 主按钮/核心高亮：纯白
    onPrimary = MonoBlack,               // 白色按钮上的文字：纯黑
    primaryContainer = MonoGray800,      // 重点区域容器：极深灰
    onPrimaryContainer = MonoWhite,      // 重点容器上的文字：纯白

    background = MonoBlack,              // 页面大背景：纯黑（OLED 纯黑体验）
    onBackground = MonoWhite,            // 大背景上的主文字：纯白

    surface = MonoGray900,               // 卡片/列表项背景：接近黑（与背景产生层级）
    onSurface = MonoWhite,               // 卡片上的主文字：纯白
    onSurfaceVariant = MonoWhite,        // 次级文字：统一为白色（满足你的硬性要求）

    outline = MonoGray600,               // 分割线 / 提示文字：深灰
    outlineVariant = MonoGray800         // 更弱的分割线
)