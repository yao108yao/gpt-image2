package com.example.gptimage2.util

import com.example.gptimage2.data.repository.ImageRepository
import com.squareup.moshi.Moshi
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

object ApiErrorParser {

    private val moshi = Moshi.Builder().build()

    fun parse(e: Throwable): String {
        return when (e) {
            is ImageRepository.ApiException -> parseHttpError(e.code, e.errorBody)
            is SocketTimeoutException -> "请求超时，请稍后重试"
            is ConnectException -> "无法连接服务器，请检查网络"
            is IOException -> "网络错误：${e.message}"
            else -> e.message ?: "未知错误"
        }
    }

    private fun parseHttpError(code: Int, body: String): String {
        val serverMessage = parseErrorBody(body)
        if (!serverMessage.isNullOrBlank()) return serverMessage

        return when (code) {
            400 -> "请求参数错误 (400)"
            401 -> "API Key 无效或未设置 (401)"
            403 -> "没有权限访问 (403)"
            404 -> "接口不存在 (404)"
            429 -> "请求过于频繁，请稍后重试 (429)"
            in 500..599 -> "服务器错误 ($code)"
            else -> "HTTP 错误 ($code)"
        }
    }

    private fun parseErrorBody(body: String): String? {
        if (body.isBlank()) return null
        return try {
            val adapter = moshi.adapter(Map::class.java)
            val map = adapter.fromJson(body) as? Map<*, *>
            val error = map?.get("error")
            if (error is Map<*, *>) {
                val message = error["message"] as? String
                val code = error["code"] as? String
                when {
                    !message.isNullOrBlank() && !code.isNullOrBlank() -> "$message ($code)"
                    !message.isNullOrBlank() -> message
                    else -> null
                }
            } else {
                map?.get("message") as? String
            }
        } catch (_: Exception) {
            null
        }
    }
}
