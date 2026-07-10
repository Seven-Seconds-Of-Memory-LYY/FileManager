package com.seven.basis.tool

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * CreateData:     2026/1/16
 *
 * Author:         ly2
 *
 * Description:    Throwable 序列化
 */
object ThrowableSerializer : KSerializer<Throwable> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Throwable", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Throwable) {
        // 序列化时：将 BigDecimal 转成 String 存入 JSON
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Throwable {
        // 反序列化时：将 JSON 里的 String 转回 BigDecimal
        return basisJson.decodeFromString<Throwable>(decoder.decodeString())
    }
}