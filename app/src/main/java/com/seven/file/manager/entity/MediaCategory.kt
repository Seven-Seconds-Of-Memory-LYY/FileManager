package com.seven.file.manager.entity

/**
 * CreateData:     2026/7/10
 *
 * Author:         ly2
 *
 * Description:    MediaCategory 媒体分类
 */
enum class MediaCategory {
    IMAGE, AUDIO, VIDEO;

    // 映射为 MediaStore 查询需要的 MimeType 前缀
    fun toMimePrefix(): String = when (this) {
        IMAGE -> "image/%"
        AUDIO -> "audio/%"
        VIDEO -> "video/%"
    }
}