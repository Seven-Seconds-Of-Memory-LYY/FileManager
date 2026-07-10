package com.seven.basis.net

/**
 * Author:         ly2
 *
 * CreateDate:     2023/6/8
 *
 * Description:    请求异常
 */
class BasisHttpException(val errCode: Int, msg: String = "") : Exception(msg)