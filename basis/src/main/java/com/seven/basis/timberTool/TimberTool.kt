package com.seven.basis.timberTool

import timber.log.Timber


object TimberTool {
    // 修改：tag 方法不再返回全局单例 TimberTool，而是返回一个一次性的 Builder 对象
    fun tag(tag: String): LogBuilder {
        return LogBuilder(tag)
    }

    fun dArgs(vararg args: Any?) = LogBuilder(null).dArgs(*args)

    fun iArgs(vararg args: Any?) = LogBuilder(null).iArgs(*args)

    fun vArgs(vararg args: Any?) = LogBuilder(null).vArgs(*args)

    fun wArgs(vararg args: Any?) = LogBuilder(null).wArgs(*args)

    fun eArgs(vararg args: Any?) = LogBuilder(null).eArgs(*args)

    // 内部核心：一次性构建者类
    class LogBuilder(private val customTag: String?) {

        private fun getTimber(): Timber.Tree {
            return if (!customTag.isNullOrBlank()) {
                Timber.tag(customTag) // 每次都直接用传进来的 tag，不存入 ThreadLocal
            } else {
                Timber.asTree()
            }
        }

        fun dArgs(vararg args: Any?) {
            getTimber().dArgs(args = args)
        }

        fun iArgs(vararg args: Any?) {
            getTimber().iArgs(args = args)
        }

        fun vArgs(vararg args: Any?) {
            getTimber().vArgs(args = args)
        }

        fun wArgs(vararg args: Any?) {
            getTimber().wArgs(args = args)
        }

        fun eArgs(vararg args: Any?) {
            getTimber().eArgs(args = args)
        }
    }
}

