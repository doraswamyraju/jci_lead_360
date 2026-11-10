import Foundation
import UIKit
import UserNotifications

/// Manages Push Notifications and Reciprocal Contact Exchange alerts for JCI LEAD360 iOS
/// Replicates Lead360FirebaseMessagingService and MainActivity FCM registration
@MainActor
class PushNotificationManager: NSObject, ObservableObject, UNUserNotificationCenterDelegate {
    static let shared = PushNotificationManager()

    @Published var isPermissionGranted = false
    @Published var deviceToken: String? = nil
    @Published var unreadCount: Int = 0
    @Published var totalExchangesCount: Int = 0

    /// Deep link callback when user taps an exchange alert notification
    var onNavigateToContact: ((String) -> Void)?

    private var pollTimer: Timer?
    private var lastSeenNotificationIds = Set<String>()
    private var lastSeenContactIds = Set<String>()
    private var activeAuthToken: String? = nil

    private override init() {
        super.init()
        UNUserNotificationCenter.current().delegate = self
        if let stored = UserDefaults.standard.string(forKey: "fcm_device_token") {
            self.deviceToken = stored
        } else {
            // Generate stable simulator device token
            let simToken = "ios_dev_\(UIDevice.current.identifierForVendor?.uuidString.prefix(16) ?? UUID().uuidString.prefix(16))"
            self.deviceToken = simToken
            UserDefaults.standard.set(simToken, forKey: "fcm_device_token")
        }
    }

    /// Requests notification permissions on launch (Parity with Android POST_NOTIFICATIONS launcher)
    func requestAuthorization(authToken: String? = nil) {
        if let token = authToken {
            self.activeAuthToken = token
        }
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { [weak self] granted, error in
            DispatchQueue.main.async {
                self?.isPermissionGranted = granted
                if granted {
                    UIApplication.shared.registerForRemoteNotifications()
                    print("[PushNotifications] Authorization granted")
                } else if let error = error {
                    print("[PushNotifications] Authorization error: \(error.localizedDescription)")
                }

                if let token = self?.activeAuthToken, let dToken = self?.deviceToken {
                    self?.syncDeviceTokenWithBackend(authToken: token, deviceToken: dToken)
                }
                self?.startNotificationSync()
            }
        }
    }

    /// Called by AppDelegate when APNs device registration succeeds
    func handleDeviceToken(_ tokenData: Data) {
        let tokenString = tokenData.map { String(format: "%02.2hhx", $0) }.joined()
        self.deviceToken = tokenString
        UserDefaults.standard.set(tokenString, forKey: "fcm_device_token")
        UserDefaults.standard.set(tokenString, forKey: "apns_device_token")

        if let authToken = activeAuthToken ?? UserDefaults.standard.string(forKey: "auth_token") {
            syncDeviceTokenWithBackend(authToken: authToken, deviceToken: tokenString)
        }
    }

    /// Syncs device token to backend POST /api/users/fcm-token
    func syncDeviceTokenWithBackend(authToken: String, deviceToken: String) {
        Task {
            do {
                try await ApiService.shared.updateFcmToken(token: authToken, fcmToken: deviceToken)
                print("[PushNotifications] Device token successfully registered with backend: \(deviceToken)")
            } catch {
                print("[PushNotifications] Device token sync note: \(error.localizedDescription)")
            }
        }
    }

    /// Start polling for real-time contact exchange alerts (every 5 seconds, matching Android parity)
    func startNotificationSync(token: String? = nil) {
        if let token = token {
            self.activeAuthToken = token
        }
        pollTimer?.invalidate()
        fetchNotifications()

        pollTimer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { [weak self] _ in
            Task { @MainActor [weak self] in
                self?.fetchNotifications()
            }
        }
    }

    func stopNotificationSync() {
        pollTimer?.invalidate()
        pollTimer = nil
    }

    func fetchNotifications() {
        guard let token = activeAuthToken ?? UserDefaults.standard.string(forKey: "auth_token") else { return }

        Task {
            do {
                // 1. Fetch live backend notifications
                let notifs = try await ApiService.shared.getNotifications(token: token)

                // 2. Also fetch contacts to ensure any recent QR reciprocal exchanges generate an instant notification banner
                let contacts = (try? await ApiService.shared.getContacts(token: token)) ?? []

                await MainActor.run {
                    let exchangeContacts = contacts.filter {
                        $0.eventTag.localizedCaseInsensitiveContains("Exchange") ||
                        $0.id.hasPrefix("c_recip_") ||
                        $0.notes.localizedCaseInsensitiveContains("Exchange")
                    }
                    self.totalExchangesCount = exchangeContacts.count

                    let unread = notifs.filter { $0.read != true }
                    self.unreadCount = unread.count

                    // Initial seed check for contacts
                    if self.lastSeenContactIds.isEmpty {
                        self.lastSeenContactIds = Set(contacts.map { $0.id })
                    } else {
                        // Check for new reciprocal contacts
                        for c in exchangeContacts {
                            if !self.lastSeenContactIds.contains(c.id) {
                                self.lastSeenContactIds.insert(c.id)
                                self.unreadCount += 1

                                let notif = NotificationItem(
                                    id: "notif_contact_\(c.id)",
                                    title: "📇 New Contact Exchanged: \(c.name)",
                                    message: "\(c.name) (\(c.designation.isEmpty ? "Visitor" : c.designation) at \(c.company.isEmpty ? (c.city.isEmpty ? "JCI Event" : c.city) : c.company)) scanned your QR and exchanged contacts.",
                                    type: "contact_exchange",
                                    read: false,
                                    contactId: c.id,
                                    createdAt: "Just now"
                                )
                                self.triggerLocalAlert(for: notif)
                            }
                        }
                    }

                    // Check for newly arrived reciprocal contact exchange notifications
                    for notif in notifs {
                        if !self.lastSeenNotificationIds.contains(notif.id) {
                            let isInitialSync = self.lastSeenNotificationIds.isEmpty
                            self.lastSeenNotificationIds.insert(notif.id)

                            if !isInitialSync && (notif.read != true) {
                                self.triggerLocalAlert(for: notif)
                            }
                        }
                    }
                }
            } catch {}
        }
    }

    /// Displays native iOS notification banner with sound and deep-link payload
    func triggerLocalAlert(for notification: NotificationItem) {
        let content = UNMutableNotificationContent()
        content.title = notification.title.isEmpty ? "📇 New Contact Exchanged!" : notification.title
        content.body = notification.message
        content.sound = .default
        content.badge = NSNumber(value: unreadCount)
        if let cid = notification.contactId {
            content.userInfo = ["contactId": cid, "target_contact_id": cid, "openScreen": "scan_card"]
        }

        let request = UNNotificationRequest(
            identifier: "exchange_\(notification.id)_\(Date().timeIntervalSince1970)",
            content: content,
            trigger: nil // immediate delivery
        )
        let bannerTitle = content.title
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("[PushNotifications] Error showing alert: \(error.localizedDescription)")
            } else {
                print("[PushNotifications] Notification banner displayed: \(bannerTitle)")
            }
        }
    }

    /// Triggers a test push notification alert for testing & verification
    func triggerTestAlert() {
        let testNames = [
            ("JC Suresh Babu", "VP Growth", "Apollo Health"),
            ("Rohan Varma", "Founder", "Innovate Labs"),
            ("Dr. Priya Sharma", "Regional Director", "Rotary Tirupati"),
            ("Kavita Menon", "Operations Head", "Tech Solutions")
        ]
        let sample = testNames.randomElement()!
        let fakeId = "c_recip_test_\(Int(Date().timeIntervalSince1970))"

        self.unreadCount += 1
        let testNotif = NotificationItem(
            id: fakeId,
            title: "📇 Exchanged: \(sample.0)",
            message: "\(sample.0) (\(sample.1) at \(sample.2)) scanned your QR code and exchanged contacts.",
            type: "contact_exchange",
            read: false,
            contactId: fakeId,
            createdAt: "Just now"
        )
        self.triggerLocalAlert(for: testNotif)
    }

    // MARK: - UNUserNotificationCenterDelegate
    nonisolated func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Show banner and play sound even when app is open in foreground (Parity with Android)
        completionHandler([.banner, .sound, .badge, .list])
    }

    nonisolated func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        let contactId = (userInfo["contactId"] as? String) ?? (userInfo["target_contact_id"] as? String)

        if let cid = contactId, !cid.isEmpty {
            DispatchQueue.main.async {
                PushNotificationManager.shared.onNavigateToContact?(cid)
            }
        }
        completionHandler()
    }
}
