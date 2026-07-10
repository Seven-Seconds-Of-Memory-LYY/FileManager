package com.seven.basis.net

/**
 * CreateData:     2026/4/13
 *
 * Author:         ly2
 *
 * Description:    简单的包装类，用于处理请求结果
 */
sealed class RequestResult<out T> {
    data class Success<out T>(val data: T?) : RequestResult<T>()
    data class Failure(val code: Int, val message: String, val error: Throwable? = null) : RequestResult<Nothing>()
}