package com.example.gptimage2.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PromptInput(
    prompt: String,
    onPromptChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxLines: Int = 6
) {
    OutlinedTextField(
        value = prompt,
        onValueChange = onPromptChange,
        label = { Text("Prompt") },
        placeholder = { Text("Describe the image you want to generate...") },
        modifier = modifier.fillMaxWidth(),
        maxLines = maxLines,
        minLines = minOf(3, maxLines),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}
