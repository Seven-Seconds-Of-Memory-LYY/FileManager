package com.seven.basis.tool

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Process
import com.seven.basis.BasisApplication
import timber.log.Timber

/**
 * CreateData:     2025/12/10
 *
 * Author:         ly2
 *
 * Description:    app相关
 */
object AppTool {

    /**
     * Is main process legacy 是否是主线程
     *
     * @return
     */
    fun isMainProcessLegacy(context: Context): Boolean {
        val currentPid = Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

        // 对 ActivityManager 进行关键的空值检查
        if (activityManager == null) {
            Timber.i("ActivityManager is null, cannot determine main process.")
            return false
        }

        val runningAppProcesses: List<ActivityManager.RunningAppProcessInfo>? = activityManager.runningAppProcesses

        // 空值判断
        if (runningAppProcesses == null) {
            Timber.i("activityManager.runningAppProcesses returned null.")
            return false
        }

        // Now it's safe to iterate over runningAppProcesses
        for (processInfo in runningAppProcesses) {
            if (processInfo.pid == currentPid) {
                return processInfo.processName == context.packageName
            }
        }
        return false
    }

    /**
     * 是否是调试模式
     * @param context Context
     * @return Boolean
     */
    @Suppress("DEPRECATION")
    fun isDebug(context: Context? = BasisApplication.getContext()): Boolean {
        if (null == context) {
            return false
        }
        return runCatching {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            val isDebug = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            isDebug
        }.getOrDefault(false)
    }
}