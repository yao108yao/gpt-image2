package com.example.gptimage2.domain.model

data class GenerationResult(
    val filePath: String,
    val revisedPrompt: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)
