package com.example.gptimage2.ui.screens.gallery

import androidx.lifecycle.ViewModel
import com.example.gptimage2.data.repository.ImageRepository
import com.example.gptimage2.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class GalleryUiState(
    val images: List<File> = emptyList()
)

class GalleryViewModel : ViewModel() {
    private val repository: ImageRepository = AppModule.imageRepository

    private val _state = MutableStateFlow(GalleryUiState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        repository.refreshGallery()
    }

    fun deleteImage(file: File): Boolean {
        return repository.deleteImage(file)
    }
}
