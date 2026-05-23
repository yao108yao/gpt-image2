package com.example.gptimage2.ui.screens.imageedit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gptimage2.data.local.ApiKeyStore
import com.example.gptimage2.di.AppModule
import com.example.gptimage2.domain.model.ImageSize
import com.example.gptimage2.util.ApiErrorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class ImageEditUiState(
    val prompt: String = "",
    val selectedSize: ImageSize = ImageSize.WIDE_1792,
    val selectedImageUris: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val resultImagePath: String? = null,
    val error: String? = null
)

class ImageEditViewModel : ViewModel() {
    private val repository = AppModule.imageRepository
    private val apiKeyStore = AppModule.apiKeyStore

    private val _state = MutableStateFlow(ImageEditUiState())
    val state = _state.asStateFlow()

    fun onPromptChange(prompt: String) {
        _state.value = _state.value.copy(prompt = prompt)
    }

    fun onSizeSelected(size: ImageSize) {
        _state.value = _state.value.copy(selectedSize = size)
    }

    fun onImagesSelected(uris: List<Uri>) {
        _state.value = _state.value.copy(selectedImageUris = _state.value.selectedImageUris + uris)
    }

    fun onImageRemoved(index: Int) {
        _state.value = _state.value.copy(
            selectedImageUris = _state.value.selectedImageUris.toMutableList().apply { removeAt(index) }
        )
    }

    fun generateImage(context: Context) {
        if (_state.value.prompt.isBlank()) return
        if (_state.value.selectedImageUris.isEmpty()) return
        if (!apiKeyStore.hasApiKey()) {
            _state.value = _state.value.copy(error = "请先在设置中配置 API Key")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, resultImagePath = null)
            try {
                val imageFiles = copyUrisToTempFiles(context, _state.value.selectedImageUris)
                val result = repository.imageToImage(
                    prompt = _state.value.prompt,
                    size = _state.value.selectedSize.apiValue,
                    imageFiles = imageFiles
                )
                if (result.isSuccess) {
                    _state.value = _state.value.copy(isLoading = false, resultImagePath = result.filePath)
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = result.errorMessage ?: "生成失败")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = ApiErrorParser.parse(e))
            }
        }
    }

    private suspend fun copyUrisToTempFiles(context: Context, uris: List<Uri>): List<File> =
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            uris.mapIndexed { index, uri ->
                val tempFile = File(context.cacheDir, "ref_image_$index.png")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                tempFile
            }
        }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
