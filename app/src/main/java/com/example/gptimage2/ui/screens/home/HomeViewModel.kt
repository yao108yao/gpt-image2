package com.example.gptimage2.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gptimage2.data.local.ApiKeyStore
import com.example.gptimage2.di.AppModule
import com.example.gptimage2.domain.model.ImageSize
import com.example.gptimage2.domain.model.Quality
import com.example.gptimage2.domain.model.OutputFormat
import com.example.gptimage2.domain.model.ApiProvider
import com.example.gptimage2.util.ApiErrorParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val prompt: String = "",
    val selectedSize: ImageSize = ImageSize.AUTO,
    val quality: Quality = Quality.AUTO,
    val outputFormat: OutputFormat = OutputFormat.PNG,
    val isLoading: Boolean = false,
    val resultImagePath: String? = null,
    val error: String? = null,
    val hasApiKey: Boolean = false,
    val providers: List<ApiProvider> = emptyList(),
    val selectedProviderIndex: Int = 0
)

class HomeViewModel : ViewModel() {
    private val repository = AppModule.imageRepository
    private val apiKeyStore = AppModule.apiKeyStore

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    init {
        _state.value = _state.value.copy(hasApiKey = apiKeyStore.hasApiKey())
        loadProviders()
    }

    private fun loadProviders() {
        val all = apiKeyStore.getProviders()
        val baseUrl = apiKeyStore.getBaseUrl()
        val index = if (baseUrl.isNotBlank()) {
            all.indexOfFirst { it.baseUrl == baseUrl }.coerceAtLeast(0)
        } else 0
        _state.value = _state.value.copy(providers = all, selectedProviderIndex = index)
    }

    fun reloadProviders() = loadProviders()

    fun onProviderSelected(index: Int) {
        val provider = _state.value.providers.getOrNull(index) ?: return
        apiKeyStore.setBaseUrl(provider.baseUrl)
        _state.value = _state.value.copy(selectedProviderIndex = index)
    }

    fun onPromptChange(prompt: String) {
        _state.value = _state.value.copy(prompt = prompt)
    }

    fun onSizeSelected(size: ImageSize) {
        _state.value = _state.value.copy(selectedSize = size)
    }

    fun onQualitySelected(quality: Quality) {
        _state.value = _state.value.copy(quality = quality)
    }

    fun onOutputFormatSelected(format: OutputFormat) {
        _state.value = _state.value.copy(outputFormat = format)
    }

    fun generateImage() {
        if (_state.value.prompt.isBlank()) return
        val baseUrl = apiKeyStore.getBaseUrl()
        if (apiKeyStore.getApiKeyForProvider(baseUrl).isBlank()) {
            _state.value = _state.value.copy(error = "请先在设置中配置 API Key")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, resultImagePath = null)
            try {
                val selectedSize = _state.value.selectedSize
                val sizeValue = selectedSize.apiValue
                val qualityValue = _state.value.quality
                val qualityParam = if (qualityValue == Quality.AUTO) null else qualityValue.apiValue
                val outputFormat = _state.value.outputFormat
                val formatParam = if (outputFormat == OutputFormat.PNG) null else outputFormat.apiValue
                val result = repository.textToImage(
                    prompt = _state.value.prompt,
                    size = sizeValue
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
