package com.example.gptimage2.domain.model

enum class ImageSize(val label: String, val apiValue: String) {
    SQUARE_1024("1024 × 1024", "1024x1024"),
    WIDE_1792("1792 × 1024", "1792x1024"),
    TALL_1024("1024 × 1792", "1024x1792")
}
