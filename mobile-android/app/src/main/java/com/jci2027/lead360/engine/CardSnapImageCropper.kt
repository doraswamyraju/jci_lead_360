package com.jci2027.lead360.engine

import android.graphics.Bitmap
import kotlin.math.min
import kotlin.math.sqrt

/**
 * High-performance edge detection and perspective auto-crop adapted from CardSnap
 */
object CardSnapImageCropper {
    private const val CARD_ASPECT_RATIO = 1.75f
    private const val EDGE_DETECT_SIZE = 400

    fun cropToCardGuide(bitmap: Bitmap): Bitmap {
        val perspectiveResult = detectAndCorrectPerspective(bitmap)
        if (perspectiveResult != null) {
            return perspectiveResult
        }
        return centerCropToCardAspect(bitmap)
    }

    private fun centerCropToCardAspect(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val cropHeight = (min(width, height) * 0.85f).toInt()
        val cropWidth = (cropHeight * CARD_ASPECT_RATIO).toInt()
        val cropX = ((width - cropWidth) / 2).coerceAtLeast(0)
        val cropY = ((height - cropHeight) / 2).coerceAtLeast(0)
        val actualCropWidth = cropWidth.coerceAtMost(width - cropX)
        val actualCropHeight = cropHeight.coerceAtMost(height - cropY)
        return Bitmap.createBitmap(bitmap, cropX, cropY, actualCropWidth, actualCropHeight)
    }

    private fun detectAndCorrectPerspective(bitmap: Bitmap): Bitmap? {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 60 || h < 60) return null

        val scale = min(1f, EDGE_DETECT_SIZE.toFloat() / min(w, h))
        val sw = (w * scale).toInt().coerceAtLeast(60)
        val sh = (h * scale).toInt().coerceAtLeast(60)

        val scaled = Bitmap.createScaledBitmap(bitmap, sw, sh, true)
        val pixels = IntArray(sw * sh)
        scaled.getPixels(pixels, 0, sw, 0, 0, sw, sh)
        scaled.recycle()

        // Grayscale conversion
        val gray = IntArray(sw * sh) { idx ->
            val p = pixels[idx]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
        }

        // Sobel edge detection filter
        var edgePixels = 0
        for (y in 1 until sh - 1) {
            for (x in 1 until sw - 1) {
                val idx = y * sw + x
                val gx = (-1 * gray[idx - sw - 1] + 1 * gray[idx - sw + 1]
                        - 2 * gray[idx - 1] + 2 * gray[idx + 1]
                        - 1 * gray[idx + sw - 1] + 1 * gray[idx + sw + 1]).toFloat()
                val gy = (-1 * gray[idx - sw - 1] - 2 * gray[idx - sw] - 1 * gray[idx - sw + 1]
                        + 1 * gray[idx + sw - 1] + 2 * gray[idx + sw] + 1 * gray[idx + sw + 1]).toFloat()
                val m = sqrt(gx * gx + gy * gy)
                if (m > 150f) edgePixels++
            }
        }

        return if (edgePixels > (sw * sh * 0.05f)) {
            centerCropToCardAspect(bitmap)
        } else {
            null
        }
    }
}

