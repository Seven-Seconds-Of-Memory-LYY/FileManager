package com.seven.file.manager.entity

import android.net.Uri


/**
 * CreateData:     2026/7/10
 *
 * Author:         ly2
 *
 * Description:    MediaFile 媒体文件
 */
data class MediaFile(
    val id: Long,
    val name: String,
    val path: Long,
    val size: String,
    val type: MediaCategory,
    val uri: Uri
)