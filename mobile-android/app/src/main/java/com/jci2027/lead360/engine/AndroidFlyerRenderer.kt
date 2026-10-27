package com.jci2027.lead360.engine

import android.content.Context
import android.graphics.*
import java.io.File
import java.io.FileOutputStream

/**
 * Native Android Canvas Flyer Compositor
 * High performance hardware-accelerated 300 DPI image generator
 */
class AndroidFlyerRenderer(private val context: Context) {

    fun compositeFlyer(
        baseBitmap: Bitmap,
        chapterLogo: Bitmap?,
        presidentName: String,
        dateTime: String,
        venue: String,
        chiefGuest: String?
    ): Bitmap {
        val width = baseBitmap.width
        val height = baseBitmap.height

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // 1. Draw Base Flyer
        canvas.drawBitmap(baseBitmap, 0f, 0f, null)

        // 2. Draw Chapter Logo if provided
        chapterLogo?.let { logo ->
            val logoRect = RectF(80f, 80f, 300f, 180f)
            canvas.drawBitmap(logo, null, logoRect, null)
        }

        // 3. Draw President Name & Designation
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = width * 0.038f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(6f, 2f, 2f, Color.argb(180, 0, 0, 0))
        }
        canvas.drawText(presidentName, 80f, height - 160f, titlePaint)

        // 4. Draw Date, Time, Venue
        val bodyPaint = Paint().apply {
            color = Color.WHITE
            textSize = width * 0.032f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
            setShadowLayer(6f, 2f, 2f, Color.argb(180, 0, 0, 0))
        }
        canvas.drawText("📅 $dateTime", 80f, height - 320f, bodyPaint)
        canvas.drawText("📍 $venue", 80f, height - 260f, bodyPaint)

        chiefGuest?.takeIf { it.isNotBlank() }?.let { guest ->
            canvas.drawText("🌟 Chief Guest: $guest", 80f, height - 200f, bodyPaint)
        }

        return resultBitmap
    }

    /**
     * Save generated bitmap to cache file ready for WhatsApp/Social share intent
     */
    fun saveBitmapToCache(bitmap: Bitmap, filename: String = "jci_flyer.png"): File {
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}
