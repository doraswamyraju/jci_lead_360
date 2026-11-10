import SwiftUI

/// President Flyer Studio & Instant 300 DPI Compositor
/// 100% Feature Parity with Android PresidentFlyerStudioScreen.kt
/// Strictly live backend templates from /api/templates with CoreGraphics compositing.
struct PresidentFlyerStudioView: View {
    let token: String
    let presidentName: String
    let chapterName: String
    var userId: String = "usr_pres_01"
    var serverPhotoUrl: String? = nil

    // Live Templates State (Strictly Live from Backend APIs)
    @State private var templates: [FlyerTemplate] = []
    @State private var isLoadingTemplates = true
    @State private var selectedCategory = "All"
    @State private var selectedTemplate: FlyerTemplate? = nil

    // Studio Mode: 0: Template Catalogue Grid, 1: Live Editor & Compositor
    @State private var studioMode = 0

    // Aspect Ratio Selector: 1:1 Square vs 4:5 Story
    @State private var selectedAspectRatio = "1:1"

    // Full-screen Zoom Modal State
    @State private var isZoomModalOpen = false
    @State private var zoomScale: CGFloat = 1.0

    // Form Field Parameters
    @State private var eventTitle = "1st LOM General Body Meeting 2027"
    @State private var eventDate = Date()
    @State private var eventTime = Date()
    @State private var eventVenue = "JCI Bhavan, Tirupati"
    @State private var chiefGuest = "JC Sen. Rajesh Singhania (National President)"
    @State private var customPresidentName: String = ""
    @State private var customChapterName: String = ""

    // Quick Venue Presets
    let venuePresets = [
        "JCI Bhavan",
        "Hotel Fortune Select Grand Ridge",
        "Hotel Bliss",
        "Rotary Hall",
        "Virtual / Zoom Meeting"
    ]

    // Compositing & Export State
    @State private var isCompositing = false
    @State private var compositedFlyer: UIImage? = nil
    @State private var showShareSheet = false
    @State private var showSaveAlert = false
    @State private var saveAlertMessage = ""

    // Bitmaps cached for compositing
    @State private var baseTemplateImage: UIImage? = nil
    @State private var presidentPhotoImage: UIImage? = nil
    @State private var chapterLogoImage: UIImage? = nil

    var filteredTemplates: [FlyerTemplate] {
        if selectedCategory == "All" {
            return templates
        }
        return templates.filter { $0.category.lowercased() == selectedCategory.lowercased() }
    }

    var availableCategories: [String] {
        var cats = ["All"]
        let uniqueCats = Array(Set(templates.map { $0.category })).sorted()
        cats.append(contentsOf: uniqueCats)
        return cats
    }

    var body: some View {
        VStack(spacing: 0) {
            // ==========================================
            // TOP STUDIO NAVIGATION SWITCHER
            // ==========================================
            HStack(spacing: 8) {
                Button(action: { studioMode = 0 }) {
                    HStack(spacing: 6) {
                        Text("🗂️")
                        Text("Catalogue (\(templates.count))")
                            .font(.system(size: 13, weight: .bold))
                    }
                    .foregroundColor(studioMode == 0 ? .white : Color(hex: "475569"))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(studioMode == 0 ? Color.jciRoyalBlue : Color.white)
                    .cornerRadius(12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color(hex: "E2E8F0"), lineWidth: studioMode == 0 ? 0 : 1)
                    )
                }
                .buttonStyle(.plain)

                Button(action: { studioMode = 1 }) {
                    HStack(spacing: 6) {
                        Text("⚡")
                        Text("Live Studio")
                            .font(.system(size: 13, weight: .bold))
                    }
                    .foregroundColor(studioMode == 1 ? .white : Color(hex: "475569"))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(studioMode == 1 ? Color.jciSkyBlue : Color.white)
                    .cornerRadius(12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color(hex: "E2E8F0"), lineWidth: studioMode == 1 ? 0 : 1)
                    )
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 8)
            .background(Color.white)

            Divider()

            // ==========================================
            // CONTENT VIEW (CATALOGUE GRID OR LIVE STUDIO)
            // ==========================================
            if isLoadingTemplates {
                VStack(spacing: 16) {
                    Spacer()
                    ProgressView()
                        .scaleEffect(1.3)
                        .progressViewStyle(CircularProgressViewStyle(tint: .jciRoyalBlue))
                    Text("Loading Live 2027 Templates from Server...")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.jciTextMuted)
                    Spacer()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if templates.isEmpty {
                VStack(spacing: 14) {
                    Spacer()
                    Text("📭")
                        .font(.system(size: 40))
                    Text("No templates found on server")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.jciDeepNavy)
                    Button(action: { Task { await fetchLiveTemplates() } }) {
                        Text("Retry Connection")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 20)
                            .padding(.vertical, 10)
                            .background(Color.jciRoyalBlue)
                            .cornerRadius(10)
                    }
                    Spacer()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if studioMode == 0 {
                // VIEW 0: FULL TEMPLATE CATALOGUE GRID
                catalogueGridView
            } else {
                // VIEW 1: LIVE STUDIO & COMPOSITOR
                liveStudioView
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
        .onAppear {
            if customPresidentName.isEmpty { customPresidentName = presidentName }
            if customChapterName.isEmpty { customChapterName = chapterName }
            loadCachedUserImages()
        }
        .task {
            await fetchLiveTemplates()
        }
        .sheet(isPresented: $showShareSheet) {
            if let flyer = compositedFlyer {
                ShareActivitySheet(activityItems: [
                    flyer,
                    "🌟 \(eventTitle)\n📅 \(formattedDateTimeString())\n📍 \(eventVenue)\n\nCordially invited by JC \(customPresidentName) (President, \(customChapterName))\nJCI India Zone XXVI"
                ])
            }
        }
        .alert(isPresented: $showSaveAlert) {
            Alert(
                title: Text("Photo Library"),
                message: Text(saveAlertMessage),
                dismissButton: .default(Text("OK"))
            )
        }
        .fullScreenCover(isPresented: $isZoomModalOpen) {
            fullscreenZoomModalView
        }
    }

    // ==========================================
    // CATALOGUE GRID VIEW
    // ==========================================
    private var catalogueGridView: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 14) {
                // Category Filter Pills
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(availableCategories, id: \.self) { cat in
                            Button(action: { selectedCategory = cat }) {
                                Text(cat)
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(selectedCategory == cat ? .white : .jciDeepNavy)
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 7)
                                    .background(selectedCategory == cat ? Color.jciDeepNavy : Color.white)
                                    .cornerRadius(20)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 20)
                                            .stroke(Color(hex: "CBD5E1"), lineWidth: selectedCategory == cat ? 0 : 1)
                                    )
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 10)
                }

                // 2-Column Grid of Live Templates
                LazyVGrid(columns: [GridItem(.flexible(), spacing: 12), GridItem(.flexible(), spacing: 12)], spacing: 14) {
                    ForEach(filteredTemplates) { tpl in
                        templateGridCard(tpl)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 30)
            }
        }
    }

    private func templateGridCard(_ tpl: FlyerTemplate) -> some View {
        let isSelected = selectedTemplate?.id == tpl.id
        let fullImageUrl = tpl.baseImageUrl.starts(with: "http") ? tpl.baseImageUrl : "\(ApiService.shared.baseUrl)\(tpl.baseImageUrl)"

        return VStack(alignment: .leading, spacing: 0) {
            // Thumbnail preview with aspect ratio 1:1
            ZStack(alignment: .topTrailing) {
                AsyncImage(url: URL(string: fullImageUrl)) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                            .frame(maxWidth: .infinity)
                            .frame(height: 160)
                            .clipped()
                    case .failure:
                        ZStack {
                            Color(hex: "0F172A")
                            Text("Template Preview")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(.white.opacity(0.8))
                        }
                        .frame(height: 160)
                    default:
                        ZStack {
                            Color(hex: "F1F5F9")
                            ProgressView()
                        }
                        .frame(height: 160)
                    }
                }

                // Quick Zoom Button
                Button(action: {
                    selectedTemplate = tpl
                    isZoomModalOpen = true
                }) {
                    Image(systemName: "arrow.up.left.and.arrow.down.right")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.white)
                        .padding(6)
                        .background(Color.black.opacity(0.6))
                        .clipShape(Circle())
                        .padding(8)
                }
                .buttonStyle(.plain)
            }

            // Template Details & Personalize Action
            VStack(alignment: .leading, spacing: 6) {
                Text(tpl.title)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.jciDeepNavy)
                    .lineLimit(2)
                    .frame(height: 34, alignment: .topLeading)

                HStack {
                    Text(tpl.category)
                        .font(.system(size: 9, weight: .heavy))
                        .foregroundColor(.jciRoyalBlue)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.jciSkyBlue.opacity(0.12))
                        .cornerRadius(4)

                    Spacer()

                    Text("1080×1080")
                        .font(.system(size: 9, weight: .medium))
                        .foregroundColor(.jciTextMuted)
                }

                Button(action: {
                    selectTemplateAndOpenStudio(tpl)
                }) {
                    HStack(spacing: 4) {
                        Image(systemName: "sparkles")
                        Text(isSelected ? "Active" : "Personalize")
                    }
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 7)
                    .background(isSelected ? Color.jciShieldTeal : Color.jciSkyBlue)
                    .cornerRadius(8)
                }
                .buttonStyle(.plain)
                .padding(.top, 4)
            }
            .padding(10)
        }
        .background(Color.white)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(isSelected ? Color.jciSkyBlue : Color(hex: "E2E8F0"), lineWidth: isSelected ? 2 : 1)
        )
        .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 3)
    }

    // ==========================================
    // LIVE STUDIO & COMPOSITOR VIEW
    // ==========================================
    private var liveStudioView: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 16) {
                // 1. LIVE COMPOSITE PREVIEW VIEWPORT WITH ASPECT RATIO SWITCH
                VStack(spacing: 10) {
                    // Control Header
                    HStack {
                        // Aspect Ratio Toggle Chips
                        HStack(spacing: 6) {
                            Button(action: {
                                selectedAspectRatio = "1:1"
                                triggerLiveComposite()
                            }) {
                                Text("1:1 Square")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(selectedAspectRatio == "1:1" ? .white : .jciDeepNavy)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 5)
                                    .background(selectedAspectRatio == "1:1" ? Color.jciRoyalBlue : Color(hex: "F1F5F9"))
                                    .cornerRadius(8)
                            }
                            .buttonStyle(.plain)

                            Button(action: {
                                selectedAspectRatio = "4:5"
                                triggerLiveComposite()
                            }) {
                                Text("4:5 Story")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(selectedAspectRatio == "4:5" ? .white : .jciDeepNavy)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 5)
                                    .background(selectedAspectRatio == "4:5" ? Color.jciRoyalBlue : Color(hex: "F1F5F9"))
                                    .cornerRadius(8)
                            }
                            .buttonStyle(.plain)
                        }

                        Spacer()

                        // Fullscreen Zoom Trigger
                        Button(action: { isZoomModalOpen = true }) {
                            HStack(spacing: 4) {
                                Image(systemName: "plus.magnifyingglass")
                                Text("Zoom")
                            }
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.jciRoyalBlue)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(Color.jciRoyalBlue.opacity(0.1))
                            .cornerRadius(8)
                        }
                        .buttonStyle(.plain)
                    }

                    // Rendered Canvas Preview (Clickable to Zoom)
                    ZStack {
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color(hex: "0A192F"))

                        if isCompositing {
                            VStack(spacing: 10) {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                Text("Compositing 300 DPI Graphics...")
                                    .font(.system(size: 12, weight: .semibold))
                                    .foregroundColor(.white.opacity(0.8))
                            }
                        } else if let flyer = compositedFlyer {
                            Image(uiImage: flyer)
                                .resizable()
                                .scaledToFit()
                                .cornerRadius(16)
                                .onTapGesture {
                                    isZoomModalOpen = true
                                }
                        } else if let base = baseTemplateImage {
                            Image(uiImage: base)
                                .resizable()
                                .scaledToFit()
                                .cornerRadius(16)
                                .onTapGesture {
                                    isZoomModalOpen = true
                                }
                        } else {
                            ProgressView()
                        }
                    }
                    .frame(height: selectedAspectRatio == "1:1" ? 300 : 375)
                    .shadow(color: Color.black.opacity(0.2), radius: 10, x: 0, y: 5)

                    // Export / Share Actions Bar
                    HStack(spacing: 10) {
                        // 1. Save to Photos
                        Button(action: saveFlyerToCameraRoll) {
                            HStack(spacing: 6) {
                                Image(systemName: "square.and.arrow.down.fill")
                                Text("Save to Photos")
                            }
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.jciDeepNavy)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color.white)
                            .cornerRadius(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color(hex: "CBD5E1"), lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)

                        // 2. WhatsApp Direct Broadcast
                        Button(action: shareToWhatsAppDirect) {
                            HStack(spacing: 6) {
                                Text("💬")
                                Text("WhatsApp")
                            }
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color(hex: "25D366"))
                            .cornerRadius(12)
                        }
                        .buttonStyle(.plain)

                        // 3. ShareSheet (AirDrop, Messages, Socials)
                        Button(action: { showShareSheet = true }) {
                            HStack(spacing: 6) {
                                Image(systemName: "square.and.arrow.up")
                                Text("Share")
                            }
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color.jciRoyalBlue)
                            .cornerRadius(12)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(14)
                .background(Color.white)
                .cornerRadius(18)
                .padding(.horizontal, 16)
                .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)

                // 2. QUICK HORIZONTAL TEMPLATE CAROUSEL
                if let currentTpl = selectedTemplate {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("More in \(currentTpl.category)")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.jciDeepNavy)
                            .padding(.horizontal, 16)

                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 10) {
                                ForEach(templates.filter { $0.category == currentTpl.category }) { tpl in
                                    Button(action: { selectTemplateAndOpenStudio(tpl) }) {
                                        ZStack {
                                            let fullUrl = tpl.baseImageUrl.starts(with: "http") ? tpl.baseImageUrl : "\(ApiService.shared.baseUrl)\(tpl.baseImageUrl)"
                                            AsyncImage(url: URL(string: fullUrl)) { phase in
                                                if let img = phase.image {
                                                    img.resizable().scaledToFill()
                                                } else {
                                                    Color(hex: "0F172A")
                                                }
                                            }
                                            .frame(width: 80, height: 80)
                                            .clipShape(RoundedRectangle(cornerRadius: 10))

                                            if selectedTemplate?.id == tpl.id {
                                                RoundedRectangle(cornerRadius: 10)
                                                    .stroke(Color.jciSkyBlue, lineWidth: 3)
                                            }
                                        }
                                        .frame(width: 80, height: 80)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                    }
                }

                // 3. SMART EVENT DETAILS FORM
                VStack(alignment: .leading, spacing: 14) {
                    HStack {
                        Text("Personalize Flyer Details")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.jciDeepNavy)
                        Spacer()
                        Text("Auto-Compositing")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(Color(hex: "0284C7"))
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(Color(hex: "F0F9FF"))
                            .cornerRadius(10)
                    }

                    // Event Title
                    VStack(alignment: .leading, spacing: 5) {
                        Text("Event Title")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(.jciTextMuted)
                        TextField("Event Title", text: $eventTitle)
                            .textFieldStyle(PlainTextFieldStyle())
                            .padding(12)
                            .background(Color(hex: "F8FAFC"))
                            .cornerRadius(10)
                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
                            .onChange(of: eventTitle) { _ in triggerLiveComposite() }
                    }

                    // Date & Time Picker
                    HStack(spacing: 12) {
                        VStack(alignment: .leading, spacing: 5) {
                            Text("Event Date")
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundColor(.jciTextMuted)
                            DatePicker("", selection: $eventDate, displayedComponents: [.date])
                                .labelsHidden()
                                .onChange(of: eventDate) { _ in triggerLiveComposite() }
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)

                        VStack(alignment: .leading, spacing: 5) {
                            Text("Time")
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundColor(.jciTextMuted)
                            DatePicker("", selection: $eventTime, displayedComponents: [.hourAndMinute])
                                .labelsHidden()
                                .onChange(of: eventTime) { _ in triggerLiveComposite() }
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }

                    // Venue with Quick Presets
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Venue / Meeting Hall")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(.jciTextMuted)

                        TextField("Venue", text: $eventVenue)
                            .textFieldStyle(PlainTextFieldStyle())
                            .padding(12)
                            .background(Color(hex: "F8FAFC"))
                            .cornerRadius(10)
                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
                            .onChange(of: eventVenue) { _ in triggerLiveComposite() }

                        // Venue Presets
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 6) {
                                ForEach(venuePresets, id: \.self) { preset in
                                    Button(action: {
                                        eventVenue = preset
                                        triggerLiveComposite()
                                    }) {
                                        Text(preset)
                                            .font(.system(size: 10, weight: .semibold))
                                            .foregroundColor(eventVenue == preset ? .white : Color(hex: "475569"))
                                            .padding(.horizontal, 10)
                                            .padding(.vertical, 5)
                                            .background(eventVenue == preset ? Color.jciSkyBlue : Color(hex: "F1F5F9"))
                                            .cornerRadius(6)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                    }

                    // Chief Guest
                    VStack(alignment: .leading, spacing: 5) {
                        Text("Chief Guest / Keynote Speaker")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(.jciTextMuted)
                        TextField("Chief Guest", text: $chiefGuest)
                            .textFieldStyle(PlainTextFieldStyle())
                            .padding(12)
                            .background(Color(hex: "F8FAFC"))
                            .cornerRadius(10)
                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
                            .onChange(of: chiefGuest) { _ in triggerLiveComposite() }
                    }

                    // President & Chapter Name Pre-filled
                    HStack(spacing: 12) {
                        VStack(alignment: .leading, spacing: 5) {
                            Text("President Name")
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundColor(.jciTextMuted)
                            TextField("President", text: $customPresidentName)
                                .textFieldStyle(PlainTextFieldStyle())
                                .padding(12)
                                .background(Color(hex: "F8FAFC"))
                                .cornerRadius(10)
                                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
                                .onChange(of: customPresidentName) { _ in triggerLiveComposite() }
                        }
                        .frame(maxWidth: .infinity)

                        VStack(alignment: .leading, spacing: 5) {
                            Text("Chapter Name")
                                .font(.system(size: 11, weight: .semibold))
                                .foregroundColor(.jciTextMuted)
                            TextField("Chapter", text: $customChapterName)
                                .textFieldStyle(PlainTextFieldStyle())
                                .padding(12)
                                .background(Color(hex: "F8FAFC"))
                                .cornerRadius(10)
                                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
                                .onChange(of: customChapterName) { _ in triggerLiveComposite() }
                        }
                        .frame(maxWidth: .infinity)
                    }
                }
                .padding(18)
                .background(Color.white)
                .cornerRadius(18)
                .padding(.horizontal, 16)
                .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)

                Spacer(minLength: 30)
            }
        }
    }

    // ==========================================
    // FULLSCREEN INTERACTIVE ZOOM MODAL
    // ==========================================
    private var fullscreenZoomModalView: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            if let flyer = compositedFlyer ?? baseTemplateImage {
                Image(uiImage: flyer)
                    .resizable()
                    .scaledToFit()
                    .scaleEffect(zoomScale)
                    .gesture(
                        MagnificationGesture()
                            .onChanged { val in
                                zoomScale = max(1.0, min(val, 4.0))
                            }
                    )
            }

            VStack {
                HStack {
                    Button(action: { zoomScale = 1.0 }) {
                        Text("Reset Zoom")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(Color.white.opacity(0.2))
                            .cornerRadius(8)
                    }

                    Spacer()

                    Button(action: {
                        zoomScale = 1.0
                        isZoomModalOpen = false
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 26))
                            .foregroundColor(.white)
                    }
                }
                .padding(20)

                Spacer()
            }
        }
    }

    // ==========================================
    // ACTIONS & LIVE COMPOSITING
    // ==========================================
    private func fetchLiveTemplates() async {
        guard !token.isEmpty else { return }
        isLoadingTemplates = true

        do {
            let fetched = try await ApiService.shared.getTemplates(token: token)
            await MainActor.run {
                self.templates = fetched
                if self.selectedTemplate == nil {
                    self.selectedTemplate = fetched.first
                }
                self.isLoadingTemplates = false
            }
            if let first = fetched.first {
                await loadBaseTemplateBitmap(first)
            }
        } catch {
            await MainActor.run {
                self.isLoadingTemplates = false
            }
        }
    }

    private func selectTemplateAndOpenStudio(_ tpl: FlyerTemplate) {
        self.selectedTemplate = tpl
        self.studioMode = 1
        Task {
            await loadBaseTemplateBitmap(tpl)
        }
    }

    private func loadBaseTemplateBitmap(_ tpl: FlyerTemplate) async {
        let fullUrl = tpl.baseImageUrl.starts(with: "http") ? tpl.baseImageUrl : "\(ApiService.shared.baseUrl)\(tpl.baseImageUrl)"
        if let img = await iOSFlyerRenderer.downloadImage(from: fullUrl) {
            await MainActor.run {
                self.baseTemplateImage = img
                self.triggerLiveComposite()
            }
        }
    }

    private func loadCachedUserImages() {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        if let docDir = paths.first {
            let portraitURL = docDir.appendingPathComponent("president_portrait.jpg")
            if let data = try? Data(contentsOf: portraitURL), let img = UIImage(data: data) {
                self.presidentPhotoImage = img
            }
            let logoURL = docDir.appendingPathComponent("chapter_logo.png")
            if let data = try? Data(contentsOf: logoURL), let img = UIImage(data: data) {
                self.chapterLogoImage = img
            }
        }

        // If local portrait is nil but remote URL is present, download it
        if self.presidentPhotoImage == nil, let photo = serverPhotoUrl ?? UserDefaults.standard.string(forKey: "auth_photo_url"), !photo.isEmpty {
            Task {
                if let downloaded = await iOSFlyerRenderer.downloadImage(from: photo) {
                    await MainActor.run {
                        self.presidentPhotoImage = downloaded
                        self.triggerLiveComposite()
                    }
                }
            }
        }
    }

    private func triggerLiveComposite() {
        guard let tpl = selectedTemplate, let base = baseTemplateImage else { return }

        isCompositing = true
        let dtString = formattedDateTimeString()

        let fieldMap: [String: String] = [
            "event_title": eventTitle,
            "title": eventTitle,
            "event_date_time": dtString,
            "date_time": dtString,
            "date": dtString,
            "event_venue": eventVenue,
            "venue": eventVenue,
            "location": eventVenue,
            "chief_guest": chiefGuest,
            "speaker": chiefGuest,
            "president_name": customPresidentName,
            "chapter_name": customChapterName
        ]

        DispatchQueue.global(qos: .userInitiated).async {
            let result = iOSFlyerRenderer.compositeDynamicFlyer(
                template: tpl,
                baseImage: base,
                chapterLogo: self.chapterLogoImage,
                presidentPhoto: self.presidentPhotoImage,
                fieldValues: fieldMap,
                targetAspectRatio: self.selectedAspectRatio
            )

            DispatchQueue.main.async {
                self.compositedFlyer = result
                self.isCompositing = false
            }
        }
    }

    private func formattedDateTimeString() -> String {
        let df = DateFormatter()
        df.dateFormat = "EEEE, d MMM yyyy"
        let datePart = df.string(from: eventDate)

        let tf = DateFormatter()
        tf.dateFormat = "h:mm a"
        let timePart = tf.string(from: eventTime)

        return "\(datePart) • \(timePart)"
    }

    private func saveFlyerToCameraRoll() {
        guard let flyer = compositedFlyer ?? baseTemplateImage else { return }
        UIImageWriteToSavedPhotosAlbum(flyer, nil, nil, nil)
        let impact = UINotificationFeedbackGenerator()
        impact.notificationOccurred(.success)
        saveAlertMessage = "High-Resolution 300 DPI Flyer saved successfully to your Photos Camera Roll!"
        showSaveAlert = true
    }

    private func shareToWhatsAppDirect() {
        let caption = "🌟 *\(eventTitle)*\n📅 \(formattedDateTimeString())\n📍 \(eventVenue)\n\nInvited by JC \(customPresidentName) (President, \(customChapterName))\nJCI India Zone XXVI"
        let encoded = caption.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        if let waUrl = URL(string: "whatsapp://send?text=\(encoded)"), UIApplication.shared.canOpenURL(waUrl) {
            UIApplication.shared.open(waUrl)
        } else {
            // Fallback to native share sheet
            showShareSheet = true
        }
    }
}

// MARK: - Reusable Share Activity Sheet
struct ShareActivitySheet: UIViewControllerRepresentable {
    let activityItems: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: activityItems, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
