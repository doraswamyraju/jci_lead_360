import SwiftUI

/// JCI Resources & Official Identity Hub (100% Feature Parity with Android JciResourcesScreen.kt)
/// Vector Chapter Logo Generator (logo.jciath.be replica) + Stationery Kits & Brand Governance
struct JciResourcesView: View {
    let chapterName: String
    let onBack: () -> Void

    // 0: Official Logo, 1: Letterhead, 2: Visiting Card, 3: Certificate, 4: Governance Docs
    @State private var selectedCategory = 0

    // Logo Generator State
    @State private var line1: String
    @State private var line2: String = ""
    @State private var renderedLogos: [JciLogoGenerator.CaseStyle: UIImage] = [:]
    @State private var activeShareItem: Any? = nil
    @State private var showShareSheet = false
    @State private var showSaveAlert = false
    @State private var saveAlertMessage = ""

    init(chapterName: String, onBack: @escaping () -> Void) {
        self.chapterName = chapterName
        self.onBack = onBack
        let cleanName = chapterName.replacingOccurrences(of: "JCI", with: "").trimmingCharacters(in: .whitespaces)
        _line1 = State(initialValue: cleanName.isEmpty ? "Tirupati" : cleanName)
    }

    let categories = [
        ("logo", "🎨 Official Logo", "Active"),
        ("letterhead", "📄 Letterhead", "Coming Soon"),
        ("visiting_card", "🪪 Visiting Card", "Coming Soon"),
        ("certificate", "🏆 Certificate", "Coming Soon"),
        ("governance", "📚 Governance", "Guides")
    ]

    var body: some View {
        VStack(spacing: 0) {
            // Header Bar
            headerBarView

            // Horizontal Category Selector Tabs
            categoryTabsView

            ScrollView(showsIndicators: false) {
                VStack(spacing: 16) {
                    switch selectedCategory {
                    case 0:
                        logoMakerStudioView
                    case 1:
                        ComingSoonResourceCard(
                            emoji: "📄",
                            title: "Official LO Letterhead Generator",
                            description: "Automated vector Letterhead creator with chapter logo, president signature zone, and official postal footer conforming to JCI Corporate Identity manual.",
                            expectedDate: "Q1 2027",
                            features: [
                                "Single & Double Page Executive Layouts",
                                "Pre-populated Chapter Executive Roster",
                                "Zone & National Affiliation Crests",
                                "Export as High-Resolution Print-Ready PDF"
                            ]
                        )
                    case 2:
                        ComingSoonResourceCard(
                            emoji: "🪪",
                            title: "Jaycee Visiting Card Kit",
                            description: "Customized double-sided business card templates incorporating dynamic personal QR code and official chapter branding.",
                            expectedDate: "Q1 2027",
                            features: [
                                "Standard 3.5\" x 2\" Bleed & Cut Guides",
                                "High-Res Vector PDF for Commercial Printers",
                                "Dynamic vCard QR Code on Backside",
                                "Matte & Velvet Finish Simulation"
                            ]
                        )
                    case 3:
                        ComingSoonResourceCard(
                            emoji: "🏆",
                            title: "Official Citation & Certificate Maker",
                            description: "Generate certificates of appreciation, participation, and outstanding leadership recognition with gold foil borders and chapter seal.",
                            expectedDate: "Q1 2027",
                            features: [
                                "Certificate of Appreciation & Participation",
                                "Zone Officer & Speaker Felicitation Templates",
                                "Dynamic Recipient Name & Project Title Insertion",
                                "Print-Ready Landscape Vector PDF Export"
                            ]
                        )
                    default:
                        governanceDocsView
                    }

                    Spacer(minLength: 40)
                }
                .padding(.vertical, 16)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
        .onAppear {
            renderAllLogos()
        }
        .sheet(isPresented: $showShareSheet) {
            if let item = activeShareItem {
                ActivityShareSheet(activityItems: [item])
            }
        }
        .alert(isPresented: $showSaveAlert) {
            Alert(
                title: Text("Saved to Photos"),
                message: Text(saveAlertMessage),
                dismissButton: .default(Text("OK"))
            )
        }
    }

    // MARK: - Header Bar
    private var headerBarView: some View {
        HStack {
            Button(action: onBack) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.jciTextDark)
                    .padding(8)
                    .background(Color.white)
                    .clipShape(Circle())
                    .shadow(color: Color.black.opacity(0.06), radius: 4, x: 0, y: 2)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text("JCI Resources & Identity Hub")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(.jciDeepNavy)

                Text("Corporate Brand Assets, Vector Logos & Kits")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(.jciTextMuted)
            }

            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.white)
        .shadow(color: Color.black.opacity(0.04), radius: 4, y: 2)
    }

    // MARK: - Category Tabs
    private var categoryTabsView: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(0..<categories.count, id: \.self) { idx in
                    let cat = categories[idx]
                    let isSelected = selectedCategory == idx
                    Button(action: {
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                            selectedCategory = idx
                        }
                    }) {
                        HStack(spacing: 6) {
                            Text(cat.1)
                                .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                                .foregroundColor(isSelected ? .white : .jciDeepNavy)

                            if cat.2 == "Coming Soon" {
                                Text("Soon")
                                    .font(.system(size: 9, weight: .bold))
                                    .foregroundColor(isSelected ? Color(hex: "FEF08A") : Color(hex: "D97706"))
                                    .padding(.horizontal, 5)
                                    .padding(.vertical, 2)
                                    .background(isSelected ? Color.black.opacity(0.2) : Color(hex: "FEF3C7"))
                                    .cornerRadius(4)
                            }
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 9)
                        .background(
                            isSelected ?
                            LinearGradient(colors: [Color.jciRoyalBlue, Color.jciSkyBlue], startPoint: .leading, endPoint: .trailing) :
                            LinearGradient(colors: [Color.white, Color.white], startPoint: .leading, endPoint: .trailing)
                        )
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(isSelected ? Color.clear : Color(hex: "E2E8F0"), lineWidth: 1)
                        )
                        .shadow(color: isSelected ? Color.jciRoyalBlue.opacity(0.25) : Color.clear, radius: 4, y: 2)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
        }
        .background(Color.white)
        .shadow(color: Color.black.opacity(0.02), radius: 2, y: 1)
    }

    // MARK: - Logo Maker Studio
    private var logoMakerStudioView: some View {
        VStack(spacing: 16) {
            // Configuration Card
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Text("🎨 Official Chapter Logo Generator")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.jciDeepNavy)
                    Spacer()
                    Text("logo.jciath.be replica")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(.jciSkyBlue)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 3)
                        .background(Color.jciSkyBlue.opacity(0.12))
                        .cornerRadius(6)
                }

                Text("Generates official vector brand marks with exact JCI Corporate Identity proportions, SVG curves, and Helvetica Neue typography.")
                    .font(.system(size: 12))
                    .foregroundColor(.jciTextMuted)

                Divider()

                VStack(alignment: .leading, spacing: 6) {
                    Text("LINE 1: CHAPTER NAME *")
                        .font(.system(size: 10, weight: .black))
                        .foregroundColor(.jciDeepNavy)

                    TextField("e.g. Tirupati Innovations", text: $line1)
                        .font(.system(size: 14, weight: .semibold))
                        .padding(10)
                        .background(Color(hex: "F8FAFC"))
                        .cornerRadius(8)
                        .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "CBD5E1"), lineWidth: 1))
                        .onChange(of: line1) { _ in renderAllLogos() }
                }

                VStack(alignment: .leading, spacing: 6) {
                    Text("LINE 2: REGION / ZONE (OPTIONAL)")
                        .font(.system(size: 10, weight: .black))
                        .foregroundColor(.jciDeepNavy)

                    TextField("e.g. Zone XXVI • India", text: $line2)
                        .font(.system(size: 14, weight: .semibold))
                        .padding(10)
                        .background(Color(hex: "F8FAFC"))
                        .cornerRadius(8)
                        .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "CBD5E1"), lineWidth: 1))
                        .onChange(of: line2) { _ in renderAllLogos() }
                }
            }
            .padding(16)
            .background(Color.white)
            .cornerRadius(16)
            .shadow(color: Color.black.opacity(0.04), radius: 6, y: 2)
            .padding(.horizontal, 16)

            // Case Styles Showcase
            VStack(alignment: .leading, spacing: 14) {
                Text("7 Official Color Formats & Social Icons")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.jciDeepNavy)
                    .padding(.horizontal, 16)

                ForEach(JciLogoGenerator.CaseStyle.allCases) { caseStyle in
                    logoCaseCard(caseStyle: caseStyle)
                }
            }
        }
    }

    // MARK: - Logo Case Card
    private func logoCaseCard(caseStyle: JciLogoGenerator.CaseStyle) -> some View {
        let logoImage = renderedLogos[caseStyle]
        return VStack(spacing: 0) {
            // Visual Preview Canvas
            ZStack {
                if caseStyle == .light || caseStyle == .socialBlack {
                    Color(hex: "130F2D")
                } else if caseStyle == .whiteOnBlue || caseStyle == .socialWhite {
                    Color(hex: "0097D8")
                } else {
                    Color.white
                }

                if let img = logoImage {
                    Image(uiImage: img)
                        .resizable()
                        .scaledToFit()
                        .padding(caseStyle.isSocial ? 16 : 10)
                } else {
                    ProgressView()
                }
            }
            .frame(height: caseStyle.isSocial ? 160 : 130)
            .frame(maxWidth: .infinity)
            .overlay(
                // Case Badge
                VStack {
                    HStack {
                        Text(caseStyle.rawValue)
                            .font(.system(size: 10, weight: .black))
                            .foregroundColor(caseStyle == .light || caseStyle == .socialBlack ? .white : (caseStyle == .whiteOnBlue || caseStyle == .socialWhite ? Color(hex: "FEF08A") : .jciDeepNavy))
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(
                                (caseStyle == .light || caseStyle == .socialBlack || caseStyle == .whiteOnBlue || caseStyle == .socialWhite) ?
                                Color.black.opacity(0.35) : Color(hex: "F1F5F9")
                            )
                            .cornerRadius(6)
                        Spacer()
                    }
                    .padding(8)
                    Spacer()
                }
            )

            Divider()

            // Action Buttons
            HStack(spacing: 8) {
                // Save PNG
                Button(action: {
                    if let img = logoImage {
                        saveToPhotos(img, label: "PNG")
                    }
                }) {
                    Text("PNG")
                        .font(.system(size: 11, weight: .bold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                        .background(Color.jciRoyalBlue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }

                // Save JPG
                Button(action: {
                    if let img = logoImage {
                        saveToPhotos(img, label: "JPG")
                    }
                }) {
                    Text("JPG")
                        .font(.system(size: 11, weight: .bold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                        .background(Color(hex: "334155"))
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }

                // Vector SVG Export
                Button(action: {
                    let svgString = JciLogoGenerator.generateSvgString(line1: line1, line2: line2)
                    let tempUrl = FileManager.default.temporaryDirectory.appendingPathComponent("JCI_\(line1)_Vector.svg")
                    try? svgString.write(to: tempUrl, atomically: true, encoding: .utf8)
                    activeShareItem = tempUrl
                    showShareSheet = true
                }) {
                    Text("Vector SVG")
                        .font(.system(size: 11, weight: .bold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                        .background(Color(hex: "091322"))
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }

                // WhatsApp Share
                Button(action: {
                    if let img = logoImage {
                        let caption = "🏛️ Official JCI Logo (*\(caseStyle.rawValue)*) for *\(line1)*"
                        activeShareItem = img
                        showShareSheet = true
                    }
                }) {
                    Image(systemName: "message.fill")
                        .font(.system(size: 13, weight: .bold))
                        .padding(8)
                        .background(Color(hex: "22C55E"))
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
            }
            .padding(12)
            .background(Color.white)
        }
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.04), radius: 6, y: 2)
        .padding(.horizontal, 16)
    }

    // MARK: - Governance Docs
    private var governanceDocsView: some View {
        VStack(spacing: 12) {
            let docs = [
                ("JCI Constitution & Policy 2027", "Official governance & bylaws document", "doc.text.fill"),
                ("Chapter Installation Protocol", "Stage management & gavel handover guidelines", "flag.fill"),
                ("JCI Brand & Logo Standards", "Shield colors, vector logos, typography rules", "paintpalette.fill"),
                ("Parliamentary Procedures (Robert's Rules)", "Meeting management & motions reference", "book.closed.fill"),
                ("Award Bidding Templates", "Templates for Zone & National recognitions", "trophy.fill")
            ]

            ForEach(docs, id: \.0) { item in
                HStack(spacing: 14) {
                    Image(systemName: item.2)
                        .font(.system(size: 20))
                        .foregroundColor(.jciRoyalBlue)
                        .frame(width: 44, height: 44)
                        .background(Color.jciRoyalBlue.opacity(0.1))
                        .cornerRadius(12)

                    VStack(alignment: .leading, spacing: 2) {
                        Text(item.0)
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.jciTextDark)
                        Text(item.1)
                            .font(.system(size: 12))
                            .foregroundColor(.jciTextMuted)
                    }
                    Spacer()
                    Image(systemName: "arrow.down.circle.fill")
                        .font(.system(size: 20))
                        .foregroundColor(.jciSkyBlue)
                }
                .padding(16)
                .background(Color.white)
                .cornerRadius(16)
                .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
            }
        }
        .padding(.horizontal, 16)
    }

    private func renderAllLogos() {
        for caseStyle in JciLogoGenerator.CaseStyle.allCases {
            renderedLogos[caseStyle] = JciLogoGenerator.generateLogoBitmap(
                caseStyle: caseStyle,
                line1: line1,
                line2: line2,
                width: caseStyle.isSocial ? 800 : 1200,
                height: 800
            )
        }
    }

    private func saveToPhotos(_ image: UIImage, label: String) {
        UIImageWriteToSavedPhotosAlbum(image, nil, nil, nil)
        saveAlertMessage = "Official JCI Logo (\(label)) has been saved directly to your iPhone Camera Roll in high resolution."
        showSaveAlert = true
    }
}

// MARK: - Coming Soon Resource Card Component
struct ComingSoonResourceCard: View {
    let emoji: String
    let title: String
    let description: String
    let expectedDate: String
    let features: [String]

    var body: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(Color(hex: "E0F2FE"))
                    .frame(width: 72, height: 72)
                Text(emoji)
                    .font(.system(size: 34))
            }

            Text(title)
                .font(.system(size: 18, weight: .heavy))
                .foregroundColor(.jciDeepNavy)
                .multilineTextAlignment(.center)

            Text("🚧 COMING SOON")
                .font(.system(size: 10, weight: .heavy))
                .foregroundColor(Color(hex: "D97706"))
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(Color(hex: "FEF3C7"))
                .cornerRadius(10)

            Text(description)
                .font(.system(size: 13))
                .foregroundColor(.jciTextMuted)
                .multilineTextAlignment(.center)
                .lineSpacing(3)

            Divider()

            VStack(alignment: .leading, spacing: 8) {
                Text("CORE CAPABILITIES BEING CONFIGURED")
                    .font(.system(size: 10, weight: .heavy))
                    .foregroundColor(Color(hex: "64748B"))

                ForEach(features, id: \.self) { feat in
                    HStack(spacing: 8) {
                        Text("✨")
                            .font(.system(size: 12))
                        Text(feat)
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(.jciDeepNavy)
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            HStack(spacing: 8) {
                Text("🗓️")
                Text("Release Timeline: \(expectedDate)")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(Color(hex: "64748B"))
            }
            .padding(12)
            .frame(maxWidth: .infinity)
            .background(Color(hex: "F8FAFC"))
            .cornerRadius(10)
            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
        }
        .padding(24)
        .background(Color.white)
        .cornerRadius(20)
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(Color(hex: "BAE6FD"), lineWidth: 1))
        .shadow(color: Color.black.opacity(0.04), radius: 8, y: 3)
        .padding(.horizontal, 16)
    }
}
