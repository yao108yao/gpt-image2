package com.example.gptimage2.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImageStorage(context: Context) {

    private val generatedDir = File(context.filesDir, "generated").also { it.mkdirs() }

    private val _images = MutableStateFlow<List<File>>(emptyList())

    init {
        refreshList()
    }

    fun getImagesFlow(): Flow<List<File>> = _images.asStateFlow()

    fun refreshList() {
        val files = generatedDir.listFiles()
            ?.filter { it.extension == "png" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        _images.value = files
    }

    fun createOutputFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(generatedDir, "image2_$timestamp.png")
    }

    fun deleteImage(file: File): Boolean {
        val deleted = file.delete()
        if (deleted) refreshList()
        return deleted
    }
}
