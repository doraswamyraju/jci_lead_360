import SwiftUI

struct AdminAppContainerView: View {
    let token: String
    let onSignOut: () -> Void

    @State private var selectedTab = 0
    let tabs = ["Vendors & Store", "Chapter Leaders", "Templates"]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Top Tab Bar
                Picker("Admin Tab", selection: $selectedTab) {
                    ForEach(0..<tabs.count, id: \.self) { idx in
                        Text(tabs[idx]).tag(idx)
                    }
                }
                .pickerStyle(.segmented)
                .padding()

                // Content
                if selectedTab == 0 {
                    AdminVendorsSubView(token: token)
                } else if selectedTab == 1 {
                    AdminUsersSubView(token: token)
                } else {
                    AdminTemplatesSubView()
                }
            }
            .navigationTitle("Super Admin Hub")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Sign Out", action: onSignOut)
                        .foregroundColor(.red)
                }
            }
        }
    }
}

struct AdminVendorsSubView: View {
    let token: String
    @State private var catalog: [CatalogItem] = []

    var body: some View {
        List(catalog) { item in
            VStack(alignment: .leading, spacing: 4) {
                Text(item.name).font(.system(size: 15, weight: .bold))
                Text("₹\(Int(item.price)) • \(item.category)").font(.system(size: 12)).foregroundColor(.jciRoyalBlue)
            }
        }
        .onAppear {
            Task {
                if let items = try? await ApiService.shared.getCatalog() {
                    await MainActor.run { self.catalog = items }
                }
            }
        }
    }
}

struct AdminUsersSubView: View {
    let token: String
    var body: some View {
        List {
            Text("Chapter Presidents & LGB Directory Database")
        }
    }
}

struct AdminTemplatesSubView: View {
    var body: some View {
        List {
            Text("20 Production Template Definitions Active")
        }
    }
}
