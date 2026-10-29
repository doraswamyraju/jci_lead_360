package com.jci2027.lead360.engine

import android.content.Context
import android.graphics.*
import java.io.File
import java.io.FileOutputStream

/**
 * JCI Official Logo Lockup Generator Engine (Inspired by logo.jciath.be)
 * Generates transparent high-resolution JCI corporate identity compliant logos:
 * [ JCI Logo Mark ] + [ CHAPTER NAME ] in official typography and colors.
 */
class JciLogoGenerator(private val context: Context) {

    enum class LogoTheme(val primaryColor: Int, val isDarkBg: Boolean) {
        JCI_BLUE(Color.parseColor("#0097D8"), false),
        JCI_NAVY(Color.parseColor("#002B49"), false),
        MONOCHROME_BLACK(Color.BLACK, false),
        MONOCHROME_WHITE(Color.WHITE, true)
    }

    fun generateOfficialChapterLogo(
        chapterName: String,
        theme: LogoTheme = LogoTheme.JCI_BLUE,
        width: Int = 1200,
        height: Int = 360
    ): Bitmap {
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        // 1. Draw JCI Logo Mark / Shield
        val iconPaint = Paint().apply {
            color = theme.primaryColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        // Draw Stylized JCI Shield / Crest on the left
        val iconSize = height * 0.65f
        val iconLeft = 40f
        val iconTop = (height - iconSize) / 2f
        val iconRect = RectF(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)

        // Outer Crest
        canvas.drawRoundRect(iconRect, 18f, 18f, iconPaint)

        // Inner negative cutout (JCI Lettering look)
        val innerPaint = Paint().apply {
            color = if (theme.isDarkBg) Color.BLACK else Color.WHITE
            textSize = iconSize * 0.42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val textY = iconRect.centerY() - ((innerPaint.descent() + innerPaint.ascent()) / 2f)
        canvas.drawText("JCI", iconRect.centerX(), textY, innerPaint)

        // 2. Vertical Divider Line
        val dividerPaint = Paint().apply {
            color = if (theme.isDarkBg) Color.LTGRAY else Color.parseColor("#CBD5E1")
            strokeWidth = 4f
            isAntiAlias = true
        }
        val dividerX = iconLeft + iconSize + 36f
        canvas.drawLine(dividerX, iconTop + 10f, dividerX, iconTop + iconSize - 10f, dividerPaint)

        // 3. Official Chapter Lockup Typography
        val chapterTextX = dividerX + 36f

        // "JUNIOR CHAMBER INTERNATIONAL" subtitle
        val subPaint = Paint().apply {
            color = if (theme.isDarkBg) Color.LTGRAY else Color.parseColor("#64748B")
            textSize = height * 0.11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.12f
            isAntiAlias = true
        }
        val subY = iconTop + (iconSize * 0.32f)
        canvas.drawText("JUNIOR CHAMBER INTERNATIONAL", chapterTextX, subY, subPaint)

        // Chapter Name (e.g. "TIRUPATI INNOVATIONS" or "BANGALORE GARDEN CITY")
        val cleanChapterName = chapterName
            .replace("JCI", "", ignoreCase = true)
            .trim()
            .uppercase()

        val mainPaint = Paint().apply {
            color = theme.primaryColor
            textSize = height * 0.28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.05f
            isAntiAlias = true
        }
        val mainY = iconTop + (iconSize * 0.82f)
        canvas.drawText(cleanChapterName, chapterTextX, mainY, mainPaint)

        return resultBitmap
    }

    fun saveLogoToCache(bitmap: Bitmap, filename: String = "jci_official_logo.png"): File {
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}

