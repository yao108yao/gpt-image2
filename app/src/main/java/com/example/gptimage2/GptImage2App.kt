package com.example.gptimage2

import android.app.Application
import com.example.gptimage2.di.AppModule

class GptImage2App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
    }
}
