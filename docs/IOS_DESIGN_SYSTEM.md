# JCI LEAD360 - iOS DESIGN SYSTEM SPECIFICATION

> **Target Platform**: iOS 16.0+ (SwiftUI & Swift 5.9+)  
> **Brand**: Junior Chamber International (JCI 2027)  
> **Design Philosophy**: Modern Apple Human Interface Guidelines with JCI Heritage Colors & Materials

---

## 1. Color Palette & Asset Specifications

| Token Name | Hex Code | SwiftUI Definition | Purpose / Usage |
|---|---|---|---|
| `jciDeepNavy` | `#0F182E` | `Color(hex: "0F182E")` | Dark primary background, Navigation headers, Title text |
| `jciDeepNavyDark`| `#0A0F1D` | `Color(hex: "0A0F1D")` | Deepest contrast background, Dark mode surfaces |
| `jciRoyalBlue` | `#005BAA` | `Color(hex: "005BAA")` | Primary interactive brand color, Primary action buttons |
| `jciSkyBlue` | `#0097D8` | `Color(hex: "0097D8")` | Accent color, Active tab indicators, Gradient highlights |
| `jciShieldTeal` | `#36BBA6` | `Color(hex: "36BBA6")` | Success badges, Verified indicators, Mint secondary accent |
| `jciTealLight` | `#E6F8F5` | `Color(hex: "E6F8F5")` | Badge background surfaces, subtle highlights |
| `jciBackgroundLight`| `#F6F8FC`| `Color(hex: "F6F8FC")` | System-grouped background (Light mode) |
| `jciCardSurface` | `#FFFFFF` | `Color.white` | Elevated card surfaces, Sheet backgrounds |
| `jciBorderSubtle` | `#E2E8F0` | `Color(hex: "E2E8F0")` | Card strokes, Divider borders |
| `jciTextDark` | `#0F172A` | `Color(hex: "0F172A")` | Primary high-contrast body & heading text |
| `jciTextMuted` | `#64748B` | `Color(hex: "64748B")` | Secondary supporting labels, captions |

---

## 2. Gradient System

```swift
extension LinearGradient {
    static let jciHero = LinearGradient(
        colors: [Color(hex: "0F182E"), Color(hex: "005BAA"), Color(hex: "0097D8")],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
    static let jciShield = LinearGradient(
        colors: [Color(hex: "0097D8"), Color(hex: "36BBA6")],
        startPoint: .leading,
        endPoint: .trailing
    )
    
    static let jciCardGlass = LinearGradient(
        colors: [Color.white.opacity(0.85), Color.white.opacity(0.65)],
        startPoint: .top,
        endPoint: .bottom
    )
}
```

---

## 3. Typography & Hierarchy (San Francisco Dynamic Type)

- **Large Hero Titles**: `.system(size: 28...34, weight: .bold, design: .default)`
- **Navigation Titles**: `.system(size: 18...20, weight: .semibold)`
- **Card Titles & Section Headers**: `.system(size: 16...18, weight: .bold)`
- **Body Text**: `.system(size: 14...15, weight: .regular)`
- **Supporting / Metadata**: `.system(size: 12...13, weight: .medium)`
- **Badges & Footnotes**: `.system(size: 10...11, weight: .semibold)`

---

## 4. Native iOS UI Adaptations

1. **Navigation**: Native `TabView` with modern iOS icons (SF Symbols) and `NavigationStack` for sub-screens.
2. **Cards & Materials**: Rounded cards with `cornerRadius: 16...20`, subtle shadows `Color.black.opacity(0.06)`, and ultra-thin material backgrounds where appropriate.
3. **Modals & Sheets**: Native `.sheet` presentation with `.presentationDetents([.medium, .large])` for BottomSheets (e.g. Notifications, Contact Details, Customizer).
4. **Haptics**: `UIImpactFeedbackGenerator(style: .medium).impactOccurred()` on primary button presses and scan actions.
5. **Interactive Controls**: iOS native `Picker(.segmented)` for category and status filtering, native `Toggle`, and native `DatePicker`.

