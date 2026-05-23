package com.example.gptimage2.domain.model

import android.graphics.Bitmap

enum class ImageSize(val label: String, val apiValue: String) {
    SQUARE_1024("1024 × 1024", "1024x1024"),
    WIDE_1792("1792 × 1024", "1792x1024"),
    TALL_1024("1024 × 1792", "1024x1792");

    companion object {
        fun fromBitmap(bitmap: Bitmap): ImageSize {
            val w = bitmap.width
            val h = bitmap.height
            val aspect = w.toFloat() / h.toFloat()
            return when {
                aspect > 1.3f -> WIDE_1792
                aspect < 0.77f -> TALL_1024
                else -> SQUARE_1024
            }
        }
    }
}
