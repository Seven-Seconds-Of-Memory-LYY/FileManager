package com.seven.basis.tool

import com.seven.basis.mmkv.MMKVString

object BasisMMKVTool {

    /**
     * Device id 设备id
     */
    var deviceId by MMKVString(key = "keyDeviceId")
}