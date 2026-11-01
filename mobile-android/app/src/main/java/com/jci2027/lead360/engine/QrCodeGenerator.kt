package com.jci2027.lead360.engine

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeGenerator {
    /**
     * Generates standard sharp vCard or Web URL QR Bitmap
     */
    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M

            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val pixels = IntArray(matrixWidth * matrixHeight)

            for (y in 0 until matrixHeight) {
                val offset = y * matrixWidth
                for (x in 0 until matrixWidth) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
                }
            }

            val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Build Reciprocal Contact Exchange URL for QR Code.
     * When scanned by any camera, it opens a responsive web portal allowing the visitor
     * to input their details (which auto-saves to the Leader's LEAD360 Rolodex directory)
     * and downloads the Leader's full contact card (.vcf) into the visitor's phone.
     */
    fun buildExchangeUrl(leaderId: String = "usr_president", baseUrl: String = "https://lead.sriddha.com"): String {
        return "$baseUrl/card/$leaderId"
    }

    /**
     * Standard vCard 3.0 string formatted for instant reciprocal contact exchange
     */
    fun buildVCardString(
        name: String,
        role: String,
        chapter: String,
        phone: String,
        email: String,
        website: String = "https://lead.sriddha.com"
    ): String {
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("FN:$name")
            appendLine("ORG:Junior Chamber International (JCI);$chapter")
            appendLine("TITLE:$role")
            if (phone.isNotBlank()) appendLine("TEL;TYPE=CELL,VOICE:$phone")
            if (email.isNotBlank()) appendLine("EMAIL;TYPE=PREF,INTERNET:$email")
            if (website.isNotBlank()) appendLine("URL:$website")
            appendLine("NOTE:JCI LEAD360 Digital Identity • Verified 2027 Leader")
            appendLine("END:VCARD")
        }
    }
}

