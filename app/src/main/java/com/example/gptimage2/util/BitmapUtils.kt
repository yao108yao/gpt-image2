package com.example.gptimage2.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object BitmapUtils {

    fun decodeBase64ToBitmap(b64: String): Bitmap {
        val bytes = Base64.decode(b64, Base64.DEFAULT)
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
    }

    fun createEmptyMask(width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun drawOnMask(
        mask: Bitmap,
        x: Float,
        y: Float,
        radius: Float,
        isPaint: Boolean
    ) {
        val canvas = Canvas(mask)
        val paint = Paint().apply {
            color = if (isPaint) android.graphics.Color.WHITE else android.graphics.Color.TRANSPARENT
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(x, y, radius, paint)
    }

    fun drawLineOnMask(
        mask: Bitmap,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        strokeWidth: Float,
        isPaint: Boolean
    ) {
        val canvas = Canvas(mask)
        val paint = Paint().apply {
            color = if (isPaint) android.graphics.Color.WHITE else android.graphics.Color.TRANSPARENT
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(startX, startY, endX, endY, paint)
    }

    fun createMaskOverlay(mask: Bitmap): Bitmap {
        val overlay = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
        val overlayColor = android.graphics.Color.argb(100, 255, 80, 80)
        for (x in 0 until mask.width) {
            for (y in 0 until mask.height) {
                if (mask.getPixel(x, y) == android.graphics.Color.WHITE) {
                    overlay.setPixel(x, y, overlayColor)
                }
            }
        }
        return overlay
    }
}
