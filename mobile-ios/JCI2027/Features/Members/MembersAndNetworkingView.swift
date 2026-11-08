import SwiftUI
import MapKit

struct MembersAndNetworkingView: View {
    let chapterName: String
    let token: String?
    let onBack: () -> Void
    let onNavigateToScanCard: () -> Void

    @State private var members: [MemberDto] = []
    @State private var isLoading = true
    @State private var searchQuery = ""
    @State private var selectedFilter = 0 // 0: All, 1: Board/LGB, 2: Active, 3: Pending Approvals
    @State private var showMapSheet = false
    @State private var selectedMemberForPreview: MemberDto? = nil
    @State private var showAddMemberSheet = false

    let filters = ["All", "LGB Board", "Active", "Pending"]

    var filteredMembers: [MemberDto] {
        members.filter { m in
            let matchesSearch = searchQuery.isEmpty || m.name.localizedCaseInsensitiveContains(searchQuery) || (m.businessName ?? "").localizedCaseInsensitiveContains(searchQuery)
            let matchesFilter: Bool = {
                switch selectedFilter {
                case 1: return (m.role ?? "").localizedCaseInsensitiveContains("President") || (m.role ?? "").localizedCaseInsensitiveContains("VP") || (m.role ?? "").localizedCaseInsensitiveContains("Secretary") || (m.role ?? "").localizedCaseInsensitiveContains("Director")
                case 2: return m.status?.lowercased() == "active"
                case 3: return m.status?.lowercased() == "pending"
                default: return true
                }
            }()
            return matchesSearch && matchesFilter
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.jciTextDark)
                        .padding(8)
                        .background(Color.white)
                        .clipShape(Circle())
                }

                Text("Members & Network")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.jciTextDark)

                Spacer()

                // Map View Toggle
                Button(action: { showMapSheet.toggle() }) {
                    Image(systemName: "map.fill")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(.jciRoyalBlue)
                        .padding(8)
                        .background(Color.white)
                        .clipShape(Circle())
                }

                // Add Member Action
                Button(action: { showAddMemberSheet.toggle() }) {
                    Image(systemName: "plus")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                        .padding(8)
                        .background(Color.jciRoyalBlue)
                        .clipShape(Circle())
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)

            // Search Bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.jciTextMuted)
                TextField("Search member, designation, or company...", text: $searchQuery)
            }
            .padding(12)
            .background(Color.white)
            .cornerRadius(12)
            .padding(.horizontal, 20)
            .padding(.bottom, 10)

            // Filter Tabs
            Picker("Filter", selection: $selectedFilter) {
                ForEach(0..<filters.count, id: \.self) { idx in
                    Text(filters[idx]).tag(idx)
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 20)
            .padding(.bottom, 12)

            // Member List Content
            if isLoading {
                Spacer()
                ProgressView("Syncing Directory...")
                Spacer()
            } else if filteredMembers.isEmpty {
                Spacer()
                VStack(spacing: 8) {
                    Image(systemName: "person.crop.circle.badge.questionmark")
                        .font(.system(size: 40))
                        .foregroundColor(.jciTextMuted)
                    Text("No Members Found")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.jciTextDark)
                    Text("Try adjusting your search or filter.")
                        .font(.system(size: 12))
                        .foregroundColor(.jciTextMuted)
                }
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(filteredMembers) { member in
                            MemberRowCard(member: member) {
                                selectedMemberForPreview = member
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 20)
                }
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
        .onAppear {
            loadMembers()
        }
        .sheet(item: $selectedMemberForPreview) { member in
            MemberDetailSheet(member: member, onApprove: {
                approveMember(member.id)
            })
            .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showMapSheet) {
            MembersMapSheet(members: members)
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

struct MemberRowCard: View {
    let member: MemberDto
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                // Avatar
                ZStack {
                    Circle()
                        .fill(JciGradients.button)
                        .frame(width: 48, height: 48)
                    Text(String(member.name.prefix(1)))
                        .font(.system(size: 18, weight: .black))
                        .foregroundColor(.white)
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text(member.name)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.jciTextDark)

                    Text(member.role ?? "Active Member")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(.jciRoyalBlue)

                    if let biz = member.businessName, !biz.isEmpty {
                        Text(biz)
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(.jciTextMuted)
                    }
                }

                Spacer()

                if member.status?.lowercased() == "pending" {
                    Text("PENDING")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(.orange)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.orange.opacity(0.1))
                        .cornerRadius(6)
                } else {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.jciTextMuted.opacity(0.5))
                }
            }
            .padding(14)
            .background(Color.white)
            .cornerRadius(16)
            .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
        }
    }
}

struct MemberDetailSheet: View {
    let member: MemberDto
    let onApprove: () -> Void
    @Environment(\.dismiss) var dismiss

    var body: some View {
        VStack(spacing: 20) {
            Capsule()
                .fill(Color.jciBorderSubtle)
                .frame(width: 40, height: 5)
                .padding(.top, 10)

            VStack(spacing: 8) {
                ZStack {
                    Circle()
                        .fill(JciGradients.hero)
                        .frame(width: 70, height: 70)
                    Text(String(member.name.prefix(1)))
                        .font(.system(size: 28, weight: .black))
                        .foregroundColor(.white)
                }

                Text(member.name)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(.jciTextDark)

                Text(member.role ?? "Chapter Member")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(.jciRoyalBlue)
            }

            VStack(alignment: .leading, spacing: 12) {
                if let phone = Optional(member.phone), !phone.isEmpty {
                    Label(phone, systemImage: "phone.fill")
                }
                if let email = member.email, !email.isEmpty {
                    Label(email, systemImage: "envelope.fill")
                }
                if let biz = member.businessName, !biz.isEmpty {
                    Label(biz, systemImage: "briefcase.fill")
                }
                if let city = member.city, !city.isEmpty {
                    Label(city, systemImage: "mappin.and.ellipse")
                }
            }
            .font(.system(size: 13, weight: .medium))
            .foregroundColor(.jciTextDark)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
            .background(Color.jciBackgroundLight)
            .cornerRadius(14)

            if member.status?.lowercased() == "pending" {
                Button(action: {
                    onApprove()
                    dismiss()
                }) {
                    Text("Approve Membership")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(Color.jciShieldTeal)
                        .cornerRadius(12)
                }
            }

            Spacer()
        }
        .padding(20)
    }
}

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
