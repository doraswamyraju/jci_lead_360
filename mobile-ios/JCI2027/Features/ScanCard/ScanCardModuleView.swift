import SwiftUI
import Contacts
import UIKit

/// Replicates the official Android ScanCardModuleScreen.kt ("Exchange & Cards" / LO Rolodex)
/// Features:
/// - TopBar with Back, Live Sync, and Add Contact
/// - Real-time Search by Name, Company, Event, or City
/// - Dynamic Event Tag Filter Pills ("All Events", "Reciprocal QR Exchange", etc.)
/// - Rich Contact Cards with 1-tap Call (`tel:`), WhatsApp, Save to Contacts (`CNContactStore`), Edit, and Delete
/// - Create & Edit Contact Modals syncing directly to backend API
/// - Detailed Contact Card Sheet
/// - 5-Second periodic auto-poll matching Android parity
struct ScanCardModuleView: View {
    let chapterName: String
    let token: String?
    let initialTargetContactId: String?
    let onBack: () -> Void

    @State private var contacts: [SaveContactRequest] = []
    @State private var isLoading = true
    @State private var isRefreshing = false
    @State private var searchQuery = ""
    @State private var selectedEventFilter = "All Events"
    @State private var selectedContact: SaveContactRequest? = nil
    @State private var contactToEdit: SaveContactRequest? = nil
    @State private var showCreateDialog = false
    @State private var toastMessage: String? = nil

    // Form fields (Create / Edit)
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
        VStack(spacing: 0) {
            // MARK: - TopBar (Parity with ScanCardModuleScreen.kt:290-329)
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
                    HStack(spacing: 4) {
                        Image(systemName: "plus")
                            .font(.system(size: 12, weight: .heavy))
                        Text("Add Contact")
                            .font(.system(size: 12, weight: .bold))
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 7)
                    .background(Color(hex: "0097D8"))
                    .cornerRadius(8)
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color.white)
            .shadow(color: Color.black.opacity(0.04), radius: 3, x: 0, y: 1)

            // MARK: - Search & Event Filter Bar
            VStack(spacing: 8) {
                // Search Input Box
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
                .padding(.vertical, 9)
                .background(Color(hex: "F8FAFC"))
                .cornerRadius(10)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
                )

                // Event Filter Pills
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 6) {
                        ForEach(availableEvents, id: \.self) { event in
                            let isSelected = selectedEventFilter == event
                            let count = (event == "All Events") ? contacts.count : contacts.count { $0.eventTag == event }

                            Button(action: {
                                selectedEventFilter = event
                            }) {
                                Text("\(event) (\(count))")
                                    .font(.system(size: 11, weight: isSelected ? .bold : .medium))
                                    .foregroundColor(isSelected ? .white : Color(hex: "475569"))
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 5)
                                    .background(isSelected ? Color(hex: "0097D8") : Color(hex: "F1F5F9"))
                                    .cornerRadius(20)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 20)
                                            .stroke(isSelected ? Color.clear : Color(hex: "E2E8F0"), lineWidth: 1)
                                    )
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.vertical, 2)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color.white)

            Divider()

            // Header Row: Count & Quick Add
            HStack {
                Text("TOTAL CONTACTS (\(filteredContacts.count))")
                    .font(.system(size: 11, weight: .heavy))
                    .foregroundColor(Color(hex: "64748B"))

                Spacer()

                Button(action: {
                    resetForm()
                    showCreateDialog = true
                }) {
                    Text("+ New Contact")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(Color(hex: "0097D8"))
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)

            // MARK: - Contacts List
            if isLoading {
                Spacer()
                ProgressView("Loading Rolodex...")
                    .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "0097D8")))
                Spacer()
            } else if filteredContacts.isEmpty {
                Spacer()
                VStack(spacing: 8) {
                    Text("📇")
                        .font(.system(size: 42))
                    Text("No contacts found")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(Color(hex: "64748B"))
                    Text("Tap '+ Add Contact' to create a new card in the Rolodex.")
                        .font(.system(size: 12))
                        .foregroundColor(Color(hex: "94A3B8"))
                }
                .padding(32)
                Spacer()
            } else {
                ScrollView {
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
                    .padding(.bottom, 24)
                }
            }
        }
        .background(Color(hex: "F8FAFC").ignoresSafeArea())
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
        .onAppear {
            loadContacts()
        }
        .task {
            // Auto-poll contacts periodically (every 5 seconds) matching Android parity so newly exchanged contacts appear live
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

    // MARK: - Contact Form Sheet (Create & Edit)
    @ViewBuilder
    private func contactFormSheet(isEditing: Bool) -> some View {
        NavigationStack {
            Form {
                Section("Visitor / Member Details") {
                    TextField("Full Name *", text: $formName)
                    TextField("Phone Number", text: $formPhone)
                        .keyboardType(.phonePad)
                    TextField("Email Address", text: $formEmail)
                        .keyboardType(.emailAddress)
                }

                Section("Professional Information") {
                    TextField("Company / Business", text: $formCompany)
                    TextField("Designation / Title", text: $formDesignation)
                    TextField("City / Region", text: $formCity)
                }

                Section("Exchange Context & Notes") {
                    TextField("Event Tag (e.g. Reciprocal QR Exchange)", text: $formEventTag)
                    TextField("Exchange notes or context...", text: $formNotes)
                }
            }
            .navigationTitle(isEditing ? "Edit Contact" : "New Rolodex Contact")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        showCreateDialog = false
                        contactToEdit = nil
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(isEditing ? "Update" : "Save") {
                        if isEditing {
                            updateExistingContact()
                        } else {
                            createNewContact()
                        }
                    }
                    .disabled(formName.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }

    private func resetForm() {
        formName = ""
        formPhone = ""
        formEmail = ""
        formCompany = ""
        formDesignation = ""
        formCity = ""
        formEventTag = "Reciprocal QR Exchange"
        formNotes = ""
    }

    private func populateForm(from contact: SaveContactRequest) {
        formName = contact.name
        formPhone = contact.phone
        formEmail = contact.email
        formCompany = contact.company
        formDesignation = contact.designation
        formCity = contact.city
        formEventTag = contact.eventTag.isEmpty ? "Reciprocal QR Exchange" : contact.eventTag
        formNotes = contact.notes
    }

    // MARK: - Backend API Actions
    private func loadContacts() {
        guard let token = token else {
            isLoading = false
            return
        }
        isLoading = true
        Task {
            do {
                let fetched = try await ApiService.shared.getContacts(token: token)
                await MainActor.run {
                    self.contacts = fetched
                    self.isLoading = false
                    if let targetId = initialTargetContactId {
                        self.selectedContact = fetched.first(where: { $0.id == targetId })
                    }
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
                self.contacts = fetched
                if let targetId = initialTargetContactId, selectedContact == nil {
                    self.selectedContact = fetched.first(where: { $0.id == targetId })
                }
            }
        }
    }

    private func createNewContact() {
        guard let token = token else { return }
        let newC = SaveContactRequest(
            id: "c_\(Int(Date().timeIntervalSince1970))",
            name: formName.trimmingCharacters(in: .whitespaces),
            company: formCompany.trimmingCharacters(in: .whitespaces),
            designation: formDesignation.trimmingCharacters(in: .whitespaces),
            phone: formPhone.trimmingCharacters(in: .whitespaces),
            email: formEmail.trimmingCharacters(in: .whitespaces),
            website: "",
            address: formCity.trimmingCharacters(in: .whitespaces),
            eventTag: formEventTag.trimmingCharacters(in: .whitespaces).isEmpty ? "Reciprocal QR Exchange" : formEventTag,
            locationName: "JCI Event",
            city: formCity.trimmingCharacters(in: .whitespaces),
            notes: formNotes.trimmingCharacters(in: .whitespaces),
            followUpDate: "2027",
            photoUrl: nil
        )

        contacts.insert(newC, at: 0)
        showCreateDialog = false
        showToast("Contact '\(newC.name)' added ✓")

        Task {
            _ = try? await ApiService.shared.saveContact(token: token, contact: newC)
        }
    }

    private func updateExistingContact() {
        guard let token = token, let original = contactToEdit else { return }
        let updated = SaveContactRequest(
            id: original.id,
            name: formName.trimmingCharacters(in: .whitespaces),
            company: formCompany.trimmingCharacters(in: .whitespaces),
            designation: formDesignation.trimmingCharacters(in: .whitespaces),
            phone: formPhone.trimmingCharacters(in: .whitespaces),
            email: formEmail.trimmingCharacters(in: .whitespaces),
            website: original.website,
            address: formCity.trimmingCharacters(in: .whitespaces),
            eventTag: formEventTag.trimmingCharacters(in: .whitespaces).isEmpty ? original.eventTag : formEventTag,
            locationName: original.locationName,
            city: formCity.trimmingCharacters(in: .whitespaces),
            notes: formNotes.trimmingCharacters(in: .whitespaces),
            followUpDate: original.followUpDate,
            photoUrl: original.photoUrl
        )

        if let idx = contacts.firstIndex(where: { $0.id == original.id }) {
            contacts[idx] = updated
        }
        contactToEdit = nil
        showToast("Contact '\(updated.name)' updated ✓")

        Task {
            _ = try? await ApiService.shared.saveContact(token: token, contact: updated)
        }
    }

    private func deleteContact(_ id: String) {
        guard let token = token else { return }
        contacts.removeAll(where: { $0.id == id })
        showToast("Contact deleted")

        Task {
            _ = try? await ApiService.shared.deleteContact(token: token, id: id)
        }
    }

    private func saveToPhoneContacts(_ contact: SaveContactRequest) {
        let store = CNContactStore()
        let newContact = CNMutableContact()
        newContact.givenName = contact.name
        newContact.organizationName = contact.company
        newContact.jobTitle = contact.designation
        newContact.note = "Connected via JCI LEAD360. Note: \(contact.notes)"

        if !contact.phone.isEmpty {
            newContact.phoneNumbers = [CNLabeledValue(label: CNLabelPhoneNumberMobile, value: CNPhoneNumber(stringValue: contact.phone))]
        }
        if !contact.email.isEmpty {
            newContact.emailAddresses = [CNLabeledValue(label: CNLabelWork, value: contact.email as NSString)]
        }

        let request = CNSaveRequest()
        request.add(newContact, toContainerWithIdentifier: nil)

        store.requestAccess(for: .contacts) { granted, _ in
            DispatchQueue.main.async {
                if granted {
                    do {
                        try store.execute(request)
                        let generator = UINotificationFeedbackGenerator()
                        generator.notificationOccurred(.success)
                        self.showToast("Saved to iPhone Contacts! 📲")
                    } catch {
                        self.showToast("Failed to save contact")
                    }
                } else {
                    self.showToast("Contacts permission required")
                }
            }
        }
    }

    private func showToast(_ message: String) {
        withAnimation { toastMessage = message }
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            withAnimation { self.toastMessage = nil }
        }
    }
}

// MARK: - Rich Rolodex Contact Card (Parity with ScanCardModuleScreen.kt:431-570)
struct RolodexContactCard: View {
    let contact: SaveContactRequest
    let onSelect: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void
    let onSaveToPhone: () -> Void
    let onShowToast: (String) -> Void

    var cleanPhone: String {
        contact.phone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()
    }

    var body: some View {
        VStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 10) {
                // Header: Event Tag Pill & Quick Edit/Delete
                HStack {
                    HStack(spacing: 4) {
                        Text("📍")
                            .font(.system(size: 9))
                        Text(contact.eventTag.isEmpty ? "Reciprocal QR Exchange" : contact.eventTag)
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(Color(hex: "0284C7"))
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
                            .foregroundColor(Color(hex: "0097D8"))
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
                                        colors: [Color(hex: "0097D8"), Color(hex: "002F6C")],
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
                                .foregroundColor(Color(hex: "0F172A"))

                            let roleCompany = contact.designation.isEmpty ?
                                (contact.company.isEmpty ? "Direct Contact" : contact.company) :
                                (contact.company.isEmpty ? contact.designation : "\(contact.designation) • \(contact.company)")

                            Text(roleCompany)
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(Color(hex: "0284C7"))
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
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color(hex: "CBD5E1"))
                    }
                }
                .buttonStyle(.plain)

                Divider()
                    .padding(.vertical, 2)

                // Fast Action Bar (Call, WhatsApp, Save to Device)
                HStack(spacing: 8) {
                    // Call Button
                    if let url = URL(string: "tel:\(cleanPhone)"), !contact.phone.isEmpty {
                        Link(destination: url) {
                            HStack(spacing: 4) {
                                Text("📞")
                                    .font(.system(size: 10))
                                Text("Call")
                                    .font(.system(size: 11, weight: .bold))
                            }
                            .foregroundColor(Color(hex: "0F172A"))
                            .frame(maxWidth: .infinity)
                            .frame(height: 32)
                            .background(Color(hex: "F8FAFC"))
                            .cornerRadius(8)
                            .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "CBD5E1"), lineWidth: 1))
                        }
                    }

                    // WhatsApp Button
                    if let waUrl = URL(string: "https://wa.me/\(cleanPhone)"), !contact.phone.isEmpty {
                        Link(destination: waUrl) {
                            HStack(spacing: 4) {
                                Text("💬")
                                    .font(.system(size: 10))
                                Text("WhatsApp")
                                    .font(.system(size: 11, weight: .bold))
                            }
                            .foregroundColor(Color(hex: "15803D"))
                            .frame(maxWidth: .infinity)
                            .frame(height: 32)
                            .background(Color(hex: "DCFCE7"))
                            .cornerRadius(8)
                        }
                    }

                    // Save to Phone Contacts
                    Button(action: onSaveToPhone) {
                        HStack(spacing: 4) {
                            Image(systemName: "person.crop.circle.badge.plus")
                                .font(.system(size: 11, weight: .bold))
                            Text("Save")
                                .font(.system(size: 11, weight: .bold))
                        }
                        .foregroundColor(Color(hex: "0284C7"))
                        .frame(maxWidth: .infinity)
                        .frame(height: 32)
                        .background(Color(hex: "E0F2FE"))
                        .cornerRadius(8)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(14)
        }
        .background(Color.white)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.03), radius: 4, x: 0, y: 1)
    }
}

// MARK: - Detailed Contact Sheet View
struct ContactDetailSheet: View {
    let contact: SaveContactRequest
    let onSaveToPhone: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void
    @Environment(\.dismiss) var dismiss

    var cleanPhone: String {
        contact.phone.components(separatedBy: CharacterSet.decimalDigits.inverted).joined()
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    // Avatar & Name Card
                    VStack(spacing: 6) {
                        ZStack {
                            Circle()
                                .fill(
                                    LinearGradient(
                                        colors: [Color(hex: "0097D8"), Color(hex: "002F6C")],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(width: 72, height: 72)

                            Text(String(contact.name.prefix(2)).uppercased())
                                .font(.system(size: 24, weight: .heavy))
                                .foregroundColor(.white)
                        }

                        Text(contact.name)
                            .font(.system(size: 20, weight: .heavy))
                            .foregroundColor(Color(hex: "0F172A"))

                        Text(contact.designation.isEmpty ? (contact.company.isEmpty ? "Direct Contact" : contact.company) : "\(contact.designation) • \(contact.company)")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(Color(hex: "0097D8"))

                        if !contact.eventTag.isEmpty {
                            Text("📍 \(contact.eventTag)")
                                .font(.system(size: 10.5, weight: .bold))
                                .foregroundColor(Color(hex: "0284C7"))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(Color(hex: "E0F2FE"))
                                .cornerRadius(6)
                        }
                    }
                    .padding(.top, 12)

                    // Communication Action Buttons
                    HStack(spacing: 10) {
                        if let callUrl = URL(string: "tel:\(cleanPhone)"), !contact.phone.isEmpty {
                            Link(destination: callUrl) {
                                Label("Call", systemImage: "phone.fill")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 38)
                                    .background(Color(hex: "0097D8"))
                                    .cornerRadius(8)
                            }
                        }

                        if let waUrl = URL(string: "https://wa.me/\(cleanPhone)"), !contact.phone.isEmpty {
                            Link(destination: waUrl) {
                                Label("WhatsApp", systemImage: "message.fill")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 38)
                                    .background(Color(hex: "16A34A"))
                                    .cornerRadius(8)
                            }
                        }

                        if let mailUrl = URL(string: "mailto:\(contact.email)"), !contact.email.isEmpty {
                            Link(destination: mailUrl) {
                                Label("Email", systemImage: "envelope.fill")
                                    .font(.system(size: 12, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 38)
                                    .background(Color(hex: "0284C7"))
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

                    // Actions
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
                            .background(Color(hex: "0097D8"))
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

struct DetailItemRow: View {
    let icon: String
    let title: String
    let value: String

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 13))
                .foregroundColor(Color(hex: "0097D8"))
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
