package com.seven.basis.net

/**
 * CreateData:     2023/6/8
 *
 * Author:         ly2
 *
 * Description:    网络请求放回Code
 *
 */
object HttpCode {
    /**
     * 未知
     */
    const val CODE_UNKNOW = -1

    /**
     * 请求成功
     */
    const val CODE_SUCCESS = 200

    /**
     * token过期
     */
    const val CODE_TOKEN_EXPIRED = 701

    /**
     * 超时
     */
    const val CODE_TIMEOUT = 401

    /**
     * 不可达
     */
    const val CODE_UNREACHABLE = 404

    /**
     * 解析数据异常
     */
    const val CODE_PARSE_ERROR = 501

}