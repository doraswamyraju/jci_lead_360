import SwiftUI
import Contacts
import AVFoundation
import UIKit
import PhotosUI

/// Replicates & elevates the official Android ScanCardModuleScreen.kt ("Exchange & Cards" / AI CardSnap Rolodex)
/// Features:
/// - 4-Step Executive Scanner Flow:
///   1. DIRECTORY: Rolodex directory with prominent AI CardSnap Scanner Hero banner & filters
///   2. SCAN_FRONT: Real-time Live Camera Viewfinder with card alignment frame, laser guide & torch
///   3. SCAN_BACK: Optional reverse side scan for QR / address / map details
///   4. EXTRACT_REVIEW: Visual OCR comparison, deduplication check, editable fields & phonebook sync
///   5. SAVED_SUCCESS: Celebration card with instant WhatsApp greeting & scan another action
struct ScanCardModuleView: View {
    let chapterName: String
    let token: String?
    let initialTargetContactId: String?
    var startWithScanner: Bool = false
    let onBack: () -> Void

    enum ScannerStep {
        case directory
        case scanFront
        case scanBack
        case extractReview
        case savedSuccess
    }

    @State private var currentStep: ScannerStep = .directory
    @State private var contacts: [SaveContactRequest] = []
    @State private var isLoading = true
    @State private var isRefreshing = false
    @State private var searchQuery = ""
    @State private var selectedEventFilter = "All Events"
    @State private var selectedContact: SaveContactRequest? = nil
    @State private var contactToEdit: SaveContactRequest? = nil
    @State private var showCreateDialog = false
    @State private var toastMessage: String? = nil

    // Scanned Card Session State
    @State private var frontCardImage: UIImage? = nil
    @State private var backCardImage: UIImage? = nil
    @State private var frontOcrText: String = ""
    @State private var backOcrText: String = ""
    @State private var currentCardModel = ScannedCardModel()

    // Form fields (Manual Create / Edit)
    @State private var formName = ""
    @State private var formPhone = ""
    @State private var formEmail = ""
    @State private var formCompany = ""
    @State private var formDesignation = ""
    @State private var formCity = ""
    @State private var formEventTag = "Reciprocal QR Exchange"
    @State private var formNotes = ""

    // Dynamic unique event tags for filter pills
    var availableEvents: [String] {
        var tags = ["All Events"]
        let uniqueTags = Array(Set(contacts.map { $0.eventTag.isEmpty ? "Reciprocal QR Exchange" : $0.eventTag })).sorted()
        tags.append(contentsOf: uniqueTags)
        return tags
    }

    var filteredContacts: [SaveContactRequest] {
        contacts.filter { c in
            let matchesEvent = (selectedEventFilter == "All Events") || (c.eventTag == selectedEventFilter)
            let q = searchQuery.trimmingCharacters(in: .whitespaces)
            let matchesQuery = q.isEmpty ||
                c.name.localizedCaseInsensitiveContains(q) ||
                c.company.localizedCaseInsensitiveContains(q) ||
                c.designation.localizedCaseInsensitiveContains(q) ||
                c.city.localizedCaseInsensitiveContains(q) ||
                c.eventTag.localizedCaseInsensitiveContains(q) ||
                c.phone.localizedCaseInsensitiveContains(q)
            return matchesEvent && matchesQuery
        }
    }

    var body: some View {
        ZStack {
            switch currentStep {
            case .directory:
                directoryMainView
            case .scanFront:
                LiveCardScannerView(
                    stepTitle: "Front Side (1 of 2)",
                    guideHint: "Align front side of business card within frame.",
                    activeStepNumber: 1,
                    onImageCaptured: { image, rawText in
                        self.frontCardImage = image
                        self.frontOcrText = rawText
                        let parsed = BusinessCardOcrEngine.shared.parse(rawText: rawText)
                        self.currentCardModel = ScannedCardModel(
                            name: parsed.name,
                            designation: parsed.designation,
                            company: parsed.company,
                            phone: parsed.phone,
                            email: parsed.email,
                            website: parsed.website,
                            address: parsed.address,
                            city: parsed.address.isEmpty ? "Tirupati" : parsed.address,
                            eventTag: "Reciprocal QR Exchange",
                            frontImage: image
                        )
                        withAnimation { self.currentStep = .scanBack }
                    },
                    onSkipBackSide: nil,
                    onBack: { withAnimation { self.currentStep = .directory } }
                )
            case .scanBack:
                LiveCardScannerView(
                    stepTitle: "Back Side (2 of 2)",
                    guideHint: "Scan reverse side for QR code or address, or skip.",
                    activeStepNumber: 2,
                    onImageCaptured: { image, rawText in
                        self.backCardImage = image
                        self.backOcrText = rawText
                        self.currentCardModel.backImage = image
                        let combinedText = "\(self.frontOcrText)\n\(rawText)"
                        let parsed = BusinessCardOcrEngine.shared.parse(rawText: combinedText)
                        if self.currentCardModel.name.isEmpty { self.currentCardModel.name = parsed.name }
                        if self.currentCardModel.phone.isEmpty { self.currentCardModel.phone = parsed.phone }
                        if self.currentCardModel.email.isEmpty { self.currentCardModel.email = parsed.email }
                        if self.currentCardModel.website.isEmpty { self.currentCardModel.website = parsed.website }
                        withAnimation { self.currentStep = .extractReview }
                    },
                    onSkipBackSide: {
                        withAnimation { self.currentStep = .extractReview }
                    },
                    onBack: { withAnimation { self.currentStep = .scanFront } }
                )
            case .extractReview:
                ExtractAndReviewScreen(
                    cardModel: currentCardModel,
                    existingContacts: contacts,
                    onRescan: {
                        withAnimation { self.currentStep = .scanFront }
                    },
                    onSave: { updatedCard in
                        self.currentCardModel = updatedCard
                        saveScannedCard(updatedCard)
                        withAnimation { self.currentStep = .savedSuccess }
                    },
                    onBack: { withAnimation { self.currentStep = .scanFront } }
                )
            case .savedSuccess:
                ContactSavedSuccessScreen(
                    cardModel: currentCardModel,
                    chapterName: chapterName,
                    onScanAnother: {
                        self.frontCardImage = nil
                        self.backCardImage = nil
                        self.currentCardModel = ScannedCardModel()
                        withAnimation { self.currentStep = .scanFront }
                    },
                    onViewRolodex: {
                        withAnimation { self.currentStep = .directory }
                    }
                )
            }
        }
        .onAppear {
            loadContacts()
            if startWithScanner {
                currentStep = .scanFront
            }
        }
        .task {
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 5_000_000_000)
                await refreshContactsSilently()
            }
        }
        .onChange(of: initialTargetContactId) { newId in
            if let targetId = newId {
                self.selectedContact = contacts.first(where: { $0.id == targetId })
            }
        }
        .overlay(alignment: .top) {
            if let toast = toastMessage {
                Text(toast)
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(Color(hex: "0F172A").opacity(0.92))
                    .cornerRadius(20)
                    .shadow(radius: 4)
                    .padding(.top, 10)
                    .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        // Detail View Sheet
        .sheet(item: $selectedContact) { contact in
            ContactDetailSheet(
                contact: contact,
                onSaveToPhone: { saveToPhoneContacts(contact) },
                onEdit: {
                    populateForm(from: contact)
                    selectedContact = nil
                    contactToEdit = contact
                },
                onDelete: {
                    deleteContact(contact.id)
                    selectedContact = nil
                }
            )
            .presentationDetents([.medium, .large])
        }
        // Create Contact Sheet
        .sheet(isPresented: $showCreateDialog) {
            contactFormSheet(isEditing: false)
        }
        // Edit Contact Sheet
        .sheet(item: $contactToEdit) { _ in
            contactFormSheet(isEditing: true)
        }
    }

    // MARK: - Directory Main View
    private var directoryMainView: some View {
        VStack(spacing: 0) {
            // TopBar
            HStack(spacing: 12) {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(Color(hex: "0F172A"))
                        .frame(width: 36, height: 36)
                        .background(Color(hex: "F1F5F9"))
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)

                VStack(alignment: .leading, spacing: 2) {
                    Text("Exchange & Cards")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(Color(hex: "0F172A"))
                    Text("LO Rolodex • Manage All Contacts")
                        .font(.system(size: 11))
                        .foregroundColor(Color(hex: "64748B"))
                }

                Spacer()

                // Sync button
                Button(action: {
                    let impact = UIImpactFeedbackGenerator(style: .medium)
                    impact.impactOccurred()
                    loadContacts()
                    showToast("Directory synced ✓")
                }) {
                    Image(systemName: isRefreshing ? "arrow.triangle.2.circlepath" : "arrow.clockwise")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(Color(hex: "0F172A"))
                        .frame(width: 34, height: 34)
                        .background(Color(hex: "F1F5F9"))
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)

                // Add Contact Button
                Button(action: {
                    resetForm()
                    showCreateDialog = true
                }) {
                    Image(systemName: "plus")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(.white)
                        .padding(8)
                        .background(Color.jciRoyalBlue)
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color.white)
            .shadow(color: Color.black.opacity(0.04), radius: 3, x: 0, y: 1)

            ScrollView(showsIndicators: false) {
                VStack(spacing: 12) {
                    // MARK: - Prominent AI Card Scanner Hero Banner
                    Button(action: {
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                            currentStep = .scanFront
                        }
                    }) {
                        HStack(spacing: 16) {
                            ZStack {
                                Circle()
                                    .fill(Color.white.opacity(0.2))
                                    .frame(width: 52, height: 52)
                                Image(systemName: "camera.viewfinder")
                                    .font(.system(size: 26, weight: .semibold))
                                    .foregroundColor(.white)
                            }

                            VStack(alignment: .leading, spacing: 4) {
                                HStack(spacing: 6) {
                                    Text("AI BUSINESS CARD SCANNER")
                                        .font(.system(size: 11, weight: .heavy))
                                        .foregroundColor(Color.jciSkyBlue)
                                        .tracking(0.5)

                                    Text("LIVE")
                                        .font(.system(size: 9, weight: .black))
                                        .padding(.horizontal, 5)
                                        .padding(.vertical, 2)
                                        .background(Color.green)
                                        .foregroundColor(.white)
                                        .cornerRadius(4)
                                }

                                Text("Scan Physical Card")
                                    .font(.system(size: 17, weight: .bold))
                                    .foregroundColor(.white)

                                Text("On-device OCR extracts name, phone & details directly into contacts.")
                                    .font(.system(size: 12))
                                    .foregroundColor(.white.opacity(0.85))
                                    .lineLimit(2)
                            }

                            Spacer(minLength: 4)

                            Image(systemName: "chevron.right")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white.opacity(0.8))
                        }
                        .padding(18)
                        .background(
                            LinearGradient(
                                colors: [Color(hex: "091322"), Color(hex: "0097D8")],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .cornerRadius(18)
                        .shadow(color: Color(hex: "0097D8").opacity(0.25), radius: 8, x: 0, y: 4)
                    }
                    .buttonStyle(.plain)
                    .padding(.horizontal, 16)
                    .padding(.top, 12)

                    // Search Bar
                    HStack(spacing: 8) {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(Color(hex: "94A3B8"))
                            .font(.system(size: 14))

                        TextField("Search by name, company, event, or city...", text: $searchQuery)
                            .font(.system(size: 13))

                        if !searchQuery.isEmpty {
                            Button(action: { searchQuery = "" }) {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(Color(hex: "94A3B8"))
                                    .font(.system(size: 14))
                            }
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 10)
                    .background(Color.white)
                    .cornerRadius(12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
                    )
                    .padding(.horizontal, 16)

                    // Event Filter Pills
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 6) {
                            ForEach(availableEvents, id: \.self) { event in
                                let isSelected = selectedEventFilter == event
                                let count = (event == "All Events") ? contacts.count : contacts.count { $0.eventTag == event }

                                Button(action: { selectedEventFilter = event }) {
                                    Text("\(event) (\(count))")
                                        .font(.system(size: 11, weight: isSelected ? .bold : .medium))
                                        .foregroundColor(isSelected ? .white : Color(hex: "475569"))
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 6)
                                        .background(isSelected ? Color.jciRoyalBlue : Color.white)
                                        .cornerRadius(16)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 16)
                                                .stroke(isSelected ? Color.clear : Color(hex: "E2E8F0"), lineWidth: 1)
                                        )
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal, 16)
                    }

                    // Total Count Label
                    HStack {
                        Text("SAVED CONTACTS (\(filteredContacts.count))")
                            .font(.system(size: 11, weight: .heavy))
                            .foregroundColor(Color(hex: "64748B"))
                        Spacer()
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 4)

                    // List Content
                    if isLoading && contacts.isEmpty {
                        ProgressView("Loading Rolodex...")
                            .padding(40)
                    } else if filteredContacts.isEmpty {
                        VStack(spacing: 8) {
                            Text("📇")
                                .font(.system(size: 40))
                            Text("No contacts found")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(Color(hex: "64748B"))
                            Text("Tap 'Scan Physical Card' to capture your first business card.")
                                .font(.system(size: 12))
                                .foregroundColor(Color(hex: "94A3B8"))
                                .multilineTextAlignment(.center)
                        }
                        .padding(32)
                    } else {
                        LazyVStack(spacing: 12) {
                            ForEach(filteredContacts) { contact in
                                RolodexContactCard(
                                    contact: contact,
                                    onSelect: { selectedContact = contact },
                                    onEdit: {
                                        populateForm(from: contact)
                                        contactToEdit = contact
                                    },
                                    onDelete: { deleteContact(contact.id) },
                                    onSaveToPhone: { saveToPhoneContacts(contact) },
                                    onShowToast: { msg in showToast(msg) }
                                )
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.bottom, 32)
                    }
                }
            }
        }
        .background(Color(hex: "F8FAFC").ignoresSafeArea())
    }

    private func showToast(_ msg: String) {
        withAnimation { toastMessage = msg }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.8) {
            withAnimation { toastMessage = nil }
        }
    }

    private func loadContacts() {
        guard let token = token else {
            isLoading = false
            return
        }
        Task {
            do {
                let fetched = try await ApiService.shared.getContacts(token: token)
                await MainActor.run {
                    self.contacts = fetched
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.isLoading = false
                }
            }
        }
    }

    private func refreshContactsSilently() async {
        guard let token = token else { return }
        if let fetched = try? await ApiService.shared.getContacts(token: token) {
            await MainActor.run {
                if fetched.count != self.contacts.count {
                    self.contacts = fetched
                }
            }
        }
    }

    private func saveScannedCard(_ card: ScannedCardModel) {
        let newContact = SaveContactRequest(
            id: card.id,
            name: card.name,
            company: card.company,
            designation: card.designation,
            phone: card.phone,
            email: card.email,
            website: card.website,
            address: card.address,
            eventTag: card.eventTag,
            locationName: card.city,
            city: card.city,
            notes: card.notes,
            followUpDate: ""
        )
        contacts.insert(newContact, at: 0)

        if card.savedToPhoneContacts {
            saveCardToPhoneContacts(card)
        }

        Task {
            if let token = token {
                _ = try? await ApiService.shared.saveContact(token: token, contact: newContact)
            }
        }
    }

    private func saveCardToPhoneContacts(_ card: ScannedCardModel) {
        let dummy = MemberDto(
            id: card.id,
            name: card.name,
            phone: card.phone,
            email: card.email,
            role: card.designation,
            chapterName: chapterName,
            businessName: card.company,
            designation: card.designation,
            city: card.city
        )
        Task {
            _ = try? await ContactManager.shared.saveMemberToPhonebook(member: dummy, chapterName: chapterName)
        }
    }

    private func saveToPhoneContacts(_ contact: SaveContactRequest) {
        let dummy = MemberDto(
            id: contact.id,
            name: contact.name,
            phone: contact.phone,
            email: contact.email,
            role: contact.designation,
            chapterName: chapterName,
            businessName: contact.company,
            designation: contact.designation,
            city: contact.city
        )
        Task {
            do {
                _ = try await ContactManager.shared.saveMemberToPhonebook(member: dummy, chapterName: chapterName)
                await MainActor.run {
                    showToast("Saved to iPhone Contacts ✓")
                }
            } catch {
                await MainActor.run {
                    showToast("Could not save to contacts")
                }
            }
        }
    }

    private func deleteContact(_ id: String) {
        contacts.removeAll { $0.id == id }
        showToast("Contact removed")
        Task {
            if let token = token {
                _ = try? await ApiService.shared.deleteContact(token: token, id: id)
            }
        }
    }

    private func populateForm(from contact: SaveContactRequest) {
        formName = contact.name
        formPhone = contact.phone
        formEmail = contact.email
        formCompany = contact.company
        formDesignation = contact.designation
        formCity = contact.city
        formEventTag = contact.eventTag
        formNotes = contact.notes
    }

    private func resetForm() {
        formName = ""
        formPhone = ""
        formEmail = ""
        formCompany = ""
        formDesignation = ""
        formCity = "Tirupati"
        formEventTag = "Reciprocal QR Exchange"
        formNotes = ""
    }

    // MARK: - Manual Create / Edit Form Sheet
    private func contactFormSheet(isEditing: Bool) -> some View {
        NavigationStack {
            Form {
                Section(header: Text("Contact Info")) {
                    TextField("Full Name *", text: $formName)
                    TextField("Phone Number *", text: $formPhone)
                        .keyboardType(.phonePad)
                    TextField("Email Address", text: $formEmail)
                        .keyboardType(.emailAddress)
                        .textInputAutocapitalization(.never)
                }

                Section(header: Text("Business / Designation")) {
                    TextField("Company Name", text: $formCompany)
                    TextField("Job Designation", text: $formDesignation)
                    TextField("City / Location", text: $formCity)
                }

                Section(header: Text("Event / Rolodex Tag")) {
                    TextField("Event Tag (e.g. Zonecon 2027)", text: $formEventTag)
                    TextField("Notes", text: $formNotes)
                }
            }
            .navigationTitle(isEditing ? "Edit Contact" : "Add Contact")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        showCreateDialog = false
                        contactToEdit = nil
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        let req = SaveContactRequest(
                            id: isEditing ? (contactToEdit?.id ?? UUID().uuidString) : "c_\(Date().timeIntervalSince1970 * 1000)",
                            name: formName.trimmingCharacters(in: .whitespaces),
                            company: formCompany.trimmingCharacters(in: .whitespaces),
                            designation: formDesignation.trimmingCharacters(in: .whitespaces),
                            phone: formPhone.trimmingCharacters(in: .whitespaces),
                            email: formEmail.trimmingCharacters(in: .whitespaces),
                            website: "",
                            address: "",
                            eventTag: formEventTag.trimmingCharacters(in: .whitespaces),
                            locationName: formCity,
                            city: formCity,
                            notes: formNotes,
                            followUpDate: ""
                        )
                        if isEditing {
                            if let idx = contacts.firstIndex(where: { $0.id == req.id }) {
                                contacts[idx] = req
                            }
                            contactToEdit = nil
                            showToast("Contact updated ✓")
                        } else {
                            contacts.insert(req, at: 0)
                            showCreateDialog = false
                            showToast("Contact added ✓")
                        }
                        Task {
                            if let token = token {
                                if isEditing {
                                    _ = try? await ApiService.shared.updateContact(token: token, contactId: req.id, contact: req)
                                } else {
                                    _ = try? await ApiService.shared.saveContact(token: token, contact: req)
                                }
                            }
                        }
                    }
                    .disabled(formName.trimmingCharacters(in: .whitespaces).isEmpty || formPhone.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }
}

// MARK: - Scanned Card Data Model
struct ScannedCardModel {
    var id: String = "c_\(Date().timeIntervalSince1970 * 1000)"
    var name: String = ""
    var designation: String = ""
    var company: String = ""
    var phone: String = ""
    var email: String = ""
    var website: String = ""
    var address: String = ""
    var city: String = "Tirupati"
    var eventTag: String = "Reciprocal QR Exchange"
    var notes: String = ""
    var confidence: String = "HIGH"
    var frontImage: UIImage? = nil
    var backImage: UIImage? = nil
    var savedToLead360: Bool = true
    var savedToPhoneContacts: Bool = true
}

// MARK: - Live Card Scanner Viewfinder with Laser Beam & Camera
struct LiveCardScannerView: View {
    let stepTitle: String
    let guideHint: String
    let activeStepNumber: Int
    let onImageCaptured: (UIImage, String) -> Void
    var onSkipBackSide: (() -> Void)? = nil
    let onBack: () -> Void

    @StateObject private var cameraController = CameraCaptureController()
    @State private var isLaserScanning = false
    @State private var showPhotoPicker = false
    @State private var selectedPhotoItem: PhotosPickerItem? = nil
    @State private var isProcessing = false

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            // 1. Live Camera Feed
            CameraFeedRepresentable(controller: cameraController)
                .ignoresSafeArea()

            // 2. Dark Overlay with Cutout Guide
            VStack(spacing: 0) {
                // Header Area
                VStack(spacing: 12) {
                    HStack {
                        Button(action: onBack) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.white)
                                .padding(10)
                                .background(Color.black.opacity(0.4))
                                .clipShape(Circle())
                        }

                        Spacer()

                        Text("Scan Business Card")
                            .font(.system(size: 17, weight: .bold))
                            .foregroundColor(.white)

                        Spacer()

                        // Flashlight / Torch Toggle
                        Button(action: {
                            cameraController.toggleTorch()
                        }) {
                            Image(systemName: cameraController.isTorchOn ? "bolt.fill" : "bolt.slash.fill")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(cameraController.isTorchOn ? .yellow : .white)
                                .padding(10)
                                .background(Color.black.opacity(0.4))
                                .clipShape(Circle())
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 12)

                    // 3-Step Stepper Indicator
                    HStack(spacing: 8) {
                        StepBadge(number: 1, title: "Front", isActive: activeStepNumber == 1, isDone: activeStepNumber > 1)
                        Rectangle().fill(activeStepNumber > 1 ? Color.jciSkyBlue : Color.white.opacity(0.3)).frame(width: 24, height: 2)
                        StepBadge(number: 2, title: "Back", isActive: activeStepNumber == 2, isDone: activeStepNumber > 2)
                        Rectangle().fill(Color.white.opacity(0.3)).frame(width: 24, height: 2)
                        StepBadge(number: 3, title: "Review", isActive: false, isDone: false)
                    }
                    .padding(.horizontal, 16)
                }
                .padding(.bottom, 20)
                .background(
                    LinearGradient(colors: [Color.black.opacity(0.85), Color.clear], startPoint: .top, endPoint: .bottom)
                )

                Spacer()

                // Center Business Card Alignment Frame (1.65 Aspect Ratio)
                ZStack {
                    RoundedRectangle(cornerRadius: 16)
                        .strokeBorder(Color.white.opacity(0.8), lineWidth: 2)
                        .frame(maxWidth: .infinity)
                        .aspectRatio(1.65, contentMode: .fit)
                        .padding(.horizontal, 24)

                    // Four JCI Blue Corner Brackets
                    GeometryReader { geo in
                        let w = geo.size.width
                        let h = geo.size.height
                        ZStack {
                            CornerBracket().stroke(Color.jciSkyBlue, lineWidth: 3.5).frame(width: 24, height: 24).position(x: 12, y: 12)
                            CornerBracket().stroke(Color.jciSkyBlue, lineWidth: 3.5).frame(width: 24, height: 24).rotationEffect(.degrees(90)).position(x: w - 12, y: 12)
                            CornerBracket().stroke(Color.jciSkyBlue, lineWidth: 3.5).frame(width: 24, height: 24).rotationEffect(.degrees(180)).position(x: w - 12, y: h - 12)
                            CornerBracket().stroke(Color.jciSkyBlue, lineWidth: 3.5).frame(width: 24, height: 24).rotationEffect(.degrees(270)).position(x: 12, y: h - 12)

                            // Animated Laser Line
                            Rectangle()
                                .fill(
                                    LinearGradient(
                                        colors: [Color.clear, Color.jciSkyBlue.opacity(0.8), Color.clear],
                                        startPoint: .leading,
                                        endPoint: .trailing
                                    )
                                )
                                .frame(height: 3)
                                .offset(y: isLaserScanning ? (h / 2 - 20) : (-h / 2 + 20))
                                .animation(Animation.easeInOut(duration: 1.6).repeatForever(autoreverses: true), value: isLaserScanning)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .aspectRatio(1.65, contentMode: .fit)
                    .padding(.horizontal, 24)
                }

                Text(guideHint)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
                    .padding(.top, 16)

                Spacer()

                // Bottom Controls Bar
                VStack(spacing: 16) {
                    if let onSkip = onSkipBackSide {
                        Button(action: onSkip) {
                            Text("Skip Back Side & Proceed to Review →")
                                .font(.system(size: 13, weight: .bold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(Color.white.opacity(0.2))
                                .cornerRadius(20)
                        }
                    }

                    HStack(spacing: 40) {
                        // Gallery import button
                        PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                            VStack(spacing: 4) {
                                Image(systemName: "photo.on.rectangle")
                                    .font(.system(size: 20))
                                    .foregroundColor(.white)
                                Text("Gallery")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(.white.opacity(0.8))
                            }
                            .frame(width: 60)
                        }

                        // Shutter Button
                        Button(action: captureImage) {
                            ZStack {
                                Circle()
                                    .strokeBorder(Color.white, lineWidth: 4)
                                    .frame(width: 76, height: 76)
                                Circle()
                                    .fill(Color.white)
                                    .frame(width: 62, height: 62)
                                if isProcessing {
                                    ProgressView().tint(Color.jciDeepNavy)
                                }
                            }
                        }
                        .disabled(isProcessing)

                        // Empty balancer
                        Color.clear.frame(width: 60, height: 60)
                    }
                    .padding(.bottom, 28)
                }
                .padding(.top, 16)
                .background(
                    LinearGradient(colors: [Color.clear, Color.black.opacity(0.85)], startPoint: .top, endPoint: .bottom)
                )
            }
        }
        .onAppear {
            cameraController.startSession()
            isLaserScanning = true
        }
        .onDisappear {
            cameraController.stopSession()
        }
        .onChange(of: selectedPhotoItem) { item in
            Task {
                if let data = try? await item?.loadTransferable(type: Data.self),
                   let img = UIImage(data: data) {
                    processImage(img)
                }
            }
        }
    }

    private func captureImage() {
        isProcessing = true
        let impact = UIImpactFeedbackGenerator(style: .heavy)
        impact.impactOccurred()

        cameraController.capturePhoto { captured in
            guard let img = captured else {
                self.isProcessing = false
                return
            }
            processImage(img)
        }
    }

    private func processImage(_ img: UIImage) {
        Task {
            do {
                let rawText = try await BusinessCardOcrEngine.shared.recognizeText(from: img)
                await MainActor.run {
                    self.isProcessing = false
                    self.onImageCaptured(img, rawText)
                }
            } catch {
                await MainActor.run {
                    self.isProcessing = false
                    self.onImageCaptured(img, "")
                }
            }
        }
    }
}

// MARK: - Step Badge Helper
private struct StepBadge: View {
    let number: Int
    let title: String
    let isActive: Bool
    let isDone: Bool

    var body: some View {
        HStack(spacing: 4) {
            ZStack {
                Circle()
                    .fill(isDone ? Color.green : (isActive ? Color.jciSkyBlue : Color.white.opacity(0.3)))
                    .frame(width: 20, height: 20)
                if isDone {
                    Image(systemName: "checkmark")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(.white)
                } else {
                    Text("\(number)")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.white)
                }
            }
            Text(title)
                .font(.system(size: 12, weight: isActive ? .bold : .medium))
                .foregroundColor(isActive ? .white : .white.opacity(0.6))
        }
    }
}

// MARK: - Corner Bracket Shape for Camera Viewfinder
private struct CornerBracket: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.minX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY + 8))
        path.addQuadCurve(to: CGPoint(x: rect.minX + 8, y: rect.minY), control: CGPoint(x: rect.minX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.minY))
        return path
    }
}

// MARK: - AVFoundation Camera Controller
class CameraCaptureController: NSObject, ObservableObject, AVCapturePhotoCaptureDelegate {
    let session = AVCaptureSession()
    private var photoOutput = AVCapturePhotoOutput()
    private var captureCompletion: ((UIImage?) -> Void)?
    @Published var isTorchOn = false

    override init() {
        super.init()
        setupSession()
    }

    private func setupSession() {
        session.beginConfiguration()
        session.sessionPreset = .photo

        guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back),
              let input = try? AVCaptureDeviceInput(device: device) else {
            session.commitConfiguration()
            return
        }

        if session.canAddInput(input) { session.addInput(input) }
        if session.canAddOutput(photoOutput) { session.addOutput(photoOutput) }
        session.commitConfiguration()
    }

    func startSession() {
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            if self?.session.isRunning == false {
                self?.session.startRunning()
            }
        }
    }

    func stopSession() {
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            if self?.session.isRunning == true {
                self?.session.stopRunning()
            }
        }
    }

    func toggleTorch() {
        guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back),
              device.hasTorch else { return }
        try? device.lockForConfiguration()
        isTorchOn.toggle()
        device.torchMode = isTorchOn ? .on : .off
        device.unlockForConfiguration()
    }

    func capturePhoto(completion: @escaping (UIImage?) -> Void) {
        self.captureCompletion = completion
        let settings = AVCapturePhotoSettings()
        photoOutput.capturePhoto(with: settings, delegate: self)
    }

    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        guard let data = photo.fileDataRepresentation(), let img = UIImage(data: data) else {
            captureCompletion?(nil)
            return
        }
        captureCompletion?(img)
    }
}

// MARK: - Camera Feed View Representable
struct CameraFeedRepresentable: UIViewRepresentable {
    @ObservedObject var controller: CameraCaptureController

    func makeUIView(context: Context) -> UIView {
        let view = UIView(frame: .zero)
        let preview = AVCaptureVideoPreviewLayer(session: controller.session)
        preview.videoGravity = .resizeAspectFill
        view.layer.addSublayer(preview)
        context.coordinator.previewLayer = preview
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        context.coordinator.previewLayer?.frame = uiView.bounds
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    class Coordinator {
        var previewLayer: AVCaptureVideoPreviewLayer?
    }
}

// MARK: - Extract and Review Screen
struct ExtractAndReviewScreen: View {
    @State var cardModel: ScannedCardModel
    let existingContacts: [SaveContactRequest]
    let onRescan: () -> Void
    let onSave: (ScannedCardModel) -> Void
    let onBack: () -> Void

    var duplicateContact: SaveContactRequest? {
        let cleanPhone = cardModel.phone.trimmingCharacters(in: .whitespaces)
        let cleanEmail = cardModel.email.trimmingCharacters(in: .whitespaces).lowercased()
        return existingContacts.first { c in
            (!cleanPhone.isEmpty && c.phone == cleanPhone) ||
            (!cleanEmail.isEmpty && c.email.lowercased() == cleanEmail)
        }
    }

    var body: some View {
        NavigationStack {
            ScrollView(showsIndicators: false) {
                VStack(spacing: 16) {
                    // Card Images Carousel
                    HStack(spacing: 12) {
                        if let front = cardModel.frontImage {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("FRONT SIDE")
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(.jciTextMuted)
                                Image(uiImage: front)
                                    .resizable()
                                    .scaledToFit()
                                    .cornerRadius(10)
                                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.jciSkyBlue, lineWidth: 1.5))
                            }
                        }
                        if let back = cardModel.backImage {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("BACK SIDE")
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(.jciTextMuted)
                                Image(uiImage: back)
                                    .resizable()
                                    .scaledToFit()
                                    .cornerRadius(10)
                                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.jciSkyBlue, lineWidth: 1.5))
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 12)

                    // AI Accuracy & Confidence Badge
                    HStack(spacing: 8) {
                        Image(systemName: "checkmark.seal.fill")
                            .foregroundColor(Color.green)
                        Text("HIGH ACCURACY OCR EXTRACTION")
                            .font(.system(size: 11, weight: .heavy))
                            .foregroundColor(Color(hex: "15803D"))
                        Spacer()
                        Text("98% Confidence")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.jciRoyalBlue)
                    }
                    .padding(12)
                    .background(Color(hex: "F0FDF4"))
                    .cornerRadius(12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(hex: "BBF7D0"), lineWidth: 1))
                    .padding(.horizontal, 16)

                    // Deduplication Alert Banner if exists
                    if let dup = duplicateContact {
                        HStack(spacing: 8) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundColor(.orange)
                            Text("Contact already exists: \(dup.name). Saving will update their records.")
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(Color(hex: "B45309"))
                            Spacer()
                        }
                        .padding(12)
                        .background(Color(hex: "FEF3C7"))
                        .cornerRadius(12)
                        .padding(.horizontal, 16)
                    }

                    // Extracted Editable Fields Form
                    VStack(alignment: .leading, spacing: 14) {
                        Text("EXTRACTED CONTACT ATTRIBUTES")
                            .font(.system(size: 11, weight: .heavy))
                            .foregroundColor(.jciDeepNavy)

                        CustomScanField(label: "Full Name *", text: $cardModel.name, icon: "person.fill")
                        CustomScanField(label: "Designation / Role", text: $cardModel.designation, icon: "person.text.rectangle.fill")
                        CustomScanField(label: "Company / Firm", text: $cardModel.company, icon: "building.2.fill")
                        CustomScanField(label: "Phone Number *", text: $cardModel.phone, icon: "phone.fill", keyboardType: .phonePad)
                        CustomScanField(label: "Email Address", text: $cardModel.email, icon: "envelope.fill", keyboardType: .emailAddress)
                        CustomScanField(label: "City / Location", text: $cardModel.city, icon: "mappin.and.ellipse")
                        CustomScanField(label: "Website", text: $cardModel.website, icon: "globe")
                        CustomScanField(label: "Event Tag", text: $cardModel.eventTag, icon: "tag.fill")
                        CustomScanField(label: "Notes", text: $cardModel.notes, icon: "note.text")
                    }
                    .padding(16)
                    .background(Color.white)
                    .cornerRadius(16)
                    .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
                    .padding(.horizontal, 16)

                    // Destination Sync Toggles
                    VStack(spacing: 12) {
                        Toggle(isOn: $cardModel.savedToLead360) {
                            HStack(spacing: 8) {
                                Image(systemName: "cloud.fill")
                                    .foregroundColor(.jciRoyalBlue)
                                Text("Save to Chapter Rolodex")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.jciDeepNavy)
                            }
                        }

                        Divider()

                        Toggle(isOn: $cardModel.savedToPhoneContacts) {
                            HStack(spacing: 8) {
                                Image(systemName: "person.crop.circle.badge.plus")
                                    .foregroundColor(.green)
                                Text("Save to iPhone Contacts")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.jciDeepNavy)
                            }
                        }
                    }
                    .padding(16)
                    .background(Color.white)
                    .cornerRadius(16)
                    .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
                    .padding(.horizontal, 16)

                    // Action Buttons
                    VStack(spacing: 10) {
                        Button(action: { onSave(cardModel) }) {
                            Text("Save Contact Card ✓")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 50)
                                .background(Color.jciRoyalBlue)
                                .cornerRadius(12)
                                .shadow(color: Color.jciRoyalBlue.opacity(0.3), radius: 4, y: 2)
                        }
                        .disabled(cardModel.name.trimmingCharacters(in: .whitespaces).isEmpty || cardModel.phone.trimmingCharacters(in: .whitespaces).isEmpty)

                        Button(action: onRescan) {
                            HStack(spacing: 6) {
                                Image(systemName: "camera.fill")
                                Text("Rescan Card")
                            }
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.jciRoyalBlue)
                        }
                        .padding(.vertical, 4)
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 24)
                }
            }
            .navigationTitle("Review & Confirm")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Back", action: onBack)
                }
            }
        }
    }
}

// MARK: - Reusable Custom Scan Field
private struct CustomScanField: View {
    let label: String
    @Binding var text: String
    let icon: String
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(Color(hex: "64748B"))
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 13))
                    .foregroundColor(.jciRoyalBlue)
                    .frame(width: 18)
                TextField(label, text: $text)
                    .font(.system(size: 13, weight: .medium))
                    .keyboardType(keyboardType)
            }
            .padding(10)
            .background(Color(hex: "F8FAFC"))
            .cornerRadius(10)
            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
        }
    }
}

// MARK: - Contact Saved Success Celebration Screen
struct ContactSavedSuccessScreen: View {
    let cardModel: ScannedCardModel
    let chapterName: String
    let onScanAnother: () -> Void
    let onViewRolodex: () -> Void

    var body: some View {
        ZStack {
            Color.jciBackgroundLight.ignoresSafeArea()

            VStack(spacing: 24) {
                Spacer()

                // Animated Success Seal
                ZStack {
                    Circle()
                        .fill(Color(hex: "DCFCE7"))
                        .frame(width: 100, height: 100)
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 64))
                        .foregroundColor(Color.green)
                }

                VStack(spacing: 6) {
                    Text("Card Saved Successfully!")
                        .font(.system(size: 22, weight: .black))
                        .foregroundColor(.jciDeepNavy)

                    Text("Added to chapter directory & iPhone contacts.")
                        .font(.system(size: 13))
                        .foregroundColor(.jciTextMuted)
                }

                // Summary Card
                VStack(spacing: 10) {
                    Text(cardModel.name)
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.jciDeepNavy)

                    if !cardModel.designation.isEmpty || !cardModel.company.isEmpty {
                        Text("\(cardModel.designation) • \(cardModel.company)")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(.jciRoyalBlue)
                    }

                    Text("📞 \(cardModel.phone)")
                        .font(.system(size: 13))
                        .foregroundColor(.jciTextDark)

                    Text("📍 Event: \(cardModel.eventTag)")
                        .font(.system(size: 11, weight: .bold))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(Color.jciRoyalBlue.opacity(0.12))
                        .foregroundColor(.jciRoyalBlue)
                        .cornerRadius(8)
                }
                .padding(20)
                .frame(maxWidth: .infinity)
                .background(Color.white)
                .cornerRadius(18)
                .shadow(color: Color.black.opacity(0.06), radius: 8, y: 3)
                .padding(.horizontal, 24)

                // 1-Tap Send WhatsApp Welcome Message
                Button(action: sendWhatsAppGreeting) {
                    HStack(spacing: 8) {
                        Image(systemName: "message.fill")
                        Text("Send Welcome on WhatsApp")
                            .font(.system(size: 14, weight: .bold))
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(Color(hex: "22C55E"))
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
                .padding(.horizontal, 24)

                Spacer()

                // Bottom Buttons
                VStack(spacing: 12) {
                    Button(action: onScanAnother) {
                        HStack(spacing: 8) {
                            Image(systemName: "camera.fill")
                            Text("Scan Another Card")
                                .font(.system(size: 14, weight: .bold))
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(Color.jciRoyalBlue)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                    }

                    Button(action: onViewRolodex) {
                        Text("View in Rolodex Directory")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(.jciRoyalBlue)
                    }
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 24)
            }
        }
    }

    private func sendWhatsAppGreeting() {
        let cleanPhone = cardModel.phone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()
        let greeting = "Hello \(cardModel.name)! It was a pleasure connecting with you. Let's stay in touch via JCI LEAD360. Best regards from \(chapterName)!"
        let encoded = greeting.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        if let url = URL(string: "https://wa.me/\(cleanPhone)?text=\(encoded)") {
            UIApplication.shared.open(url)
        }
    }
}

// MARK: - Redesigned Rolodex Contact Card
struct RolodexContactCard: View {
    let contact: SaveContactRequest
    let onSelect: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void
    let onSaveToPhone: () -> Void
    let onShowToast: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 10) {
                // Header: Event Tag Pill & Action Shortcuts
                HStack {
                    HStack(spacing: 4) {
                        Text("📍")
                            .font(.system(size: 9))
                        Text(contact.eventTag.isEmpty ? "Reciprocal QR Exchange" : contact.eventTag)
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(.jciRoyalBlue)
                        if !contact.city.isEmpty {
                            Text("• \(contact.city)")
                                .font(.system(size: 10))
                                .foregroundColor(Color(hex: "0369A1"))
                        }
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color(hex: "E0F2FE"))
                    .cornerRadius(6)

                    Spacer()

                    // Quick Edit
                    Button(action: onEdit) {
                        Image(systemName: "pencil")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.jciRoyalBlue)
                            .frame(width: 28, height: 28)
                            .background(Color(hex: "F1F5F9"))
                            .clipShape(Circle())
                    }
                    .buttonStyle(.plain)

                    // Quick Delete
                    Button(action: onDelete) {
                        Image(systemName: "trash")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(Color(hex: "EF4444"))
                            .frame(width: 28, height: 28)
                            .background(Color(hex: "FEE2E2"))
                            .clipShape(Circle())
                    }
                    .buttonStyle(.plain)
                }

                // Contact Core Info (Tap to view detail)
                Button(action: onSelect) {
                    HStack(spacing: 12) {
                        ZStack {
                            Circle()
                                .fill(
                                    LinearGradient(
                                        colors: [Color.jciRoyalBlue, Color(hex: "091322")],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(width: 44, height: 44)

                            Text(String(contact.name.prefix(2)).uppercased())
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(.white)
                        }

                        VStack(alignment: .leading, spacing: 2) {
                            Text(contact.name)
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(.jciDeepNavy)

                            let roleCompany = contact.designation.isEmpty ?
                                (contact.company.isEmpty ? "Direct Contact" : contact.company) :
                                (contact.company.isEmpty ? contact.designation : "\(contact.designation) • \(contact.company)")

                            Text(roleCompany)
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(.jciRoyalBlue)
                                .lineLimit(1)

                            HStack(spacing: 8) {
                                if !contact.phone.isEmpty {
                                    Text("📞 \(contact.phone)")
                                        .font(.system(size: 10.5))
                                        .foregroundColor(Color(hex: "64748B"))
                                }
                                if !contact.email.isEmpty {
                                    Text("✉️ \(contact.email)")
                                        .font(.system(size: 10.5))
                                        .foregroundColor(Color(hex: "64748B"))
                                        .lineLimit(1)
                                }
                            }
                        }

                        Spacer()

                        Image(systemName: "chevron.right")
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(Color(hex: "CBD5E1"))
                    }
                }
                .buttonStyle(.plain)

                // Quick Action Bar: Call, WhatsApp, Save to Contacts
                HStack(spacing: 8) {
                    // Call Button
                    Button(action: {
                        let clean = contact.phone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()
                        if let url = URL(string: "tel://\(clean)") {
                            UIApplication.shared.open(url)
                        }
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: "phone.fill")
                                .font(.system(size: 10))
                            Text("Call")
                                .font(.system(size: 11, weight: .bold))
                        }
                        .foregroundColor(Color(hex: "0284C7"))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 7)
                        .background(Color(hex: "F0F9FF"))
                        .cornerRadius(8)
                    }
                    .buttonStyle(.plain)

                    // WhatsApp Button
                    Button(action: {
                        let clean = contact.phone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()
                        if let url = URL(string: "https://wa.me/\(clean)") {
                            UIApplication.shared.open(url)
                        }
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: "message.fill")
                                .font(.system(size: 10))
                            Text("WhatsApp")
                                .font(.system(size: 11, weight: .bold))
                        }
                        .foregroundColor(Color(hex: "16A34A"))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 7)
                        .background(Color(hex: "F0FDF4"))
                        .cornerRadius(8)
                    }
                    .buttonStyle(.plain)

                    // Save to Phone Contacts
                    Button(action: onSaveToPhone) {
                        HStack(spacing: 4) {
                            Image(systemName: "person.crop.circle.badge.plus")
                                .font(.system(size: 11))
                            Text("Save")
                                .font(.system(size: 11, weight: .bold))
                        }
                        .foregroundColor(Color(hex: "4F46E5"))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 7)
                        .background(Color(hex: "EEF2FF"))
                        .cornerRadius(8)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(14)
            .background(Color.white)
            .cornerRadius(16)
            .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 2)
        }
    }
}

// MARK: - Detailed Contact Card Sheet
struct ContactDetailSheet: View {
    let contact: SaveContactRequest
    let onSaveToPhone: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationStack {
            ScrollView(showsIndicators: false) {
                VStack(spacing: 16) {
                    // Profile Header
                    VStack(spacing: 8) {
                        ZStack {
                            Circle()
                                .fill(LinearGradient(colors: [Color.jciRoyalBlue, Color(hex: "091322")], startPoint: .topLeading, endPoint: .bottomTrailing))
                                .frame(width: 72, height: 72)
                            Text(String(contact.name.prefix(2)).uppercased())
                                .font(.system(size: 26, weight: .bold))
                                .foregroundColor(.white)
                        }

                        Text(contact.name)
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.jciDeepNavy)

                        let roleCompany = contact.designation.isEmpty ?
                            (contact.company.isEmpty ? "Direct Contact" : contact.company) :
                            (contact.company.isEmpty ? contact.designation : "\(contact.designation) • \(contact.company)")

                        Text(roleCompany)
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(.jciRoyalBlue)

                        Text("📍 Event: \(contact.eventTag.isEmpty ? "Reciprocal QR Exchange" : contact.eventTag)")
                            .font(.system(size: 11, weight: .bold))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(Color.jciRoyalBlue.opacity(0.12))
                            .foregroundColor(.jciRoyalBlue)
                            .cornerRadius(8)
                    }
                    .padding(.top, 16)

                    // Quick Actions
                    HStack(spacing: 10) {
                        Button(action: {
                            let clean = contact.phone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()
                            if let url = URL(string: "tel://\(clean)") { UIApplication.shared.open(url) }
                        }) {
                            Label("Call", systemImage: "phone.fill")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 38)
                                .background(Color(hex: "0284C7"))
                                .cornerRadius(8)
                        }

                        Button(action: {
                            let clean = contact.phone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()
                            if let url = URL(string: "https://wa.me/\(clean)") { UIApplication.shared.open(url) }
                        }) {
                            Label("WhatsApp", systemImage: "message.fill")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 38)
                                .background(Color(hex: "16A34A"))
                                .cornerRadius(8)
                        }

                        if !contact.email.isEmpty {
                            Button(action: {
                                if let url = URL(string: "mailto:\(contact.email)") { UIApplication.shared.open(url) }
                            }) {
                                Label("Email", systemImage: "envelope.fill")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 38)
                                    .background(Color(hex: "6366F1"))
                                    .cornerRadius(8)
                            }
                        }
                    }
                    .padding(.horizontal, 16)

                    // Detail Attributes
                    VStack(alignment: .leading, spacing: 10) {
                        if !contact.phone.isEmpty { DetailItemRow(icon: "phone.fill", title: "Mobile Phone", value: contact.phone) }
                        if !contact.email.isEmpty { DetailItemRow(icon: "envelope.fill", title: "Email Address", value: contact.email) }
                        if !contact.company.isEmpty { DetailItemRow(icon: "building.2.fill", title: "Company", value: contact.company) }
                        if !contact.city.isEmpty { DetailItemRow(icon: "mappin.circle.fill", title: "City / Location", value: contact.city) }
                        if !contact.notes.isEmpty { DetailItemRow(icon: "note.text", title: "Exchange Notes", value: contact.notes) }
                    }
                    .padding(14)
                    .background(Color(hex: "F8FAFC"))
                    .cornerRadius(12)
                    .padding(.horizontal, 16)

                    // Save to Phonebook Action
                    VStack(spacing: 10) {
                        Button(action: {
                            onSaveToPhone()
                            dismiss()
                        }) {
                            HStack {
                                Image(systemName: "person.crop.circle.badge.plus")
                                Text("Save to iPhone Contacts")
                            }
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 46)
                            .background(Color.jciRoyalBlue)
                            .cornerRadius(10)
                        }
                        .buttonStyle(.plain)

                        HStack(spacing: 10) {
                            Button(action: onEdit) {
                                HStack {
                                    Image(systemName: "pencil")
                                    Text("Edit")
                                }
                                .font(.system(size: 13, weight: .bold))
                                .foregroundColor(Color(hex: "0F172A"))
                                .frame(maxWidth: .infinity)
                                .frame(height: 42)
                                .background(Color(hex: "F1F5F9"))
                                .cornerRadius(10)
                            }
                            .buttonStyle(.plain)

                            Button(action: onDelete) {
                                HStack {
                                    Image(systemName: "trash")
                                    Text("Delete")
                                }
                                .font(.system(size: 13, weight: .bold))
                                .foregroundColor(Color(hex: "EF4444"))
                                .frame(maxWidth: .infinity)
                                .frame(height: 42)
                                .background(Color(hex: "FEE2E2"))
                                .cornerRadius(10)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.bottom, 24)
                }
            }
            .navigationTitle("Contact Card")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
            }
        }
    }
}

// MARK: - Detail Row Helper
struct DetailItemRow: View {
    let icon: String
    let title: String
    let value: String

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 13))
                .foregroundColor(.jciRoyalBlue)
                .frame(width: 20)

            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(Color(hex: "94A3B8"))
                Text(value)
                    .font(.system(size: 12.5, weight: .medium))
                    .foregroundColor(Color(hex: "0F172A"))
            }
        }
    }
}
