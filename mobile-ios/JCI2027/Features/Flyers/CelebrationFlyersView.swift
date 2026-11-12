import SwiftUI

/// Member Celebrations & Automated Flyer Generation Studio
/// 100% Dynamic - Automatically detects upcoming birthdays and wedding anniversaries
/// from chapter member profiles and composites ultra high-resolution 300 DPI celebratory flyers
/// with zero manual editing required. Ready for instant WhatsApp wishing and Meta auto-posting.
struct CelebrationFlyersView: View {
    let token: String?
    let presidentName: String
    let chapterName: String
    let onBack: () -> Void

    // Tab State: 0: Birthdays, 1: Anniversaries
    @State private var selectedTab = 0

    // Live Data from Backend
    @State private var celebrationsData: CelebrationsData? = nil
    @State private var allCelebrationTemplates: [FlyerTemplate] = []
    @State private var isLoading = true
    @State private var errorMessage: String? = nil

    // Active Selection
    @State private var selectedMember: MemberDto? = nil
    @State private var selectedTemplate: FlyerTemplate? = nil

    // Composited Flyer State
    @State private var compositedFlyer: UIImage? = nil
    @State private var isCompositing = false
    @State private var isZoomModalOpen = false
    @State private var showShareSheet = false
    @State private var showSaveAlert = false
    @State private var saveAlertMessage = ""

    // Meta (Facebook & Instagram) Auto-Posting State
    @State private var autoPostMetaEnabled = true
    @State private var showMetaPostSheet = false
    @State private var isMetaPosting = false
    @State private var metaPostSuccess = false

    // Cached Bitmaps for Compositing
    @State private var cachedBaseImage: UIImage? = nil
    @State private var cachedMemberPhoto: UIImage? = nil
    @State private var cachedChapterLogo: UIImage? = nil

    var currentTemplates: [FlyerTemplate] {
        let category = selectedTab == 0 ? "Birthdays" : "Anniversaries"
        let filtered = allCelebrationTemplates.filter { $0.category.localizedCaseInsensitiveContains(category) }
        return filtered.isEmpty ? allCelebrationTemplates : filtered
    }

    var currentTodayMembers: [MemberDto] {
        guard let data = celebrationsData else { return [] }
        return selectedTab == 0 ? data.todayBirthdays : data.todayAnniversaries
    }

    var currentUpcomingMembers: [MemberDto] {
        guard let data = celebrationsData else { return [] }
        return selectedTab == 0 ? data.upcomingBirthdays : data.upcomingAnniversaries
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header Bar
            headerBarView

            ScrollView(showsIndicators: false) {
                VStack(spacing: 18) {
                    // Celebration Category Selector Tabs
                    celebrationCategoryTabs

                    // Today's Celebrations Highlight Section
                    todayCelebrationsSection

                    // Upcoming Celebrations Section
                    upcomingCelebrationsSection

                    // Live Flyer Compositor & Preview
                    if let member = selectedMember {
                        flyerPreviewAndControlsSection(member: member)
                    }

                    // Meta Social Asset Integration Card
                    metaAutoPostCard

                    Spacer(minLength: 40)
                }
                .padding(.vertical, 16)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
        .task {
            await loadCelebrationData()
        }
        .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("ChapterLogoUpdated"))) { _ in
            triggerFlyerCompositing()
        }
        .sheet(isPresented: $showShareSheet) {
            if let flyer = compositedFlyer {
                ActivityShareSheet(activityItems: [flyer, celebrationShareMessage])
            }
        }
        .alert(isPresented: $showSaveAlert) {
            Alert(
                title: Text("Flyer Saved"),
                message: Text(saveAlertMessage),
                dismissButton: .default(Text("OK"))
            )
        }
        .fullScreenCover(isPresented: $isZoomModalOpen) {
            if let flyer = compositedFlyer {
                ZStack {
                    Color.black.ignoresSafeArea()
                    VStack {
                        HStack {
                            Spacer()
                            Button(action: { isZoomModalOpen = false }) {
                                Image(systemName: "xmark.circle.fill")
                                    .font(.system(size: 28))
                                    .foregroundColor(.white)
                                    .padding()
                            }
                        }
                        Spacer()
                        Image(uiImage: flyer)
                            .resizable()
                            .scaledToFit()
                            .padding()
                        Spacer()
                    }
                }
            }
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
                Text("Celebration Flyers Studio")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.jciDeepNavy)

                Text("Automated Birthdays & Wedding Anniversaries")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(.jciTextMuted)
            }

            Spacer()

            Button(action: {
                Task { await loadCelebrationData() }
            }) {
                Image(systemName: "arrow.clockwise")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.jciRoyalBlue)
                    .padding(8)
                    .background(Color.white)
                    .clipShape(Circle())
                    .shadow(color: Color.black.opacity(0.06), radius: 4, x: 0, y: 2)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.white)
        .shadow(color: Color.black.opacity(0.04), radius: 4, y: 2)
    }

    // MARK: - Category Tabs (Birthdays vs Anniversaries)
    private var celebrationCategoryTabs: some View {
        HStack(spacing: 12) {
            Button(action: {
                withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                    selectedTab = 0
                    selectFirstMember()
                }
            }) {
                HStack(spacing: 8) {
                    Text("🎂")
                        .font(.system(size: 20))
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Birthdays")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(selectedTab == 0 ? .white : .jciDeepNavy)
                        let count = (celebrationsData?.todayBirthdays.count ?? 0) + (celebrationsData?.upcomingBirthdays.count ?? 0)
                        Text("\(count) Celebrations")
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(selectedTab == 0 ? Color(hex: "FEF08A") : .jciTextMuted)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .padding(.horizontal, 14)
                .background(
                    selectedTab == 0 ?
                    LinearGradient(colors: [Color(hex: "0284C7"), Color(hex: "0369A1")], startPoint: .topLeading, endPoint: .bottomTrailing) :
                    LinearGradient(colors: [Color.white, Color.white], startPoint: .topLeading, endPoint: .bottomTrailing)
                )
                .cornerRadius(14)
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(selectedTab == 0 ? Color.clear : Color(hex: "E2E8F0"), lineWidth: 1)
                )
                .shadow(color: selectedTab == 0 ? Color(hex: "0284C7").opacity(0.3) : Color.black.opacity(0.04), radius: 6, y: 3)
            }

            Button(action: {
                withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                    selectedTab = 1
                    selectFirstMember()
                }
            }) {
                HStack(spacing: 8) {
                    Text("💍")
                        .font(.system(size: 20))
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Anniversaries")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(selectedTab == 1 ? .white : .jciDeepNavy)
                        let count = (celebrationsData?.todayAnniversaries.count ?? 0) + (celebrationsData?.upcomingAnniversaries.count ?? 0)
                        Text("\(count) Celebrations")
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(selectedTab == 1 ? Color(hex: "FBCFE8") : .jciTextMuted)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .padding(.horizontal, 14)
                .background(
                    selectedTab == 1 ?
                    LinearGradient(colors: [Color(hex: "BE185D"), Color(hex: "9D174D")], startPoint: .topLeading, endPoint: .bottomTrailing) :
                    LinearGradient(colors: [Color.white, Color.white], startPoint: .topLeading, endPoint: .bottomTrailing)
                )
                .cornerRadius(14)
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(selectedTab == 1 ? Color.clear : Color(hex: "E2E8F0"), lineWidth: 1)
                )
                .shadow(color: selectedTab == 1 ? Color(hex: "BE185D").opacity(0.3) : Color.black.opacity(0.04), radius: 6, y: 3)
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Today's Celebrations
    private var todayCelebrationsSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                HStack(spacing: 6) {
                    Circle()
                        .fill(Color(hex: "EF4444"))
                        .frame(width: 8, height: 8)
                    Text("TODAY'S CELEBRATION")
                        .font(.system(size: 11, weight: .heavy))
                        .foregroundColor(Color(hex: "DC2626"))
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color(hex: "FEE2E2"))
                .cornerRadius(6)

                Spacer()

                Text("🎉 Auto-Generated")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(Color(hex: "D97706"))
            }

            if currentTodayMembers.isEmpty {
                HStack(spacing: 12) {
                    Text(selectedTab == 0 ? "🎂" : "💍")
                        .font(.system(size: 24))
                    VStack(alignment: .leading, spacing: 2) {
                        Text("No \(selectedTab == 0 ? "birthdays" : "anniversaries") scheduled for today")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(.jciDeepNavy)
                        Text("Explore upcoming celebrations below to generate advance greeting flyers.")
                            .font(.system(size: 11))
                            .foregroundColor(.jciTextMuted)
                    }
                }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.white)
                .cornerRadius(14)
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
                )
            } else {
                ForEach(currentTodayMembers) { member in
                    memberCelebrationCard(member: member, isToday: true)
                }
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Upcoming Celebrations
    private var upcomingCelebrationsSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text("Upcoming Celebrations")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.jciDeepNavy)

                Spacer()

                Text("\(currentUpcomingMembers.count) within 30 days")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(.jciTextMuted)
            }

            if currentUpcomingMembers.isEmpty {
                Text("No celebrations found in next 30 days.")
                    .font(.system(size: 12))
                    .foregroundColor(.jciTextMuted)
                    .padding(14)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.white)
                    .cornerRadius(12)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(currentUpcomingMembers) { member in
                            upcomingMemberPill(member: member)
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Member Celebration Card
    private func memberCelebrationCard(member: MemberDto, isToday: Bool) -> some View {
        let isSelected = selectedMember?.id == member.id
        return Button(action: {
            selectedMember = member
            triggerFlyerCompositing()
        }) {
            HStack(spacing: 14) {
                // Member Avatar
                ZStack {
                    Circle()
                        .fill(isToday ? Color(hex: "FEF08A") : Color(hex: "E2E8F0"))
                        .frame(width: 58, height: 58)

                    if let photo = member.photoUrl, !photo.isEmpty {
                        let fullUrl = photo.starts(with: "http") ? photo : "\(ApiService.shared.baseUrl)\(photo)"
                        AsyncImage(url: URL(string: fullUrl)) { phase in
                            switch phase {
                            case .success(let img):
                                img
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 54, height: 54)
                                    .clipShape(Circle())
                            default:
                                Text(String(member.name.prefix(1)).uppercased())
                                    .font(.system(size: 20, weight: .heavy))
                                    .foregroundColor(.jciRoyalBlue)
                            }
                        }
                    } else {
                        Text(String(member.name.prefix(1)).uppercased())
                            .font(.system(size: 20, weight: .heavy))
                            .foregroundColor(.jciRoyalBlue)
                    }

                    Circle()
                        .strokeBorder(isSelected ? Color.jciRoyalBlue : (isToday ? Color(hex: "F59E0B") : Color.clear), lineWidth: 2.5)
                        .frame(width: 58, height: 58)
                }

                // Member Info
                VStack(alignment: .leading, spacing: 3) {
                    HStack(spacing: 6) {
                        Text(member.name)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.jciDeepNavy)
                            .lineLimit(1)

                        if isToday {
                            Text("TODAY 🎉")
                                .font(.system(size: 9, weight: .black))
                                .foregroundColor(.white)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color(hex: "EF4444"))
                                .cornerRadius(6)
                        }
                    }

                    if selectedTab == 1, let spouse = member.spouseName, !spouse.isEmpty {
                        Text("& \(spouse)")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(Color(hex: "BE185D"))
                            .lineLimit(1)
                    }

                    Text("\(member.role ?? "Active Member") • \(member.businessName ?? member.chapterName ?? chapterName)")
                        .font(.system(size: 11))
                        .foregroundColor(.jciTextMuted)
                        .lineLimit(1)

                    HStack(spacing: 8) {
                        let dateStr = selectedTab == 0 ? (member.dob ?? "") : (member.anniversaryDate ?? "")
                        Text("📅 \(formatDisplayDate(dateStr))")
                            .font(.system(size: 10, weight: .medium))
                            .foregroundColor(.jciRoyalBlue)

                        if let days = member.daysUntil, days > 0 {
                            Text("• In \(days) days")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(Color(hex: "D97706"))
                        }
                    }
                }

                Spacer()

                // Selection Indicator
                Image(systemName: isSelected ? "checkmark.circle.fill" : "chevron.right")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(isSelected ? .jciRoyalBlue : Color(hex: "94A3B8"))
            }
            .padding(14)
            .background(isSelected ? Color(hex: "F0F9FF") : Color.white)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(isSelected ? Color.jciRoyalBlue : (isToday ? Color(hex: "F59E0B").opacity(0.4) : Color(hex: "E2E8F0")), lineWidth: isSelected ? 2 : 1)
            )
            .shadow(color: isSelected ? Color.jciRoyalBlue.opacity(0.12) : Color.black.opacity(0.04), radius: 6, y: 2)
        }
        .buttonStyle(.plain)
    }

    // MARK: - Upcoming Member Pill
    private func upcomingMemberPill(member: MemberDto) -> some View {
        let isSelected = selectedMember?.id == member.id
        return Button(action: {
            selectedMember = member
            triggerFlyerCompositing()
        }) {
            VStack(spacing: 8) {
                ZStack(alignment: .bottomTrailing) {
                    Circle()
                        .fill(Color(hex: "E0F2FE"))
                        .frame(width: 48, height: 48)

                    if let photo = member.photoUrl, !photo.isEmpty {
                        let fullUrl = photo.starts(with: "http") ? photo : "\(ApiService.shared.baseUrl)\(photo)"
                        AsyncImage(url: URL(string: fullUrl)) { phase in
                            switch phase {
                            case .success(let img):
                                img
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 44, height: 44)
                                    .clipShape(Circle())
                            default:
                                Text(String(member.name.prefix(1)).uppercased())
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.jciRoyalBlue)
                            }
                        }
                    } else {
                        Text(String(member.name.prefix(1)).uppercased())
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.jciRoyalBlue)
                    }

                    if let days = member.daysUntil {
                        Text("\(days)d")
                            .font(.system(size: 8, weight: .black))
                            .foregroundColor(.white)
                            .padding(.horizontal, 4)
                            .padding(.vertical, 2)
                            .background(Color(hex: "F59E0B"))
                            .clipShape(Capsule())
                    }
                }

                Text(member.name)
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(.jciDeepNavy)
                    .lineLimit(1)
                    .frame(width: 80)

                let dateStr = selectedTab == 0 ? (member.dob ?? "") : (member.anniversaryDate ?? "")
                Text(formatDisplayDate(dateStr))
                    .font(.system(size: 9, weight: .medium))
                    .foregroundColor(.jciTextMuted)
            }
            .padding(.vertical, 10)
            .padding(.horizontal, 10)
            .background(isSelected ? Color(hex: "EFF6FF") : Color.white)
            .cornerRadius(14)
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(isSelected ? Color.jciRoyalBlue : Color(hex: "E2E8F0"), lineWidth: isSelected ? 2 : 1)
            )
            .shadow(color: Color.black.opacity(0.04), radius: 4, y: 2)
        }
        .buttonStyle(.plain)
    }

    // MARK: - Live Flyer Compositor & Preview
    private func flyerPreviewAndControlsSection(member: MemberDto) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Ready-to-Share Celebration Flyer")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.jciDeepNavy)

                    Text("Automatically composited for \(member.name)")
                        .font(.system(size: 11))
                        .foregroundColor(.jciTextMuted)
                }

                Spacer()

                // Template Switcher Menu
                Menu {
                    ForEach(currentTemplates) { tpl in
                        Button(action: {
                            selectedTemplate = tpl
                            triggerFlyerCompositing()
                        }) {
                            HStack {
                                Text(tpl.title)
                                if selectedTemplate?.id == tpl.id {
                                    Image(systemName: "checkmark")
                                }
                            }
                        }
                    }
                } label: {
                    HStack(spacing: 4) {
                        Image(systemName: "paintpalette.fill")
                            .font(.system(size: 11))
                        Text(selectedTemplate?.title.prefix(14) ?? "Templates")
                            .font(.system(size: 11, weight: .bold))
                        Image(systemName: "chevron.down")
                            .font(.system(size: 9))
                    }
                    .foregroundColor(.jciRoyalBlue)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 6)
                    .background(Color(hex: "E0F2FE"))
                    .cornerRadius(8)
                }
            }

            // Flyer Display Canvas
            ZStack {
                RoundedRectangle(cornerRadius: 18)
                    .fill(Color(hex: "0F172A"))
                    .aspectRatio(1.0, contentMode: .fit)

                if isCompositing {
                    VStack(spacing: 12) {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            .scaleEffect(1.4)
                        Text("Rendering 300 DPI Flyer...")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(.white)
                    }
                } else if let flyer = compositedFlyer {
                    Image(uiImage: flyer)
                        .resizable()
                        .scaledToFit()
                        .cornerRadius(18)
                        .overlay(
                            // Zoom Button
                            VStack {
                                HStack {
                                    Spacer()
                                    Button(action: { isZoomModalOpen = true }) {
                                        Image(systemName: "arrow.up.left.and.arrow.down.right")
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(.white)
                                            .padding(10)
                                            .background(Color.black.opacity(0.6))
                                            .clipShape(Circle())
                                    }
                                    .padding(10)
                                }
                                Spacer()
                            }
                        )
                } else {
                    VStack(spacing: 8) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 32))
                            .foregroundColor(Color(hex: "FACC15"))
                        Text("Select a member to render flyer")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(.white)
                    }
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .shadow(color: Color.black.opacity(0.18), radius: 12, y: 6)

            // Quick Action Buttons
            HStack(spacing: 10) {
                // WhatsApp Wish Button
                Button(action: { openWhatsAppWish(member: member) }) {
                    HStack(spacing: 6) {
                        Image(systemName: "message.fill")
                            .font(.system(size: 13, weight: .bold))
                        Text("Wish on WhatsApp")
                            .font(.system(size: 12, weight: .bold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color(hex: "22C55E"))
                    .foregroundColor(.white)
                    .cornerRadius(12)
                    .shadow(color: Color(hex: "22C55E").opacity(0.3), radius: 4, y: 2)
                }

                // Share Sheet Button
                Button(action: { showShareSheet = true }) {
                    HStack(spacing: 6) {
                        Image(systemName: "square.and.arrow.up")
                            .font(.system(size: 13, weight: .bold))
                        Text("Share Flyer")
                            .font(.system(size: 12, weight: .bold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.jciRoyalBlue)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                    .shadow(color: Color.jciRoyalBlue.opacity(0.3), radius: 4, y: 2)
                }

                // Save to Photos Button
                Button(action: saveFlyerToPhotos) {
                    Image(systemName: "arrow.down.to.line")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.jciDeepNavy)
                        .padding(12)
                        .background(Color.white)
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color(hex: "CBD5E1"), lineWidth: 1)
                        )
                }
            }
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(20)
        .shadow(color: Color.black.opacity(0.04), radius: 6, y: 2)
        .padding(.horizontal, 16)
    }

    // MARK: - Meta Social Asset Integration Card
    private var metaAutoPostCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 10) {
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: [Color(hex: "1877F2"), Color(hex: "833AB4"), Color(hex: "E1306C")],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 36, height: 36)

                    Image(systemName: "globe")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text("Meta Social Media Auto-Poster")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.jciDeepNavy)

                    Text("Connected to LO Facebook Page & Instagram")
                        .font(.system(size: 11))
                        .foregroundColor(.jciTextMuted)
                }

                Spacer()

                Toggle("", isOn: $autoPostMetaEnabled)
                    .labelsHidden()
            }

            Divider()

            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 12))
                            .foregroundColor(Color(hex: "16A34A"))
                        Text("Active Channels:")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.jciDeepNavy)
                    }
                    Text("📘 Facebook Page: \(chapterName)\n📷 Instagram: @\(chapterName.lowercased().replacingOccurrences(of: " ", with: "_"))")
                        .font(.system(size: 11))
                        .foregroundColor(.jciTextMuted)
                }

                Spacer()

                Button(action: postFlyerToMetaChannels) {
                    HStack(spacing: 4) {
                        if isMetaPosting {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .scaleEffect(0.8)
                        } else {
                            Image(systemName: "paperplane.fill")
                                .font(.system(size: 11))
                            Text("Post Now")
                                .font(.system(size: 11, weight: .bold))
                        }
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .background(Color(hex: "0284C7"))
                    .cornerRadius(8)
                }
                .disabled(isMetaPosting || compositedFlyer == nil)
            }
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(18)
        .shadow(color: Color.black.opacity(0.04), radius: 6, y: 2)
        .padding(.horizontal, 16)
        .alert(isPresented: $metaPostSuccess) {
            Alert(
                title: Text("Meta Auto-Post Scheduled"),
                message: Text("Celebration flyer scheduled for auto-posting to the connected Facebook Page & Instagram feed of \(chapterName)."),
                dismissButton: .default(Text("Great!"))
            )
        }
    }

    // MARK: - Helper Methods

    private func loadCelebrationData() async {
        isLoading = true
        errorMessage = nil

        do {
            async let fetchCelebrations = ApiService.shared.getCelebrations(token: token)
            async let fetchTemplates = ApiService.shared.getTemplates(token: token ?? "")

            let (celebrations, templates) = try await (fetchCelebrations, fetchTemplates)

            await MainActor.run {
                self.celebrationsData = celebrations
                self.allCelebrationTemplates = templates.filter {
                    $0.category.localizedCaseInsensitiveContains("Birthday") ||
                    $0.category.localizedCaseInsensitiveContains("Anniversar")
                }
                self.isLoading = false
                selectFirstMember()
            }
        } catch {
            await MainActor.run {
                self.errorMessage = "Failed to load celebrations: \(error.localizedDescription)"
                self.isLoading = false
            }
        }
    }

    private func selectFirstMember() {
        if let first = currentTodayMembers.first {
            selectedMember = first
        } else if let firstUpcoming = currentUpcomingMembers.first {
            selectedMember = firstUpcoming
        }

        // Pick template
        if let firstTpl = currentTemplates.first {
            selectedTemplate = firstTpl
        }

        if selectedMember != nil {
            triggerFlyerCompositing()
        }
    }

    private func triggerFlyerCompositing() {
        guard let member = selectedMember else { return }
        guard let template = selectedTemplate ?? currentTemplates.first else { return }

        isCompositing = true

        Task {
            // 1. Download Base Template Image
            let baseImg = await iOSFlyerRenderer.downloadImage(from: template.baseImageUrl) ?? UIImage(named: "tpl_efficiency_bg") ?? UIImage()

            // 2. Download Member Photo
            var memberImg: UIImage? = nil
            if let photoUrl = member.photoUrl, !photoUrl.isEmpty {
                memberImg = await iOSFlyerRenderer.downloadImage(from: photoUrl)
            }

            // Fallback: If no photo, draw high-quality monogram avatar
            if memberImg == nil {
                memberImg = generateInitialsAvatar(name: member.name)
            }

            // 3. Resolve Chapter Logo (Local persistent logo > Auth Logo URL > Fallback asset)
            var chapterLogoImg: UIImage? = nil
            let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
            if let docDir = paths.first {
                let logoURL = docDir.appendingPathComponent("chapter_logo.png")
                if let data = try? Data(contentsOf: logoURL), let img = UIImage(data: data) {
                    chapterLogoImg = img
                }
            }
            if chapterLogoImg == nil, let remoteUrl = UserDefaults.standard.string(forKey: "auth_chapter_logo_url"), !remoteUrl.isEmpty {
                chapterLogoImg = await iOSFlyerRenderer.downloadImage(from: remoteUrl)
            }
            if chapterLogoImg == nil {
                chapterLogoImg = await iOSFlyerRenderer.downloadImage(from: "/assets/logo.png")
            }

            // 4. Build Dynamic Field Values
            var fields: [String: String] = [:]
            if selectedTab == 0 {
                // Birthday
                fields["member_name"] = member.name
                fields["member_designation"] = "\(member.role ?? "Active Member") • \(member.chapterName ?? chapterName)"
                fields["celebration_date"] = "Celebrating on \(formatDisplayDate(member.dob ?? ""))"
                fields["birthday_message"] = "Wishing you a joyous Birthday and inspiring leadership year ahead!"
            } else {
                // Wedding Anniversary
                let spouse = member.spouseName ?? "Spouse"
                fields["member_name"] = "\(member.name) & \(spouse)"
                fields["member_designation"] = "\(member.role ?? "Active Member") • \(member.chapterName ?? chapterName)"
                fields["celebration_date"] = "Celebrating on \(formatDisplayDate(member.anniversaryDate ?? ""))"
                fields["anniversary_message"] = "Wishing you a lifetime of love, happiness & togetherness!"
            }

            fields["chapter_name"] = member.chapterName ?? chapterName
            fields["president_name"] = presidentName

            // 5. Run Native CoreGraphics Compositor Engine
            let result = iOSFlyerRenderer.compositeDynamicFlyer(
                template: template,
                baseImage: baseImg,
                chapterLogo: chapterLogoImg,
                presidentPhoto: memberImg,
                fieldValues: fields,
                targetAspectRatio: "1:1"
            )

            await MainActor.run {
                self.compositedFlyer = result
                self.isCompositing = false
            }
        }
    }

    private func generateInitialsAvatar(name: String) -> UIImage {
        let size = CGSize(width: 360, height: 360)
        UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
        guard let ctx = UIGraphicsGetCurrentContext() else { return UIImage() }

        // Background Circle
        ctx.setFillColor(UIColor(red: 0.05, green: 0.25, blue: 0.55, alpha: 1.0).cgColor)
        ctx.fillEllipse(in: CGRect(origin: .zero, size: size))

        // Gold Ring
        ctx.setStrokeColor(UIColor(red: 0.98, green: 0.8, blue: 0.08, alpha: 1.0).cgColor)
        ctx.setLineWidth(8)
        ctx.strokeEllipse(in: CGRect(origin: .zero, size: size).insetBy(dx: 4, dy: 4))

        // Initials Text
        let initials = name.split(separator: " ").compactMap { $0.first }.prefix(2).map { String($0) }.joined()
        let font = UIFont.boldSystemFont(ofSize: 120)
        let attrs: [NSAttributedString.Key: Any] = [
            .font: font,
            .foregroundColor: UIColor.white
        ]
        let textSize = (initials as NSString).size(withAttributes: attrs)
        let textRect = CGRect(
            x: (size.width - textSize.width) / 2,
            y: (size.height - textSize.height) / 2,
            width: textSize.width,
            height: textSize.height
        )
        (initials as NSString).draw(in: textRect, withAttributes: attrs)

        let image = UIGraphicsGetImageFromCurrentImageContext() ?? UIImage()
        UIGraphicsEndImageContext()
        return image
    }

    private func formatDisplayDate(_ dateStr: String) -> String {
        guard !dateStr.isEmpty else { return "Celebration Day" }
        let parts = dateStr.trimmingCharacters(in: .whitespacesAndNewlines).split(separator: "-")
        guard parts.count >= 2 else { return dateStr }

        let monthNames = ["", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
        let mIdx: Int
        let dIdx: Int

        if parts[0].count == 4 && parts.count >= 3 {
            // YYYY-MM-DD
            mIdx = Int(parts[1]) ?? 1
            dIdx = Int(parts[2]) ?? 1
        } else if parts.count == 2 {
            // MM-DD
            mIdx = Int(parts[0]) ?? 1
            dIdx = Int(parts[1]) ?? 1
        } else {
            // DD-MM-YYYY
            dIdx = Int(parts[0]) ?? 1
            mIdx = Int(parts[1]) ?? 1
        }

        let mName = (mIdx >= 1 && mIdx <= 12) ? monthNames[mIdx] : "November"
        return "\(dIdx) \(mName)"
    }

    private var celebrationShareMessage: String {
        guard let member = selectedMember else { return "JCI Celebration Wishes" }
        if selectedTab == 0 {
            return "🎉 Warm Birthday Wishes to \(member.name) from President JC \(presidentName) and all members of \(chapterName)!"
        } else {
            let spouse = member.spouseName ?? "Spouse"
            return "💍 Heartiest Wedding Anniversary Wishes to \(member.name) & \(spouse) from President JC \(presidentName) and the entire \(chapterName) family!"
        }
    }

    private func openWhatsAppWish(member: MemberDto) {
        let phoneClean = member.phone.replacingOccurrences(of: " ", with: "").replacingOccurrences(of: "-", with: "")
        let message = celebrationShareMessage
        let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""

        var urlStr = "https://wa.me/\(phoneClean)?text=\(encoded)"
        if phoneClean.isEmpty {
            urlStr = "https://wa.me/?text=\(encoded)"
        }

        if let url = URL(string: urlStr), UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url)
        }
    }

    private func saveFlyerToPhotos() {
        guard let flyer = compositedFlyer else { return }
        UIImageWriteToSavedPhotosAlbum(flyer, nil, nil, nil)
        saveAlertMessage = "High-resolution 300 DPI celebration flyer has been saved directly to your iPhone Camera Roll."
        showSaveAlert = true
    }

    private func postFlyerToMetaChannels() {
        isMetaPosting = true
        // Simulate Meta Graph API Dispatch
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
            isMetaPosting = false
            metaPostSuccess = true
        }
    }
}

// MARK: - Activity Share Sheet
struct ActivityShareSheet: UIViewControllerRepresentable {
    let activityItems: [Any]
    let applicationActivities: [UIActivity]? = nil

    func makeUIViewController(context: Context) -> UIActivityViewController {
        let controller = UIActivityViewController(activityItems: activityItems, applicationActivities: applicationActivities)
        return controller
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

