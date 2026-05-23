package com.example.gptimage2.ui.screens.mask

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gptimage2.data.local.ApiKeyStore
import com.example.gptimage2.di.AppModule
import com.example.gptimage2.domain.model.ImageSize
import com.example.gptimage2.util.ApiErrorParser
import com.example.gptimage2.util.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class DrawMode { PAINT, ERASE }

data class MaskEditorUiState(
    val sourceBitmap: Bitmap? = null,
    val maskBitmap: Bitmap? = null,
    val maskVersion: Int = 0,
    val sourceFile: File? = null,
    val brushSize: Int = 30,
    val drawMode: DrawMode = DrawMode.PAINT,
    val prompt: String = "",
    val selectedSize: ImageSize = ImageSize.WIDE_1792,
    val isLoading: Boolean = false,
    val resultImagePath: String? = null,
    val error: String? = null
)

class MaskEditorViewModel : ViewModel() {
    private val repository = AppModule.imageRepository
    private val apiKeyStore = AppModule.apiKeyStore

    private val _state = MutableStateFlow(MaskEditorUiState())
    val state = _state.asStateFlow()

    private var lastDrawPoint: android.graphics.PointF? = null

    fun loadSourceImage(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                val tempFile = File(context.cacheDir, "mask_source_${System.currentTimeMillis()}.png")
                BitmapUtils.saveBitmapToFile(bitmap, tempFile)

                val mask = BitmapUtils.createEmptyMask(bitmap.width, bitmap.height)

                _state.value = _state.value.copy(
                    sourceBitmap = bitmap,
                    maskBitmap = mask,
                    sourceFile = tempFile
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "加载图片失败: ${e.message}")
            }
        }
    }

    fun onDraw(imageX: Float, imageY: Float) {
        val mask = _state.value.maskBitmap ?: return
        val isPaint = _state.value.drawMode == DrawMode.PAINT
        val radius = _state.value.brushSize / 2f
        BitmapUtils.drawOnMask(mask, imageX, imageY, radius, isPaint)
        lastDrawPoint = android.graphics.PointF(imageX, imageY)
        _state.value = _state.value.copy(maskVersion = _state.value.maskVersion + 1)
    }

    fun onDrawLine(startX: Float, startY: Float, endX: Float, endY: Float) {
        val mask = _state.value.maskBitmap ?: return
        val isPaint = _state.value.drawMode == DrawMode.PAINT
        val strokeWidth = _state.value.brushSize.toFloat()
        BitmapUtils.drawLineOnMask(mask, startX, startY, endX, endY, strokeWidth, isPaint)
        lastDrawPoint = android.graphics.PointF(endX, endY)
        _state.value = _state.value.copy(maskVersion = _state.value.maskVersion + 1)
    }

    fun onBrushSizeChange(size: Int) {
        _state.value = _state.value.copy(brushSize = size)
    }

    fun onDrawModeChange(isPaint: Boolean) {
        _state.value = _state.value.copy(drawMode = if (isPaint) DrawMode.PAINT else DrawMode.ERASE)
    }

    fun resetMask() {
        val source = _state.value.sourceBitmap ?: return
        val mask = BitmapUtils.createEmptyMask(source.width, source.height)
        _state.value = _state.value.copy(maskBitmap = mask)
    }

    fun onPromptChange(prompt: String) {
        _state.value = _state.value.copy(prompt = prompt)
    }

    fun onSizeSelected(size: ImageSize) {
        _state.value = _state.value.copy(selectedSize = size)
    }

    fun generateInpainting() {
        val sourceFile = _state.value.sourceFile ?: return
        val maskBitmap = _state.value.maskBitmap ?: return
        if (_state.value.prompt.isBlank()) return
        if (!apiKeyStore.hasApiKey()) {
            _state.value = _state.value.copy(error = "请先在设置中配置 API Key")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, resultImagePath = null)
            try {
                val actualMaskFile = File(
                    _state.value.sourceFile!!.parent!!,
                    "mask_${System.currentTimeMillis()}.png"
                )
                BitmapUtils.saveBitmapToFile(maskBitmap, actualMaskFile)

                val result = repository.imageToImage(
                    prompt = _state.value.prompt,
                    size = _state.value.selectedSize.apiValue,
                    imageFiles = listOf(sourceFile),
                    maskFile = actualMaskFile
                )

                if (result.isSuccess) {
                    _state.value = _state.value.copy(isLoading = false, resultImagePath = result.filePath)
                } else {
                    _state.value = _state.value.copy(isLoading = false, error = result.errorMessage ?: "生成失败")
                }

                actualMaskFile.delete()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = ApiErrorParser.parse(e))
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        _state.value.sourceBitmap?.recycle()
        _state.value.maskBitmap?.recycle()
    }
}
