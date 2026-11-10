import UIKit

/**
 * Native iOS CoreGraphics High-Resolution Flyer Compositor Engine
 * Parity with AndroidFlyerRenderer.kt
 * Generates 300 DPI high-resolution composited graphics matching backend template coordinates,
 * layers, custom typography, circular portrait avatars, borders, and shadows.
 */
class iOSFlyerRenderer {

    static func compositeDynamicFlyer(
        template: FlyerTemplate,
        baseImage: UIImage,
        chapterLogo: UIImage?,
        presidentPhoto: UIImage?,
        fieldValues: [String: String],
        targetAspectRatio: String = "1:1" // "1:1" or "4:5"
    ) -> UIImage {
        let baseWidth = CGFloat(template.dimensions?.width ?? Double(baseImage.size.width))
        let baseHeight = CGFloat(template.dimensions?.height ?? Double(baseImage.size.height))

        let canvasWidth = baseWidth > 0 ? baseWidth : 1080
        var canvasHeight = baseHeight > 0 ? baseHeight : 1080

        if targetAspectRatio == "4:5" {
            canvasHeight = canvasWidth * 1.25 // 1080 x 1350
        }

        let canvasSize = CGSize(width: canvasWidth, height: canvasHeight)
        let format = UIGraphicsImageRendererFormat()
        format.scale = 1.0 // Exact pixel rendering (1080p)
        let renderer = UIGraphicsImageRenderer(size: canvasSize, format: format)

        return renderer.image { context in
            let cgCtx = context.cgContext

            // 1. Draw Base Flyer Image (Aspect fill / fit into canvas)
            let drawRect = CGRect(origin: .zero, size: canvasSize)
            baseImage.draw(in: drawRect)

            // 2. Iterate through configured template layers from backend
            let layers = template.layers ?? []
            for layer in layers {
                let zoneType = (layer.type ?? "").lowercased()

                let x = CGFloat(layer.x ?? 80)
                let y = CGFloat(layer.y ?? 80)
                let w = CGFloat(layer.width ?? 300)
                let h = CGFloat(layer.height ?? 80)

                let shape = layer.shape ?? (zoneType.contains("photo") ? "circle" : "rectangle")
                let borderWidth = CGFloat(layer.borderWidth ?? (zoneType.contains("photo") ? 4 : 0))
                let borderColor = parseHexColor(layer.borderColor, fallback: .white)

                if zoneType.contains("logo") {
                    // CHAPTER LOGO / CO-HOST LOGO
                    if let logo = chapterLogo {
                        let rect = CGRect(x: x, y: y, width: w, height: h)
                        if shape == "circle" {
                            drawCircleImage(cgCtx: cgCtx, image: logo, rect: rect, borderWidth: borderWidth, borderColor: borderColor)
                        } else {
                            logo.draw(in: rect)
                        }
                    }
                } else if zoneType.contains("photo") || zoneType.contains("president_photo") || zoneType.contains("speaker") {
                    // PRESIDENT / SPEAKER PORTRAIT
                    if let photo = presidentPhoto {
                        let rect = CGRect(x: x, y: y, width: w, height: h)
                        if shape == "circle" {
                            drawCircleImage(cgCtx: cgCtx, image: photo, rect: rect, borderWidth: borderWidth, borderColor: borderColor)
                        } else {
                            photo.draw(in: rect)
                            if borderWidth > 0 {
                                cgCtx.setLineWidth(borderWidth)
                                cgCtx.setStrokeColor(borderColor.cgColor)
                                cgCtx.stroke(rect)
                            }
                        }
                    }
                } else {
                    // TEXT FIELDS (Date, Time, Venue, Chief Guest, Title, President Name, etc.)
                    let text = fieldValues[zoneType] ?? fieldValues[layer.label?.lowercased() ?? ""] ?? layer.label ?? ""
                    if !text.isEmpty {
                        let fontSize = CGFloat(layer.fontSize ?? 26)
                        let textColor = parseHexColor(layer.color, fallback: .white)
                        let align = layer.align ?? "left"
                        let isBold = layer.bold ?? true

                        drawFormattedText(
                            text: text,
                            x: x,
                            y: y,
                            maxWidth: w,
                            maxHeight: h,
                            textColor: textColor,
                            fontSize: fontSize,
                            isBold: isBold,
                            align: align
                        )
                    }
                }
            }

            // Fallback: If template had 0 layers configured in DB, render standard footer lockups
            if layers.isEmpty {
                renderDefaultFlyerOverlays(
                    size: canvasSize,
                    presidentName: fieldValues["president_name"] ?? "JC Leader",
                    chapterName: fieldValues["chapter_name"] ?? "JCI Chapter",
                    dateTime: fieldValues["event_date_time"] ?? "",
                    venue: fieldValues["event_venue"] ?? "",
                    chiefGuest: fieldValues["chief_guest"],
                    chapterLogo: chapterLogo
                )
            }
        }
    }

    private static func drawCircleImage(cgCtx: CGContext, image: UIImage, rect: CGRect, borderWidth: CGFloat, borderColor: UIColor) {
        cgCtx.saveGState()
        let path = UIBezierPath(ovalIn: rect)
        path.addClip()
        image.draw(in: rect)
        cgCtx.restoreGState()

        if borderWidth > 0 {
            let strokePath = UIBezierPath(ovalIn: rect.insetBy(dx: borderWidth / 2, dy: borderWidth / 2))
            borderColor.setStroke()
            strokePath.lineWidth = borderWidth
            strokePath.stroke()
        }
    }

    private static func drawFormattedText(
        text: String,
        x: CGFloat,
        y: CGFloat,
        maxWidth: CGFloat,
        maxHeight: CGFloat,
        textColor: UIColor,
        fontSize: CGFloat,
        isBold: Bool,
        align: String
    ) {
        let shadow = NSShadow()
        shadow.shadowColor = UIColor.black.withAlphaComponent(0.85)
        shadow.shadowOffset = CGSize(width: 1.5, height: 1.5)
        shadow.shadowBlurRadius = 4

        let paragraphStyle = NSMutableParagraphStyle()
        paragraphStyle.lineBreakMode = .byWordWrapping
        switch align.lowercased() {
        case "center":
            paragraphStyle.alignment = .center
        case "right":
            paragraphStyle.alignment = .right
        default:
            paragraphStyle.alignment = .left
        }

        let font = isBold ? UIFont.boldSystemFont(ofSize: fontSize) : UIFont.systemFont(ofSize: fontSize, weight: .semibold)
        let attrs: [NSAttributedString.Key: Any] = [
            .font: font,
            .foregroundColor: textColor,
            .shadow: shadow,
            .paragraphStyle: paragraphStyle
        ]

        let rect = CGRect(x: x, y: y, width: maxWidth, height: maxHeight > 0 ? maxHeight : 120)
        (text as NSString).draw(in: rect, withAttributes: attrs)
    }

    private static func renderDefaultFlyerOverlays(
        size: CGSize,
        presidentName: String,
        chapterName: String,
        dateTime: String,
        venue: String,
        chiefGuest: String?,
        chapterLogo: UIImage?
    ) {
        // Draw Chapter Logo Top Left
        if let logo = chapterLogo {
            logo.draw(in: CGRect(x: 60, y: 60, width: 220, height: 90))
        }

        // Draw Details bottom section
        let titleAttrs: [NSAttributedString.Key: Any] = [
            .font: UIFont.boldSystemFont(ofSize: size.width * 0.035),
            .foregroundColor: UIColor.white
        ]
        let bodyAttrs: [NSAttributedString.Key: Any] = [
            .font: UIFont.systemFont(ofSize: size.width * 0.026, weight: .semibold),
            .foregroundColor: UIColor.white.withAlphaComponent(0.95)
        ]

        if !dateTime.isEmpty {
            ("📅 \(dateTime)" as NSString).draw(at: CGPoint(x: 60, y: size.height - 230), withAttributes: bodyAttrs)
        }
        if !venue.isEmpty {
            ("📍 \(venue)" as NSString).draw(at: CGPoint(x: 60, y: size.height - 180), withAttributes: bodyAttrs)
        }
        if let guest = chiefGuest, !guest.isEmpty {
            ("🌟 Chief Guest: \(guest)" as NSString).draw(at: CGPoint(x: 60, y: size.height - 130), withAttributes: bodyAttrs)
        }
        ("JC \(presidentName) • President 2027 • \(chapterName)" as NSString).draw(at: CGPoint(x: 60, y: size.height - 75), withAttributes: titleAttrs)
    }

    private static func parseHexColor(_ hex: String?, fallback: UIColor) -> UIColor {
        guard let hex = hex?.trimmingCharacters(in: .whitespacesAndNewlines), !hex.isEmpty else {
            return fallback
        }
        var cleanHex = hex
        if cleanHex.hasPrefix("#") {
            cleanHex.removeFirst()
        }
        guard cleanHex.count == 6, let rgb = UInt64(cleanHex, radix: 16) else {
            return fallback
        }
        let r = CGFloat((rgb >> 16) & 0xFF) / 255.0
        let g = CGFloat((rgb >> 8) & 0xFF) / 255.0
        let b = CGFloat(rgb & 0xFF) / 255.0
        return UIColor(red: r, green: g, blue: b, alpha: 1.0)
    }

    /**
     * Download UIImage asynchronously from remote URL
     */
    static func downloadImage(from urlString: String) async -> UIImage? {
        let fullUrl = urlString.starts(with: "http") ? urlString : "\(ApiService.shared.baseUrl)\(urlString)"
        guard let url = URL(string: fullUrl) else { return nil }
        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            return UIImage(data: data)
        } catch {
            return nil
        }
    }
}
