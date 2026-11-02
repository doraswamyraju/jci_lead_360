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
import com.jci2027.lead360.vendor.model.*
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
    CREATE_CATALOG_ITEM,
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

    // Live catalog state shared across modules
    var catalogItems by remember {
        mutableStateOf(
            listOf(
                CatalogItem(
                    id = "cat_01",
                    name = "JCI Crystal Memento",
                    category = "Mementoes",
                    price = 1250.0,
                    unit = "piece",
                    leadTime = "3-4 Days",
                    description = "Premium crystal memento with JCI logo engraving. Ideal for installations, awards and recognitions.",
                    productCountInCat = 28
                ),
                CatalogItem(
                    id = "cat_02",
                    name = "Banners & Standees",
                    category = "Banners",
                    price = 1450.0,
                    unit = "unit",
                    leadTime = "24 Hours",
                    description = "Star flex heavy duty roll-up standees (6x3 ft) with metal hardware & zip carry case.",
                    productCountInCat = 16
                ),
                CatalogItem(
                    id = "cat_03",
                    name = "Brochures & Flyers",
                    category = "Brochures",
                    price = 8.5,
                    unit = "piece",
                    leadTime = "2 Days",
                    description = "170 GSM art paper tri-fold brochures with glossy lamination for chapter events.",
                    productCountInCat = 12
                ),
                CatalogItem(
                    id = "cat_04",
                    name = "Apparel & Merchandise",
                    category = "Merchandise",
                    price = 450.0,
                    unit = "piece",
                    leadTime = "5 Days",
                    description = "Official JCI dry-fit collared polo t-shirt with embroidered crest on chest.",
                    productCountInCat = 10
                )
            )
        )
    }

    // Live orders state
    var ordersList by remember {
        mutableStateOf(
            listOf(
                VendorOrder(
                    orderId = "#ORD10248",
                    title = "Installation Kit (Backdrop, Banners)",
                    chapterName = "JCI Tirupati Innovations",
                    orderDate = "10 Jan 2027",
                    amount = 48500.0,
                    status = "In Production",
                    itemsSummary = "20 ft stage backdrop, 2 podium banners, 100 induction dossiers",
                    assets = listOf(
                        OrderAsset("Stage_Backdrop_Artwork.pdf", "/assets/logo.png", "12.4 MB"),
                        OrderAsset("Official_PO_Doc.pdf", "/assets/logo.png", "1.2 MB"),
                        OrderAsset("Color_Proof_Approval.jpg", "/assets/logo.png", "3.8 MB")
                    )
                ),
                VendorOrder(
                    orderId = "#ORD10237",
                    title = "Mementoes – 15 Nos",
                    chapterName = "JCI Chittoor",
                    orderDate = "08 Jan 2027",
                    amount = 18750.0,
                    status = "Confirmed",
                    itemsSummary = "15 Golden crystal shields with custom engraving",
                    assets = listOf(
                        OrderAsset("Memento_Engraving_List.xlsx", "/assets/logo.png", "850 KB"),
                        OrderAsset("PO_JCI_Chittoor.pdf", "/assets/logo.png", "1.1 MB")
                    )
                ),
                VendorOrder(
                    orderId = "#ORD10210",
                    title = "Brochures – 500 Nos",
                    chapterName = "JCI Nellore",
                    orderDate = "05 Jan 2027",
                    amount = 7500.0,
                    status = "Completed",
                    itemsSummary = "500 Nos Tri-fold gloss brochures",
                    assets = listOf(
                        OrderAsset("Brochure_Final_Print_Ready.pdf", "/assets/logo.png", "18.6 MB"),
                        OrderAsset("Delivery_Challan_Signed.pdf", "/assets/logo.png", "640 KB")
                    )
                )
            )
        )
    }

    val royalBlue = Color(0xFF0D47A1)
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
                                selectedIconColor = royalBlue,
                                selectedTextColor = royalBlue,
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
                                        "create_catalog" -> currentSubScreen = VendorSubScreen.CREATE_CATALOG_ITEM
                                        "earnings" -> currentSubScreen = VendorSubScreen.EARNINGS
                                        "profile" -> currentSubScreen = VendorSubScreen.BUSINESS_PROFILE
                                        "quote" -> currentSubScreen = VendorSubScreen.CREATE_QUOTE
                                    }
                                },
                                onViewRfqs = {
                                    selectedTab = VendorTab.RFQS
                                },
                                onLogout = onExitVendorMode
                            )
                        }
                        VendorTab.CATALOG -> {
                            VendorCatalogScreen(
                                catalogList = catalogItems,
                                onBack = { selectedTab = VendorTab.HOME },
                                onProductClick = { item ->
                                    selectedCatalogItem = item
                                    currentSubScreen = VendorSubScreen.PRODUCT_DETAIL
                                },
                                onAddNewProduct = {
                                    currentSubScreen = VendorSubScreen.CREATE_CATALOG_ITEM
                                }
                            )
                        }
                        VendorTab.ORDERS -> {
                            VendorOrdersScreen(
                                ordersList = ordersList,
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
                                onEditProfileClick = onExitVendorMode,
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
                VendorSubScreen.CREATE_CATALOG_ITEM -> {
                    CreateCatalogItemScreen(
                        onBack = { currentSubScreen = VendorSubScreen.MAIN_TABS },
                        onItemCreated = { newItem ->
                            catalogItems = listOf(newItem) + catalogItems
                            currentSubScreen = VendorSubScreen.MAIN_TABS
                            selectedTab = VendorTab.CATALOG
                        }
                    )
                }
                VendorSubScreen.PRODUCT_DETAIL -> {
                    ProductDetailScreen(
                        item = selectedCatalogItem ?: catalogItems.first(),
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
                        order = selectedOrder ?: ordersList.first(),
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
