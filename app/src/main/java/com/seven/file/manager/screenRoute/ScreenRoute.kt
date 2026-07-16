package com.seven.file.manager.screenRoute

import com.seven.file.manager.entity.StorageSpace
import kotlinx.serialization.Serializable

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

    /**
     * File directory 文件浏览
     *
     * @property storageSpace the storage space
     */
    @Serializable
    data class FileDirectory(val storageSpace: StorageSpace) : ScreenRoute()

    /**
     * Media files 媒体文件列表
     *
     * @property mediaCategoryName the media category name 媒体分类名称
     */
    @Serializable
    data class MediaFiles(val mediaCategoryName: String) : ScreenRoute()


}