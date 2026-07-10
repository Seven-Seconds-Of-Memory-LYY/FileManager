package com.seven.file.manager.extensions

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Bytes to gb on the receiver [Long] bytes 转换 Gb
 *
 * @return the string 0 GB
 */
fun Long.bytesToGB(): String {
    return runCatching {
        BigDecimal(this).divide(BigDecimal(1024), 10, RoundingMode.HALF_DOWN)
            .divide(BigDecimal(1024), 10, RoundingMode.HALF_DOWN)
            .divide(BigDecimal(1024), 2, RoundingMode.HALF_DOWN)
    }.getOrDefault(BigDecimal.ZERO)
        .toDouble()
        .toString()
        .plus(" GB")
}