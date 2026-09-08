package com.example.ui.customer

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
import com.example.core.model.OrderStatus
import com.example.data.local.OrderActivityLogEntity
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemEntity
import com.example.engine.OrderEngine

@Composable
fun CustomerOrdersScreen(
    orders: List<OrderEntity>,
    onSelectOrderToTrack: (Long) -> Unit,
    onReviewOrder: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Past

    val activeOrders = remember(orders) {
        orders.filter { it.orderStatus !in listOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.REJECTED) }
    }
    val pastOrders = remember(orders) {
        orders.filter { it.orderStatus in listOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.REJECTED) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "طلباتي 📦",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceCard,
            contentColor = BrandPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("طلبات جارية (${activeOrders.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("طلبات سابقة (${pastOrders.size})", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        val currentList = if (selectedTab == 0) activeOrders else pastOrders

        if (currentList.isEmpty()) {
            EmptyState(
                title = if (selectedTab == 0) "لا توجد طلبات جارية" else "لا توجد طلبات سابقة",
                message = "ابدأ طلبك الآن من أشهى المطاعم والصيدليات واستمتع بالتوصيل السريع ⚡",
                icon = Icons.Default.ReceiptLong
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(currentList) { order ->
                    CustomerOrderCard(
                        order = order,
                        onTrackClick = { onSelectOrderToTrack(order.id) },
                        onReviewClick = { onReviewOrder(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerOrderCard(
    order: OrderEntity,
    onTrackClick: () -> Unit,
    onReviewClick: () -> Unit = {}
) {
    val (statusBg, statusText) = order.orderStatus.getBadgeColor()
    val isDelivered = order.orderStatus == OrderStatus.DELIVERED || order.deliveryStatus == DeliveryStatus.DELIVERED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTrackClick)
            .testTag("customer_order_card_${order.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CustomerDarkNavy
                )
                StatusBadge(
                    text = order.orderStatus.titleArabic,
                    backgroundColor = statusBg,
                    textColor = statusText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${order.partnerType.iconEmoji} ${order.partnerName}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Friendly humanized message
            Text(
                text = OrderEngine.getHumanizedTrackingMessage(order.orderStatus, order.deliveryStatus),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = SurfaceBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الإجمالي: ${order.total.toInt()} ج.م",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CustomerPrimaryBlue
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isDelivered) {
                        Button(
                            onClick = onReviewClick,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusYellow),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("⭐ قيّم", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    AppOutlinedButton(
                        text = "التتبع والتفاصيل 📍",
                        onClick = onTrackClick,
                        testTag = "track_order_btn_${order.id}"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveOrderTrackingSheet(
    order: OrderEntity,
    items: List<OrderItemEntity>,
    activityLogs: List<OrderActivityLogEntity>,
    onCancelOrder: () -> Unit,
    onDismiss: () -> Unit
) {
    val canCancel = order.orderStatus in listOf(OrderStatus.CREATED, OrderStatus.PENDING_REVIEW)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceCard
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentPadding = PaddingValues(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(order.orderNumber, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(order.partnerName, style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                    }
                    val (bg, txt) = order.orderStatus.getBadgeColor()
                    StatusBadge(text = order.orderStatus.titleArabic, backgroundColor = bg, textColor = txt)
                }
            }

            // Live status banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CustomerPrimaryBlue.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CustomerPrimaryBlue.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚡", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = OrderEngine.getHumanizedTrackingMessage(order.orderStatus, order.deliveryStatus),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "الوقت المتوقع للوصول: 15 - 20 دقيقة",
                                style = MaterialTheme.typography.labelSmall,
                                color = CustomerPrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Simulated Live Map Route (Matching Mockup Screen 11)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EEF5)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background road simulation
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            // Draw simulated streets
                            drawRect(
                                color = Color(0xFFD6E2F0),
                                topLeft = androidx.compose.ui.geometry.Offset(0f, h * 0.35f),
                                size = androidx.compose.ui.geometry.Size(w, 24.dp.toPx())
                            )
                            drawRect(
                                color = Color(0xFFD6E2F0),
                                topLeft = androidx.compose.ui.geometry.Offset(w * 0.45f, 0f),
                                size = androidx.compose.ui.geometry.Size(24.dp.toPx(), h)
                            )
                            // Route line from store to customer
                            drawLine(
                                color = CustomerPrimaryBlue,
                                start = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.4f),
                                end = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.6f),
                                strokeWidth = 8f,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                            )
                        }

                        // Store Pin (Left)
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 3.dp
                            ) {
                                Text(
                                    text = "🏪",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Text(
                                text = order.partnerName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Driver Moving Scooter (Center)
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(bottom = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CustomerDarkNavy,
                                shadowElevation = 4.dp
                            ) {
                                Text(
                                    text = "🛵",
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CustomerPrimaryBlue
                            ) {
                                Text(
                                    text = "الكابتن في الطريق",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Customer Pin (Right)
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 3.dp
                            ) {
                                Text(
                                    text = "📍",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                            Text(
                                text = "موقعك",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Timeline Steps
            item {
                Text("مراحل الطلب والتوصيل:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                val stages = listOf(
                    Pair("تم استلام الطلب", order.orderStatus.ordinal >= OrderStatus.CREATED.ordinal),
                    Pair("تم الاعتماد والمراجعة", order.orderStatus.ordinal >= OrderStatus.APPROVED.ordinal),
                    Pair("جاري التجهيز", order.orderStatus.ordinal >= OrderStatus.PREPARING.ordinal),
                    Pair("جاهز للاستلام", order.orderStatus.ordinal >= OrderStatus.READY_FOR_PICKUP.ordinal),
                    Pair("المندوب استلم الطلب وفي الطريق 🛵", order.deliveryStatus.ordinal >= DeliveryStatus.PICKED_UP.ordinal),
                    Pair("تم التسليم بنجاح 🟢", order.deliveryStatus == DeliveryStatus.DELIVERED || order.orderStatus == OrderStatus.DELIVERED)
                )

                stages.forEachIndexed { index, (label, isReached) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isReached) StatusGreen else StatusGrayLight),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isReached) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isReached) FontWeight.Bold else FontWeight.Normal,
                            color = if (isReached) TextPrimary else TextMuted
                        )
                    }
                    if (index < stages.size - 1) {
                        Box(
                            modifier = Modifier
                                .padding(start = 11.dp)
                                .width(2.dp)
                                .height(16.dp)
                                .background(if (stages[index + 1].second) StatusGreen else StatusGrayLight)
                        )
                    }
                }
            }

            // Driver Card if assigned
            if (order.driverName != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(StatusGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🛵", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(order.driverName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("مندوب فالسريع • 4.9 ⭐", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            AppButton(
                                text = "اتصال 📞",
                                onClick = {},
                                containerColor = StatusGreen
                            )
                        }
                    }
                }
            }

            // Order items breakdown
            item {
                Text("الأصناف (${items.size}):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.quantity}x ${item.productName}", style = MaterialTheme.typography.bodySmall)
                        Text("${item.totalPrice.toInt()} ج.م", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Divider(color = SurfaceBorder)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الإجمالي الكلي المدفوع:", fontWeight = FontWeight.Bold)
                    Text("${order.total.toInt()} ج.م", fontWeight = FontWeight.ExtraBold, color = BrandPrimary)
                }
            }

            // Cancel button if allowed
            if (canCancel) {
                item {
                    AppOutlinedButton(
                        text = "إلغاء الطلب ❌",
                        onClick = onCancelOrder,
                        borderColor = StatusRed,
                        contentColor = StatusRed,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "cancel_order_sheet_btn"
                    )
                }
            }
        }
    }
}
