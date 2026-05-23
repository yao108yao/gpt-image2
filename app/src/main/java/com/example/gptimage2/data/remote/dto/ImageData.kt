package com.example.gptimage2.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageData(
    @Json(name = "b64_json") val b64Json: String? = null,
    val url: String? = null,
    @Json(name = "revised_prompt") val revisedPrompt: String? = null
)
