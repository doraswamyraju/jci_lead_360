# JCI LEAD360 - Dynamic Flyer & Frame Automation Platform
## Project Status & Master Implementation Report

> **Project Name**: **JCI LEAD360** (*Digital Support for JCI Leaders*)  
> **Target Year**: JCI 2027  
> **Production Domain**: `lead.sriddha.com`  
> **Assigned VPS Port**: `5087` (Isolated PM2 / Node service)  
> **Platforms**: Web Portal, Native Android (Kotlin / Jetpack Compose), Native iOS (Swift / SwiftUI)  
> **Hosting & Infrastructure**: Hostinger VPS (Ubuntu, Nginx Reverse Proxy, Node.js/PostgreSQL)  
> **Architecture Principle**: Highly modular, single authentication entry point with role-based routing, standalone isolated components per feature.
> **Deployment Workflow**: Code pushed from local to GitHub repo (`jci_lead_360`) -> Pulled directly onto Hostinger VPS -> Built and run on port `5087` -> Tested live on `lead.sriddha.com`. No local dev server execution.

---

## 1. Project Overview & Architectural Standards

### 1.1 Single Unified Authentication Engine
Across Web, Android, and iOS, there is only **one login screen** (`/login` or `LoginScreen`).
- The user enters their credentials (or mobile OTP / email + password).
- The auth payload returns the JWT token along with the user's role and chapter mapping:
  - `SUPER_ADMIN`
  - `EMPLOYEE`
  - `PRESIDENT`
  - `SECRETARY`
  - `TREASURER`
- The system automatically resolves permissions and routes the user to their designated dashboard module without separate app flavors or fragmented login portals.

### 1.2 Modular Code Structure Standard (Keep Files Small & Focused)
Every feature is built as a self-contained, standalone component in its own file and imported cleanly into dashboard layouts:
- **Web**: Modular React/Vue components (e.g., `<TemplateMarkerCanvas />`, `<QuickUserToggle />`, `<PSTProfileCard />`, `<WhatsAppShareTrigger />`).
- **Android**: Jetpack Compose individual composable files (e.g., `TemplateGridItem.kt`, `FrameOverlayRenderer.kt`, `RoleRouter.kt`, `WhatsAppShareManager.kt`).
- **iOS**: SwiftUI standalone views & modifiers (e.g., `TemplateCardView.swift`, `FrameCanvasRenderer.swift`, `AuthRoutingView.swift`, `ShareActivityHelper.swift`).
- Max file size guideline: **< 200–250 lines per file** for clean maintainability, performance, and instant compilation.

---

## 2. Daily Development Status Log

| Date | Module / Layer | Tasks Completed / In Progress | Status | Notes & Blockers |
| :--- | :--- | :--- | :--- | :--- |
| **2026-10-17** | **Architecture & VPS Setup** | Configured GitHub remote, Port 5087 binding, firewall specs, isolated Nginx setup, and master tracking report. | **Completed** | Master project report created. |
| **2026-10-17** | **Hostinger Production Deployment** | Configured Nginx reverse proxy on port 5087, SSL certificate with Certbot on `lead.sriddha.com`, PM2 process isolation. | **Completed** | Live on HTTPS with multi-site protection. |
| **2026-10-17** | **Super Admin Web & DB** | Built real-data Super Admin suite: Live DB Stats, User/Chapter Manager, Visual Template Marker with bounding-box canvas, Frame Studio, and Multer file uploads. No dummy data. | **Completed** | Fully wired to live database API. |
| **2026-10-17** | **Enterprise UI & Navigation Shell** | Built modular dark sidebar with branded logo pill, sticky topbar with VPS status, floating action buttons (FAB), and mobile bottom navigation. | **Completed** | Clean separation into standalone modules. |
| *Upcoming* | **Chapter President Desk** | 1-Tap dynamic flyer generator with auto-populated PST details & WhatsApp direct share. | *Pending* | - |
| *Upcoming* | **Android Native (Kotlin)** | Project init, Compose navigation, single auth screen, and local canvas renderer. | *Pending* | - |
| *Upcoming* | **iOS Native (Swift)** | Project init, SwiftUI navigation, single auth screen, and CoreGraphics renderer. | *Pending* | - |

---

## 3. Work Breakdown Structure (WBS) & Feature Modules

### Module 0: Unified Authentication & Role Resolver (Cross-Platform)
- [x] `AuthService` (Backend): Single login endpoint validating email/phone & password.
- [x] `RoleResolver` (Web/iOS/Android): Interceptor/router directing tokens to appropriate dashboard:
  - `Role == SUPER_ADMIN` $\rightarrow$ Super Admin Dashboard
  - `Role == EMPLOYEE` $\rightarrow$ Employee Studio Dashboard
  - `Role == PRESIDENT` $\rightarrow$ President Command Hub
  - `Role == SECRETARY \| TREASURER` $\rightarrow$ PST Flyer Desk
- [x] Session persistence and automatic re-authentication on refresh.

---

### Module 1: Super Admin & Employee Suite (Web Primary + Mobile Companion)

#### 1.1 Web Portal (Heavy Ops)
- [ ] **Auth & Navigation Container**: `AdminDashboardLayout`
- [ ] **Visual Template Marker Tool**:
  - `ImageUploadZone.tsx`: Upload high-res flyer base images (`.webp`, `.png`).
  - `CanvasMarkerBoard.tsx`: Draggable/resizable bounding boxes for dynamic fields:
    - Fixed tags: Chapter Logo, President photo/name, Secretary name, Treasurer name.
    - Custom event tags: Event title, Date, Time, Venue, Chief Guest photo/name, Register link.
  - `CoordinateExporter.tsx`: Generates standardized coordinate JSON mapping.
- [ ] **Frame Overlay Creator**:
  - `FrameUploadModal.tsx`: Upload transparent PNG overlays (1:1, 4:5, 9:16).
  - `FrameSnapGrid.tsx`: Define user photo mask area.
- [ ] **User & Chapter Management**:
  - `ChapterListTable.tsx`: Search, view, activate/deactivate LOM chapters.
  - `PresidentProvisioningModal.tsx`: Create chapter and assign President credentials.
  - `EmployeeManager.tsx`: Invite and manage staff/designers with specific access limits.
- [ ] **JCI 2027 Calendar & Efficiency Engine**:
  - `CalendarActivityManager.tsx`: Manage 100% Efficiency checklist items, UN Days, and national events.
  - `TemplateTagAssigner.tsx`: Attach specific templates to release on targeted dates.

#### 1.2 Mobile Companion (Android & iOS for Super Admin & Employee)
- [ ] `AdminStatsCard`: Live KPIs (active chapters, daily flyers generated, VPS health).
- [ ] `QuickUserSearch`: Quick lookup of any chapter/president to reset passwords or toggle status.
- [ ] `TemplateProofViewer`: Preview newly uploaded templates on actual phone screens with sample data.
- [ ] `BroadcastPushModal`: Send instant announcements to chapter presidents.

---

### Module 2: Chapter President Hub (Android / iOS / Web)

#### 2.1 Profile & Team Setup
- [ ] `ChapterProfileModule`:
  - Upload Chapter Logo (transparent PNG).
  - President Name, High-res portrait photo, contact details, social links.
- [ ] `TeamCredentialsManager`:
  - Create/manage login for Secretary (Name, Email/Phone, Photo).
  - Create/manage login for Treasurer (Name, Email/Phone, Photo).
  - 1-tap regenerate password or re-assign mid-year.

#### 2.2 Flyer & Frame Generation
- [ ] `TemplateBrowseGrid`: Categorized list (100% Efficiency, Training, Business, Social, Special Days).
- [ ] `FlyerDetailForm`: Auto-prefills PST data; user only inputs Date, Time, Venue, Chief Guest.
- [ ] `FrameSelfieMaker`: Native camera/gallery photo picker + pinch/zoom/crop tool inside transparent JCI 2027 frame.
- [ ] `NativeCanvasRenderer`: Local client-side image compositor producing 300 DPI graphics in milliseconds.
- [ ] `NativeShareDispatcher`: 1-click share to WhatsApp group with auto-generated text caption, and share to Instagram Story/Feed.

---

### Module 3: Secretary & Treasurer Desk

- [ ] Same fast flyer generation and frame tools as President.
- [ ] Circular & Notice template drafter (meeting notices, agendas, financial reports).
- [ ] Direct WhatsApp Chapter broadcast trigger.

---

### Module 4: Backend API & Hostinger VPS Services

- [ ] **Runtime**: Node.js (Express / Fastify) or Go running under PM2/systemd.
- [ ] **Reverse Proxy & Security**: Nginx with SSL, Rate Limiting, and CORS.
- [ ] **Database**: PostgreSQL with JSONB support for fast coordinate querying.
- [ ] **Media Pipeline**: Automated optimization of uploaded flyer backgrounds (compress to WebP/PNG).
- [ ] **Push Notification Gateway**: Firebase Cloud Messaging (FCM) integration for Android & iOS.

---

## 4. File Structure Strategy (Modular by Design)

```
pixel/
├── docs/
│   └── PROJECT_REPORT.md             <-- This master tracking document
├── backend/                          <-- Hostinger VPS API
│   ├── src/
│   │   ├── modules/
│   │   │   ├── auth/                 <-- Single login & role resolver
│   │   │   ├── users/                <-- Super Admin, Employees, Presidents, PST
│   │   │   ├── templates/            <-- Base images, categories, coordinates
│   │   │   ├── frames/               <-- Frame overlays & photo masks
│   │   │   └── calendar/             <-- JCI 100% Efficiency & Special Days
│   │   └── server.js
├── web/                              <-- Web Portal (Admin, Employees, Chapter Desktop)
│   └── src/
│       ├── components/
│       │   ├── auth/                 <-- Single login screen & role dispatcher
│       │   ├── admin/                <-- Super Admin specific standalone tools
│       │   ├── studio/               <-- Visual template marker & frame maker
│       │   └── chapter/              <-- Chapter profile setup & flyer generator
│       └── pages/
├── mobile-android/                   <-- Native Android (Kotlin + Jetpack Compose)
│   └── app/src/main/java/com/jci2027/
│       ├── auth/                     <-- Single login screen
│       ├── navigation/               <-- RoleRouter navigating to respective dashboard
│       ├── modules/
│       │   ├── admin_companion/      <-- Stats & Quick User Search
│       │   ├── flyer_engine/         <-- Compose Canvas rendering engine
│       │   ├── frame_engine/         <-- Photo crop & frame overlay
│       │   └── share/                <-- WhatsApp & Social direct share intents
└── mobile-ios/                       <-- Native iOS (Swift + SwiftUI)
    └── JCI2027/
        ├── Auth/                     <-- Single login view
        ├── Navigation/               <-- RoleRouter navigating to respective view
        ├── Modules/
        │   ├── AdminCompanion/       <-- Swift dashboard cards
        │   ├── FlyerEngine/          <-- CoreGraphics rendering engine
        │   ├── FrameEngine/          <-- SwiftUI PhotoPicker & frame overlay
        │   └── Share/                <-- UIActivityViewController & WhatsApp URL Scheme
```

---

## 5. Daily Checklist Template (To be updated every session)

```markdown
### Date: [YYYY-MM-DD]
- **Target Component**: [e.g. Web Template Marker / Android Native Canvas]
- **Files Created / Modified**:
  - `file1.ext`
  - `file2.ext`
- **Accomplishments**:
  - [x] Item 1
  - [x] Item 2
- **Pending / Next Up**:
  - [ ] Item 3
```
