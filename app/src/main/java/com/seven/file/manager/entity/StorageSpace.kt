package com.seven.file.manager.entity

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.navigation.NavType
import com.seven.file.manager.tool.UriSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
    val absolutePath: String = "",
    @Serializable(with = UriSerializer::class)
    val rootUri: Uri = "/".toUri(),
) {
    val usedBytes: Long get() = totalBytes - freeBytes
}
val StorageSpaceNavType = object : NavType<StorageSpace>(isNullableAllowed = false) {

    override fun get(bundle: Bundle, key: String): StorageSpace? {
        // 从 Bundle 中恢复数据
        return bundle.getString(key)?.let { Json.decodeFromString(it) }
    }

    override fun parseValue(value: String): StorageSpace {
        // 导航底层会将参数进行 URL 解码，这里将其反序列化为对象
        // 注意：由于 value 是经过 URL 解码的 String，我们需要通过 Json 还原
        return Json.decodeFromString(Uri.decode(value))
    }

    override fun put(bundle: Bundle, key: String, value: StorageSpace) {
        // 存入 Bundle
        bundle.putString(key, Json.encodeToString(value))
    }

    override fun serializeAsValue(value: StorageSpace): String {
        // 将对象序列化为 String 用于路由拼接
        // 关键点：因为对象内部含有 Uri 或特殊字符，序列化后必须进行 Uri.encode 转义
        return Uri.encode(Json.encodeToString(value))
    }
}
