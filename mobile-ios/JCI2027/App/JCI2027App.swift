import SwiftUI

@main
struct JCI2027App: App {
    @State private var token: String? = UserDefaults.standard.string(forKey: "auth_token")
    @State private var userRole: String = UserDefaults.standard.string(forKey: "auth_role") ?? "PRESIDENT"
    @State private var userName: String = UserDefaults.standard.string(forKey: "auth_name") ?? "JC Doraswamy Raju"
    @State private var userChapter: String = UserDefaults.standard.string(forKey: "auth_chapter") ?? "JCI Tirupati Innovations"
    @State private var userId: String = UserDefaults.standard.string(forKey: "auth_user_id") ?? "usr_pres_01"

    var body: some Scene {
        WindowGroup {
            if token == nil {
                LoginView { newToken, role, name, chapter, uid in
                    UserDefaults.standard.set(newToken, forKey: "auth_token")
                    UserDefaults.standard.set(role, forKey: "auth_role")
                    UserDefaults.standard.set(name, forKey: "auth_name")
                    UserDefaults.standard.set(chapter, forKey: "auth_chapter")
                    UserDefaults.standard.set(uid, forKey: "auth_user_id")

                    self.token = newToken
                    self.userRole = role
                    self.userName = name
                    self.userChapter = chapter
                    self.userId = uid
                }
            } else if userRole.uppercased() == "SUPER_ADMIN" || userRole.uppercased() == "ADMIN" {
                AdminAppContainerView(token: token!) {
                    signOut()
                }
            } else if userRole.uppercased() == "VENDOR" {
                VendorAppContainerView {
                    signOut()
                }
            } else {
                MainPresidentContainerView(
                    token: token,
                    presidentName: userName,
                    chapterName: userChapter,
                    userId: userId,
                    onSignOut: { signOut() }
                )
            }
        }
    }

    private func signOut() {
        UserDefaults.standard.removeObject(forKey: "auth_token")
        UserDefaults.standard.removeObject(forKey: "auth_role")
        UserDefaults.standard.removeObject(forKey: "auth_name")
        UserDefaults.standard.removeObject(forKey: "auth_chapter")
        UserDefaults.standard.removeObject(forKey: "auth_user_id")
        self.token = nil
    }
}

struct MainPresidentContainerView: View {
    let token: String?
    let presidentName: String
    let chapterName: String
    let userId: String
    let onSignOut: () -> Void

    @State private var selectedTab = 0
    @State private var activeModalRoute: String? = nil
    @State private var targetContactId: String? = nil
    @State private var showExchangeQrModal = false
    @State private var showNotificationCenter = false

    var body: some View {
        TabView(selection: $selectedTab) {
            // Tab 1: Dashboard
            NavigationStack {
                DashboardOverviewView(
                    presidentName: presidentName,
                    chapterName: chapterName,
                    onNavigateTab: { route in
                        handleSubRoute(route)
                    },
                    onOpenQrExchange: {
                        showExchangeQrModal = true
                    },
                    onOpenNotifications: {
                        showNotificationCenter = true
                    }
                )
                .navigationTitle("LEAD360")
                .navigationBarTitleDisplayMode(.inline)
            }
            .tabItem {
                Label("Home", systemImage: "house.fill")
            }
            .tag(0)

            // Tab 2: Projects
            NavigationStack {
                ProjectsAndTasksView(chapterName: chapterName) {
                    selectedTab = 0
                }
                .navigationTitle("Projects")
                .navigationBarTitleDisplayMode(.inline)
            }
            .tabItem {
                Label("Projects", systemImage: "checklist")
            }
            .tag(1)

            // Tab 3: Members
            NavigationStack {
                MembersAndNetworkingView(
                    chapterName: chapterName,
                    token: token,
                    onBack: { selectedTab = 0 },
                    onNavigateToScanCard: { selectedTab = 3 }
                )
                .navigationTitle("Members")
                .navigationBarTitleDisplayMode(.inline)
            }
            .tabItem {
                Label("Members", systemImage: "person.3.fill")
            }
            .tag(2)

            // Tab 4: Scan Card
            NavigationStack {
                ScanCardModuleView(
                    chapterName: chapterName,
                    token: token,
                    initialTargetContactId: targetContactId,
                    onBack: { selectedTab = 0 }
                )
                .navigationTitle("Scan Card")
                .navigationBarTitleDisplayMode(.inline)
            }
            .tabItem {
                Label("Scan Card", systemImage: "qrcode.viewfinder")
            }
            .tag(3)

            // Tab 5: Flyer Studio
            NavigationStack {
                PresidentFlyerStudioView(
                    token: token ?? "",
                    presidentName: presidentName,
                    chapterName: chapterName
                )
                .navigationTitle("Flyers")
                .navigationBarTitleDisplayMode(.inline)
            }
            .tabItem {
                Label("Flyers", systemImage: "sparkles")
            }
            .tag(4)

            // Tab 6: Store
            NavigationStack {
                VendorStoreView(
                    chapterName: chapterName,
                    token: token,
                    onBack: { selectedTab = 0 }
                )
                .navigationTitle("Store")
                .navigationBarTitleDisplayMode(.inline)
            }
            .tabItem {
                Label("Store", systemImage: "cart.fill")
            }
            .tag(5)
        }
        .tint(Color.jciRoyalBlue)
        .sheet(item: $activeModalRoute) { route in
            switch route {
            case "website_portal":
                ChapterWebsiteBuilderView(chapterName: chapterName, presidentName: presidentName) {
                    activeModalRoute = nil
                }
            case "resources":
                JciResourcesView(chapterName: chapterName) {
                    activeModalRoute = nil
                }
            case "calendar":
                InteractiveCalendarView {
                    activeModalRoute = nil
                    selectedTab = 4
                }
            case "lgb":
                LgbAndPstManagementView(token: token ?? "", chapterName: chapterName, presidentName: presidentName)
            case "frames":
                FramesView()
            case "profile":
                ProfileView(name: presidentName, chapterName: chapterName, role: "PRESIDENT", onLogout: onSignOut)
            default:
                EmptyView()
            }
        }
        .sheet(isPresented: $showExchangeQrModal) {
            NavigationStack {
                VStack(spacing: 24) {
                    VStack(spacing: 6) {
                        Text("Reciprocal Contact Exchange")
                            .font(.system(size: 20, weight: .bold))
                        Text("Let visitors scan this QR to instantly exchange contacts with you.")
                            .font(.system(size: 13))
                            .foregroundColor(.jciTextMuted)
                            .multilineTextAlignment(.center)
                    }

                    if let qrImg = QrCodeGenerator.generateQrImage(from: QrCodeGenerator.buildExchangeUrl(leaderId: userId)) {
                        Image(uiImage: qrImg)
                            .resizable()
                            .interpolation(.none)
                            .scaledToFit()
                            .frame(width: 260, height: 260)
                            .padding(16)
                            .background(Color.white)
                            .cornerRadius(20)
                            .shadow(color: Color.black.opacity(0.1), radius: 10, x: 0, y: 5)
                    }

                    VStack(spacing: 4) {
                        Text(presidentName)
                            .font(.system(size: 16, weight: .bold))
                        Text("President 2027 • \(chapterName)")
                            .font(.system(size: 13))
                            .foregroundColor(.jciRoyalBlue)
                    }

                    Spacer()
                }
                .padding(24)
                .navigationTitle("Leader's Digital QR")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Done") { showExchangeQrModal = false }
                    }
                }
            }
        }
        .sheet(isPresented: $showNotificationCenter) {
            NotificationCenterSheet(token: token) { contactId in
                if let cid = contactId {
                    self.targetContactId = cid
                    self.selectedTab = 3
                }
            }
        }
    }

    private func handleSubRoute(_ route: String) {
        switch route {
        case "projects": selectedTab = 1
        case "members": selectedTab = 2
        case "scan_card": selectedTab = 3
        case "flyers": selectedTab = 4
        case "store": selectedTab = 5
        default:
            activeModalRoute = route
        }
    }
}

extension String: @retroactive Identifiable {
    public var id: String { self }
}
