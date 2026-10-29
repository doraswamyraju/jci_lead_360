# JCI LEAD360 Android App — Project Changelog & Architecture Summary

## 📌 Executive Overview
This document captures all enhancements, architectural updates, fixes, and new features implemented in the **JCI LEAD360 Android Application** (`mobile-android`) and backend integrations.

---

## 🏛️ 1. Official JCI Logo Generator (Replica of `logo.jciath.be`)

### Problem Addressed
- The previous logo generator used generic placeholder vectors, forced all text to uppercase, lacked official typography, and social circular avatars were getting clipped or misaligned.

### Implementation Details
- **Official Vector Paths**: Integrated exact SVG bezier curves directly from the corporate identity source:
  - Letters $J$, $C$, and rectangular $I$.
  - Official Teal & Cyan double-crest shield.
  - Crest inner contours and TM symbol.
- **Official Typography Bundled**:
  - Embedded **Helvetica Neue Bold** (`res/font/helvetica_neue_bold.ttf`) and **Plus Jakarta Sans** (`res/font/plus_jakarta_sans_bold.ttf` & `plus_jakarta_sans_medium.ttf`) into the app.
  - Linked `TextPaint` to dynamically load and render with these fonts via `ResourcesCompat`.
- **Casing Preservation**:
  - Removed all forced `.uppercase()` conversions.
  - Preserves user input exactly as typed (e.g. `Tirupati`, `Ath`, `Pays Vert`).
- **7 Authentic Cases & Layouts**:
  1. `COLOR`: Original Corporate Blue & Navy on White.
  2. `BLACK`: Monochrome Black on White.
  3. `LIGHT`: White Typography & Shield on Dark Navy (`#130F2D`).
  4. `WHITE_ON_BLUE`: White Typography on JCI Blue (`#0097D7`).
  5. `SOCIAL_WHITE`: Centered badge on White circular container.
  6. `SOCIAL_BLACK`: Centered badge on Dark Navy circular container.
  7. `SOCIAL_BLUE`: Centered badge on JCI Blue circular container.
- **Circular Social Badges Geometry**:
  - Re-proportioned dimensions (`circleRadius = width * 0.42f`).
  - Added anti-aliased canvas circular clipping (`clipPath`).
  - Positioned the mark in the upper 60% and the chapter name comfortably in the lower arc without clipping.
  - Added `ContentScale.Fit` to preview containers.

---

## 🖼️ 2. TopBar, Identity & Navigation Redesign

### Clean Brand Lockup
- **Removed Text Clutter**: Removed the redundant chapter name text sitting next to the logo in the top bar.
- **Dynamic Chapter Logo Replacement**:
  - Displays the official LEAD360 logo by default.
  - When the Chapter President uploads the local organization logo, the top bar logo dynamically swaps to display their custom chapter logo.
  - Clicking the logo routes directly back to the Home Dashboard.

### Profile Avatar & Persistent Storage
- **TopBar Avatar**: Displays the user's actual portrait photo instead of a generic letter icon.
- **Internal Storage Persistence (`InternalPhotoStore`)**:
  - Uploaded images are compressed to PNG and stored in private internal storage (`context.filesDir`).
  - Uses timestamped file names (`"${System.currentTimeMillis()}_$fileName"`) to invalidate cache and trigger instant state updates in Compose.
  - Image paths persist across app reboots via `SharedPreferences`.

### Standard Exit-to-App Logout
- Replaced previous temporary door emoji (`🚪`) with an Android vector icon asset (`ic_logout.xml`).
- Applied across both the TopBar action icon and Navigation Drawer sign-out link.

---

## 🔐 3. Authentication & Login Polish

- **Password Visibility Toggle**:
  - Added eye toggle (`👁️` / `🙈`) to show/hide password text on the login screen.
- **Credentials Persistence ("Remember Me")**:
  - Checkbox to save login email and password locally in encrypted/private `SharedPreferences`.
  - Automatically pre-populates email and password on subsequent launches so the user doesn't have to retype them every time.
- **Simplified CTA**:
  - Streamlined primary button label strictly to **"Sign In"**.

---

## 👥 4. LGB & PST Leadership Directory Management

### Interactive Bottom Sheet (`ModalBottomSheet`)
- Clicking any PST or LGB officer in the directory opens an interactive bottom sheet.
- Supports:
  - Viewing full contact information (Role, Name, Email, Phone).
  - Editing member details in-place.
  - Updating officer profile portrait.
  - Deleting officer from the chapter board with instant list state synchronization.

### PST Account Generation
- Capability to generate credentials for chapter officers (Secretary, Treasurer, VP) to grant access to templates and communications.

---

## 🎨 5. Flyer Studio & Template Management

### Two-Mode Switcher in `PresidentFlyerStudioScreen`
- **📂 Template Catalogue**: Browse categorized templates (Business, Training, Community, International, Celebrations).
  - Displays marked customizable field badges directly on top of each creative preview (e.g. `🏛️ Logo`, `👤 Photo`, `📅 Date`, `📍 Venue`, `👤 President`) so users immediately see what can be customized.
  - Tapping any template creative immediately activates the Live Studio with all pre-filled fields.
- **✨ Live Studio**: Real-time canvas preview of selected templates with live text inputs, date/time pickers, and instant generation.
  - **Mapped Chapter Logo & President Photo**: Wired `chapterLogoBitmap` and `presidentPhotoBitmap` into `triggerComposite` and `AndroidFlyerRenderer`. Both now accurately composite into template zones.
  - **Full-Screen Interactive Zoom**: Added full-screen interactive preview dialog with multi-touch pinch-to-zoom (up to 5x), two-finger pan/drag, reset button, and zoom scale indicator. Accessible via the **"🔍 Zoom / Fullscreen"** button or by tapping directly on the live flyer preview.

### WhatsApp Sharing & Media Storage (Crash-Free)
- Integrated Android `FileProvider` (`res/xml/file_paths.xml`) and `MediaStore` gallery insertion in `AndroidShareManager.kt`.
- Resolves Android 10+ scoped storage restrictions (`FileUriExposedException`), enabling crash-free direct WhatsApp image dispatch and gallery downloads.

---

## 🛠️ 6. Technical Stack & Dependencies

| Component | Technology |
|---|---|
| **Language** | Kotlin 1.9.24 |
| **UI Toolkit** | Jetpack Compose (BOM `2024.10.01`) + Material 3 |
| **Architecture** | Single-Activity Architecture (`MainActivity.kt`) |
| **Fonts** | `Helvetica Neue Bold`, `Plus Jakarta Sans` |
| **Networking** | Retrofit 2.11.0 + OkHttp 4.12.0 + Gson |
| **Image Loading** | Coil Compose 2.7.0 |
| **Vector Engine** | Android `PathParser` + Custom Canvas Vector Renderer |
| **Storage** | `MediaStore` + Scoped `FileProvider` + `SharedPreferences` |

---

## 📦 7. Key Commit History Reference

- `3ce4106`: Bundled official fonts, aligned social circles to `logo.jciath.be`, cleaned TopBar logo, replaced logout icon with standard vector, and fixed TopBar portrait caching.
- `20af036`: Preserved exact casing in logo generator and resolved circular badge clipping.
- `7b1d10b`: Aligned parameter names for `NotificationCenterBottomSheet` and `DashboardOverviewScreen`.
- `530d7ee`: Added `InternalPhotoStore`, dynamic TopBar chapter logo, and profile photo avatar.
- `76f97fe`: Restored `FramesScreen` definition and imported border modifier.
- `d366caf`: Added sleek logout icon, LO & president details in Profile, 2-view Flyer Studio, and PST/LGB bottom sheet.
- `c27e5e1`: Fixed JCI official vector paths, registered `FileProvider` for WhatsApp share, added LO logo upload.
- `e3cab2e`: Fixed Compose icon dependency issues with zero-overhead inline toggle.
- `29b674a`: Added password visibility, "Remember Me", "Sign In" button, and initial `logo.jciath.be` replica.

