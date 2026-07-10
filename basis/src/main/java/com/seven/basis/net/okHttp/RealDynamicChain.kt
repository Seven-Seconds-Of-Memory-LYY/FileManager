package com.seven.basis.net.okHttp

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 辅助类：实现内部拦截器链的推进。
 */
class RealDynamicChain(
    private val chainList: List<Interceptor>,
    private val index: Int,
    // 关键修正：RealDynamicChain 实例需要携带它应该处理的 Request
    private val currentRequest: okhttp3.Request,
    // 原始 Chain 仍然用来委托其他方法和最终的网络请求
    private val originalChain: Interceptor.Chain
) : Interceptor.Chain by originalChain {

    // 重写 request() 方法，返回当前 RealDynamicChain 实例携带的 Request
    override fun request(): okhttp3.Request = currentRequest

    // 核心逻辑：拦截器链的推进
    override fun proceed(request: okhttp3.Request): Response {
        if (index >= chainList.size) {
            // 链尾：所有动态拦截器都已执行，现在交给原始 Chain，触发网络请求
            return originalChain.proceed(request)
        }

        // 1. 创建下一个 RealDynamicChain 实例
        val nextChain = RealDynamicChain(
            chainList = chainList,
            index = index + 1,
            // 修正点：将最新的 Request 传递给下一个 RealDynamicChain 实例
            currentRequest = request,
            originalChain = originalChain
        )

        // 2. 获取当前的动态拦截器
        val interceptor = chainList[index]

        // 3. 执行当前的拦截器，它调用 nextChain.proceed()
        val response = interceptor.intercept(nextChain)

        return response
    }
}