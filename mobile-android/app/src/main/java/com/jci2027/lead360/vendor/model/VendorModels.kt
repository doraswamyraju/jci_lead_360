package com.jci2027.lead360.vendor.model

data class VendorProfile(
    val vendorId: String = "usr_vnd_01",
    val businessName: String = "Rs Creations",
    val isApproved: Boolean = true,
    val phone: String = "+91 98765 43210",
    val email: String = "vendor@lead360.com",
    val serviceAreas: String = "Andhra Pradesh & Telangana",
    val gstin: String = "37AAAAA0000A1Z5",
    val logoUrl: String? = null,
    val bankDetails: BankDetails? = null
)

data class BankDetails(
    val holder: String = "",
    val accountNumber: String = "",
    val bankName: String = "",
    val ifsc: String = ""
)

data class VendorKpiSummary(
    val newRfqsCount: Int = 12,
    val ordersCount: Int = 8,
    val inProductionCount: Int = 3,
    val monthlyEarnings: String = "₹2,48,500",
    val completedAmount: String = "₹1,85,000",
    val inProgressAmount: String = "₹52,500",
    val pendingAmount: String = "₹11,000"
)

data class ProductDimension(
    val width: Int = 800,
    val height: Int = 800
)

data class ProductLayer(
    val id: String = "",
    val type: String = "custom_text",
    val label: String = "",
    val x: Int = 200,
    val y: Int = 200,
    val width: Int = 300,
    val height: Int = 40,
    val color: String = "#ffffff",
    val fontSize: Int = 20,
    val fontFamily: String = "'Outfit', 'Inter', sans-serif",
    val bold: Boolean = true,
    val italic: Boolean = false,
    val uppercase: Boolean = false,
    val backgroundColor: String = "transparent",
    val align: String = "center",
    val defaultText: String = ""
)

data class CatalogItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val unit: String = "piece",
    val description: String = "",
    val imageUrl: String = "",
    val productCountInCat: Int = 0,
    val leadTime: String = "3-4 Days",
    val vendorName: String = "Rs Creations",
    val vendorId: String? = "usr_vnd_01",
    val customizable: Boolean = true,
    val dimensions: ProductDimension? = null,
    val layers: List<ProductLayer> = emptyList()
)

data class CatalogResponse(
    val success: Boolean,
    val items: List<CatalogItem>
)

data class RfqItem(
    val id: String,
    val title: String,
    val chapterName: String,
    val eventDate: String,
    val status: String = "Open", // Open, Quoted, Closed
    val isNew: Boolean = true,
    val emoji: String = "🏆",
    val description: String = "",
    val itemsSummary: String = "",
    val quotedAmount: Double? = null,
    val createdAt: String? = null
)

data class RfqsResponse(
    val success: Boolean,
    val rfqs: List<RfqItem>
)

data class OrderAsset(
    val name: String,
    val url: String,
    val size: String
)

data class VendorOrder(
    val orderId: String = "",
    val id: String = "",
    val title: String = "",
    val chapterName: String = "",
    val orderDate: String = "",
    val amount: Double = 0.0,
    val status: String = "In Production", // Confirmed, In Production, Ready, Delivered
    val itemsSummary: String = "",
    val confirmedDate: String = "10 Jan 2027",
    val inProductionDate: String = "11 Jan 2027",
    val readyDate: String = "-",
    val deliveredDate: String = "-",
    val vendorId: String? = "usr_vnd_01",
    val assets: List<OrderAsset> = emptyList()
)

data class OrdersResponse(
    val success: Boolean,
    val orders: List<VendorOrder>
)

data class PlaceOrderRequest(
    val title: String,
    val chapterName: String,
    val amount: Double,
    val itemsSummary: String,
    val vendorId: String = "usr_vnd_01",
    val notifyVendor: Boolean = true
)
