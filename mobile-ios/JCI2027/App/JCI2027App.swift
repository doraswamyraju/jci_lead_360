import SwiftUI
import UserNotifications

// MARK: - App Delegate for Push Notifications (Parity with Lead360FirebaseMessagingService)
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Task { @MainActor in
            PushNotificationManager.shared.handleDeviceToken(deviceToken)
        }
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        print("Remote notification registration note: \(error.localizedDescription)")
    }
}

@main
struct JCI2027App: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    @State private var token: String? = UserDefaults.standard.string(forKey: "auth_token")
    @State private var userRole: String = UserDefaults.standard.string(forKey: "auth_role") ?? "PRESIDENT"
    @State private var userName: String = UserDefaults.standard.string(forKey: "auth_name") ?? "JC Doraswamy Raju"
    @State private var userChapter: String = UserDefaults.standard.string(forKey: "auth_chapter") ?? "JCI Tirupati Innovations"
    @State private var userId: String = UserDefaults.standard.string(forKey: "auth_user_id") ?? "usr_pres_01"
    @State private var userPhotoUrl: String? = UserDefaults.standard.string(forKey: "auth_photo_url")

    var body: some Scene {
        WindowGroup {
            if token == nil {
                LoginView { newToken, role, name, chapter, uid, photoUrl in
                    UserDefaults.standard.set(newToken, forKey: "auth_token")
                    UserDefaults.standard.set(role, forKey: "auth_role")
                    UserDefaults.standard.set(name, forKey: "auth_name")
                    UserDefaults.standard.set(chapter, forKey: "auth_chapter")
                    UserDefaults.standard.set(uid, forKey: "auth_user_id")
                    if let photoUrl = photoUrl {
                        UserDefaults.standard.set(photoUrl, forKey: "auth_photo_url")
                    }

                    self.token = newToken
                    self.userRole = role
                    self.userName = name
                    self.userChapter = chapter
                    self.userId = uid
                    self.userPhotoUrl = photoUrl

                    // Immediately register push notifications upon successful login
                    PushNotificationManager.shared.requestAuthorization(authToken: newToken)
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
                    initialPhotoUrl: userPhotoUrl,
                    onSignOut: { signOut() }
                )
            }
        }
    }

    private func signOut() {
        PushNotificationManager.shared.stopNotificationSync()
        UserDefaults.standard.removeObject(forKey: "auth_token")
        UserDefaults.standard.removeObject(forKey: "auth_role")
        UserDefaults.standard.removeObject(forKey: "auth_name")
        UserDefaults.standard.removeObject(forKey: "auth_chapter")
        UserDefaults.standard.removeObject(forKey: "auth_user_id")
        UserDefaults.standard.removeObject(forKey: "auth_photo_url")
        self.token = nil
        self.userPhotoUrl = nil
    }
}

// MARK: - Main President Container with TopBar, Sidebar Drawer & BottomBar (Parity with MainActivity.kt)
struct MainPresidentContainerView: View {
    let token: String?
    let presidentName: String
    let chapterName: String
    let userId: String
    var initialPhotoUrl: String? = nil
    let onSignOut: () -> Void

    @StateObject private var pushManager = PushNotificationManager.shared
    @State private var userPhotoUrl: String? = nil
    @State private var currentScreen: AppScreen = .dashboard
    @State private var isDrawerOpen = false
    @State private var showExchangeQrModal = false
    @State private var showNotificationCenter = false
    @State private var targetContactId: String? = nil

    var body: some View {
        ZStack {
            // Main Content Area with pinned TopBar and BottomBar via safeAreaInset
            ZStack {
                Color.jciBackgroundLight.ignoresSafeArea()

                switch currentScreen {
                case .dashboard:
                    DashboardOverviewView(
                        token: token,
                        presidentName: presidentName,
                        chapterName: chapterName,
                        userId: userId,
                        serverPhotoUrl: userPhotoUrl,
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
                case .projects:
                    ProjectsAndTasksView(chapterName: chapterName) {
                        currentScreen = .dashboard
                    }
                case .members:
                    MembersAndNetworkingView(
                        chapterName: chapterName,
                        token: token,
                        onBack: { currentScreen = .dashboard },
                        onNavigateToScanCard: { currentScreen = .scanCard }
                    )
                case .scanCard:
                    ScanCardModuleView(
                        chapterName: chapterName,
                        token: token,
                        initialTargetContactId: targetContactId,
                        onBack: { currentScreen = .dashboard }
                    )
                case .flyers:
                    PresidentFlyerStudioView(
                        token: token ?? "",
                        presidentName: presidentName,
                        chapterName: chapterName,
                        userId: userId,
                        serverPhotoUrl: userPhotoUrl
                    )
                case .store, .vendor:
                    VendorStoreView(
                        chapterName: chapterName,
                        token: token,
                        onBack: { currentScreen = .dashboard }
                    )
                case .websitePortal:
                    ChapterWebsiteBuilderView(
                        chapterName: chapterName,
                        presidentName: presidentName,
                        onBack: { currentScreen = .dashboard }
                    )
                case .resources:
                    JciResourcesView(
                        chapterName: chapterName,
                        onBack: { currentScreen = .dashboard }
                    )
                case .calendar:
                    InteractiveCalendarView(
                        onBack: { currentScreen = .dashboard },
                        onNavigateToFlyer: { currentScreen = .flyers }
                    )
                case .lgb:
                    LgbAndPstManagementView(
                        token: token ?? "",
                        chapterName: chapterName,
                        presidentName: presidentName,
                        onBack: { currentScreen = .dashboard }
                    )
                case .frames:
                    FramesView(
                        onBack: { currentScreen = .dashboard }
                    )
                case .profile:
                    ProfileView(
                        name: presidentName,
                        chapterName: chapterName,
                        role: "PRESIDENT",
                        token: token,
                        onLogout: onSignOut,
                        onBack: { currentScreen = .dashboard }
                    )
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            // Pin TopBar below the top safe area (notch/Dynamic Island/status bar), extending dark navy into safe area
            .safeAreaInset(edge: .top, spacing: 0) {
                TopBarView(
                    userName: presidentName,
                    chapterName: chapterName,
                    photoUrl: userPhotoUrl,
                    unreadCount: pushManager.unreadCount,
                    onOpenSidebar: {
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.86)) {
                            isDrawerOpen.toggle()
                        }
                    },
                    onNavigateToDashboard: {
                        currentScreen = .dashboard
                    },
                    onOpenExchangeQr: {
                        showExchangeQrModal = true
                    },
                    onOpenNotifications: {
                        showNotificationCenter = true
                    },
                    onNavigateToProfile: {
                        currentScreen = .profile
                    },
                    onSignOut: onSignOut
                )
                .background(Color.jciDeepNavyDark.ignoresSafeArea(edges: .top))
            }
            // Pin BottomBar above the bottom safe area (home indicator), extending white into safe area
            .safeAreaInset(edge: .bottom, spacing: 0) {
                BottomNavigationBarView(currentScreen: $currentScreen)
                    .background(Color.white.ignoresSafeArea(edges: .bottom))
            }

            // Full Height Sidebar Navigation Drawer (MainActivity.kt:287-448)
            SidebarDrawerView(
                isOpen: $isDrawerOpen,
                currentScreen: currentScreen,
                userName: presidentName,
                chapterName: chapterName,
                onSelectScreen: { screen in
                    currentScreen = screen
                },
                onSignOut: onSignOut
            )

            // Luxury Contact Exchange QR Modal Dialog (MainActivity.kt:183-284)
            if showExchangeQrModal {
                ExchangeQrDialogView(
                    userName: presidentName,
                    chapterName: chapterName,
                    userId: userId,
                    onDismiss: { showExchangeQrModal = false },
                    onOpenFullScanner: {
                        showExchangeQrModal = false
                        currentScreen = .scanCard
                    }
                )
                .transition(.opacity)
                .zIndex(100)
            }
        }
        .onAppear {
            if userPhotoUrl == nil || userPhotoUrl?.isEmpty == true {
                userPhotoUrl = initialPhotoUrl ?? UserDefaults.standard.string(forKey: "auth_photo_url")
            }
            if userPhotoUrl == nil || userPhotoUrl?.isEmpty == true {
                Task {
                    if let leader = try? await ApiService.shared.getLeaderProfile(userId: userId),
                       let photo = leader.photoUrl, !photo.isEmpty {
                        await MainActor.run {
                            self.userPhotoUrl = photo
                            UserDefaults.standard.set(photo, forKey: "auth_photo_url")
                        }
                    }
                }
            }
            pushManager.requestAuthorization(authToken: token)
            pushManager.startNotificationSync(token: token)
            pushManager.onNavigateToContact = { contactId in
                self.targetContactId = contactId
                self.currentScreen = .scanCard
            }
        }
        .sheet(isPresented: $showNotificationCenter) {
            NotificationCenterSheet(token: token) { contactId in
                if let cid = contactId {
                    self.targetContactId = cid
                    self.currentScreen = .scanCard
                }
            }
        }
    }

    private func handleSubRoute(_ route: String) {
        switch route {
        case "projects": currentScreen = .projects
        case "members": currentScreen = .members
        case "scan_card": currentScreen = .scanCard
        case "flyers": currentScreen = .flyers
        case "store": currentScreen = .store
        case "website_builder", "website_portal": currentScreen = .websitePortal
        case "resources": currentScreen = .resources
        case "calendar": currentScreen = .calendar
        case "lgb": currentScreen = .lgb
        case "frames": currentScreen = .frames
        case "profile": currentScreen = .profile
        case "vendor": currentScreen = .vendor
        default:
            currentScreen = .dashboard
        }
    }
}
