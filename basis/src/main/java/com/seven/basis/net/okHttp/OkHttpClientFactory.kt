package com.seven.basis.net.okHttp

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * CreateData:     2025/12/12
 *
 * Author:         ly2
 *
 * Description:    OkHttpClient工厂
 */
object OkHttpClientFactory {
    /**
     * Base builder 通用配置
     */
    private val baseBuilder = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)

    /**
     * Normal client 正常OkHttpClient
     */
    val normalClient: OkHttpClient by lazy {
        baseBuilder
            .addInterceptor(DynamicInterceptorManager)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Download client 下载OkHttpClient
     */
    val downloadClient: OkHttpClient by lazy {
        baseBuilder
            .connectTimeout(60, TimeUnit.SECONDS) // 下载连接超时更长
            .readTimeout(0, TimeUnit.SECONDS)     // 读取时间设置为无限
            .build()
    }

}