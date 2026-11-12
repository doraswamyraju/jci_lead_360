import UIKit
import CoreText

/**
 * JCI Official Logo Generator Engine — Conforming to JCI Brand Guidelines 2026 (v1.2)
 *
 * Implements:
 * - Exact JCI Shield & Wordmark vector geometry directly from official SVG paths:
 *   - pathJ, pathC, Rect I
 *   - pathShieldTeal, pathShieldCyan, pathShieldCrest1, pathShieldCrest2
 *   - pathTmLetterT, pathTmLetterM
 * - Plus Jakarta Sans Bold typography in Proper Case (Page 12)
 * - 4 Official 2026 Logo Colourways: Default, Inverted, Monochrome Black, Monochrome White
 * - Standalone Shield Social Profile Circles (Page 13)
 * - Secondary Logo Option (Page 10: "Junior Chamber International" subtitle)
 * - Transparent & Solid Background PNG, JPG, and 2400x1600 Scalable Vector SVG
 */
class JciLogoGenerator {

    enum CaseStyle: String, CaseIterable, Identifiable {
        case defaultColor = "Default (Full Color)"
        case inverted = "Inverted (White / Dark)"
        case monochromeBlack = "Monochrome Black"
        case monochromeWhite = "Monochrome White"
        case socialBlue = "Social Circle (Blue)"
        case socialDark = "Social Circle (Dark Navy)"
        case socialWhite = "Social Circle (White)"

        var id: String { rawValue }

        var isSocial: Bool {
            switch self {
            case .socialBlue, .socialDark, .socialWhite: return true
            default: return false
            }
        }

        var description: String {
            switch self {
            case .defaultColor: return "Primary colorway on white or light backgrounds"
            case .inverted: return "For dark backgrounds: white wordmark with teal shield"
            case .monochromeBlack: return "100% Black for single-color print & fax"
            case .monochromeWhite: return "100% White for solid color or photo backdrops"
            case .socialBlue: return "JCI Blue circular avatar with white shield icon"
            case .socialDark: return "Dark Navy circular avatar with teal shield icon"
            case .socialWhite: return "White circular avatar with full-color shield icon"
            }
        }
    }

    // Official JCI Brand Guidelines 2026 Palette (Page 19-20)
    static let jciBlue = UIColor(red: 0/255.0, green: 151/255.0, blue: 215/255.0, alpha: 1.0)     // #0097D7 (PMS 2925 C)
    static let jciBlack = UIColor(red: 19/255.0, green: 15/255.0, blue: 45/255.0, alpha: 1.0)     // #130F2D (PMS 5395 C)
    static let jciTeal = UIColor(red: 87/255.0, green: 188/255.0, blue: 188/255.0, alpha: 1.0)   // #57BCBC (PMS 3255 C)
    static let jciWhite = UIColor.white

    // Official Vector Path Strings from official SVG repository (logogen.jciath.be)
    static let svgPathJ = "M396.6,411.8v368c0,55.9-45.6,101.5-101.5,101.5c-28,0-53.3-11.4-71.7-29.8l-73,73c37,37,88.2,60,144.7,60s107.7-22.9,144.7-60c37-37,60-88.2,60-144.7v-368H396.6z"
    static let svgPathC = "M751.7,827.6c-71.4-71.4-71.4-187.7,0-259.1s187.7-71.4,259.1,0l0,0l72.9-72.9l0,0c-111.8-111.8-293.1-111.8-404.9,0C567,607.4,567,788.7,678.9,900.5c111.8,111.8,293.1,111.8,404.9,0l-72.9-72.9C939.4,899,823.2,899,751.7,827.6z"
    static let svgPathShieldTeal = "M2188,377.7c-23.7-13.4-48.3-25.6-73.8-36.3c4,26,6.7,52.3,7.9,79.1v0.2c0.7,13.1,1,26.1,1,39.2c0,7.7-0.2,15.5-0.5,23.2c-0.5,26-2.6,51.6-5.8,76.8c-2.4,20.1-5.8,40-9.8,59.8c-5.2,24.8-11.3,49-18.7,72.9c-19.3,63.1-46.4,122.7-79.9,177.9c-12,19.8-24.9,38.8-38.7,57.4c-10.7,14.8-22,29.2-34,43.1c-14.8,17.5-30.4,34.4-46.8,50.5c-13.1,13.1-26.6,25.8-40.7,37.6c-17.5,15.3-35.9,29.7-54.8,43.1c17.5,15.8,35.9,30.8,54.8,45.2c18.9-14.3,37.3-29.2,55-45c14.1-12.4,27.7-25.3,40.7-38.7c16.3-16.3,31.8-33.3,46.8-51.1c11.9-14.1,23.2-28.5,34.2-43.5c13.6-18.4,26.3-37.3,38.5-56.9c31.6-51.1,58.3-105.9,78.7-163.5c9.1-25.3,17-51.2,23.7-77.7c5.3-21,9.8-42.3,13.4-63.8c4.6-26.5,8.1-53.5,10.1-80.8c1.7-21.7,2.6-43.7,2.6-65.7v-1.2C2192,432.2,2190.6,404.7,2188,377.7z"
    static let svgPathShieldCyan = "M1760.6,971c-12-13.9-23.4-28.4-34-43.1c-13.8-18.6-26.6-37.6-38.7-57.4c-33.5-55.2-60.7-114.8-79.9-177.9c-7.4-23.9-13.6-48.1-18.7-72.9c-4-19.8-7.4-39.7-9.8-59.8c-3.3-25.3-5.3-50.9-5.8-76.8c-0.3-7.7-0.5-15.5-0.5-23.2c0-13.1,0.3-26.1,1-39.2v-0.2c18.4-9.1,37.3-17.4,56.7-24.6c23.2-8.6,47.1-16,71.5-22c18.7-4.5,37.8-8.1,57.2-10.8c23.2-3.4,46.8-5.5,70.8-6.2c5.8-0.2,11.9-0.2,17.7-0.2s11.9,0,17.7,0.2c24.1,0.7,47.6,2.7,70.8,6.2c19.4,2.8,38.5,6.4,57.3,10.8c24.4,6,48.3,13.4,71.5,22c-2.2-25.8-5.8-51.2-10.7-76.3c-24.2-7.7-49-13.9-74.4-18.9c-19.4-3.8-39.2-6.7-59.3-8.8c-23.9-2.6-48-4-72.6-4c-0.2,0-0.2,0-0.3,0s-0.2,0-0.3,0c-24.6,0-48.7,1.4-72.5,4c-20.1,2.1-39.9,5-59.3,8.8c-25.4,5-50.2,11.2-74.4,18.9c-20.3,6.4-40.1,13.6-59.5,21.8h-0.2c-25.4,10.7-49.9,22.9-73.6,36.3c-2.6,27-4,54.5-4,82.2v1.2c0,22,0.9,44,2.6,65.7c2.1,27.3,5.5,54.3,10.1,80.8c3.6,21.5,8.1,42.6,13.6,63.6c6.5,26.5,14.4,52.4,23.6,77.7c20.5,57.6,47.1,112.4,78.9,163.5c12,19.6,24.8,38.5,38.5,56.9c10.8,15,22.2,29.4,34.2,43.5c14.8,17.7,30.4,34.9,46.6,51.2c19.1-13.1,37.5-27.3,55-42.3C1791,1005.4,1775.4,988.6,1760.6,971z"
    static let svgPathShieldCrest1 = "M1848.1,789.4c14.3-22.3,27.2-45.7,38.5-70c16.2-34,29.1-70,38.7-107.1c5.5-22,10-44.5,13.1-67.6c-22.2-4.6-44.9-7.6-68.1-8.8c-7.4-0.3-14.8-0.5-22.2-0.5s-14.8,0.2-22.2,0.5c-23.2,1.2-45.9,4.1-68.1,8.8c3.1,23,7.6,45.6,13.1,67.6c9.6,37.1,22.5,73.1,38.7,107.1C1821,743.6,1833.8,767,1848.1,789.4z"
    static let svgPathShieldCrest2 = "M1704.2,559.7c-22.5,7.6-44.4,17.2-65,28.4c4.1,23.9,9.5,47.3,15.8,70.3c17.2,61.2,42.1,119.3,73.6,172.9c11.9,20.5,24.8,40.2,38.5,59.1c10.8,15.1,22.4,29.6,34.2,43.7c14.8,17.5,30.4,34.4,46.8,50.4c16.3-16,32-32.8,46.8-50.4c11.9-14.1,23.4-28.5,34.2-43.7c13.8-18.9,26.6-38.7,38.5-59.1c31.5-53.6,56.4-111.7,73.6-172.9c6.4-23,11.7-46.4,15.8-70.3c3.4-19.1,6-38.2,7.9-57.6v-0.2c2.1-23.2,3.3-46.8,3.3-70.5v-3.6c-22.4-9.5-45.6-17.5-69.3-24.2c-18.2-5-37-9.1-55.9-12.4c-22.7-3.8-45.9-6.4-69.5-7.4c-8.4-0.3-16.8-0.5-25.4-0.5c-8.4,0-17,0.2-25.4,0.5c-23.6,1-46.8,3.6-69.5,7.6c-19.1,3.1-37.6,7.2-55.9,12.2v0.2c-23.9,6.5-46.9,14.6-69.3,24.2v3.4c0,23.7,1.2,47.3,3.3,70.5c21.3-10.5,43.7-19.4,66.9-26.8c17.7-5.5,35.9-10.1,54.5-13.8c22.2-4.5,45-7.2,68.4-8.4c8.9-0.7,17.9-0.9,27-0.9s18.1,0.2,27,0.9c23.4,1.2,46.2,4,68.4,8.4c18.6,3.6,36.8,8.3,54.5,13.8c-1.2,18.9-3.3,37.6-6,56v0.2c-3.4,23.2-8.1,45.7-13.9,67.9c-13.2,51.6-32.7,100.7-57.3,146.6c-11.7,21.8-24.6,43-38.5,63.3c-10.7,15.6-22.2,30.8-34.2,45.5c-12-14.8-23.6-29.9-34.2-45.5c-13.9-20.3-26.8-41.4-38.5-63.3c-24.6-45.9-44-95.1-57.3-146.6C1712.3,605.4,1707.7,582.9,1704.2,559.7z"
    static let svgPathTmLetterT = "M2122.4,920.2v6.3h17.7v58h6.9v-58h17.6v-6.3H2122.4z"
    static let svgPathTmLetterM = "M2232.7,920.2 2232.7,984.5 2225.7,984.5 2225.7,933.4 2204.3,964.7 2203.7,964.7 2182.3,933.2 2182.3,984.5 2175.2,984.5 2175.2,920.2 2181.4,920.2 2204,953.2 2226.6,920.2"

    // Lazily parsed exact CGPaths
    static let cachedPathJ: CGPath = SvgPathParser.parse(svgPathJ)
    static let cachedPathC: CGPath = SvgPathParser.parse(svgPathC)
    static let cachedPathShieldTeal: CGPath = SvgPathParser.parse(svgPathShieldTeal)
    static let cachedPathShieldCyan: CGPath = SvgPathParser.parse(svgPathShieldCyan)
    static let cachedPathShieldCrest1: CGPath = SvgPathParser.parse(svgPathShieldCrest1)
    static let cachedPathShieldCrest2: CGPath = SvgPathParser.parse(svgPathShieldCrest2)
    static let cachedPathTmLetterT: CGPath = SvgPathParser.parse(svgPathTmLetterT)
    static let cachedPathTmLetterM: CGPath = SvgPathParser.parse(svgPathTmLetterM)

    // Registered font tracking
    private static var isFontRegistered = false

    private static func registerFontIfNeeded() {
        guard !isFontRegistered else { return }
        isFontRegistered = true

        let fontNames = ["plus_jakarta_sans_bold", "plus_jakarta_sans_medium"]
        for fontName in fontNames {
            if let fontURL = Bundle.main.url(forResource: fontName, withExtension: "ttf"),
               let fontDataProvider = CGDataProvider(url: fontURL as CFURL),
               let font = CGFont(fontDataProvider) {
                var error: Unmanaged<CFError>?
                CTFontManagerRegisterGraphicsFont(font, &error)
            }
        }
    }

    private static func getApprovedFont(size: CGFloat, weight: UIFont.Weight = .bold) -> UIFont {
        registerFontIfNeeded()
        if let customFont = UIFont(name: "PlusJakartaSans-Bold", size: size) {
            return customFont
        }
        if let customFontAlt = UIFont(name: "Plus Jakarta Sans Bold", size: size) {
            return customFontAlt
        }
        return UIFont.systemFont(ofSize: size, weight: weight)
    }

    /**
     * Converts a string into Proper Case (Title Case with only the first letter capitalized per word)
     * as required by JCI Brand Guidelines 2026 Page 12 ("Location Name in Proper Case").
     */
    static func toProperCase(_ string: String) -> String {
        let trimmed = string.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return "" }

        return trimmed.components(separatedBy: " ")
            .filter { !$0.isEmpty }
            .map { word -> String in
                let lower = word.lowercased()
                if lower == "jci" { return "JCI" }
                if lower == "lo" { return "LO" }
                return lower.prefix(1).uppercased() + lower.dropFirst()
            }
            .joined(separator: " ")
    }

    /**
     * Generates a high-resolution UIImage bitmap strictly matching JCI Brand Guidelines 2026.
     */
    static func generateLogoBitmap(
        caseStyle: CaseStyle,
        line1: String,
        line2: String = "",
        isSecondaryLogo: Bool = false,
        isTransparentBg: Bool = false,
        width: CGFloat = 2400,
        height: CGFloat = 1600
    ) -> UIImage {
        let size = CGSize(width: width, height: height)
        let rendererFormat = UIGraphicsImageRendererFormat.default()
        rendererFormat.scale = 1.0
        rendererFormat.opaque = !isTransparentBg

        let renderer = UIGraphicsImageRenderer(size: size, format: rendererFormat)
        return renderer.image { ctx in
            let cgCtx = ctx.cgContext

            // 1. Solid / Transparent Background
            if !isTransparentBg {
                let bgColor: UIColor
                switch caseStyle {
                case .defaultColor, .monochromeBlack:
                    bgColor = jciWhite
                case .inverted:
                    bgColor = jciBlack
                case .monochromeWhite:
                    bgColor = jciBlue
                case .socialBlue:
                    bgColor = jciWhite
                case .socialDark:
                    bgColor = jciWhite
                case .socialWhite:
                    bgColor = jciBlue
                }
                bgColor.setFill()
                cgCtx.fill(CGRect(origin: .zero, size: size))
            }

            // 2. Handle Standalone Shield Social Profile Circle (Page 13)
            if caseStyle.isSocial {
                drawSocialCircleEmblem(
                    cgCtx: cgCtx,
                    size: size,
                    caseStyle: caseStyle,
                    line1: line1,
                    line2: line2
                )
                return
            }

            // 3. Determine Brand Colors for the 4 Official Colourways
            let wordmarkColor: UIColor
            let shieldTealColor: UIColor
            let shieldCyanColor: UIColor
            let locationTextColor: UIColor

            switch caseStyle {
            case .defaultColor:
                wordmarkColor = jciBlack
                shieldTealColor = jciTeal
                shieldCyanColor = jciBlue
                locationTextColor = jciBlue

            case .inverted:
                wordmarkColor = jciWhite
                shieldTealColor = jciTeal
                shieldCyanColor = jciWhite
                locationTextColor = jciWhite

            case .monochromeBlack:
                wordmarkColor = UIColor.black
                shieldTealColor = UIColor.black
                shieldCyanColor = UIColor.black
                locationTextColor = UIColor.black

            case .monochromeWhite:
                wordmarkColor = jciWhite
                shieldTealColor = jciWhite
                shieldCyanColor = jciWhite
                locationTextColor = jciWhite

            default:
                wordmarkColor = jciBlack
                shieldTealColor = jciTeal
                shieldCyanColor = jciBlue
                locationTextColor = jciBlue
            }

            // Format location names into Proper Case
            let cleanLine1 = toProperCase(line1.isEmpty ? "Exemple" : line1)
            let cleanLine2 = toProperCase(line2)

            // 4. Scale & Offset calculation based on 2400x1600 viewBox
            let scale = width / 2400.0
            let jciTranslateY: CGFloat = (!cleanLine2.isEmpty && !cleanLine1.isEmpty) ? (-100.0 * scale) : 0.0

            // 5. Draw Official Vector Mark (JCI + Shield + TM)
            drawOfficialVectorMark(
                cgCtx: cgCtx,
                scale: scale,
                translateY: jciTranslateY,
                mainColor: wordmarkColor,
                tealColor: shieldTealColor,
                cyanColor: shieldCyanColor
            )

            // 6. Draw Secondary Full Name if enabled (Page 10)
            let textStartX = 185.0 * scale
            if isSecondaryLogo {
                let secondaryFont = getApprovedFont(size: 42.0 * scale, weight: .semibold)
                let secAttrs: [NSAttributedString.Key: Any] = [
                    .font: secondaryFont,
                    .foregroundColor: wordmarkColor.withAlphaComponent(0.85)
                ]
                let secRect = CGRect(x: textStartX, y: (1030.0 * scale) + jciTranslateY, width: size.width - textStartX - 40, height: 50.0 * scale)
                ("Junior Chamber International" as NSString).draw(in: secRect, withAttributes: secAttrs)
            }

            // 7. Draw National & Local Organisation Location Name (Pages 11 & 12)
            let paragraphStyle = NSMutableParagraphStyle()
            paragraphStyle.alignment = .left

            if cleanLine2.isEmpty {
                let fontSize = 120.0 * scale
                let font = getApprovedFont(size: fontSize, weight: .bold)
                let attrs: [NSAttributedString.Key: Any] = [
                    .font: font,
                    .foregroundColor: locationTextColor,
                    .paragraphStyle: paragraphStyle
                ]
                let baselineY = isSecondaryLogo ? ((1150.0 * scale) + jciTranslateY) : ((1200.0 * scale) + jciTranslateY)
                let rect = CGRect(x: textStartX, y: baselineY, width: size.width - textStartX - 40, height: 160.0 * scale)
                (cleanLine1 as NSString).draw(in: rect, withAttributes: attrs)
            } else {
                let fontSize = 110.0 * scale
                let font = getApprovedFont(size: fontSize, weight: .bold)
                let attrs: [NSAttributedString.Key: Any] = [
                    .font: font,
                    .foregroundColor: locationTextColor,
                    .paragraphStyle: paragraphStyle
                ]
                let line1Y = isSecondaryLogo ? ((1110.0 * scale) + jciTranslateY) : ((1140.0 * scale) + jciTranslateY)
                let line2Y = line1Y + (130.0 * scale)
                let rect1 = CGRect(x: textStartX, y: line1Y, width: size.width - textStartX - 40, height: 140.0 * scale)
                let rect2 = CGRect(x: textStartX, y: line2Y, width: size.width - textStartX - 40, height: 140.0 * scale)
                (cleanLine1 as NSString).draw(in: rect1, withAttributes: attrs)
                (cleanLine2 as NSString).draw(in: rect2, withAttributes: attrs)
            }
        }
    }

    /**
     * Draws the exact JCI Corporate Identity vector marks using official paths parsed directly from SVG
     */
    private static func drawOfficialVectorMark(
        cgCtx: CGContext,
        scale: CGFloat,
        translateY: CGFloat,
        offsetX: CGFloat = 0,
        mainColor: UIColor,
        tealColor: UIColor,
        cyanColor: UIColor
    ) {
        cgCtx.saveGState()
        cgCtx.translateBy(x: offsetX, y: translateY)
        cgCtx.scaleBy(x: scale, y: scale)

        // 1. Letters J, C, I (Rectangle)
        mainColor.setFill()
        cgCtx.addPath(cachedPathJ)
        cgCtx.fillPath()

        cgCtx.addPath(cachedPathC)
        cgCtx.fillPath()

        let rectI = CGRect(x: 1187.1, y: 411.8, width: 103.2, height: 572.7)
        cgCtx.fill(rectI)

        // 2. Official JCI Emblem Shield / Crest (4 exact geometric components)
        tealColor.setFill()
        cgCtx.addPath(cachedPathShieldTeal)
        cgCtx.fillPath()

        cyanColor.setFill()
        cgCtx.addPath(cachedPathShieldCyan)
        cgCtx.fillPath()

        mainColor.setFill()
        cgCtx.addPath(cachedPathShieldCrest1)
        cgCtx.fillPath()

        cgCtx.addPath(cachedPathShieldCrest2)
        cgCtx.fillPath()

        // 3. TM Trademark symbol
        cgCtx.addPath(cachedPathTmLetterT)
        cgCtx.fillPath()

        cgCtx.addPath(cachedPathTmLetterM)
        cgCtx.fillPath()

        cgCtx.restoreGState()
    }

    /**
     * Draws the Standalone Shield Social Profile Circle Icon (Page 13) matching Android 1-on-1
     */
    private static func drawSocialCircleEmblem(
        cgCtx: CGContext,
        size: CGSize,
        caseStyle: CaseStyle,
        line1: String,
        line2: String
    ) {
        let circleRadius = size.width * 0.42
        let circleCenterX = size.width / 2.0
        let circleCenterY = size.height / 2.0

        let circleBgColor: UIColor = {
            switch caseStyle {
            case .socialWhite: return jciWhite
            case .socialDark: return jciBlack
            case .socialBlue: return jciBlue
            default: return jciWhite
            }
        }()

        circleBgColor.setFill()
        let circleRect = CGRect(
            x: circleCenterX - circleRadius,
            y: circleCenterY - circleRadius,
            width: circleRadius * 2,
            height: circleRadius * 2
        )
        cgCtx.fillEllipse(in: circleRect)

        // Clip drawing to circular badge
        cgCtx.saveGState()
        cgCtx.addEllipse(in: circleRect)
        cgCtx.clip()

        // Scale and center the JCI corporate mark inside the circular badge
        let markScale = (circleRadius * 1.05) / 2400.0
        let markOffsetX = circleCenterX - (2400.0 * markScale / 2.0)
        let markOffsetY = circleCenterY - (1600.0 * markScale / 2.0) - (circleRadius * 0.16)

        let primaryColor: UIColor = {
            switch caseStyle {
            case .socialWhite: return jciBlue
            case .socialDark: return jciWhite
            case .socialBlue: return jciWhite
            default: return jciBlue
            }
        }()

        drawOfficialVectorMark(
            cgCtx: cgCtx,
            scale: markScale,
            translateY: markOffsetY,
            offsetX: markOffsetX,
            mainColor: primaryColor,
            tealColor: (caseStyle == .socialWhite) ? jciTeal : primaryColor,
            cyanColor: primaryColor
        )

        // Centered Chapter Name inside circle lower section
        let displayLine1 = toProperCase(line1.isEmpty ? "Exemple" : line1)
        let displayLine2 = toProperCase(line2)

        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.alignment = .center

        if displayLine2.isEmpty {
            let font = getApprovedFont(size: circleRadius * 0.16, weight: .bold)
            let attrs: [NSAttributedString.Key: Any] = [
                .font: font,
                .foregroundColor: primaryColor,
                .paragraphStyle: paragraphStyle
            ]
            let textRect = CGRect(
                x: circleCenterX - circleRadius,
                y: circleCenterY + (circleRadius * 0.52),
                width: circleRadius * 2,
                height: circleRadius * 0.28
            )
            (displayLine1 as NSString).draw(in: textRect, withAttributes: attrs)
        } else {
            let font = getApprovedFont(size: circleRadius * 0.13, weight: .bold)
            let attrs: [NSAttributedString.Key: Any] = [
                .font: font,
                .foregroundColor: primaryColor,
                .paragraphStyle: paragraphStyle
            ]
            let textRect1 = CGRect(
                x: circleCenterX - circleRadius,
                y: circleCenterY + (circleRadius * 0.44),
                width: circleRadius * 2,
                height: circleRadius * 0.22
            )
            let textRect2 = CGRect(
                x: circleCenterX - circleRadius,
                y: circleCenterY + (circleRadius * 0.64),
                width: circleRadius * 2,
                height: circleRadius * 0.22
            )
            (displayLine1 as NSString).draw(in: textRect1, withAttributes: attrs)
            (displayLine2 as NSString).draw(in: textRect2, withAttributes: attrs)
        }

        cgCtx.restoreGState()
    }

    /**
     * Generates a 2400x1600 Scalable Vector Graphics (SVG) string conforming to JCI 2026 specs.
     */
    static func generateSvgString(
        line1: String,
        line2: String = "",
        isSecondary: Bool = false,
        caseStyle: CaseStyle = .defaultColor
    ) -> String {
        let displayLine1 = toProperCase(line1.isEmpty ? "Exemple" : line1)
        let displayLine2 = toProperCase(line2)

        let mainColor: String
        let tealColor: String
        let cyanColor: String
        let textColor: String
        let bgColor: String

        switch caseStyle {
        case .defaultColor:
            mainColor = "#130F2D"
            tealColor = "#57BCBC"
            cyanColor = "#0097D7"
            textColor = "#0097D7"
            bgColor = "#FFFFFF"
        case .inverted:
            mainColor = "#FFFFFF"
            tealColor = "#57BCBC"
            cyanColor = "#FFFFFF"
            textColor = "#FFFFFF"
            bgColor = "#130F2D"
        case .monochromeBlack:
            mainColor = "#000000"
            tealColor = "#000000"
            cyanColor = "#000000"
            textColor = "#000000"
            bgColor = "#FFFFFF"
        case .monochromeWhite:
            mainColor = "#FFFFFF"
            tealColor = "#FFFFFF"
            cyanColor = "#FFFFFF"
            textColor = "#FFFFFF"
            bgColor = "#0097D7"
        default:
            mainColor = "#130F2D"
            tealColor = "#57BCBC"
            cyanColor = "#0097D7"
            textColor = "#0097D7"
            bgColor = "#FFFFFF"
        }

        let jciTranslateY = (!displayLine2.isEmpty && !displayLine1.isEmpty) ? -100 : 0

        return """
        <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2400 1600" style="background:\(bgColor); enable-background:new 0 0 2400 1600;">
          <defs>
            <style type="text/css">
              @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@700&amp;display=swap');
              .jci-text { font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-weight: 700; fill: \(textColor); }
              .jci-subtext { font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-weight: 600; font-size: 42px; fill: \(mainColor); }
            </style>
          </defs>
          <g transform="translate(0, \(jciTranslateY))">
            <!-- JCI Wordmark (J, C, I) -->
            <rect fill="\(mainColor)" x="1187.1" y="411.8" width="103.2" height="572.7"/>
            <path fill="\(mainColor)" d="M396.6,411.8v368c0,55.9-45.6,101.5-101.5,101.5c-28,0-53.3-11.4-71.7-29.8l-73,73c37,37,88.2,60,144.7,60s107.7-22.9,144.7-60c37-37,60-88.2,60-144.7v-368H396.6z"/>
            <path fill="\(mainColor)" d="M751.7,827.6c-71.4-71.4-71.4-187.7,0-259.1s187.7-71.4,259.1,0l0,0l72.9-72.9l0,0c-111.8-111.8-293.1-111.8-404.9,0C567,607.4,567,788.7,678.9,900.5c111.8,111.8,293.1,111.8,404.9,0l-72.9-72.9C939.4,899,823.2,899,751.7,827.6z"/>

            <!-- Official JCI Emblem Shield (4 components) -->
            <path fill="\(tealColor)" d="\(svgPathShieldTeal)"/>
            <path fill="\(cyanColor)" d="\(svgPathShieldCyan)"/>
            <path fill="\(mainColor)" d="\(svgPathShieldCrest1)"/>
            <path fill="\(mainColor)" d="\(svgPathShieldCrest2)"/>

            <!-- TM Symbol -->
            <path fill="\(mainColor)" d="\(svgPathTmLetterT)"/>
            <polygon fill="\(mainColor)" points="2232.7,920.2 2232.7,984.5 2225.7,984.5 2225.7,933.4 2204.3,964.7 2203.7,964.7 2182.3,933.2 2182.3,984.5 2175.2,984.5 2175.2,920.2 2181.4,920.2 2204,953.2 2226.6,920.2"/>

            <!-- Secondary Subtitle (Page 10) -->
            \(isSecondary ? "<text x=\"185\" y=\"1030\" class=\"jci-subtext\">Junior Chamber International</text>" : "")

            <!-- Location Typography (Page 11-12) -->
            <text x="185" y="\(isSecondary ? "1160" : "1200")" class="jci-text" font-size="120">\(displayLine1)</text>
            \(displayLine2.isEmpty ? "" : "<text x=\"185\" y=\"\(isSecondary ? "1300" : "1350")\" class=\"jci-text\" font-size=\"105\">\(displayLine2)</text>")
          </g>
        </svg>
        """
    }
}

/**
 * High-performance, zero-dependency SVG Path Parser for iOS CoreGraphics
 * Parses SVG path strings (M, m, L, l, H, h, V, v, C, c, S, s, Z, z) into CGPath.
 */
class SvgPathParser {
    static func parse(_ d: String) -> CGPath {
        let path = CGMutablePath()
        var index = d.startIndex
        var currentPoint = CGPoint.zero
        var lastControlPoint: CGPoint? = nil
        var currentCommand: Character = " "

        func skipWhitespaceAndCommas() {
            while index < d.endIndex && (d[index].isWhitespace || d[index] == ",") {
                index = d.index(after: index)
            }
        }

        func readNumber() -> CGFloat? {
            skipWhitespaceAndCommas()
            guard index < d.endIndex else { return nil }

            var sign = 1.0
            if d[index] == "-" {
                sign = -1.0
                index = d.index(after: index)
            } else if d[index] == "+" {
                index = d.index(after: index)
            }

            guard index < d.endIndex else { return nil }

            var numStr = ""
            var hasDot = false
            while index < d.endIndex {
                let ch = d[index]
                if ch.isNumber {
                    numStr.append(ch)
                    index = d.index(after: index)
                } else if ch == "." && !hasDot {
                    hasDot = true
                    numStr.append(ch)
                    index = d.index(after: index)
                } else {
                    break
                }
            }

            guard !numStr.isEmpty, let val = Double(numStr) else { return nil }
            return CGFloat(sign * val)
        }

        while index < d.endIndex {
            skipWhitespaceAndCommas()
            guard index < d.endIndex else { break }

            let ch = d[index]
            if ch.isLetter {
                currentCommand = ch
                index = d.index(after: index)
            }

            switch currentCommand {
            case "M":
                guard let x = readNumber(), let y = readNumber() else { break }
                currentPoint = CGPoint(x: x, y: y)
                path.move(to: currentPoint)
                lastControlPoint = nil
                currentCommand = "L"
            case "m":
                guard let dx = readNumber(), let dy = readNumber() else { break }
                currentPoint = CGPoint(x: currentPoint.x + dx, y: currentPoint.y + dy)
                path.move(to: currentPoint)
                lastControlPoint = nil
                currentCommand = "l"
            case "L":
                guard let x = readNumber(), let y = readNumber() else { break }
                currentPoint = CGPoint(x: x, y: y)
                path.addLine(to: currentPoint)
                lastControlPoint = nil
            case "l":
                guard let dx = readNumber(), let dy = readNumber() else { break }
                currentPoint = CGPoint(x: currentPoint.x + dx, y: currentPoint.y + dy)
                path.addLine(to: currentPoint)
                lastControlPoint = nil
            case "H":
                guard let x = readNumber() else { break }
                currentPoint = CGPoint(x: x, y: currentPoint.y)
                path.addLine(to: currentPoint)
                lastControlPoint = nil
            case "h":
                guard let dx = readNumber() else { break }
                currentPoint = CGPoint(x: currentPoint.x + dx, y: currentPoint.y)
                path.addLine(to: currentPoint)
                lastControlPoint = nil
            case "V":
                guard let y = readNumber() else { break }
                currentPoint = CGPoint(x: currentPoint.x, y: y)
                path.addLine(to: currentPoint)
                lastControlPoint = nil
            case "v":
                guard let dy = readNumber() else { break }
                currentPoint = CGPoint(x: currentPoint.x, y: currentPoint.y + dy)
                path.addLine(to: currentPoint)
                lastControlPoint = nil
            case "C":
                guard let x1 = readNumber(), let y1 = readNumber(),
                      let x2 = readNumber(), let y2 = readNumber(),
                      let x = readNumber(), let y = readNumber() else { break }
                let cp1 = CGPoint(x: x1, y: y1)
                let cp2 = CGPoint(x: x2, y: y2)
                currentPoint = CGPoint(x: x, y: y)
                path.addCurve(to: currentPoint, control1: cp1, control2: cp2)
                lastControlPoint = cp2
            case "c":
                guard let dx1 = readNumber(), let dy1 = readNumber(),
                      let dx2 = readNumber(), let dy2 = readNumber(),
                      let dx = readNumber(), let dy = readNumber() else { break }
                let cp1 = CGPoint(x: currentPoint.x + dx1, y: currentPoint.y + dy1)
                let cp2 = CGPoint(x: currentPoint.x + dx2, y: currentPoint.y + dy2)
                currentPoint = CGPoint(x: currentPoint.x + dx, y: currentPoint.y + dy)
                path.addCurve(to: currentPoint, control1: cp1, control2: cp2)
                lastControlPoint = cp2
            case "S":
                guard let x2 = readNumber(), let y2 = readNumber(),
                      let x = readNumber(), let y = readNumber() else { break }
                let cp1: CGPoint
                if let last = lastControlPoint {
                    cp1 = CGPoint(x: 2 * currentPoint.x - last.x, y: 2 * currentPoint.y - last.y)
                } else {
                    cp1 = currentPoint
                }
                let cp2 = CGPoint(x: x2, y: y2)
                currentPoint = CGPoint(x: x, y: y)
                path.addCurve(to: currentPoint, control1: cp1, control2: cp2)
                lastControlPoint = cp2
            case "s":
                guard let dx2 = readNumber(), let dy2 = readNumber(),
                      let dx = readNumber(), let dy = readNumber() else { break }
                let cp1: CGPoint
                if let last = lastControlPoint {
                    cp1 = CGPoint(x: 2 * currentPoint.x - last.x, y: 2 * currentPoint.y - last.y)
                } else {
                    cp1 = currentPoint
                }
                let cp2 = CGPoint(x: currentPoint.x + dx2, y: currentPoint.y + dy2)
                currentPoint = CGPoint(x: currentPoint.x + dx, y: currentPoint.y + dy)
                path.addCurve(to: currentPoint, control1: cp1, control2: cp2)
                lastControlPoint = cp2
            case "Z", "z":
                path.closeSubpath()
                lastControlPoint = nil
            default:
                index = d.index(after: index)
            }
        }
        return path
    }
}
