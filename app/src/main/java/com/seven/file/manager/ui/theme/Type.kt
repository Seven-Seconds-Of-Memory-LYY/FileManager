package com.seven.file.manager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.seven.basis.dimens.asp

/**
 * Typography 默认字体样式
 */
val typography: Typography
    @Composable
    get() = Typography(
        // ==================== 标题系列 ====================

        // 【标题】：通常用于页面大标题、TopAppBar 标题
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.asp,
            lineHeight = 28.asp,
            color = MaterialTheme.colorScheme.onBackground // 映射大背景上的主文字颜色
        ),

        // 【次级标题】：用于分类的小标题、分组头部
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 16.asp,
            lineHeight = 22.asp,
            color = MaterialTheme.colorScheme.onSurface // 映射容器层上的主文字颜色
        ),

        // 【卡片标题】：用于列表项（Item）的主标题、文件列表的文件名
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 15.asp,
            lineHeight = 20.asp,
            color = MaterialTheme.colorScheme.onSurface // 映射卡片上的主文字颜色
        ),

        // ==================== 内容/正文系列 ====================

        // 【内容】：用于大段正文、详情描述
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.asp,
            lineHeight = 24.asp,
            letterSpacing = 0.5.asp,
            color = MaterialTheme.colorScheme.onSurface // 默认正文主色
        ),

        // 【次级内容/提示】：通常用于文件的修改时间、大小等副文本
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 13.asp,
            lineHeight = 18.asp,
            color = MaterialTheme.colorScheme.onSurfaceVariant // 映射次级文字（浅色深灰，深色纯白）
        ),

        // ==================== 标签/辅助系列 ====================

        // 辅助微型文本
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 11.asp,
            lineHeight = 16.asp,
            letterSpacing = 0.5.asp,
            color = MaterialTheme.colorScheme.outline // 映射提示线或更弱的文本颜色
        )
    )
