package com.seven.basis.net

import com.seven.basis.timberTool.TimberTool
import com.seven.basis.tool.basisJson
import kotlinx.serialization.json.JsonElement
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/**
 * CreateData:     2023/7/7

 * Author:         ly2

 * Description:    请求返回拦截器
 */
class BasisResponseLogInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (isMultipart(request = chain.request())) {
            return chain.proceed(chain.request())
        }
        val requestBodySb = StringBuilder()
        runCatching {
            val requestBody = chain.request().body
            if (requestBody?.contentType()?.toString()?.lowercase()?.contains("multipart/form-data") == false) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                requestBodySb.append(buffer.readUtf8())
            }
        }.onFailure {
            TimberTool.eArgs(it.message)
        }
        val response: Response?
        val requestUrl = chain.request().url.toUri().toString()
        val token = chain.request().header("token").toString()
        try {
            response = chain.proceed(chain.request())
        } catch (e: Exception) {
            val resp = e.toString()
            TimberTool.eArgs(
                    requestUrl,
                    token,
                    requestBodySb,
                    resp
            )

            val body = BasisResponseEntity<JsonElement>(code = HttpCode.CODE_UNREACHABLE, msg = e.message.orEmpty())
            val mediaType = "application/json; charset=utf-8".toMediaType()
            return Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .body(basisJson.encodeToString(body).toResponseBody(mediaType))
                .message("")
                .build()
        }

        val build: Response.Builder = response.newBuilder()
        val clone: Response = build.build()
        var body = clone.body
        val mediaType = body.contentType()
        val resp = body.string()
        TimberTool.iArgs(
                requestUrl,
                token,
                requestBodySb,
                resp
        )
        // 创建新的response
        body = resp.toResponseBody(mediaType)
        return response.newBuilder().body(body).build()

    }

    /**
     * 是否文件上传请求
     * @param request Request
     * @return Boolean
     */
    private fun isMultipart(request: Request): Boolean {
        if (request.method.lowercase() == "post") {
            val header = request.header("Content-Type")
            return header != null && header.startsWith("multipart/form-data")
        }
        return false
    }
}