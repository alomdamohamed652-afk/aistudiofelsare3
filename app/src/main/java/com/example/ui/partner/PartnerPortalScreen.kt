package com.example.ui.partner

import androidx.compose.foundation.background
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
import com.example.BuildConfig
import com.example.core.designsystem.*
import com.example.BuildConfig
import com.example.core.model.OrderStatus
import com.example.core.model.ProductStatus
import com.example.core.model.UserRole
import com.example.data.local.PartnerEntity
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity

@Composable
fun PartnerPortalScreen(
    allPartners: List<PartnerEntity>,
    activePartner: PartnerEntity?,
    orders: List<OrderEntity>,
    products: List<ProductEntity>,
    onSelectPartner: (Long) -> Unit,
    onToggleOpen: (Boolean) -> Unit,
    onAcceptOrder: (OrderEntity) -> Unit,
    onRejectOrder: (OrderEntity, reason: String) -> Unit,
    onStartPreparing: (OrderEntity, prepMinutes: Int) -> Unit,
    onReadyForPickup: (OrderEntity) -> Unit,
    onSaveProduct: (ProductEntity) -> Unit,
    onUpdateProductStatus: (Long, ProductStatus) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var partnerTab by remember { mutableIntStateOf(0) } // 0: Orders Queue, 1: Products Menu, 2: Reports
    var showAddProductDialog by remember { mutableStateOf(false) }

    val partnerOrders = remember(orders, activePartner) {
        if (activePartner == null) emptyList()
        else orders.filter { it.partnerId == activePartner.id }
    }

    val actionRequiredOrders = remember(partnerOrders) {
        partnerOrders.filter { it.orderStatus in listOf(OrderStatus.PENDING_REVIEW, OrderStatus.APPROVED, OrderStatus.PREPARING) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
    ) {
        // Partner Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BrandSecondary,
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(activePartner?.logoEmoji ?: "🏪", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = activePartner?.name ?: "تطبيق الشريك",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${activePartner?.type?.titleArabic ?: "شريك"} • ${activePartner?.address ?: ""}",
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
                // Partner Switcher (To easily test Restaurant vs Pharmacy vs Cafe vs Store)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (BuildConfig.DEBUG) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("تبديل الشريك:", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.width(6.dp))
                            allPartners.take(4).forEach { p ->
                                val isChosen = activePartner?.id == p.id
                                Text(
                                    text = p.logoEmoji,
                                    fontSize = 20.sp,
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clip(CircleShape)
                                        .background(if (isChosen) BrandPrimary else Color.Transparent)
                                        .clickable { onSelectPartner(p.id) }
                                        .padding(4.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Store open/close switch
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (activePartner?.isOpen == true) "مفتوح 🟢" else "مغلق 🔴",
                            color = if (activePartner?.isOpen == true) StatusGreen else StatusRed,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = activePartner?.isOpen == true,
                            onCheckedChange = onToggleOpen,
                            colors = SwitchDefaults.colors(checkedThumbColor = StatusGreen)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Subtabs
                TabRow(
                    selectedTabIndex = partnerTab,
                    containerColor = BrandSecondary,
                    contentColor = BrandAccent,
                    divider = {}
                ) {
                    Tab(
                        selected = partnerTab == 0,
                        onClick = { partnerTab = 0 },
                        text = { Text("الطلبات والمهام (${actionRequiredOrders.size})", color = if (partnerTab == 0) BrandAccent else Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = partnerTab == 1,
                        onClick = { partnerTab = 1 },
                        text = { Text("قائمة الأصناف (${products.size})", color = if (partnerTab == 1) BrandAccent else Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = partnerTab == 2,
                        onClick = { partnerTab = 2 },
                        text = { Text("التقارير والمبيعات 📊", color = if (partnerTab == 2) BrandAccent else Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        // Subtab Views
        when (partnerTab) {
            0 -> {
                // Orders Queue
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("طلبات تتطلب اتخاذ إجراء 🚨", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    if (actionRequiredOrders.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                            ) {
                                Text(
                                    "لا توجد طلبات معلقة تتطلب إجراء حاليًا ✅",
                                    modifier = Modifier.padding(20.dp),
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        items(actionRequiredOrders) { ord ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
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
                                        Text(ord.orderNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        val (bg, txt) = ord.orderStatus.getBadgeColor()
                                        StatusBadge(text = ord.orderStatus.titleArabic, backgroundColor = bg, textColor = txt)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("العميل: ${ord.customerName} • ${ord.customerPhone}", style = MaterialTheme.typography.bodySmall)
                                    if (ord.customerNotes.isNotBlank()) {
                                        Text("ملاحظات العميل: ${ord.customerNotes}", style = MaterialTheme.typography.bodySmall, color = BrandPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("الإجمالي: ${ord.total.toInt()} ج.م (${ord.paymentMethod.titleArabic})", fontWeight = FontWeight.Bold, color = TextPrimary)

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Action buttons based on workflow
                                    if (ord.orderStatus == OrderStatus.PENDING_REVIEW) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            AppButton(
                                                text = "قبول الطلب ✅",
                                                onClick = { onAcceptOrder(ord) },
                                                containerColor = StatusGreen,
                                                modifier = Modifier.weight(1f),
                                                testTag = "partner_accept_order_${ord.id}"
                                            )
                                            AppOutlinedButton(
                                                text = "رفض ❌",
                                                onClick = { onRejectOrder(ord, "غير متوفر بالمطبخ") },
                                                borderColor = StatusRed,
                                                contentColor = StatusRed,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    } else if (ord.orderStatus == OrderStatus.APPROVED) {
                                        AppButton(
                                            text = "بدء التجهيز الآن (20 دقيقة) ⏱️",
                                            onClick = { onStartPreparing(ord, 20) },
                                            containerColor = StatusOrange,
                                            modifier = Modifier.fillMaxWidth(),
                                            testTag = "partner_start_prep_${ord.id}"
                                        )
                                    } else if (ord.orderStatus == OrderStatus.PREPARING) {
                                        AppButton(
                                            text = "تأكيد جاهزية الطلب للاستلام 📦",
                                            onClick = { onReadyForPickup(ord) },
                                            containerColor = StatusGreen,
                                            modifier = Modifier.fillMaxWidth(),
                                            testTag = "partner_ready_for_pickup_${ord.id}"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Completed orders section
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("الطلبات المكتملة والمستلمة 📦", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    val finishedOrders = partnerOrders.filter { it.orderStatus in listOf(OrderStatus.READY_FOR_PICKUP, OrderStatus.PICKED_UP, OrderStatus.DELIVERED) }
                    items(finishedOrders) { ord ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(ord.orderNumber, fontWeight = FontWeight.Bold)
                                    Text("المندوب: ${ord.driverName ?: "بانتظار وصول المندوب"}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                val (bg, txt) = ord.orderStatus.getBadgeColor()
                                StatusBadge(text = ord.orderStatus.titleArabic, backgroundColor = bg, textColor = txt)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Products Catalog Tab
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
                            Text("قائمة الأصناف والمخزون (${products.size}) 🍔", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            AppButton(text = "+ صنف جديد", onClick = { showAddProductDialog = true })
                        }
                    }

                    items(products) { prod ->
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
                                    Text(prod.imageEmoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(prod.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("${prod.price.toInt()} ج.م • ${prod.category}", style = MaterialTheme.typography.bodySmall, color = BrandPrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // Status toggle
                                val isAvailable = prod.status == ProductStatus.AVAILABLE
                                FilterChip(
                                    selected = isAvailable,
                                    onClick = {
                                        val next = if (isAvailable) ProductStatus.TEMPORARILY_UNAVAILABLE else ProductStatus.AVAILABLE
                                        onUpdateProductStatus(prod.id, next)
                                    },
                                    label = { Text(if (isAvailable) "متاح 🟢" else "غير متاح 🔴") }
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Reports Tab
                val partnerTotalSales = remember(partnerOrders) { partnerOrders.sumOf { it.total } }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("تقارير وأداء المتجر 📊", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandSecondary)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("إجمالي مبيعات اليوم:", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                                Text("${partnerTotalSales.toInt()} ج.م", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("إجمالي الطلبات المسجلة: ${partnerOrders.size}", color = BrandAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "التقييم العام",
                                value = "${activePartner?.rating ?: 4.8} ⭐",
                                icon = Icons.Default.Star,
                                color = StatusYellow,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "أصناف معروضة",
                                value = "${products.size}",
                                icon = Icons.Default.Fastfood,
                                color = BrandPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Product Dialog
    if (showAddProductDialog && activePartner != null) {
        var prodName by remember { mutableStateOf("") }
        var prodCat by remember { mutableStateOf("عام") }
        var prodPrice by remember { mutableStateOf("90") }
        var prodDesc by remember { mutableStateOf("") }
        var prodEmoji by remember { mutableStateOf("🍕") }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("إضافة صنف جديد", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppInput(value = prodEmoji, onValueChange = { prodEmoji = it }, label = "الأيقونة (إيموجي)")
                    AppInput(value = prodName, onValueChange = { prodName = it }, label = "اسم الصنف")
                    AppInput(value = prodCat, onValueChange = { prodCat = it }, label = "التصنيف")
                    AppInput(value = prodPrice, onValueChange = { prodPrice = it }, label = "السعر (ج.م)")
                    AppInput(value = prodDesc, onValueChange = { prodDesc = it }, label = "الوصف", singleLine = false)
                }
            },
            confirmButton = {
                AppButton(
                    text = "حفظ الصنف",
                    onClick = {
                        if (prodName.isNotBlank()) {
                            onSaveProduct(
                                ProductEntity(
                                    partnerId = activePartner.id,
                                    category = prodCat,
                                    name = prodName,
                                    description = prodDesc,
                                    price = prodPrice.toDoubleOrNull() ?: 50.0,
                                    imageEmoji = prodEmoji
                                )
                            )
                            showAddProductDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
