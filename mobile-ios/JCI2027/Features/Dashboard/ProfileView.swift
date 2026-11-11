import SwiftUI
import PhotosUI

/// Replicates the official Android ProfileScreen and JciDigitalBusinessCard from AppScreens.kt (lines 212-868)
/// Features:
/// - 3D Interactive Flip Digital Business Card & Membership ID with Reciprocal QR Code
/// - Local Organization (LO) Chapter Profile settings (Official logo, chapter name, zone, email, phone)
/// - President Personal Profile details (Portrait photo upload, name, phone, email)
/// - Local persistence (UserDefaults) and live backend synchronization (`/api/users/officer-details` & `/api/users/chapter-details`)
/// - Session Management (Sign out)
struct ProfileView: View {
    let name: String
    let chapterName: String
    let role: String
    let token: String?
    var userId: String = UserDefaults.standard.string(forKey: "auth_user_id") ?? "usr_pres_01"
    var onLogout: () -> Void
    var onBack: (() -> Void)? = nil
    var onProfileUpdated: ((String, String, String?, UIImage?) -> Void)? = nil

    // State persisted in UserDefaults
    @State private var presidentNameInput: String
    @State private var presidentPhone: String
    @State private var presidentEmail: String
    @State private var chapterNameInput: String
    @State private var chapterZone: String
    @State private var chapterEmail: String
    @State private var chapterPhone: String

    // PhotosPicker items
    @State private var selectedPortraitItem: PhotosPickerItem? = nil
    @State private var portraitImage: UIImage? = nil
    @State private var serverPhotoUrl: String = UserDefaults.standard.string(forKey: "auth_photo_url") ?? ""
    @State private var selectedLogoItem: PhotosPickerItem? = nil
    @State private var chapterLogoImage: UIImage? = nil

    @State private var isSavedToastVisible = false
    @State private var isSaving = false

    init(
        name: String,
        chapterName: String,
        role: String,
        token: String? = nil,
        onLogout: @escaping () -> Void,
        onBack: (() -> Void)? = nil,
        onProfileUpdated: ((String, String, String?, UIImage?) -> Void)? = nil
    ) {
        self.name = name
        self.chapterName = chapterName
        self.role = role
        self.token = token
        self.onLogout = onLogout
        self.onBack = onBack
        self.onProfileUpdated = onProfileUpdated

        _presidentNameInput = State(initialValue: UserDefaults.standard.string(forKey: "auth_name") ?? name)
        _presidentPhone = State(initialValue: UserDefaults.standard.string(forKey: "auth_phone") ?? "+91 98765 43210")
        _presidentEmail = State(initialValue: UserDefaults.standard.string(forKey: "saved_email") ?? "president@jcitirupati.com")
        _chapterNameInput = State(initialValue: UserDefaults.standard.string(forKey: "auth_chapter") ?? chapterName)
        _chapterZone = State(initialValue: UserDefaults.standard.string(forKey: "chapter_zone") ?? "Zone IV")
        _chapterEmail = State(initialValue: UserDefaults.standard.string(forKey: "chapter_email") ?? "info@jcitirupati.com")
        _chapterPhone = State(initialValue: UserDefaults.standard.string(forKey: "chapter_phone") ?? "+91 98765 43210")
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Header Bar
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
                        Text("President Profile & Settings")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.jciTextDark)
                        Text("Official JCI Digital ID & Chapter Parameters")
                            .font(.system(size: 11))
                            .foregroundColor(.jciTextMuted)
                    }

                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)

                // 1. OFFICIAL JCI DIGITAL BUSINESS CARD WITH 3D FLIP ANIMATION
                let userId = UserDefaults.standard.string(forKey: "auth_user_id") ?? "usr_pres_01"
                JciDigitalBusinessCardView(
                    presidentName: presidentNameInput,
                    role: role,
                    chapterName: chapterNameInput,
                    zone: chapterZone,
                    phone: presidentPhone,
                    email: presidentEmail,
                    userId: userId,
                    portraitImage: portraitImage,
                    serverPhotoUrl: serverPhotoUrl,
                    chapterLogoImage: chapterLogoImage
                )
                .padding(.horizontal, 16)

                // 2. Local Organization (LO) Chapter Profile Settings
                VStack(alignment: .leading, spacing: 14) {
                    HStack(spacing: 8) {
                        Text("🏛️")
                            .font(.system(size: 18))
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Local Organization (LO) Chapter Profile")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(.jciTextDark)
                            Text("Configure chapter logo & parameters (shown in TopBar & flyers)")
                                .font(.system(size: 11))
                                .foregroundColor(.jciTextMuted)
                        }
                    }

                    // Chapter Logo Picker Bar
                    Text("OFFICIAL CHAPTER LOGO")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(Color(hex: "475569"))

                    PhotosPicker(selection: $selectedLogoItem, matching: .images) {
                        HStack(spacing: 12) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 8)
                                    .fill(Color.white)
                                    .shadow(color: Color.black.opacity(0.06), radius: 2, x: 0, y: 1)

                                if let logo = chapterLogoImage {
                                    Image(uiImage: logo)
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 44, height: 44)
                                        .cornerRadius(6)
                                } else {
                                    Text("🏛️")
                                        .font(.system(size: 24))
                                }
                            }
                            .frame(width: 52, height: 52)

                            VStack(alignment: .leading, spacing: 2) {
                                Text(chapterLogoImage != nil ? "Chapter Logo Attached" : "Upload Chapter Logo")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.jciTextDark)
                                Text("Appears on digital cards, TopBar, and flyers")
                                    .font(.system(size: 10))
                                    .foregroundColor(.jciTextMuted)
                            }

                            Spacer()

                            Text(chapterLogoImage != nil ? "Change" : "Browse")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(Color.jciSkyBlue)
                                .cornerRadius(8)
                        }
                        .padding(10)
                        .background(Color(hex: "F8FAFC"))
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
                        )
                    }
                    .buttonStyle(.plain)

                    VStack(spacing: 10) {
                        ProfileInputField(label: "Chapter Organization Name", text: $chapterNameInput)
                        ProfileInputField(label: "JCI Zone / Region", text: $chapterZone)
                        ProfileInputField(label: "Chapter Official Email", text: $chapterEmail, keyboardType: .emailAddress)
                        ProfileInputField(label: "Chapter Official Phone", text: $chapterPhone, keyboardType: .phonePad)
                    }
                }
                .padding(16)
                .background(Color.white)
                .cornerRadius(16)
                .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
                .padding(.horizontal, 16)

                // 3. President Personal Profile Details
                VStack(alignment: .leading, spacing: 14) {
                    HStack(spacing: 8) {
                        Text("👤")
                            .font(.system(size: 18))
                        VStack(alignment: .leading, spacing: 2) {
                            Text("President Personal Profile")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(.jciTextDark)
                            Text("Appears automatically on dynamic flyers and invitations")
                                .font(.system(size: 11))
                                .foregroundColor(.jciTextMuted)
                        }
                    }

                    // Portrait Photo Picker
                    PhotosPicker(selection: $selectedPortraitItem, matching: .images) {
                        HStack(spacing: 12) {
                            portraitPickerAvatarView

                            VStack(alignment: .leading, spacing: 2) {
                                Text((portraitImage != nil || !serverPhotoUrl.isEmpty) ? "Portrait Photo Attached" : "Upload Portrait Photo")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.jciTextDark)
                                Text("High-res portrait for executive card & invitations")
                                    .font(.system(size: 10))
                                    .foregroundColor(.jciTextMuted)
                            }

                            Spacer()

                            Text((portraitImage != nil || !serverPhotoUrl.isEmpty) ? "Change" : "Browse")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(Color.jciSkyBlue)
                                .cornerRadius(8)
                        }
                        .padding(10)
                        .background(Color(hex: "F8FAFC"))
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
                        )
                    }
                    .buttonStyle(.plain)

                    VStack(spacing: 10) {
                        ProfileInputField(label: "President Full Name", text: $presidentNameInput)
                        ProfileInputField(label: "President Mobile Number", text: $presidentPhone, keyboardType: .phonePad)
                        ProfileInputField(label: "President Email Address", text: $presidentEmail, keyboardType: .emailAddress)
                    }

                    // Save Button
                    Button(action: saveAllChanges) {
                        HStack(spacing: 6) {
                            if isSaving {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Image(systemName: isSavedToastVisible ? "checkmark.circle.fill" : "square.and.arrow.down.fill")
                            }
                            Text(isSavedToastVisible ? "✓ Profile & Chapter Details Saved" : "Save All Changes")
                                .font(.system(size: 14, weight: .bold))
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 46)
                        .background(isSavedToastVisible ? Color(hex: "10B981") : Color.jciSkyBlue)
                        .cornerRadius(10)
                    }
                    .buttonStyle(.plain)
                    .disabled(isSaving)
                }
                .padding(16)
                .background(Color.white)
                .cornerRadius(16)
                .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
                .padding(.horizontal, 16)

                // 4. Session Management (Sign Out)
                VStack(alignment: .leading, spacing: 10) {
                    Text("Session Management")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(.jciTextDark)
                    Text("Clears active session credentials on this device")
                        .font(.system(size: 11))
                        .foregroundColor(.jciTextMuted)

                    Button(action: onLogout) {
                        HStack(spacing: 6) {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                            Text("Sign Out from Device")
                                .font(.system(size: 13, weight: .bold))
                        }
                        .foregroundColor(Color(hex: "DC2626"))
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                        .background(Color(hex: "FEE2E2"))
                        .cornerRadius(10)
                    }
                    .buttonStyle(.plain)
                }
                .padding(16)
                .background(Color.white)
                .cornerRadius(16)
                .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
                .padding(.horizontal, 16)
                .padding(.bottom, 24)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
        .onAppear {
            loadPersistedImages()
            if let img = portraitImage {
                onProfileUpdated?(presidentNameInput, chapterNameInput, serverPhotoUrl, img)
            }
            Task {
                if let leader = try? await ApiService.shared.getLeaderProfile(userId: userId),
                   let photo = leader.photoUrl, !photo.isEmpty {
                    await MainActor.run {
                        self.serverPhotoUrl = photo
                        UserDefaults.standard.set(photo, forKey: "auth_photo_url")
                        self.onProfileUpdated?(self.presidentNameInput, self.chapterNameInput, photo, self.portraitImage)
                    }
                }
            }
        }
        .onChange(of: selectedPortraitItem) { newItem in
            Task {
                if let data = try? await newItem?.loadTransferable(type: Data.self),
                   let uiImage = UIImage(data: data) {
                    await MainActor.run {
                        self.portraitImage = uiImage
                        saveImageToDisk(data: data, filename: "president_portrait.jpg")
                        self.onProfileUpdated?(self.presidentNameInput, self.chapterNameInput, self.serverPhotoUrl, uiImage)
                    }
                    // Upload to server asynchronously and persist URL
                    if let token = token {
                        if let uploadedUrl = try? await ApiService.shared.uploadImage(token: token, imageData: data, filename: "president_\(Date().timeIntervalSince1970).jpg") {
                            await MainActor.run {
                                self.serverPhotoUrl = uploadedUrl
                                UserDefaults.standard.set(uploadedUrl, forKey: "auth_photo_url")
                                self.onProfileUpdated?(self.presidentNameInput, self.chapterNameInput, uploadedUrl, uiImage)
                            }
                            _ = try? await ApiService.shared.updateOfficerDetails(
                                token: token,
                                name: presidentNameInput,
                                phone: presidentPhone,
                                email: presidentEmail,
                                photoUrl: uploadedUrl,
                                role: role
                            )
                        }
                    }
                }
            }
        }
        .onChange(of: selectedLogoItem) { newItem in
            Task {
                if let data = try? await newItem?.loadTransferable(type: Data.self),
                   let uiImage = UIImage(data: data) {
                    await MainActor.run {
                        self.chapterLogoImage = uiImage
                        saveImageToDisk(data: data, filename: "chapter_logo.png")
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var portraitPickerAvatarView: some View {
        ZStack {
            Circle()
                .fill(Color(hex: "F1F5F9"))
                .frame(width: 52, height: 52)
                .overlay(Circle().stroke(Color.jciSkyBlue, lineWidth: 1.5))

            if let portraitImage = portraitImage {
                Image(uiImage: portraitImage)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 50, height: 50)
                    .clipShape(Circle())
            } else if !serverPhotoUrl.isEmpty {
                let fullUrl = serverPhotoUrl.starts(with: "http") ? serverPhotoUrl : "\(ApiService.shared.baseUrl)\(serverPhotoUrl)"
                AsyncImage(url: URL(string: fullUrl)) { phase in
                    switch phase {
                    case .success(let img):
                        img
                            .resizable()
                            .scaledToFill()
                            .frame(width: 50, height: 50)
                            .clipShape(Circle())
                    default:
                        Text(String(presidentNameInput.prefix(1)).uppercased())
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(Color.jciSkyBlue)
                    }
                }
            } else {
                Text(String(presidentNameInput.prefix(1)).uppercased())
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(Color.jciSkyBlue)
            }
        }
    }

    private func saveAllChanges() {
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.success)

        isSaving = true

        UserDefaults.standard.set(presidentNameInput.trimmingCharacters(in: .whitespaces), forKey: "auth_name")
        UserDefaults.standard.set(presidentPhone.trimmingCharacters(in: .whitespaces), forKey: "auth_phone")
        UserDefaults.standard.set(presidentEmail.trimmingCharacters(in: .whitespaces), forKey: "saved_email")
        UserDefaults.standard.set(chapterNameInput.trimmingCharacters(in: .whitespaces), forKey: "auth_chapter")
        UserDefaults.standard.set(chapterZone.trimmingCharacters(in: .whitespaces), forKey: "chapter_zone")
        UserDefaults.standard.set(chapterEmail.trimmingCharacters(in: .whitespaces), forKey: "chapter_email")
        UserDefaults.standard.set(chapterPhone.trimmingCharacters(in: .whitespaces), forKey: "chapter_phone")

        onProfileUpdated?(presidentNameInput, chapterNameInput, serverPhotoUrl, portraitImage)

        Task {
            if let token = token {
                _ = try? await ApiService.shared.updateOfficerDetails(
                    token: token,
                    name: presidentNameInput,
                    phone: presidentPhone,
                    email: presidentEmail,
                    photoUrl: serverPhotoUrl.isEmpty ? nil : serverPhotoUrl,
                    role: role
                )
                _ = try? await ApiService.shared.updateChapterDetails(
                    token: token,
                    chapterName: chapterNameInput,
                    zone: chapterZone,
                    phone: chapterPhone,
                    email: chapterEmail
                )
            }

            await MainActor.run {
                self.isSaving = false
                withAnimation { self.isSavedToastVisible = true }
                DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                    withAnimation { self.isSavedToastVisible = false }
                }
            }
        }
    }

    private func saveImageToDisk(data: Data, filename: String) {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        if let docDir = paths.first {
            let fileURL = docDir.appendingPathComponent(filename)
            try? data.write(to: fileURL)
        }
    }

    private func loadPersistedImages() {
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        if let docDir = paths.first {
            let portraitURL = docDir.appendingPathComponent("president_portrait.jpg")
            if let data = try? Data(contentsOf: portraitURL), let img = UIImage(data: data) {
                self.portraitImage = img
            }
            let logoURL = docDir.appendingPathComponent("chapter_logo.png")
            if let data = try? Data(contentsOf: logoURL), let img = UIImage(data: data) {
                self.chapterLogoImage = img
            }
        }
    }
}

// MARK: - Reusable Profile Input Field
struct ProfileInputField: View {
    let label: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(Color(hex: "64748B"))
            TextField(label, text: $text)
                .font(.system(size: 13))
                .padding(.horizontal, 12)
                .padding(.vertical, 10)
                .background(Color(hex: "F8FAFC"))
                .cornerRadius(8)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color(hex: "CBD5E1"), lineWidth: 1)
                )
                .keyboardType(keyboardType)
        }
    }
}

// MARK: - 3D Interactive Flip Digital Business Card View (Parity with JciDigitalBusinessCard from AppScreens.kt)
struct JciDigitalBusinessCardView: View {
    let presidentName: String
    let role: String
    let chapterName: String
    let zone: String
    let phone: String
    let email: String
    let userId: String
    let portraitImage: UIImage?
    var serverPhotoUrl: String? = nil
    let chapterLogoImage: UIImage?

    @State private var isCardFlipped = false

    var exchangeUrl: String {
        QrCodeGenerator.buildExchangeUrl(leaderId: userId)
    }

    var body: some View {
        ZStack {
            // Front Card View
            cardContent(isFront: !isCardFlipped)
                .opacity(isCardFlipped ? 0 : 1)
                .rotation3DEffect(.degrees(isCardFlipped ? 180 : 0), axis: (x: 0, y: 1, z: 0))

            // Back Card View
            cardContent(isFront: false)
                .opacity(isCardFlipped ? 1 : 0)
                .rotation3DEffect(.degrees(isCardFlipped ? 0 : -180), axis: (x: 0, y: 1, z: 0))
        }
        .onTapGesture {
            let impact = UIImpactFeedbackGenerator(style: .light)
            impact.impactOccurred()
            withAnimation(.spring(response: 0.6, dampingFraction: 0.8)) {
                isCardFlipped.toggle()
            }
        }
    }

    @ViewBuilder
    private func cardContent(isFront: Bool) -> some View {
        VStack(spacing: 0) {
            // JCI Official Corporate Identity Color Strip (JCI Blue #0097D7, JCI Navy #17142C, Seafoam #59BDBD)
            LinearGradient(
                colors: [Color(hex: "0097D7"), Color(hex: "17142C"), Color(hex: "59BDBD")],
                startPoint: .leading,
                endPoint: .trailing
            )
            .frame(height: 6)

            VStack(spacing: 14) {
                // Header Bar: Brand Lockup + Flipped status pill
                HStack(alignment: .center) {
                    HStack(spacing: 8) {
                        if let logo = chapterLogoImage {
                            Image(uiImage: logo)
                                .resizable()
                                .scaledToFit()
                                .frame(width: 32, height: 32)
                                .cornerRadius(6)
                        } else {
                            Text("JCI")
                                .font(.system(size: 13, weight: .black))
                                .foregroundColor(.white)
                                .padding(.horizontal, 7)
                                .padding(.vertical, 3)
                                .background(Color(hex: "0097D7"))
                                .cornerRadius(6)
                        }

                        VStack(alignment: .leading, spacing: 1) {
                            Text("Junior Chamber International")
                                .font(.system(size: 9, weight: .bold))
                                .foregroundColor(Color(hex: "64748B"))
                                .tracking(0.5)
                            Text(chapterName)
                                .font(.system(size: 13, weight: .heavy))
                                .foregroundColor(Color(hex: "0F172A"))
                                .lineLimit(1)
                        }
                    }

                    Spacer()

                    Text(isFront ? "🔄 TAP TO FLIP" : "🔄 FRONT")
                        .font(.system(size: 9, weight: .heavy))
                        .foregroundColor(Color(hex: "0284C7"))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color(hex: "F1F5F9"))
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color(hex: "CBD5E1"), lineWidth: 1)
                        )
                }

                if isFront {
                    // FRONT SIDE: EXECUTIVE BUSINESS CARD
                    HStack(spacing: 14) {
                        // Portrait
                        ZStack {
                            Circle()
                                .fill(Color(hex: "F1F5F9"))
                                .frame(width: 68, height: 68)
                                .overlay(Circle().stroke(Color(hex: "0097D7"), lineWidth: 2))

                            if let portraitImage = portraitImage {
                                Image(uiImage: portraitImage)
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 66, height: 66)
                                    .clipShape(Circle())
                            } else if let url = serverPhotoUrl, !url.isEmpty {
                                let fullUrl = url.starts(with: "http") ? url : "\(ApiService.shared.baseUrl)\(url)"
                                AsyncImage(url: URL(string: fullUrl)) { phase in
                                    switch phase {
                                    case .success(let img):
                                        img
                                            .resizable()
                                            .scaledToFill()
                                            .frame(width: 66, height: 66)
                                            .clipShape(Circle())
                                    default:
                                        Text(String(presidentName.prefix(2)).uppercased())
                                            .font(.system(size: 22, weight: .bold))
                                            .foregroundColor(Color(hex: "0097D7"))
                                    }
                                }
                            } else {
                                Text(String(presidentName.prefix(2)).uppercased())
                                    .font(.system(size: 22, weight: .bold))
                                    .foregroundColor(Color(hex: "0097D7"))
                            }
                        }

                        VStack(alignment: .leading, spacing: 3) {
                            Text(presidentName)
                                .font(.system(size: 16, weight: .heavy))
                                .foregroundColor(Color(hex: "0F172A"))
                            Text(role)
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Color(hex: "0097D7"))

                            Text("MEMBER ID: JCI-2027-\(String(presidentName.prefix(3)).uppercased())")
                                .font(.system(size: 9, weight: .bold))
                                .foregroundColor(Color(hex: "15803D"))
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color(hex: "DCFCE7"))
                                .cornerRadius(4)
                        }

                        Spacer()
                    }

                    // Contact Details
                    VStack(alignment: .leading, spacing: 5) {
                        HStack(spacing: 6) {
                            Text("📞").font(.system(size: 11))
                            Text(phone).font(.system(size: 11.5, weight: .medium)).foregroundColor(Color(hex: "334155"))
                        }
                        HStack(spacing: 6) {
                            Text("✉️").font(.system(size: 11))
                            Text(email).font(.system(size: 11.5, weight: .medium)).foregroundColor(Color(hex: "334155"))
                        }
                        HStack(spacing: 6) {
                            Text("📍").font(.system(size: 11))
                            Text("\(zone) • India").font(.system(size: 11)).foregroundColor(Color(hex: "64748B"))
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(10)
                    .background(Color(hex: "F8FAFC"))
                    .cornerRadius(10)
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
                    )
                } else {
                    // BACK SIDE: RECIPROCAL CONTACT EXCHANGE QR CODE
                    VStack(spacing: 8) {
                        Text("RECIPROCAL CONTACT EXCHANGE QR")
                            .font(.system(size: 11, weight: .black))
                            .foregroundColor(Color(hex: "0097D7"))
                            .tracking(0.5)

                        Text("Scan with any camera to exchange contact details")
                            .font(.system(size: 11))
                            .foregroundColor(Color(hex: "64748B"))

                        if let qrImg = QrCodeGenerator.generateQrImage(from: exchangeUrl, size: CGSize(width: 320, height: 320)) {
                            Image(uiImage: qrImg)
                                .resizable()
                                .interpolation(.none)
                                .scaledToFit()
                                .frame(width: 140, height: 140)
                                .padding(8)
                                .background(Color.white)
                                .cornerRadius(12)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
                                )
                        }

                        Text("Auto-saves their contact into your Rolodex & saves yours to their phone")
                            .font(.system(size: 9.5))
                            .foregroundColor(Color(hex: "64748B"))
                            .multilineTextAlignment(.center)
                    }
                    .padding(.vertical, 4)
                }
            }
            .padding(16)
        }
        .background(Color.white)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.08), radius: 8, x: 0, y: 3)
    }
}
