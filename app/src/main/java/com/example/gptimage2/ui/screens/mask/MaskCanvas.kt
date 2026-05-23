package com.example.gptimage2.ui.screens.mask

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize

@Composable
fun MaskCanvas(
    sourceBitmap: Bitmap?,
    maskBitmap: Bitmap?,
    maskVersion: Int,
    brushSize: Int,
    isPaintMode: Boolean,
    onDraw: (imageX: Float, imageY: Float) -> Unit,
    onDrawLine: (startX: Float, startY: Float, endX: Float, endY: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sourceBitmap == null) return

    var pointerPosition by remember { mutableStateOf<Offset?>(null) }
    var lastDrawPoint by remember { mutableStateOf<Offset?>(null) }

    val maskOverlay = remember(maskBitmap, maskVersion) {
        maskBitmap?.let { createMaskOverlay(it) }
    }

    val imageAspect = sourceBitmap.width.toFloat() / sourceBitmap.height.toFloat()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .aspectRatio(imageAspect)
                .align(Alignment.Center)
                .pointerInput(sourceBitmap, maskBitmap, brushSize, isPaintMode) {
                    val imageWidth = sourceBitmap.width.toFloat()
                    val imageHeight = sourceBitmap.height.toFloat()

                    detectDragGestures(
                        onDragStart = { offset ->
                            val mapped = mapCanvasToImage(offset, size, imageWidth, imageHeight)
                            onDraw(mapped.x, mapped.y)
                            lastDrawPoint = mapped
                            pointerPosition = offset
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val mapped = mapCanvasToImage(change.position, size, imageWidth, imageHeight)
                            lastDrawPoint?.let { last ->
                                onDrawLine(last.x, last.y, mapped.x, mapped.y)
                            }
                            lastDrawPoint = mapped
                            pointerPosition = change.position
                        },
                        onDragEnd = {
                            lastDrawPoint = null
                            pointerPosition = null
                        },
                        onDragCancel = {
                            lastDrawPoint = null
                            pointerPosition = null
                        }
                    )
                }
        ) {
            val w = size.width.toInt()
            val h = size.height.toInt()

            drawImage(
                image = sourceBitmap.asImageBitmap(),
                dstSize = IntSize(w, h)
            )

            maskOverlay?.let { overlay ->
                drawImage(
                    image = overlay.asImageBitmap(),
                    dstSize = IntSize(w, h)
                )
            }

            pointerPosition?.let { pos ->
                val scale = w.toFloat() / sourceBitmap.width.toFloat()
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = brushSize.toFloat() / 2f * scale,
                    center = pos
                )
            }
        }
    }
}

private fun mapCanvasToImage(
    canvasOffset: Offset,
    canvasSize: IntSize,
    imageWidth: Float,
    imageHeight: Float
): Offset {
    val scaleX = imageWidth / canvasSize.width.toFloat()
    val scaleY = imageHeight / canvasSize.height.toFloat()
    return Offset(canvasOffset.x * scaleX, canvasOffset.y * scaleY)
}

private fun createMaskOverlay(mask: Bitmap): Bitmap {
    val overlay = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
    val overlayColor = android.graphics.Color.argb(100, 255, 80, 80)
    val white = android.graphics.Color.WHITE
    for (x in 0 until mask.width) {
        for (y in 0 until mask.height) {
            if (mask.getPixel(x, y) == white) {
                overlay.setPixel(x, y, overlayColor)
            }
        }
    }
    return overlay
}
