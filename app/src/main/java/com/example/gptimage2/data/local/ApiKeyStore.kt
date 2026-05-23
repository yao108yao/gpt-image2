package com.example.gptimage2.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.gptimage2.domain.model.ApiProvider
import com.example.gptimage2.domain.model.ProviderRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ApiKeyStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "api_key_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _apiKey = MutableStateFlow("")

    fun getApiKeyFlow(): Flow<String> = _apiKey.asStateFlow()

    fun getApiKeyForProvider(baseUrl: String): String {
        val key = "api_key_${baseUrl.trimEnd('/')}"
        return prefs.getString(key, "") ?: ""
    }

    fun setApiKeyForProvider(baseUrl: String, apiKey: String) {
        val key = "api_key_${baseUrl.trimEnd('/')}"
        prefs.edit().putString(key, apiKey).apply()
    }

    fun getApiKeyForRequestUrl(requestUrl: String): String {
        val allUrls = getProviders().map { it.baseUrl.trimEnd('/') }
        for (baseUrl in allUrls) {
            if (requestUrl.startsWith(baseUrl)) {
                return getApiKeyForProvider(baseUrl)
            }
        }
        return ""
    }

    fun hasApiKey(): Boolean {
        val baseUrl = getBaseUrl()
        return getApiKeyForProvider(baseUrl).isNotBlank()
    }

    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, "") ?: ""
    }

    fun setBaseUrl(url: String) {
        val normalized = url.trimEnd('/')
        prefs.edit().putString(KEY_BASE_URL, normalized).apply()
    }

    fun getProviders(): List<ApiProvider> {
        val json = prefs.getString(KEY_PROVIDERS, null)
        if (json != null) {
            return ProviderRegistry.fromJson(json)
        }
        // First run: initialize with default presets
        val defaults = ProviderRegistry.INITIAL_PRESETS
        setProviders(defaults)
        return defaults
    }

    fun setProviders(providers: List<ApiProvider>) {
        prefs.edit().putString(KEY_PROVIDERS, ProviderRegistry.toJson(providers)).apply()
    }

    fun addProvider(provider: ApiProvider) {
        val current = getProviders().toMutableList()
        current.add(provider)
        setProviders(current)
    }

    fun removeProvider(index: Int) {
        val current = getProviders().toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            setProviders(current)
        }
    }

    companion object {
        private const val KEY_API_KEY = "dreamfield_api_key"
        private const val KEY_BASE_URL = "api_base_url"
        private const val KEY_PROVIDERS = "providers_json"
    }
}
