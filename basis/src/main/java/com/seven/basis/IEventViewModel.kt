package com.seven.basis

import kotlinx.coroutines.flow.SharedFlow

/**
 * CreateData:     2025/12/12
 *
 * Author:         ly2
 *
 * Description:    动作触发接口，在activity监听，viewmodel中发出事件
 */
interface IEventViewModel {
    val actionEvent: SharedFlow<BasisAction>
}