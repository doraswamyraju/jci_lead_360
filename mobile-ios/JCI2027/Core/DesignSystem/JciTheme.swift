import SwiftUI

// MARK: - JCI Brand Colors
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }

    static let jciDeepNavy = Color(hex: "0F182E")
    static let jciDeepNavyDark = Color(hex: "0A0F1D")
    static let jciRoyalBlue = Color(hex: "005BAA")
    static let jciSkyBlue = Color(hex: "0097D8")
    static let jciShieldTeal = Color(hex: "36BBA6")
    static let jciTealLight = Color(hex: "E6F8F5")
    static let jciBackgroundLight = Color(hex: "F6F8FC")
    static let jciCardSurface = Color.white
    static let jciBorderSubtle = Color(hex: "E2E8F0")
    static let jciTextDark = Color(hex: "0F172A")
    static let jciTextMuted = Color(hex: "64748B")
}

// MARK: - Brand Gradients
struct JciGradients {
    static let hero = LinearGradient(
        colors: [Color.jciDeepNavy, Color(hex: "132247"), Color.jciRoyalBlue],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    static let shield = LinearGradient(
        colors: [Color.jciSkyBlue, Color.jciShieldTeal],
        startPoint: .leading,
        endPoint: .trailing
    )

    static let button = LinearGradient(
        colors: [Color.jciRoyalBlue, Color.jciSkyBlue],
        startPoint: .leading,
        endPoint: .trailing
    )
}

