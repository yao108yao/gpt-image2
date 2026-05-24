package com.example.gptimage2.domain.model

import android.graphics.Bitmap

enum class OutputFormat(val label: String, val apiValue: String, val extension: String, val compressFormat: Bitmap.CompressFormat) {
    PNG("PNG", "png", "png", Bitmap.CompressFormat.PNG),
    JPEG("JPEG", "jpeg", "jpg", Bitmap.CompressFormat.JPEG),
    WEBP("WebP", "webp", "webp", Bitmap.CompressFormat.WEBP)
}
