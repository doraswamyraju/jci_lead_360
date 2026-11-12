import SwiftUI

// MARK: - LO Website Builder Screen
struct ChapterWebsiteBuilderView: View {
    let chapterName: String
    let presidentName: String
    let onBack: () -> Void

    @State private var headline = "Welcome to JCI Tirupati Innovations"
    @State private var aboutMessage = "Dedicated to leadership development, active citizenship, and impactful community projects throughout 2027."
    @State private var copiedLink = false

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                HStack {
                    Button(action: onBack) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.jciTextDark)
                            .padding(8)
                            .background(Color.white)
                            .clipShape(Circle())
                    }
                    Text("LO Public Website Portal")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.jciTextDark)
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.top, 10)

                VStack(alignment: .leading, spacing: 14) {
                    Text("Live Web Link")
                        .font(.system(size: 14, weight: .bold))
                    HStack {
                        Text("https://lead.sriddha.com/lo/tirupati")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(.jciRoyalBlue)
                        Spacer()
                        Button(action: {
                            UIPasteboard.general.string = "https://lead.sriddha.com/lo/tirupati"
                            copiedLink = true
                        }) {
                            Text(copiedLink ? "Copied!" : "Copy")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(Color.jciRoyalBlue)
                                .cornerRadius(8)
                        }
                    }
                    .padding(12)
                    .background(Color.jciBackgroundLight)
                    .cornerRadius(10)
                }
                .padding(20)
                .background(Color.white)
                .cornerRadius(18)
                .padding(.horizontal, 20)

                VStack(alignment: .leading, spacing: 14) {
                    Text("Configure Homepage Content")
                        .font(.system(size: 16, weight: .bold))
                    CustomStudioField(label: "Banner Headline", text: $headline)
                    CustomStudioField(label: "President's Welcome Note", text: $aboutMessage)
                }
                .padding(20)
                .background(Color.white)
                .cornerRadius(18)
                .padding(.horizontal, 20)

                Spacer(minLength: 30)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
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

// MARK: - Photo Frames Screen
struct FramesView: View {
    var onBack: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 20) {
            HStack {
                if let onBack = onBack {
                    Button(action: onBack) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.jciTextDark)
                            .padding(8)
                            .background(Color.white)
                            .clipShape(Circle())
                    }
                }
                Text("JCI Official Photo Frames")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.jciTextDark)
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.top, 10)

            VStack(spacing: 12) {
                Image(systemName: "photo.artframe")
                    .font(.system(size: 60))
                    .foregroundColor(.jciSkyBlue)
                Text("Official 2027 Frames Available")
                    .font(.system(size: 16, weight: .bold))
                Text("Fit and export official JCI ribbons and officer insignia directly over portrait photos.")
                    .font(.system(size: 13))
                    .foregroundColor(.jciTextMuted)
                    .multilineTextAlignment(.center)
            }
            .padding(30)
            .background(Color.white)
            .cornerRadius(20)
            .padding(.horizontal, 20)

            Spacer()
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
    }
}

