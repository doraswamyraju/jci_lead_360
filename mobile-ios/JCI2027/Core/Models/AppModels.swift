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
struct FlyerTemplate: Codable, Identifiable {
    let id: String
    let name: String
    let category: String
    let assetPath: String
    let primaryColorHex: String?
}

struct TemplatesResponse: Codable {
    let success: Bool
    let templates: [FlyerTemplate]?
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
