package com.example.gptimage2.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
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
            style = Paint.Style.FILL
            isAntiAlias = true
            if (isPaint) {
                color = android.graphics.Color.WHITE
            } else {
                color = android.graphics.Color.TRANSPARENT
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
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
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            if (isPaint) {
                color = android.graphics.Color.WHITE
            } else {
                color = android.graphics.Color.TRANSPARENT
                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }
        }
        canvas.drawLine(startX, startY, endX, endY, paint)
    }

    fun createMaskOverlay(mask: Bitmap): Bitmap {
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

    fun invertMaskForApi(mask: Bitmap): Bitmap {
        val w = mask.width
        val h = mask.height
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val srcPixels = IntArray(w * h)
        val dstPixels = IntArray(w * h)
        mask.getPixels(srcPixels, 0, w, 0, 0, w, h)
        val black = android.graphics.Color.BLACK
        for (i in srcPixels.indices) {
            val alpha = (srcPixels[i] ushr 24) and 0xFF
            dstPixels[i] = if (alpha > 0) 0 else black
        }
        result.setPixels(dstPixels, 0, w, 0, 0, w, h)
        return result
    }
}
