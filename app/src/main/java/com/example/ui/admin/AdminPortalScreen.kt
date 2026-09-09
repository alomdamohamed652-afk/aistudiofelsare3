package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.core.designsystem.*
import com.example.core.model.*
import com.example.data.local.*
import com.example.engine.OrderEngine

@Composable
fun AdminPortalScreen(
    orders: List<OrderEntity>,
    partners: List<PartnerEntity>,
    drivers: List<DriverProfileEntity>,
    driverAccounts: List<UserEntity>,
    homeSections: List<HomeSectionEntity>,
    onboardingPages: List<OnboardingPageEntity>,
    appSettings: AppSettingsEntity?,
    activityLogs: List<OrderActivityLogEntity>,
    onUpdateOrderStatus: (orderId: Long, newStatus: OrderStatus, reason: String, forceOverride: Boolean) -> Unit,
    onAssignDriver: (orderId: Long, driver: DriverProfileEntity) -> Unit,
    onReviewTransfer: (orderId: Long, isApproved: Boolean, reason: String) -> Unit,
    onTogglePartnerStatus: (partnerId: Long, isOpen: Boolean) -> Unit,
    onToggleDriverStatus: (driverId: Long, status: DriverStatus) -> Unit,
    onAddDriver: (DriverProfileEntity) -> Unit,
    onUpdateDriver: (DriverProfileEntity) -> Unit,
    onDeleteDriver: (DriverProfileEntity) -> Unit,
    onSetAccountActivation: (userId: Long, active: Boolean, reason: String) -> Unit,
    onSaveHomeSection: (HomeSectionEntity) -> Unit,
    onDeleteHomeSection: (HomeSectionEntity) -> Unit,
    onSaveOnboardingPage: (OnboardingPageEntity) -> Unit,
    onDeleteOnboardingPage: (OnboardingPageEntity) -> Unit,
    onToggleOnboardingEnabled: (Boolean) -> Unit,
    onSaveAppSettings: (AppSettingsEntity) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var adminSubTab by remember { mutableIntStateOf(0) }
    // 0: لوحة التحكم (Dashboard & Attention Center)
    // 1: الطلبات (Orders Management)
    // 2: الشركاء (Partners)
    // 3: المناديب (Drivers)
    // 4: Home Builder
    // 5: Onboarding
    // 6: الإعدادات والتحويلات (Settings & Transfers)
    // 7: سجل العمليات (Activity Logs)

    var selectedOrderForDetail by remember { mutableStateOf<OrderEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
    ) {
        // Admin Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BrandSecondary,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF6366F1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👑", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "لوحة تحكم إدارة فالسريع ⚡",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "تحكم كامل وفوري في جميع العمليات",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    if (BuildConfig.DEBUG) {
                        // Development-only role switch.
                        OutlinedButton(
                            onClick = { onSwitchRole(UserRole.CUSTOMER) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("👤 العميل", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable subtabs
                ScrollableTabRow(
                    selectedTabIndex = adminSubTab,
                    containerColor = BrandSecondary,
                    contentColor = BrandAccent,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    val tabs = listOf(
                        "📊 المؤشرات والتنبيهات",
                        "📦 إدارة الطلبات",
                        "🏪 الشركاء والمتاجر",
                        "🛵 المناديب والأسطول",
                        "🏗️ مخصص الرئيسية",
                        "🧭 شاشات الترحيب",
                        "💰 التحويلات والإعدادات",
                        "📜 سجل العمليات"
                    )
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = adminSubTab == index,
                            onClick = { adminSubTab = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (adminSubTab == index) BrandAccent else Color.White.copy(alpha = 0.7f),
                                    fontWeight = if (adminSubTab == index) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            }
        }

        // Subtab Content
        Box(modifier = Modifier.weight(1f)) {
            when (adminSubTab) {
                0 -> AdminDashboardTab(
                    orders = orders,
                    partners = partners,
                    drivers = drivers,
                    onGoToOrders = { adminSubTab = 1 },
                    onGoToTransfers = { adminSubTab = 6 },
                    onSelectOrder = { selectedOrderForDetail = it }
                )
                1 -> AdminOrdersTab(
                    orders = orders,
                    drivers = drivers,
                    onSelectOrder = { selectedOrderForDetail = it }
                )
                2 -> AdminPartnersTab(
                    partners = partners,
                    onTogglePartnerStatus = onTogglePartnerStatus
                )
                3 -> AdminDriversTab(
                    drivers = drivers,
                    onToggleDriverStatus = onToggleDriverStatus,
                    onAddDriver = onAddDriver,
                    onUpdateDriver = onUpdateDriver,
                    onDeleteDriver = onDeleteDriver,
                    driverAccounts = driverAccounts,
                    onSetAccountActivation = onSetAccountActivation
                )
                4 -> AdminHomeBuilderTab(
                    sections = homeSections,
                    onSaveSection = onSaveHomeSection,
                    onDeleteSection = onDeleteHomeSection
                )
                5 -> AdminOnboardingTab(
                    pages = onboardingPages,
                    isEnabled = appSettings?.onboardingEnabled ?: true,
                    onToggleEnabled = onToggleOnboardingEnabled,
                    onSavePage = onSaveOnboardingPage,
                    onDeletePage = onDeleteOnboardingPage
                )
                6 -> AdminSettingsAndTransfersTab(
                    orders = orders,
                    appSettings = appSettings,
                    onReviewTransfer = onReviewTransfer,
                    onSaveAppSettings = onSaveAppSettings
                )
                7 -> AdminActivityLogsTab(logs = activityLogs)
            }
        }
    }

    // Admin Order Details & Status Transition Override Dialog
    if (selectedOrderForDetail != null) {
        val order = selectedOrderForDetail!!
        AdminOrderDetailDialog(
            order = order,
            drivers = drivers,
            onDismiss = { selectedOrderForDetail = null },
            onUpdateStatus = { newStatus, reason, override ->
                onUpdateOrderStatus(order.id, newStatus, reason, override)
                selectedOrderForDetail = null
            },
            onAssignDriver = { driver ->
                onAssignDriver(order.id, driver)
                selectedOrderForDetail = null
            }
        )
    }
}

@Composable
fun AdminDashboardTab(
    orders: List<OrderEntity>,
    partners: List<PartnerEntity>,
    drivers: List<DriverProfileEntity>,
    onGoToOrders: () -> Unit,
    onGoToTransfers: () -> Unit,
    onSelectOrder: (OrderEntity) -> Unit
) {
    val totalRevenue = remember(orders) { orders.sumOf { it.total } }
    val ordersInDelivery = remember(orders) {
        orders.count { it.deliveryStatus in listOf(DeliveryStatus.DRIVER_TO_PICKUP, DeliveryStatus.PICKED_UP, DeliveryStatus.OUT_FOR_DELIVERY) }
    }
    val availableDrivers = remember(drivers) { drivers.count { it.status == DriverStatus.AVAILABLE } }
    val activePartners = remember(partners) { partners.count { it.isOpen } }

    // Attention Center Alerts
    val pendingReviewOrders = remember(orders) { orders.filter { it.orderStatus == OrderStatus.PENDING_REVIEW } }
    val unassignedOrders = remember(orders) { orders.filter { it.driverId == null && it.deliveryStatus == DeliveryStatus.WAITING_FOR_DRIVER && it.orderStatus !in listOf(OrderStatus.CANCELLED, OrderStatus.REJECTED) } }
    val pendingTransfers = remember(orders) { orders.filter { it.paymentStatus == PaymentStatus.PENDING_VERIFICATION } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPI Cards Row
        item {
            Text("المؤشرات التشغيلية الحية 📈", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "إجمالي الطلبات",
                    value = "${orders.size}",
                    icon = Icons.Default.Receipt,
                    color = BrandPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onGoToOrders
                )
                MetricCard(
                    title = "قيد التوصيل 🛵",
                    value = "$ordersInDelivery",
                    icon = Icons.Default.ElectricMoped,
                    color = StatusBlue,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "مناديب متاحين",
                    value = "$availableDrivers / ${drivers.size}",
                    icon = Icons.Default.Person,
                    color = StatusGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "شركاء نشطين",
                    value = "$activePartners / ${partners.size}",
                    icon = Icons.Default.Store,
                    color = StatusYellow,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            MetricCard(
                title = "إجمالي إيرادات النظام (اليوم)",
                value = "${totalRevenue.toInt()} ج.م",
                subtitle = "عمولات ورسوم التوصيل والمبيعات",
                icon = Icons.Default.AttachMoney,
                color = StatusGreen,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 🚨 Attention Center Section (Critical operational alerts)
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🚨 مركز التدخل السريع (Attention Center):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusRed)
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (pendingReviewOrders.isEmpty() && unassignedOrders.isEmpty() && pendingTransfers.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StatusGreenLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "جميع العمليات منتظمة ولا توجد طلبات متأخرة أو تحتاج تدخل يدوي حاليًا!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = StatusGreen
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Pending review alert
                    if (pendingReviewOrders.isNotEmpty()) {
                        AlertItem(
                            icon = "🟡",
                            title = "${pendingReviewOrders.size} طلبات بانتظار الاعتماد والمراجعة",
                            subtitle = "اضغط للاعتماد الفوري",
                            actionText = "فحص الطلبات",
                            onAction = { onSelectOrder(pendingReviewOrders.first()) }
                        )
                    }

                    // Unassigned drivers alert
                    if (unassignedOrders.isNotEmpty()) {
                        AlertItem(
                            icon = "🛵",
                            title = "${unassignedOrders.size} طلبات بحاجة لتعيين مندوب",
                            subtitle = "طلبات بدون مندوب مخصص حتى الآن",
                            actionText = "تعيين مندوب",
                            onAction = { onSelectOrder(unassignedOrders.first()) }
                        )
                    }

                    // Bank transfers pending alert
                    if (pendingTransfers.isNotEmpty()) {
                        AlertItem(
                            icon = "💰",
                            title = "${pendingTransfers.size} إيصالات تحويل بنكي بانتظار التحقق",
                            subtitle = "تحويلات انستاباي بحاجة لاعتماد الإدارة",
                            actionText = "مراجعة التحويلات",
                            onAction = onGoToTransfers
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlertItem(
    icon: String,
    title: String,
    subtitle: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            AppButton(
                text = actionText,
                onClick = onAction,
                containerColor = BrandPrimary,
                modifier = Modifier.defaultMinSize(minHeight = 36.dp)
            )
        }
    }
}

@Composable
fun AdminOrdersTab(
    orders: List<OrderEntity>,
    drivers: List<DriverProfileEntity>,
    onSelectOrder: (OrderEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val filteredOrders = remember(orders, searchQuery, selectedStatusFilter) {
        orders.filter { o ->
            val matchQuery = searchQuery.isBlank() ||
                o.orderNumber.contains(searchQuery, ignoreCase = true) ||
                o.customerName.contains(searchQuery, ignoreCase = true) ||
                o.partnerName.contains(searchQuery, ignoreCase = true)
            val matchStatus = selectedStatusFilter == null || o.orderStatus == selectedStatusFilter
            matchQuery && matchStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AppInput(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "بحث في الطلبات",
            placeholder = "رقم الطلب، اسم العميل، أو المحل...",
            leadingIcon = Icons.Default.Search,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null },
                    label = { Text("الكل (${orders.size})") }
                )
            }
            items(OrderStatus.values()) { status ->
                val count = orders.count { it.orderStatus == status }
                FilterChip(
                    selected = selectedStatusFilter == status,
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == status) null else status },
                    label = { Text("${status.titleArabic} ($count)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredOrders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectOrder(order) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(order.orderNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            val (bg, txt) = order.orderStatus.getBadgeColor()
                            StatusBadge(text = order.orderStatus.titleArabic, backgroundColor = bg, textColor = txt)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${order.customerName} • ${order.partnerName}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Text("العنوان: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "المندوب: ${order.driverName ?: "لم يُعين بعد ⚠️"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (order.driverName != null) StatusGreen else StatusYellow,
                                fontWeight = FontWeight.Bold
                            )
                            Text("${order.total.toInt()} ج.م", fontWeight = FontWeight.Bold, color = BrandPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPartnersTab(
    partners: List<PartnerEntity>,
    onTogglePartnerStatus: (partnerId: Long, isOpen: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("إدارة الشركاء والمتاجر (${partners.size}) 🏪", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(partners) { partner ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SurfaceBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(partner.logoEmoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(partner.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("${partner.type.titleArabic} • ${partner.address}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("آلية الاعتماد: ${partner.approvalWorkflow.titleArabic}", style = MaterialTheme.typography.labelSmall, color = BrandPrimary)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = partner.isOpen,
                            onCheckedChange = { onTogglePartnerStatus(partner.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = StatusGreen)
                        )
                        Text(
                            if (partner.isOpen) "مفتوح 🟢" else "مغلق 🔴",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (partner.isOpen) StatusGreen else StatusRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDriversTab(
    drivers: List<DriverProfileEntity>,
    onToggleDriverStatus: (driverId: Long, status: DriverStatus) -> Unit,
    onAddDriver: (DriverProfileEntity) -> Unit,
    onUpdateDriver: (DriverProfileEntity) -> Unit,
    onDeleteDriver: (DriverProfileEntity) -> Unit,
    onForcedBreak: (Long, Int) -> Unit,
    onRestoreDriver: (Long) -> Unit,
    driverAccounts: List<UserEntity>,
    onSetAccountActivation: (userId: Long, active: Boolean, reason: String) -> Unit
) {
    var editing by remember { mutableStateOf<DriverProfileEntity?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("إدارة المناديب وأسطول التوصيل (" + drivers.size + ") 🛵", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            AppButton(text = "+ إضافة مندوب", onClick = { showAdd = true })
        }}
        if (driverAccounts.isNotEmpty()) {
            item { Text("حسابات المناديب والتفعيل", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall) }
            items(driverAccounts.filter { !it.isActive }) { account ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceCard)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(account.name, fontWeight = FontWeight.Bold)
                        Text(account.phone + " • " + account.activationStatus, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppButton(text = "تفعيل الحساب", onClick = { onSetAccountActivation(account.id, true, "") }, modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { onSetAccountActivation(account.id, false, "مرفوض من الإدارة") }, modifier = Modifier.weight(1f)) { Text("رفض") }
                        }
                    }
                }
            }
        }
        items(drivers) { driver ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceCard), border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(driver.name, fontWeight = FontWeight.Bold)
                    Text(driver.vehicle + " • " + driver.phone, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("المنطقة: " + driver.workingArea, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppButton(text = "تعديل", onClick = { editing = driver }, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { onToggleDriverStatus(driver.id, if (driver.status == DriverStatus.SUSPENDED) DriverStatus.AVAILABLE else DriverStatus.SUSPENDED) }, modifier = Modifier.weight(1f)) { Text(if (driver.status == DriverStatus.SUSPENDED) "إلغاء الإيقاف" else "إيقاف") }
                        IconButton(onClick = { onDeleteDriver(driver) }) { Icon(Icons.Default.Delete, "حذف", tint = StatusRed) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (driver.status == DriverStatus.BREAK) {
                            OutlinedButton(onClick = { onRestoreDriver(driver.id) }, modifier = Modifier.weight(1f)) { Text("إنهاء الاستراحة") }
                        } else {
                            OutlinedButton(onClick = { onForcedBreak(driver.id, 30) }, modifier = Modifier.weight(1f)) { Text("استراحة 30 دقيقة") }
                        }
                    }
                }
            }
        }
    }
    if (showAdd || editing != null) {
        val current = editing
        var name by remember(current) { mutableStateOf(current?.name ?: "") }
        var phone by remember(current) { mutableStateOf(current?.phone ?: "") }
        var vehicle by remember(current) { mutableStateOf(current?.vehicle ?: "") }
        var area by remember(current) { mutableStateOf(current?.workingArea ?: "") }
        AlertDialog(onDismissRequest = { showAdd=false; editing=null }, title={ Text(if(current==null) "إضافة مندوب" else "تعديل بيانات المندوب") },
            text={ Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppInput(name,{name=it},"الاسم",modifier=Modifier.fillMaxWidth())
                AppInput(phone,{phone=it},"رقم الهاتف",modifier=Modifier.fillMaxWidth())
                AppInput(vehicle,{vehicle=it},"المركبة",modifier=Modifier.fillMaxWidth())
                AppInput(area,{area=it},"منطقة العمل",modifier=Modifier.fillMaxWidth())
            }},
            confirmButton={ TextButton(onClick={ if(name.isNotBlank()&&phone.isNotBlank()){
                val value=current?.copy(name=name.trim(),phone=phone.trim(),vehicle=vehicle.trim(),workingArea=area.trim()) ?: DriverProfileEntity(name=name.trim(),phone=phone.trim(),vehicle=vehicle.trim(),workingArea=area.trim(),status=DriverStatus.OFFLINE)
                if(current==null) onAddDriver(value) else onUpdateDriver(value)
                showAdd=false; editing=null
            }}){Text("حفظ")}},
            dismissButton={TextButton(onClick={showAdd=false;editing=null}){Text("إلغاء")}})
    }
}
@Composable
fun AdminHomeBuilderTab(
    sections: List<HomeSectionEntity>,
    onSaveSection: (HomeSectionEntity) -> Unit,
    onDeleteSection: (HomeSectionEntity) -> Unit
) {
    var showAddSectionDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تخصيص الصفحة الرئيسية (Home Builder) 🏗️", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                AppButton(text = "+ إضافة قسم", onClick = { showAddSectionDialog = true })
            }
        }

        items(sections) { sec ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sec.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("النوع: ${sec.type.titleArabic} • ترتيب: ${sec.sortOrder}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = sec.active,
                            onCheckedChange = { onSaveSection(sec.copy(active = it)) }
                        )
                        IconButton(onClick = { onDeleteSection(sec) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = StatusRed)
                        }
                    }
                }
            }
        }
    }

    if (showAddSectionDialog) {
        var title by remember { mutableStateOf("") }
        var type by remember { mutableStateOf(HomeSectionType.NEARBY_PARTNERS) }

        AlertDialog(
            onDismissRequest = { showAddSectionDialog = false },
            title = { Text("إضافة قسم جديد للرئيسية", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppInput(value = title, onValueChange = { title = it }, label = "عنوان القسم")
                }
            },
            confirmButton = {
                AppButton(
                    text = "إضافة",
                    onClick = {
                        if (title.isNotBlank()) {
                            onSaveSection(
                                HomeSectionEntity(
                                    title = title,
                                    type = type,
                                    sortOrder = sections.size + 1,
                                    active = true
                                )
                            )
                            showAddSectionDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddSectionDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun AdminOnboardingTab(
    pages: List<OnboardingPageEntity>,
    isEnabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onSavePage: (OnboardingPageEntity) -> Unit,
    onDeletePage: (OnboardingPageEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("تفعيل شاشات الترحيب (Onboarding)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("إظهار الشرائح التعريفية للعملاء الجدد", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(checked = isEnabled, onCheckedChange = onToggleEnabled)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الشرائح التعريفية (${pages.size}) 🧭", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                AppButton(text = "+ شريحة جديدة", onClick = { showAddDialog = true })
            }
        }

        items(pages) { page ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(page.iconEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(page.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(page.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    IconButton(onClick = { onDeletePage(page) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = StatusRed)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var emoji by remember { mutableStateOf("⚡") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة شريحة ترحيب", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppInput(value = emoji, onValueChange = { emoji = it }, label = "الأيقونة (إيموجي)")
                    AppInput(value = title, onValueChange = { title = it }, label = "العنوان الرئيسي")
                    AppInput(value = desc, onValueChange = { desc = it }, label = "الوصف", singleLine = false)
                }
            },
            confirmButton = {
                AppButton(
                    text = "حفظ",
                    onClick = {
                        if (title.isNotBlank()) {
                            onSavePage(
                                OnboardingPageEntity(
                                    sortOrder = pages.size + 1,
                                    title = title,
                                    description = desc,
                                    iconEmoji = emoji,
                                    active = true
                                )
                            )
                            showAddDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun AdminSettingsAndTransfersTab(
    orders: List<OrderEntity>,
    appSettings: AppSettingsEntity?,
    onReviewTransfer: (orderId: Long, isApproved: Boolean, reason: String) -> Unit,
    onSaveAppSettings: (AppSettingsEntity) -> Unit
) {
    val settings = appSettings ?: AppSettingsEntity()
    var baseDeliveryFee by remember(settings) { mutableStateOf(settings.baseDeliveryFee.toString()) }
    var pricePerKm by remember(settings) { mutableStateOf(settings.pricePerKm.toString()) }
    var minimumOrderAmount by remember(settings) { mutableStateOf(settings.minimumOrderAmount.toString()) }
    var extraPickupFee by remember(settings) { mutableStateOf(settings.extraPickupFee.toString()) }
    var driverOfferTimeout by remember(settings) { mutableStateOf(settings.driverOfferTimeoutSeconds.toString()) }
    var busyDriversOpenDispatch by remember(settings) { mutableStateOf(settings.busyDriversOpenDispatch) }
    val pendingTransfers = remember(orders) {
        orders.filter { it.paymentMethod == PaymentMethod.BANK_TRANSFER && it.paymentStatus == PaymentStatus.PENDING_VERIFICATION }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Pending transfers
        item {
            Text("مراجعة التحويلات البنكية وانستاباي (${pendingTransfers.size}) 💰", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (pendingTransfers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Text("لا توجد تحويلات معلقة بحاجة للتحقق حاليًا ✅", modifier = Modifier.padding(16.dp), color = StatusGreen, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            items(pendingTransfers) { ord ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ord.orderNumber, fontWeight = FontWeight.Bold)
                            Text("${ord.total.toInt()} ج.م", fontWeight = FontWeight.Bold, color = BrandPrimary)
                        }
                        Text("العميل: ${ord.customerName} (${ord.customerPhone})", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceBackground),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "إشعار العميل: ${ord.transferReceiptNote.ifBlank { "تم إرفاق إيصال التحويل" }}",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppButton(
                                text = "اعتماد التحويل ✅",
                                onClick = { onReviewTransfer(ord.id, true, "تم التحقق من استلام المبلغ بحساب الشركة") },
                                containerColor = StatusGreen,
                                modifier = Modifier.weight(1f)
                            )
                            AppOutlinedButton(
                                text = "رفض الإيصال ❌",
                                onClick = { onReviewTransfer(ord.id, false, "لم يتم العثور على التحويل بالحساب البنكي") },
                                borderColor = StatusRed,
                                contentColor = StatusRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text("إعدادات التسعير والتشغيل ⚙️", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceCard), border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("التسعير الأساسي", fontWeight = FontWeight.Bold)
                    AppInput(value = baseDeliveryFee, onValueChange = { baseDeliveryFee = it }, label = "سعر بداية التوصيل (ج.م)", modifier = Modifier.fillMaxWidth())
                    AppInput(value = pricePerKm, onValueChange = { pricePerKm = it }, label = "سعر الكيلو الإضافي (ج.م)", modifier = Modifier.fillMaxWidth())
                    AppInput(value = minimumOrderAmount, onValueChange = { minimumOrderAmount = it }, label = "الحد الأدنى للطلب (0 = بدون حد)", modifier = Modifier.fillMaxWidth())
                    AppInput(value = extraPickupFee, onValueChange = { extraPickupFee = it }, label = "رسوم كل جهة استلام إضافية (ج.م)", modifier = Modifier.fillMaxWidth())
                    Divider()
                    Text("توزيع الطلبات", fontWeight = FontWeight.Bold)
                    AppInput(value = driverOfferTimeout, onValueChange = { driverOfferTimeout = it.filter(Char::isDigit) }, label = "مهلة قبول المندوب بالثواني", modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = busyDriversOpenDispatch, onCheckedChange = { busyDriversOpenDispatch = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("عند انشغال جميع المناديب: عرض الطلب للجميع")
                    }
                    AppButton(text = "حفظ إعدادات التشغيل 💾", onClick = {
                        onSaveAppSettings(settings.copy(
                            baseDeliveryFee = baseDeliveryFee.toDoubleOrNull() ?: settings.baseDeliveryFee,
                            pricePerKm = pricePerKm.toDoubleOrNull() ?: settings.pricePerKm,
                            minimumOrderAmount = minimumOrderAmount.toDoubleOrNull() ?: settings.minimumOrderAmount,
                            extraPickupFee = extraPickupFee.toDoubleOrNull() ?: settings.extraPickupFee,
                            driverOfferTimeoutSeconds = (driverOfferTimeout.toIntOrNull() ?: settings.driverOfferTimeoutSeconds).coerceAtLeast(5),
                            busyDriversOpenDispatch = busyDriversOpenDispatch
                        ))
                    }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun AdminActivityLogsTab(logs: List<OrderActivityLogEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("سجل الرقابة وتتبع العمليات (Audit Logs) 📜", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (logs.isEmpty()) {
            item {
                Text("لا توجد سجلات مسجلة حتى الآن.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        } else {
            items(logs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${log.actor} (${log.actorRole})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text(log.timeFormatted, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${log.action}: من [${log.oldValue}] إلى [${log.newValue}]",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandPrimary
                        )
                        if (log.reason.isNotBlank()) {
                            Text("السبب: ${log.reason}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderDetailDialog(
    order: OrderEntity,
    drivers: List<DriverProfileEntity>,
    onDismiss: () -> Unit,
    onUpdateStatus: (newStatus: OrderStatus, reason: String, override: Boolean) -> Unit,
    onAssignDriver: (driver: DriverProfileEntity) -> Unit
) {
    var selectedNewStatus by remember { mutableStateOf(order.orderStatus) }
    var changeReason by remember { mutableStateOf("") }
    var forceOverride by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.orderNumber, fontWeight = FontWeight.Bold)
                val (bg, txt) = order.orderStatus.getBadgeColor()
                StatusBadge(text = order.orderStatus.titleArabic, backgroundColor = bg, textColor = txt)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("العميل: ${order.customerName} (${order.customerPhone})", style = MaterialTheme.typography.bodyMedium)
                    Text("المتجر: ${order.partnerName}", style = MaterialTheme.typography.bodyMedium)
                    Text("العنوان: ${order.deliveryAddress}", style = MaterialTheme.typography.bodyMedium)
                    Text("المندوب الحالي: ${order.driverName ?: "لم يُعين"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }

                item {
                    Divider(color = SurfaceBorder)
                    Text("تغيير حالة الطلب (Order Engine State Machine):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    OrderStatus.values().forEach { st ->
                        val isCurrent = order.orderStatus == st
                        val isValidNext = OrderEngine.isValidOrderStatusTransition(order.orderStatus, st)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedNewStatus = st }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedNewStatus == st,
                                onClick = { selectedNewStatus = st }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = st.titleArabic + if (isCurrent) " (الحالية)" else if (isValidNext) " (متاح)" else " (تجاوز يدوي)",
                                color = if (isCurrent) BrandPrimary else if (isValidNext) TextPrimary else TextMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                item {
                    AppInput(
                        value = changeReason,
                        onValueChange = { changeReason = it },
                        label = "سبب التغيير (إلزامي للرقابة)",
                        placeholder = "مثال: طلب العميل تعديل الموعد..."
                    )
                }

                item {
                    Divider(color = SurfaceBorder)
                    Text("تعيين مندوب للطلب يدويًا:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    drivers.forEach { d ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(d.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("${d.vehicle} • ${d.status.titleArabic}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            AppButton(
                                text = "تعيين 🛵",
                                onClick = { onAssignDriver(d) },
                                containerColor = BrandPrimary,
                                modifier = Modifier.defaultMinSize(minHeight = 32.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            AppButton(
                text = "حفظ تغيير الحالة",
                onClick = {
                    val isValid = OrderEngine.isValidOrderStatusTransition(order.orderStatus, selectedNewStatus)
                    onUpdateStatus(selectedNewStatus, changeReason, !isValid)
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}
