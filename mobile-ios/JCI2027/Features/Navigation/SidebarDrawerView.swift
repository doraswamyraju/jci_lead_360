import SwiftUI

/// Replicates the official Android ModalNavigationDrawer and ModalDrawerSheet
/// from MainActivity.kt (lines 287-448)
/// Container color: JciColors.DeepNavy (#0F182E), width: 300dp
struct SidebarDrawerView: View {
    @Binding var isOpen: Bool
    let currentScreen: AppScreen
    let userName: String
    let chapterName: String
    var onSelectScreen: (AppScreen) -> Void
    var onSignOut: () -> Void

    var body: some View {
        GeometryReader { geometry in
            let topSafeArea = geometry.safeAreaInsets.top

            ZStack(alignment: .leading) {
                // MARK: - Scrim / Dimmed Backdrop
                if isOpen {
                    Color.black.opacity(0.55)
                        .ignoresSafeArea()
                        .transition(.opacity)
                        .onTapGesture {
                            withAnimation(.spring(response: 0.35, dampingFraction: 0.86)) {
                                isOpen = false
                            }
                        }
                }

                // MARK: - Drawer Sheet
                HStack(spacing: 0) {
                    VStack(spacing: 0) {
                        // Safe Area Header Padding (Dynamic per device)
                        Color.clear
                            .frame(height: max(topSafeArea, 20))

                        // Header Section (Logo + Profile Badge)
                        VStack(alignment: .leading, spacing: 14) {
                            // Brand Logo Card
                            HStack {
                                Spacer()
                                Image("lead360_logo")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(height: 44)
                                    .padding(6)
                                Spacer()
                            }
                            .background(Color.white)
                            .cornerRadius(10)
                            .shadow(color: Color.black.opacity(0.1), radius: 3, x: 0, y: 1)

                            // User Profile Summary
                            HStack(spacing: 10) {
                                ZStack {
                                    Circle()
                                        .fill(Color(hex: "0097D8"))
                                        .frame(width: 42, height: 42)

                                    Text(String(userName.prefix(1)).uppercased())
                                        .font(.system(size: 18, weight: .bold))
                                        .foregroundColor(.white)
                                }

                                VStack(alignment: .leading, spacing: 2) {
                                    Text(userName)
                                        .font(.system(size: 14, weight: .bold))
                                        .foregroundColor(.white)
                                        .lineLimit(1)

                                    Text(chapterName)
                                        .font(.system(size: 11))
                                        .foregroundColor(Color(hex: "BAE6FD"))
                                        .lineLimit(1)
                                }
                            }
                        }
                        .padding(.horizontal, 20)
                        .padding(.top, 12)
                        .padding(.bottom, 16)

                        // Horizontal Divider (#1E293B)
                        Rectangle()
                            .fill(Color(hex: "1E293B"))
                            .frame(height: 1)

                        // Navigation Items Scroll View
                        ScrollView(.vertical, showsIndicators: false) {
                            VStack(spacing: 4) {
                                ForEach(AppScreen.drawerItems, id: \.id) { screen in
                                    let isSelected = (currentScreen == screen)

                                    Button(action: {
                                        withAnimation(.spring(response: 0.35, dampingFraction: 0.86)) {
                                            isOpen = false
                                        }
                                        onSelectScreen(screen)
                                    }) {
                                        HStack(spacing: 12) {
                                            Text(screen.icon)
                                                .font(.system(size: 16))

                                            Text(screen.title)
                                                .font(.system(size: 14, weight: isSelected ? .bold : .medium))
                                                .foregroundColor(isSelected ? Color(hex: "BAE6FD") : Color(hex: "CBD5E1"))

                                            Spacer()
                                        }
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 10)
                                        .background(
                                            isSelected ? Color(hex: "0097D8").opacity(0.2) : Color.clear
                                        )
                                        .cornerRadius(10)
                                    }
                                    .buttonStyle(.plain)
                                    .padding(.horizontal, 12)
                                }

                                Spacer(minLength: 16)

                                // Bottom Divider
                                Rectangle()
                                    .fill(Color(hex: "1E293B"))
                                    .frame(height: 1)
                                    .padding(.horizontal, 12)

                                // Sign Out Link
                                Button(action: {
                                    withAnimation(.spring(response: 0.35, dampingFraction: 0.86)) {
                                        isOpen = false
                                    }
                                    onSignOut()
                                }) {
                                    HStack(spacing: 10) {
                                        Image(systemName: "rectangle.portrait.and.arrow.right")
                                            .font(.system(size: 18, weight: .semibold))
                                            .foregroundColor(Color(hex: "EF4444"))

                                        Text("Sign Out")
                                            .font(.system(size: 14, weight: .bold))
                                            .foregroundColor(Color(hex: "EF4444"))

                                        Spacer()
                                    }
                                    .padding(.horizontal, 20)
                                    .padding(.vertical, 16)
                                }
                                .buttonStyle(.plain)
                            }
                            .padding(.top, 8)
                            .padding(.bottom, 24)
                        }
                    }
                    .frame(width: 300)
                    .background(Color.jciDeepNavy)
                    .ignoresSafeArea(edges: .vertical)
                    .offset(x: isOpen ? 0 : -300)
                    .animation(.spring(response: 0.35, dampingFraction: 0.86), value: isOpen)
                    .gesture(
                        DragGesture()
                            .onEnded { value in
                                if value.translation.width < -50 {
                                    withAnimation(.spring(response: 0.35, dampingFraction: 0.86)) {
                                        isOpen = false
                                    }
                                }
                            }
                    )

                    Spacer()
                }
            }
        }
    }
}
