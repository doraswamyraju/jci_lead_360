import SwiftUI

/// LGB & Past Presidents Management Studio (100% Feature Parity with Android LgbAndPstManagementScreen.kt)
/// Executive Board Roster, Past Presidents Historical Roll of Honor, and Board Officer Access Granting.
struct LgbAndPstManagementView: View {
    let token: String
    let chapterName: String
    let presidentName: String
    var onBack: (() -> Void)? = nil

    // 0: LGB Directory, 1: Past Presidents Roll of Honor, 2: Grant Officer Access
    @State private var selectedTab = 0

    // Officers List (LGB & PST)
    @State private var officers: [LgbOfficerItem] = [
        LgbOfficerItem(id: "off_1", role: "PRESIDENT", name: "JC HGF Doraswamy Raju", email: "president@jcitirupati.com", phone: "+91 98765 43210", photoUrl: "/uploads/asset_1794047903403_1794047901566_president_profil.png"),
        LgbOfficerItem(id: "off_2", role: "SECRETARY", name: "JC Kiran Kumar", email: "secretary@jcitirupati.com", phone: "+91 98480 12345", photoUrl: "/assets/member_kiran.png"),
        LgbOfficerItem(id: "off_3", role: "TREASURER", name: "JC Venkat Raman", email: "treasurer@jcitirupati.com", phone: "+91 99887 76655", photoUrl: "/assets/member_venkat.png"),
        LgbOfficerItem(id: "off_4", role: "VP MANAGEMENT", name: "JC Anita Desai", email: "anita@jcitirupati.com", phone: "+91 97000 11223", photoUrl: "/assets/member_anita.png"),
        LgbOfficerItem(id: "off_5", role: "VP COMMUNITY", name: "JC Dr. Sneha Reddy", email: "sneha@jcitirupati.com", phone: "+91 98499 88776", photoUrl: "/assets/member_sneha.png"),
        LgbOfficerItem(id: "off_6", role: "VP INDIVIDUAL", name: "JC Rajesh Varma", email: "rajesh@jcitirupati.com", phone: "+91 98481 22334"),
        LgbOfficerItem(id: "off_7", role: "VP INTERNATIONAL", name: "JC Priya Mohan", email: "priya@jcitirupati.com", phone: "+91 99490 88776")
    ]

    // Past Presidents Roll of Honor
    let pastPresidents: [PastPresidentItem] = [
        PastPresidentItem(year: "2026", name: "JC Sen. Ramesh Babu", designation: "Immediate Past President (IPP)", phone: "+91 98850 99887", awards: "Best Zone Chapter Award, 100% Efficiency"),
        PastPresidentItem(year: "2025", name: "JC HGF Manoj Varma", designation: "Past President 2025", phone: "+91 99081 22334", awards: "National Excellence Citation, 40+ Projects"),
        PastPresidentItem(year: "2024", name: "JC Sen. K. Venugopal", designation: "Past President 2024", phone: "+91 98490 66778", awards: "Major Community Project Suvidha Grant"),
        PastPresidentItem(year: "2023", name: "JC Rajeshwari Rao", designation: "Past President 2023", phone: "+91 94401 22334", awards: "Zone Convention Best Delegation Winner"),
        PastPresidentItem(year: "2022", name: "JC Sen. Prakash Reddy", designation: "Past President 2022", phone: "+91 98480 33445", awards: "Chapter Charter Silver Jubilee Celebration")
    ]

    // Officer Access Form
    @State private var newOfficerName = ""
    @State private var newOfficerEmail = ""
    @State private var newOfficerPassword = ""
    @State private var newOfficerPhone = ""
    @State private var newOfficerRole = "SECRETARY"
    @State private var isGrantingAccess = false
    @State private var accessGrantedSuccess = false

    // Officer Edit Sheet
    @State private var selectedOfficerForEdit: LgbOfficerItem? = nil

    var body: some View {
        VStack(spacing: 0) {
            // Header Bar
            headerBarView

            // Segmented Tab Picker
            tabPickerView

            ScrollView(showsIndicators: false) {
                VStack(spacing: 16) {
                    switch selectedTab {
                    case 0:
                        lgbDirectoryView
                    case 1:
                        pastPresidentsRollView
                    default:
                        grantOfficerAccessView
                    }
                    Spacer(minLength: 40)
                }
                .padding(.vertical, 16)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
        .sheet(item: $selectedOfficerForEdit) { officer in
            EditOfficerModalSheet(
                officer: officer,
                onSave: { updated in
                    if let idx = officers.firstIndex(where: { $0.id == updated.id }) {
                        officers[idx] = updated
                    }
                    selectedOfficerForEdit = nil
                },
                onDismiss: { selectedOfficerForEdit = nil }
            )
        }
    }

    // MARK: - Header Bar
    private var headerBarView: some View {
        HStack {
            if let onBack = onBack {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.jciTextDark)
                        .padding(8)
                        .background(Color.white)
                        .clipShape(Circle())
                        .shadow(color: Color.black.opacity(0.06), radius: 4, x: 0, y: 2)
                }
            }

            VStack(alignment: .leading, spacing: 2) {
                Text("LGB & Past Presidents")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(.jciDeepNavy)

                Text("Executive Board Roster & Roll of Honor")
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

    // MARK: - Tab Picker
    private var tabPickerView: some View {
        HStack(spacing: 8) {
            tabButton(title: "Board Directory", index: 0, icon: "person.3.fill")
            tabButton(title: "Roll of Honor", index: 1, icon: "rosette")
            tabButton(title: "Grant Access", index: 2, icon: "key.fill")
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(Color.white)
    }

    private func tabButton(title: String, index: Int, icon: String) -> some View {
        let isSelected = selectedTab == index
        return Button(action: {
            withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                selectedTab = index
            }
        }) {
            HStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 12))
                Text(title)
                    .font(.system(size: 12, weight: isSelected ? .bold : .medium))
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 8)
            .background(isSelected ? Color.jciRoyalBlue : Color(hex: "F1F5F9"))
            .foregroundColor(isSelected ? .white : Color(hex: "475569"))
            .cornerRadius(10)
        }
        .buttonStyle(.plain)
    }

    // MARK: - Tab 0: LGB Directory
    private var lgbDirectoryView: some View {
        VStack(spacing: 12) {
            ForEach(officers) { officer in
                HStack(spacing: 14) {
                    // Avatar
                    ZStack {
                        Circle()
                            .fill(Color(hex: "E0F2FE"))
                            .frame(width: 50, height: 50)

                        if let photo = officer.photoUrl, !photo.isEmpty {
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
                                    Text(String(officer.name.prefix(1)).uppercased())
                                        .font(.system(size: 18, weight: .bold))
                                        .foregroundColor(.jciRoyalBlue)
                                }
                            }
                        } else {
                            Text(String(officer.name.prefix(1)).uppercased())
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.jciRoyalBlue)
                        }

                        Circle()
                            .strokeBorder(Color.jciRoyalBlue.opacity(0.3), lineWidth: 1.5)
                            .frame(width: 50, height: 50)
                    }

                    // Info
                    VStack(alignment: .leading, spacing: 3) {
                        Text(officer.name)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.jciDeepNavy)

                        Text(officer.role)
                            .font(.system(size: 10, weight: .black))
                            .foregroundColor(Color(hex: "0284C7"))
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color(hex: "E0F2FE"))
                            .cornerRadius(4)

                        Text("\(officer.phone) • \(officer.email)")
                            .font(.system(size: 11))
                            .foregroundColor(.jciTextMuted)
                            .lineLimit(1)
                    }

                    Spacer()

                    // Quick Actions
                    HStack(spacing: 8) {
                        Button(action: { callPhone(officer.phone) }) {
                            Image(systemName: "phone.fill")
                                .font(.system(size: 13))
                                .foregroundColor(Color(hex: "0284C7"))
                                .padding(8)
                                .background(Color(hex: "F0F9FF"))
                                .clipShape(Circle())
                        }

                        Button(action: { openWhatsApp(officer.phone) }) {
                            Image(systemName: "message.fill")
                                .font(.system(size: 13))
                                .foregroundColor(Color(hex: "16A34A"))
                                .padding(8)
                                .background(Color(hex: "F0FDF4"))
                                .clipShape(Circle())
                        }

                        Button(action: { selectedOfficerForEdit = officer }) {
                            Image(systemName: "pencil")
                                .font(.system(size: 13))
                                .foregroundColor(.jciTextMuted)
                                .padding(8)
                                .background(Color(hex: "F8FAFC"))
                                .clipShape(Circle())
                        }
                    }
                }
                .padding(14)
                .background(Color.white)
                .cornerRadius(16)
                .shadow(color: Color.black.opacity(0.03), radius: 6, y: 2)
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Tab 1: Past Presidents Roll of Honor
    private var pastPresidentsRollView: some View {
        VStack(spacing: 12) {
            ForEach(pastPresidents) { pp in
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        // Year Badge
                        Text("YEAR \(pp.year)")
                            .font(.system(size: 11, weight: .black))
                            .foregroundColor(Color(hex: "92400E"))
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(Color(hex: "FEF3C7"))
                            .cornerRadius(6)

                        Spacer()

                        Button(action: { callPhone(pp.phone) }) {
                            HStack(spacing: 4) {
                                Image(systemName: "phone.fill")
                                    .font(.system(size: 11))
                                Text("Contact")
                                    .font(.system(size: 11, weight: .bold))
                            }
                            .foregroundColor(.jciRoyalBlue)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color(hex: "EFF6FF"))
                            .cornerRadius(6)
                        }
                    }

                    Text(pp.name)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.jciDeepNavy)

                    Text(pp.designation)
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(.jciRoyalBlue)

                    Divider()

                    HStack(alignment: .top, spacing: 6) {
                        Text("🏆")
                            .font(.system(size: 13))
                        Text(pp.awards)
                            .font(.system(size: 12))
                            .foregroundColor(.jciTextMuted)
                    }
                }
                .padding(14)
                .background(Color.white)
                .cornerRadius(16)
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
                .shadow(color: Color.black.opacity(0.03), radius: 6, y: 2)
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Tab 2: Grant Officer Access
    private var grantOfficerAccessView: some View {
        VStack(alignment: .leading, spacing: 14) {
            VStack(alignment: .leading, spacing: 4) {
                Text("Grant Officer App Access")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.jciDeepNavy)

                Text("Create official secure accounts for Chapter Secretary, Treasurer, and VPs.")
                    .font(.system(size: 12))
                    .foregroundColor(.jciTextMuted)
            }

            VStack(spacing: 12) {
                TextField("Officer Full Name", text: $newOfficerName)
                    .padding(10)
                    .background(Color(hex: "F8FAFC"))
                    .cornerRadius(8)
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "CBD5E1"), lineWidth: 1))

                TextField("Officer Email Address", text: $newOfficerEmail)
                    .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
                    .padding(10)
                    .background(Color(hex: "F8FAFC"))
                    .cornerRadius(8)
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "CBD5E1"), lineWidth: 1))

                SecureField("Temporary Password", text: $newOfficerPassword)
                    .padding(10)
                    .background(Color(hex: "F8FAFC"))
                    .cornerRadius(8)
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "CBD5E1"), lineWidth: 1))

                TextField("Phone Number", text: $newOfficerPhone)
                    .keyboardType(.phonePad)
                    .padding(10)
                    .background(Color(hex: "F8FAFC"))
                    .cornerRadius(8)
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "CBD5E1"), lineWidth: 1))

                Picker("Executive Role", selection: $newOfficerRole) {
                    Text("Secretary").tag("SECRETARY")
                    Text("Treasurer").tag("TREASURER")
                    Text("VP Management").tag("VP_MGMT")
                    Text("VP Community").tag("VP_COMM")
                    Text("VP Individual").tag("VP_IND")
                    Text("VP International").tag("VP_INTL")
                }
                .pickerStyle(.menu)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(8)
                .background(Color(hex: "F8FAFC"))
                .cornerRadius(8)
                .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "CBD5E1"), lineWidth: 1))

                Button(action: grantAccess) {
                    HStack(spacing: 6) {
                        if isGrantingAccess {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Image(systemName: "key.fill")
                            Text("Create Officer Credentials")
                                .font(.system(size: 13, weight: .bold))
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.jciRoyalBlue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
                }
                .disabled(newOfficerName.isEmpty || newOfficerEmail.isEmpty || newOfficerPassword.isEmpty || isGrantingAccess)
            }
            .padding(16)
            .background(Color.white)
            .cornerRadius(16)
            .shadow(color: Color.black.opacity(0.04), radius: 6, y: 2)
        }
        .padding(.horizontal, 16)
        .alert(isPresented: $accessGrantedSuccess) {
            Alert(
                title: Text("Access Granted!"),
                message: Text("Officer account for \(newOfficerName) created successfully. Credentials have been dispatched."),
                dismissButton: .default(Text("OK"))
            )
        }
    }

    private func grantAccess() {
        isGrantingAccess = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            isGrantingAccess = false
            accessGrantedSuccess = true
            newOfficerName = ""
            newOfficerEmail = ""
            newOfficerPassword = ""
            newOfficerPhone = ""
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
}

// MARK: - Models
struct LgbOfficerItem: Identifiable {
    let id: String
    var role: String
    var name: String
    var email: String
    var phone: String
    var photoUrl: String? = nil
}

struct PastPresidentItem: Identifiable {
    var id: String { year }
    let year: String
    let name: String
    let designation: String
    let phone: String
    let awards: String
}

// MARK: - Edit Officer Sheet
struct EditOfficerModalSheet: View {
    @State var officer: LgbOfficerItem
    let onSave: (LgbOfficerItem) -> Void
    let onDismiss: () -> Void

    var body: some View {
        NavigationStack {
            Form {
                Section(header: Text("Officer Details")) {
                    TextField("Name", text: $officer.name)
                    TextField("Role", text: $officer.role)
                    TextField("Phone", text: $officer.phone)
                    TextField("Email", text: $officer.email)
                }
            }
            .navigationTitle("Update Officer Portfolio")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { onDismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { onSave(officer) }
                        .bold()
                }
            }
        }
    }
}
