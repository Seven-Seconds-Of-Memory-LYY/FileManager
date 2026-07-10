package com.seven.basis.timberTool

import android.util.Log
import timber.log.Timber

/**
 * 实现类似 PrettyLogger 的 Box 格式日志输出。
 * 注意：Logcat 仅支持每行最多 4000 个字符。
 */
class PrettyBoxTree(
    private val maxStackCount: Int = 2 // 限制打印的堆栈行数
) : Timber.DebugTree() {
    private val normalTag = "BasisLog"

    // Box Drawing Characters (盒绘字符)
    private val topBorder = "╔"
    private val middleBorder = "╟"
    private val bottomBorder = "╚"
    private val vertical = "║"
    private val callTrace = "├"
    private val lastTrace = "└"

    // 默认的日志长度，用于绘制边框
    private val defaultLineLength = 100

    // Logcat 单行最大字符限制（预留安全空间取 3800）
    private val maxLogLength = 3800

    /**
     * 重写 log 方法，定制输出格式。
     */
    @Synchronized
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val endTag = tag ?: normalTag

        // 获取和格式化堆栈信息
        val stackTrace = Throwable().stackTrace
        val traces = getFormattedStackTrace(stackTrace)

        // 绘制顶部边框
        val header = drawLine(topBorder)

        // 构建堆栈信息块
        val traceBlock = traces.joinToString("\n$vertical\t") { it }

        // 打印头部信息
        Log.println(priority, endTag, "$header\n$vertical\t$traceBlock")

        val msgList = message.split("||").filter { it.isNotBlank() }

        // 分隔符
        val divider = "$middleBorder${"─".repeat(defaultLineLength)}"
        Log.println(priority, endTag, divider)

        if (msgList.size == 1) {
            printLongMessage(priority, endTag, msgList.first(), "")
        } else {
            msgList.forEachIndexed { index, subMsg ->
                printLongMessage(priority, endTag, subMsg, "p$index -> ")
            }
        }
        // 绘制底部边框
        val footer = drawLine(bottomBorder)
        Log.println(priority, endTag, footer)
    }

    /**
     * 核心补全：循环切片打印长文本，确保每一行都有盒子边框
     */
    private fun printLongMessage(priority: Int, tag: String, msg: String, prefix: String) {
        var i = 0
        val length = msg.length

        // 如果内容为空，直接打印空行边框
        if (length == 0) {
            Log.println(priority, tag, "$vertical\t$prefix")
            return
        }

        // 循环切片
        while (i < length) {
            val end = minOf(i + maxLogLength, length)
            val part = msg.substring(i, end)

            // 只有第一行带自定义前缀（如 p0 ->），后续行对齐
            val currentPrefix = if (i == 0) prefix else " ".repeat(prefix.length)

            // 按换行符再次细分，防止单段内自带换行导致边框丢失
            val lines = part.split("\n")
            lines.forEachIndexed { index, line ->
                if (i == 0 && index == 0) {
                    Log.println(priority, tag, "$vertical\t$currentPrefix$line")
                } else {
                    // 后续行或切片行保持对齐
                    Log.println(priority, tag, "$vertical\t${" ".repeat(prefix.length)}$line")
                }
            }
            i = end
        }
    }

    /**
     * 辅助函数：获取格式化的堆栈信息
     */
    private fun getFormattedStackTrace(stackTrace: Array<StackTraceElement>): List<String> {
        val traces = mutableListOf<String>()
        // 忽略 Timber/Log 相关的堆栈元素，找到实际的调用者
        val filteredStack = stackTrace.filter { element ->
            !element.className.startsWith("timber.log.") &&
                    !element.className.startsWith("android.util.Log") &&
                    element.className != this.javaClass.name &&
                    !element.className.startsWith("kotlinx.coroutines.") && // 忽略协程内部调用
                    !element.className.startsWith("com.yogoshort.basis.timberTool.")
        }.toList()

        for (i in 0 until minOf(maxStackCount, filteredStack.size)) {
            val element = filteredStack[i]
            val prefix = if (i == minOf(maxStackCount, filteredStack.size) - 1) lastTrace else callTrace
            val className = element.className.substringAfterLast('.')

            // 格式: ├ com.package.Class.method(File.kt:Line)
            traces.add("$prefix $className.${element.methodName}(${element.fileName}:${element.lineNumber})")
        }
        return traces
    }

    /**
     * 辅助函数：绘制指定长度的边框线
     */
    private fun drawLine(prefix: String, length: Int = defaultLineLength): String {
        return prefix + "═".repeat(length)
    }


    // Tag 定义
    override fun createStackElementTag(element: StackTraceElement): String {
        return normalTag
    }
}