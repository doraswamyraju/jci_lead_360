import UIKit

/**
 * Native iOS CoreGraphics Flyer Compositor Engine
 * Renders 300 DPI high-resolution graphics and overlays for WhatsApp and Photos
 */
class iOSFlyerRenderer {

    static func compositeFlyer(
        baseImage: UIImage,
        chapterLogo: UIImage?,
        presidentName: String,
        dateTime: String,
        venue: String,
        chiefGuest: String?
    ) -> UIImage? {
        let size = baseImage.size
        let renderer = UIGraphicsImageRenderer(size: size)

        let composite = renderer.image { context in
            // 1. Draw base flyer image
            baseImage.draw(in: CGRect(origin: .zero, size: size))

            // 2. Draw Chapter Logo if available
            if let logo = chapterLogo {
                let logoRect = CGRect(x: 80, y: 80, width: 220, height: 100)
                logo.draw(in: logoRect)
            }

            // 3. Draw text attributes with shadow
            let shadow = NSShadow()
            shadow.shadowColor = UIColor.black.withAlphaComponent(0.8)
            shadow.shadowOffset = CGSize(width: 2, height: 2)
            shadow.shadowBlurRadius = 6

            let titleAttributes: [NSAttributedString.Key: Any] = [
                .font: UIFont.boldSystemFont(ofSize: size.width * 0.038),
                .foregroundColor: UIColor.white,
                .shadow: shadow
            ]

            let bodyAttributes: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: size.width * 0.032, weight: .semibold),
                .foregroundColor: UIColor.white,
                .shadow: shadow
            ]

            // 4. Render Strings
            let presString = NSAttributedString(string: presidentName, attributes: titleAttributes)
            presString.draw(at: CGPoint(x: 80, y: size.height - 160))

            let dtString = NSAttributedString(string: "📅 \(dateTime)", attributes: bodyAttributes)
            dtString.draw(at: CGPoint(x: 80, y: size.height - 300))

            let venueString = NSAttributedString(string: "📍 \(venue)", attributes: bodyAttributes)
            venueString.draw(at: CGPoint(x: 80, y: size.height - 240))

            if let guest = chiefGuest, !guest.isEmpty {
                let guestString = NSAttributedString(string: "🌟 Chief Guest: \(guest)", attributes: bodyAttributes)
                guestString.draw(at: CGPoint(x: 80, y: size.height - 180))
            }
        }

        return composite
    }

    /**
     * Dispatch 1-Tap Share via UIActivityViewController or WhatsApp URL Scheme
     */
    static func shareFlyer(
        from viewController: UIViewController,
        image: UIImage,
        caption: String
    ) {
        let activityVC = UIActivityViewController(
            activityItems: [image, caption],
            applicationActivities: nil
        )
        viewController.present(activityVC, animated: true)
    }
}
