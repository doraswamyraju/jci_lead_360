import Foundation

// MARK: - Auth & User Models
struct LoginRequest: Codable {
    let email: String
    let password: String
}

struct LoginResponse: Codable {
    let success: BooleanLiteralType?
    let message: String?
    let token: String?
    let user: UserDto?
    
    // Fallback decoding
    enum CodingKeys: String, CodingKey {
        case success, message, token, user
    }
}

struct UserDto: Codable, Identifiable {
    let id: String?
    let name: String?
    let email: String?
    let role: String?
    let chapterName: String?
    let phone: String?
    let photoUrl: String?
    let zone: String?
    let businessName: String?
    let gstin: String?
    let serviceAreas: String?
    let logoUrl: String?
}

struct GenericSuccessResponse: Codable {
    let success: Bool
    let message: String?
}

// MARK: - Contact Models
struct SaveContactRequest: Codable, Identifiable {
    let id: String
    var name: String
    var company: String
    var designation: String
    var phone: String
    var email: String
    var website: String
    var address: String
    var eventTag: String
    var locationName: String
    var city: String
    var notes: String
    var followUpDate: String
    var photoUrl: String?

    enum CodingKeys: String, CodingKey {
        case id, name, company, designation, phone, email, website, address, eventTag, locationName, city, notes, followUpDate, photoUrl
    }

    init(
        id: String,
        name: String,
        company: String = "",
        designation: String = "",
        phone: String = "",
        email: String = "",
        website: String = "",
        address: String = "",
        eventTag: String = "QR Reciprocal Exchange",
        locationName: String = "",
        city: String = "",
        notes: String = "",
        followUpDate: String = "",
        photoUrl: String? = nil
    ) {
        self.id = id
        self.name = name
        self.company = company
        self.designation = designation
        self.phone = phone
        self.email = email
        self.website = website
        self.address = address
        self.eventTag = eventTag
        self.locationName = locationName
        self.city = city
        self.notes = notes
        self.followUpDate = followUpDate
        self.photoUrl = photoUrl
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decodeIfPresent(String.self, forKey: .id) ?? "c_\(UUID().uuidString)"
        name = try c.decodeIfPresent(String.self, forKey: .name) ?? ""
        company = try c.decodeIfPresent(String.self, forKey: .company) ?? ""
        designation = try c.decodeIfPresent(String.self, forKey: .designation) ?? ""
        phone = try c.decodeIfPresent(String.self, forKey: .phone) ?? ""
        email = try c.decodeIfPresent(String.self, forKey: .email) ?? ""
        website = try c.decodeIfPresent(String.self, forKey: .website) ?? ""
        address = try c.decodeIfPresent(String.self, forKey: .address) ?? ""
        eventTag = try c.decodeIfPresent(String.self, forKey: .eventTag) ?? "QR Reciprocal Exchange"
        locationName = try c.decodeIfPresent(String.self, forKey: .locationName) ?? ""
        city = try c.decodeIfPresent(String.self, forKey: .city) ?? ""
        notes = try c.decodeIfPresent(String.self, forKey: .notes) ?? ""
        followUpDate = try c.decodeIfPresent(String.self, forKey: .followUpDate) ?? ""
        photoUrl = try c.decodeIfPresent(String.self, forKey: .photoUrl)
    }
}

struct GetContactsResponse: Codable {
    let success: Bool
    let contacts: [SaveContactRequest]?
}

struct SaveContactResponse: Codable {
    let success: Bool
    let message: String?
    let contact: SaveContactRequest?
}

// MARK: - Member Models
struct MemberDto: Codable, Identifiable {
    let id: String
    var name: String
    var phone: String
    var email: String?
    var role: String?
    var membershipType: String?
    var joinYear: String?
    var bloodGroup: String?
    var dob: String?
    var anniversaryDate: String?
    var chapterName: String?
    var chapterId: String?
    var zone: String?
    var businessName: String?
    var designation: String?
    var businessCategory: String?
    var businessPhone: String?
    var businessAddress: String?
    var city: String?
    var website: String?
    var services: String?
    var status: String?
    var photoUrl: String?
    var lat: Double?
    var lng: Double?
    var spouseName: String?
    var daysUntil: Int?
    var celebrationType: String?
}

struct CelebrationsData: Codable {
    let todayCount: Int
    let todayBirthdays: [MemberDto]
    let todayAnniversaries: [MemberDto]
    let upcomingBirthdays: [MemberDto]
    let upcomingAnniversaries: [MemberDto]
}

struct CelebrationsResponse: Codable {
    let success: Bool
    let data: CelebrationsData
}

struct MembersResponse: Codable {
    let success: Bool
    let count: Int
    let members: [MemberDto]
}

// MARK: - Notification Models
struct NotificationItem: Codable, Identifiable {
    let id: String
    let title: String
    let message: String
    let type: String?
    let read: Bool?
    let contactId: String?
    let createdAt: String?
}

struct NotificationsResponse: Codable {
    let success: Bool
    let notifications: [NotificationItem]
}

// MARK: - Template Models
struct TemplateDimensions: Codable {
    let width: Double?
    let height: Double?
}

struct TemplateLayer: Codable, Identifiable {
    var id: String { "\(type ?? "layer")_\(Int(x ?? 0))_\(Int(y ?? 0))" }
    let type: String?
    let label: String?
    let x: Double?
    let y: Double?
    let width: Double?
    let height: Double?
    let color: String?
    let fontSize: Double?
    let align: String?
    let bold: Bool?
    let shape: String?
    let borderWidth: Double?
    let borderColor: String?
}

struct FlyerTemplate: Codable, Identifiable {
    let id: String
    let title: String
    let category: String
    let baseImageUrl: String
    let dimensions: TemplateDimensions?
    let layers: [TemplateLayer]?
    let createdById: String?
    let createdByName: String?
    let createdAt: String?
    
    // Compatibility helpers
    var name: String { title }
    var assetPath: String { baseImageUrl }
}

struct TemplatesResponse: Codable {
    let success: Bool
    let count: Int?
    let templates: [FlyerTemplate]?
}

// MARK: - Calendar Models
struct CalendarEventDto: Codable, Identifiable {
    let id: String
    let title: String
    let category: String?
    let targetDate: String
    let mandatory: Bool?
    let description: String?
    let templateId: String?
}

struct CalendarEventsResponse: Codable {
    let success: Bool
    let count: Int?
    let events: [CalendarEventDto]?
}

// MARK: - Leader Profile Models
struct LeaderDto: Codable, Identifiable {
    let id: String
    let name: String?
    let role: String?
    let chapter: String?
    let zone: String?
    let phone: String?
    let email: String?
    let website: String?
    let photoUrl: String?
}

struct LeaderProfileResponse: Codable {
    let success: Bool
    let leader: LeaderDto?
}

// MARK: - Vendor & Catalog Models
struct CatalogItem: Codable, Identifiable {
    let id: String
    var name: String
    var category: String
    var price: Double
    var unit: String
    var leadTime: String
    var description: String
    var productCountInCat: Int?
}

struct CatalogResponse: Codable {
    let success: Bool
    let items: [CatalogItem]
}

struct PlaceOrderRequest: Codable {
    let chapterName: String
    let customerName: String
    let phone: String
    let email: String
    let deliveryAddress: String
    let notes: String
    let items: [OrderItemRequest]
    let totalAmount: Double
}

struct OrderItemRequest: Codable {
    let itemId: String
    let name: String
    let quantity: Int
    let price: Double
}

