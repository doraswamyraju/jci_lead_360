package com.jci2027.lead360.engine

import android.content.Context
import android.graphics.*
import com.jci2027.lead360.model.TemplateLayer
import com.jci2027.lead360.model.TemplateModel
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Native Android High-Res Dynamic Flyer Compositor
 * Accurately translates all Super Admin template coordinates, typography,
 * text alignment, colors, circular avatars, and photo borders.
 */
class AndroidFlyerRenderer(private val context: Context) {

    fun compositeDynamicFlyer(
        template: TemplateModel,
        baseBitmap: Bitmap,
        chapterLogoBitmap: Bitmap?,
        presidentPhotoBitmap: Bitmap?,
        fieldValues: Map<String, String>
    ): Bitmap {
        val width = template.dimensions?.width ?: baseBitmap.width
        val height = template.dimensions?.height ?: baseBitmap.height

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // 1. Draw Base Flyer
        val baseRect = Rect(0, 0, width, height)
        canvas.drawBitmap(baseBitmap, null, baseRect, null)

        // 2. Iterate through configured layers
        template.layers?.forEach { layer ->
            val zoneType = (layer.type ?: layer.key ?: "custom_text").lowercase()

            // Support both percentage (0-100) and absolute pixel coordinates from backend
            val x = if (layer.xPercent > 0f) (layer.xPercent / 100f) * width else layer.x
            val y = if (layer.yPercent > 0f) (layer.yPercent / 100f) * height else layer.y
            val w = if (layer.wPercent > 0f) (layer.wPercent / 100f) * width else (if (layer.width > 0f) layer.width else 200f)
            val h = if (layer.hPercent > 0f) (layer.hPercent / 100f) * height else (if (layer.height > 0f) layer.height else 80f)

            val shape = layer.shape ?: if (zoneType.contains("photo")) "circle" else "rectangle"
            val borderWidth = layer.borderWidth ?: if (zoneType.contains("photo")) 4 else 0
            val borderColorInt = parseHexColor(layer.borderColor, Color.WHITE)

            when {
                // CHAPTER LOGO / CO-HOST LOGO
                zoneType.contains("logo") -> {
                    chapterLogoBitmap?.let { logo ->
                        if (shape == "circle") {
                            drawCircleBitmap(canvas, logo, x, y, min(w, h), borderWidth, borderColorInt)
                        } else {
                            val destRect = RectF(x, y, x + w, y + h)
                            canvas.drawBitmap(logo, null, destRect, null)
                        }
                    }
                }

                // PRESIDENT PHOTO / SECRETARY PHOTO / SPEAKER PHOTO
                zoneType.contains("photo") || zoneType.contains("president_photo") || zoneType.contains("speaker") -> {
                    presidentPhotoBitmap?.let { photo ->
                        if (shape == "circle") {
                            drawCircleBitmap(canvas, photo, x, y, min(w, h), borderWidth, borderColorInt)
                        } else {
                            val destRect = RectF(x, y, x + w, y + h)
                            canvas.drawBitmap(photo, null, destRect, null)
                            if (borderWidth > 0) {
                                val strokePaint = Paint().apply {
                                    style = Paint.Style.STROKE
                                    strokeWidth = borderWidth.toFloat()
                                    color = borderColorInt
                                    isAntiAlias = true
                                }
                                canvas.drawRect(destRect, strokePaint)
                            }
                        }
                    }
                }

                // TEXT FIELDS
                else -> {
                    val text = fieldValues[zoneType] ?: layer.label ?: ""
                    if (text.isNotBlank()) {
                        drawWrappedText(
                            canvas = canvas,
                            text = text,
                            x = x,
                            y = y,
                            maxWidth = w,
                            maxHeight = h,
                            textColor = parseHexColor(layer.textColor, Color.WHITE),
                            fontSize = (layer.fontSize ?: 24).toFloat() * 1.5f,
                            align = layer.textAlign ?: "left",
                            hasShadow = layer.hasShadow != false
                        )
                    }
                }
            }
        }

        return resultBitmap
    }

    private fun drawCircleBitmap(canvas: Canvas, bitmap: Bitmap, x: Float, y: Float, size: Float, borderWidth: Int, borderColor: Int) {
        val radius = size / 2f
        val cx = x + radius
        val cy = y + radius

        val output = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
        val circleCanvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }

        circleCanvas.drawCircle(radius, radius, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        circleCanvas.drawBitmap(bitmap, null, RectF(0f, 0f, size, size), paint)

        canvas.drawBitmap(output, x, y, null)

        if (borderWidth > 0) {
            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = borderWidth.toFloat()
                color = borderColor
                isAntiAlias = true
            }
            canvas.drawCircle(cx, cy, radius, borderPaint)
        }
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        maxHeight: Float,
        textColor: Int,
        fontSize: Float,
        align: String,
        hasShadow: Boolean
    ) {
        val paint = Paint().apply {
            color = textColor
            textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            if (hasShadow) {
                setShadowLayer(6f, 2f, 2f, Color.argb(200, 0, 0, 0))
            }
            textAlign = when (align.lowercase()) {
                "center" -> Paint.Align.CENTER
                "right" -> Paint.Align.RIGHT
                else -> Paint.Align.LEFT
            }
        }

        val anchorX = when (align.lowercase()) {
            "center" -> x + (maxWidth / 2f)
            "right" -> x + maxWidth
            else -> x
        }

        val words = text.split(" ")
        var line = ""
        var currentY = y + fontSize
        val lineHeight = fontSize * 1.3f

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line, anchorX, currentY, paint)
                line = word
                currentY += lineHeight
                if (currentY > y + maxHeight) break
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty() && currentY <= y + maxHeight + lineHeight) {
            canvas.drawText(line, anchorX, currentY, paint)
        }
    }

    private fun parseHexColor(hex: String?, fallback: Int): Int {
        if (hex.isNullOrBlank()) return fallback
        return try {
            Color.parseColor(hex)
        } catch (_: Exception) {
            fallback
        }
    }

    fun saveBitmapToCache(bitmap: Bitmap, filename: String = "jci_flyer.png"): File {
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}
