package com.example.gptimage2.data.remote.api

import com.example.gptimage2.data.remote.dto.GenerationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ImageApiService {

    @POST("images/generations")
    suspend fun generateImage(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): GenerationResponse

    @POST("images/edits")
    suspend fun editImageDirect(
        @Body multipartBody: MultipartBody
    ): GenerationResponse
}
