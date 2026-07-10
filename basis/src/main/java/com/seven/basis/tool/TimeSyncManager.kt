package com.seven.basis.tool


/**
 * CreateData:     2025/10/23
 *
 * Author:         ly2
 *
 * Description:    服务器时间，计算本地时间偏差值
 */
object TimeSyncManager {
    // 与服务器的时间偏移量（单位：毫秒）
    @Volatile
    private var offset: Long = 0L

    // 平滑系数（越小越平滑）
    private const val SMOOTH_FACTOR = 0.2f

    // 更新偏移量（在拦截器中调用）
    fun updateOffset(newOffset: Long) {
        synchronized(this) {
            // 平滑更新，防止抖动过大
            offset = if (offset == 0L) {
                newOffset
            } else {
                ((1 - SMOOTH_FACTOR) * offset + SMOOTH_FACTOR * newOffset).toLong()
            }
        }
    }

    /**
     * Now millis   获取当前服务端时间（毫秒）
     *
     * @return Long
     */
    fun nowMillis(): Long {
        return System.currentTimeMillis() + offset
    }

    /**
     * Now seconds 获取当前服务端时间（秒）
     *
     * @return Long
     */
    fun nowSeconds(): Long {
        return nowMillis() / 1000
    }

}