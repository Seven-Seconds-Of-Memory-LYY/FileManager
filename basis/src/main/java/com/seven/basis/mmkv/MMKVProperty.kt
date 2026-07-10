package com.seven.basis.mmkv

import com.tencent.mmkv.MMKV
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * MMKV 基础委托类
 */
abstract class MMKVProperty<T>(
    protected val keyName: String,
    protected val defaultValue: T,
    protected val mmkv: MMKV = MMKV.defaultMMKV()
) : ReadWriteProperty<Any?, T> {
    // 只有当前属性被监听时，才会往这个列表里塞入回调，不监听则为 0 消耗
    private val localListeners = mutableListOf<(T) -> Unit>()

    abstract fun readValue(): T
    abstract fun writeValue(value: T)

    final override fun getValue(thisRef: Any?, property: KProperty<*>): T = readValue()

    final override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val oldValue = readValue()
        if (oldValue != value) {
            writeValue(value)
            // 依然是那同一个单例对象，直接通知
            localListeners.forEach { it.invoke(value) }
        }
    }

    /**
     * 属性专属的冷流：谁需要监听，直接对这个委托对象调用 .asFlow()
     */
    fun asFlow(): Flow<T> = callbackFlow {
        // 1. 刚订阅时触发一次 readValue() 发射初始值
        trySend(readValue())

        // 2. 创建回调，直接将新值 trySend 出去
        val listener: (T) -> Unit = { newValue ->
            trySend(newValue)
        }
        localListeners.add(listener)

        // 3. 当协程生命周期结束（如 Activity 销毁），自动从列表中移除，防止内存泄漏
        awaitClose {
            localListeners.remove(listener)
        }
    }
}