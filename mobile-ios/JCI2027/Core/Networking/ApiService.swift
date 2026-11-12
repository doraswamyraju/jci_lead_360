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

    func getCelebrations(token: String?) async throws -> CelebrationsData {
        let request = createRequest(path: "/api/members/celebrations", token: token)
        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(CelebrationsResponse.self, from: data)
        return response.data
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

    // MARK: - Flyer Templates & Calendar Events (Live Backend Data)
    func getTemplates(token: String) async throws -> [FlyerTemplate] {
        let request = createRequest(path: "/api/templates", token: token)
        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(TemplatesResponse.self, from: data)
        return response.templates ?? []
    }

    func getCalendarEvents(token: String) async throws -> [CalendarEventDto] {
        let request = createRequest(path: "/api/templates/calendar/events", token: token)
        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(CalendarEventsResponse.self, from: data)
        return response.events ?? []
    }

    func getLeaderProfile(userId: String) async throws -> LeaderDto? {
        let request = createRequest(path: "/api/card/profile/\(userId)")
        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(LeaderProfileResponse.self, from: data)
        return response.leader
    }

    // MARK: - FCM Device Token Sync
    func updateFcmToken(token: String, fcmToken: String) async throws {
        let reqData = try JSONEncoder().encode(["fcmToken": fcmToken])
        let request = createRequest(path: "/api/users/fcm-token", method: "POST", token: token, body: reqData)
        _ = try await URLSession.shared.data(for: request)
    }

    // MARK: - Officer & Chapter Profile Sync
    func updateOfficerDetails(token: String, name: String, phone: String, email: String, photoUrl: String? = nil, role: String? = nil) async throws -> Bool {
        var payload: [String: String] = ["name": name, "phone": phone, "email": email]
        if let photoUrl = photoUrl { payload["photoUrl"] = photoUrl }
        if let role = role { payload["role"] = role }
        let reqData = try JSONSerialization.data(withJSONObject: payload)
        let request = createRequest(path: "/api/users/officer-details", method: "POST", token: token, body: reqData)
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 200
    }

    func fetchChapterDetails(token: String) async throws -> ChapterDetails? {
        let request = createRequest(path: "/api/users/chapter-details", method: "GET", token: token)
        let (data, response) = try await URLSession.shared.data(for: request)
        guard (response as? HTTPURLResponse)?.statusCode == 200,
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let success = json["success"] as? Bool, success,
              let chapDict = json["chapter"] as? [String: Any] else {
            return nil
        }
        return ChapterDetails(
            id: chapDict["id"] as? String ?? "",
            name: chapDict["name"] as? String ?? "",
            zone: chapDict["zone"] as? String ?? "",
            phone: chapDict["phone"] as? String ?? "",
            email: chapDict["email"] as? String ?? "",
            logoUrl: chapDict["logoUrl"] as? String
        )
    }

    func updateChapterDetails(token: String, chapterName: String, zone: String, phone: String, email: String, logoUrl: String? = nil) async throws -> Bool {
        var payload: [String: String] = ["chapterName": chapterName, "zone": zone, "phone": phone, "email": email]
        if let logoUrl = logoUrl { payload["logoUrl"] = logoUrl }
        let reqData = try JSONSerialization.data(withJSONObject: payload)
        let request = createRequest(path: "/api/users/chapter-details", method: "POST", token: token, body: reqData)
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 200
    }

    func uploadImage(token: String?, imageData: Data, filename: String = "profile.jpg") async throws -> String? {
        let boundary = "Boundary-\(UUID().uuidString)"
        var request = URLRequest(url: URL(string: "\(baseUrl)/api/upload?type=avatar")!)
        request.httpMethod = "POST"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        if let token = token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        var body = Data()
        body.append("--\(boundary)\r\n".data(using: .utf8)!)
        body.append("Content-Disposition: form-data; name=\"image\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
        body.append("Content-Type: image/jpeg\r\n\r\n".data(using: .utf8)!)
        body.append(imageData)
        body.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)
        request.httpBody = body

        let (data, response) = try await URLSession.shared.data(for: request)
        if (response as? HTTPURLResponse)?.statusCode == 200,
           let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
           let fileUrl = json["fileUrl"] as? String {
            return fileUrl
        }
        return nil
    }
}

