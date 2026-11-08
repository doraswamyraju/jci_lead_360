import SwiftUI

struct PresidentFlyerStudioView: View {
    let token: String
    let presidentName: String
    let chapterName: String

    @State private var selectedCategory = "Installation"
    let categories = ["Installation", "Meetings", "Community", "Awards", "Festivals"]

    @State private var eventTitle = "Chapter Installation Ceremony 2027"
    @State private var eventDate = "Sunday, 24 Jan 2027 • 6:00 PM"
    @State private var eventVenue = "Grand Ballroom, Fortune Select Grand Ridge, Tirupati"
    @State private var chiefGuest = "JC Sen. Rajesh Singhania (National President)"

    @State private var isCompositing = false
    @State private var renderedFlyer: UIImage? = nil
    @State private var showShareSheet = false

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Header
                VStack(alignment: .leading, spacing: 4) {
                    Text("President's Flyer Studio")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.jciTextDark)
                    Text("Instant 300 DPI personalized flyers with 1-tap WhatsApp broadcast")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.jciTextMuted)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 20)
                .padding(.top, 10)

                // Category Selector
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(categories, id: \.self) { cat in
                            Button(action: { selectedCategory = cat }) {
                                Text(cat)
                                    .font(.system(size: 13, weight: .semibold))
                                    .foregroundColor(selectedCategory == cat ? .white : .jciTextDark)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 8)
                                    .background(selectedCategory == cat ? Color.jciRoyalBlue : Color.white)
                                    .cornerRadius(20)
                                    .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                }

                // Live Preview Canvas Container
                ZStack {
                    RoundedRectangle(cornerRadius: 20)
                        .fill(JciGradients.hero)
                        .frame(height: 280)

                    VStack(spacing: 12) {
                        HStack {
                            Text("JCI 2027 • \(selectedCategory.uppercased())")
                                .font(.system(size: 11, weight: .black))
                                .foregroundColor(.jciShieldTeal)
                            Spacer()
                            Text(chapterName)
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.white)
                        }

                        Spacer()

                        Text(eventTitle)
                            .font(.system(size: 18, weight: .black))
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)

                        VStack(spacing: 4) {
                            Text("📅 \(eventDate)")
                            Text("📍 \(eventVenue)")
                            if !chiefGuest.isEmpty {
                                Text("🌟 Chief Guest: \(chiefGuest)")
                            }
                        }
                        .font(.system(size: 11, weight: .semibold))
                        .foregroundColor(.white.opacity(0.9))

                        Spacer()

                        HStack {
                            Text("JC \(presidentName)")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.white)
                            Spacer()
                            Text("Chapter President 2027")
                                .font(.system(size: 11, weight: .medium))
                                .foregroundColor(.jciSkyBlue)
                        }
                    }
                    .padding(20)
                    .frame(height: 280)
                }
                .padding(.horizontal, 20)
                .shadow(color: Color.jciDeepNavy.opacity(0.25), radius: 12, x: 0, y: 6)

                // Editable Parameters
                VStack(alignment: .leading, spacing: 14) {
                    Text("Personalize Flyer Details")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.jciTextDark)

                    CustomStudioField(label: "Event Title", text: $eventTitle)
                    CustomStudioField(label: "Date & Timing", text: $eventDate)
                    CustomStudioField(label: "Venue / Location", text: $eventVenue)
                    CustomStudioField(label: "Chief Guest / Keynote", text: $chiefGuest)
                }
                .padding(20)
                .background(Color.white)
                .cornerRadius(20)
                .padding(.horizontal, 20)

                // Action Buttons
                VStack(spacing: 12) {
                    Button(action: generateAndShare) {
                        HStack {
                            Image(systemName: "square.and.arrow.up.fill")
                            Text("Share Flyer to WhatsApp & Socials")
                        }
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(JciGradients.button)
                        .cornerRadius(14)
                        .shadow(color: Color.jciRoyalBlue.opacity(0.3), radius: 8, x: 0, y: 4)
                    }
                }
                .padding(.horizontal, 20)

                Spacer(minLength: 30)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
        .sheet(isPresented: $showShareSheet) {
            if let image = renderedFlyer {
                ShareActivitySheet(activityItems: [image, "🌟 \(eventTitle)\n📅 \(eventDate)\n📍 \(eventVenue)\n\nInvited by JC \(presidentName) (President, \(chapterName))"])
            }
        }
    }

    private func generateAndShare() {
        let size = CGSize(width: 1080, height: 1080)
        let renderer = UIGraphicsImageRenderer(size: size)
        let image = renderer.image { ctx in
            // Background gradient
            let colors = [UIColor(Color.jciDeepNavy).cgColor, UIColor(Color.jciRoyalBlue).cgColor]
            let gradient = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(), colors: colors as CFArray, locations: [0.0, 1.0])!
            ctx.cgContext.drawLinearGradient(gradient, start: .zero, end: CGPoint(x: size.width, y: size.height), options: [])

            // Text attributes
            let titleAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.boldSystemFont(ofSize: 48),
                .foregroundColor: UIColor.white
            ]
            let bodyAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 28, weight: .semibold),
                .foregroundColor: UIColor.white.withAlphaComponent(0.9)
            ]

            (eventTitle as NSString).draw(at: CGPoint(x: 60, y: 400), withAttributes: titleAttrs)
            ("📅 \(eventDate)" as NSString).draw(at: CGPoint(x: 60, y: 520), withAttributes: bodyAttrs)
            ("📍 \(eventVenue)" as NSString).draw(at: CGPoint(x: 60, y: 580), withAttributes: bodyAttrs)
            ("🌟 Chief Guest: \(chiefGuest)" as NSString).draw(at: CGPoint(x: 60, y: 640), withAttributes: bodyAttrs)
            ("JC \(presidentName) • President 2027" as NSString).draw(at: CGPoint(x: 60, y: 960), withAttributes: bodyAttrs)
        }

        self.renderedFlyer = image
        self.showShareSheet = true
    }
}

struct CustomStudioField: View {
    let label: String
    @Binding var text: String

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(.system(size: 12, weight: .semibold))
                .foregroundColor(.jciTextMuted)
            TextField(label, text: $text)
                .padding(12)
                .background(Color.jciBackgroundLight)
                .cornerRadius(10)
        }
    }
}

struct ShareActivitySheet: UIViewControllerRepresentable {
    let activityItems: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

