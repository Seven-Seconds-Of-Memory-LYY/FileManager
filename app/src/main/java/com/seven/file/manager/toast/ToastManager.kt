package com.seven.file.manager.toast

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * CreateData:     2026/4/15
 *
 * Author:         ly2
 *
 * Description:    吐司管理类
 */
object ToastManager {
    // 仅控制文本和显示开关状态
    var message by mutableStateOf("")
    var isVisible by mutableStateOf(false)

    private var job: Job? = null
    private val globalScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun show(message: String?) {
        if (message.isNullOrBlank()) return
        ToastManager.message = message
        job?.cancel()
        job = globalScope.launch {
            isVisible = true
            delay(2000L.milliseconds)
            isVisible = false
        }
    }
}