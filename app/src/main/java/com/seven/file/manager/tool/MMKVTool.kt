package com.seven.file.manager.tool

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.seven.basis.mmkv.MMKVInt

object MMKVTool {

    /**
     * App theme color 系统主题颜色
     */
    var appThemeColorProperty = MMKVInt(key = "AppThemeColor", default = Color.White.toArgb())
    var appThemeColor by appThemeColorProperty


    /**
     * Theme type property
     *
     * 主题类型：1、浅色 -1：深色 0：跟随系统
     */
    var themeTypeProperty = MMKVInt(key = "ThemeType", default = 0)
    var themeType by themeTypeProperty
}