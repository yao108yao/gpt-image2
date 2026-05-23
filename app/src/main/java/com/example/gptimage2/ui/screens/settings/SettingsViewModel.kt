package com.example.gptimage2.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gptimage2.data.local.ApiKeyStore
import com.example.gptimage2.di.AppModule
import com.example.gptimage2.domain.model.ApiProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiKey: String = "",
    val savedKey: String = "",
    val isKeySet: Boolean = false,
    val showPassword: Boolean = false,
    val saved: Boolean = false,
    val allProviders: List<ApiProvider> = emptyList(),
    val selectedProviderIndex: Int = 0,
    val showAddDialog: Boolean = false,
    val newProviderName: String = "",
    val newProviderUrl: String = ""
) {
    val currentBaseUrl: String
        get() = allProviders.getOrNull(selectedProviderIndex)?.baseUrl ?: ""
}

class SettingsViewModel : ViewModel() {
    private val apiKeyStore: ApiKeyStore = AppModule.apiKeyStore

    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    init {
        val allProviders = apiKeyStore.getProviders()
        val savedUrl = apiKeyStore.getBaseUrl()
        val index = if (savedUrl.isNotBlank()) {
            allProviders.indexOfFirst { it.baseUrl == savedUrl }.coerceAtLeast(0)
        } else 0
        val url = allProviders.getOrNull(index)?.baseUrl ?: ""
        val key = apiKeyStore.getApiKeyForProvider(url)

        _state.value = SettingsUiState(
            apiKey = key,
            savedKey = key,
            isKeySet = key.isNotBlank(),
            allProviders = allProviders,
            selectedProviderIndex = index
        )
    }

    fun onApiKeyChange(key: String) {
        _state.value = _state.value.copy(apiKey = key, saved = false)
    }

    fun togglePasswordVisibility() {
        _state.value = _state.value.copy(showPassword = !_state.value.showPassword)
    }

    fun onProviderSelected(index: Int) {
        val url = _state.value.allProviders.getOrNull(index)?.baseUrl ?: return
        val key = apiKeyStore.getApiKeyForProvider(url)
        _state.value = _state.value.copy(
            selectedProviderIndex = index,
            apiKey = key,
            savedKey = key,
            isKeySet = key.isNotBlank(),
            saved = false
        )
    }

    fun showAddDialog() {
        _state.value = _state.value.copy(showAddDialog = true, newProviderName = "", newProviderUrl = "")
    }

    fun dismissAddDialog() {
        _state.value = _state.value.copy(showAddDialog = false)
    }

    fun onNewProviderNameChange(name: String) {
        _state.value = _state.value.copy(newProviderName = name)
    }

    fun onNewProviderUrlChange(url: String) {
        _state.value = _state.value.copy(newProviderUrl = url)
    }

    fun addCustomProvider() {
        val name = _state.value.newProviderName.trim()
        var url = _state.value.newProviderUrl.trim().trimEnd('/')
        if (name.isBlank() || url.isBlank()) return
        if (!url.startsWith("http")) url = "https://$url"

        val provider = ApiProvider(name, url)
        apiKeyStore.addProvider(provider)

        val allProviders = _state.value.allProviders + provider
        _state.value = _state.value.copy(
            allProviders = allProviders,
            selectedProviderIndex = allProviders.lastIndex,
            apiKey = "",
            savedKey = "",
            isKeySet = false,
            showAddDialog = false,
            saved = false
        )
    }

    fun removeProvider(index: Int) {
        if (_state.value.allProviders.isEmpty()) return
        if (index !in _state.value.allProviders.indices) return

        apiKeyStore.removeProvider(index)

        val allProviders = _state.value.allProviders.toMutableList().apply { removeAt(index) }
        if (allProviders.isEmpty()) {
            _state.value = _state.value.copy(
                allProviders = emptyList(),
                selectedProviderIndex = 0,
                apiKey = "",
                savedKey = "",
                isKeySet = false,
                saved = false
            )
            return
        }

        val newIndex = (_state.value.selectedProviderIndex).coerceAtMost(allProviders.lastIndex)
        val url = allProviders[newIndex].baseUrl
        val key = apiKeyStore.getApiKeyForProvider(url)

        _state.value = _state.value.copy(
            allProviders = allProviders,
            selectedProviderIndex = newIndex,
            apiKey = key,
            savedKey = key,
            isKeySet = key.isNotBlank(),
            saved = false
        )
    }

    fun saveApiKey() {
        viewModelScope.launch {
            val baseUrl = _state.value.currentBaseUrl
            apiKeyStore.setApiKeyForProvider(baseUrl, _state.value.apiKey)
            apiKeyStore.setBaseUrl(baseUrl)
            _state.value = _state.value.copy(
                saved = true,
                savedKey = _state.value.apiKey,
                isKeySet = _state.value.apiKey.isNotBlank()
            )
        }
    }
}
