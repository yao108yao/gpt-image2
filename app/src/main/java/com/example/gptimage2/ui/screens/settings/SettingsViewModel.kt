package com.example.gptimage2.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gptimage2.data.local.ApiKeyStore
import com.example.gptimage2.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiKey: String = "",
    val savedKey: String = "",
    val isKeySet: Boolean = false,
    val showPassword: Boolean = false,
    val saved: Boolean = false
)

class SettingsViewModel : ViewModel() {
    private val apiKeyStore: ApiKeyStore = AppModule.apiKeyStore

    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    init {
        val key = apiKeyStore.getApiKeySync()
        _state.value = SettingsUiState(
            apiKey = key,
            savedKey = key,
            isKeySet = key.isNotBlank()
        )
    }

    fun onApiKeyChange(key: String) {
        _state.value = _state.value.copy(apiKey = key, saved = false)
    }

    fun togglePasswordVisibility() {
        _state.value = _state.value.copy(showPassword = !_state.value.showPassword)
    }

    fun saveApiKey() {
        viewModelScope.launch {
            apiKeyStore.setApiKey(_state.value.apiKey)
            _state.value = _state.value.copy(
                saved = true,
                savedKey = _state.value.apiKey,
                isKeySet = _state.value.apiKey.isNotBlank()
            )
        }
    }
}
