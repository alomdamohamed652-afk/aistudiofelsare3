package com.example.ui

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.BuildConfig
import com.example.core.designsystem.*
import com.example.core.model.UserRole
import com.example.ui.admin.AdminPortalScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.common.NotificationCenterSheet
import com.example.ui.customer.*
import com.example.ui.driver.DriverPortalScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.partner.PartnerPortalScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FalsareeApp(
    viewModel: FalsareeViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()

    val appSettings by viewModel.repository.appSettings.collectAsStateWithLifecycle(initialValue = null)
    val onboardingPages by viewModel.repository.activeOnboardingPages.collectAsStateWithLifecycle(initialValue = emptyList())
    val partners by viewModel.repository.allPartners.collectAsStateWithLifecycle(initialValue = emptyList())
    val products by viewModel.repository.allProducts.collectAsStateWithLifecycle(initialValue = emptyList())
    val orders by viewModel.repository.allOrders.collectAsStateWithLifecycle(initialValue = emptyList())
    val drivers by viewModel.repository.allDrivers.collectAsStateWithLifecycle(initialValue = emptyList())
    val homeSections by viewModel.repository.activeHomeSections.collectAsStateWithLifecycle(initialValue = emptyList())
    val coupons by viewModel.repository.allCoupons.collectAsStateWithLifecycle(initialValue = emptyList())
    val customerOrders by viewModel.customerOrders.collectAsStateWithLifecycle()
    val addresses by viewModel.customerAddresses.collectAsStateWithLifecycle()
    val tickets by viewModel.customerTickets.collectAsStateWithLifecycle()
    val activityLogs by viewModel.repository.recentActivityLogs.collectAsStateWithLifecycle(initialValue = emptyList())

    val notifications by remember(currentSession, currentRole) {
        currentSession?.let { viewModel.repository.getNotificationsForUser(currentRole, it.userId) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    val unreadNotificationsCount by remember(currentSession, currentRole) {
        currentSession?.let { viewModel.repository.getUnreadNotificationsCountForUser(currentRole, it.userId) }
            ?: kotlinx.coroutines.flow.flowOf(0)
    }.collectAsStateWithLifecycle(initialValue = 0)

    val customerTab by viewModel.customerSelectedTab.collectAsStateWithLifecycle()
    val selectedPartner by viewModel.selectedPartner.collectAsStateWithLifecycle()
    val cartPartner by viewModel.cartPartner.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartOptions by viewModel.cartOptions.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val discountAmount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val trackedOrderId by viewModel.trackedOrderId.collectAsStateWithLifecycle()
    val reviewingOrderId by viewModel.reviewingOrderId.collectAsStateWithLifecycle()
    val favoritePartnerIds by viewModel.favoritePartnerIds.collectAsStateWithLifecycle()
    val driverPayoutRequests by viewModel.driverPayoutRequests.collectAsStateWithLifecycle()
    val activeDriverId by viewModel.activeDriverId.collectAsStateWithLifecycle()
    val activePartnerId by viewModel.activePartnerId.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val onlyOpenFilter by viewModel.onlyOpenFilter.collectAsStateWithLifecycle()
    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()

    var showCartScreen by remember { mutableStateOf(false) }
    var showNotificationSheet by remember { mutableStateOf(false) }

    LaunchedEffect(alertMessage) {
        alertMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearAlert()
        }
    }

    FalsareeTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (appSettings?.onboardingEnabled == true && !isOnboardingCompleted) {
                    OnboardingScreen(
                        pages = onboardingPages,
                        onFinish = { viewModel.completeOnboarding() }
                    )
                } else if (!isUserLoggedIn) {
                    AuthScreen(
                        onLogin = { identifier, password -> viewModel.login(identifier, password) },
                        onRegister = { name, phone, email, password, confirmPassword ->
                            viewModel.register(name, phone, email, password, confirmPassword)
                        }
                    )
                } else {
                    when (currentRole) {
                        UserRole.CUSTOMER -> {
                            CustomerPortalView(
                                customerTab = customerTab,
                                onSelectTab = { viewModel.setCustomerTab(it) },
                                selectedPartner = selectedPartner,
                                onSelectPartner = { viewModel.selectPartner(it) },
                                showCartScreen = showCartScreen,
                                onToggleCart = { showCartScreen = it },
                                homeSections = homeSections,
                                partners = partners,
                                products = products,
                                cartPartner = cartPartner,
                                cartItems = cartItems,
                                cartOptions = cartOptions,
                                appliedCoupon = appliedCoupon,
                                discountAmount = discountAmount,
                                addresses = addresses,
                                coupons = coupons,
                                tickets = tickets,
                                orders = customerOrders,
                                searchQuery = searchQuery,
                                onQueryChange = { viewModel.setSearchQuery(it) },
                                categoryFilter = categoryFilter,
                                onCategorySelect = { viewModel.setCategoryFilter(it) },
                                onlyOpenFilter = onlyOpenFilter,
                                onToggleOnlyOpen = { viewModel.toggleOnlyOpenFilter() },
                                onAddToCart = { partner, prod, qty, opts -> viewModel.addToCart(partner, prod, qty, opts) },
                                onUpdateCartQty = { prod, delta -> viewModel.updateCartItemQuantity(prod, delta) },
                                onApplyCoupon = { viewModel.applyCoupon(it) },
                                onClearCart = { viewModel.clearCart() },
                                onConfirmOrder = { addr, notes, pay, receipt ->
                                    viewModel.placeOrder(
                                        deliveryAddress = addr,
                                        customerNotes = notes,
                                        paymentMethod = pay,
                                        transferReceiptNote = receipt
                                    )
                                    showCartScreen = false
                                },
                                onSelectOrderToTrack = { viewModel.trackOrder(it) },
                                favoritePartnerIds = favoritePartnerIds,
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onReviewOrder = { viewModel.openReviewDialog(it) },
                                onOpenNotifications = { showNotificationSheet = true },
                                unreadNotifications = unreadNotificationsCount,
                                onAddAddress = { viewModel.addAddress(it) },
                                onDeleteAddress = { viewModel.deleteAddress(it) },
                                onCreateTicket = { viewModel.createSupportTicket(it) },
                                onSwitchRole = { viewModel.switchRole(it) }
                            )
                        }

                        UserRole.ADMIN -> {
                            AdminPortalScreen(
                                orders = orders,
                                partners = partners,
                                drivers = drivers,
                                homeSections = homeSections,
                                onboardingPages = onboardingPages,
                                appSettings = appSettings,
                                activityLogs = activityLogs,
                                onUpdateOrderStatus = { id, st, reason, force ->
                                    coroutineScope.launch {
                                        viewModel.repository.updateOrderStatus(id, st, "مدير النظام", "الإدارة", reason, force)
                                    }
                                },
                                onAssignDriver = { id, driver ->
                                    coroutineScope.launch {
                                        viewModel.repository.assignDriverToOrder(id, driver, "مدير النظام", "الإدارة")
                                    }
                                },
                                onReviewTransfer = { id, approved, reason ->
                                    coroutineScope.launch {
                                        viewModel.repository.reviewBankTransfer(id, approved, reason, "قسم المالية")
                                    }
                                },
                                onTogglePartnerStatus = { id, isOpen ->
                                    coroutineScope.launch { viewModel.repository.setPartnerOpenStatus(id, isOpen) }
                                },
                                onToggleDriverStatus = { id, st ->
                                    coroutineScope.launch { viewModel.repository.setDriverAvailability(id, st) }
                                },
                                onSaveHomeSection = { sec -> coroutineScope.launch { viewModel.repository.saveHomeSection(sec) } },
                                onDeleteHomeSection = { sec -> coroutineScope.launch { viewModel.repository.deleteHomeSection(sec) } },
                                onSaveOnboardingPage = { page -> coroutineScope.launch { viewModel.repository.saveOnboardingPage(page) } },
                                onDeleteOnboardingPage = { page -> coroutineScope.launch { viewModel.repository.deleteOnboardingPage(page) } },
                                onToggleOnboardingEnabled = { enabled -> coroutineScope.launch { viewModel.repository.setOnboardingEnabled(enabled) } },
                                onSwitchRole = { viewModel.switchRole(it) }
                            )
                        }

                        UserRole.DRIVER -> {
                            val activeDriver = drivers.find { it.id == activeDriverId }
                                ?: if (BuildConfig.DEBUG) drivers.firstOrNull() else null
                            val driverActiveOrder = orders.find {
                                it.driverId == activeDriver?.id &&
                                    it.deliveryStatus != com.example.core.model.DeliveryStatus.DELIVERED
                            }
                            val openOrders = orders.filter {
                                it.driverId == null &&
                                    it.deliveryStatus == com.example.core.model.DeliveryStatus.WAITING_FOR_DRIVER &&
                                    it.orderStatus !in listOf(
                                        com.example.core.model.OrderStatus.CANCELLED,
                                        com.example.core.model.OrderStatus.REJECTED
                                    )
                            }
                            val driverOrders = orders.filter { it.driverId == activeDriver?.id }

                            DriverPortalScreen(
                                driver = activeDriver,
                                activeOrder = driverActiveOrder,
                                openOrders = openOrders,
                                driverOrders = driverOrders,
                                payoutRequests = driverPayoutRequests,
                                onRequestPayout = { amt -> viewModel.requestDriverPayout(amt) },
                                onToggleAvailability = { st ->
                                    activeDriver?.let { driver ->
                                        coroutineScope.launch { viewModel.repository.setDriverAvailability(driver.id, st) }
                                    }
                                },
                                onAcceptOrder = { ord ->
                                    activeDriver?.let { driver ->
                                        coroutineScope.launch {
                                            viewModel.repository.assignDriverToOrder(ord.id, driver, driver.name, "المندوب")
                                        }
                                    }
                                },
                                onConfirmPickup = { ord ->
                                    coroutineScope.launch {
                                        viewModel.repository.updateDeliveryStatus(
                                            orderId = ord.id,
                                            newStatus = com.example.core.model.DeliveryStatus.OUT_FOR_DELIVERY,
                                            driverId = activeDriver?.id,
                                            driverName = activeDriver?.name,
                                            actor = activeDriver?.name ?: "المندوب",
                                            actorRole = "المندوب",
                                            reason = "تم استلام الطلب من المحل والانطلاق للتسليم"
                                        )
                                    }
                                },
                                onConfirmDelivered = { ord ->
                                    coroutineScope.launch {
                                        viewModel.repository.updateDeliveryStatus(
                                            orderId = ord.id,
                                            newStatus = com.example.core.model.DeliveryStatus.DELIVERED,
                                            driverId = activeDriver?.id,
                                            driverName = activeDriver?.name,
                                            actor = activeDriver?.name ?: "المندوب",
                                            actorRole = "المندوب",
                                            reason = "تم تسليم الطلب للعميل واستلام المبلغ"
                                        )
                                    }
                                },
                                onSwitchRole = { viewModel.switchRole(it) }
                            )
                        }

                        UserRole.PARTNER -> {
                            val activePartner = partners.find { it.id == activePartnerId }
                                ?: if (BuildConfig.DEBUG) partners.firstOrNull() else null
                            val partnerProducts = products.filter { it.partnerId == activePartner?.id }

                            PartnerPortalScreen(
                                allPartners = partners,
                                activePartner = activePartner,
                                orders = orders,
                                products = partnerProducts,
                                onSelectPartner = { viewModel.setActivePartnerId(it) },
                                onToggleOpen = { isOpen ->
                                    activePartner?.let { partner ->
                                        coroutineScope.launch { viewModel.repository.setPartnerOpenStatus(partner.id, isOpen) }
                                    }
                                },
                                onAcceptOrder = { ord ->
                                    activePartner?.let { partner ->
                                        coroutineScope.launch {
                                            viewModel.repository.updateOrderStatus(
                                                orderId = ord.id,
                                                newStatus = com.example.core.model.OrderStatus.APPROVED,
                                                actor = partner.name,
                                                actorRole = "الشريك",
                                                reason = "تم قبول الطلب وجاري التحضير"
                                            )
                                        }
                                    }
                                },
                                onRejectOrder = { ord, reason ->
                                    activePartner?.let { partner ->
                                        coroutineScope.launch {
                                            viewModel.repository.updateOrderStatus(
                                                orderId = ord.id,
                                                newStatus = com.example.core.model.OrderStatus.REJECTED,
                                                actor = partner.name,
                                                actorRole = "الشريك",
                                                reason = reason
                                            )
                                        }
                                    }
                                },
                                onStartPreparing = { ord, _ ->
                                    activePartner?.let { partner ->
                                        coroutineScope.launch {
                                            viewModel.repository.updateOrderStatus(
                                                orderId = ord.id,
                                                newStatus = com.example.core.model.OrderStatus.PREPARING,
                                                actor = partner.name,
                                                actorRole = "الشريك",
                                                reason = "بدء إعداد الطلب بالمطبخ"
                                            )
                                        }
                                    }
                                },
                                onReadyForPickup = { ord ->
                                    activePartner?.let { partner ->
                                        coroutineScope.launch {
                                            viewModel.repository.updateOrderStatus(
                                                orderId = ord.id,
                                                newStatus = com.example.core.model.OrderStatus.READY_FOR_PICKUP,
                                                actor = partner.name,
                                                actorRole = "الشريك",
                                                reason = "تم تجهيز الطلب وجاهز لاستلام المندوب"
                                            )
                                        }
                                    }
                                },
                                onSaveProduct = { prod -> coroutineScope.launch { viewModel.repository.saveProduct(prod) } },
                                onUpdateProductStatus = { id, st -> coroutineScope.launch { viewModel.repository.updateProductStatus(id, st) } },
                                onSwitchRole = { viewModel.switchRole(it) }
                            )
                        }
                    }
                }
            }
        }

        if (trackedOrderId != null && currentRole == UserRole.CUSTOMER) {
            val order = customerOrders.find { it.id == trackedOrderId }
            if (order != null) {
                val orderItems by viewModel.repository.getOrderItems(order.id).collectAsStateWithLifecycle(initialValue = emptyList())
                val orderLogs by viewModel.repository.getOrderActivityLogs(order.id).collectAsStateWithLifecycle(initialValue = emptyList())
                LiveOrderTrackingSheet(
                    order = order,
                    items = orderItems,
                    activityLogs = orderLogs,
                    onCancelOrder = { viewModel.cancelCustomerOrder(order.id) },
                    onDismiss = { viewModel.trackOrder(null) }
                )
            }
        }

        if (reviewingOrderId != null && currentRole == UserRole.CUSTOMER) {
            val orderToReview = customerOrders.find { it.id == reviewingOrderId }
            if (orderToReview != null) {
                OrderReviewDialog(
                    order = orderToReview,
                    onDismiss = { viewModel.closeReviewDialog() },
                    onSubmit = { partnerRating, driverRating, notes ->
                        viewModel.submitReview(orderToReview.id, partnerRating, driverRating, notes)
                    }
                )
            }
        }

        if (showNotificationSheet) {
            NotificationCenterSheet(
                notifications = notifications,
                onMarkAllRead = {
                    currentSession?.let { session ->
                        coroutineScope.launch {
                            viewModel.repository.markAllNotificationsReadForUser(currentRole, session.userId)
                        }
                    }
                },
                onDismiss = { showNotificationSheet = false }
            )
        }
    }
}

@Composable
fun CustomerPortalView(
    customerTab: Int,
    onSelectTab: (Int) -> Unit,
    selectedPartner: com.example.data.local.PartnerEntity?,
    onSelectPartner: (com.example.data.local.PartnerEntity?) -> Unit,
    showCartScreen: Boolean,
    onToggleCart: (Boolean) -> Unit,
    homeSections: List<com.example.data.local.HomeSectionEntity>,
    partners: List<com.example.data.local.PartnerEntity>,
    products: List<com.example.data.local.ProductEntity>,
    cartPartner: com.example.data.local.PartnerEntity?,
    cartItems: Map<com.example.data.local.ProductEntity, Int>,
    cartOptions: Map<Long, String>,
    appliedCoupon: com.example.data.local.CouponEntity?,
    discountAmount: Double,
    addresses: List<com.example.data.local.CustomerAddressEntity>,
    coupons: List<com.example.data.local.CouponEntity>,
    tickets: List<com.example.data.local.SupportTicketEntity>,
    orders: List<com.example.data.local.OrderEntity>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    categoryFilter: com.example.core.model.PartnerType?,
    onCategorySelect: (com.example.core.model.PartnerType?) -> Unit,
    onlyOpenFilter: Boolean,
    onToggleOnlyOpen: () -> Unit,
    onAddToCart: (com.example.data.local.PartnerEntity, com.example.data.local.ProductEntity, Int, String) -> Unit,
    onUpdateCartQty: (com.example.data.local.ProductEntity, Int) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onClearCart: () -> Unit,
    onConfirmOrder: (String, String, com.example.core.model.PaymentMethod, String) -> Unit,
    onSelectOrderToTrack: (Long) -> Unit,
    favoritePartnerIds: List<Long> = emptyList(),
    onToggleFavorite: (Long) -> Unit = {},
    onReviewOrder: (Long) -> Unit = {},
    onOpenNotifications: () -> Unit,
    unreadNotifications: Int,
    onAddAddress: (com.example.data.local.CustomerAddressEntity) -> Unit,
    onDeleteAddress: (com.example.data.local.CustomerAddressEntity) -> Unit,
    onCreateTicket: (com.example.data.local.SupportTicketEntity) -> Unit,
    onSwitchRole: (UserRole) -> Unit
) {
    val totalCartItemsCount = remember(cartItems) { cartItems.values.sum() }

    if (showCartScreen) {
        CartCheckoutScreen(
            partner = cartPartner,
            cartItems = cartItems,
            cartOptions = cartOptions,
            appliedCoupon = appliedCoupon,
            discountAmount = discountAmount,
            addresses = addresses,
            onUpdateQuantity = onUpdateCartQty,
            onApplyCoupon = onApplyCoupon,
            onConfirmOrder = onConfirmOrder,
            onBack = { onToggleCart(false) },
            onClearCart = onClearCart
        )
    } else if (selectedPartner != null) {
        val partnerProducts = remember(products, selectedPartner) { products.filter { it.partnerId == selectedPartner.id } }
        PartnerDetailScreen(
            partner = selectedPartner,
            products = partnerProducts,
            onBack = { onSelectPartner(null) },
            onAddToCart = { prod, qty, opts -> onAddToCart(selectedPartner, prod, qty, opts) },
            onViewCart = { onToggleCart(true) },
            cartItemCount = totalCartItemsCount
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = SurfaceCard, tonalElevation = 6.dp) {
                    val tabs = listOf(
                        Triple("الرئيسية", Icons.Default.Home, 0),
                        Triple("البحث", Icons.Default.Search, 1),
                        Triple("المفضلة", Icons.Default.Favorite, 2),
                        Triple("طلباتي", Icons.Default.ReceiptLong, 3),
                        Triple("حسابي", Icons.Default.Person, 4)
                    )
                    tabs.forEach { (title, icon, idx) ->
                        val isSelected = customerTab == idx
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { onSelectTab(idx) },
                            icon = { Icon(icon, contentDescription = title, tint = if (isSelected) BrandPrimary else TextSecondary) },
                            label = { Text(title, color = if (isSelected) BrandPrimary else TextSecondary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (customerTab) {
                    0 -> CustomerHomeScreen(
                        sections = homeSections,
                        partners = partners,
                        products = products,
                        favoritePartnerIds = favoritePartnerIds,
                        onToggleFavorite = onToggleFavorite,
                        onPartnerClick = onSelectPartner,
                        onCategoryClick = { type -> onCategorySelect(type); onSelectTab(1) },
                        onProductClick = { prod -> partners.find { it.id == prod.partnerId }?.let(onSelectPartner) },
                        onSearchClick = { onSelectTab(1) },
                        onCartClick = { onToggleCart(true) },
                        cartItemCount = totalCartItemsCount,
                        onNotificationClick = onOpenNotifications,
                        unreadNotificationCount = unreadNotifications
                    )
                    1 -> CustomerSearchScreen(
                        partners = partners,
                        products = products,
                        searchQuery = searchQuery,
                        onQueryChange = onQueryChange,
                        selectedCategory = categoryFilter,
                        onCategorySelect = onCategorySelect,
                        onlyOpenFilter = onlyOpenFilter,
                        onToggleOnlyOpen = onToggleOnlyOpen,
                        onPartnerClick = onSelectPartner,
                        onProductClick = { prod -> partners.find { it.id == prod.partnerId }?.let(onSelectPartner) }
                    )
                    2 -> CustomerFavoritesScreen(
                        partners = partners,
                        favoritePartnerIds = favoritePartnerIds,
                        onToggleFavorite = onToggleFavorite,
                        onPartnerClick = onSelectPartner,
                        onExploreClick = { onSelectTab(1) }
                    )
                    3 -> CustomerOrdersScreen(
                        orders = orders,
                        onSelectOrderToTrack = onSelectOrderToTrack,
                        onReviewOrder = onReviewOrder
                    )
                    4 -> CustomerProfileScreen(
                        addresses = addresses,
                        coupons = coupons,
                        tickets = tickets,
                        onAddAddress = onAddAddress,
                        onDeleteAddress = onDeleteAddress,
                        onCreateTicket = onCreateTicket,
                        onSwitchRole = onSwitchRole
                    )
                }
            }
        }
    }
}
