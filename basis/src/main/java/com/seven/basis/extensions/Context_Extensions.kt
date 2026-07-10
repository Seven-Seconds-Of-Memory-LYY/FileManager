package com.seven.basis.extensions

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.seven.basis.timberTool.TimberTool

/**
 * 跳转系统浏览器
 *
 * @param url 地址
 * @param onError 错误回调
 */
fun Context.jumpSystemBor(url: String?, onError: (Throwable) -> Unit = {}) {
    runCatching {
        // 创建一个 ACTION_VIEW 的 Intent，并传入解析后的 Uri
        val intent = Intent(Intent.ACTION_VIEW, url.orEmpty().toUri()).apply {
            // 可选：如果希望浏览器在一个全新的任务栈中打开，可以加上这个 Flag
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }.onFailure {
        onError.invoke(it)
        TimberTool.equals(it.message)
    }
}