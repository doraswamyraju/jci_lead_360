import SwiftUI

/// Replicates the official Android NavigationBar from MainActivity.kt (lines 575-606)
/// White background, 8dp elevation, Royal Blue active tint with soft blue indicator pill
struct BottomNavigationBarView: View {
    @Binding var currentScreen: AppScreen

    var body: some View {
        HStack(spacing: 0) {
            ForEach(AppScreen.bottomNavItems, id: \.id) { screen in
                let isSelected = (currentScreen == screen)

                Button(action: {
                    currentScreen = screen
                }) {
                    VStack(spacing: 3) {
                        // Indicator pill
                        ZStack {
                            if isSelected {
                                RoundedRectangle(cornerRadius: 16)
                                    .fill(Color(hex: "E0F2FE"))
                                    .frame(width: 54, height: 28)
                            } else {
                                Color.clear
                                    .frame(width: 54, height: 28)
                            }

                            Text(screen.icon)
                                .font(.system(size: 18))
                        }

                        Text(screen.title)
                            .font(.system(size: 10, weight: isSelected ? .bold : .regular))
                            .foregroundColor(isSelected ? Color.jciRoyalBlue : Color(hex: "64748B"))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 6)
                }
                .buttonStyle(.plain)
            }
        }
        .frame(height: 56)
        .background(Color.white)
        .overlay(
            Rectangle()
                .fill(Color(hex: "E2E8F0"))
                .frame(height: 1),
            alignment: .top
        )
        .shadow(color: Color.black.opacity(0.06), radius: 6, x: 0, y: -2)
    }
}
