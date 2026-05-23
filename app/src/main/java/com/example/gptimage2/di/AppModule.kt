package com.example.gptimage2.di

import com.example.gptimage2.GptImage2App
import com.example.gptimage2.data.local.ApiKeyStore
import com.example.gptimage2.data.local.ImageStorage
import com.example.gptimage2.data.remote.interceptor.AuthInterceptor
import com.example.gptimage2.data.repository.ImageRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object AppModule {

    private val app: GptImage2App by lazy {
        _app ?: throw IllegalStateException("AppModule not initialized. Call init() in Application.onCreate()")
    }
    private var _app: GptImage2App? = null

    fun init(app: GptImage2App) {
        _app = app
    }

    val apiKeyStore: ApiKeyStore by lazy {
        ApiKeyStore(app)
    }

    val imageStorage: ImageStorage by lazy {
        ImageStorage(app)
    }

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(apiKeyStore)
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()
    }

    val imageRepository: ImageRepository by lazy {
        ImageRepository(apiKeyStore, imageStorage, okHttpClient, moshi)
    }
}
