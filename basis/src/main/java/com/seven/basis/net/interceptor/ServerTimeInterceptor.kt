package com.seven.basis.net.interceptor

import com.seven.basis.tool.TimeSyncManager
import okhttp3.Interceptor
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * CreateData:     2025/10/23
 *
 * Author:         ly2
 *
 * Description:    服务器时间，计算偏差值拦截器
 */
class ServerTimeInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestTime = System.currentTimeMillis()
        val response = chain.proceed(chain.request())
        val responseTime = System.currentTimeMillis()

        val dateHeader = response.header("Date")
        if (dateHeader != null) {
            runCatching {
                val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("GMT")
                val serverTime = sdf.parse(dateHeader)?.time ?: return@runCatching

                // 计算往返延迟时间
                val rtt = responseTime - requestTime
                val estimatedServerNow = serverTime + rtt / 2

                // 更新本地偏移
                val newOffset = estimatedServerNow - responseTime
                TimeSyncManager.updateOffset(newOffset)
            }
        }
        return response
    }
}
