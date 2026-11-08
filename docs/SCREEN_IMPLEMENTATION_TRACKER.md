# JCI LEAD360 - iOS SCREEN IMPLEMENTATION TRACKER

> **Source of Truth**: Android Project (`mobile-android/app/src/main/java/com/jci2027/lead360/`)  
> **iOS Destination**: `mobile-ios/JCI2027/`

---

| # | Screen Name | iOS Swift File | Status | Notes / Features Replicated |
|---|---|---|---|---|
| 1 | **Login Screen** | `Features/Auth/LoginView.swift` | **Verified** | Full auth flow, Remember credentials, Role routing to President, Super Admin, and Vendor |
| 2 | **Dashboard Overview** | `Features/Dashboard/DashboardOverviewView.swift` | **Verified** | JCI Hero banner, Stat badges, 10-module quick launcher grid, QR button, Notification bell |
| 3 | **Projects & Tasks** | `Features/Projects/ProjectsAndTasksView.swift` | **Verified** | Category filtering (Community, Business, Individual, etc.), Progress indicator bars, Due dates |
| 4 | **Members & Networking** | `Features/Members/MembersAndNetworkingView.swift` | **Verified** | Live `GET /api/members`, Category filter, Search query, MapKit sheet, Member preview sheet, Approval button |
| 5 | **Scan Card & Rolodex** | `Features/ScanCard/ScanCardModuleView.swift` | **Verified** | Live `GET /api/contacts`, Add contact form, CNContactStore export to iPhone Contacts, Delete contact |
| 6 | **President Flyer Studio** | `Features/Flyers/PresidentFlyerStudioView.swift` | **Verified** | 5 Categories, Live dynamic canvas preview, Text customization, 1080x1080 CoreGraphics rendering, 1-tap WhatsApp/Social share |
| 7 | **Vendor Store** | `Features/Store/VendorStoreView.swift` | **Verified** | Live `GET /api/vendor/catalog`, Stepper quantity selector, Notes field, Total calculation, `POST /api/vendor/orders` |
| 8 | **LO Website Portal** | `Features/Dashboard/SecondaryScreens.swift` | **Verified** | Public web link preview, Clipboard copy, Editable homepage banner and president welcome note |
| 9 | **JCI Resources Hub** | `Features/Dashboard/SecondaryScreens.swift` | **Verified** | Categorized protocol manuals, Brand standards, Robert's Rules, Constitution & Policy |
| 10 | **Interactive Calendar** | `Features/Dashboard/SecondaryScreens.swift` | **Verified** | Local, Zone, and National events, Direct "Make Flyer" action routing into Flyer Studio |
| 11 | **LGB & PST Directory** | `Features/Dashboard/SecondaryScreens.swift` | **Verified** | Executive board listing, Direct `tel:` phone call intent action |
| 12 | **Photo Frames Studio** | `Features/Dashboard/SecondaryScreens.swift` | **Verified** | Official 2027 frames description and photo fitting view |
| 13 | **Profile & Settings** | `Features/Dashboard/SecondaryScreens.swift` | **Verified** | Leader profile overview, Chapter information, Digital business card, Sign Out |
| 14 | **Notification Center** | `Features/Dashboard/NotificationCenterSheet.swift` | **Verified** | Live `GET /api/vendor/notifications`, Mark all read, Tap notification to deep link directly to contact card |
| 15 | **Admin App Container** | `Features/Admin/AdminAppContainerView.swift` | **Verified** | Multi-tab container (Vendors & Products, Chapter Leaders DB, Templates), Sign Out |
| 16 | **Vendor App Container** | `Features/Vendor/VendorAppContainerView.swift` | **Verified** | Multi-tab container (Home, Catalog, Orders, RFQs), Sign Out |
