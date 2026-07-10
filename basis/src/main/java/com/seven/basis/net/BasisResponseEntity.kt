package com.seven.basis.net

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

/**
 * CreateData:     2023/7/7

 * Author:         ly2

 * Description:    基础返回数据对象
 */
@Keep
@Serializable
class BasisResponseEntity<T>(
    val code: Int = -1,
    val msg: String = "",
    val data: T? = null
){
    // 业务是否成功
    fun isSuccess() = code == HttpCode.CODE_SUCCESS
}