import SwiftUI

/// Replicates NotificationCenterBottomSheet.kt from Android app
/// Displays real-time alerts, chapter updates, and reciprocal contact exchanges.
struct NotificationCenterSheet: View {
    let token: String?
    let onNavigateToContact: (String?) -> Void
    @Environment(\.dismiss) var dismiss

    @State private var notifications: [NotificationItem] = []
    @State private var isLoading = true

    var body: some View {
        NavigationStack {
            Group {
                if isLoading {
                    VStack(spacing: 12) {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: Color.jciSkyBlue))
                        Text("Loading alerts & exchanges...")
                            .font(.system(size: 13))
                            .foregroundColor(.jciTextMuted)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if notifications.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "bell.slash.fill")
                            .font(.system(size: 42))
                            .foregroundColor(.jciTextMuted.opacity(0.6))
                        Text("No Recent Notifications")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.jciTextDark)
                        Text("Reciprocal contact exchanges and chapter updates will pop up here.")
                            .font(.system(size: 13))
                            .foregroundColor(.jciTextMuted)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 32)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        ForEach(notifications) { item in
                            Button(action: {
                                if let cid = item.contactId {
                                    onNavigateToContact(cid)
                                    dismiss()
                                }
                            }) {
                                HStack(alignment: .top, spacing: 12) {
                                    // Icon badge based on notification type
                                    ZStack {
                                        Circle()
                                            .fill(item.type == "contact_exchange" ? Color(hex: "E0F2FE") : Color(hex: "F1F5F9"))
                                            .frame(width: 40, height: 40)

                                        if item.type == "contact_exchange" {
                                            Image(systemName: "person.crop.circle.badge.checkmark")
                                                .font(.system(size: 18))
                                                .foregroundColor(Color(hex: "0284C7"))
                                        } else {
                                            Text("🔔")
                                                .font(.system(size: 18))
                                        }
                                    }

                                    VStack(alignment: .leading, spacing: 5) {
                                        HStack {
                                            Text(item.title)
                                                .font(.system(size: 14, weight: .bold))
                                                .foregroundColor(.jciTextDark)
                                                .lineLimit(1)

                                            Spacer()

                                            if item.type == "contact_exchange" {
                                                Text("EXCHANGE")
                                                    .font(.system(size: 9, weight: .black))
                                                    .foregroundColor(Color(hex: "0369A1"))
                                                    .padding(.horizontal, 6)
                                                    .padding(.vertical, 2)
                                                    .background(Color(hex: "E0F2FE"))
                                                    .cornerRadius(6)
                                            }
                                        }

                                        Text(item.message)
                                            .font(.system(size: 13))
                                            .foregroundColor(.jciTextMuted)
                                            .fixedSize(horizontal: false, vertical: true)

                                        HStack {
                                            if let date = item.createdAt {
                                                Text(date)
                                                    .font(.system(size: 11))
                                                    .foregroundColor(Color(hex: "94A3B8"))
                                            }

                                            Spacer()

                                            if item.contactId != nil {
                                                Text("View Contact →")
                                                    .font(.system(size: 11, weight: .bold))
                                                    .foregroundColor(Color.jciSkyBlue)
                                            }
                                        }
                                        .padding(.top, 2)
                                    }
                                }
                                .padding(.vertical, 6)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("Notification Center")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Menu {
                        Button(action: {
                            PushNotificationManager.shared.triggerTestAlert()
                            loadNotifications()
                        }) {
                            Label("Send Test Push Alert", systemImage: "bell.badge")
                        }

                        Button(action: { markRead() }) {
                            Label("Mark All Read", systemImage: "checkmark.circle")
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                            .font(.system(size: 18))
                    }
                }
            }
        }
        .onAppear {
            loadNotifications()
        }
    }

    private func loadNotifications() {
        guard let token = token else {
            isLoading = false
            return
        }
        isLoading = true
        Task {
            do {
                // 1. Fetch live backend notifications
                var loadedNotifs = (try? await ApiService.shared.getNotifications(token: token)) ?? []

                // 2. Also fetch contacts to ensure any recent QR reciprocal exchanges generate an instant notification card (Parity with NotificationCenterBottomSheet.kt lines 49-78)
                let contacts = (try? await ApiService.shared.getContacts(token: token)) ?? []
                let exchangeContacts = contacts.filter {
                    $0.eventTag.localizedCaseInsensitiveContains("Exchange") ||
                    $0.id.hasPrefix("c_recip_") ||
                    $0.notes.localizedCaseInsensitiveContains("Exchange")
                }

                let syntheticNotifs = exchangeContacts.map { c in
                    let role = c.designation.isEmpty ? "Visitor" : c.designation
                    let companyOrCity = !c.company.isEmpty ? c.company : (!c.city.isEmpty ? c.city : "JCI Event")
                    return NotificationItem(
                        id: "notif_contact_\(c.id)",
                        title: "📇 Exchanged: \(c.name)",
                        message: "\(c.name) (\(role) at \(companyOrCity)) scanned your QR code and exchanged contacts.",
                        type: "contact_exchange",
                        read: false,
                        contactId: c.id,
                        createdAt: !c.followUpDate.isEmpty ? c.followUpDate : "Reciprocal Exchange"
                    )
                }

                // Combine without duplicates
                let existingContactIds = Set(loadedNotifs.compactMap { $0.contactId })
                var combined = loadedNotifs
                for syn in syntheticNotifs {
                    if let cid = syn.contactId, !existingContactIds.contains(cid) {
                        combined.insert(syn, at: 0)
                    }
                }

                await MainActor.run {
                    self.notifications = combined
                    self.isLoading = false
                }
            }
        }
    }

    private func markRead() {
        guard let token = token else { return }
        Task {
            _ = try? await ApiService.shared.markAllNotificationsRead(token: token)
            await MainActor.run {
                PushNotificationManager.shared.unreadCount = 0
            }
            loadNotifications()
        }
    }
}
