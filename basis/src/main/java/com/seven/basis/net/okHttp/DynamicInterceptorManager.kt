package com.seven.basis.net.okHttp

import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 核心：动态拦截器管理器，作为 OkHttpClient 的一个固定拦截器。
 */
object DynamicInterceptorManager : Interceptor {

    // 使用 CopyOnWriteArrayList 确保线程安全地增删操作
    private val dynamicInterceptors = CopyOnWriteArrayList<Interceptor>()

    // -- 外部调用接口 --
    fun addInterceptor(interceptor: Interceptor) {
        dynamicInterceptors.add(interceptor)
    }

    fun addInterceptors(interceptors: List<Interceptor>) {
        dynamicInterceptors.addAll(interceptors)
    }

    fun removeInterceptor(interceptor: Interceptor) {
        dynamicInterceptors.remove(interceptor)
    }

    // -- 核心 Chain 逻辑 --
    override fun intercept(chain: Interceptor.Chain): Response {
        val chainList = dynamicInterceptors.toList()

        // 修正点：构造 RealDynamicChain 时，传入原始请求作为初始 currentRequest
        val internalChain = RealDynamicChain(
            chainList = chainList,
            index = 0,
            currentRequest = chain.request(), // 传入初始请求
            originalChain = chain
        )

        // 启动内部链，返回最终的 Response
        // 注意：这里调用的是 internalChain.proceed()，它会启动链式调用
        return internalChain.proceed(chain.request())
    }
}