package com.seven.file.manager.screenRoute

import android.net.Uri
import com.seven.file.manager.entity.StorageSpace
import com.seven.file.manager.tool.UriSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

/**
 * CreateData:     2026/1/15
 *
 * Author:         ly2
 *
 * Description:    页面路由
 */
@Serializable
sealed class ScreenRoute(
    val isLightStatusBars: Boolean = true, //状态栏是否是亮色（内容展示黑色：图标、文字）
    val isLightNavigationBars: Boolean = isLightStatusBars, //导航栏是否是亮色（内容展示黑色：图标、文字）
) {

    /**
     * Home Main，主页
     */
    @Serializable
    object Main : ScreenRoute()

    @Serializable
    data class FileDirectory(val storageSpace: StorageSpace) : ScreenRoute()


}