import SwiftUI
import Contacts

struct ScanCardModuleView: View {
    let chapterName: String
    let token: String?
    let initialTargetContactId: String?
    let onBack: () -> Void

    @State private var contacts: [SaveContactRequest] = []
    @State private var isLoading = true
    @State private var searchQuery = ""
    @State private var selectedContact: SaveContactRequest? = nil
    @State private var showCreateDialog = false

    // New contact form fields
    @State private var newName = ""
    @State private var newPhone = ""
    @State private var newEmail = ""
    @State private var newCompany = ""
    @State private var newDesignation = ""
    @State private var newCity = ""
    @State private var newNotes = ""

    var filteredContacts: [SaveContactRequest] {
        if searchQuery.isEmpty { return contacts }
        return contacts.filter {
            $0.name.localizedCaseInsensitiveContains(searchQuery) ||
            $0.company.localizedCaseInsensitiveContains(searchQuery) ||
            $0.phone.localizedCaseInsensitiveContains(searchQuery)
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

                Text("Scan Card & AI Rolodex")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.jciTextDark)

                Spacer()

                Button(action: { showCreateDialog.toggle() }) {
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
                TextField("Search directory by name or company...", text: $searchQuery)
            }
            .padding(12)
            .background(Color.white)
            .cornerRadius(12)
            .padding(.horizontal, 20)
            .padding(.bottom, 12)

            // Contacts List
            if isLoading {
                Spacer()
                ProgressView("Loading Rolodex...")
                Spacer()
            } else if filteredContacts.isEmpty {
                Spacer()
                VStack(spacing: 8) {
                    Image(systemName: "person.crop.rectangle.badge.plus")
                        .font(.system(size: 40))
                        .foregroundColor(.jciTextMuted)
                    Text("No Contacts in Rolodex")
                        .font(.system(size: 16, weight: .bold))
                    Text("Exchanged QR cards and scanned cards will appear here.")
                        .font(.system(size: 12))
                        .foregroundColor(.jciTextMuted)
                }
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(filteredContacts) { contact in
                            ContactCardRow(contact: contact) {
                                selectedContact = contact
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
            loadContacts()
        }
        .sheet(item: $selectedContact) { contact in
            ContactDetailSheet(contact: contact, onSaveToPhone: {
                saveToPhoneContacts(contact)
            }, onDelete: {
                deleteContact(contact.id)
            })
            .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showCreateDialog) {
            NavigationStack {
                Form {
                    Section("Visitor Details") {
                        TextField("Full Name", text: $newName)
                        TextField("Phone", text: $newPhone)
                        TextField("Email", text: $newEmail)
                    }
                    Section("Professional Info") {
                        TextField("Company / Business", text: $newCompany)
                        TextField("Designation", text: $newDesignation)
                        TextField("City", text: $newCity)
                    }
                    Section("Notes") {
                        TextField("Exchange notes or context...", text: $newNotes)
                    }
                }
                .navigationTitle("New Rolodex Contact")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { showCreateDialog = false }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Save") {
                            createContact()
                        }
                        .disabled(newName.isEmpty)
                    }
                }
            }
        }
    }

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

    private func createContact() {
        guard let token = token else { return }
        let newC = SaveContactRequest(
            id: "c_\(Date().timeIntervalSince1970)",
            name: newName,
            company: newCompany,
            designation: newDesignation,
            phone: newPhone,
            email: newEmail,
            website: "",
            address: newCity,
            eventTag: "Reciprocal QR Exchange",
            locationName: "JCI Event",
            city: newCity,
            notes: newNotes,
            followUpDate: "2027",
            photoUrl: nil
        )

        Task {
            do {
                _ = try await ApiService.shared.saveContact(token: token, contact: newC)
                await MainActor.run {
                    showCreateDialog = false
                    newName = ""; newPhone = ""; newEmail = ""; newCompany = ""; newDesignation = ""; newCity = ""; newNotes = ""
                    loadContacts()
                }
            } catch {}
        }
    }

    private func deleteContact(_ id: String) {
        guard let token = token else { return }
        Task {
            do {
                _ = try await ApiService.shared.deleteContact(token: token, id: id)
                loadContacts()
            } catch {}
        }
    }

    private func saveToPhoneContacts(_ contact: SaveContactRequest) {
        let store = CNContactStore()
        let newContact = CNMutableContact()
        newContact.givenName = contact.name
        newContact.organizationName = contact.company
        newContact.jobTitle = contact.designation
        newContact.note = "Saved via JCI LEAD360. Note: \(contact.notes)"

        if !contact.phone.isEmpty {
            newContact.phoneNumbers = [CNLabeledValue(label: CNLabelPhoneNumberMobile, value: CNPhoneNumber(stringValue: contact.phone))]
        }
        if !contact.email.isEmpty {
            newContact.emailAddresses = [CNLabeledValue(label: CNLabelWork, value: contact.email as NSString)]
        }

        let request = CNSaveRequest()
        request.add(newContact, toContainerWithIdentifier: nil)

        store.requestAccess(for: .contacts) { granted, _ in
            if granted {
                try? store.execute(request)
            }
        }
    }
}

struct ContactCardRow: View {
    let contact: SaveContactRequest
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(JciGradients.shield)
                        .frame(width: 46, height: 46)
                    Text(String(contact.name.prefix(1)))
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text(contact.name)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.jciTextDark)

                    Text(contact.designation.isEmpty ? (contact.company.isEmpty ? "Visitor" : contact.company) : "\(contact.designation) • \(contact.company)")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.jciTextMuted)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.jciTextMuted.opacity(0.5))
            }
            .padding(14)
            .background(Color.white)
            .cornerRadius(16)
            .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
        }
    }
}

struct ContactDetailSheet: View {
    let contact: SaveContactRequest
    let onSaveToPhone: () -> Void
    let onDelete: () -> Void
    @Environment(\.dismiss) var dismiss

    var body: some View {
        VStack(spacing: 18) {
            Capsule()
                .fill(Color.jciBorderSubtle)
                .frame(width: 40, height: 5)
                .padding(.top, 10)

            VStack(spacing: 6) {
                Text(contact.name)
                    .font(.system(size: 20, weight: .bold))
                Text(contact.company.isEmpty ? "Direct Contact" : contact.company)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(.jciRoyalBlue)
            }

            VStack(alignment: .leading, spacing: 10) {
                if !contact.phone.isEmpty { Label(contact.phone, systemImage: "phone.fill") }
                if !contact.email.isEmpty { Label(contact.email, systemImage: "envelope.fill") }
                if !contact.city.isEmpty { Label(contact.city, systemImage: "mappin.circle.fill") }
                if !contact.notes.isEmpty { Label(contact.notes, systemImage: "note.text") }
            }
            .font(.system(size: 13))
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(14)
            .background(Color.jciBackgroundLight)
            .cornerRadius(12)

            HStack(spacing: 12) {
                Button(action: {
                    onSaveToPhone()
                    dismiss()
                }) {
                    Label("Save to iOS Contacts", systemImage: "person.crop.circle.badge.plus")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(Color.jciRoyalBlue)
                        .cornerRadius(12)
                }

                Button(action: {
                    onDelete()
                    dismiss()
                }) {
                    Image(systemName: "trash.fill")
                        .foregroundColor(.red)
                        .frame(width: 48, height: 48)
                        .background(Color.red.opacity(0.1))
                        .cornerRadius(12)
                }
            }

            Spacer()
        }
        .padding(20)
    }
}

