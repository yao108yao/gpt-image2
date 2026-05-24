package com.example.gptimage2.domain.model

enum class ImageSize(val label: String, val apiValue: String) {
    AUTO("自动", "auto"),
    SQUARE_1024("1024×1024", "1024x1024"),
    WIDE_1536("1536×1024", "1536x1024"),
    TALL_1536("1024×1536", "1024x1536"),
    SQUARE_2048("2048×2048", "2048x2048"),
    WIDE_2048("2048×1152", "2048x1152"),
    WIDE_4K("3840×2160", "3840x2160"),
    TALL_4K("2160×3840", "2160x3840")
}
