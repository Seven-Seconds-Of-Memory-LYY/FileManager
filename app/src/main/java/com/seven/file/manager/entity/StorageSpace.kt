package com.seven.file.manager.entity

import android.net.Uri
import androidx.core.net.toUri
import com.seven.file.manager.tool.UriSerializer
import kotlinx.serialization.Serializable

/**
 * CreateData:     2026/7/10
 *
 * Author:         ly2
 *
 * Description:    StorageSpace 存储空间
 */
@Serializable
data class StorageSpace(
    val id: String = "",              // 唯一标识（使用 UUID 或 primary 标记）
    val description: String = "",     // 系统盘名称（如 "内部存储"、"SanDisk USB 驱动器"）
    val isPrimary: Boolean = true,      // 是否为主存储（内置存储）
    val isRemovable: Boolean = false,    // 是否为可拔插设备（如 SD卡、U盘）
    val totalBytes: Long = 0,
    val freeBytes: Long = 0,
    @Serializable(with = UriSerializer::class)
    val rootUri: Uri = "/".toUri(),
) {
    val usedBytes: Long get() = totalBytes - freeBytes
}