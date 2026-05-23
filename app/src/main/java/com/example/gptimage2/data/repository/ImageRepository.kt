package com.example.gptimage2.data.repository

import com.example.gptimage2.data.local.ApiKeyStore
import com.example.gptimage2.data.local.ImageStorage
import com.example.gptimage2.data.remote.dto.GenerationResponse
import com.example.gptimage2.domain.model.GenerationResult
import com.example.gptimage2.util.BitmapUtils
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import kotlin.math.pow

class ImageRepository(
    private val apiKeyStore: ApiKeyStore,
    private val imageStorage: ImageStorage,
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) {

    private val retryableStatuses = setOf(408, 429, 500, 502, 503, 504)

    private val generationsUrl = "https://www.dreamfield.top/v1/images/generations"
    private val editsUrl = "https://www.dreamfield.top/v1/images/edits"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val imageMediaType = "image/png".toMediaType()

    private val responseAdapter by lazy { moshi.adapter(GenerationResponse::class.java) }

    suspend fun textToImage(
        prompt: String,
        size: String,
        n: Int = 1
    ): GenerationResult = withContext(Dispatchers.IO) {
        val apiKey = apiKeyStore.getApiKeySync()
        if (apiKey.isBlank()) {
            return@withContext GenerationResult(filePath = "", isSuccess = false, errorMessage = "未设置 API Key")
        }

        val jsonBody = """{"model":"gpt-image-2","prompt":${moshi.adapter(String::class.java).toJson(prompt)},"size":"$size","n":$n,"response_format":"b64_json"}"""

        val request = Request.Builder()
            .url(generationsUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        val response = retryWithBackoff {
            executeAndParse(request)
        }

        handleResponse(response)
    }

    suspend fun imageToImage(
        prompt: String,
        size: String,
        imageFiles: List<File>,
        maskFile: File? = null,
        n: Int = 1
    ): GenerationResult = withContext(Dispatchers.IO) {
        val apiKey = apiKeyStore.getApiKeySync()
        if (apiKey.isBlank()) {
            return@withContext GenerationResult(filePath = "", isSuccess = false, errorMessage = "未设置 API Key")
        }

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", "gpt-image-2")
            .addFormDataPart("prompt", prompt)
            .addFormDataPart("size", size)
            .addFormDataPart("n", n.toString())
            .addFormDataPart("response_format", "b64_json")
            .apply {
                imageFiles.forEach { file ->
                    addFormDataPart("image[]", file.name, file.asRequestBody(imageMediaType))
                }
                maskFile?.let {
                    addFormDataPart("mask", it.name, it.asRequestBody(imageMediaType))
                }
            }
            .build()

        val request = Request.Builder()
            .url(editsUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(multipartBody)
            .build()

        val response = retryWithBackoff {
            executeAndParse(request)
        }

        handleResponse(response)
    }

    private fun executeAndParse(request: Request): GenerationResponse {
        val response = okHttpClient.newCall(request).execute()
        val body = response.body?.string() ?: ""
        if (!response.isSuccessful) {
            throw ApiException(response.code, body)
        }
        return responseAdapter.fromJson(body) ?: throw RuntimeException("Failed to parse response")
    }

    fun getGeneratedImages() = imageStorage.getImagesFlow()

    fun deleteImage(file: File): Boolean = imageStorage.deleteImage(file)

    fun refreshGallery() = imageStorage.refreshList()

    private suspend fun handleResponse(response: GenerationResponse): GenerationResult {
        val data = response.data
        if (data.isEmpty()) {
            return GenerationResult(filePath = "", isSuccess = false, errorMessage = "No image data returned")
        }

        val item = data.first()
        val outputFile = imageStorage.createOutputFile()

        if (!item.b64Json.isNullOrBlank()) {
            val bitmap = BitmapUtils.decodeBase64ToBitmap(item.b64Json)
            BitmapUtils.saveBitmapToFile(bitmap, outputFile)
        } else if (!item.url.isNullOrBlank()) {
            return GenerationResult(filePath = "", isSuccess = false, errorMessage = "URL response not supported")
        } else {
            return GenerationResult(filePath = "", isSuccess = false, errorMessage = "Response contains neither b64_json nor url")
        }

        imageStorage.refreshList()

        return GenerationResult(
            filePath = outputFile.absolutePath,
            revisedPrompt = item.revisedPrompt,
            isSuccess = true
        )
    }

    private suspend fun <T> retryWithBackoff(maxRetries: Int = 2, block: () -> T): T {
        var attempt = 0
        while (true) {
            try {
                return block()
            } catch (e: ApiException) {
                if (attempt >= maxRetries || e.code !in retryableStatuses) throw e
                delay(minOf(2.0.pow(attempt).toLong() * 1000, 8000))
                attempt++
            } catch (e: java.io.IOException) {
                if (attempt >= maxRetries) throw e
                delay(minOf(2.0.pow(attempt).toLong() * 1000, 8000))
                attempt++
            }
        }
    }

    class ApiException(val code: Int, val errorBody: String) : Exception("HTTP $code: $errorBody")
}
