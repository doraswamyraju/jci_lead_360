package com.jci2027.lead360.vendor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jci2027.lead360.vendor.model.CatalogItem
import com.jci2027.lead360.vendor.model.VendorOrder
import com.jci2027.lead360.vendor.screens.*

enum class VendorTab(val label: String, val icon: ImageVector, val emoji: String) {
    HOME("Home", Icons.Default.Home, "🏠"),
    CATALOG("Catalog", Icons.Default.ShoppingCart, "📦"),
    ORDERS("Orders", Icons.Default.ShoppingCart, "📋"),
    RFQS("RFQs", Icons.Default.Edit, "📑"),
    MORE("More", Icons.Default.Menu, "☰")
}

enum class VendorSubScreen {
    MAIN_TABS,
    PRODUCT_DETAIL,
    CREATE_QUOTE,
    ORDER_DETAIL,
    BUSINESS_PROFILE,
    EARNINGS
}

@Composable
fun VendorAppContainer(
    onExitVendorMode: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(VendorTab.HOME) }
    var currentSubScreen by remember { mutableStateOf(VendorSubScreen.MAIN_TABS) }
    var selectedCatalogItem by remember { mutableStateOf<CatalogItem?>(null) }
    var selectedOrder by remember { mutableStateOf<VendorOrder?>(null) }

    val navyBlue = Color(0xFF0D47A1)
    val inactiveColor = Color(0xFF94A3B8)

    Scaffold(
        bottomBar = {
            if (currentSubScreen == VendorSubScreen.MAIN_TABS) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    VendorTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Text(tab.emoji, fontSize = 20.sp)
                            },
                            label = {
                                Text(
                                    tab.label,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = navyBlue,
                                selectedTextColor = navyBlue,
                                indicatorColor = Color(0xFFEFF6FF),
                                unselectedIconColor = inactiveColor,
                                unselectedTextColor = inactiveColor
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentSubScreen) {
                VendorSubScreen.MAIN_TABS -> {
                    when (selectedTab) {
                        VendorTab.HOME -> {
                            VendorHomeScreen(
                                onNavigateTab = { tab ->
                                    when (tab) {
                                        "rfqs" -> selectedTab = VendorTab.RFQS
                                        "orders" -> selectedTab = VendorTab.ORDERS
                                        "catalog" -> selectedTab = VendorTab.CATALOG
                                        "earnings" -> currentSubScreen = VendorSubScreen.EARNINGS
                                        "profile" -> currentSubScreen = VendorSubScreen.BUSINESS_PROFILE
                                        "quote" -> currentSubScreen = VendorSubScreen.CREATE_QUOTE
                                    }
                                },
                                onViewRfqs = {
                                    selectedTab = VendorTab.RFQS
                                }
                            )
                        }
                        VendorTab.CATALOG -> {
                            VendorCatalogScreen(
                                onBack = { selectedTab = VendorTab.HOME },
                                onProductClick = { item ->
                                    selectedCatalogItem = item
                                    currentSubScreen = VendorSubScreen.PRODUCT_DETAIL
                                }
                            )
                        }
                        VendorTab.ORDERS -> {
                            VendorOrdersScreen(
                                onBack = { selectedTab = VendorTab.HOME },
                                onOrderClick = { order ->
                                    selectedOrder = order
                                    currentSubScreen = VendorSubScreen.ORDER_DETAIL
                                }
                            )
                        }
                        VendorTab.RFQS -> {
                            VendorRfqsScreen(
                                onBack = { selectedTab = VendorTab.HOME },
                                onRfqSelected = { _ ->
                                    currentSubScreen = VendorSubScreen.CREATE_QUOTE
                                }
                            )
                        }
                        VendorTab.MORE -> {
                            VendorBusinessProfileScreen(
                                onBack = { selectedTab = VendorTab.HOME },
                                onItemClick = { itemId ->
                                    when (itemId) {
                                        "product_catalog" -> selectedTab = VendorTab.CATALOG
                                        "price_lists", "documents" -> currentSubScreen = VendorSubScreen.EARNINGS
                                        else -> currentSubScreen = VendorSubScreen.BUSINESS_PROFILE
                                    }
                                }
                            )
                        }
                    }
                }
                VendorSubScreen.PRODUCT_DETAIL -> {
                    ProductDetailScreen(
                        item = selectedCatalogItem ?: CatalogItem(
                            id = "default",
                            name = "JCI Crystal Memento",
                            category = "Mementoes",
                            price = 1250.0,
                            unit = "piece",
                            description = "Premium crystal memento with JCI logo engraving. Ideal for installations, awards and recognitions."
                        ),
                        onBack = { currentSubScreen = VendorSubScreen.MAIN_TABS },
                        onCreateQuoteForProduct = {
                            currentSubScreen = VendorSubScreen.CREATE_QUOTE
                        }
                    )
                }
                VendorSubScreen.CREATE_QUOTE -> {
                    CreateCustomQuoteScreen(
                        onBack = { currentSubScreen = VendorSubScreen.MAIN_TABS },
                        onQuoteSubmitted = {
                            currentSubScreen = VendorSubScreen.MAIN_TABS
                            selectedTab = VendorTab.RFQS
                        }
                    )
                }
                VendorSubScreen.ORDER_DETAIL -> {
                    VendorOrderDetailScreen(
                        order = selectedOrder ?: VendorOrder(
                            orderId = "#ORD10248",
                            title = "Installation Kit (Backdrop, Banners)",
                            chapterName = "JCI Tirupati Innovations",
                            orderDate = "10 Jan 2027",
                            amount = 48500.0,
                            status = "In Production",
                            itemsSummary = "20 ft backdrop, 2 banners, 100 brochures"
                        ),
                        onBack = { currentSubScreen = VendorSubScreen.MAIN_TABS }
                    )
                }
                VendorSubScreen.BUSINESS_PROFILE -> {
                    VendorBusinessProfileScreen(
                        onBack = { currentSubScreen = VendorSubScreen.MAIN_TABS },
                        onItemClick = { itemId ->
                            when (itemId) {
                                "product_catalog" -> {
                                    currentSubScreen = VendorSubScreen.MAIN_TABS
                                    selectedTab = VendorTab.CATALOG
                                }
                                else -> {}
                            }
                        }
                    )
                }
                VendorSubScreen.EARNINGS -> {
                    VendorEarningsScreen(
                        onBack = { currentSubScreen = VendorSubScreen.MAIN_TABS }
                    )
                }
            }
        }
    }
}
