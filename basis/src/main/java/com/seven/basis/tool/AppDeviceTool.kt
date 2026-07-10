package com.seven.basis.tool

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.seven.basis.BasisApplication
import java.security.MessageDigest
import java.util.UUID

/**
 * CreateData:     2026/4/16
 *
 * Author:         ly2
 *
 * Description:    设备工具类
 */
object AppDeviceTool {


    /**
     * 获得设备硬件标识
     *
     * @param context 上下文
     * @return 设备硬件标识
     */
    fun getDeviceId(context: Context? = BasisApplication.getContext()): String {
        //如果 context 为null，或已生成过 直接返回本地缓存
        if (null == context || BasisMMKVTool.deviceId.isNotEmpty()) {
            return BasisMMKVTool.deviceId
        }
        val sbDeviceId = StringBuilder()
        // 获得AndroidId（无需权限）
        val androidId: String? = getAndroidId(context)
        // 获得设备序列号（无需权限）
        val serial: String? = getSERIAL()
        // 获得硬件uuid（根据硬件相关属性，生成uuid）（无需权限）
        val uuid: String? = getDeviceUUID()

        // 追加androidId
        if (!androidId.isNullOrEmpty()) {
            sbDeviceId.append(androidId)
            sbDeviceId.append("|")
        }
        // 追加serial
        if (!serial.isNullOrEmpty()) {
            sbDeviceId.append(serial)
            sbDeviceId.append("|")
        }
        // 追加硬件uuid
        if (!uuid.isNullOrEmpty()) {
            sbDeviceId.append(uuid)
        }
        // 生成SHA1，统一DeviceId长度
        if (sbDeviceId.isNotEmpty()) {
            return try {
                val hash: ByteArray? = getHashByString(sbDeviceId.toString())
                val sha1: String = ConvertTool.bytes2HexString(hash ?: byteArrayOf())
                if (sha1.isNotEmpty()) {
                    // 返回最终的DeviceId
                    BasisMMKVTool.deviceId = sha1
                    sha1
                } else {
                    createUUID()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                createUUID()

            }
        }
        // 如果以上硬件标识数据均无法获得，
        // 则DeviceId默认使用系统随机数，这样保证DeviceId不为空
        // return UUID.randomUUID().toString().replace("-", "")
        return createUUID()
    }

    private fun createUUID(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * 获得设备的AndroidId
     *
     * @param context 上下文
     * @return 设备的AndroidId
     */
    @SuppressLint("HardwareIds")
    private fun getAndroidId(context: Context): String? {
        try {
            return Settings.Secure.getString(
                context.contentResolver, Settings.Secure.ANDROID_ID
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }


    /**
     * 获得设备硬件uuid
     * 使用硬件信息，计算出一个随机数
     *
     * @return 设备硬件uuid
     */
    private fun getDeviceUUID(): String? {
        val hardwareInfo = StringBuilder().apply {
            append(Build.BOARD) // 主板名称
            append(Build.BRAND) // 设备品牌
            append(Build.DEVICE) // 设备参数
            append(Build.DISPLAY) // 显示屏参数
            append(Build.HOST) // 执行代码编译的Host值
            append(Build.ID) // 修订版本列表
            append(Build.MANUFACTURER) // 制造商
            append(Build.PRODUCT) // 产品名称
            append(Build.MODEL) // 设备型号
            append(Build.TAGS) // 描述Build的标签
            append(Build.TYPE) // builder类型
            append(Build.USER) // 执行代码编译的User值
            append(Build.TIME) // 编译时间
            append(Build.HARDWARE) // 硬件名
            append(basisJson.encodeToString(Build.SUPPORTED_ABIS)) // 手机cpu架构，支持的指令集
        }
        return MD5Tool.calcMD5(hardwareInfo.toString())
    }


    /**
     * 获得设备序列号（如：WTK7N16923005607）, 个别设备无法获取
     *
     * @return 设备序列号
     */
    @SuppressLint("HardwareIds")
    @Suppress("DEPRECATION")
    private fun getSERIAL(): String? {
        try {
            return Build.SERIAL
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }


    /**
     * 取SHA1
     * @param data 数据
     * @return 对应的hash值
     */
    private fun getHashByString(data: String): ByteArray? {
        return try {
            val messageDigest = MessageDigest.getInstance("SHA1")
            messageDigest.reset()
            messageDigest.update(data.toByteArray(charset("UTF-8")))
            messageDigest.digest()
        } catch (e: Exception) {
            "".toByteArray()
        }
    }
}