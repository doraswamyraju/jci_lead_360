import SwiftUI

struct ProjectsAndTasksView: View {
    let chapterName: String
    let onBack: () -> Void

    @State private var selectedFilter = "All"
    let categories = ["All", "Community", "Business", "Individual", "International"]

    @State private var projects = [
        ProjectItem(id: "p1", title: "Installation Ceremony 2027", category: "Individual", progress: 0.85, dueDate: "15 Jan 2027", status: "In Progress"),
        ProjectItem(id: "p2", title: "Youth Leadership Conclave", category: "Community", progress: 0.40, dueDate: "28 Feb 2027", status: "Planning"),
        ProjectItem(id: "p3", title: "B2B Chapter Networking Expo", category: "Business", progress: 0.65, dueDate: "12 Mar 2027", status: "In Progress"),
        ProjectItem(id: "p4", title: "Global Sister Chapter Summit", category: "International", progress: 0.15, dueDate: "25 Apr 2027", status: "Initiated")
    ]

    var filteredProjects: [ProjectItem] {
        if selectedFilter == "All" { return projects }
        return projects.filter { $0.category == selectedFilter }
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.jciTextDark)
                        .padding(8)
                        .background(Color.white)
                        .clipShape(Circle())
                }

                Text("Projects & Chapter KPIs")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.jciTextDark)

                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)

            // Category Filter Scroll
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(categories, id: \.self) { cat in
                        Button(action: { selectedFilter = cat }) {
                            Text(cat)
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(selectedFilter == cat ? .white : .jciTextDark)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(selectedFilter == cat ? Color.jciRoyalBlue : Color.white)
                                .cornerRadius(20)
                                .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
            }

            // Projects List
            ScrollView {
                VStack(spacing: 14) {
                    ForEach(filteredProjects) { item in
                        VStack(alignment: .leading, spacing: 12) {
                            HStack {
                                Text(item.category.uppercased())
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(.jciRoyalBlue)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(Color.jciRoyalBlue.opacity(0.1))
                                    .cornerRadius(6)

                                Spacer()

                                Text(item.dueDate)
                                    .font(.system(size: 12, weight: .medium))
                                    .foregroundColor(.jciTextMuted)
                            }

                            Text(item.title)
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.jciTextDark)

                            VStack(alignment: .leading, spacing: 6) {
                                HStack {
                                    Text("Execution Progress")
                                        .font(.system(size: 11, weight: .medium))
                                        .foregroundColor(.jciTextMuted)
                                    Spacer()
                                    Text("\(Int(item.progress * 100))%")
                                        .font(.system(size: 11, weight: .bold))
                                        .foregroundColor(.jciRoyalBlue)
                                }

                                GeometryReader { geo in
                                    ZStack(alignment: .leading) {
                                        RoundedRectangle(cornerRadius: 4)
                                            .fill(Color.jciBorderSubtle)
                                            .frame(height: 8)

                                        RoundedRectangle(cornerRadius: 4)
                                            .fill(JciGradients.button)
                                            .frame(width: geo.size.width * CGFloat(item.progress), height: 8)
                                    }
                                }
                                .frame(height: 8)
                            }
                        }
                        .padding(18)
                        .background(Color.white)
                        .cornerRadius(18)
                        .shadow(color: Color.black.opacity(0.04), radius: 8, x: 0, y: 3)
                    }
                }
                .padding(20)
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
    }
}

struct ProjectItem: Identifiable {
    let id: String
    let title: String
    let category: String
    let progress: Double
    let dueDate: String
    let status: String
}
