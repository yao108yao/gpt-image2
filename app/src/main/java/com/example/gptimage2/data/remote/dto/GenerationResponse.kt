package com.example.gptimage2.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerationResponse(
    val created: Long = 0,
    val data: List<ImageData> = emptyList()
)
