import Foundation
import Contacts
import UIKit

/**
 * ContactManager — Integrates JCI Lead360 Members with iOS Phonebook (Contacts framework)
 * and generates standard vCards for Contact Exchange.
 */
class ContactManager {
    static let shared = ContactManager()
    private let contactStore = CNContactStore()

    private init() {}

    /**
     * Saves a JCI Member's complete profile directly into the user's native iOS Phonebook.
     */
    func saveMemberToPhonebook(member: MemberDto, chapterName: String) async throws -> Bool {
        // 1. Request Contacts Permission
        let isAuthorized: Bool
        if #available(iOS 16.0, *) {
            isAuthorized = try await contactStore.requestAccess(for: .contacts)
        } else {
            isAuthorized = try await withCheckedThrowingContinuation { continuation in
                contactStore.requestAccess(for: .contacts) { granted, error in
                    if let err = error {
                        continuation.resume(throwing: err)
                    } else {
                        continuation.resume(returning: granted)
                    }
                }
            }
        }

        guard isAuthorized else {
            throw NSError(
                domain: "ContactManager",
                code: 403,
                userInfo: [NSLocalizedDescriptionKey: "Contacts permission was not granted. Please enable Contacts in Settings > JCI2027."]
            )
        }

        // 2. Build Native CNMutableContact
        let contact = CNMutableContact()

        // Name parsing
        let components = member.name.trimmingCharacters(in: .whitespaces).components(separatedBy: " ")
        if components.count > 1 {
            contact.givenName = components.dropLast().joined(separator: " ")
            contact.familyName = components.last ?? ""
        } else {
            contact.givenName = member.name
        }

        // Phone Numbers
        let cleanPhone = member.phone.trimmingCharacters(in: .whitespaces)
        if !cleanPhone.isEmpty {
            contact.phoneNumbers = [
                CNLabeledValue(label: CNLabelPhoneNumberMobile, value: CNPhoneNumber(stringValue: cleanPhone))
            ]
        }

        // Email Addresses
        if let email = member.email?.trimmingCharacters(in: .whitespaces), !email.isEmpty {
            contact.emailAddresses = [
                CNLabeledValue(label: CNLabelWork, value: email as NSString)
            ]
        }

        // Organization & Job Title
        let orgName = (member.businessName?.isEmpty == false) ? member.businessName! : (member.chapterName ?? chapterName)
        contact.organizationName = orgName

        if let desig = member.designation, !desig.isEmpty {
            contact.jobTitle = desig
        } else if let role = member.role, !role.isEmpty {
            contact.jobTitle = role
        }

        // Postal / City Address
        if let city = member.city, !city.isEmpty {
            let address = CNMutablePostalAddress()
            address.city = city
            address.country = "India"
            contact.postalAddresses = [
                CNLabeledValue(label: CNLabelWork, value: address)
            ]
        }

        // Birthday Components if present (YYYY-MM-DD)
        if let dob = member.dob, dob.count >= 10 {
            let parts = dob.components(separatedBy: "-")
            if parts.count == 3, let year = Int(parts[0]), let month = Int(parts[1]), let day = Int(parts[2]) {
                var dc = DateComponents()
                dc.year = year
                dc.month = month
                dc.day = day
                contact.birthday = dc
            }
        }

        // Note with JCI Affiliation & Blood Group
        var noteLines: [String] = [
            "JCI LEAD360 Verified Chapter Member",
            "Chapter: \(member.chapterName ?? chapterName)",
            "Role: \(member.role ?? "Active Member")"
        ]
        if let bg = member.bloodGroup, !bg.isEmpty {
            noteLines.append("Blood Group: \(bg)")
        }
        if let cat = member.businessCategory, !cat.isEmpty {
            noteLines.append("Industry Category: \(cat)")
        }
        contact.note = noteLines.joined(separator: "\n")

        // Avatar Image if available
        if let photoUrl = member.photoUrl, !photoUrl.isEmpty {
            if let img = await iOSFlyerRenderer.downloadImage(from: photoUrl),
               let jpegData = img.jpegData(compressionQuality: 0.85) {
                contact.imageData = jpegData
            }
        }

        // 3. Save to Store
        let saveRequest = CNSaveRequest()
        saveRequest.add(contact, toContainerWithIdentifier: nil)
        try contactStore.execute(saveRequest)
        return true
    }

    /**
     * Generates a vCard 3.0 string representation for seamless contact exchange.
     */
    func generateVCardString(member: MemberDto, chapterName: String) -> String {
        var vcard = """
        BEGIN:VCARD
        VERSION:3.0
        FN:\(member.name)
        N:\(member.name);;;;
        TEL;TYPE=CELL:\(member.phone)
        """
        if let email = member.email, !email.isEmpty {
            vcard += "\nEMAIL;TYPE=WORK:\(email)"
        }
        if let biz = member.businessName, !biz.isEmpty {
            vcard += "\nORG:\(biz)"
        } else {
            vcard += "\nORG:\(member.chapterName ?? chapterName)"
        }
        if let desig = member.designation, !desig.isEmpty {
            vcard += "\nTITLE:\(desig)"
        } else if let role = member.role, !role.isEmpty {
            vcard += "\nTITLE:\(role)"
        }
        if let city = member.city, !city.isEmpty {
            vcard += "\nADR;TYPE=WORK:;;;\(city);;;India"
        }
        vcard += "\nNOTE:JCI LEAD360 Member • \(member.chapterName ?? chapterName)"
        vcard += "\nEND:VCARD"
        return vcard
    }
}

