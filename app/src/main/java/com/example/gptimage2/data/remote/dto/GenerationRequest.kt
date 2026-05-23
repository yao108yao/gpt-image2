package com.example.gptimage2.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerationRequest(
    val model: String = "gpt-image-2",
    val prompt: String,
    val size: String,
    val n: Int = 1,
    @Json(name = "response_format") val responseFormat: String = "b64_json"
)
