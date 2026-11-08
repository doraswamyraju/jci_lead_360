import SwiftUI

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
                    ProgressView("Loading Alerts...")
                } else if notifications.isEmpty {
                    VStack(spacing: 8) {
                        Image(systemName: "bell.slash.fill")
                            .font(.system(size: 36))
                            .foregroundColor(.jciTextMuted)
                        Text("No Recent Notifications")
                            .font(.system(size: 16, weight: .bold))
                        Text("New reciprocal contact exchanges will pop up here.")
                            .font(.system(size: 12))
                            .foregroundColor(.jciTextMuted)
                    }
                } else {
                    List {
                        ForEach(notifications) { item in
                            Button(action: {
                                onNavigateToContact(item.contactId)
                                dismiss()
                            }) {
                                VStack(alignment: .leading, spacing: 6) {
                                    Text(item.title)
                                        .font(.system(size: 15, weight: .bold))
                                        .foregroundColor(.jciTextDark)
                                    Text(item.message)
                                        .font(.system(size: 13))
                                        .foregroundColor(.jciTextMuted)
                                    if let date = item.createdAt {
                                        Text(date)
                                            .font(.system(size: 10))
                                            .foregroundColor(.jciSkyBlue)
                                    }
                                }
                                .padding(.vertical, 4)
                            }
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
                    Button("Mark All Read") {
                        markRead()
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
                let notifs = try await ApiService.shared.getNotifications(token: token)
                await MainActor.run {
                    self.notifications = notifs
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.isLoading = false
                }
            }
        }
    }

    private func markRead() {
        guard let token = token else { return }
        Task {
            _ = try? await ApiService.shared.markAllNotificationsRead(token: token)
            loadNotifications()
        }
    }
}
