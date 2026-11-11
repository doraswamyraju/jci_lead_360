import SwiftUI

struct ExtractAndReviewSheet: View {
    let capturedImage: UIImage?
    @State var parsed: ParsedCardData
    let token: String?
    let onSave: (SaveContactRequest) -> Void
    let onRescan: () -> Void
    @Environment(\.dismiss) var dismiss

    @State private var name: String = ""
    @State private var phone: String = ""
    @State private var email: String = ""
    @State private var company: String = ""
    @State private var designation: String = ""
    @State private var website: String = ""
    @State private var address: String = ""
    @State private var city: String = "Tirupati"
    @State private var eventTag: String = "CardSnap Scanner"
    @State private var notes: String = ""
    @State private var saveToPhoneContacts: Bool = true
    @State private var isSaving: Bool = false

    init(
        capturedImage: UIImage?,
        parsed: ParsedCardData,
        token: String?,
        onSave: @escaping (SaveContactRequest) -> Void,
        onRescan: @escaping () -> Void
    ) {
        self.capturedImage = capturedImage
        self.parsed = parsed
        self.token = token
        self.onSave = onSave
        self.onRescan = onRescan

        _name = State(initialValue: parsed.name)
        _phone = State(initialValue: parsed.phone)
        _email = State(initialValue: parsed.email)
        _company = State(initialValue: parsed.company)
        _designation = State(initialValue: parsed.designation)
        _website = State(initialValue: parsed.website)
        _address = State(initialValue: parsed.address)
        _notes = State(initialValue: parsed.notes.isEmpty ? "Scanned via CardSnap OCR" : parsed.notes)
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    // 1. Card Thumbnail Preview Box with Confidence Indicator
                    VStack(spacing: 8) {
                        if let img = capturedImage {
                            Image(uiImage: img)
                                .resizable()
                                .scaledToFit()
                                .frame(maxHeight: 180)
                                .cornerRadius(12)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(Color(hex: "0097D8"), lineWidth: 1.5)
                                )
                                .shadow(color: Color.black.opacity(0.08), radius: 6, x: 0, y: 3)
                        }

                        HStack {
                            HStack(spacing: 4) {
                                Text("⚡")
                                    .font(.system(size: 11))
                                Text("\(parsed.confidence) CONFIDENCE OCR")
                                    .font(.system(size: 10, weight: .bold))
                            }
                            .foregroundColor(parsed.confidence == "HIGH" ? Color(hex: "059669") : Color(hex: "D97706"))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(parsed.confidence == "HIGH" ? Color(hex: "D1FAE5") : Color(hex: "FEF3C7"))
                            .cornerRadius(12)

                            Spacer()

                            Button(action: onRescan) {
                                HStack(spacing: 4) {
                                    Image(systemName: "camera.arrow.trianglehead.counterclockwise.rotate.90")
                                        .font(.system(size: 11))
                                    Text("Rescan")
                                        .font(.system(size: 11, weight: .bold))
                                }
                                .foregroundColor(Color(hex: "0284C7"))
                            }
                        }
                    }
                    .padding(14)
                    .background(Color.white)
                    .cornerRadius(14)

                    // 2. Extracted & Editable Contact Fields
                    VStack(spacing: 12) {
                        Text("Extracted Contact Information")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(Color(hex: "0F172A"))
                            .frame(maxWidth: .infinity, alignment: .leading)

                        ReviewInputField(label: "Full Name *", text: $name, icon: "person.fill", placeholder: "e.g. John Doe")
                        ReviewInputField(label: "Designation / Role", text: $designation, icon: "briefcase.fill", placeholder: "e.g. Managing Director")
                        ReviewInputField(label: "Company / Organization", text: $company, icon: "building.2.fill", placeholder: "e.g. Apex Enterprises")
                        ReviewInputField(label: "Mobile / Phone *", text: $phone, icon: "phone.fill", placeholder: "e.g. +91 98765 43210")
                        ReviewInputField(label: "Email Address", text: $email, icon: "envelope.fill", placeholder: "e.g. contact@domain.com")
                        ReviewInputField(label: "Website", text: $website, icon: "globe", placeholder: "e.g. www.domain.com")
                        ReviewInputField(label: "City / Region", text: $city, icon: "mappin.and.ellipse", placeholder: "e.g. Tirupati")
                        ReviewInputField(label: "Address", text: $address, icon: "map.fill", placeholder: "Full office or street address")
                        ReviewInputField(label: "Event / Tag", text: $eventTag, icon: "tag.fill", placeholder: "e.g. Zonecon 2027")
                        ReviewInputField(label: "Notes", text: $notes, icon: "note.text", placeholder: "Add notes from conversation")

                        // Option to Save to Device Phonebook
                        Toggle(isOn: $saveToPhoneContacts) {
                            HStack(spacing: 8) {
                                Image(systemName: "person.crop.circle.badge.plus")
                                    .foregroundColor(Color(hex: "0097D8"))
                                Text("Also Save to iPhone Contacts")
                                    .font(.system(size: 13, weight: .semibold))
                                    .foregroundColor(Color(hex: "0F172A"))
                            }
                        }
                        .padding(.top, 4)
                    }
                    .padding(14)
                    .background(Color.white)
                    .cornerRadius(14)

                    // 3. Save Button
                    Button(action: saveContact) {
                        HStack(spacing: 8) {
                            if isSaving {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                            } else {
                                Image(systemName: "checkmark.circle.fill")
                                    .font(.system(size: 15, weight: .bold))
                                Text("Save Contact to Directory")
                                    .font(.system(size: 15, weight: .bold))
                            }
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(Color(hex: "0097D8"))
                        .cornerRadius(12)
                    }
                    .disabled(isSaving || name.trimmingCharacters(in: .whitespaces).isEmpty)
                }
                .padding(16)
            }
            .background(Color(hex: "F8FAFC").ignoresSafeArea())
            .navigationTitle("CardSnap Review")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { dismiss() }
                        .foregroundColor(Color(hex: "64748B"))
                }
            }
        }
    }

    private func saveContact() {
        guard !name.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        isSaving = true

        Task {
            var uploadedPhotoUrl: String? = nil
            // If image is captured, upload asynchronously to server
            if let img = capturedImage,
               let data = img.jpegData(compressionQuality: 0.8) {
                uploadedPhotoUrl = try? await ApiService.shared.uploadImage(
                    token: token,
                    imageData: data,
                    filename: "cardsnap_\(Date().timeIntervalSince1970).jpg"
                )
            }

            let newContact = SaveContactRequest(
                id: "c_snap_\(Int(Date().timeIntervalSince1970 * 1000))",
                name: name.trimmingCharacters(in: .whitespaces),
                company: company.trimmingCharacters(in: .whitespaces),
                designation: designation.trimmingCharacters(in: .whitespaces),
                phone: phone.trimmingCharacters(in: .whitespaces),
                email: email.trimmingCharacters(in: .whitespaces),
                website: website.trimmingCharacters(in: .whitespaces),
                address: address.trimmingCharacters(in: .whitespaces),
                eventTag: eventTag.trimmingCharacters(in: .whitespaces),
                locationName: city.trimmingCharacters(in: .whitespaces),
                city: city.trimmingCharacters(in: .whitespaces),
                notes: notes.trimmingCharacters(in: .whitespaces),
                followUpDate: "",
                photoUrl: uploadedPhotoUrl
            )

            await MainActor.run {
                isSaving = false
                onSave(newContact)
                dismiss()
            }
        }
    }
}

private struct ReviewInputField: View {
    let label: String
    @Binding var text: String
    let icon: String
    let placeholder: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.system(size: 11.5, weight: .bold))
                .foregroundColor(Color(hex: "475569"))

            HStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 12))
                    .foregroundColor(Color(hex: "94A3B8"))
                    .frame(width: 16)

                TextField(placeholder, text: $text)
                    .font(.system(size: 13))
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 8)
            .background(Color(hex: "F8FAFC"))
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
            )
        }
    }
}

