package com.seven.basis.manager

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * CreateData:     2025/12/15
 *
 * Author:         ly2
 *
 * Description:    Toast 管理器：确保新的 Toast 总是覆盖旧的 Toast，避免消息排队。
 */
class SingletonToastManager private constructor(private val appContext: Context) {

    // 使用 CoroutineScope，绑定到 Dispatchers.Main 确保所有代码在主线程执行。
    // SupervisorJob 允许子协程失败不影响 Scope。
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 追踪当前活跃的 Toast 实例
    private var currentToast: Toast? = null

    companion object {
        @Volatile
        private var INSTANCE: SingletonToastManager? = null

        /**
         * 初始化方法：建议在 Application 的 onCreate 中调用。
         * @param context 必须是 Application Context 以避免内存泄漏。
         */
        fun init(context: Context): SingletonToastManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SingletonToastManager(context.applicationContext).also { INSTANCE = it }
            }

        /**
         * 获取单例实例。
         */
        fun getInstance(): SingletonToastManager {
            return INSTANCE ?: throw IllegalStateException("SingletonToastManager must be initialized via init(Context) first.")
        }
    }

    /**
     * 显示一个新的 Toast 消息。
     * 自动取消前一个正在排队或正在显示的 Toast。
     *
     * @param message 要显示的消息文本。
     * @param duration Toast 的显示时长，使用 Toast.LENGTH_SHORT 或 Toast.LENGTH_LONG。
     */
    fun show(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        // 使用 scope.launch 在主线程执行，确保 UI 安全
        scope.launch {
            // 1. 取消旧的 Toast
            currentToast?.cancel()

            // 2. 创建并存储新的 Toast 实例
            val newToast = Toast.makeText(appContext, message, duration)
            currentToast = newToast

            // 3. 显示新的 Toast
            newToast.show()
        }
    }

    /**
     * 手动取消当前正在显示的 Toast。
     */
    fun cancel() {
        // 使用 scope.launch 在主线程执行
        scope.launch {
            currentToast?.cancel()
            currentToast = null
        }
    }

    /**
     * 清理资源：在应用关闭时调用（可选）。
     */
    fun release() {
        // 取消 Scope 下的所有协程
        scope.cancel()
        currentToast?.cancel()
        currentToast = null
        INSTANCE = null // 重置单例
    }
}