import Foundation

class ApiService {
    static let shared = ApiService()
    let baseUrl = "https://lead.sriddha.com"

    private init() {}

    private func createRequest(path: String, method: String = "GET", token: String? = nil, body: Data? = nil) -> URLRequest {
        let url = URL(string: "\(baseUrl)\(path)")!
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let token = token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        request.httpBody = body
        return request
    }

    // MARK: - Auth
    func login(email: String, password: String) async throws -> LoginResponse {
        let reqData = try JSONEncoder().encode(LoginRequest(email: email, password: password))
        let request = createRequest(path: "/api/auth/login", method: "POST", body: reqData)
        let (data, _) = try await URLSession.shared.data(for: request)
        return try JSONDecoder().decode(LoginResponse.self, from: data)
    }

    // MARK: - Contacts (Scan Card)
    func getContacts(token: String) async throws -> [SaveContactRequest] {
        let request = createRequest(path: "/api/contacts", token: token)
        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(GetContactsResponse.self, from: data)
        return response.contacts ?? []
    }

    func saveContact(token: String, contact: SaveContactRequest) async throws -> Bool {
        let reqData = try JSONEncoder().encode(contact)
        let request = createRequest(path: "/api/contacts", method: "POST", token: token, body: reqData)
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 200 || (response as? HTTPURLResponse)?.statusCode == 201
    }

    func deleteContact(token: String, id: String) async throws -> Bool {
        let request = createRequest(path: "/api/contacts/\(id)", method: "DELETE", token: token)
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 200
    }

    // MARK: - Members
    func getMembers(token: String) async throws -> [MemberDto] {
        let request = createRequest(path: "/api/members", token: token)
        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(MembersResponse.self, from: data)
        return response.members
    }

    func saveMember(token: String, member: MemberDto) async throws -> Bool {
        let reqData = try JSONEncoder().encode(member)
        let request = createRequest(path: "/api/members", method: "POST", token: token, body: reqData)
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 200 || (response as? HTTPURLResponse)?.statusCode == 201
    }

    func approveMember(token: String, memberId: String) async throws -> Bool {
        let request = createRequest(path: "/api/members/\(memberId)/approve", method: "POST", token: token)
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 200
    }

    // MARK: - Catalog & Store
    func getCatalog() async throws -> [CatalogItem] {
        let request = createRequest(path: "/api/vendor/catalog")
        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(CatalogResponse.self, from: data)
        return response.items
    }

    func placeOrder(token: String, order: PlaceOrderRequest) async throws -> Bool {
        let reqData = try JSONEncoder().encode(order)
        let request = createRequest(path: "/api/vendor/orders", method: "POST", token: token, body: reqData)
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 200 || (response as? HTTPURLResponse)?.statusCode == 201
    }

    // MARK: - Notifications
    func getNotifications(token: String) async throws -> [NotificationItem] {
        let request = createRequest(path: "/api/vendor/notifications", token: token)
        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(NotificationsResponse.self, from: data)
        return response.notifications
    }

    func markAllNotificationsRead(token: String) async throws -> Bool {
        let request = createRequest(path: "/api/vendor/notifications/read-all", method: "POST", token: token)
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 200
    }

    // MARK: - FCM Device Token Sync
    func updateFcmToken(token: String, fcmToken: String) async throws {
        let reqData = try JSONEncoder().encode(["fcmToken": fcmToken])
        let request = createRequest(path: "/api/users/fcm-token", method: "POST", token: token, body: reqData)
        _ = try await URLSession.shared.data(for: request)
    }
}

