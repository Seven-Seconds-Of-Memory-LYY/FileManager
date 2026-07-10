package com.seven.basis.net

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.seven.basis.net.interceptor.ServerTimeInterceptor
import com.seven.basis.net.okHttp.DynamicInterceptorManager
import com.seven.basis.net.okHttp.OkHttpClientFactory
import com.seven.basis.net.okHttp.OkHttpClientType
import com.seven.basis.tool.basisJson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import java.util.concurrent.ConcurrentHashMap

/**
 * CreateData:     2025/12/12
 *
 * Author:         ly2
 *
 * Description:    Retrofit 管理
 */
object RetrofitCacheManager {
    /**
     * Retrofit map Retrofit 实例集合
     * 使用 ConcurrentHashMap 存储 Retrofit 实例，键为 Base URL
     * 保证多线程环境下的安全访问
     */
    private val retrofitMap = ConcurrentHashMap<String, Retrofit>()

    /**
     * Service map Service 接口实例集合
     * 使用 ConcurrentHashMap 存储 Service 实例，键为 Base URL + 接口类名
     * 保证多线程环境下的安全访问
     */
    private val serviceMap = ConcurrentHashMap<String, Any>()

    private var normalBaseUrl = ""

    /**
     * 初始化网络配置
     */
    @JvmStatic
    fun init(baseUrl: String, interceptors: ArrayList<Interceptor>? = null) {
        this.normalBaseUrl = baseUrl
        interceptors?.let { DynamicInterceptorManager.addInterceptors(it) }
        DynamicInterceptorManager.addInterceptor(ServerTimeInterceptor())
        DynamicInterceptorManager.addInterceptor(BasisResponseLogInterceptor())
        retrofitMap.clear()
        serviceMap.clear()
    }

    /**
     * 【Service 接口实例创建】 (新增逻辑，实现 Service 实例的按需缓存)
     * 线程安全地获取或创建指定 Base URL 下的 Service 接口实例
     */
    fun <T> createService(baseUrl: String = normalBaseUrl, serviceClass: Class<T>): T {
        // 1. 生成唯一的缓存键：Base URL + 接口类名
        val serviceKey = "$baseUrl#${serviceClass.name}"

        // 2. 第一次快速检查 (不加锁)
        @Suppress("UNCHECKED_CAST")
        var serviceInstance = serviceMap[serviceKey] as? T
        if (serviceInstance != null) {
            return serviceInstance
        }

        // 3. 如果不存在，进入同步块
        synchronized(this) {
            // 4. 第二次检查 (加锁后)
            @Suppress("UNCHECKED_CAST")
            serviceInstance = serviceMap[serviceKey] as? T
            if (serviceInstance == null) {
                // 5. 如果确实不存在，先获取对应的 Retrofit 实例
                val retrofitInstance = getRetrofit(baseUrl.ifBlank {
                    normalBaseUrl
                })

                // 6. 使用 Retrofit 实例创建 Service 接口的实现
                serviceInstance = retrofitInstance.create(serviceClass)

                // 7. 存入 Service 缓存
                serviceMap[serviceKey] = serviceInstance!!
            }
        }

        // 8. 返回 Service 实例
        return serviceInstance!!
    }

    /**
     * 获取或创建 Retrofit 实例
     * @param baseUrl 对应服务的 Base URL
     * @param clientType OkHttpClientFactory 中定义的客户端类型 (Normal/Download)
     */
    private fun getRetrofit(
        baseUrl: String,
        clientType: OkHttpClientType = OkHttpClientType.NORMAL
    ): Retrofit {
        // 1. 快速检查 (不加锁)：如果已存在，则直接返回
        var retrofitInstance = retrofitMap[baseUrl]
        if (retrofitInstance != null) {
            return retrofitInstance
        }

        // 2. 如果不存在，则进入同步块，保证只有一个线程创建实例
        // 锁定当前对象 (RetrofitCacheManager.class)
        synchronized(this) {
            // 3. 再次检查 (加锁后)：防止多个线程等待进入同步块时重复创建
            retrofitInstance = retrofitMap[baseUrl]
            if (retrofitInstance == null) {
                // 4. 创建新的 Retrofit 实例
                val client = when (clientType) {
                    OkHttpClientType.NORMAL -> OkHttpClientFactory.normalClient
                    OkHttpClientType.DOWNLOAD -> OkHttpClientFactory.downloadClient
                }
                retrofitInstance = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(basisJson.asConverterFactory("application/json".toMediaType()))
                    .build()

                // 5. 存入 Map
                retrofitMap[baseUrl] = retrofitInstance
            }
        }
        // 6. 返回结果 (无论是获取到的还是新创建的)
        return retrofitInstance!! // 此时 retrofitInstance 不可能为 null
    }
}
