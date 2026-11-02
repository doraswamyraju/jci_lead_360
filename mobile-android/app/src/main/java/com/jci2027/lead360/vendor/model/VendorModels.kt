package com.jci2027.lead360.vendor.model

data class VendorProfile(
    val vendorId: String = "vnd_001",
    val businessName: String = "Rs Creations",
    val isApproved: Boolean = true,
    val phone: String = "+91 98765 43210",
    val email: String = "sales@rscreations.in",
    val serviceAreas: String = "Andhra Pradesh & Telangana",
    val gstin: String = "37AAAAA0000A1Z5"
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

data class CatalogItem(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val unit: String = "piece",
    val description: String,
    val imageUrl: String = "",
    val productCountInCat: Int = 0
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
    val quotedAmount: Double? = null
)

data class VendorOrder(
    val orderId: String,
    val title: String,
    val chapterName: String,
    val orderDate: String,
    val amount: Double,
    val status: String, // Confirmed, In Production, Ready, Delivered
    val itemsSummary: String,
    val confirmedDate: String = "10 Jan 2027",
    val inProductionDate: String = "11 Jan 2027",
    val readyDate: String = "-",
    val deliveredDate: String = "-"
)

