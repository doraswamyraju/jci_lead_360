import SwiftUI
import MapKit
import Contacts
import CoreLocation

/// Members Directory, Business Networking & Approvals View
/// 100% Parity with Android MembersAndNetworkingScreen.kt
/// Live member data, business categories, contact actions, MapKit networking map, and instant Celebration Flyer integration.
struct MembersAndNetworkingView: View {
    let chapterName: String
    let token: String?
    let onBack: () -> Void
    let onNavigateToScanCard: () -> Void
    var onOpenCelebrationFlyer: ((MemberDto) -> Void)? = nil

    @State private var members: [MemberDto] = []
    @State private var isLoading = true
    @State private var searchQuery = ""
    @State private var selectedFilter = 0 // 0: All, 1: LGB Board, 2: Active, 3: Pending Approvals
    @State private var showMapSheet = false
    @State private var selectedMemberForPreview: MemberDto? = nil
    @State private var memberToEdit: MemberDto? = nil
    @State private var memberToExchange: MemberDto? = nil
    @State private var showAddMemberSheet = false
    @State private var statusToastMessage: String? = nil

    let filters = ["All", "LGB Board", "Active", "Pending"]

    var filteredMembers: [MemberDto] {
        members.filter { m in
            let matchesSearch = searchQuery.isEmpty ||
                m.name.localizedCaseInsensitiveContains(searchQuery) ||
                (m.businessName ?? "").localizedCaseInsensitiveContains(searchQuery) ||
                (m.designation ?? "").localizedCaseInsensitiveContains(searchQuery) ||
                (m.city ?? "").localizedCaseInsensitiveContains(searchQuery)

            let matchesFilter: Bool = {
                switch selectedFilter {
                case 1:
                    let r = (m.role ?? "").lowercased()
                    return r.contains("president") || r.contains("vp") || r.contains("secretary") || r.contains("treasurer") || r.contains("director")
                case 2:
                    return (m.status ?? "active").lowercased() == "active"
                case 3:
                    return (m.status ?? "").lowercased() == "pending"
                default:
                    return true
                }
            }()

            return matchesSearch && matchesFilter
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header Bar
            headerBarView

            // Search Bar
            searchBarView

            // Filter Tabs
            Picker("Filter", selection: $selectedFilter) {
                ForEach(0..<filters.count, id: \.self) { idx in
                    Text(filters[idx]).tag(idx)
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)

            // Status Banner if present
            if let toast = statusToastMessage {
                HStack(spacing: 8) {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                    Text(toast)
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(.jciDeepNavy)
                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(Color(hex: "ECFDF5"))
                .transition(.move(edge: .top).combined(with: .opacity))
            }

            // Member List Content
            if isLoading && members.isEmpty {
                Spacer()
                ProgressView("Syncing Directory...")
                    .progressViewStyle(CircularProgressViewStyle(tint: .jciRoyalBlue))
                Spacer()
            } else if filteredMembers.isEmpty {
                emptyStateView
            } else {
                ScrollView(showsIndicators: false) {
                    LazyVStack(spacing: 12) {
                        ForEach(filteredMembers) { member in
                            MemberRowCard(
                                member: member,
                                onTap: { selectedMemberForPreview = member },
                                onEdit: { memberToEdit = member }
                            )
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .padding(.bottom, 24)
                }
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
        .onAppear {
            loadMembers()
        }
        // Member Detail Bottom Sheet
        .sheet(item: $selectedMemberForPreview) { member in
            MemberDetailSheet(
                member: member,
                chapterName: chapterName,
                onApprove: { approveMember(member.id) },
                onEdit: { memberToEdit = $0 },
                onExchange: { memberToExchange = $0 },
                onCelebrationFlyer: {
                    selectedMemberForPreview = nil
                    onOpenCelebrationFlyer?(member)
                }
            )
            .presentationDetents([.medium, .large])
        }
        // Edit Member Sheet (President action)
        .sheet(item: $memberToEdit) { member in
            EditMemberModalSheet(
                member: member,
                chapterName: chapterName,
                token: token,
                onSuccess: { updated in
                    memberToEdit = nil
                    showToast("Member profile updated successfully")
                    loadMembers()
                },
                onDismiss: { memberToEdit = nil }
            )
        }
        // Contact Exchange Sheet
        .sheet(item: $memberToExchange) { member in
            ContactExchangeSheet(member: member, chapterName: chapterName)
                .presentationDetents([.medium, .large])
        }
        // Chapter Networking Map Sheet
        .sheet(isPresented: $showMapSheet) {
            MembersMapSheet(
                members: members,
                chapterName: chapterName,
                token: token,
                onSelectMember: { member in
                    showMapSheet = false
                    selectedMemberForPreview = member
                },
                onExchangeContact: { member in
                    showMapSheet = false
                    memberToExchange = member
                }
            )
        }
        // Add Member Modal
        .sheet(isPresented: $showAddMemberSheet) {
            AddMemberModalSheet(
                chapterName: chapterName,
                token: token,
                onSuccess: {
                    showAddMemberSheet = false
                    showToast("New member registered successfully")
                    loadMembers()
                },
                onDismiss: { showAddMemberSheet = false }
            )
        }
    }

    private func showToast(_ msg: String) {
        withAnimation { statusToastMessage = msg }
        DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
            withAnimation { statusToastMessage = nil }
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
                Text("Members & Networking")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(.jciDeepNavy)

                Text("\(members.count) Registered Leaders • \(chapterName)")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(.jciTextMuted)
            }

            Spacer()

            // Map View Toggle
            Button(action: { showMapSheet.toggle() }) {
                HStack(spacing: 4) {
                    Image(systemName: "map.fill")
                        .font(.system(size: 13, weight: .semibold))
                    Text("Map")
                        .font(.system(size: 12, weight: .bold))
                }
                .foregroundColor(.jciRoyalBlue)
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .background(Color.white)
                .cornerRadius(16)
                .shadow(color: Color.black.opacity(0.06), radius: 4, x: 0, y: 2)
            }

            // Add Member Action
            Button(action: { showAddMemberSheet.toggle() }) {
                Image(systemName: "plus")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(.white)
                    .padding(8)
                    .background(Color.jciRoyalBlue)
                    .clipShape(Circle())
                    .shadow(color: Color.jciRoyalBlue.opacity(0.3), radius: 4, x: 0, y: 2)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.white)
        .shadow(color: Color.black.opacity(0.04), radius: 4, y: 2)
    }

    // MARK: - Search Bar
    private var searchBarView: some View {
        HStack(spacing: 8) {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.jciTextMuted)
            TextField("Search name, business category, city...", text: $searchQuery)
                .font(.system(size: 13))
            if !searchQuery.isEmpty {
                Button(action: { searchQuery = "" }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.jciTextMuted)
                }
            }
        }
        .padding(10)
        .background(Color.white)
        .cornerRadius(12)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
        .padding(.horizontal, 16)
        .padding(.top, 10)
    }

    private var emptyStateView: some View {
        VStack(spacing: 12) {
            Spacer()
            Image(systemName: "person.2.slash")
                .font(.system(size: 44))
                .foregroundColor(.jciTextMuted.opacity(0.6))
            Text("No members found")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(.jciTextDark)
            Text("Try searching with another name, company, or category.")
                .font(.system(size: 13))
                .foregroundColor(.jciTextMuted)
            Spacer()
        }
        .padding(32)
    }

    private func loadMembers() {
        guard let token = token else {
            isLoading = false
            return
        }
        Task {
            do {
                let fetched = try await ApiService.shared.getMembers(token: token)
                await MainActor.run {
                    self.members = fetched
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.isLoading = false
                }
            }
        }
    }

    private func approveMember(_ id: String) {
        guard let token = token else { return }
        Task {
            let ok = try? await ApiService.shared.approveMember(token: token, memberId: id)
            if ok == true {
                await MainActor.run {
                    showToast("Member successfully approved")
                    loadMembers()
                }
            }
        }
    }
}

// MARK: - Member Row Card
struct MemberRowCard: View {
    let member: MemberDto
    let onTap: () -> Void
    var onEdit: (() -> Void)? = nil

    var isPending: Bool {
        member.status?.lowercased() == "pending"
    }

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                // Profile Avatar with Role Ring
                ZStack {
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
                                    .frame(width: 46, height: 46)
                                    .clipShape(Circle())
                            default:
                                Text(String(member.name.prefix(1)).uppercased())
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(.jciRoyalBlue)
                            }
                        }
                    } else {
                        Text(String(member.name.prefix(1)).uppercased())
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.jciRoyalBlue)
                    }

                    Circle()
                        .strokeBorder(isPending ? Color.orange : Color.jciRoyalBlue.opacity(0.8), lineWidth: 2)
                        .frame(width: 48, height: 48)

                    if member.isLocationSharingEnabled == true {
                        Circle()
                            .fill(Color.green)
                            .frame(width: 10, height: 10)
                            .overlay(Circle().stroke(Color.white, lineWidth: 1.5))
                            .offset(x: 18, y: -18)
                    }
                }

                // Member Name & Business Designation
                VStack(alignment: .leading, spacing: 3) {
                    HStack(spacing: 6) {
                        Text(member.name)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.jciDeepNavy)
                            .lineLimit(1)

                        if isPending {
                            Text("PENDING")
                                .font(.system(size: 9, weight: .heavy))
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color.orange.opacity(0.15))
                                .foregroundColor(.orange)
                                .cornerRadius(4)
                        }
                    }

                    Text(member.role ?? "Active Member")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.jciRoyalBlue)
                        .lineLimit(1)

                    if let biz = member.businessName, !biz.isEmpty {
                        Text(biz + (member.designation.map { " • " + $0 } ?? ""))
                            .font(.system(size: 11))
                            .foregroundColor(.jciTextMuted)
                            .lineLimit(1)
                    }
                }

                Spacer()

                // Edit Button Shortcut
                if let onEdit = onEdit {
                    Button(action: onEdit) {
                        Image(systemName: "pencil.circle.fill")
                            .font(.system(size: 22))
                            .foregroundColor(Color(hex: "0284C7").opacity(0.85))
                    }
                    .buttonStyle(.plain)
                    .padding(.trailing, 4)
                }

                Image(systemName: "chevron.right")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(Color(hex: "CBD5E1"))
            }
            .padding(14)
            .background(Color.white)
            .cornerRadius(14)
            .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Member Detail Bottom Sheet
struct MemberDetailSheet: View {
    let member: MemberDto
    let chapterName: String
    let onApprove: () -> Void
    var onEdit: ((MemberDto) -> Void)? = nil
    var onExchange: ((MemberDto) -> Void)? = nil
    var onCelebrationFlyer: (() -> Void)? = nil

    @Environment(\.dismiss) var dismiss
    @State private var isSavingToContacts = false
    @State private var saveContactAlertMessage: String? = nil
    @State private var showSaveAlert = false

    var isPending: Bool {
        member.status?.lowercased() == "pending"
    }

    var body: some View {
        NavigationStack {
            ScrollView(showsIndicators: false) {
                VStack(spacing: 16) {
                    // Profile Header Card
                    VStack(spacing: 8) {
                        ZStack {
                            Circle()
                                .fill(Color(hex: "E0F2FE"))
                                .frame(width: 80, height: 80)

                            if let photo = member.photoUrl, !photo.isEmpty {
                                let fullUrl = photo.starts(with: "http") ? photo : "\(ApiService.shared.baseUrl)\(photo)"
                                AsyncImage(url: URL(string: fullUrl)) { phase in
                                    switch phase {
                                    case .success(let img):
                                        img
                                            .resizable()
                                            .scaledToFill()
                                            .frame(width: 76, height: 76)
                                            .clipShape(Circle())
                                    default:
                                        Text(String(member.name.prefix(1)).uppercased())
                                            .font(.system(size: 32, weight: .black))
                                            .foregroundColor(.jciRoyalBlue)
                                    }
                                }
                            } else {
                                Text(String(member.name.prefix(1)).uppercased())
                                    .font(.system(size: 32, weight: .black))
                                    .foregroundColor(.jciRoyalBlue)
                            }

                            Circle()
                                .strokeBorder(isPending ? Color.orange : Color.jciRoyalBlue, lineWidth: 3)
                                .frame(width: 80, height: 80)
                        }

                        Text(member.name)
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.jciDeepNavy)

                        Text("\(member.role ?? "Active Member") • \(member.chapterName ?? chapterName)")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(.jciRoyalBlue)

                        HStack(spacing: 8) {
                            if let bg = member.bloodGroup, !bg.isEmpty {
                                Label("Blood: \(bg)", systemImage: "drop.fill")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(Color(hex: "DC2626"))
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(Color(hex: "FEE2E2"))
                                    .cornerRadius(6)
                            }
                            if let join = member.joinYear, !join.isEmpty {
                                Text("Joined \(join)")
                                    .font(.system(size: 11, weight: .semibold))
                                    .foregroundColor(.jciTextMuted)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(Color(hex: "F1F5F9"))
                                    .cornerRadius(6)
                            }
                        }
                    }
                    .padding(.top, 10)

                    // Quick Action Contact Grid (Call, WhatsApp, Email, Save Contact, Exchange)
                    VStack(spacing: 10) {
                        HStack(spacing: 10) {
                            Button(action: { callPhone(member.phone) }) {
                                VStack(spacing: 4) {
                                    Image(systemName: "phone.fill")
                                        .font(.system(size: 15))
                                    Text("Call")
                                        .font(.system(size: 11, weight: .bold))
                                }
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 10)
                                .background(Color(hex: "0284C7"))
                                .foregroundColor(.white)
                                .cornerRadius(10)
                            }

                            Button(action: { openWhatsApp(member.phone) }) {
                                VStack(spacing: 4) {
                                    Image(systemName: "message.fill")
                                        .font(.system(size: 15))
                                    Text("WhatsApp")
                                        .font(.system(size: 11, weight: .bold))
                                }
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 10)
                                .background(Color(hex: "22C55E"))
                                .foregroundColor(.white)
                                .cornerRadius(10)
                            }

                            if let email = member.email, !email.isEmpty {
                                Button(action: { sendEmail(email) }) {
                                    VStack(spacing: 4) {
                                        Image(systemName: "envelope.fill")
                                            .font(.system(size: 15))
                                        Text("Email")
                                            .font(.system(size: 11, weight: .bold))
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 10)
                                    .background(Color(hex: "6366F1"))
                                    .foregroundColor(.white)
                                    .cornerRadius(10)
                                }
                            }
                        }

                        // Secondary Action Bar: Save to Phonebook + Exchange Contact
                        HStack(spacing: 10) {
                            Button(action: saveToPhonebook) {
                                HStack(spacing: 6) {
                                    if isSavingToContacts {
                                        ProgressView().tint(Color(hex: "0369A1"))
                                    } else {
                                        Image(systemName: "person.crop.circle.badge.plus")
                                            .font(.system(size: 14, weight: .bold))
                                    }
                                    Text("Save to Contacts")
                                        .font(.system(size: 12, weight: .bold))
                                }
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 10)
                                .background(Color(hex: "E0F2FE"))
                                .foregroundColor(Color(hex: "0369A1"))
                                .cornerRadius(10)
                            }
                            .disabled(isSavingToContacts)

                            Button(action: {
                                dismiss()
                                onExchange?(member)
                            }) {
                                HStack(spacing: 6) {
                                    Image(systemName: "arrow.triangle.2.circlepath")
                                        .font(.system(size: 13, weight: .bold))
                                    Text("Exchange Card")
                                        .font(.system(size: 12, weight: .bold))
                                }
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 10)
                                .background(Color(hex: "FEF3C7"))
                                .foregroundColor(Color(hex: "B45309"))
                                .cornerRadius(10)
                            }
                        }
                    }
                    .padding(.horizontal, 16)

                    // Business & Milestone Parameters
                    VStack(alignment: .leading, spacing: 12) {
                        HStack {
                            Text("MEMBER PROFILE & OCCUPATION")
                                .font(.system(size: 11, weight: .black))
                                .foregroundColor(.jciDeepNavy)
                            Spacer()
                            Button(action: {
                                dismiss()
                                onEdit?(member)
                            }) {
                                HStack(spacing: 4) {
                                    Image(systemName: "pencil")
                                    Text("Edit")
                                }
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.jciRoyalBlue)
                            }
                        }

                        if let biz = member.businessName, !biz.isEmpty {
                            DetailInfoRow(icon: "briefcase.fill", title: "Business Name", value: biz)
                        }
                        if let desig = member.designation, !desig.isEmpty {
                            DetailInfoRow(icon: "person.text.rectangle.fill", title: "Designation", value: desig)
                        }
                        if let cat = member.businessCategory, !cat.isEmpty {
                            DetailInfoRow(icon: "tag.fill", title: "Category", value: cat)
                        }
                        if let city = member.city, !city.isEmpty {
                            DetailInfoRow(icon: "mappin.and.ellipse", title: "City / Location", value: city)
                        }
                        if let dob = member.dob, !dob.isEmpty {
                            DetailInfoRow(icon: "gift.fill", title: "Date of Birth", value: dob)
                        }
                        if let ann = member.anniversaryDate, !ann.isEmpty {
                            let spouse = member.spouseName ?? ""
                            DetailInfoRow(icon: "heart.fill", title: "Wedding Anniversary", value: spouse.isEmpty ? ann : "\(ann) (with \(spouse))")
                        }
                    }
                    .padding(16)
                    .background(Color.white)
                    .cornerRadius(16)
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
                    .padding(.horizontal, 16)

                    // Celebration Flyer Action
                    if member.dob?.isEmpty == false || member.anniversaryDate?.isEmpty == false {
                        Button(action: { onCelebrationFlyer?() }) {
                            HStack(spacing: 8) {
                                Text("🎉")
                                Text("Generate Celebration Flyer")
                                    .font(.system(size: 13, weight: .bold))
                                Spacer()
                                Image(systemName: "chevron.right")
                            }
                            .padding(14)
                            .background(Color(hex: "F0FDF4"))
                            .foregroundColor(Color(hex: "15803D"))
                            .cornerRadius(14)
                            .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color(hex: "BBF7D0"), lineWidth: 1))
                        }
                        .padding(.horizontal, 16)
                    }

                    // Approval Action
                    if isPending {
                        Button(action: {
                            onApprove()
                            dismiss()
                        }) {
                            Text("✅ Approve as Active Chapter Member")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 48)
                                .background(Color(hex: "16A34A"))
                                .cornerRadius(12)
                        }
                        .padding(.horizontal, 16)
                    }

                    Spacer(minLength: 20)
                }
                .padding(.vertical, 16)
            }
            .navigationTitle("Member Details")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: {
                        dismiss()
                        onEdit?(member)
                    }) {
                        Text("Edit")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.jciRoyalBlue)
                    }
                }
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
            }
            .alert(isPresented: $showSaveAlert) {
                Alert(
                    title: Text("Contacts"),
                    message: Text(saveContactAlertMessage ?? ""),
                    dismissButton: .default(Text("OK"))
                )
            }
        }
    }

    private func saveToPhonebook() {
        isSavingToContacts = true
        let haptic = UINotificationFeedbackGenerator()
        Task {
            do {
                _ = try await ContactManager.shared.saveMemberToPhonebook(member: member, chapterName: chapterName)
                await MainActor.run {
                    isSavingToContacts = false
                    haptic.notificationOccurred(.success)
                    saveContactAlertMessage = "✅ \(member.name) has been saved directly into your iPhone Contacts with chapter affiliation & photo."
                    showSaveAlert = true
                }
            } catch {
                await MainActor.run {
                    isSavingToContacts = false
                    haptic.notificationOccurred(.error)
                    saveContactAlertMessage = "Could not save contact: \(error.localizedDescription)"
                    showSaveAlert = true
                }
            }
        }
    }

    private func callPhone(_ phone: String) {
        let clean = phone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()
        if let url = URL(string: "tel://\(clean)"), UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url)
        }
    }

    private func openWhatsApp(_ phone: String) {
        let clean = phone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()
        if let url = URL(string: "https://wa.me/\(clean)"), UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url)
        }
    }

    private func sendEmail(_ email: String) {
        if let url = URL(string: "mailto:\(email)"), UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url)
        }
    }
}

private struct DetailInfoRow: View {
    let icon: String
    let title: String
    let value: String

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(.jciRoyalBlue)
                .frame(width: 20)
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.jciTextMuted)
                Text(value)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(.jciDeepNavy)
            }
            Spacer()
        }
    }
}

// MARK: - Edit Member Modal Sheet (President Capability)
struct EditMemberModalSheet: View {
    let member: MemberDto
    let chapterName: String
    let token: String?
    let onSuccess: (MemberDto) -> Void
    let onDismiss: () -> Void

    @State private var name: String
    @State private var phone: String
    @State private var email: String
    @State private var role: String
    @State private var businessName: String
    @State private var designation: String
    @State private var businessCategory: String
    @State private var bloodGroup: String
    @State private var dob: String
    @State private var anniversaryDate: String
    @State private var spouseName: String
    @State private var city: String
    @State private var status: String
    @State private var isSubmitting = false
    @State private var errorMessage: String? = nil

    let bloodGroups = ["A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"]
    let roles = ["President", "Immediate Past President", "Vice President", "Secretary", "Treasurer", "Director - Growth", "Director - Programs", "Director - Training", "Active Member"]
    let statuses = ["Active", "Pending", "Inactive"]

    init(member: MemberDto, chapterName: String, token: String?, onSuccess: @escaping (MemberDto) -> Void, onDismiss: @escaping () -> Void) {
        self.member = member
        self.chapterName = chapterName
        self.token = token
        self.onSuccess = onSuccess
        self.onDismiss = onDismiss

        _name = State(initialValue: member.name)
        _phone = State(initialValue: member.phone)
        _email = State(initialValue: member.email ?? "")
        _role = State(initialValue: member.role ?? "Active Member")
        _businessName = State(initialValue: member.businessName ?? "")
        _designation = State(initialValue: member.designation ?? "")
        _businessCategory = State(initialValue: member.businessCategory ?? "General")
        _bloodGroup = State(initialValue: member.bloodGroup ?? "O+")
        _dob = State(initialValue: member.dob ?? "")
        _anniversaryDate = State(initialValue: member.anniversaryDate ?? "")
        _spouseName = State(initialValue: member.spouseName ?? "")
        _city = State(initialValue: member.city ?? "Tirupati")
        _status = State(initialValue: member.status ?? "Active")
    }

    var body: some View {
        NavigationStack {
            Form {
                if let error = errorMessage {
                    Section {
                        Text(error)
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(.red)
                    }
                }

                Section(header: Text("Personal Details")) {
                    TextField("Full Name *", text: $name)
                    TextField("Mobile Phone *", text: $phone)
                        .keyboardType(.phonePad)
                    TextField("Email Address", text: $email)
                        .keyboardType(.emailAddress)
                        .textInputAutocapitalization(.never)

                    Picker("Chapter Role / Title", selection: $role) {
                        ForEach(roles, id: \.self) { r in
                            Text(r).tag(r)
                        }
                    }

                    Picker("Membership Status", selection: $status) {
                        ForEach(statuses, id: \.self) { s in
                            Text(s).tag(s)
                        }
                    }
                }

                Section(header: Text("Business & Occupation")) {
                    TextField("Company / Organization Name", text: $businessName)
                    TextField("Designation (e.g. Founder & CEO)", text: $designation)
                    TextField("Category (e.g. IT, Healthcare, Real Estate)", text: $businessCategory)
                    TextField("City / Location", text: $city)
                }

                Section(header: Text("Milestones & Celebrations")) {
                    Picker("Blood Group", selection: $bloodGroup) {
                        ForEach(bloodGroups, id: \.self) { bg in
                            Text(bg).tag(bg)
                        }
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Date of Birth (YYYY-MM-DD)")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(.jciTextMuted)
                        TextField("e.g. 1992-05-18", text: $dob)
                    }

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Wedding Anniversary (YYYY-MM-DD)")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundColor(.jciTextMuted)
                        TextField("e.g. 2018-11-12", text: $anniversaryDate)
                    }

                    TextField("Spouse Full Name", text: $spouseName)
                }
            }
            .navigationTitle("Edit Member Details")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { onDismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(action: saveChanges) {
                        if isSubmitting {
                            ProgressView()
                        } else {
                            Text("Save")
                                .bold()
                        }
                    }
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty || phone.trimmingCharacters(in: .whitespaces).isEmpty || isSubmitting)
                }
            }
        }
    }

    private func saveChanges() {
        guard let token = token else { return }
        isSubmitting = true
        errorMessage = nil

        var updated = member
        updated.name = name.trimmingCharacters(in: .whitespaces)
        updated.phone = phone.trimmingCharacters(in: .whitespaces)
        updated.email = email.trimmingCharacters(in: .whitespaces)
        updated.role = role
        updated.status = status
        updated.businessName = businessName.trimmingCharacters(in: .whitespaces)
        updated.designation = designation.trimmingCharacters(in: .whitespaces)
        updated.businessCategory = businessCategory.trimmingCharacters(in: .whitespaces)
        updated.bloodGroup = bloodGroup
        updated.dob = dob.trimmingCharacters(in: .whitespaces)
        updated.anniversaryDate = anniversaryDate.trimmingCharacters(in: .whitespaces)
        updated.spouseName = spouseName.trimmingCharacters(in: .whitespaces)
        updated.city = city.trimmingCharacters(in: .whitespaces)

        Task {
            do {
                let ok = try await ApiService.shared.updateMember(token: token, member: updated)
                await MainActor.run {
                    isSubmitting = false
                    if ok {
                        onSuccess(updated)
                    } else {
                        errorMessage = "Could not save changes to server. Please try again."
                    }
                }
            } catch {
                await MainActor.run {
                    isSubmitting = false
                    errorMessage = error.localizedDescription
                }
            }
        }
    }
}

// MARK: - Contact Exchange Sheet
struct ContactExchangeSheet: View {
    let member: MemberDto
    let chapterName: String
    @Environment(\.dismiss) var dismiss

    @State private var qrImage: UIImage? = nil
    @State private var showShareSheet = false
    @State private var isSavedToContacts = false
    @State private var toastMessage: String? = nil

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                // Header with member preview
                VStack(spacing: 6) {
                    Text(member.name)
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(.jciDeepNavy)

                    Text("\(member.role ?? "Active Member") • \(member.chapterName ?? chapterName)")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.jciRoyalBlue)

                    if let biz = member.businessName, !biz.isEmpty {
                        Text(biz)
                            .font(.system(size: 12))
                            .foregroundColor(.jciTextMuted)
                    }
                }
                .padding(.top, 16)

                // QR Code Display Card
                VStack(spacing: 12) {
                    if let qr = qrImage {
                        Image(uiImage: qr)
                            .interpolation(.none)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 220, height: 220)
                            .padding(16)
                            .background(Color.white)
                            .cornerRadius(16)
                            .shadow(color: Color.black.opacity(0.08), radius: 8, x: 0, y: 3)
                    } else {
                        Rectangle()
                            .fill(Color(hex: "F1F5F9"))
                            .frame(width: 220, height: 220)
                            .cornerRadius(16)
                            .overlay(ProgressView())
                    }

                    Text("Scan to instantly save \(member.name)'s vCard")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.jciTextMuted)
                }

                // Action Buttons
                VStack(spacing: 12) {
                    Button(action: saveToContacts) {
                        HStack(spacing: 8) {
                            Image(systemName: "person.crop.circle.badge.plus")
                            Text("Save to My Phonebook")
                                .font(.system(size: 14, weight: .bold))
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(Color.jciRoyalBlue)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                    }

                    HStack(spacing: 12) {
                        Button(action: shareViaWhatsApp) {
                            HStack(spacing: 6) {
                                Image(systemName: "message.fill")
                                Text("Send on WhatsApp")
                                    .font(.system(size: 13, weight: .bold))
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 44)
                            .background(Color(hex: "22C55E"))
                            .foregroundColor(.white)
                            .cornerRadius(10)
                        }

                        Button(action: { showShareSheet = true }) {
                            HStack(spacing: 6) {
                                Image(systemName: "square.and.arrow.up")
                                Text("Share vCard")
                                    .font(.system(size: 13, weight: .bold))
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 44)
                            .background(Color(hex: "E2E8F0"))
                            .foregroundColor(.jciDeepNavy)
                            .cornerRadius(10)
                        }
                    }
                }
                .padding(.horizontal, 24)

                Spacer()
            }
            .navigationTitle("Exchange Contact")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Done") { dismiss() }
                }
            }
            .onAppear {
                generateQr()
            }
            .sheet(isPresented: $showShareSheet) {
                let vcardString = ContactManager.shared.generateVCardString(member: member, chapterName: chapterName)
                ActivityShareSheet(activityItems: [vcardString])
            }
        }
    }

    private func generateQr() {
        let vcard = ContactManager.shared.generateVCardString(member: member, chapterName: chapterName)
        self.qrImage = QrCodeGenerator.generateQrImage(from: vcard, size: CGSize(width: 480, height: 480))
    }

    private func saveToContacts() {
        Task {
            _ = try? await ContactManager.shared.saveMemberToPhonebook(member: member, chapterName: chapterName)
            await MainActor.run {
                let haptic = UINotificationFeedbackGenerator()
                haptic.notificationOccurred(.success)
                dismiss()
            }
        }
    }

    private func shareViaWhatsApp() {
        let vcard = ContactManager.shared.generateVCardString(member: member, chapterName: chapterName)
        let message = "Hello! Here is the verified JCI Lead360 Contact for \(member.name):\n\nPhone: \(member.phone)\nChapter: \(member.chapterName ?? chapterName)\nRole: \(member.role ?? "Active Member")"
        let encoded = message.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        if let url = URL(string: "https://wa.me/?text=\(encoded)") {
            UIApplication.shared.open(url)
        }
    }
}

// MARK: - Add Member Modal Sheet
struct AddMemberModalSheet: View {
    let chapterName: String
    let token: String?
    let onSuccess: () -> Void
    let onDismiss: () -> Void

    @State private var name = ""
    @State private var phone = ""
    @State private var email = ""
    @State private var role = "Active Member"
    @State private var businessName = ""
    @State private var designation = ""
    @State private var businessCategory = "General"
    @State private var bloodGroup = "O+"
    @State private var dob = ""
    @State private var anniversaryDate = ""
    @State private var spouseName = ""
    @State private var isSubmitting = false

    let bloodGroups = ["A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"]

    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text("Basic Info")) {
                    TextField("Full Name *", text: $name)
                    TextField("Mobile Phone *", text: $phone)
                        .keyboardType(.phonePad)
                    TextField("Email Address", text: $email)
                        .keyboardType(.emailAddress)
                        .textInputAutocapitalization(.never)
                    TextField("Chapter Role / Title", text: $role)
                }

                Section(header: Text("Business / Occupation")) {
                    TextField("Business / Organization Name", text: $businessName)
                    TextField("Designation (e.g. Managing Director)", text: $designation)
                    TextField("Category (e.g. IT, Healthcare)", text: $businessCategory)
                }

                Section(header: Text("Milestones & Celebrations")) {
                    Picker("Blood Group", selection: $bloodGroup) {
                        ForEach(bloodGroups, id: \.self) { bg in
                            Text(bg).tag(bg)
                        }
                    }
                    TextField("Date of Birth (YYYY-MM-DD)", text: $dob)
                    TextField("Wedding Anniversary (YYYY-MM-DD)", text: $anniversaryDate)
                    TextField("Spouse Full Name", text: $spouseName)
                }
            }
            .navigationTitle("Add Chapter Member")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { onDismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(action: saveMember) {
                        if isSubmitting {
                            ProgressView()
                        } else {
                            Text("Save")
                                .bold()
                        }
                    }
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty || phone.trimmingCharacters(in: .whitespaces).isEmpty || isSubmitting)
                }
            }
        }
    }

    private func saveMember() {
        guard let token = token else { return }
        isSubmitting = true

        let newMember = MemberDto(
            id: "mem_\(Date().timeIntervalSince1970 * 1000)",
            name: name.trimmingCharacters(in: .whitespaces),
            phone: phone.trimmingCharacters(in: .whitespaces),
            email: email.trimmingCharacters(in: .whitespaces),
            role: role,
            membershipType: "Active Member",
            joinYear: "2027",
            bloodGroup: bloodGroup,
            dob: dob.trimmingCharacters(in: .whitespaces),
            anniversaryDate: anniversaryDate.trimmingCharacters(in: .whitespaces),
            chapterName: chapterName,
            businessName: businessName,
            designation: designation,
            businessCategory: businessCategory,
            city: "Tirupati",
            status: "Active",
            lat: 13.6288 + Double.random(in: -0.02...0.02),
            lng: 79.4192 + Double.random(in: -0.02...0.02),
            spouseName: spouseName,
            isLocationSharingEnabled: true
        )

        Task {
            do {
                _ = try await ApiService.shared.saveMember(token: token, member: newMember)
                await MainActor.run {
                    isSubmitting = false
                    onSuccess()
                }
            } catch {
                await MainActor.run {
                    isSubmitting = false
                }
            }
        }
    }
}

// MARK: - Interactive Chapter Networking Map Sheet with Custom Profile Pic Pins
struct MembersMapSheet: View {
    let members: [MemberDto]
    let chapterName: String
    let token: String?
    var onSelectMember: ((MemberDto) -> Void)? = nil
    var onExchangeContact: ((MemberDto) -> Void)? = nil

    @Environment(\.dismiss) var dismiss
    @StateObject private var locationService = LocationTrackingService.shared

    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 13.6288, longitude: 79.4192),
        span: MKCoordinateSpan(latitudeDelta: 0.08, longitudeDelta: 0.08)
    )
    @State private var selectedPinMember: MemberDto? = nil

    var mappedMembers: [MemberDto] {
        members.filter { m in
            guard let lat = m.lat, let lng = m.lng else { return false }
            return lat != 0 && lng != 0
        }
    }

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottom) {
                // MapKit Canvas
                Map(coordinateRegion: $region, annotationItems: mappedMembers) { member in
                    MapAnnotation(
                        coordinate: CLLocationCoordinate2D(
                            latitude: member.lat ?? 13.6288,
                            longitude: member.lng ?? 79.4192
                        )
                    ) {
                        MemberMapPinAnnotationView(
                            member: member,
                            isSelected: selectedPinMember?.id == member.id
                        ) {
                            withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                selectedPinMember = member
                            }
                        }
                    }
                }
                .ignoresSafeArea(edges: .bottom)

                // Top Location Sharing Status Pill
                VStack {
                    HStack(spacing: 10) {
                        Toggle(isOn: Binding(
                            get: { locationService.isLocationSharingActive },
                            set: { _ in
                                locationService.toggleLocationSharing(token: token, currentMemberId: "usr_pres_01")
                            }
                        )) {
                            HStack(spacing: 6) {
                                Circle()
                                    .fill(locationService.isLocationSharingActive ? Color.green : Color.gray)
                                    .frame(width: 8, height: 8)
                                Text(locationService.isLocationSharingActive ? "Live Location Active" : "Share My Location")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(.jciDeepNavy)
                            }
                        }
                        .toggleStyle(SwitchToggleStyle(tint: .green))

                        Spacer()

                        Text("\(mappedMembers.count) Mapped")
                            .font(.system(size: 11, weight: .bold))
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color.jciRoyalBlue.opacity(0.12))
                            .foregroundColor(.jciRoyalBlue)
                            .cornerRadius(8)
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(Color.white.opacity(0.95))
                    .cornerRadius(14)
                    .shadow(color: Color.black.opacity(0.08), radius: 6, x: 0, y: 2)
                    .padding(.horizontal, 16)
                    .padding(.top, 8)

                    Spacer()
                }

                // Interactive Bottom Preview Card when a pin is tapped
                if let selected = selectedPinMember {
                    VStack(spacing: 12) {
                        HStack(spacing: 12) {
                            // Avatar
                            ZStack {
                                Circle()
                                    .fill(Color(hex: "E0F2FE"))
                                    .frame(width: 50, height: 50)

                                if let photo = selected.photoUrl, !photo.isEmpty {
                                    let fullUrl = photo.starts(with: "http") ? photo : "\(ApiService.shared.baseUrl)\(photo)"
                                    AsyncImage(url: URL(string: fullUrl)) { phase in
                                        if let img = phase.image {
                                            img.resizable().scaledToFill().frame(width: 48, height: 48).clipShape(Circle())
                                        } else {
                                            Text(String(selected.name.prefix(1)).uppercased())
                                                .font(.system(size: 18, weight: .bold))
                                                .foregroundColor(.jciRoyalBlue)
                                        }
                                    }
                                } else {
                                    Text(String(selected.name.prefix(1)).uppercased())
                                        .font(.system(size: 18, weight: .bold))
                                        .foregroundColor(.jciRoyalBlue)
                                }
                            }

                            VStack(alignment: .leading, spacing: 2) {
                                Text(selected.name)
                                    .font(.system(size: 15, weight: .bold))
                                    .foregroundColor(.jciDeepNavy)

                                Text(selected.role ?? "Active Member")
                                    .font(.system(size: 12, weight: .semibold))
                                    .foregroundColor(.jciRoyalBlue)

                                if let biz = selected.businessName, !biz.isEmpty {
                                    Text(biz)
                                        .font(.system(size: 11))
                                        .foregroundColor(.jciTextMuted)
                                }
                            }

                            Spacer()

                            Button(action: { selectedPinMember = nil }) {
                                Image(systemName: "xmark.circle.fill")
                                    .font(.system(size: 20))
                                    .foregroundColor(Color(hex: "94A3B8"))
                            }
                        }

                        // Distance badge
                        if let dist = locationService.distanceFormatted(from: selected.lat, lng: selected.lng) {
                            HStack(spacing: 4) {
                                Image(systemName: "location.fill")
                                    .font(.system(size: 10))
                                Text(dist)
                                    .font(.system(size: 11, weight: .bold))
                            }
                            .foregroundColor(Color(hex: "0284C7"))
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }

                        // Bottom Actions: View Profile & Exchange Contact
                        HStack(spacing: 10) {
                            Button(action: {
                                onSelectMember?(selected)
                            }) {
                                Text("View Profile")
                                    .font(.system(size: 12, weight: .bold))
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 10)
                                    .background(Color(hex: "091322"))
                                    .foregroundColor(.white)
                                    .cornerRadius(10)
                            }

                            Button(action: {
                                onExchangeContact?(selected)
                            }) {
                                HStack(spacing: 4) {
                                    Image(systemName: "arrow.triangle.2.circlepath")
                                    Text("Exchange")
                                }
                                .font(.system(size: 12, weight: .bold))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 10)
                                .background(Color.jciRoyalBlue)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                            }

                            Button(action: {
                                let clean = selected.phone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()
                                if let url = URL(string: "https://wa.me/\(clean)") {
                                    UIApplication.shared.open(url)
                                }
                            }) {
                                Image(systemName: "message.fill")
                                    .font(.system(size: 14))
                                    .padding(10)
                                    .background(Color(hex: "22C55E"))
                                    .foregroundColor(.white)
                                    .clipShape(Circle())
                            }
                        }
                    }
                    .padding(16)
                    .background(Color.white)
                    .cornerRadius(20)
                    .shadow(color: Color.black.opacity(0.12), radius: 12, x: 0, y: 4)
                    .padding(.horizontal, 16)
                    .padding(.bottom, 20)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                }
            }
            .navigationTitle("Chapter Networking Map")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
            }
            .onAppear {
                locationService.requestPermissionAndStart()
                if let first = mappedMembers.first, let lat = first.lat, let lng = first.lng {
                    region.center = CLLocationCoordinate2D(latitude: lat, longitude: lng)
                }
            }
        }
    }
}

// MARK: - Custom Profile Pic Map Pin Annotation
struct MemberMapPinAnnotationView: View {
    let member: MemberDto
    let isSelected: Bool
    let onTap: () -> Void

    var isLive: Bool {
        member.isLocationSharingEnabled == true
    }

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 0) {
                ZStack {
                    // Pulsing radar glow if live location is active
                    if isLive {
                        Circle()
                            .fill(Color.green.opacity(0.3))
                            .frame(width: 52, height: 52)
                    }

                    // Avatar Circle
                    ZStack {
                        Circle()
                            .fill(Color.white)
                            .frame(width: 42, height: 42)

                        if let photo = member.photoUrl, !photo.isEmpty {
                            let fullUrl = photo.starts(with: "http") ? photo : "\(ApiService.shared.baseUrl)\(photo)"
                            AsyncImage(url: URL(string: fullUrl)) { phase in
                                switch phase {
                                case .success(let img):
                                    img.resizable().scaledToFill().frame(width: 38, height: 38).clipShape(Circle())
                                default:
                                    Text(String(member.name.prefix(1)).uppercased())
                                        .font(.system(size: 15, weight: .bold))
                                        .foregroundColor(.jciRoyalBlue)
                                }
                            }
                        } else {
                            Text(String(member.name.prefix(1)).uppercased())
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(.jciRoyalBlue)
                        }

                        // Border Ring
                        Circle()
                            .strokeBorder(
                                isSelected ? Color(hex: "091322") : Color.jciSkyBlue,
                                lineWidth: isSelected ? 3 : 2
                            )
                            .frame(width: 42, height: 42)

                        // Live green badge
                        if isLive {
                            Circle()
                                .fill(Color.green)
                                .frame(width: 10, height: 10)
                                .overlay(Circle().stroke(Color.white, lineWidth: 1.5))
                                .offset(x: 14, y: -14)
                        }
                    }
                    .shadow(color: Color.black.opacity(0.18), radius: 4, x: 0, y: 2)
                }

                // Triangle pointer
                Image(systemName: "triangle.fill")
                    .resizable()
                    .scaledToFit()
                    .foregroundColor(isSelected ? Color(hex: "091322") : Color.jciSkyBlue)
                    .frame(width: 10, height: 7)
                    .rotationEffect(.degrees(180))
                    .offset(y: -2)

                // Compact name label
                Text(member.name.components(separatedBy: " ").first ?? member.name)
                    .font(.system(size: 10, weight: .bold))
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color.white.opacity(0.92))
                    .foregroundColor(.jciDeepNavy)
                    .cornerRadius(6)
                    .shadow(color: Color.black.opacity(0.1), radius: 2, y: 1)
            }
        }
        .buttonStyle(.plain)
    }
}
