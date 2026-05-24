package com.example.gptimage2.ui.screens.mask

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.gptimage2.ui.components.GenerateButton
import com.example.gptimage2.ui.components.PromptInput
import com.example.gptimage2.ui.components.ProviderSelector
import com.example.gptimage2.ui.components.QualitySelector
import com.example.gptimage2.ui.components.OutputFormatSelector
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaskEditorScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: MaskEditorViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

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

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.loadSourceImage(context, it) }
    }

    if (state.isFullscreen && state.sourceBitmap != null) {
        // Fullscreen mask editing mode - use Dialog to cover everything including bottom nav
        Dialog(
            onDismissRequest = viewModel::exitFullscreen,
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                MaskCanvas(
                    sourceBitmap = state.sourceBitmap,
                    maskBitmap = state.maskBitmap,
                    maskVersion = state.maskVersion,
                    brushSize = state.brushSize,
                    isPaintMode = state.drawMode == DrawMode.PAINT,
                    onDraw = viewModel::onDraw,
                    onDrawLine = viewModel::onDrawLine,
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom toolbar overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BrushSettings(
                        brushSize = state.brushSize,
                        onBrushSizeChange = viewModel::onBrushSizeChange,
                        isPaintMode = state.drawMode == DrawMode.PAINT,
                        onPaintModeChange = viewModel::onDrawModeChange,
                        onResetMask = viewModel::resetMask,
                        showResetButton = false,
                        showSizeLabel = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = viewModel::cancelFullscreen,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("取消")
                        }
                        OutlinedButton(
                            onClick = viewModel::resetMask,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("重置")
                        }
                        Button(
                            onClick = viewModel::exitFullscreen,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("完成")
                        }
                    }
                }
            }
        }
        return
    }

    // Normal mode
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("局部重绘") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.sourceBitmap == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text("选择源图片")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Thumbnail preview - click to enter fullscreen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(200.dp)
                        .clipToBounds()
                        .clickable { viewModel.enterFullscreen() }
                ) {
                    MaskCanvas(
                        sourceBitmap = state.sourceBitmap,
                        maskBitmap = state.maskBitmap,
                        maskVersion = state.maskVersion,
                        brushSize = state.brushSize,
                        isPaintMode = state.drawMode == DrawMode.PAINT,
                        onDraw = { _, _ -> },
                        onDrawLine = { _, _, _, _ -> },
                        modifier = Modifier.fillMaxWidth(),
                        interactive = false
                    )

                    // Re-pick image button
                    SmallFloatingActionButton(
                        onClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "重新选择图片",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PromptInput(
                        prompt = state.prompt,
                        onPromptChange = viewModel::onPromptChange,
                        enabled = !state.isLoading,
                        maxLines = 2
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
                        onClick = viewModel::generateInpainting,
                        isLoading = state.isLoading,
                        enabled = state.sourceBitmap != null && state.prompt.isNotBlank(),
                        text = "生成重绘"
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
                }

                state.resultImagePath?.let { path ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onClick = { onNavigateToDetail(path) }
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Inpainted image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}
