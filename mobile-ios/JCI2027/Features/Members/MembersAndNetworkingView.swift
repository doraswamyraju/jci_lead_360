import SwiftUI
import MapKit

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
    @State private var showAddMemberSheet = false

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
                            MemberRowCard(member: member) {
                                selectedMemberForPreview = member
                            }
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
        .sheet(item: $selectedMemberForPreview) { member in
            MemberDetailSheet(
                member: member,
                chapterName: chapterName,
                onApprove: { approveMember(member.id) },
                onCelebrationFlyer: {
                    selectedMemberForPreview = nil
                    onOpenCelebrationFlyer?(member)
                }
            )
            .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showMapSheet) {
            MembersMapSheet(members: members)
        }
        .sheet(isPresented: $showAddMemberSheet) {
            AddMemberModalSheet(
                chapterName: chapterName,
                token: token,
                onSuccess: {
                    showAddMemberSheet = false
                    loadMembers()
                },
                onDismiss: { showAddMemberSheet = false }
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
                Image(systemName: "map.fill")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.jciRoyalBlue)
                    .padding(8)
                    .background(Color.white)
                    .clipShape(Circle())
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
            ZStack {
                Circle()
                    .fill(Color(hex: "E0F2FE"))
                    .frame(width: 72, height: 72)
                Text("👥")
                    .font(.system(size: 32))
            }
            Text("No Members Found")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(.jciDeepNavy)
            Text("Try clearing your search or switching filter tabs.")
                .font(.system(size: 12))
                .foregroundColor(.jciTextMuted)
            Spacer()
        }
    }

    private func loadMembers() {
        guard let token = token else {
            isLoading = false
            return
        }
        isLoading = true
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
            do {
                _ = try await ApiService.shared.approveMember(token: token, memberId: id)
                loadMembers()
            } catch {}
        }
    }
}

// MARK: - Member Row Card
struct MemberRowCard: View {
    let member: MemberDto
    let onTap: () -> Void

    var isPending: Bool {
        member.status?.lowercased() == "pending"
    }

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                // Avatar
                ZStack {
                    Circle()
                        .fill(Color(hex: "E2E8F0"))
                        .frame(width: 50, height: 50)

                    if let photo = member.photoUrl, !photo.isEmpty {
                        let fullUrl = photo.starts(with: "http") ? photo : "\(ApiService.shared.baseUrl)\(photo)"
                        AsyncImage(url: URL(string: fullUrl)) { phase in
                            switch phase {
                            case .success(let img):
                                img
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 48, height: 48)
                                    .clipShape(Circle())
                            default:
                                Text(String(member.name.prefix(1)).uppercased())
                                    .font(.system(size: 18, weight: .black))
                                    .foregroundColor(.jciRoyalBlue)
                            }
                        }
                    } else {
                        Text(String(member.name.prefix(1)).uppercased())
                            .font(.system(size: 18, weight: .black))
                            .foregroundColor(.jciRoyalBlue)
                    }

                    Circle()
                        .strokeBorder(isPending ? Color.orange : Color(hex: "BAE6FD"), lineWidth: 2)
                        .frame(width: 50, height: 50)
                }

                VStack(alignment: .leading, spacing: 3) {
                    HStack(spacing: 6) {
                        Text(member.name)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.jciDeepNavy)
                            .lineLimit(1)

                        if let bg = member.bloodGroup, !bg.isEmpty {
                            Text(bg)
                                .font(.system(size: 9, weight: .black))
                                .foregroundColor(Color(hex: "DC2626"))
                                .padding(.horizontal, 4)
                                .padding(.vertical, 1)
                                .background(Color(hex: "FEE2E2"))
                                .cornerRadius(4)
                        }
                    }

                    Text(member.role ?? "Active Member")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(.jciRoyalBlue)
                        .lineLimit(1)

                    if let biz = member.businessName, !biz.isEmpty {
                        Text("\(biz) • \(member.designation ?? member.businessCategory ?? "")")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(.jciTextMuted)
                            .lineLimit(1)
                    }
                }

                Spacer()

                if isPending {
                    Text("PENDING")
                        .font(.system(size: 9, weight: .black))
                        .foregroundColor(.orange)
                        .padding(.horizontal, 7)
                        .padding(.vertical, 3)
                        .background(Color.orange.opacity(0.12))
                        .cornerRadius(6)
                } else {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(Color(hex: "94A3B8"))
                }
            }
            .padding(12)
            .background(Color.white)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(isPending ? Color.orange.opacity(0.4) : Color(hex: "E2E8F0"), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.03), radius: 6, y: 2)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Member Detail Bottom Sheet
struct MemberDetailSheet: View {
    let member: MemberDto
    let chapterName: String
    let onApprove: () -> Void
    var onCelebrationFlyer: (() -> Void)? = nil
    @Environment(\.dismiss) var dismiss

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

                    // Quick Action Contact Grid
                    HStack(spacing: 10) {
                        Button(action: { callPhone(member.phone) }) {
                            VStack(spacing: 4) {
                                Image(systemName: "phone.fill")
                                    .font(.system(size: 16))
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
                                    .font(.system(size: 16))
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
                                        .font(.system(size: 16))
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
                    .padding(.horizontal, 16)

                    // Business & Milestone Parameters
                    VStack(alignment: .leading, spacing: 12) {
                        Text("MEMBER PROFILE & OCCUPATION")
                            .font(.system(size: 11, weight: .black))
                            .foregroundColor(.jciDeepNavy)

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
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
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
            spouseName: spouseName
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

// MARK: - Networking Map Sheet
struct MembersMapSheet: View {
    let members: [MemberDto]
    @Environment(\.dismiss) var dismiss

    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 13.6288, longitude: 79.4192),
        span: MKCoordinateSpan(latitudeDelta: 0.1, longitudeDelta: 0.1)
    )

    var body: some View {
        NavigationStack {
            Map(coordinateRegion: $region, annotationItems: members) { member in
                MapPin(
                    coordinate: CLLocationCoordinate2D(latitude: member.lat ?? 13.6288, longitude: member.lng ?? 79.4192),
                    tint: .blue
                )
            }
            .navigationTitle("Chapter Networking Map")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
            }
        }
    }
}
