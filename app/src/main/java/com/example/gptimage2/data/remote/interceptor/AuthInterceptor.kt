package com.example.gptimage2.data.remote.interceptor

import com.example.gptimage2.data.local.ApiKeyStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val apiKeyStore: ApiKeyStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        // Find the matching API key by checking if any known base URL is a prefix of the request URL
        val key = apiKeyStore.getApiKeyForRequestUrl(url)
        val newRequest = if (key.isNotBlank()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $key")
                .build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }
}
