package com.seven.file.manager.tool

import android.net.Uri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * CreateData:     2026/7/10
 *
 * Author:         ly2
 *
 * Description:    UriSerializer Uri 序列化
 */
object UriSerializer : KSerializer<Uri> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("AndroidUri", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Uri) {
        // 序列化时转为 String
        // 如果用于 Navigation，建议使用 Uri.encode 确保特殊字符安全
        encoder.encodeString(Uri.encode(value.toString()))
    }

    override fun deserialize(decoder: Decoder): Uri {
        // 反序列化时还原为 Uri
        val decoded = Uri.decode(decoder.decodeString())
        return Uri.parse(decoded)
    }
}