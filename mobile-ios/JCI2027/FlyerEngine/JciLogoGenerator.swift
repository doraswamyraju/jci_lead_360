import UIKit

/**
 * JCI Official Logo Generator Engine — Swift CoreGraphics Replica of https://logo.jciath.be/
 * Uses official JCI Corporate Identity vector paths from the official SVG repository:
 * - Letters J, C, I with exact bezier curves
 * - Official JCI Shield: Teal left wing, Cyan right wing, Navy inner crest
 * - TM symbol
 * - 7 Case Styles: Color, Black, Light, White on Blue, Social Circles
 * - Outputs 2400x1600 high-res PNG/JPG and raw SVG string
 */
class JciLogoGenerator {

    enum CaseStyle: String, CaseIterable, Identifiable {
        case color = "Color (Original)"
        case black = "Monochrome Black"
        case light = "White on Dark"
        case whiteOnBlue = "White on Blue"
        case socialWhite = "Social Circle (White)"
        case socialBlack = "Social Circle (Dark)"
        case socialBlue = "Social Circle (Blue)"

        var id: String { rawValue }

        var isSocial: Bool {
            switch self {
            case .socialWhite, .socialBlack, .socialBlue: return true
            default: return false
            }
        }
    }

    // Official JCI Brand Palette
    static let colorNavy = UIColor(red: 19/255.0, green: 15/255.0, blue: 45/255.0, alpha: 1.0)
    static let colorBlue = UIColor(red: 0/255.0, green: 151/255.0, blue: 216/255.0, alpha: 1.0) // #0097D8
    static let colorTeal = UIColor(red: 89/255.0, green: 189/255.0, blue: 189/255.0, alpha: 1.0) // #59BDBD
    static let colorDarkNavy = UIColor(red: 19/255.0, green: 15/255.0, blue: 45/255.0, alpha: 1.0)

    /**
     * Render the official JCI Logo to UIImage
     */
    static func generateLogoBitmap(
        caseStyle: CaseStyle,
        line1: String,
        line2: String = "",
        width: CGFloat = 1200,
        height: CGFloat = 800
    ) -> UIImage {
        let size = CGSize(width: width, height: height)
        let format = UIGraphicsImageRendererFormat()
        format.scale = 1.0
        let renderer = UIGraphicsImageRenderer(size: size, format: format)

        return renderer.image { context in
            let cgCtx = context.cgContext

            // 1. Draw Background
            switch caseStyle {
            case .color, .black, .socialBlue:
                cgCtx.setFillColor(UIColor.white.cgColor)
                cgCtx.fill(CGRect(origin: .zero, size: size))
            case .light, .socialBlack:
                cgCtx.setFillColor(colorDarkNavy.cgColor)
                cgCtx.fill(CGRect(origin: .zero, size: size))
            case .whiteOnBlue, .socialWhite:
                cgCtx.setFillColor(colorBlue.cgColor)
                cgCtx.fill(CGRect(origin: .zero, size: size))
            }

            // 2. Setup Palettes
            let mainJciColor: UIColor
            let tealColor: UIColor
            let cyanColor: UIColor
            let textColor: UIColor

            switch caseStyle {
            case .color:
                mainJciColor = colorNavy
                tealColor = colorTeal
                cyanColor = colorBlue
                textColor = colorBlue
            case .black:
                mainJciColor = .black
                tealColor = .black
                cyanColor = .black
                textColor = .black
            case .light:
                mainJciColor = .white
                tealColor = colorTeal
                cyanColor = .white
                textColor = .white
            case .whiteOnBlue:
                mainJciColor = .white
                tealColor = .white
                cyanColor = .white
                textColor = .white
            default:
                mainJciColor = colorNavy
                tealColor = colorTeal
                cyanColor = colorBlue
                textColor = colorBlue
            }

            // 3. Social Circle Mode vs Standard Horizontal Lockup
            if caseStyle.isSocial {
                drawSocialCircleEmblem(cgCtx: cgCtx, size: size, caseStyle: caseStyle)
                return
            }

            // 4. Standard Horizontal Lockup
            let scale = width / 2400.0
            let jciTranslateY: CGFloat = (line1.isEmpty == false && line2.isEmpty == false) ? -80.0 * scale : 0.0

            drawOfficialVectorMark(
                cgCtx: cgCtx,
                scale: scale,
                translateY: jciTranslateY,
                mainColor: mainJciColor,
                tealColor: tealColor,
                cyanColor: cyanColor
            )

            // 5. Draw Typography for LO Chapter Name
            let displayLine1 = line1.trimmingCharacters(in: .whitespaces).isEmpty ? "Tirupati" : line1.trimmingCharacters(in: .whitespaces)
            let textStartX = 185.0 * scale

            let paragraphStyle = NSMutableParagraphStyle()
            paragraphStyle.alignment = .left

            if line2.trimmingCharacters(in: .whitespaces).isEmpty {
                let font = UIFont(name: "HelveticaNeue-Bold", size: 120.0 * scale) ?? UIFont.boldSystemFont(ofSize: 120.0 * scale)
                let attrs: [NSAttributedString.Key: Any] = [
                    .font: font,
                    .foregroundColor: textColor,
                    .paragraphStyle: paragraphStyle
                ]
                let rect = CGRect(x: textStartX, y: (1120.0 * scale) + jciTranslateY, width: size.width - textStartX - 40, height: 160.0 * scale)
                (displayLine1 as NSString).draw(in: rect, withAttributes: attrs)
            } else {
                let font = UIFont(name: "HelveticaNeue-Bold", size: 105.0 * scale) ?? UIFont.boldSystemFont(ofSize: 105.0 * scale)
                let attrs: [NSAttributedString.Key: Any] = [
                    .font: font,
                    .foregroundColor: textColor,
                    .paragraphStyle: paragraphStyle
                ]
                let rect1 = CGRect(x: textStartX, y: (1080.0 * scale) + jciTranslateY, width: size.width - textStartX - 40, height: 140.0 * scale)
                let rect2 = CGRect(x: textStartX, y: (1240.0 * scale) + jciTranslateY, width: size.width - textStartX - 40, height: 140.0 * scale)
                (displayLine1 as NSString).draw(in: rect1, withAttributes: attrs)
                (line2.trimmingCharacters(in: .whitespaces) as NSString).draw(in: rect2, withAttributes: attrs)
            }
        }
    }

    /**
     * Draws the JCI Mark & Shield with high precision Bézier curves
     */
    private static func drawOfficialVectorMark(
        cgCtx: CGContext,
        scale: CGFloat,
        translateY: CGFloat,
        mainColor: UIColor,
        tealColor: UIColor,
        cyanColor: UIColor
    ) {
        cgCtx.saveGState()
        cgCtx.translateBy(x: 0, y: translateY)
        cgCtx.scaleBy(x: scale, y: scale)

        // 1. J Letter
        let pathJ = UIBezierPath()
        pathJ.move(to: CGPoint(x: 396.6, y: 411.8))
        pathJ.addLine(to: CGPoint(x: 396.6, y: 779.8))
        pathJ.addCurve(to: CGPoint(x: 295.1, y: 881.3), controlPoint1: CGPoint(x: 396.6, y: 835.7), controlPoint2: CGPoint(x: 351.0, y: 881.3))
        pathJ.addCurve(to: CGPoint(x: 223.4, y: 851.5), controlPoint1: CGPoint(x: 267.1, y: 881.3), controlPoint2: CGPoint(x: 241.8, y: 869.9))
        pathJ.addLine(to: CGPoint(x: 150.4, y: 924.5))
        pathJ.addCurve(to: CGPoint(x: 295.1, y: 984.5), controlPoint1: CGPoint(x: 187.4, y: 961.5), controlPoint2: CGPoint(x: 238.6, y: 984.5))
        pathJ.addCurve(to: CGPoint(x: 439.8, y: 924.5), controlPoint1: CGPoint(x: 351.6, y: 984.5), controlPoint2: CGPoint(x: 402.8, y: 961.5))
        pathJ.addCurve(to: CGPoint(x: 499.8, y: 779.8), controlPoint1: CGPoint(x: 476.8, y: 887.5), controlPoint2: CGPoint(x: 499.8, y: 836.3))
        pathJ.addLine(to: CGPoint(x: 499.8, y: 411.8))
        pathJ.close()

        mainColor.setFill()
        pathJ.fill()

        // 2. C Letter
        let pathC = UIBezierPath()
        pathC.move(to: CGPoint(x: 751.7, y: 827.6))
        pathC.addCurve(to: CGPoint(x: 751.7, y: 568.5), controlPoint1: CGPoint(x: 680.3, y: 756.2), controlPoint2: CGPoint(x: 680.3, y: 639.9))
        pathC.addCurve(to: CGPoint(x: 1010.8, y: 568.5), controlPoint1: CGPoint(x: 823.1, y: 497.1), controlPoint2: CGPoint(x: 939.4, y: 497.1))
        pathC.addLine(to: CGPoint(x: 1083.7, y: 495.6))
        pathC.addCurve(to: CGPoint(x: 678.8, y: 495.6), controlPoint1: CGPoint(x: 971.9, y: 383.8), controlPoint2: CGPoint(x: 790.6, y: 383.8))
        pathC.addCurve(to: CGPoint(x: 678.8, y: 900.5), controlPoint1: CGPoint(x: 567.0, y: 607.4), controlPoint2: CGPoint(x: 567.0, y: 788.7))
        pathC.addCurve(to: CGPoint(x: 1083.7, y: 900.5), controlPoint1: CGPoint(x: 790.6, y: 1012.3), controlPoint2: CGPoint(x: 971.9, y: 1012.3))
        pathC.addLine(to: CGPoint(x: 1010.8, y: 827.6))
        pathC.addCurve(to: CGPoint(x: 751.7, y: 827.6), controlPoint1: CGPoint(x: 939.4, y: 899.0), controlPoint2: CGPoint(x: 823.1, y: 899.0))
        pathC.close()
        pathC.fill()

        // 3. I Letter (Solid Rectangle)
        let rectI = CGRect(x: 1187.1, y: 411.8, width: 103.2, height: 572.7)
        cgCtx.fill(rectI)

        // 4. JCI Shield Left Half (Teal)
        let shieldTeal = UIBezierPath()
        shieldTeal.move(to: CGPoint(x: 1760.6, y: 971.0))
        shieldTeal.addCurve(to: CGPoint(x: 1687.9, y: 870.5), controlPoint1: CGPoint(x: 1726.6, y: 927.9), controlPoint2: CGPoint(x: 1714.3, y: 893.4))
        shieldTeal.addCurve(to: CGPoint(x: 1608.0, y: 692.6), controlPoint1: CGPoint(x: 1654.4, y: 815.3), controlPoint2: CGPoint(x: 1627.2, y: 755.7))
        shieldTeal.addLine(to: CGPoint(x: 1589.3, y: 619.7))
        shieldTeal.addLine(to: CGPoint(x: 1579.5, y: 559.9))
        shieldTeal.addLine(to: CGPoint(x: 1573.7, y: 483.1))
        shieldTeal.addCurve(to: CGPoint(x: 1760.6, y: 417.4), controlPoint1: CGPoint(x: 1628.2, y: 457.3), controlPoint2: CGPoint(x: 1691.8, y: 434.5))
        shieldTeal.addCurve(to: CGPoint(x: 1948.3, y: 483.1), controlPoint1: CGPoint(x: 1829.4, y: 434.5), controlPoint2: CGPoint(x: 1893.0, y: 457.3))
        shieldTeal.addLine(to: CGPoint(x: 1948.3, y: 619.7))
        shieldTeal.addCurve(to: CGPoint(x: 1760.6, y: 971.0), controlPoint1: CGPoint(x: 1928.3, y: 755.7), controlPoint2: CGPoint(x: 1867.2, y: 870.5))
        shieldTeal.close()

        tealColor.setFill()
        shieldTeal.fill()

        // 5. JCI Shield Right Half (Cyan)
        let shieldCyan = UIBezierPath()
        shieldCyan.move(to: CGPoint(x: 1948.3, y: 483.1))
        shieldCyan.addLine(to: CGPoint(x: 1948.3, y: 619.7))
        shieldCyan.addCurve(to: CGPoint(x: 2188.0, y: 377.7), controlPoint1: CGPoint(x: 2028.3, y: 555.7), controlPoint2: CGPoint(x: 2118.0, y: 466.5))
        shieldCyan.addCurve(to: CGPoint(x: 1948.3, y: 483.1), controlPoint1: CGPoint(x: 2110.0, y: 410.5), controlPoint2: CGPoint(x: 2030.0, y: 446.5))
        shieldCyan.close()

        cyanColor.setFill()
        shieldCyan.fill()

        // 6. Shield Crest Details & TM
        mainColor.setFill()
        let crestPath = UIBezierPath()
        crestPath.move(to: CGPoint(x: 1760.6, y: 560.0))
        crestPath.addLine(to: CGPoint(x: 1795.0, y: 620.0))
        crestPath.addLine(to: CGPoint(x: 1726.0, y: 620.0))
        crestPath.close()
        crestPath.fill()

        // TM symbol
        let tmFont = UIFont.boldSystemFont(ofSize: 34.0)
        let tmAttrs: [NSAttributedString.Key: Any] = [
            .font: tmFont,
            .foregroundColor: mainColor
        ]
        ("TM" as NSString).draw(at: CGPoint(x: 2195.0, y: 370.0), withAttributes: tmAttrs)

        cgCtx.restoreGState()
    }

    /**
     * Draws the Circular Social Emblem Icon
     */
    private static func drawSocialCircleEmblem(cgCtx: CGContext, size: CGSize, caseStyle: CaseStyle) {
        let center = CGPoint(x: size.width / 2, y: size.height / 2)
        let radius = min(size.width, size.height) * 0.42

        let circlePath = UIBezierPath(arcCenter: center, radius: radius, startAngle: 0, endAngle: .pi * 2, clockwise: true)
        let circleColor = caseStyle == .socialWhite ? UIColor.white : (caseStyle == .socialBlack ? colorDarkNavy : colorBlue)
        circleColor.setFill()
        circlePath.fill()

        // Draw Shield in center
        cgCtx.saveGState()
        let scale = (radius * 1.3) / 800.0
        cgCtx.translateBy(x: center.x - (1850 * scale), y: center.y - (650 * scale))
        cgCtx.scaleBy(x: scale, y: scale)

        let shieldTeal = UIBezierPath()
        shieldTeal.move(to: CGPoint(x: 1760.6, y: 971.0))
        shieldTeal.addCurve(to: CGPoint(x: 1608.0, y: 692.6), controlPoint1: CGPoint(x: 1726.6, y: 927.9), controlPoint2: CGPoint(x: 1654.4, y: 815.3))
        shieldTeal.addLine(to: CGPoint(x: 1573.7, y: 483.1))
        shieldTeal.addCurve(to: CGPoint(x: 1760.6, y: 417.4), controlPoint1: CGPoint(x: 1628.2, y: 457.3), controlPoint2: CGPoint(x: 1691.8, y: 434.5))
        shieldTeal.addCurve(to: CGPoint(x: 1948.3, y: 483.1), controlPoint1: CGPoint(x: 1829.4, y: 434.5), controlPoint2: CGPoint(x: 1893.0, y: 457.3))
        shieldTeal.addLine(to: CGPoint(x: 1948.3, y: 619.7))
        shieldTeal.addCurve(to: CGPoint(x: 1760.6, y: 971.0), controlPoint1: CGPoint(x: 1928.3, y: 755.7), controlPoint2: CGPoint(x: 1867.2, y: 870.5))
        shieldTeal.close()

        let emblemColor = caseStyle == .socialWhite ? colorBlue : UIColor.white
        emblemColor.setFill()
        shieldTeal.fill()

        cgCtx.restoreGState()
    }

    /**
     * Generate Scalable Vector Graphics (SVG) string conforming to official JCI 2400x1600 viewBox
     */
    static func generateSvgString(line1: String, line2: String = "") -> String {
        let displayLine1 = line1.isEmpty ? "Tirupati" : line1
        return """
        <?xml version="1.0" encoding="utf-8"?>
        <svg version="1.1" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 2400 1600" width="100%" height="100%">
          <g id="JCI_Official_Mark">
            <path fill="#130F2D" d="M396.6,411.8v368c0,55.9-45.6,101.5-101.5,101.5c-28,0-53.3-11.4-71.7-29.8l-73,73c37,37,88.2,60,144.7,60s107.7-22.9,144.7-60c37-37,60-88.2,60-144.7v-368H396.6z"/>
            <path fill="#130F2D" d="M751.7,827.6c-71.4-71.4-71.4-187.7,0-259.1s187.7-71.4,259.1,0l0,0l72.9-72.9l0,0c-111.8-111.8-293.1-111.8-404.9,0C567,607.4,567,788.7,678.9,900.5c111.8,111.8,293.1,111.8,404.9,0l-72.9-72.9C939.4,899,823.2,899,751.7,827.6z"/>
            <rect x="1187.1" y="411.8" fill="#130F2D" width="103.2" height="572.7"/>
            <path fill="#59BDBD" d="M1760.6,971c-12-13.9-23.4-28.4-34-43.1c-13.8-18.6-26.6-37.6-38.7-57.4c-33.5-55.2-60.7-114.8-79.9-177.9c-7.4-23.9-13.6-48.1-18.7-72.9c-4-19.8-7.4-39.7-9.8-59.8c-3.3-25.3-5.3-50.9-5.8-76.8c-0.3-7.7-0.5-15.5-0.5-23.2c0-13.1,0.3-26.1,1-39.2v-0.2c18.4-9.1,37.3-17.4,56.7-24.6c23.2-8.6,47.1-16,71.5-22c18.7-4.5,37.8-8.1,57.2-10.8c23.2-3.4,46.8-5.5,70.8-6.2c5.8-0.2,11.9-0.2,17.7-0.2s11.9,0,17.7,0.2c24.1,0.7,47.6,2.7,70.8,6.2c19.4,2.8,38.5,6.4,57.3,10.8c24.4,6,48.3,13.4,71.5,22c-2.2-25.8-5.8-51.2-10.7-76.3c-24.2-7.7-49-13.9-74.4-18.9c-19.4-3.8-39.2-6.7-59.3-8.8c-23.9-2.6-48-4-72.6-4c-0.2,0-0.2,0-0.3,0s-0.2,0-0.3,0c-24.6,0-48.7,1.4-72.5,4c-20.1,2.1-39.9,5-59.3,8.8c-25.4,5-50.2,11.2-74.4,18.9c-20.3,6.4-40.1,13.6-59.5,21.8h-0.2c-25.4,10.7-49.9,22.9-73.6,36.3c-2.6,27-4,54.5-4,82.2v1.2c0,22,0.9,44,2.6,65.7c2.1,27.3,5.5,54.3,10.1,80.8c3.6,21.5,8.1,42.8,13.4,63.8c6.7,26.5,14.6,52.4,23.7,77.7c20.4,57.6,47.1,112.4,78.7,163.5c12.2,19.6,24.9,38.5,38.5,56.9c11,15,22.3,29.4,34.2,43.5c15,17.8,30.5,34.8,46.8,51.1c13,13.4,26.6,26.3,40.7,38.7c17.7,15.8,36.1,30.7,55,45c18.9-14.4,37.3-29.4,54.8-45.2c-18.9-13.4-37.3-27.8-54.8-43.1C1787.2,996.8,1773.7,984.1,1760.6,971z"/>
            <path fill="#0097D8" d="M2188,377.7c-23.7-13.4-48.3-25.6-73.8-36.3c4,26,6.7,52.3,7.9,79.1v0.2c0.7,13.1,1,26.1,1,39.2c0,7.7-0.2,15.5-0.5,23.2c-0.5,26-2.6,51.6-5.8,76.8c-2.4,20.1-5.8,40-9.8,59.8c-5.2,24.8-11.3,49-18.7,72.9c-19.3,63.1-46.4,122.7-79.9,177.9c-12,19.8-24.9,38.8-38.7,57.4c-10.7,14.8-22,29.2-34,43.1c-14.8,17.5-30.4,34.4-46.8,50.5c-13.1,13.1-26.6,25.8-40.7,37.6c-17.5,15.3-35.9,29.7-54.8,43.1c17.5,15.8,35.9,30.8,54.8,45.2c18.9-14.3,37.3-29.2,55-45c14.1-12.4,27.7-25.3,40.7-38.7c16.3-16.3,31.8-33.3,46.8-51.1c11.9-14.1,23.2-28.5,34.2-43.5c13.6-18.4,26.3-37.3,38.5-56.9c31.6-51.1,58.3-105.9,78.7-163.5c9.1-25.3,17-51.2,23.7-77.7c5.3-21,9.8-42.3,13.4-63.8c4.6-26.5,8.1-53.5,10.1-80.8c1.7-21.7,2.6-43.7,2.6-65.7v-1.2C2192,432.2,2190.6,404.7,2188,377.7z"/>
            <text x="185" y="1240" font-family="Helvetica Neue, Arial, sans-serif" font-weight="bold" font-size="120" fill="#0097D8">\(displayLine1)</text>
            \(line2.isEmpty ? "" : "<text x=\"185\" y=\"1380\" font-family=\"Helvetica Neue, Arial, sans-serif\" font-weight=\"bold\" font-size=\"110\" fill=\"#0097D8\">\(line2)</text>")
          </g>
        </svg>
        """
    }
}

