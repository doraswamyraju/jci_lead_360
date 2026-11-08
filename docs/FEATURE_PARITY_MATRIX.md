# JCI LEAD360 - ANDROID TO iOS FEATURE PARITY MATRIX

| Feature Area | Android Implementation (Source of Truth) | iOS Implementation | Parity Status | Verification Notes |
|---|---|---|---|---|
| **Branding & Theme** | `JciColors.kt` (DeepNavy, RoyalBlue, SkyBlue, ShieldTeal) | `JciTheme.swift` (Native `Color` + `JciGradients`) | **100%** | Exact hex match across all surfaces & gradients |
| **Authentication** | `LoginScreen.kt` (Retrofit `POST /api/auth/login`) | `LoginView.swift` (`ApiService.shared.login`) | **100%** | Supports President, Super Admin, and Vendor roles |
| **Session Persistence** | `SharedPreferences ("lead360_auth_prefs")` | `UserDefaults.standard` | **100%** | Persists token, role, name, chapter, and userId |
| **Role Routing** | `MainActivity.kt` role condition branching | `JCI2027App.swift` root scene routing | **100%** | Routes to President TabView, Admin, or Vendor |
| **Dashboard KPIs** | `DashboardOverviewScreen.kt` | `DashboardOverviewView.swift` | **100%** | Stat cards, Chapter banner, 10 quick launchers |
| **Projects & Tasks** | `ProjectsAndTasksScreen.kt` | `ProjectsAndTasksView.swift` | **100%** | Category filter chips, progress bar, due dates |
| **Members Directory** | `MembersAndNetworkingScreen.kt` (`/api/members`) | `MembersAndNetworkingView.swift` | **100%** | Live API fetch, search, filter tabs, approve action |
| **Members Map** | `InteractiveMembersMapView` (OSM / Map) | `MembersMapSheet` (`MapKit.Map`) | **100%** | Native iOS MapKit pin rendering of member locations |
| **Scan Card / Rolodex** | `ScanCardModuleScreen.kt` (`/api/contacts`) | `ScanCardModuleView.swift` | **100%** | Full contact listing, search, add form, delete |
| **Export to Contacts** | `ContactsContract.Intents.Insert` | `CNContactStore` + `CNMutableContact` | **100%** | Saves visitor contact directly into iPhone Contacts app |
| **Flyer Studio** | `PresidentFlyerStudioScreen.kt` | `PresidentFlyerStudioView.swift` | **100%** | 5 Categories, dynamic live canvas, custom inputs |
| **Flyer Compositor** | `AndroidFlyerRenderer.kt` (Bitmap Canvas) | `iOSFlyerRenderer.swift` (CoreGraphics / UIGraphics) | **100%** | High-res 300 DPI rendering with shadow typography |
| **WhatsApp Sharing** | Android `ACTION_SEND` Intent | `UIActivityViewController` | **100%** | 1-tap share image and invite text to WhatsApp/Socials |
| **Vendor Store** | `VendorStoreScreen.kt` (`/api/vendor/catalog`) | `VendorStoreView.swift` | **100%** | Catalog items, order sheet, total calculator, place order |
| **Reciprocal QR** | `QrCodeGenerator.kt` (ZXing `buildExchangeUrl`) | `QrCodeGenerator.swift` (`CIFilter.qrCodeGenerator`) | **100%** | Generates QR for `https://lead.sriddha.com/card/:userId` |
| **Notification Center** | `NotificationCenterBottomSheet.kt` | `NotificationCenterSheet.swift` | **100%** | Live alert list, mark all read, deep link to contact |
| **Super Admin Module** | `AdminAppContainer.kt` | `AdminAppContainerView.swift` | **100%** | Vendors, Chapter leaders, and templates tabs |
| **Vendor Partner App** | `VendorAppContainer.kt` | `VendorAppContainerView.swift` | **100%** | Catalog, Orders, and Home tabs for vendor partners |
| **LO Website Portal** | `ChapterWebsiteBuilderScreen.kt` | `ChapterWebsiteBuilderView` | **100%** | Web link copy, headline & welcome message editor |
| **JCI Resources** | `JciResourcesScreen.kt` | `JciResourcesView` | **100%** | Download cards for constitution, brand & protocols |
| **Calendar** | `InteractiveCalendarScreen.kt` | `InteractiveCalendarView` | **100%** | Event list with direct flyer generator action |
| **LGB & PST Board** | `LgbAndPstManagementScreen.kt` | `LgbAndPstManagementView` | **100%** | Officer directory with 1-tap `tel:` phone call action |
