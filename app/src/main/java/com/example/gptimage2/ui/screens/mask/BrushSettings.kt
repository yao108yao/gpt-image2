package com.example.gptimage2.ui.screens.mask

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BrushSettings(
    brushSize: Int,
    onBrushSizeChange: (Int) -> Unit,
    isPaintMode: Boolean,
    onPaintModeChange: (Boolean) -> Unit,
    onResetMask: () -> Unit,
    modifier: Modifier = Modifier,
    showResetButton: Boolean = true,
    showSizeLabel: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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

        // Circular brush preview
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            val previewSize = (brushSize.toFloat() / 100f * 30f + 6f).dp
            Box(
                modifier = Modifier
                    .size(previewSize)
                    .clip(CircleShape)
                    .background(Color(255, 80, 80, 160))
            )
        }

        Slider(
            value = brushSize.toFloat(),
            onValueChange = { onBrushSizeChange(it.toInt()) },
            valueRange = 5f..100f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        if (showResetButton) {
            TextButton(onClick = onResetMask) {
                Text("重置")
            }
        }
    }
}
