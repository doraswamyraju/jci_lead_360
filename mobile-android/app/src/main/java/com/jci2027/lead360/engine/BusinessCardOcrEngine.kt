package com.jci2027.lead360.engine

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.regex.Pattern
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
    val rawText: String = ""
)

object BusinessCardOcrEngine {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val emailPattern = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )

    private val phonePattern = Pattern.compile(
        "(\\+?\\d{1,3}[-.\\s]?)?(\\(?\\d{2,5}\\)?[-.\\s]?)?\\d{3,5}[-.\\s]?\\d{4,5}"
    )

    private val websitePattern = Pattern.compile(
        "(https?://)?(www\\.)?[a-zA-Z0-9\\-]{2,256}\\.(com|in|org|net|co|io|biz|info)",
        Pattern.CASE_INSENSITIVE
    )

    private val designationKeywords = listOf(
        "president", "vice president", "secretary", "treasurer", "director", "manager",
        "founder", "co-founder", "ceo", "cto", "cfo", "md", "managing director",
        "partner", "consultant", "advocate", "doctor", "dr.", "engineer", "officer",
        "chair", "chairman", "lead", "head", "executive", "senator", "past president"
    )

    private val companyKeywords = listOf(
        "ltd", "limited", "pvt", "private", "corp", "corporation", "inc", "technologies",
        "solutions", "enterprises", "associates", "group", "industries", "agency", "academy",
        "clinics", "hospital", "builders", "realty", "infra", "services", "chambers"
    )

    /**
     * Process bitmap using Google ML Kit on-device neural text recognizer
     */
    suspend fun processCardBitmap(bitmap: Bitmap): ParsedCardData = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
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
    }

    /**
     * Heuristic parser to extract business card fields from OCR text lines
     */
    fun parseExtractedText(raw: String): ParsedCardData {
        val lines = raw.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        var foundEmail = ""
        var foundPhone = ""
        var foundWebsite = ""
        var foundName = ""
        var foundDesignation = ""
        var foundCompany = ""

        val remainingLines = mutableListOf<String>()

        for (line in lines) {
            val emailMatcher = emailPattern.matcher(line)
            if (foundEmail.isBlank() && emailMatcher.find()) {
                foundEmail = emailMatcher.group()
                continue
            }

            val webMatcher = websitePattern.matcher(line)
            if (foundWebsite.isBlank() && webMatcher.find()) {
                foundWebsite = webMatcher.group()
                continue
            }

            val phoneMatcher = phonePattern.matcher(line)
            if (foundPhone.isBlank() && phoneMatcher.find()) {
                val candidate = phoneMatcher.group().trim()
                if (candidate.filter { it.isDigit() }.length in 8..14) {
                    foundPhone = candidate
                    continue
                }
            }

            // Check if line indicates designation
            val lower = line.lowercase()
            if (foundDesignation.isBlank() && designationKeywords.any { lower.contains(it) }) {
                foundDesignation = line
                continue
            }

            // Check if line indicates company
            if (foundCompany.isBlank() && companyKeywords.any { lower.contains(it) }) {
                foundCompany = line
                continue
            }

            remainingLines.add(line)
        }

        // The top-most remaining short line is usually the person's name
        if (remainingLines.isNotEmpty()) {
            val candidateName = remainingLines.firstOrNull { line ->
                val words = line.split("\\s+".toRegex())
                words.size in 1..4 && line.none { it.isDigit() } && line.length in 3..35
            }
            if (candidateName != null) {
                foundName = candidateName
                remainingLines.remove(candidateName)
            }
        }

        // If company wasn't found by keyword, second line or header line often is company
        if (foundCompany.isBlank() && remainingLines.isNotEmpty()) {
            foundCompany = remainingLines.first()
        }

        return ParsedCardData(
            name = foundName.ifBlank { "JC Executive" },
            designation = foundDesignation.ifBlank { "Chapter Member / Delegate" },
            company = foundCompany.ifBlank { "Business Enterprise" },
            phone = foundPhone.ifBlank { "+91 " },
            email = foundEmail,
            website = foundWebsite,
            rawText = raw
        )
    }
}
