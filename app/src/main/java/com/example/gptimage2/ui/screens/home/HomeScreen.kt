package com.example.gptimage2.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.gptimage2.ui.components.GenerateButton
import com.example.gptimage2.ui.components.PromptInput
import com.example.gptimage2.ui.components.ProviderSelector
import com.example.gptimage2.ui.components.QualitySelector
import com.example.gptimage2.ui.components.OutputFormatSelector
import com.example.gptimage2.ui.components.SizeSelector
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.reloadProviders()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文生图") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            PromptInput(
                prompt = state.prompt,
                onPromptChange = viewModel::onPromptChange,
                enabled = !state.isLoading
            )

            SizeSelector(
                selectedSize = state.selectedSize,
                onSizeSelected = viewModel::onSizeSelected
            )

            QualitySelector(
                selectedQuality = state.quality,
                onQualitySelected = viewModel::onQualitySelected
            )

            OutputFormatSelector(
                selectedFormat = state.outputFormat,
                onFormatSelected = viewModel::onOutputFormatSelected
            )

            ProviderSelector(
                providers = state.providers,
                selectedIndex = state.selectedProviderIndex,
                onProviderSelected = viewModel::onProviderSelected
            )

            GenerateButton(
                onClick = viewModel::generateImage,
                isLoading = state.isLoading,
                enabled = state.prompt.isNotBlank()
            )

            state.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = viewModel::clearError) {
                            Text("关闭")
                        }
                    }
                }
            }

            state.resultImagePath?.let { path ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigateToDetail(path) }
                ) {
                    AsyncImage(
                        model = File(path),
                        contentDescription = "Generated image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
