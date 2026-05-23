package com.example.gptimage2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.gptimage2.ui.navigation.AppNavigation
import com.example.gptimage2.ui.theme.GptImage2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GptImage2Theme {
                AppNavigation()
            }
        }
    }
}
