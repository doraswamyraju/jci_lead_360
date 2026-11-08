import SwiftUI

struct VendorStoreView: View {
    let chapterName: String
    let token: String?
    let onBack: () -> Void

    @State private var items: [CatalogItem] = []
    @State private var isLoading = true
    @State private var searchQuery = ""
    @State private var selectedItemForOrder: CatalogItem? = nil

    @State private var orderQuantity = 1
    @State private var orderNotes = ""
    @State private var orderPlacedSuccess = false

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

                Text("JCI Official Supplies & Store")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.jciTextDark)

                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)

            // Search Bar
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.jciTextMuted)
                TextField("Search banners, mementoes, collars, pins...", text: $searchQuery)
            }
            .padding(12)
            .background(Color.white)
            .cornerRadius(12)
            .padding(.horizontal, 20)
            .padding(.bottom, 12)

            if isLoading {
                Spacer()
                ProgressView("Fetching Official Catalog...")
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 14) {
                        ForEach(items) { item in
                            StoreItemCard(item: item) {
                                selectedItemForOrder = item
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 20)
                }
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
        .onAppear {
            loadCatalog()
        }
        .sheet(item: $selectedItemForOrder) { item in
            NavigationStack {
                VStack(spacing: 20) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(item.name)
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.jciTextDark)
                        Text(item.description)
                            .font(.system(size: 13))
                            .foregroundColor(.jciTextMuted)
                        Text("₹\(Int(item.price)) / \(item.unit)")
                            .font(.system(size: 18, weight: .black))
                            .foregroundColor(.jciRoyalBlue)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)

                    Stepper("Quantity: \(orderQuantity)", value: $orderQuantity, in: 1...100)
                        .padding()
                        .background(Color.jciBackgroundLight)
                        .cornerRadius(12)

                    TextField("Delivery notes or customizations...", text: $orderNotes)
                        .padding()
                        .background(Color.jciBackgroundLight)
                        .cornerRadius(12)

                    HStack {
                        Text("Total Amount:")
                            .font(.system(size: 14, weight: .semibold))
                        Spacer()
                        Text("₹\(Int(item.price * Double(orderQuantity)))")
                            .font(.system(size: 20, weight: .black))
                            .foregroundColor(.jciRoyalBlue)
                    }
                    .padding(.top, 10)

                    Button(action: submitOrder) {
                        Text("Confirm Order for \(chapterName)")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .background(JciGradients.button)
                            .cornerRadius(14)
                    }

                    Spacer()
                }
                .padding(20)
                .navigationTitle("Order Supplies")
                .navigationBarTitleDisplayMode(.inline)
            }
            .presentationDetents([.medium, .large])
        }
    }

    private func loadCatalog() {
        isLoading = true
        Task {
            do {
                let list = try await ApiService.shared.getCatalog()
                await MainActor.run {
                    self.items = list
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.isLoading = false
                }
            }
        }
    }

    private func submitOrder() {
        guard let token = token, let item = selectedItemForOrder else { return }
        let orderReq = PlaceOrderRequest(
            chapterName: chapterName,
            customerName: "President",
            phone: "+91 98765 43210",
            email: "president@jcitirupati.com",
            deliveryAddress: "Tirupati Chapter Secretariat",
            notes: orderNotes,
            items: [OrderItemRequest(itemId: item.id, name: item.name, quantity: orderQuantity, price: item.price)],
            totalAmount: item.price * Double(orderQuantity)
        )

        Task {
            do {
                _ = try await ApiService.shared.placeOrder(token: token, order: orderReq)
                await MainActor.run {
                    selectedItemForOrder = nil
                    orderQuantity = 1
                    orderNotes = ""
                }
            } catch {}
        }
    }
}

struct StoreItemCard: View {
    let item: CatalogItem
    let onOrder: () -> Void

    var body: some View {
        HStack(spacing: 14) {
            VStack(alignment: .leading, spacing: 6) {
                Text(item.category.uppercased())
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.jciSkyBlue)
                Text(item.name)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(.jciTextDark)
                Text(item.description)
                    .font(.system(size: 12))
                    .foregroundColor(.jciTextMuted)
                    .lineLimit(2)
                Text("₹\(Int(item.price)) / \(item.unit)")
                    .font(.system(size: 15, weight: .black))
                    .foregroundColor(.jciRoyalBlue)
            }

            Spacer()

            Button(action: onOrder) {
                Text("Order")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(Color.jciRoyalBlue)
                    .cornerRadius(10)
            }
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(18)
        .shadow(color: Color.black.opacity(0.03), radius: 6, x: 0, y: 3)
    }
}

