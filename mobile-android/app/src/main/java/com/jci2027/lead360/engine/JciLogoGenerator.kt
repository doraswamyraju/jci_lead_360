package com.jci2027.lead360.engine

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import androidx.core.graphics.PathParser
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * JCI Official Logo Generator Engine — Exact replica of https://logo.jciath.be/
 *
 * Uses the exact JCI Corporate Identity vector paths from the official SVG repository:
 * - Rectangle: (1187.1, 411.8, 103.2, 572.7)
 * - J Path: "M396.6,411.8v368c0,55.9-45.6,101.5-101.5,101.5c-28,0-53.3-11.4-71.7-29.8l-73,73c37,37,88.2,60,144.7,60s107.7-22.9,144.7-60c37-37,60-88.2,60-144.7v-368H396.6z"
 * - C Path: "M751.7,827.6c-71.4-71.4-71.4-187.7,0-259.1s187.7-71.4,259.1,0l0,0l72.9-72.9l0,0c-111.8-111.8-293.1-111.8-404.9,0C567,607.4,567,788.7,678.9,900.5c111.8,111.8,293.1,111.8,404.9,0l-72.9-72.9C939.4,899,823.2,899,751.7,827.6z"
 * - Shield (Teal): path from official SVG
 * - Shield (Cyan): path from official SVG
 * - Shield Center & Crest: path from official SVG
 * - TM symbol: path from official SVG
 */
class JciLogoGenerator(private val context: Context) {

    enum class CaseStyle(val title: String, val bgType: BgType, val isSocial: Boolean) {
        COLOR("Color (Original)", BgType.WHITE, false),
        BLACK("Monochrome Black", BgType.WHITE, false),
        LIGHT("White on Dark", BgType.DARK_NAVY, false),
        WHITE_ON_BLUE("White on Blue", BgType.JCI_BLUE, false),
        SOCIAL_WHITE("Social Circle (White)", BgType.JCI_BLUE, true),
        SOCIAL_BLACK("Social Circle (Dark)", BgType.DARK_NAVY, true),
        SOCIAL_BLUE("Social Circle (Blue)", BgType.WHITE, true)
    }

    enum class BgType(val hexColor: Int) {
        WHITE(Color.WHITE),
        DARK_NAVY(Color.parseColor("#130F2D")),
        JCI_BLUE(Color.parseColor("#0097D7")),
        TRANSPARENT(Color.TRANSPARENT)
    }

    // Cached Official JCI Paths from SVG
    private val pathJ: Path by lazy {
        PathParser.createPathFromPathData("M396.6,411.8v368c0,55.9-45.6,101.5-101.5,101.5c-28,0-53.3-11.4-71.7-29.8l-73,73c37,37,88.2,60,144.7,60s107.7-22.9,144.7-60c37-37,60-88.2,60-144.7v-368H396.6z")
    }

    private val pathC: Path by lazy {
        PathParser.createPathFromPathData("M751.7,827.6c-71.4-71.4-71.4-187.7,0-259.1s187.7-71.4,259.1,0l0,0l72.9-72.9l0,0c-111.8-111.8-293.1-111.8-404.9,0C567,607.4,567,788.7,678.9,900.5c111.8,111.8,293.1,111.8,404.9,0l-72.9-72.9C939.4,899,823.2,899,751.7,827.6z")
    }

    private val pathShieldTeal: Path by lazy {
        PathParser.createPathFromPathData("M2188,377.7c-23.7-13.4-48.3-25.6-73.8-36.3c4,26,6.7,52.3,7.9,79.1v0.2c0.7,13.1,1,26.1,1,39.2c0,7.7-0.2,15.5-0.5,23.2c-0.5,26-2.6,51.6-5.8,76.8c-2.4,20.1-5.8,40-9.8,59.8c-5.2,24.8-11.3,49-18.7,72.9c-19.3,63.1-46.4,122.7-79.9,177.9c-12,19.8-24.9,38.8-38.7,57.4c-10.7,14.8-22,29.2-34,43.1c-14.8,17.5-30.4,34.4-46.8,50.5c-13.1,13.1-26.6,25.8-40.7,37.6c-17.5,15.3-35.9,29.7-54.8,43.1c17.5,15.8,35.9,30.8,54.8,45.2c18.9-14.3,37.3-29.2,55-45c14.1-12.4,27.7-25.3,40.7-38.7c16.3-16.3,31.8-33.3,46.8-51.1c11.9-14.1,23.2-28.5,34.2-43.5c13.6-18.4,26.3-37.3,38.5-56.9c31.6-51.1,58.3-105.9,78.7-163.5c9.1-25.3,17-51.2,23.7-77.7c5.3-21,9.8-42.3,13.4-63.8c4.6-26.5,8.1-53.5,10.1-80.8c1.7-21.7,2.6-43.7,2.6-65.7v-1.2C2192,432.2,2190.6,404.7,2188,377.7z")
    }

    private val pathShieldCyan: Path by lazy {
        PathParser.createPathFromPathData("M1760.6,971c-12-13.9-23.4-28.4-34-43.1c-13.8-18.6-26.6-37.6-38.7-57.4c-33.5-55.2-60.7-114.8-79.9-177.9c-7.4-23.9-13.6-48.1-18.7-72.9c-4-19.8-7.4-39.7-9.8-59.8c-3.3-25.3-5.3-50.9-5.8-76.8c-0.3-7.7-0.5-15.5-0.5-23.2c0-13.1,0.3-26.1,1-39.2v-0.2c18.4-9.1,37.3-17.4,56.7-24.6c23.2-8.6,47.1-16,71.5-22c18.7-4.5,37.8-8.1,57.2-10.8c23.2-3.4,46.8-5.5,70.8-6.2c5.8-0.2,11.9-0.2,17.7-0.2s11.9,0,17.7,0.2c24.1,0.7,47.6,2.7,70.8,6.2c19.4,2.8,38.5,6.4,57.3,10.8c24.4,6,48.3,13.4,71.5,22c-2.2-25.8-5.8-51.2-10.7-76.3c-24.2-7.7-49-13.9-74.4-18.9c-19.4-3.8-39.2-6.7-59.3-8.8c-23.9-2.6-48-4-72.6-4c-0.2,0-0.2,0-0.3,0s-0.2,0-0.3,0c-24.6,0-48.7,1.4-72.5,4c-20.1,2.1-39.9,5-59.3,8.8c-25.4,5-50.2,11.2-74.4,18.9c-20.3,6.4-40.1,13.6-59.5,21.8h-0.2c-25.4,10.7-49.9,22.9-73.6,36.3c-2.6,27-4,54.5-4,82.2v1.2c0,22,0.9,44,2.6,65.7c2.1,27.3,5.5,54.3,10.1,80.8c3.6,21.5,8.1,42.6,13.6,63.6c6.5,26.5,14.4,52.4,23.6,77.7c20.5,57.6,47.1,112.4,78.9,163.5c12,19.6,24.8,38.5,38.5,56.9c10.8,15,22.2,29.4,34.2,43.5c14.8,17.7,30.4,34.9,46.6,51.2c19.1-13.1,37.5-27.3,55-42.3C1791,1005.4,1775.4,988.6,1760.6,971z")
    }

    private val pathShieldCrest1: Path by lazy {
        PathParser.createPathFromPathData("M1848.1,789.4c14.3-22.3,27.2-45.7,38.5-70c16.2-34,29.1-70,38.7-107.1c5.5-22,10-44.5,13.1-67.6c-22.2-4.6-44.9-7.6-68.1-8.8c-7.4-0.3-14.8-0.5-22.2-0.5s-14.8,0.2-22.2,0.5c-23.2,1.2-45.9,4.1-68.1,8.8c3.1,23,7.6,45.6,13.1,67.6c9.6,37.1,22.5,73.1,38.7,107.1C1821,743.6,1833.8,767,1848.1,789.4z")
    }

    private val pathShieldCrest2: Path by lazy {
        PathParser.createPathFromPathData("M1704.2,559.7c-22.5,7.6-44.4,17.2-65,28.4c4.1,23.9,9.5,47.3,15.8,70.3c17.2,61.2,42.1,119.3,73.6,172.9c11.9,20.5,24.8,40.2,38.5,59.1c10.8,15.1,22.4,29.6,34.2,43.7c14.8,17.5,30.4,34.4,46.8,50.4c16.3-16,32-32.8,46.8-50.4c11.9-14.1,23.4-28.5,34.2-43.7c13.8-18.9,26.6-38.7,38.5-59.1c31.5-53.6,56.4-111.7,73.6-172.9c6.4-23,11.7-46.4,15.8-70.3c3.4-19.1,6-38.2,7.9-57.6v-0.2c2.1-23.2,3.3-46.8,3.3-70.5v-3.6c-22.4-9.5-45.6-17.5-69.3-24.2c-18.2-5-37-9.1-55.9-12.4c-22.7-3.8-45.9-6.4-69.5-7.4c-8.4-0.3-16.8-0.5-25.4-0.5c-8.4,0-17,0.2-25.4,0.5c-23.6,1-46.8,3.6-69.5,7.6c-19.1,3.1-37.6,7.2-55.9,12.2v0.2c-23.9,6.5-46.9,14.6-69.3,24.2v3.4c0,23.7,1.2,47.3,3.3,70.5c21.3-10.5,43.7-19.4,66.9-26.8c17.7-5.5,35.9-10.1,54.5-13.8c22.2-4.5,45-7.2,68.4-8.4c8.9-0.7,17.9-0.9,27-0.9s18.1,0.2,27,0.9c23.4,1.2,46.2,4,68.4,8.4c18.6,3.6,36.8,8.3,54.5,13.8c-1.2,18.9-3.3,37.6-6,56v0.2c-3.4,23.2-8.1,45.7-13.9,67.9c-13.2,51.6-32.7,100.7-57.3,146.6c-11.7,21.8-24.6,43-38.5,63.3c-10.7,15.6-22.2,30.8-34.2,45.5c-12-14.8-23.6-29.9-34.2-45.5c-13.9-20.3-26.8-41.4-38.5-63.3c-24.6-45.9-44-95.1-57.3-146.6C1712.3,605.4,1707.7,582.9,1704.2,559.7z")
    }

    private val pathTmLetterT: Path by lazy {
        PathParser.createPathFromPathData("M2122.4,920.2v6.3h17.7v58h6.9v-58h17.6v-6.3H2122.4z")
    }

    private val pathTmLetterM: Path by lazy {
        PathParser.createPathFromPathData("M2232.7,920.2 2232.7,984.5 2225.7,984.5 2225.7,933.4 2204.3,964.7 2203.7,964.7 2182.3,933.2 2182.3,984.5 2175.2,984.5 2175.2,920.2 2181.4,920.2 2204,953.2 2226.6,920.2")
    }

    /**
     * Fetches exact vector SVG text directly from logogen.jciath.be backend
     */
    suspend fun fetchVectorSvg(line1: String, line2: String = ""): String = withContext(Dispatchers.IO) {
        try {
            val encLine1 = URLEncoder.encode(line1.trim(), "UTF-8")
            val encLine2 = URLEncoder.encode(line2.trim(), "UTF-8")
            val url = URL("https://logogen.jciath.be/?line1=$encLine1&line2=$encLine2")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "Mozilla/5.0 JCI-Lead360-Android")
            }
            conn.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            generateOfflineSvg(line1, line2)
        }
    }

    /**
     * Complete offline SVG generator matching exact jciath.be markup
     */
    fun generateOfflineSvg(line1: String, line2: String): String {
        return """
<svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2400 1600" style="enable-background:new 0 0 2400 1600;">
<style type="text/css">
	.st0{fill:#59BDBD;}
	.st1{fill:#1197D5;}
	.st2{fill:#17142C;}
</style>
<text x="185" y="1240" font-size="120" font-family="Arial, Helvetica, sans-serif" font-weight="bold" fill="#1197D5">${line1.uppercase()}</text>
${if (line2.isNotBlank()) "<text x=\"185\" y=\"1440\" font-size=\"110\" font-family=\"Arial, Helvetica, sans-serif\" font-weight=\"bold\" fill=\"#1197D5\">${line2.uppercase()}</text>" else ""}
<g class="jci">
    <g>
        <rect class="st2" x="1187.1" y="411.8" width="103.2" height="572.7"/>
        <path class="st2" d="M396.6,411.8v368c0,55.9-45.6,101.5-101.5,101.5c-28,0-53.3-11.4-71.7-29.8l-73,73c37,37,88.2,60,144.7,60s107.7-22.9,144.7-60c37-37,60-88.2,60-144.7v-368H396.6z"/>
        <path class="st2" d="M751.7,827.6c-71.4-71.4-71.4-187.7,0-259.1s187.7-71.4,259.1,0l0,0l72.9-72.9l0,0c-111.8-111.8-293.1-111.8-404.9,0C567,607.4,567,788.7,678.9,900.5c111.8,111.8,293.1,111.8,404.9,0l-72.9-72.9C939.4,899,823.2,899,751.7,827.6z"/>
    </g>
    <g>
        <path class="st0" d="M2188,377.7c-23.7-13.4-48.3-25.6-73.8-36.3c4,26,6.7,52.3,7.9,79.1v0.2c0.7,13.1,1,26.1,1,39.2c0,7.7-0.2,15.5-0.5,23.2c-0.5,26-2.6,51.6-5.8,76.8c-2.4,20.1-5.8,40-9.8,59.8c-5.2,24.8-11.3,49-18.7,72.9c-19.3,63.1-46.4,122.7-79.9,177.9c-12,19.8-24.9,38.8-38.7,57.4c-10.7,14.8-22,29.2-34,43.1c-14.8,17.5-30.4,34.4-46.8,50.5c-13.1,13.1-26.6,25.8-40.7,37.6c-17.5,15.3-35.9,29.7-54.8,43.1c17.5,15.8,35.9,30.8,54.8,45.2c18.9-14.3,37.3-29.2,55-45c14.1-12.4,27.7-25.3,40.7-38.7c16.3-16.3,31.8-33.3,46.8-51.1c11.9-14.1,23.2-28.5,34.2-43.5c13.6-18.4,26.3-37.3,38.5-56.9c31.6-51.1,58.3-105.9,78.7-163.5c9.1-25.3,17-51.2,23.7-77.7c5.3-21,9.8-42.3,13.4-63.8c4.6-26.5,8.1-53.5,10.1-80.8c1.7-21.7,2.6-43.7,2.6-65.7v-1.2C2192,432.2,2190.6,404.7,2188,377.7z"/>
        <path class="st1" d="M1760.6,971c-12-13.9-23.4-28.4-34-43.1c-13.8-18.6-26.6-37.6-38.7-57.4c-33.5-55.2-60.7-114.8-79.9-177.9c-7.4-23.9-13.6-48.1-18.7-72.9c-4-19.8-7.4-39.7-9.8-59.8c-3.3-25.3-5.3-50.9-5.8-76.8c-0.3-7.7-0.5-15.5-0.5-23.2c0-13.1,0.3-26.1,1-39.2v-0.2c18.4-9.1,37.3-17.4,56.7-24.6c23.2-8.6,47.1-16,71.5-22c18.7-4.5,37.8-8.1,57.2-10.8c23.2-3.4,46.8-5.5,70.8-6.2c5.8-0.2,11.9-0.2,17.7-0.2s11.9,0,17.7,0.2c24.1,0.7,47.6,2.7,70.8,6.2c19.4,2.8,38.5,6.4,57.3,10.8c24.4,6,48.3,13.4,71.5,22c-2.2-25.8-5.8-51.2-10.7-76.3c-24.2-7.7-49-13.9-74.4-18.9c-19.4-3.8-39.2-6.7-59.3-8.8c-23.9-2.6-48-4-72.6-4c-0.2,0-0.2,0-0.3,0s-0.2,0-0.3,0c-24.6,0-48.7,1.4-72.5,4c-20.1,2.1-39.9,5-59.3,8.8c-25.4,5-50.2,11.2-74.4,18.9c-20.3,6.4-40.1,13.6-59.5,21.8h-0.2c-25.4,10.7-49.9,22.9-73.6,36.3c-2.6,27-4,54.5-4,82.2v1.2c0,22,0.9,44,2.6,65.7c2.1,27.3,5.5,54.3,10.1,80.8c3.6,21.5,8.1,42.6,13.6,63.6c6.5,26.5,14.4,52.4,23.6,77.7c20.5,57.6,47.1,112.4,78.9,163.5c12,19.6,24.8,38.5,38.5,56.9c10.8,15,22.2,29.4,34.2,43.5c14.8,17.7,30.4,34.9,46.6,51.2c19.1-13.1,37.5-27.3,55-42.3C1791,1005.4,1775.4,988.6,1760.6,971z"/>
        <path class="st2" d="M1848.1,789.4c14.3-22.3,27.2-45.7,38.5-70c16.2-34,29.1-70,38.7-107.1c5.5-22,10-44.5,13.1-67.6c-22.2-4.6-44.9-7.6-68.1-8.8c-7.4-0.3-14.8-0.5-22.2-0.5s-14.8,0.2-22.2,0.5c-23.2,1.2-45.9,4.1-68.1,8.8c3.1,23,7.6,45.6,13.1,67.6c9.6,37.1,22.5,73.1,38.7,107.1C1821,743.6,1833.8,767,1848.1,789.4z"/>
        <path class="st2" d="M1704.2,559.7c-22.5,7.6-44.4,17.2-65,28.4c4.1,23.9,9.5,47.3,15.8,70.3c17.2,61.2,42.1,119.3,73.6,172.9c11.9,20.5,24.8,40.2,38.5,59.1c10.8,15.1,22.4,29.6,34.2,43.7c14.8,17.5,30.4,34.4,46.8,50.4c16.3-16,32-32.8,46.8-50.4c11.9-14.1,23.4-28.5,34.2-43.7c13.8-18.9,26.6-38.7,38.5-59.1c31.5-53.6,56.4-111.7,73.6-172.9c6.4-23,11.7-46.4,15.8-70.3c3.4-19.1,6-38.2,7.9-57.6v-0.2c2.1-23.2,3.3-46.8,3.3-70.5v-3.6c-22.4-9.5-45.6-17.5-69.3-24.2c-18.2-5-37-9.1-55.9-12.4c-22.7-3.8-45.9-6.4-69.5-7.4c-8.4-0.3-16.8-0.5-25.4-0.5c-8.4,0-17,0.2-25.4,0.5c-23.6,1-46.8,3.6-69.5,7.6c-19.1,3.1-37.6,7.2-55.9,12.2v0.2c-23.9,6.5-46.9,14.6-69.3,24.2v3.4c0,23.7,1.2,47.3,3.3,70.5c21.3-10.5,43.7-19.4,66.9-26.8c17.7-5.5,35.9-10.1,54.5-13.8c22.2-4.5,45-7.2,68.4-8.4c8.9-0.7,17.9-0.9,27-0.9s18.1,0.2,27,0.9c23.4,1.2,46.2,4,68.4,8.4c18.6,3.6,36.8,8.3,54.5,13.8c-1.2,18.9-3.3,37.6-6,56v0.2c-3.4,23.2-8.1,45.7-13.9,67.9c-13.2,51.6-32.7,100.7-57.3,146.6c-11.7,21.8-24.6,43-38.5,63.3c-10.7,15.6-22.2,30.8-34.2,45.5c-12-14.8-23.6-29.9-34.2-45.5c-13.9-20.3-26.8-41.4-38.5-63.3c-24.6-45.9-44-95.1-57.3-146.6C1712.3,605.4,1707.7,582.9,1704.2,559.7z"/>
    </g>
    <g>
        <path class="st2" d="M2122.4,920.2v6.3h17.7v58h6.9v-58h17.6v-6.3H2122.4z"/>
        <polygon class="st2" points="2232.7,920.2 2232.7,984.5 2225.7,984.5 2225.7,933.4 2204.3,964.7 2203.7,964.7 2182.3,933.2 2182.3,984.5 2175.2,984.5 2175.2,920.2 2181.4,920.2 2204,953.2 2226.6,920.2"/>
    </g>
</g>
</svg>
        """.trimIndent()
    }

    /**
     * Renders exact high-resolution raster bitmap matching logo.jciath.be visual cases
     * Uses 2400 x 1600 coordinate space scaled to output dimensions.
     */
    fun renderLogoBitmap(
        line1: String,
        line2: String = "",
        caseStyle: CaseStyle = CaseStyle.COLOR,
        width: Int = 1600,
        height: Int = if (caseStyle.isSocial) 1600 else 1066
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill background
        canvas.drawColor(caseStyle.bgType.hexColor)

        val colorNavy = Color.parseColor("#17142C")
        val colorBlue = Color.parseColor("#1197D5")
        val colorTeal = Color.parseColor("#59BDBD")
        val colorWhite = Color.WHITE

        if (caseStyle.isSocial) {
            val circleRadius = width * 0.40f
            val circleCenterX = width / 2f
            val circleCenterY = height / 2f

            val circleBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = when (caseStyle) {
                    CaseStyle.SOCIAL_WHITE -> Color.WHITE
                    CaseStyle.SOCIAL_BLACK -> colorNavy
                    CaseStyle.SOCIAL_BLUE -> colorBlue
                    else -> Color.WHITE
                }
                style = Paint.Style.FILL
            }
            canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, circleBgPaint)

            // Scale and draw full mark inside circle
            val markScale = (circleRadius * 1.5f) / 2400f
            val markOffsetX = circleCenterX - (2400f * markScale / 2f)
            val markOffsetY = circleCenterY - (1600f * markScale / 2f) - (circleRadius * 0.12f)

            val primaryColor = when (caseStyle) {
                CaseStyle.SOCIAL_WHITE -> colorBlue
                CaseStyle.SOCIAL_BLACK -> Color.WHITE
                CaseStyle.SOCIAL_BLUE -> Color.WHITE
                else -> colorBlue
            }

            drawOfficialVectorMark(
                canvas = canvas,
                offsetX = markOffsetX,
                offsetY = markOffsetY,
                scale = markScale,
                mainColor = primaryColor,
                tealColor = if (caseStyle == CaseStyle.SOCIAL_WHITE) colorTeal else primaryColor,
                cyanColor = primaryColor
            )

            // Centered Chapter Name inside circle lower section
            if (line1.isNotBlank()) {
                val socialTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = primaryColor
                    textSize = circleRadius * 0.18f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(line1.uppercase(), circleCenterX, circleCenterY + (circleRadius * 0.70f), socialTextPaint)
            }
            return bitmap
        }

        // Standard Triple Cases (Color, Black, Light, White on Blue)
        val mainJciColor: Int
        val tealColor: Int
        val cyanColor: Int
        val textColor: Int

        when (caseStyle) {
            CaseStyle.COLOR -> {
                mainJciColor = colorNavy
                tealColor = colorTeal
                cyanColor = colorBlue
                textColor = colorBlue
            }
            CaseStyle.BLACK -> {
                mainJciColor = Color.BLACK
                tealColor = Color.BLACK
                cyanColor = Color.BLACK
                textColor = Color.BLACK
            }
            CaseStyle.LIGHT -> {
                mainJciColor = colorWhite
                tealColor = colorTeal
                cyanColor = colorWhite
                textColor = colorWhite
            }
            CaseStyle.WHITE_ON_BLUE -> {
                mainJciColor = colorWhite
                tealColor = colorWhite
                cyanColor = colorWhite
                textColor = colorWhite
            }
            else -> {
                mainJciColor = colorNavy
                tealColor = colorTeal
                cyanColor = colorBlue
                textColor = colorBlue
            }
        }

        // Canvas Scale Factor relative to official 2400x1600 SVG viewBox
        val scale = width / 2400f
        val jciTranslateY = if (line2.isNotBlank() && line1.isNotBlank()) -100f * scale else 0f

        // Draw Official JCI Mark with exact SVG paths
        drawOfficialVectorMark(
            canvas = canvas,
            offsetX = 0f,
            offsetY = jciTranslateY,
            scale = scale,
            mainColor = mainJciColor,
            tealColor = tealColor,
            cyanColor = cyanColor
        )

        // Draw Chapter Name Typography
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }

        val textStartX = 185f * scale
        val displayLine1 = if (line1.isBlank()) "Exemple" else line1.trim().uppercase()

        if (line2.isBlank()) {
            textPaint.textSize = 120f * scale
            val baselineY = (1240f * scale) + jciTranslateY
            canvas.drawText(displayLine1, textStartX, baselineY, textPaint)
        } else {
            textPaint.textSize = 110f * scale
            val line1Y = (1180f * scale) + jciTranslateY
            val line2Y = (1380f * scale) + jciTranslateY
            canvas.drawText(displayLine1, textStartX, line1Y, textPaint)
            canvas.drawText(line2.trim().uppercase(), textStartX, line2Y, textPaint)
        }

        return bitmap
    }

    /**
     * Draws the exact JCI Corporate Identity vector marks using official PathParser paths
     */
    private fun drawOfficialVectorMark(
        canvas: Canvas,
        offsetX: Float,
        offsetY: Float,
        scale: Float,
        mainColor: Int,
        tealColor: Int,
        cyanColor: Int
    ) {
        val paintMain = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mainColor
            style = Paint.Style.FILL
        }

        val paintTeal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tealColor
            style = Paint.Style.FILL
        }

        val paintCyan = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = cyanColor
            style = Paint.Style.FILL
        }

        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)

        // 1. Letters J, C, I (Rectangle)
        canvas.drawPath(pathJ, paintMain)
        canvas.drawPath(pathC, paintMain)
        canvas.drawRect(1187.1f, 411.8f, 1187.1f + 103.2f, 411.8f + 572.7f, paintMain)

        // 2. Official JCI Emblem Crest / Shield
        canvas.drawPath(pathShieldTeal, paintTeal)
        canvas.drawPath(pathShieldCyan, paintCyan)
        canvas.drawPath(pathShieldCrest1, paintMain)
        canvas.drawPath(pathShieldCrest2, paintMain)

        // 3. TM Trademark symbol
        canvas.drawPath(pathTmLetterT, paintMain)
        canvas.drawPath(pathTmLetterM, paintMain)

        canvas.restore()
    }

    /**
     * Saves rendered Bitmap to cache or export directory
     */
    fun saveBitmap(bitmap: Bitmap, filename: String, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG): File {
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(format, 100, out)
        }
        return file
    }

    /**
     * Saves raw SVG string to cache or export file
     */
    fun saveSvgFile(svgContent: String, filename: String): File {
        val file = File(context.cacheDir, filename)
        file.writeText(svgContent, Charsets.UTF_8)
        return file
    }
}
