import Foundation

enum AppScreen: String, CaseIterable, Identifiable {
    case dashboard = "dashboard"
    case projects = "projects"
    case members = "members"
    case resources = "resources"
    case scanCard = "scan_card"
    case store = "store"
    case flyers = "flyers"
    case celebrations = "celebrations"
    case websitePortal = "website_portal"
    case calendar = "calendar"
    case lgb = "lgb"
    case frames = "frames"
    case vendor = "vendor"
    case profile = "profile"

    var id: String { rawValue }

    var title: String {
        switch self {
        case .dashboard: return "Home"
        case .projects: return "Projects"
        case .members: return "Members"
        case .resources: return "JCI Resources"
        case .scanCard: return "Scan Card"
        case .store: return "Store"
        case .flyers: return "Flyers"
        case .celebrations: return "Celebrations"
        case .websitePortal: return "LO Website"
        case .calendar: return "Calendar"
        case .lgb: return "LGB & PST"
        case .frames: return "Frames"
        case .vendor: return "Vendor Portal"
        case .profile: return "Profile"
        }
    }

    var icon: String {
        switch self {
        case .dashboard: return "🏛️"
        case .projects: return "🚀"
        case .members: return "👥"
        case .resources: return "🗂️"
        case .scanCard: return "📇"
        case .store: return "🛍️"
        case .flyers: return "⚡"
        case .celebrations: return "🎉"
        case .websitePortal: return "🌐"
        case .calendar: return "📅"
        case .lgb: return "🪪"
        case .frames: return "📸"
        case .vendor: return "🏪"
        case .profile: return "👤"
        }
    }

    /// Primary Drawer Items in exact Android order (MainActivity.kt:378-390)
    static let drawerItems: [AppScreen] = [
        .dashboard,
        .projects,
        .members,
        .resources,
        .scanCard,
        .store,
        .flyers,
        .celebrations,
        .websitePortal,
        .calendar,
        .lgb,
        .frames,
        .vendor,
        .profile
    ]

    /// Primary Bottom Navigation Items in exact Android order (MainActivity.kt:115-121)
    static let bottomNavItems: [AppScreen] = [
        .dashboard,
        .projects,
        .members,
        .flyers,
        .store
    ]
}

