import SwiftUI

/// Official Interactive Calendar & 100% Efficiency Milestones
/// 100% Feature Parity with Android InteractiveCalendarScreen.kt
/// 31-day interactive monthly calendar grid, milestone inspect cards, and 1-tap flyer creation.
struct InteractiveCalendarView: View {
    var onBack: (() -> Void)? = nil
    var onNavigateToFlyer: (() -> Void)? = nil

    let currentMonthName = "January 2027"
    @State private var selectedDay: Int = 15

    let monthEvents: [Int: [CalendarEventItemDto]] = [
        15: [
            CalendarEventItemDto(
                day: 15,
                title: "1st LOM General Body Meeting & Board Meeting",
                category: "⭐ 100% Efficiency",
                fullDate: "Friday, 15th January 2027 • 6:30 PM",
                hasFlyer: true,
                hasInvitation: true,
                description: "Mandatory adoption of Chapter Plan of Action, Budget 2027, and committee appointments."
            )
        ],
        26: [
            CalendarEventItemDto(
                day: 26,
                title: "Republic Day Flag Hoisting & Community Outreach",
                category: "🌍 Special National Day",
                fullDate: "Tuesday, 26th January 2027 • 8:30 AM",
                hasFlyer: true,
                hasInvitation: false,
                description: "Flag salutation, fellowship breakfast, and community service project."
            )
        ],
        31: [
            CalendarEventItemDto(
                day: 31,
                title: "Submission of 1st Month Efficiency Report",
                category: "🏛️ Compliance Deadline",
                fullDate: "Sunday, 31st January 2027 • 11:59 PM",
                hasFlyer: false,
                hasInvitation: false,
                description: "Official reporting on JCI India national portal for 100% Efficiency points."
            )
        ]
    ]

    var selectedDayEvents: [CalendarEventItemDto] {
        monthEvents[selectedDay] ?? []
    }

    let columns = Array(repeating: GridItem(.flexible(), spacing: 6), count: 7)
    let daysOfWeek = ["M", "T", "W", "T", "F", "S", "S"]

    var body: some View {
        VStack(spacing: 0) {
            // Header Bar
            headerBarView

            ScrollView(showsIndicators: false) {
                VStack(spacing: 16) {
                    // Month Banner Card
                    monthBannerCard

                    // Interactive Monthly 31-Day Grid
                    monthlyGridCard

                    // Selected Day Milestone Inspector
                    selectedDayMilestoneSection

                    Spacer(minLength: 40)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 16)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
    }

    // MARK: - Header Bar
    private var headerBarView: some View {
        HStack {
            if let onBack = onBack {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.jciTextDark)
                        .padding(8)
                        .background(Color.white)
                        .clipShape(Circle())
                        .shadow(color: Color.black.opacity(0.06), radius: 4, x: 0, y: 2)
                }
            }

            VStack(alignment: .leading, spacing: 2) {
                Text("Chapter & National Calendar")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(.jciDeepNavy)

                Text("Official JCI 2027 Milestones & 100% Efficiency")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(.jciTextMuted)
            }

            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.white)
        .shadow(color: Color.black.opacity(0.04), radius: 4, y: 2)
    }

    // MARK: - Month Banner Card
    private var monthBannerCard: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text("📅 Interactive Year Calendar")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(Color(hex: "94A3B8"))

                Text(currentMonthName)
                    .font(.system(size: 20, weight: .heavy))
                    .foregroundColor(.white)
            }

            Spacer()

            Text("JCI 2027")
                .font(.system(size: 11, weight: .heavy))
                .foregroundColor(Color(hex: "38BDF8"))
                .padding(.horizontal, 10)
                .padding(.vertical, 5)
                .background(Color(hex: "0097D8").opacity(0.2))
                .cornerRadius(8)
        }
        .padding(18)
        .background(
            LinearGradient(
                colors: [Color(hex: "091322"), Color(hex: "132742")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.12), radius: 8, y: 4)
    }

    // MARK: - Monthly Grid Card
    private var monthlyGridCard: some View {
        VStack(spacing: 12) {
            // Days of week header
            HStack {
                ForEach(0..<daysOfWeek.count, id: \.self) { idx in
                    Text(daysOfWeek[idx])
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color(hex: "94A3B8"))
                        .frame(maxWidth: .infinity)
                }
            }
            .padding(.bottom, 4)

            Divider()

            // 1 to 31 Day Grid
            LazyVGrid(columns: columns, spacing: 10) {
                // Leading offset for Jan 1 2027 (Friday = index 4)
                ForEach(0..<4, id: \.self) { _ in
                    Text("")
                        .frame(width: 36, height: 36)
                }

                ForEach(1...31, id: \.self) { day in
                    let isSelected = selectedDay == day
                    let hasEvent = monthEvents[day] != nil

                    Button(action: {
                        withAnimation(.spring(response: 0.25, dampingFraction: 0.8)) {
                            selectedDay = day
                        }
                    }) {
                        ZStack(alignment: .bottom) {
                            Text("\(day)")
                                .font(.system(size: 13, weight: isSelected ? .heavy : (hasEvent ? .bold : .medium)))
                                .foregroundColor(isSelected ? .white : (hasEvent ? .jciRoyalBlue : .jciDeepNavy))
                                .frame(width: 36, height: 36)
                                .background(
                                    isSelected ? Color.jciRoyalBlue :
                                    (hasEvent ? Color(hex: "E0F2FE") : Color.clear)
                                )
                                .clipShape(Circle())
                                .overlay(
                                    Circle().stroke(isSelected ? Color.jciSkyBlue : (hasEvent ? Color(hex: "BAE6FD") : Color.clear), lineWidth: 1.5)
                                )

                            if hasEvent && !isSelected {
                                Circle()
                                    .fill(Color(hex: "F59E0B"))
                                    .frame(width: 4, height: 4)
                                    .offset(y: -2)
                            }
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(18)
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
        .shadow(color: Color.black.opacity(0.03), radius: 6, y: 2)
    }

    // MARK: - Selected Day Milestone Section
    private var selectedDayMilestoneSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Day \(selectedDay) Milestone & Details")
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(.jciDeepNavy)

            if selectedDayEvents.isEmpty {
                HStack(spacing: 12) {
                    Text("🍃")
                        .font(.system(size: 24))
                    VStack(alignment: .leading, spacing: 2) {
                        Text("No scheduled mandatory events for Day \(selectedDay)")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(.jciDeepNavy)
                        Text("Tap day 15, 26, or 31 to inspect official calendar milestones.")
                            .font(.system(size: 11))
                            .foregroundColor(.jciTextMuted)
                    }
                }
                .padding(16)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.white)
                .cornerRadius(16)
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
            } else {
                ForEach(selectedDayEvents) { event in
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Text(event.category)
                                .font(.system(size: 10, weight: .heavy))
                                .foregroundColor(Color(hex: "92400E"))
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(Color(hex: "FEF3C7"))
                                .cornerRadius(6)

                            Spacer()

                            Text("Day \(event.day)")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(Color(hex: "64748B"))
                        }

                        Text(event.title)
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.jciDeepNavy)

                        Text(event.fullDate)
                            .font(.system(size: 12, weight: .semibold))
                            .foregroundColor(Color(hex: "0284C7"))

                        Text(event.description)
                            .font(.system(size: 12))
                            .foregroundColor(.jciTextMuted)
                            .lineSpacing(2)

                        Divider()

                        HStack(spacing: 10) {
                            if event.hasFlyer {
                                Button(action: { onNavigateToFlyer?() }) {
                                    HStack(spacing: 6) {
                                        Text("⚡")
                                        Text("Open 1-Tap Flyer")
                                            .font(.system(size: 12, weight: .bold))
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 10)
                                    .background(Color.jciRoyalBlue)
                                    .foregroundColor(.white)
                                    .cornerRadius(8)
                                }
                            }

                            if event.hasInvitation {
                                Button(action: { onNavigateToFlyer?() }) {
                                    HStack(spacing: 6) {
                                        Text("💌")
                                        Text("Invitation")
                                            .font(.system(size: 12, weight: .bold))
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 10)
                                    .background(Color(hex: "10B981"))
                                    .foregroundColor(.white)
                                    .cornerRadius(8)
                                }
                            }
                        }
                    }
                    .padding(16)
                    .background(Color.white)
                    .cornerRadius(16)
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
                    .shadow(color: Color.black.opacity(0.04), radius: 6, y: 2)
                }
            }
        }
    }
}

struct CalendarEventItemDto: Identifiable {
    var id: String { "\(day)_\(title)" }
    let day: Int
    let title: String
    let category: String
    let fullDate: String
    let hasFlyer: Bool
    let hasInvitation: Bool
    let description: String
}

