package com.example.gptimage2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gptimage2.domain.model.OutputFormat

@Composable
fun OutputFormatSelector(
    selectedFormat: OutputFormat,
    onFormatSelected: (OutputFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutputFormat.entries.forEach { format ->
            FilterChip(
                selected = format == selectedFormat,
                onClick = { onFormatSelected(format) },
                label = { Text(format.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
