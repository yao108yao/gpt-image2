package com.example.gptimage2.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gptimage2.data.local.ApiKeyStore
import com.example.gptimage2.di.AppModule
import com.example.gptimage2.domain.model.ImageSize
import com.example.gptimage2.util.ApiErrorParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val prompt: String = "",
    val selectedSize: ImageSize = ImageSize.WIDE_1792,
    val isLoading: Boolean = false,
    val resultImagePath: String? = null,
    val error: String? = null,
    val hasApiKey: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val repository = AppModule.imageRepository
    private val apiKeyStore = AppModule.apiKeyStore

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        _state.value = _state.value.copy(hasApiKey = apiKeyStore.hasApiKey())
    }

    fun onPromptChange(prompt: String) {
        _state.value = _state.value.copy(prompt = prompt)
    }

    fun onSizeSelected(size: ImageSize) {
        _state.value = _state.value.copy(selectedSize = size)
    }

    fun generateImage() {
        if (_state.value.prompt.isBlank()) return
        if (!apiKeyStore.hasApiKey()) {
            _state.value = _state.value.copy(error = "请先在设置中配置 API Key")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, resultImagePath = null)
            try {
                val result = repository.textToImage(
                    prompt = _state.value.prompt,
                    size = _state.value.selectedSize.apiValue
                )
                if (result.isSuccess) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        resultImagePath = result.filePath
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.errorMessage ?: "生成失败"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = ApiErrorParser.parse(e)
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
