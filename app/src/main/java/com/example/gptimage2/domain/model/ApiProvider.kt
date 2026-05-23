package com.example.gptimage2.domain.model

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class ApiProvider(val name: String, val baseUrl: String)

@JsonClass(generateAdapter = true)
data class ProvidersWrapper(val providers: List<ApiProvider>)

object ProviderRegistry {
    private val moshi: Moshi by lazy {
        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    }
    private val adapter by lazy { moshi.adapter(ProvidersWrapper::class.java) }

    val INITIAL_PRESETS = listOf(
        ApiProvider("Dreamfield", "https://www.dreamfield.top/v1"),
        ApiProvider("OpenAI 官方", "https://api.openai.com/v1"),
    )

    fun selectedIndex(all: List<ApiProvider>, baseUrl: String): Int {
        return all.indexOfFirst { it.baseUrl == baseUrl }.coerceAtLeast(0)
    }

    fun toJson(providers: List<ApiProvider>): String {
        return adapter.toJson(ProvidersWrapper(providers))
    }

    fun fromJson(json: String): List<ApiProvider> {
        return try {
            adapter.fromJson(json)?.providers ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
