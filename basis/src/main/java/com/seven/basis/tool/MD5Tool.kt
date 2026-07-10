package com.seven.basis.tool

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object MD5Tool {
    private val hexArray = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f")

    /***
     * 获取指定的字符串的MD5
     */
    fun calcMD5(originString: String): String? {
        try {
            // 创建具有MD5算法的信息摘要
            val md = MessageDigest.getInstance("MD5")
            // 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
            val bytes = md.digest(originString.toByteArray())
            // 将得到的字节数组变成字符串返回
            val s = byteArrayToHex(bytes)
            return s.lowercase(Locale.getDefault())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 将字节数组转换成十六进制，并以字符串的形式返回
     * 128位是指二进制位。二进制太长，所以一般都改写成16进制，
     * 每一位16进制数可以代替4位二进制数，所以128位二进制数写成16进制就变成了128/4=32位。
     */
    private fun byteArrayToHex(b: ByteArray): String {
        val sb = StringBuffer()
        for (i in b.indices) {
            sb.append(byteToHex(b[i]))
        }
        return sb.toString()
    }

    /**
     * 将一个字节转换成十六进制，并以字符串的形式返回
     */
    private fun byteToHex(b: Byte): String {
        var n = b.toInt()
        if (n < 0) n += 256
        val d1 = n / 16
        val d2 = n % 16
        return hexArray[d1] + hexArray[d2]
    }

    /**
     * MD5加密
     */
    fun encryptMD5(securityStr: String): String {
        val data = securityStr.toByteArray()
        var md5: MessageDigest? = null
        try {
            md5 = MessageDigest.getInstance("MD5")
            md5.update(data)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        val resultBytes = md5!!.digest()
        val builder = StringBuilder()
        for (resultByte in resultBytes) {
            if (Integer.toHexString(0xFF and resultByte.toInt()).length == 1) {
                builder.append("0").append(
                    Integer.toHexString(0xFF and resultByte.toInt())
                )
            } else {
                builder.append(Integer.toHexString(0xFF and resultByte.toInt()))
            }
        }
        return builder.toString()
    }
}