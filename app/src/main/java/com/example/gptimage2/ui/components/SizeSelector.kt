package com.example.gptimage2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gptimage2.domain.model.ImageSize

@Composable
fun SizeSelector(
    selectedSize: ImageSize,
    onSizeSelected: (ImageSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ImageSize.entries.forEach { size ->
            FilterChip(
                selected = size == selectedSize,
                onClick = { onSizeSelected(size) },
                label = { Text(size.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
