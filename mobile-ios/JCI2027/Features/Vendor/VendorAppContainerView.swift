import SwiftUI

struct VendorAppContainerView: View {
    let onExitVendorMode: () -> Void
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationStack {
                VStack(spacing: 20) {
                    Text("Official Partner Portal")
                        .font(.system(size: 20, weight: .bold))
                    Text("Manage orders, quotes, and fulfillment for JCI chapters.")
                        .font(.system(size: 13))
                        .foregroundColor(.jciTextMuted)
                    Spacer()
                }
                .padding()
                .navigationTitle("Vendor Home")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Exit", action: onExitVendorMode)
                    }
                }
            }
            .tabItem {
                Label("Home", systemImage: "house.fill")
            }
            .tag(0)

            NavigationStack {
                List {
                    Text("Crystal Mementoes (28 SKU)")
                    Text("Banners & Rollup Standees (16 SKU)")
                    Text("Brochures & Event Flyers (12 SKU)")
                }
                .navigationTitle("Catalog")
            }
            .tabItem {
                Label("Catalog", systemImage: "cube.box.fill")
            }
            .tag(1)

            NavigationStack {
                List {
                    Text("Order #10248 • JCI Tirupati Innovations (₹48,500)")
                }
                .navigationTitle("Orders")
            }
            .tabItem {
                Label("Orders", systemImage: "cart.fill")
            }
            .tag(2)
        }
    }
}

