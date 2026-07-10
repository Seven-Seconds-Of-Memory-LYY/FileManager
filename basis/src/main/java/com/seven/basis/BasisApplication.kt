package com.seven.basis

import android.app.Application
import com.tencent.mmkv.MMKV
import com.seven.basis.manager.SingletonToastManager
import com.seven.basis.timberTool.PrettyBoxTree
import com.seven.basis.tool.AppTool
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 * CreateData:     2026/7/9
 *
 * Author:         ly2
 *
 * Description:    BasisApplication
 */
open class BasisApplication : Application() {

    companion object {
        private var app: WeakReference<Application>? = null

        fun getContext(): Application? {
            return app?.get()
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (AppTool.isMainProcessLegacy(this)) {
            onMain()
        } else {
            onCommon()
        }
    }

    /**
     * On main 主进程
     */
    protected open fun onMain() {
        app = WeakReference(this)
        MMKV.initialize(this)
        initTimber()
        SingletonToastManager.init(this)
    }

    /**
     * On common 所有进程
     */
    protected open fun onCommon() {

    }

    /**
     * Init timber 初始化日志工具
     */
    private fun initTimber() {
        Timber.uprootAll()
        if (AppTool.isDebug(this)) {
            Timber.plant(PrettyBoxTree())
        }
    }
}