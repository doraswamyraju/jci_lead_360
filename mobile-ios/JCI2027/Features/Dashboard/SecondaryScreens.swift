import SwiftUI

// MARK: - LO Website Builder Screen
struct ChapterWebsiteBuilderView: View {
    let chapterName: String
    let presidentName: String
    let onBack: () -> Void

    @State private var headline = "Welcome to JCI Tirupati Innovations"
    @State private var aboutMessage = "Dedicated to leadership development, active citizenship, and impactful community projects throughout 2027."
    @State private var copiedLink = false

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                HStack {
                    Button(action: onBack) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.jciTextDark)
                            .padding(8)
                            .background(Color.white)
                            .clipShape(Circle())
                    }
                    Text("LO Public Website Portal")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.jciTextDark)
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.top, 10)

                VStack(alignment: .leading, spacing: 14) {
                    Text("Live Web Link")
                        .font(.system(size: 14, weight: .bold))
                    HStack {
                        Text("https://lead.sriddha.com/lo/tirupati")
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(.jciRoyalBlue)
                        Spacer()
                        Button(action: {
                            UIPasteboard.general.string = "https://lead.sriddha.com/lo/tirupati"
                            copiedLink = true
                        }) {
                            Text(copiedLink ? "Copied!" : "Copy")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.white)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(Color.jciRoyalBlue)
                                .cornerRadius(8)
                        }
                    }
                    .padding(12)
                    .background(Color.jciBackgroundLight)
                    .cornerRadius(10)
                }
                .padding(20)
                .background(Color.white)
                .cornerRadius(18)
                .padding(.horizontal, 20)

                VStack(alignment: .leading, spacing: 14) {
                    Text("Configure Homepage Content")
                        .font(.system(size: 16, weight: .bold))
                    CustomStudioField(label: "Banner Headline", text: $headline)
                    CustomStudioField(label: "President's Welcome Note", text: $aboutMessage)
                }
                .padding(20)
                .background(Color.white)
                .cornerRadius(18)
                .padding(.horizontal, 20)

                Spacer(minLength: 30)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
    }
}

// MARK: - JCI Resources Screen
struct JciResourcesView: View {
    let chapterName: String
    let onBack: () -> Void

    let resources = [
        ("JCI Constitution & Policy 2027", "Official governance & bylaws document", "doc.text.fill"),
        ("Chapter Installation Protocol", "Stage management & gavel handover guidelines", "flag.fill"),
        ("JCI Brand & Logo Standards", "Shield colors, vector logos, typography rules", "paintpalette.fill"),
        ("Parliamentary Procedures (Robert's Rules)", "Meeting management & motions reference", "book.closed.fill"),
        ("Award Bidding Templates", "Templates for Zone & National recognitions", "trophy.fill")
    ]

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                HStack {
                    Button(action: onBack) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.jciTextDark)
                            .padding(8)
                            .background(Color.white)
                            .clipShape(Circle())
                    }
                    Text("JCI Resources & Protocol")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.jciTextDark)
                    Spacer()
                }
                .padding(.horizontal, 20)
                .padding(.top, 10)

                VStack(spacing: 12) {
                    ForEach(resources, id: \.0) { item in
                        HStack(spacing: 14) {
                            Image(systemName: item.2)
                                .font(.system(size: 22))
                                .foregroundColor(.jciRoyalBlue)
                                .frame(width: 44, height: 44)
                                .background(Color.jciRoyalBlue.opacity(0.1))
                                .cornerRadius(12)

                            VStack(alignment: .leading, spacing: 2) {
                                Text(item.0)
                                    .font(.system(size: 14, weight: .bold))
                                    .foregroundColor(.jciTextDark)
                                Text(item.1)
                                    .font(.system(size: 12))
                                    .foregroundColor(.jciTextMuted)
                            }
                            Spacer()
                            Image(systemName: "arrow.down.circle.fill")
                                .font(.system(size: 20))
                                .foregroundColor(.jciSkyBlue)
                        }
                        .padding(16)
                        .background(Color.white)
                        .cornerRadius(16)
                        .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
                    }
                }
                .padding(.horizontal, 20)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
    }
}

// MARK: - Interactive Calendar Screen
struct InteractiveCalendarView: View {
    let onNavigateToFlyer: () -> Void

    let events = [
        ("Jan 24, 2027", "Chapter Installation 2027", "Tirupati Grand", "Local Event"),
        ("Feb 14, 2027", "Zone Conference Mid-Term", "Bangalore Conclave", "Zone Event"),
        ("Mar 08, 2027", "International Women's Day Conclave", "Community Center", "Community"),
        ("Apr 18, 2027", "National Academy (NATCON 2027)", "Goa Convention Centre", "National")
    ]

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text("Chapter & National Calendar")
                    .font(.system(size: 20, weight: .bold))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)
                    .padding(.top, 10)

                VStack(spacing: 12) {
                    ForEach(events, id: \.1) { ev in
                        VStack(alignment: .leading, spacing: 10) {
                            HStack {
                                Text(ev.3.uppercased())
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(.jciSkyBlue)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(Color.jciSkyBlue.opacity(0.1))
                                    .cornerRadius(6)
                                Spacer()
                                Text(ev.0)
                                    .font(.system(size: 12, weight: .semibold))
                                    .foregroundColor(.jciTextMuted)
                            }

                            Text(ev.1)
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.jciTextDark)

                            HStack {
                                Label(ev.2, systemImage: "mappin.circle")
                                    .font(.system(size: 12))
                                    .foregroundColor(.jciTextMuted)
                                Spacer()
                                Button(action: onNavigateToFlyer) {
                                    Text("Make Flyer")
                                        .font(.system(size: 11, weight: .bold))
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 5)
                                        .background(Color.jciRoyalBlue)
                                        .cornerRadius(8)
                                }
                            }
                        }
                        .padding(16)
                        .background(Color.white)
                        .cornerRadius(16)
                        .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 2)
                    }
                }
                .padding(.horizontal, 20)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
    }
}

// MARK: - LGB & PST Management Screen
struct LgbAndPstManagementView: View {
    let token: String
    let chapterName: String
    let presidentName: String

    let officers = [
        ("JC HGF Doraswamy Raju", "President", "+91 98765 43210"),
        ("JC Suresh Kumar", "Secretary", "+91 98490 11223"),
        ("JC Anita Rao", "Treasurer", "+91 94401 55667"),
        ("JC Sen. Ramesh Babu", "Past President (IPP)", "+91 98850 99887"),
        ("JC Manoj Varma", "Vice President (Management)", "+91 99081 22334")
    ]

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text("LGB & Past Presidents Directory")
                    .font(.system(size: 20, weight: .bold))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)
                    .padding(.top, 10)

                VStack(spacing: 12) {
                    ForEach(officers, id: \.0) { off in
                        HStack(spacing: 14) {
                            ZStack {
                                Circle().fill(JciGradients.hero).frame(width: 44, height: 44)
                                Text(String(off.0.prefix(1))).font(.system(size: 16, weight: .bold)).foregroundColor(.white)
                            }
                            VStack(alignment: .leading, spacing: 2) {
                                Text(off.0).font(.system(size: 15, weight: .bold))
                                Text(off.1).font(.system(size: 12, weight: .semibold)).foregroundColor(.jciRoyalBlue)
                                Text(off.2).font(.system(size: 11)).foregroundColor(.jciTextMuted)
                            }
                            Spacer()
                            Button(action: {
                                if let url = URL(string: "tel:\(off.2.replacingOccurrences(of: " ", with: ""))") {
                                    UIApplication.shared.open(url)
                                }
                            }) {
                                Image(systemName: "phone.circle.fill")
                                    .font(.system(size: 28))
                                    .foregroundColor(.jciShieldTeal)
                            }
                        }
                        .padding(14)
                        .background(Color.white)
                        .cornerRadius(16)
                    }
                }
                .padding(.horizontal, 20)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
    }
}

// MARK: - Photo Frames Screen
struct FramesView: View {
    var body: some View {
        VStack(spacing: 20) {
            Text("JCI Official Photo Frames")
                .font(.system(size: 20, weight: .bold))
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 20)
                .padding(.top, 10)

            VStack(spacing: 12) {
                Image(systemName: "photo.artframe")
                    .font(.system(size: 60))
                    .foregroundColor(.jciSkyBlue)
                Text("Official 2027 Frames Available")
                    .font(.system(size: 16, weight: .bold))
                Text("Fit and export official JCI ribbons and officer insignia directly over portrait photos.")
                    .font(.system(size: 13))
                    .foregroundColor(.jciTextMuted)
                    .multilineTextAlignment(.center)
            }
            .padding(30)
            .background(Color.white)
            .cornerRadius(20)
            .padding(.horizontal, 20)

            Spacer()
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
    }
}

// MARK: - Profile Screen
struct ProfileView: View {
    let name: String
    let chapterName: String
    let role: String
    let onLogout: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                VStack(spacing: 10) {
                    ZStack {
                        Circle().fill(JciGradients.hero).frame(width: 80, height: 80)
                        Text(String(name.prefix(1))).font(.system(size: 32, weight: .black)).foregroundColor(.white)
                    }
                    Text(name).font(.system(size: 22, weight: .bold))
                    Text("\(role) • \(chapterName)")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(.jciRoyalBlue)
                }
                .padding(.top, 20)

                VStack(spacing: 12) {
                    Button(action: onLogout) {
                        HStack {
                            Image(systemName: "arrow.right.square.fill")
                            Text("Sign Out")
                        }
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity)
                        .frame(height: 50)
                        .background(Color.red.opacity(0.1))
                        .cornerRadius(14)
                    }
                }
                .padding(20)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
    }
}

