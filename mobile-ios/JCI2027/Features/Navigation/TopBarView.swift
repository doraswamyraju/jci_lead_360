import SwiftUI

/// Replicates the official Android TopAppBar from MainActivity.kt (lines 452-573)
/// Container color: JciColors.DeepNavyDark (#0A0F1D)
/// Proper safe-area handling: content stays completely clear of notch, Dynamic Island, and status bar.
struct TopBarView: View {
    let userName: String
    let chapterName: String
    var photoUrl: String? = nil
    var localImage: UIImage? = nil
    var chapterLogo: UIImage? = nil
    var chapterLogoUrl: String? = nil
    var unreadCount: Int = 0
    var onOpenSidebar: () -> Void
    var onNavigateToDashboard: () -> Void
    var onOpenExchangeQr: () -> Void
    var onOpenNotifications: () -> Void
    var onNavigateToProfile: () -> Void
    var onSignOut: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            // MARK: - Navigation Icon (Hamburger Menu)
            Button(action: onOpenSidebar) {
                Text("☰")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundColor(.white)
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)

            // MARK: - Title: Clickable Dynamic Logo Badge (Chapter Logo if uploaded, else LEAD360)
            Button(action: onNavigateToDashboard) {
                HStack(spacing: 0) {
                    if let logo = chapterLogo {
                        Image(uiImage: logo)
                            .resizable()
                            .scaledToFit()
                            .frame(height: 32)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 3)
                    } else if let urlStr = chapterLogoUrl, !urlStr.isEmpty {
                        let fullUrl = urlStr.starts(with: "http") ? urlStr : "\(ApiService.shared.baseUrl)\(urlStr)"
                        AsyncImage(url: URL(string: fullUrl)) { phase in
                            switch phase {
                            case .success(let img):
                                img
                                    .resizable()
                                    .scaledToFit()
                                    .frame(height: 32)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 3)
                            default:
                                Image("lead360_logo")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(height: 30)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                            }
                        }
                    } else {
                        Image("lead360_logo")
                            .resizable()
                            .scaledToFit()
                            .frame(height: 30)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                    }
                }
                .background(Color.white)
                .cornerRadius(8)
                .shadow(color: Color.black.opacity(0.15), radius: 2, x: 0, y: 1)
            }
            .buttonStyle(.plain)

            Spacer(minLength: 4)

            // MARK: - Action 1: Instant QR Code Badge
            Button(action: onOpenExchangeQr) {
                HStack(spacing: 4) {
                    Image(systemName: "qrcode")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(.white)

                    Text("QR")
                        .font(.system(size: 11, weight: .black))
                        .foregroundColor(.white)
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 6)
                .background(Color(hex: "0097D8").opacity(0.25))
                .cornerRadius(8)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color(hex: "38BDF8").opacity(0.6), lineWidth: 1)
                )
            }
            .buttonStyle(.plain)

            // MARK: - Action 2: Notification Bell Shortcut with Live Badge
            Button(action: onOpenNotifications) {
                ZStack(alignment: .topTrailing) {
                    Text("🔔")
                        .font(.system(size: 17))
                        .frame(width: 36, height: 36)

                    if unreadCount > 0 {
                        Circle()
                            .fill(Color(hex: "EF4444"))
                            .frame(width: 9, height: 9)
                            .overlay(
                                Circle().stroke(Color.jciDeepNavyDark, lineWidth: 1.5)
                            )
                            .offset(x: 2, y: -2)
                    }
                }
                .frame(width: 40, height: 44)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)

            // MARK: - Action 3: Profile Avatar Shortcut
            Button(action: onNavigateToProfile) {
                ZStack {
                    Circle()
                        .fill(Color(hex: "0097D8"))
                        .frame(width: 34, height: 34)

                    if let local = localImage {
                        Image(uiImage: local)
                            .resizable()
                            .scaledToFill()
                            .frame(width: 34, height: 34)
                            .clipShape(Circle())
                    } else if let photo = photoUrl, !photo.isEmpty {
                        let fullUrl = photo.starts(with: "http") ? photo : "\(ApiService.shared.baseUrl)\(photo)"
                        AsyncImage(url: URL(string: fullUrl)) { phase in
                            switch phase {
                            case .success(let image):
                                image
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 34, height: 34)
                                    .clipShape(Circle())
                            default:
                                Text(String(userName.prefix(1)).uppercased())
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(.white)
                            }
                        }
                        .id(fullUrl)
                    } else {
                        Text(String(userName.prefix(1)).uppercased())
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(.white)
                    }

                    Circle()
                        .strokeBorder(Color(hex: "BAE6FD"), lineWidth: 1.5)
                        .frame(width: 34, height: 34)
                }
                .frame(width: 40, height: 44)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)

            // MARK: - Action 4: Sleek Header Logout Button
            Button(action: onSignOut) {
                Image(systemName: "rectangle.portrait.and.arrow.right")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundColor(.white)
                    .frame(width: 40, height: 44)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 10)
        .frame(height: 54)
        .frame(maxWidth: .infinity)
        .background(Color.jciDeepNavyDark)
    }
}
