package com.example.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.*
import com.example.core.model.DeliveryStatus
import com.example.core.model.DriverStatus
import com.example.core.model.OrderStatus
import com.example.core.model.UserRole
import com.example.data.local.DriverPayoutRequestEntity
import com.example.data.local.DriverProfileEntity
import com.example.data.local.DriverShiftAssignmentEntity
import com.example.data.local.OrderEntity

@Composable
fun DriverPortalScreen(
    driver: DriverProfileEntity?,
    activeOrder: OrderEntity?,
    openOrders: List<OrderEntity>,
    offerExpiryByOrderId: Map<Long, Long> = emptyMap(),
    onOfferTimeout: (OrderEntity) -> Unit = {},
    driverPerformance: com.example.data.local.DriverPerformanceEntity? = null,
    driverOrders: List<OrderEntity> = emptyList(),
    payoutRequests: List<DriverPayoutRequestEntity> = emptyList(),
    shiftAssignment: DriverShiftAssignmentEntity? = null,
    onRequestPayout: (Double) -> Unit = {},
    onToggleAvailability: (DriverStatus) -> Unit,
    onAcceptOrder: (OrderEntity) -> Unit,
    onRejectOrder: (OrderEntity, String) -> Unit,
    onConfirmPickup: (OrderEntity) -> Unit,
    onConfirmDelivered: (OrderEntity) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDriverTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Orders, 2: Wallet, 3: Profile

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DriverDarkCard,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Triple("الرئيسية", Icons.Default.Home, 0),
                    Triple("الطلبات", Icons.Default.ReceiptLong, 1),
                    Triple("المحفظة", Icons.Default.AccountBalanceWallet, 2),
                    Triple("حسابي", Icons.Default.Person, 3)
                )

                tabs.forEach { (title, icon, idx) ->
                    val isSelected = selectedDriverTab == idx
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedDriverTab = idx },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) DriverPrimaryOrange else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                color = if (isSelected) DriverPrimaryOrange else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Driver Top Header (Driver Night Palette: Dark Canvas with Orange & Yellow accents)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DriverDarkCanvas,
                shadowElevation = 3.dp
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(DriverPrimaryOrange.copy(alpha = 0.2f))
                                    .border(1.5.dp, DriverPrimaryOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🛵", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = driver?.name ?: "مندوب غير معروف",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = driver?.vehicle?.takeIf { it.isNotBlank() } ?: "لا توجد بيانات مركبة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Availability Toggle Switch (متصل / غير متصل)
                        val isAvailable = driver?.status == DriverStatus.AVAILABLE
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isAvailable) StatusGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAvailable) StatusGreen else Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.clickable(enabled = driver?.status in setOf(DriverStatus.OFFLINE, DriverStatus.AVAILABLE)) {
                                onToggleAvailability(if (isAvailable) DriverStatus.OFFLINE else DriverStatus.AVAILABLE)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isAvailable) StatusGreen else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAvailable) "متصل 🟢" else "غير متصل ⏸️",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Stats Pill (Today orders, earnings, rating)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = DriverDarkCard
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("طلبات اليوم", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                Text("${driver?.completedOrdersCount ?: 0} رحلات", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("دخل اليوم", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                Text("${driver?.todayEarnings?.toInt() ?: 0} ج.م", fontWeight = FontWeight.ExtraBold, color = DriverAccentYellow, style = MaterialTheme.typography.bodyMedium)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.15f)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("التقييم", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                Text(driver?.rating?.let { "⭐ $it" } ?: "—", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            shiftAssignment?.let { assignment ->
                val now = System.currentTimeMillis()
                val breakActive = assignment.forcedBreakUntil > now
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الشيفت", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                            Text(assignment.shiftName, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ترتيب الدور", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                            Text(if (assignment.active) "#${assignment.queuePosition}" else "غير نشط", color = DriverAccentYellow, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("الحالة", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                            Text(
                                if (breakActive) "استراحة" else if (assignment.active) "نشط" else "خارج الدور",
                                color = if (breakActive) DriverAccentYellow else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Main Content Body based on selectedDriverTab
            when (selectedDriverTab) {
                0 -> DriverHomeScreen(
                    activeOrder = activeOrder,
                    openOrders = openOrders,
                    onAcceptOrder = onAcceptOrder,
                    onRejectOrder = onRejectOrder,
                    onConfirmPickup = onConfirmPickup,
                    onConfirmDelivered = onConfirmDelivered,
                    driverPerformance = driverPerformance
                )
                1 -> DriverOrdersHistoryScreen(
                    activeOrder = activeOrder,
                    allDriverOrders = driverOrders,
                    openOrders = openOrders,
                    onAcceptOrder = onAcceptOrder,
                    onRejectOrder = onRejectOrder
                )
                2 -> DriverWalletScreen(
                    driver = driver,
                    payoutRequests = payoutRequests,
                    onRequestPayout = onRequestPayout
                )
                3 -> DriverProfileScreen(
                    driver = driver,
                    onSwitchRole = { onSwitchRole(UserRole.CUSTOMER) }
                )
            }
        }
    }
}

@Composable
fun DriverHomeScreen(
    activeOrder: OrderEntity?,
    openOrders: List<OrderEntity>,
    offerExpiryByOrderId: Map<Long, Long> = emptyMap(),
    onOfferTimeout: (OrderEntity) -> Unit = {},
    onAcceptOrder: (OrderEntity) -> Unit,
    onRejectOrder: (OrderEntity, String) -> Unit,
    onConfirmPickup: (OrderEntity) -> Unit,
    onConfirmDelivered: (OrderEntity) -> Unit,
    driverPerformance: com.example.data.local.DriverPerformanceEntity? = null
) {
    var rejectOrder by remember { mutableStateOf<OrderEntity?>(null) }
    val now by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    openOrders.forEach { order ->
        val expiresAt = offerExpiryByOrderId[order.id] ?: 0L
        if (expiresAt > 0L) {
            LaunchedEffect(order.id, expiresAt) {
                val remaining = expiresAt - System.currentTimeMillis()
                if (remaining > 0L) kotlinx.coroutines.delay(remaining)
                onOfferTimeout(order)
            }
        }
    }

    rejectOrder?.let { order ->
        val reasons = listOf("بعيد جدًا", "مشكلة بالمركبة", "نهاية الشيفت", "ظروف شخصية", "سبب آخر")
        var selectedReason by remember(order.id) { mutableStateOf(reasons.first()) }
        AlertDialog(
            onDismissRequest = { rejectOrder = null },
            title = { Text("سبب رفض الطلب") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("يجب اختيار سبب قبل رفض الطلب.", color = TextSecondary)
                    reasons.forEach { reason ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { selectedReason = reason },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason })
                            Text(reason)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onRejectOrder(order, selectedReason)
                    rejectOrder = null
                }) { Text("تأكيد الرفض") }
            },
            dismissButton = { TextButton(onClick = { rejectOrder = null }) { Text("إلغاء") } }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ملخص الأداء", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("تم القبول: ${driverPerformance?.totalAccepted ?: 0} • تم الرفض: ${driverPerformance?.totalRejected ?: 0}")
                    Text("انتهت المهلة: ${driverPerformance?.totalTimeouts ?: 0}")
                    Text("رفضات متتالية: ${driverPerformance?.consecutiveRejects ?: 0} • مهلات متتالية: ${driverPerformance?.consecutiveTimeouts ?: 0}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
        // Active Order Card (If driver has an ongoing task)
        if (activeOrder != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مهمتك الحالية قيد التنفيذ ⚡",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DriverPrimaryOrange
                    )
                    val (bg, txt) = activeOrder.deliveryStatus.getBadgeColor()
                    StatusBadge(text = activeOrder.deliveryStatus.titleArabic, backgroundColor = bg, textColor = txt)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(2.dp, DriverPrimaryOrange)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeOrder.orderNumber,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${activeOrder.deliveryFee.toInt()} ج.م أجرة",
                                fontWeight = FontWeight.Bold,
                                color = StatusGreen,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pickup Store
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🏪 الاستلام من المتجر:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(activeOrder.partnerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Customer Dropoff
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("📍 التسليم للعميل:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text("${activeOrder.customerName} • ${activeOrder.customerPhone}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text(activeOrder.deliveryAddress, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("المطلوب تحصيله:", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Text(
                                text = "${activeOrder.total.toInt()} ج.م (كاش)",
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Modern Swipe-to-confirm action button
                        if (activeOrder.deliveryStatus in listOf(DeliveryStatus.DRIVER_ASSIGNED, DeliveryStatus.DRIVER_TO_PICKUP)) {
                            SwipeToConfirmButton(
                                text = "اسحب لتأكيد استلام الطلب من المحل 📦",
                                onConfirmed = { onConfirmPickup(activeOrder) },
                                backgroundColor = DriverPrimaryOrange,
                                testTag = "swipe_confirm_pickup"
                            )
                        } else if (activeOrder.deliveryStatus in listOf(DeliveryStatus.PICKED_UP, DeliveryStatus.OUT_FOR_DELIVERY)) {
                            SwipeToConfirmButton(
                                text = "اسحب لتأكيد التسليم واستلام الحساب 🟢",
                                onConfirmed = { onConfirmDelivered(activeOrder) },
                                backgroundColor = StatusGreen,
                                testTag = "swipe_confirm_delivered"
                            )
                        }
                    }
                }
            }
        }

        // Radar / Open Dispatch Orders
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "طلبات متاحة للاستلام 📡",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${openOrders.size} طلبات جاهزة",
                    style = MaterialTheme.typography.labelSmall,
                    color = DriverPrimaryOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (openOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📡", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "رادار الطلبات نشط ويعمل ⚡",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "لا توجد طلبات جديدة بانتظار مندوب حاليًا. ستصلك إشعارات فورية عند توفر طلب قريب منك.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(openOrders, key = { it.id }) { ord ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ord.orderNumber,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusGreenLight
                            ) {
                                Text(
                                    text = "+${ord.deliveryFee.toInt()} ج.م توصيل",
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "من: ${ord.partnerName}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "إلى: ${ord.deliveryAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        val expiresAt = offerExpiryByOrderId[ord.id] ?: 0L
                        if (expiresAt > 0L) {
                            val remainingSeconds = ((expiresAt - now).coerceAtLeast(0L) + 999L) / 1000L
                            Text(
                                text = "متبقي لقبول العرض: ${remainingSeconds} ثانية",
                                color = if (remainingSeconds <= 10) Color.Red else DriverPrimaryOrange,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SwipeToConfirmButton(
                            text = "اسحب لقبول الطلب والتحرك ⚡",
                            onConfirmed = { onAcceptOrder(ord) },
                            backgroundColor = DriverPrimaryOrange,
                            testTag = "driver_accept_order_btn_${ord.id}"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { rejectOrder = ord },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("رفض الطلب") }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverOrdersHistoryScreen(
    activeOrder: OrderEntity?,
    allDriverOrders: List<OrderEntity>,
    openOrders: List<OrderEntity>,
    onAcceptOrder: (OrderEntity) -> Unit
) {
    var historyTab by remember { mutableIntStateOf(0) } // 0: Past delivered, 1: Open dispatch

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "سجل رحلات التوصيل 🛵",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = historyTab,
            containerColor = SurfaceCard,
            contentColor = DriverPrimaryOrange,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = historyTab == 0,
                onClick = { historyTab = 0 },
                text = { Text("المكتملة والسابقة", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = historyTab == 1,
                onClick = { historyTab = 1 },
                text = { Text("الطلبات المتاحة (${openOrders.size})", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (historyTab == 0) {
            val deliveredOrders = allDriverOrders.filter {
                it.deliveryStatus == DeliveryStatus.DELIVERED || it.orderStatus == OrderStatus.DELIVERED
            }

            if (deliveredOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد رحلات سابقة مكتملة بعد", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(deliveredOrders) { ord ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ord.orderNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    StatusBadge(text = "تم التسليم بنجاح 🟢", backgroundColor = StatusGreenLight, textColor = StatusGreen)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${ord.partnerName} ⬅️ ${ord.customerName}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("أجرة التوصيل المحققة:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text("${ord.deliveryFee.toInt()} ج.م", fontWeight = FontWeight.Bold, color = StatusGreen)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(openOrders) { ord ->
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
                                Text("${ord.deliveryFee.toInt()} ج.م", fontWeight = FontWeight.Bold, color = StatusGreen)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("من: ${ord.partnerName}", style = MaterialTheme.typography.bodySmall)
                            Text("إلى: ${ord.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onAcceptOrder(ord) },
                                colors = ButtonDefaults.buttonColors(containerColor = DriverPrimaryOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("قبول الطلب ⚡")
                            }
                        }
                    }
                }
            }
        }
    }
}
