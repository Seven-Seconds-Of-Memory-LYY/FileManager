package com.seven.basis.tool

import kotlinx.serialization.json.Json


/**
 * Basis json 定义Json
 */
val basisJson = Json {
    // 如果 JSON 里有 App 没定义的字段，忽略它（不崩）
    ignoreUnknownKeys = true

    //  如果 JSON 格式不规范（比如 Key 没加双引号），强制解析
    isLenient = true

    // 将 JSON 中的 String 自动转换为数字类型（或反之）
    coerceInputValues = true

    // 允许解析包含注释的 JSON
    allowComments = true

    // 编码默认值：序列化时，如果字段值等于默认值，也将其写入 JSON
    encodeDefaults = true

    // 序列化时忽略 null 值
    explicitNulls = true
}

/**
 * Decode from string or null 反序列化可空、null
 */
inline fun <reified T> Json.decodeFromStringOrNull(json: String?): T? {
    if (json.isNullOrBlank()) return null
    return runCatching {
        decodeFromString<T>(json)
    }.getOrNull()
}