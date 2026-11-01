package com.jci2027.lead360.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ParsedCardData(
    val name: String = "",
    val designation: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val address: String = "",
    val socialProfiles: List<String> = emptyList(),
    val rawText: String = "",
    val confidence: String = "HIGH"
)

object BusinessCardOcrEngine {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val PHONE_REGEX = Regex("""(?:\+?\d{1,3}[-.\s]?)?(?:\(?\d{1,4}\)?[-.\s]?)?\d{2,6}[-.\s]?\d{2,6}(?:[-.\s]?\d{2,6})*""")
    private val WEBSITE_REGEX = Regex("(?:https?://)?(?:www\\.)?[a-zA-Z0-9][a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?")
    private val SOCIAL_REGEX = Regex("(?:linkedin\\.com/[^\\s]+|youtube\\.com/[^\\s]+|instagram\\.com/[^\\s]+|facebook\\.com/[^\\s]+|twitter\\.com/[^\\s]+|x\\.com/[^\\s]+)", RegexOption.IGNORE_CASE)

    private val COMPANY_SUFFIXES = Regex(
        "\\b(?:Pty\\s+Ltd|Inc|LLC|Ltd|Corp|Corporation|Company|Co\\.|GmbH|AG|SA|SAS|SL|BV|NV|PLC|LLP|Group|Technologies|Solutions|Consulting|Associates|Partners|Enterprises|Industries|International|Labs|Specialty|Clinics)\\b",
        RegexOption.IGNORE_CASE
    )
    private val TITLE_KEYWORDS = Regex(
        "\\b(?:CEO|CTO|CFO|COO|CMO|President|Director|Manager|Engineer|Founder|Co-Founder|VP\\s+of|SVP\\s+of|Director\\s+of|Head\\s+of|VP|SVP|EVP|Chief|Head|Lead|Senior|Junior|Associate|Consultant|Specialist|Coordinator|Administrator|Analyst|Developer|Architect|Officer|Partner|Owner|Principal|Advisor|Doctor|Dr\\.|Secretary|Treasurer|Senator)\\b",
        RegexOption.IGNORE_CASE
    )
    private val ADDRESS_STREET_REGEX = Regex(
        "\\d+[A-Za-z]?\\s+.*(?:Street|St\\.?|Road|Rd\\.?|Avenue|Ave\\.?|Drive|Dr\\.?|Lane|Ln\\.?|Boulevard|Blvd\\.?|Way|Court|Ct\\.?|Plaza|Square|Highway|Hwy\\.?|Parkway|Pkwy\\.?|Circle|Cir\\.?|Terrace|Ter\\.?|Place|Loop|Nagar|Colony|Layout|Pradesh|Sector)",
        RegexOption.IGNORE_CASE
    )

    /**
     * Process ImageProxy directly from CameraX analyzer
     */
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun processImageProxy(imageProxy: ImageProxy, onResult: (String) -> Unit) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    onResult(visionText.text)
                    imageProxy.close()
                }
                .addOnFailureListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    /**
     * Process bitmap with CardSnap auto-crop & Google ML Kit
     */
    suspend fun processCardBitmap(originalBitmap: Bitmap): ParsedCardData = suspendCancellableCoroutine { cont ->
        try {
            val croppedBitmap = CardSnapImageCropper.cropToCardGuide(originalBitmap)
            val image = InputImage.fromBitmap(croppedBitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (cont.isActive) {
                        val parsed = parseExtractedText(visionText.text)
                        cont.resume(parsed)
                    }
                }
                .addOnFailureListener { exception ->
                    if (cont.isActive) {
                        cont.resumeWithException(exception)
                    }
                }
        } catch (e: Exception) {
            if (cont.isActive) {
                cont.resumeWithException(e)
            }
        }
    }

    /**
     * Merge front and back OCR outputs into one rich contact record
     */
    fun mergeAndParse(frontText: String, backText: String): ParsedCardData {
        val combined = "$frontText\n$backText"
        return parseExtractedText(combined)
    }

    /**
     * Heuristic parser extracting Name, Title, Company, Phone, Email, Website, Address, Socials
     */
    fun parseExtractedText(ocrText: String): ParsedCardData {
        val lines = ocrText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val emails = EMAIL_REGEX.findAll(ocrText).map { it.value }.toList()
        val phones = PHONE_REGEX.findAll(ocrText).map { it.value.trim() }.filter { it.filter { c -> c.isDigit() }.length in 8..15 }.toList()
        val websites = WEBSITE_REGEX.findAll(ocrText).map { it.value }.filter { !it.contains("@") && !SOCIAL_REGEX.containsMatchIn(it) }.toList()
        val socials = SOCIAL_REGEX.findAll(ocrText).map { it.value.removePrefix("https://").removePrefix("http://") }.toList()

        var name = ""
        var company = ""
        var title = ""
        val addressLines = mutableListOf<String>()

        for (line in lines) {
            // Check for valid Name (no @, no digits, reasonable length, not a title keyword)
            if (name.isBlank() && line.length in 3..40 && !line.contains("@") && line.none { it.isDigit() } && !TITLE_KEYWORDS.containsMatchIn(line) && !COMPANY_SUFFIXES.containsMatchIn(line)) {
                name = line
            }
            // Check company
            if (company.isBlank() && COMPANY_SUFFIXES.containsMatchIn(line)) {
                company = line
            }
            // Check title / designation
            if (title.isBlank() && TITLE_KEYWORDS.containsMatchIn(line)) {
                title = line
            }
            // Check address
            if (ADDRESS_STREET_REGEX.containsMatchIn(line)) {
                addressLines.add(line)
            }
        }

        // Common Indian city/state fallback in address
        val commonLocations = listOf("Tirupati", "Hyderabad", "Bengaluru", "Bangalore", "Chennai", "Mumbai", "Delhi", "Andhra Pradesh", "Telangana", "Karnataka", "Tamil Nadu")
        for (line in lines) {
            if (commonLocations.any { line.contains(it, ignoreCase = true) } && !addressLines.contains(line)) {
                addressLines.add(line)
            }
        }

        if (name.isBlank() && lines.isNotEmpty()) {
            name = lines.firstOrNull { it.none { c -> c.isDigit() } } ?: lines.first()
        }
        if (company.isBlank()) {
            company = if (lines.size > 1) lines.getOrNull(1) ?: "Organization" else "Organization"
        }
        if (title.isBlank()) {
            title = "Executive / Member"
        }

        val address = addressLines.distinct().joinToString(", ")

        val hasRealName = name.isNotBlank() && name.length >= 2
        val hasPhone = phones.isNotEmpty()
        val hasEmail = emails.isNotEmpty()
        val confidence = when {
            hasRealName && (hasPhone || hasEmail) -> "HIGH"
            hasRealName || hasPhone || hasEmail -> "MEDIUM"
            else -> "LOW"
        }

        return ParsedCardData(
            name = name,
            designation = title,
            company = company,
            phone = phones.firstOrNull() ?: "",
            email = emails.firstOrNull() ?: "",
            website = websites.firstOrNull() ?: "",
            address = address,
            socialProfiles = socials,
            rawText = ocrText,
            confidence = confidence
        )
    }
}
