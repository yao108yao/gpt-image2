package com.example.gptimage2.data.remote.interceptor

import com.example.gptimage2.data.local.ApiKeyStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val apiKeyStore: ApiKeyStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val key = apiKeyStore.getApiKeySync()
        val request = if (key.isNotBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $key")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
