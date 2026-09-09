package com.example.ui

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.BuildConfig
import com.example.core.designsystem.*
import com.example.core.model.DeliveryStatus
import com.example.core.model.OrderStatus
import com.example.core.model.UserRole
import com.example.core.model.UserSession
import com.example.ui.admin.AdminPortalScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.common.NotificationCenterSheet
import com.example.ui.customer.*
import com.example.ui.driver.DriverPortalScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.partner.PartnerPortalScreen
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FalsareeApp(viewModel: FalsareeViewModel = viewModel()) {
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
    val favoritePartnerIds by viewModel.favoritePartnerIds.collectAsStateWithLifecycle()
    val driverPayoutRequests by viewModel.driverPayoutRequests.collectAsStateWithLifecycle()
    val activeDriverId by viewModel.activeDriverId.collectAsStateWithLifecycle()
    val activePartnerId by viewModel.activePartnerId.collectAsStateWithLifecycle()
    val activityLogs by viewModel.repository.recentActivityLogs.collectAsStateWithLifecycle(initialValue = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val onlyOpenFilter by viewModel.onlyOpenFilter.collectAsStateWithLifecycle()
    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()
    val customerTab by viewModel.customerSelectedTab.collectAsStateWithLifecycle()
    val selectedPartner by viewModel.selectedPartner.collectAsStateWithLifecycle()
    val cartPartner by viewModel.cartPartner.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartOptions by viewModel.cartOptions.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val discountAmount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val trackedOrderId by viewModel.trackedOrderId.collectAsStateWithLifecycle()
    val reviewingOrderId by viewModel.reviewingOrderId.collectAsStateWithLifecycle()

    var showCartScreen by remember { mutableStateOf(false) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    var showRolePickerSheet by remember { mutableStateOf(false) }

    val notifications by remember(currentSession, currentRole) {
        currentSession?.let { viewModel.repository.getNotificationsForUser(currentRole, it.userId) }
            ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    val unreadNotificationsCount by remember(currentSession, currentRole) {
        currentSession?.let { viewModel.repository.getUnreadNotificationsCountForUser(currentRole, it.userId) }
            ?: flowOf(0)
    }.collectAsStateWithLifecycle(initialValue = 0)

    LaunchedEffect(alertMessage) {
        alertMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearAlert() }
    }

    FalsareeTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing,
            floatingActionButton = {
                if (BuildConfig.DEBUG && isUserLoggedIn) {
                    FloatingActionButton(
                        onClick = { showRolePickerSheet = true },
                        containerColor = BrandSecondary,
                        contentColor = BrandAccent,
                        modifier = Modifier
                            .padding(bottom = if (currentRole == UserRole.CUSTOMER && !showCartScreen && selectedPartner == null) 64.dp else 16.dp)
                            .testTag("global_role_switcher_fab")
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(currentRole.iconEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(currentRole.titleArabic, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when {
                    appSettings?.onboardingEnabled == true && !isOnboardingCompleted -> {
                        OnboardingScreen(pages = onboardingPages, onFinish = viewModel::completeOnboarding)
                    }
                    !isUserLoggedIn -> {
                        AuthScreen(
                            onLogin = viewModel::login,
                            onRegister = viewModel::register,
                            onRequestOtp = { identifier ->
                                if (identifier.isBlank()) viewModel.setAlert("أدخل رقم الهاتف أولًا")
                                else viewModel.setAlert("رمز التحقق التجريبي: 1234 (سيتم استبداله بـ OTP حقيقي لاحقًا)")
                            }
                        )
                    }
                    else -> when (currentRole) {
                        UserRole.CUSTOMER -> CustomerPortalView(
                            customerTab = customerTab,
                            onSelectTab = viewModel::setCustomerTab,
                            selectedPartner = selectedPartner,
                            onSelectPartner = viewModel::selectPartner,
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
                            currentSession = currentSession,
                            onLogout = {
                                viewModel.logout()
                                showCartScreen = false
                                showNotificationSheet = false
                            },
                            orders = customerOrders,
                            searchQuery = searchQuery,
                            onQueryChange = viewModel::setSearchQuery,
                            categoryFilter = categoryFilter,
                            onCategorySelect = viewModel::setCategoryFilter,
                            onlyOpenFilter = onlyOpenFilter,
                            onToggleOnlyOpen = viewModel::toggleOnlyOpenFilter,
                            onAddToCart = viewModel::addToCart,
                            onUpdateCartQty = viewModel::updateCartItemQuantity,
                            onApplyCoupon = viewModel::applyCoupon,
                            onClearCart = viewModel::clearCart,
                            onConfirmOrder = { addr, notes, pay, receipt ->
                                viewModel.placeOrder(deliveryAddress = addr, customerNotes = notes, paymentMethod = pay, transferReceiptNote = receipt)
                                showCartScreen = false
                            },
                            onSelectOrderToTrack = viewModel::trackOrder,
                            favoritePartnerIds = favoritePartnerIds,
                            onToggleFavorite = viewModel::toggleFavorite,
                            onReviewOrder = viewModel::openReviewDialog,
                            onOpenNotifications = { showNotificationSheet = true },
                            unreadNotifications = unreadNotificationsCount,
                            onAddAddress = viewModel::addAddress,
                            onDeleteAddress = viewModel::deleteAddress,
                            onCreateTicket = viewModel::createSupportTicket,
                            onSwitchRole = viewModel::switchRole
                        )
                        UserRole.ADMIN -> AdminPortalScreen(
                            orders = orders,
                            partners = partners,
                            drivers = drivers,
                            homeSections = homeSections,
                            onboardingPages = onboardingPages,
                            appSettings = appSettings,
                            activityLogs = activityLogs,
                            onUpdateOrderStatus = { id, st, reason, force -> coroutineScope.launch { viewModel.repository.updateOrderStatus(id, st, "مدير النظام", "الإدارة", reason, force) } },
                            onAssignDriver = { id, driver -> coroutineScope.launch { viewModel.repository.assignDriverToOrder(id, driver, "مدير النظام", "الإدارة") } },
                            onReviewTransfer = { id, approved, reason -> coroutineScope.launch { viewModel.repository.reviewBankTransfer(id, approved, reason, "قسم المالية") } },
                            onTogglePartnerStatus = { id, isOpen -> coroutineScope.launch { viewModel.repository.setPartnerOpenStatus(id, isOpen) } },
                            onToggleDriverStatus = { id, status -> coroutineScope.launch { viewModel.repository.setDriverAvailability(id, status) } },
                            onSaveHomeSection = { section -> coroutineScope.launch { viewModel.repository.saveHomeSection(section) } },
                            onDeleteHomeSection = { section -> coroutineScope.launch { viewModel.repository.deleteHomeSection(section) } },
                            onSaveOnboardingPage = { page -> coroutineScope.launch { viewModel.repository.saveOnboardingPage(page) } },
                            onDeleteOnboardingPage = { page -> coroutineScope.launch { viewModel.repository.deleteOnboardingPage(page) } },
                            onToggleOnboardingEnabled = { enabled -> coroutineScope.launch { viewModel.repository.setOnboardingEnabled(enabled) } },
                            onSwitchRole = viewModel::switchRole
                        )
                        UserRole.DRIVER -> {
                            val activeDriver = activeDriverId?.let { id -> drivers.find { it.id == id } }
                            val driverActiveOrder = orders.find { it.driverId == activeDriver?.id && it.deliveryStatus != DeliveryStatus.DELIVERED }
                            val openOrders = orders.filter { it.driverId == null && it.deliveryStatus == DeliveryStatus.WAITING_FOR_DRIVER && it.orderStatus !in listOf(OrderStatus.CANCELLED, OrderStatus.REJECTED) }
                            val driverOrders = orders.filter { it.driverId == activeDriver?.id }
                            DriverPortalScreen(
                                driver = activeDriver,
                                activeOrder = driverActiveOrder,
                                openOrders = openOrders,
                                driverOrders = driverOrders,
                                payoutRequests = driverPayoutRequests,
                                onRequestPayout = viewModel::requestDriverPayout,
                                onToggleAvailability = { status -> activeDriver?.let { viewModel.setDriverAvailability(it.id, status) } },
                                onAcceptOrder = { order -> activeDriver?.let { driver -> viewModel.acceptDriverOffer(order.id, driver.id) } },
                                onConfirmPickup = { order -> viewModel.updateDriverDeliveryStatus(order, activeDriver, DeliveryStatus.OUT_FOR_DELIVERY, "تم استلام الطلب من المحل والانطلاق للتسليم") },
                                onConfirmDelivered = { order -> viewModel.updateDriverDeliveryStatus(order, activeDriver, DeliveryStatus.DELIVERED, "تم تسليم الطلب للعميل واستلام المبلغ") },
                                onSwitchRole = viewModel::switchRole
                            )
                        }
                        UserRole.PARTNER -> {
                            val activePartner = activePartnerId?.let { id -> partners.find { it.id == id } }
                            val partnerProducts = activePartner?.let { partner -> products.filter { it.partnerId == partner.id } } ?: emptyList()
                            if (activePartner == null) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("هذا الحساب غير مرتبط بشريك بعد. يرجى التواصل مع الإدارة.", modifier = Modifier.padding(24.dp))
                                }
                            } else {
                                PartnerPortalScreen(
                                    allPartners = partners,
                                    activePartner = activePartner,
                                    orders = orders,
                                    products = partnerProducts,
                                    onSelectPartner = viewModel::setActivePartnerId,
                                    onToggleOpen = { open -> coroutineScope.launch { viewModel.repository.setPartnerOpenStatus(activePartner.id, open) } },
                                    onAcceptOrder = { order -> coroutineScope.launch { viewModel.repository.updateOrderStatus(order.id, OrderStatus.APPROVED, activePartner.name, "الشريك", "تم قبول الطلب وجاري التحضير") } },
                                    onRejectOrder = { order, reason -> coroutineScope.launch { viewModel.repository.updateOrderStatus(order.id, OrderStatus.REJECTED, activePartner.name, "الشريك", reason) } },
                                    onStartPreparing = { order, _ -> coroutineScope.launch { viewModel.repository.updateOrderStatus(order.id, OrderStatus.PREPARING, activePartner.name, "الشريك", "بدء إعداد الطلب") } },
                                    onReadyForPickup = { order -> coroutineScope.launch { viewModel.repository.updateOrderStatus(order.id, OrderStatus.READY_FOR_PICKUP, activePartner.name, "الشريك", "الطلب جاهز لاستلام المندوب") } },
                                    onSaveProduct = { product -> coroutineScope.launch { viewModel.repository.saveProduct(product.copy(partnerId = activePartner.id)) } },
                                    onUpdateProductStatus = { productId, status -> coroutineScope.launch { viewModel.repository.updateProductStatus(productId, status) } },
                                    onSwitchRole = viewModel::switchRole
                                )
                            }
                        }
                    }
                }
            }
        }

        if (trackedOrderId != null) {
            val order = orders.find { it.id == trackedOrderId }
            if (order != null) {
                val orderItems by viewModel.repository.getOrderItems(order.id).collectAsStateWithLifecycle(initialValue = emptyList())
                val orderLogs by viewModel.repository.getOrderActivityLogs(order.id).collectAsStateWithLifecycle(initialValue = emptyList())
                LiveOrderTrackingSheet(order = order, items = orderItems, activityLogs = orderLogs, onCancelOrder = { viewModel.cancelCustomerOrder(order.id) }, onDismiss = { viewModel.trackOrder(null) })
            }
        }

        if (reviewingOrderId != null) {
            val orderToReview = orders.find { it.id == reviewingOrderId }
            if (orderToReview != null) {
                OrderReviewDialog(order = orderToReview, onDismiss = viewModel::closeReviewDialog, onSubmit = { partnerRating, driverRating, notes -> viewModel.submitReview(orderToReview.id, partnerRating, driverRating, notes) })
            }
        }

        if (showNotificationSheet) {
            NotificationCenterSheet(
                notifications = notifications,
                onMarkAllRead = { currentSession?.let { session -> coroutineScope.launch { viewModel.repository.markAllNotificationsReadForUser(currentRole, session.userId) } } },
                onDismiss = { showNotificationSheet = false }
            )
        }

        if (BuildConfig.DEBUG && showRolePickerSheet) {
            ModalBottomSheet(onDismissRequest = { showRolePickerSheet = false }, containerColor = SurfaceCard) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("اختر واجهة النظام للتجربة والمعاينة ⚡", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    UserRole.values().forEach { role ->
                        val isCurrent = currentRole == role
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { viewModel.switchRole(role); showRolePickerSheet = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isCurrent) BrandPrimary.copy(alpha = 0.12f) else SurfaceBackground),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) BrandPrimary else SurfaceBorder)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(role.iconEmoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(role.titleArabic, style = MaterialTheme.typography.titleSmall, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium, color = if (isCurrent) BrandPrimary else TextPrimary)
                                Spacer(modifier = Modifier.weight(1f))
                                if (isCurrent) StatusBadge(text = "نشط الآن", backgroundColor = BrandPrimary, textColor = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun CustomerPortalView(
    customerTab: Int,
    onSelectTab: (Int) -> Unit,
    selectedPartner: PartnerEntity?,
    onSelectPartner: (PartnerEntity?) -> Unit,
    showCartScreen: Boolean,
    onToggleCart: (Boolean) -> Unit,
    homeSections: List<HomeSectionEntity>,
    partners: List<PartnerEntity>,
    products: List<ProductEntity>,
    cartPartner: PartnerEntity?,
    cartItems: Map<ProductEntity, Int>,
    cartOptions: Map<Long, String>,
    appliedCoupon: CouponEntity?,
    discountAmount: Double,
    addresses: List<CustomerAddressEntity>,
    coupons: List<CouponEntity>,
    tickets: List<SupportTicketEntity>,
    currentSession: UserSession?,
    onLogout: () -> Unit,
    orders: List<OrderEntity>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    categoryFilter: com.example.core.model.PartnerType?,
    onCategorySelect: (com.example.core.model.PartnerType?) -> Unit,
    onlyOpenFilter: Boolean,
    onToggleOnlyOpen: () -> Unit,
    onAddToCart: (partner: PartnerEntity, product: ProductEntity, quantity: Int, optionsSummary: String) -> Unit,
    onUpdateCartQty: (product: ProductEntity, delta: Int) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onClearCart: () -> Unit,
    onConfirmOrder: (address: String, notes: String, payment: com.example.core.model.PaymentMethod, receiptNote: String) -> Unit,
    onSelectOrderToTrack: (Long) -> Unit,
    favoritePartnerIds: List<Long> = emptyList(),
    onToggleFavorite: (Long) -> Unit = {},
    onReviewOrder: (Long) -> Unit = {},
    onOpenNotifications: () -> Unit,
    unreadNotifications: Int,
    onAddAddress: (CustomerAddressEntity) -> Unit,
    onDeleteAddress: (CustomerAddressEntity) -> Unit,
    onCreateTicket: (SupportTicketEntity) -> Unit,
    onSwitchRole: (UserRole) -> Unit
) {
    val totalCartItemsCount = remember(cartItems) { cartItems.values.sum() }
    when {
        showCartScreen -> CartCheckoutScreen(
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
        selectedPartner != null -> {
            val partnerProducts = remember(products, selectedPartner) { products.filter { it.partnerId == selectedPartner.id } }
            PartnerDetailScreen(
                partner = selectedPartner,
                products = partnerProducts,
                onBack = { onSelectPartner(null) },
                onAddToCart = { product, quantity, options -> onAddToCart(selectedPartner, product, quantity, options) },
                onViewCart = { onToggleCart(true) },
                cartItemCount = totalCartItemsCount
            )
        }
        else -> Scaffold(
            bottomBar = {
                NavigationBar(containerColor = SurfaceCard, tonalElevation = 6.dp) {
                    val tabs = listOf(
                        Triple("الرئيسية", Icons.Default.Home, 0),
                        Triple("البحث", Icons.Default.Search, 1),
                        Triple("المفضلة", Icons.Default.Favorite, 2),
                        Triple("طلباتي", Icons.Default.ReceiptLong, 3),
                        Triple("حسابي", Icons.Default.Person, 4)
                    )
                    tabs.forEach { (title, icon, index) ->
                        val isSelected = customerTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { onSelectTab(index) },
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
                        onProductClick = { product -> partners.find { it.id == product.partnerId }?.let(onSelectPartner) },
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
                        onProductClick = { product -> partners.find { it.id == product.partnerId }?.let(onSelectPartner) }
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
                        currentSession = currentSession,
                        onLogout = onLogout,
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
