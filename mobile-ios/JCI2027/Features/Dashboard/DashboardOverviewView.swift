import SwiftUI

struct DashboardOverviewView: View {
    let presidentName: String
    let chapterName: String
    let onNavigateTab: (String) -> Void
    let onOpenQrExchange: () -> Void
    let onOpenNotifications: () -> Void

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 20) {
                // Header Bar with Notification Bell & Instant QR
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Welcome Back,")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(.jciTextMuted)
                        Text(presidentName)
                            .font(.system(size: 22, weight: .bold))
                            .foregroundColor(.jciTextDark)
                    }

                    Spacer()

                    // Quick QR Button
                    Button(action: onOpenQrExchange) {
                        Image(systemName: "qrcode")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(.jciRoyalBlue)
                            .padding(10)
                            .background(Color.jciRoyalBlue.opacity(0.1))
                            .clipShape(Circle())
                    }

                    // Notification Bell Button
                    Button(action: onOpenNotifications) {
                        Image(systemName: "bell.fill")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(.jciSkyBlue)
                            .padding(10)
                            .background(Color.jciSkyBlue.opacity(0.1))
                            .clipShape(Circle())
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 10)

                // Chapter Brand Banner
                HStack {
                    VStack(alignment: .leading, spacing: 6) {
                        HStack(spacing: 6) {
                            Circle()
                                .fill(Color.jciShieldTeal)
                                .frame(width: 8, height: 8)
                            Text("ACTIVE CHAPTER • ZONE XXVI")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.jciShieldTeal)
                        }

                        Text(chapterName)
                            .font(.system(size: 20, weight: .black))
                            .foregroundColor(.white)

                        Text("JCI LEAD360 Digital Ecosystem 2027")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(.white.opacity(0.8))
                    }
                    Spacer()
                }
                .padding(20)
                .background(JciGradients.hero)
                .cornerRadius(20)
                .shadow(color: Color.jciDeepNavy.opacity(0.3), radius: 10, x: 0, y: 6)
                .padding(.horizontal, 20)

                // High Executive Stat Badges
                HStack(spacing: 12) {
                    StatCard(title: "Active Projects", value: "8", icon: "rocket.fill", tint: .jciRoyalBlue)
                    StatCard(title: "Members", value: "48", icon: "person.3.fill", tint: .jciShieldTeal)
                    StatCard(title: "Rolodex", value: "112", icon: "person.crop.rectangle.fill", tint: .jciSkyBlue)
                }
                .padding(.horizontal, 20)

                // Quick Navigation Grid
                VStack(alignment: .leading, spacing: 14) {
                    Text("Executive Command Hub")
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.jciTextDark)
                        .padding(.horizontal, 20)

                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
                        ModuleLaunchCard(title: "Projects & Tasks", subtitle: "Track chapter KPIs", icon: "checklist", color: .jciRoyalBlue) {
                            onNavigateTab("projects")
                        }
                        ModuleLaunchCard(title: "Chapter Members", subtitle: "Directory & network map", icon: "person.2.fill", color: .jciShieldTeal) {
                            onNavigateTab("members")
                        }
                        ModuleLaunchCard(title: "Scan Card & QR", subtitle: "AI Rolodex & OCR", icon: "qrcode.viewfinder", color: .jciSkyBlue) {
                            onNavigateTab("scan_card")
                        }
                        ModuleLaunchCard(title: "Flyer Studio", subtitle: "300 DPI templates", icon: "sparkles", color: Color(hex: "F59E0B")) {
                            onNavigateTab("flyers")
                        }
                        ModuleLaunchCard(title: "Vendor Store", subtitle: "Installation kits & flags", icon: "cart.fill", color: Color(hex: "8B5CF6")) {
                            onNavigateTab("store")
                        }
                        ModuleLaunchCard(title: "Chapter Portal", subtitle: "LO website builder", icon: "globe", color: Color(hex: "EC4899")) {
                            onNavigateTab("website_portal")
                        }
                        ModuleLaunchCard(title: "JCI Resources", subtitle: "Manuals & constitution", icon: "folder.fill", color: .jciTextDark) {
                            onNavigateTab("resources")
                        }
                        ModuleLaunchCard(title: "Calendar 2027", subtitle: "National & local events", icon: "calendar", color: Color(hex: "06B6D4")) {
                            onNavigateTab("calendar")
                        }
                        ModuleLaunchCard(title: "LGB & PST Board", subtitle: "Officers directory", icon: "person.text.rectangle", color: .jciRoyalBlue) {
                            onNavigateTab("lgb")
                        }
                        ModuleLaunchCard(title: "Frames Studio", subtitle: "Social media frames", icon: "camera.viewfinder", color: .jciShieldTeal) {
                            onNavigateTab("frames")
                        }
                    }
                    .padding(.horizontal, 20)
                }

                Spacer(minLength: 30)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
    }
}

struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    let tint: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: icon)
                    .foregroundColor(tint)
                Spacer()
            }
            Text(value)
                .font(.system(size: 22, weight: .black, design: .rounded))
                .foregroundColor(.jciTextDark)
            Text(title)
                .font(.system(size: 11, weight: .semibold))
                .foregroundColor(.jciTextMuted)
        }
        .padding(14)
        .frame(maxWidth: .infinity)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.04), radius: 6, x: 0, y: 3)
    }
}

struct ModuleLaunchCard: View {
    let title: String
    let subtitle: String
    let icon: String
    let color: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(alignment: .leading, spacing: 10) {
                Image(systemName: icon)
                    .font(.system(size: 22))
                    .foregroundColor(.white)
                    .padding(10)
                    .background(color)
                    .cornerRadius(12)

                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(.jciTextDark)
                    Text(subtitle)
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(.jciTextMuted)
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.white)
            .cornerRadius(18)
            .shadow(color: Color.black.opacity(0.04), radius: 8, x: 0, y: 4)
        }
    }
}
