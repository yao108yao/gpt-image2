package com.example.gptimage2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gptimage2.domain.model.Quality

@Composable
fun QualitySelector(
    selectedQuality: Quality,
    onQualitySelected: (Quality) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Quality.entries.forEach { quality ->
            FilterChip(
                selected = quality == selectedQuality,
                onClick = { onQualitySelected(quality) },
                label = { Text(quality.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
