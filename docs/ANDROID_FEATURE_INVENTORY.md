# JCI LEAD360 - ANDROID FEATURE INVENTORY & AUDIT

> **Source of Truth**: Android Project (`mobile-android/app/src/main/java/com/jci2027/lead360/`)  
> **Backend Integration**: Production REST API (`https://lead.sriddha.com`) & Local Backend (`src/server.js`)  
> **Date of Audit**: November 2026

---

## 1. Executive Summary & Application Roles

The application serves three distinct user roles with dedicated routing containers:
1. **Chapter President / Leader** (`PRESIDENT`): Complete operational dashboard (Projects, Members, QR Rolodex & Business Card Scanner, AI Flyer Studio, Store, LO Website Portal, Resources, Calendar, LGB & PST Directory, Frames, Profile).
2. **Super Admin / Employee** (`SUPER_ADMIN` / `ADMIN` / `EMPLOYEE`): Multi-tab administrative container managing Vendors & Products, Chapter Leaders & Staff DB, and Flyer Templates.
3. **Official Vendor** (`VENDOR`): Multi-tab enterprise vendor portal (Dashboard, Catalog Management, Order Tracking, Custom Quotes & RFQs, Business Profile, Earnings).

---

## 2. Complete Screen & Component Inventory

| # | Screen Name | Screen Identifier | Navigation Entry Point | Available Actions | API Dependencies | UI States | User Roles | Implementation Status |
|---|---|---|---|---|---|---|---|---|
| 1 | **Login Screen** | `auth_login` | App Launch (when `token == null`) | Email/Pass entry, Password toggle, Remember me checkbox, Sign-In button | `POST /api/auth/login` | Idle, Loading, Error message | All | Audited |
| 2 | **Dashboard Overview** | `dashboard` | Bottom Nav #1 / Drawer #1 | Stat badges, 1-tap quick actions (Projects, Members, Scan Card, Store, Flyers, Website, Resources, Calendar, LGB, Frames, Profile), Instant Reciprocal QR modal, Notification bell | Local state & prefs | Loaded, Active badges | President | Audited |
| 3 | **Projects & Tasks** | `projects` | Bottom Nav #2 / Drawer #2 | Filter projects by category, Add project, Complete task, View project progress, Deadlines | In-memory + Shared state | Active, Completed, Filtered | President | Audited |
| 4 | **Members & Networking** | `members` | Bottom Nav #3 / Drawer #3 | Search members, Segmented filter (All, LGB/Board, Active, Pending Approvals), Interactive Map View toggle, Member preview dialog, Add/Edit member form, Approve member, Delete member | `GET /api/members`<br>`POST /api/members`<br>`PUT /api/members/{id}`<br>`POST /api/members/{id}/approve`<br>`DELETE /api/members/{id}` | Loading, Empty, Populated, Map sheet, Modal preview | President, Admin | Audited |
| 5 | **Scan Card & Rolodex** | `scan_card` | Quick Action / Notification tap | Scan physical business card via Camera, OCR extraction, Deduplication, Cropping, Filter by Event, Search, Add/Edit contact, Export vCard to phone contacts, Delete contact | `GET /api/contacts`<br>`POST /api/contacts`<br>`PUT /api/contacts/{id}`<br>`DELETE /api/contacts/{id}` | Loading, Scanning, Detail sheet, Edit dialog | President | Audited |
| 6 | **President Flyer Studio** | `flyers` | Bottom Nav #4 / Drawer #5 | Category selector (5 categories), Template picker (4 per category = 20 total), Field customizer (Title, Date, Venue, Chief Guest, Time), Live Preview, Render 300 DPI, WhatsApp 1-tap share, Photo save | `GET /api/templates` | Category tabs, Customizer, Render preview | President | Audited |
| 7 | **Vendor Store (Client)** | `store` | Bottom Nav #5 / Drawer #6 | Browse catalog items, Category chips, Search catalog, Request Quote / Order dialog, Quantity selector, Chapter delivery notes | `GET /api/vendor/catalog`<br>`POST /api/vendor/orders` | Loading, Grid view, Order modal | President | Audited |
| 8 | **LO Website Portal Builder** | `website_portal` | Quick Action / Drawer #7 | Live website preview (`lead.sriddha.com/lo/:chapterId`), Edit banner, Edit leader message, Toggle public sections, Copy portal link, Open in Browser | Web view / API preview | Active preview, Editable fields | President | Audited |
| 9 | **JCI Resources Hub** | `resources` | Quick Action / Drawer #8 | Download constitution, Policy manuals, JCI Brand guidelines, Protocol handbooks, Form templates | Bundled & remote PDF links | Categorized list, Download action | President | Audited |
| 10 | **Interactive Calendar** | `calendar` | Quick Action / Drawer #9 | Month view, Day event list, National/Zone/Local event tags, Quick navigate to create flyer from event | Local events store | Month grid, Selected day list | President | Audited |
| 11 | **LGB & PST Management** | `lgb` | Quick Action / Drawer #10 | Directory of Local Governing Board & Past Presidents, Add officer, Edit contact, Call/WhatsApp officer, Role hierarchy | `POST /api/users/officer-details` | Filtered list, Edit modal | President | Audited |
| 12 | **Frames & Photo Studio** | `frames` | Quick Action / Drawer #11 | Select official JCI photo frame, Upload portrait, Auto-fit/crop portrait into frame, Export framed image | Local canvas renderer | Frame picker, Canvas preview | President | Audited |
| 13 | **Profile Screen** | `profile` | Quick Action / Drawer #12 | Edit President name, Phone, Chapter name, Upload photo, Upload chapter logo, Digital Business Card modal, Sign Out | `POST /api/users/officer-details`<br>`POST /api/users/chapter-details`<br>`POST /api/upload` | Saved, Editing, Card preview | President | Audited |
| 14 | **Notification Center** | `notification_center` | Bell icon in TopAppBar | List push alerts and QR reciprocal contact exchange notifications, Mark all read, Tap to open contact card | `GET /api/vendor/notifications`<br>`POST /api/vendor/notifications/read-all` | Loading, List, Empty state | All | Audited |
| 15 | **Admin Container** | `admin_container` | Root route for `SUPER_ADMIN` | Top tabs: Vendors & Products, Leaders & Staff DB, Flyer Templates. Stats header, Create user modal, Sign out | `GET /api/users`<br>`GET /api/vendor/catalog`<br>`POST /api/users/create-user` | Tabbed views, Data tables | Super Admin | Audited |
| 16 | **Vendor Container** | `vendor_container` | Root route for `VENDOR` | Bottom tabs: Home, Catalog, Orders, RFQs, More. Sub-screens for Product detail, Add item, Create quote, Order detail, Profile, Earnings | `GET /api/vendor/catalog`<br>`GET /api/vendor/orders`<br>`GET /api/vendor/rfqs`<br>`PUT /api/vendor/profile`<br>`PUT /api/vendor/orders/{id}/status` | Multi-screen flow, Tab navigation | Vendor | Audited |

---

## 3. Native Device & Engine Inventory

1. **Flyer Compositor Engine** (`AndroidFlyerRenderer` ➔ `iOSFlyerRenderer`):
   - High-res CoreGraphics drawing of template bitmap, overlays, chapter logo, dynamic custom typography, and shadow text.
2. **Reciprocal QR Code Generator** (`QrCodeGenerator`):
   - Generates sharp QR bitmap encoding `https://lead.sriddha.com/card/:userId` for instant exchange.
3. **Business Card OCR & Contact Deduplication**:
   - Camera scanner / photo picker feeding OCR text parser and deduplication algorithms.
4. **Push Notifications & Deep Linking**:
   - APNs / FCM token registration (`POST /api/users/fcm-token`), topic broadcast (`presidents`), and deep link routing to `scan_card`.
5. **Native Sharing & Export**:
   - `UIActivityViewController` sharing high-res PNG/JPEG directly to WhatsApp, AirDrop, Messages, and saving to Photos library / iOS Contacts.

