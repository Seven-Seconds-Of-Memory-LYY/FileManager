package com.seven.basis.mmkv

import kotlin.reflect.KProperty

// String 类型
class MMKVString(key: String, default: String = "") : MMKVProperty<String>(key, default) {
    override fun readValue(): String {
        return mmkv.decodeString(keyName, defaultValue) ?: defaultValue
    }

    override fun writeValue(value: String) {
        mmkv.encode(keyName, value)
    }
}

// Int 类型
class MMKVInt(key: String, default: Int = 0) : MMKVProperty<Int>(key, default) {
    override fun readValue(): Int {
        return mmkv.decodeInt(keyName, defaultValue)
    }

    override fun writeValue(value: Int) {
        mmkv.encode(keyName, value)
    }
}

// Boolean 类型
class MMKVBool(key: String, default: Boolean = false) : MMKVProperty<Boolean>(key, default) {
    override fun readValue(): Boolean {
        return mmkv.decodeBool(keyName, defaultValue)
    }

    override fun writeValue(value: Boolean) {
        mmkv.encode(keyName, value)
    }
}

// Long 类型
class MMKVLong(key: String, default: Long = 0) : MMKVProperty<Long>(key, default) {
    override fun readValue(): Long {
        return mmkv.decodeLong(keyName, defaultValue)
    }

    override fun writeValue(value: Long) {
        mmkv.encode(keyName, value)
    }
}