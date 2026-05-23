package com.example.gptimage2.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiError(
    val error: ErrorDetail? = null
)

@JsonClass(generateAdapter = true)
data class ErrorDetail(
    val type: String? = null,
    val code: String? = null,
    val message: String? = null
)
