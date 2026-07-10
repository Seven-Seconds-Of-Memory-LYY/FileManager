package com.seven.basis

import androidx.annotation.StringRes

/**
 * CreateData:     2026/7/9
 *
 * Author:         ly2
 *
 * Description:    BasisAction 基础事件
 */
sealed class BasisAction {

    /**
     * Show toast 显示吐司
     */
    data class ShowToast(val msg: String) : BasisAction()

    /**
     * Show toast 显示吐司
     */
    data class ShowToastRes(@param:StringRes val msgRes: Int) : BasisAction()

    /**
     * Show loading 展示加载dialog
     * @property msg 标题
     */
    data class ShowLoading(val msg: String? = null) : BasisAction()

    /**
     * Dismiss loading 隐藏加载dialog
     */
    object DismissLoading : BasisAction()

    /**
     * Token expired token过期
     */
    object TokenExpired : BasisAction()
}