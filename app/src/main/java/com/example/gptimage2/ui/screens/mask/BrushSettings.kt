package com.example.gptimage2.ui.screens.mask

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BrushSettings(
    brushSize: Int,
    onBrushSizeChange: (Int) -> Unit,
    isPaintMode: Boolean,
    onPaintModeChange: (Boolean) -> Unit,
    onResetMask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("画笔:", style = MaterialTheme.typography.labelMedium)

        Slider(
            value = brushSize.toFloat(),
            onValueChange = { onBrushSizeChange(it.toInt()) },
            valueRange = 5f..100f,
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = isPaintMode,
            onClick = { onPaintModeChange(true) },
            label = { Text("涂画") }
        )

        FilterChip(
            selected = !isPaintMode,
            onClick = { onPaintModeChange(false) },
            label = { Text("擦除") }
        )

        TextButton(onClick = onResetMask) {
            Text("重置")
        }
    }
}
