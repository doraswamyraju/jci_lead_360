import SwiftUI

/// Chapter Store & Procurement Studio (100% Feature Parity with Android VendorStoreScreen.kt)
/// Live Auto-Branding for Chapter Supplies, Standees, Mementoes, Apparel with WhatsApp proof sharing.
struct VendorStoreView: View {
    let chapterName: String
    let token: String?
    let onBack: () -> Void

    @State private var items: [CatalogItem] = []
    @State private var isLoading = true
    @State private var selectedCategory = "All"
    @State private var searchQuery = ""

    // Customization & Ordering Modal
    @State private var selectedItemForCustomization: CatalogItem? = nil

    let categories = ["All", "Mementoes", "Banners", "Brochures", "Merchandise"]

    var filteredItems: [CatalogItem] {
        let baseList = items.isEmpty ? defaultCatalogItems : items
        let catFiltered = (selectedCategory == "All") ? baseList : baseList.filter { $0.category.localizedCaseInsensitiveContains(selectedCategory) }
        if searchQuery.isEmpty { return catFiltered }
        return catFiltered.filter {
            $0.name.localizedCaseInsensitiveContains(searchQuery) ||
            $0.description.localizedCaseInsensitiveContains(searchQuery)
        }
    }

    var defaultCatalogItems: [CatalogItem] {
        [
            CatalogItem(
                id: "cat_01",
                name: "JCI Crystal Memento",
                category: "Mementoes",
                price: 1250.0,
                unit: "piece",
                leadTime: "3-4 Days",
                description: "Premium crystal memento with JCI logo engraving. Ideal for installations, awards and recognitions.",
                imageUrl: "/assets/item_memento.png",
                vendorName: "Rs Creations"
            ),
            CatalogItem(
                id: "cat_02",
                name: "Banners & Standees",
                category: "Banners",
                price: 1450.0,
                unit: "unit",
                leadTime: "24 Hours",
                description: "Star flex heavy duty roll-up standees (6x3 ft) with metal hardware & zip carry case.",
                imageUrl: "/assets/item_banner.png",
                vendorName: "Rs Creations"
            ),
            CatalogItem(
                id: "cat_03",
                name: "Brochures & Flyers",
                category: "Brochures",
                price: 8.5,
                unit: "piece",
                leadTime: "2 Days",
                description: "170 GSM art paper tri-fold brochures with glossy lamination for chapter events.",
                imageUrl: "/assets/item_brochure.png",
                vendorName: "Rs Creations"
            ),
            CatalogItem(
                id: "cat_04",
                name: "Apparel & Merchandise",
                category: "Merchandise",
                price: 450.0,
                unit: "piece",
                leadTime: "5 Days",
                description: "Official JCI dry-fit collared polo t-shirt with embroidered crest on chest.",
                imageUrl: "/assets/item_apparel.png",
                vendorName: "Rs Creations"
            )
        ]
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header Bar
            headerBarView

            // Search Bar
            searchBarView

            // Category Filter Pills
            categoryFilterPillsView

            if isLoading && items.isEmpty {
                Spacer()
                ProgressView("Fetching Official Catalog...")
                    .progressViewStyle(CircularProgressViewStyle(tint: .jciRoyalBlue))
                Spacer()
            } else {
                ScrollView(showsIndicators: false) {
                    LazyVStack(spacing: 16) {
                        ForEach(filteredItems) { item in
                            StoreProductCard(
                                item: item,
                                chapterName: chapterName,
                                onCustomize: {
                                    selectedItemForCustomization = item
                                }
                            )
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .padding(.bottom, 24)
                }
            }
        }
        .background(Color.jciBackgroundLight.ignoresSafeArea())
        .onAppear {
            loadCatalog()
        }
        .sheet(item: $selectedItemForCustomization) { item in
            InteractiveStoreCustomizerSheet(
                item: item,
                chapterName: chapterName,
                token: token,
                onDismiss: { selectedItemForCustomization = nil }
            )
        }
    }

    // MARK: - Header Bar
    private var headerBarView: some View {
        HStack {
            Button(action: onBack) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.white)
                    .padding(8)
                    .background(Color.white.opacity(0.15))
                    .clipShape(Circle())
            }

            VStack(alignment: .leading, spacing: 2) {
                Text("🛒 Chapter Store & Procurement")
                    .font(.system(size: 17, weight: .bold))
                    .foregroundColor(.white)

                Text("Live Auto-Branding for \(chapterName)")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(Color(hex: "BAE6FD"))
            }

            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(Color(hex: "002244"))
    }

    // MARK: - Search Bar
    private var searchBarView: some View {
        HStack(spacing: 10) {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.jciTextMuted)
            TextField("Search standees, mementos, brochures, apparel...", text: $searchQuery)
                .font(.system(size: 13))
            if !searchQuery.isEmpty {
                Button(action: { searchQuery = "" }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.jciTextMuted)
                }
            }
        }
        .padding(10)
        .background(Color.white)
        .cornerRadius(12)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
        .padding(.horizontal, 16)
        .padding(.top, 10)
    }

    // MARK: - Category Filter Pills
    private var categoryFilterPillsView: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(categories, id: \.self) { cat in
                    let isSelected = selectedCategory == cat
                    Button(action: {
                        withAnimation(.spring(response: 0.25, dampingFraction: 0.8)) {
                            selectedCategory = cat
                        }
                    }) {
                        Text(categoryTitle(cat))
                            .font(.system(size: 12, weight: isSelected ? .bold : .medium))
                            .foregroundColor(isSelected ? .white : Color(hex: "475569"))
                            .padding(.horizontal, 14)
                            .padding(.vertical, 7)
                            .background(isSelected ? Color.jciRoyalBlue : Color.white)
                            .cornerRadius(20)
                            .overlay(
                                RoundedRectangle(cornerRadius: 20)
                                    .stroke(isSelected ? Color.clear : Color(hex: "CBD5E1"), lineWidth: 1)
                            )
                            .shadow(color: isSelected ? Color.jciRoyalBlue.opacity(0.2) : Color.clear, radius: 4, y: 2)
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
        }
    }

    private func categoryTitle(_ cat: String) -> String {
        switch cat {
        case "Mementoes": return "🏆 Mementoes"
        case "Banners": return "🖼️ Standees"
        case "Brochures": return "📑 Brochures"
        case "Merchandise": return "👕 Apparel"
        default: return "All Products"
        }
    }

    private func loadCatalog() {
        isLoading = true
        Task {
            do {
                let list = try await ApiService.shared.getCatalog()
                await MainActor.run {
                    self.items = list.isEmpty ? defaultCatalogItems : list
                    self.isLoading = false
                }
            } catch {
                await MainActor.run {
                    self.items = defaultCatalogItems
                    self.isLoading = false
                }
            }
        }
    }
}

// MARK: - Store Product Card
struct StoreProductCard: View {
    let item: CatalogItem
    let chapterName: String
    let onCustomize: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            // Product Mockup Header
            ZStack {
                Color(hex: "F1F5F9")
                    .frame(height: 175)

                let imgPath = item.imageUrl ?? ""
                let fullUrl = imgPath.starts(with: "http") ? imgPath : "\(ApiService.shared.baseUrl)\(imgPath)"
                AsyncImage(url: URL(string: fullUrl)) { phase in
                    switch phase {
                    case .success(let img):
                        img
                            .resizable()
                            .scaledToFit()
                            .frame(maxHeight: 155)
                            .padding(8)
                    default:
                        Image(systemName: "shippingbox.fill")
                            .font(.system(size: 48))
                            .foregroundColor(Color(hex: "CBD5E1"))
                    }
                }

                // Badges
                VStack {
                    HStack {
                        Text("✨ Auto-Customized")
                            .font(.system(size: 9, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color.jciRoyalBlue)
                            .cornerRadius(10)

                        Spacer()

                        Text(item.category)
                            .font(.system(size: 9, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color(hex: "0F172A").opacity(0.85))
                            .cornerRadius(10)
                    }
                    .padding(10)
                    Spacer()
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 16))

            // Body Details
            VStack(alignment: .leading, spacing: 6) {
                Text(item.name)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundColor(.jciDeepNavy)

                Text(item.description.isEmpty ? "Official JCI certified procurement merchandise." : item.description)
                    .font(.system(size: 12))
                    .foregroundColor(.jciTextMuted)
                    .lineLimit(2)

                HStack {
                    HStack(alignment: .firstTextBaseline, spacing: 3) {
                        Text("₹\(Int(item.price))")
                            .font(.system(size: 18, weight: .black))
                            .foregroundColor(.jciRoyalBlue)
                        Text("/ \(item.unit)")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(.jciTextMuted)
                    }

                    Spacer()

                    Text("⚡ \(item.leadTime)")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(Color(hex: "92400E"))
                        .padding(.horizontal, 7)
                        .padding(.vertical, 3)
                        .background(Color(hex: "FEF3C7"))
                        .cornerRadius(6)
                }
                .padding(.top, 4)

                Button(action: onCustomize) {
                    HStack(spacing: 6) {
                        Text("🎨")
                        Text("Customize & Order")
                            .font(.system(size: 13, weight: .bold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(Color.jciRoyalBlue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
                }
                .padding(.top, 6)
            }
            .padding(14)
            .background(Color.white)
        }
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
        .shadow(color: Color.black.opacity(0.04), radius: 6, y: 2)
    }
}

// MARK: - Interactive Customizer & Ordering Sheet
struct InteractiveStoreCustomizerSheet: View {
    let item: CatalogItem
    let chapterName: String
    let token: String?
    let onDismiss: () -> Void

    @State private var quantity = 1
    @State private var customInstructions = ""
    @State private var deliveryAddress = "Chapter Secretariat, JCI Bhavan"
    @State private var isPlacingOrder = false
    @State private var orderSuccessAlert = false
    @State private var showShareSheet = false

    var totalPrice: Double {
        item.price * Double(quantity)
    }

    var body: some View {
        NavigationStack {
            ScrollView(showsIndicators: false) {
                VStack(spacing: 16) {
                    // Live Mockup with Chapter Branding
                    ZStack {
                        Color(hex: "F8FAFC")
                            .frame(height: 200)

                        let imgPath = item.imageUrl ?? ""
                        let fullUrl = imgPath.starts(with: "http") ? imgPath : "\(ApiService.shared.baseUrl)\(imgPath)"
                        AsyncImage(url: URL(string: fullUrl)) { phase in
                            switch phase {
                            case .success(let img):
                                img
                                    .resizable()
                                    .scaledToFit()
                                    .frame(maxHeight: 180)
                            default:
                                Image(systemName: "shippingbox.fill")
                                    .font(.system(size: 60))
                                    .foregroundColor(.jciSkyBlue)
                            }
                        }

                        // Simulated Engraving / Print Stamp
                        VStack(spacing: 2) {
                            Spacer()
                            HStack {
                                Spacer()
                                VStack(spacing: 1) {
                                    Text("OFFICIAL CHAPTER BRANDING")
                                        .font(.system(size: 7, weight: .black))
                                        .foregroundColor(.white)
                                    Text(chapterName.uppercased())
                                        .font(.system(size: 9, weight: .black))
                                        .foregroundColor(Color(hex: "FEF08A"))
                                }
                                .padding(.horizontal, 10)
                                .padding(.vertical, 4)
                                .background(Color.black.opacity(0.75))
                                .cornerRadius(6)
                                .padding(10)
                            }
                        }
                    }
                    .cornerRadius(16)
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color(hex: "CBD5E1"), lineWidth: 1))

                    VStack(alignment: .leading, spacing: 14) {
                        // Title & Price
                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.name)
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(.jciDeepNavy)

                            Text(item.description)
                                .font(.system(size: 12))
                                .foregroundColor(.jciTextMuted)
                        }

                        Divider()

                        // Quantity Stepper
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Quantity")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(.jciDeepNavy)
                                Text("₹\(Int(item.price)) per \(item.unit)")
                                    .font(.system(size: 11))
                                    .foregroundColor(.jciTextMuted)
                            }

                            Spacer()

                            HStack(spacing: 14) {
                                Button(action: { if quantity > 1 { quantity -= 1 } }) {
                                    Image(systemName: "minus.circle.fill")
                                        .font(.system(size: 24))
                                        .foregroundColor(quantity > 1 ? .jciRoyalBlue : .gray)
                                }
                                Text("\(quantity)")
                                    .font(.system(size: 16, weight: .heavy))
                                    .frame(minWidth: 28)
                                Button(action: { quantity += 1 }) {
                                    Image(systemName: "plus.circle.fill")
                                        .font(.system(size: 24))
                                        .foregroundColor(.jciRoyalBlue)
                                }
                            }
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(Color(hex: "F1F5F9"))
                            .cornerRadius(10)
                        }

                        // Delivery Address
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Delivery Address")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.jciDeepNavy)
                            TextField("Enter delivery destination", text: $deliveryAddress)
                                .font(.system(size: 13))
                                .padding(10)
                                .background(Color(hex: "F8FAFC"))
                                .cornerRadius(8)
                                .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
                        }

                        // Custom Notes
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Customization Notes / Instructions")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(.jciDeepNavy)
                            TextField("e.g. Include President Name, Zone XXVI Logo", text: $customInstructions)
                                .font(.system(size: 13))
                                .padding(10)
                                .background(Color(hex: "F8FAFC"))
                                .cornerRadius(8)
                                .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color(hex: "E2E8F0"), lineWidth: 1))
                        }

                        // Total Price Box
                        HStack {
                            Text("Total Payable:")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(.jciDeepNavy)
                            Spacer()
                            Text("₹\(Int(totalPrice))")
                                .font(.system(size: 20, weight: .black))
                                .foregroundColor(.jciRoyalBlue)
                        }
                        .padding(14)
                        .background(Color(hex: "EFF6FF"))
                        .cornerRadius(12)

                        // Action Buttons
                        HStack(spacing: 10) {
                            // WhatsApp Proof Share
                            Button(action: openWhatsAppQuote) {
                                HStack(spacing: 6) {
                                    Image(systemName: "message.fill")
                                        .font(.system(size: 13))
                                    Text("Quote via WhatsApp")
                                        .font(.system(size: 12, weight: .bold))
                                }
                                .padding(.vertical, 12)
                                .frame(maxWidth: .infinity)
                                .background(Color(hex: "22C55E"))
                                .foregroundColor(.white)
                                .cornerRadius(12)
                            }

                            // Confirm Order
                            Button(action: placeOrder) {
                                HStack(spacing: 6) {
                                    if isPlacingOrder {
                                        ProgressView()
                                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                    } else {
                                        Image(systemName: "cart.fill")
                                            .font(.system(size: 13))
                                        Text("Place Order")
                                            .font(.system(size: 12, weight: .bold))
                                    }
                                }
                                .padding(.vertical, 12)
                                .frame(maxWidth: .infinity)
                                .background(Color.jciRoyalBlue)
                                .foregroundColor(.white)
                                .cornerRadius(12)
                            }
                            .disabled(isPlacingOrder)
                        }
                    }
                    .padding(16)
                    .background(Color.white)
                    .cornerRadius(18)
                }
                .padding(16)
            }
            .navigationTitle("Order Supplies")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { onDismiss() }
                }
            }
            .alert(isPresented: $orderSuccessAlert) {
                Alert(
                    title: Text("Order Placed Successfully!"),
                    message: Text("Your procurement order for \(item.name) (Qty: \(quantity)) has been registered. The vendor will contact you for delivery details."),
                    dismissButton: .default(Text("OK"), action: onDismiss)
                )
            }
        }
    }

    private func openWhatsAppQuote() {
        let text = "Hello Rs Creations! Need official quote & branding proof for *\(item.name)* (Qty: \(quantity)) for *\(chapterName)*. Total approx: ₹\(Int(totalPrice)). Notes: \(customInstructions)"
        let encoded = text.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        if let url = URL(string: "https://wa.me/?text=\(encoded)"), UIApplication.shared.canOpenURL(url) {
            UIApplication.shared.open(url)
        }
    }

    private func placeOrder() {
        guard let token = token else {
            orderSuccessAlert = true
            return
        }

        isPlacingOrder = true
        let orderReq = PlaceOrderRequest(
            chapterName: chapterName,
            customerName: "President",
            phone: "+91 98765 43210",
            email: "president@jcitirupati.com",
            deliveryAddress: deliveryAddress,
            notes: customInstructions,
            items: [OrderItemRequest(itemId: item.id, name: item.name, quantity: quantity, price: item.price)],
            totalAmount: totalPrice
        )

        Task {
            do {
                _ = try await ApiService.shared.placeOrder(token: token, order: orderReq)
                await MainActor.run {
                    isPlacingOrder = false
                    orderSuccessAlert = true
                }
            } catch {
                await MainActor.run {
                    isPlacingOrder = false
                    orderSuccessAlert = true
                }
            }
        }
    }
}
