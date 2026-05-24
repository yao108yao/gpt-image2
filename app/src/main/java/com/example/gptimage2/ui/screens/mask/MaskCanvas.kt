package com.example.gptimage2.ui.screens.mask

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
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
    modifier: Modifier = Modifier,
    interactive: Boolean = true
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
                .fillMaxSize()
                .align(Alignment.Center)
                .then(
                    if (interactive) Modifier.pointerInput(sourceBitmap, maskBitmap, brushSize, isPaintMode) {
                    val imageWidth = sourceBitmap.width.toFloat()
                    val imageHeight = sourceBitmap.height.toFloat()

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val mapped = mapCanvasToImage(down.position, size, imageWidth, imageHeight)
                        onDraw(mapped.x, mapped.y)
                        lastDrawPoint = mapped
                        pointerPosition = down.position

                        val slop = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                            change.consume()
                        }

                        if (slop != null) {
                            drag(down.id) { change ->
                                change.consume()
                                val mappedDrag = mapCanvasToImage(change.position, size, imageWidth, imageHeight)
                                lastDrawPoint?.let { last ->
                                    onDrawLine(last.x, last.y, mappedDrag.x, mappedDrag.y)
                                }
                                lastDrawPoint = mappedDrag
                                pointerPosition = change.position
                            }
                        }

                        lastDrawPoint = null
                        pointerPosition = null
                    }
                } else Modifier
                )
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
    val w = mask.width
    val h = mask.height
    val overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val srcPixels = IntArray(w * h)
    val dstPixels = IntArray(w * h)
    mask.getPixels(srcPixels, 0, w, 0, 0, w, h)
    val overlayColor = android.graphics.Color.argb(100, 255, 80, 80)
    val white = android.graphics.Color.WHITE
    for (i in srcPixels.indices) {
        dstPixels[i] = if (srcPixels[i] == white) overlayColor else 0
    }
    overlay.setPixels(dstPixels, 0, w, 0, 0, w, h)
    return overlay
}
