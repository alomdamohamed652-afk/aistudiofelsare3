package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.core.model.PaymentMethod
import com.example.data.local.CouponEntity
import com.example.data.local.CustomerAddressEntity
import com.example.data.local.PartnerEntity
import com.example.data.local.ProductEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutScreen(
    partner: PartnerEntity?,
    cartItems: Map<ProductEntity, Int>,
    cartOptions: Map<Long, String>,
    appliedCoupon: CouponEntity?,
    discountAmount: Double,
    addresses: List<CustomerAddressEntity>,
    onUpdateQuantity: (ProductEntity, Int) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onConfirmOrder: (address: String, notes: String, payment: PaymentMethod, receiptNote: String) -> Unit,
    onBack: () -> Unit,
    onClearCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var couponCodeInput by remember { mutableStateOf("") }
    var selectedAddressText by remember {
        mutableStateOf(addresses.firstOrNull()?.let { "${it.area} - ${it.street} (${it.building})" } ?: "المهندسين - شارع سوريا عمارة 14")
    }
    var orderNotes by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH_ON_DELIVERY) }
    var transferReceiptNote by remember { mutableStateOf("تم التحويل عبر انستاباي بنجاح") }
    var isReceiptSimulatedAttached by remember { mutableStateOf(false) }

    val subtotal = remember(cartItems) {
        cartItems.entries.sumOf { it.key.price * it.value }
    }
    val deliveryFee = partner?.deliveryFee ?: 20.0
    val total = (subtotal + deliveryFee - discountAmount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سلة المشتريات والطلب ⚡", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("cart_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = onClearCart, modifier = Modifier.testTag("cart_clear_btn")) {
                            Text("إفراغ السلة", color = StatusRed, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = SurfaceCard
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الإجمالي الكلي المستحق:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${total.toInt()} ج.م", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = BrandPrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        AppButton(
                            text = "تأكيد وإرسال الطلب (${total.toInt()} ج.م) ⚡",
                            onClick = {
                                onConfirmOrder(selectedAddressText, orderNotes, selectedPaymentMethod, transferReceiptNote)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "cart_confirm_order_btn"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (cartItems.isEmpty() || partner == null) {
            EmptyState(
                title = "سلتك فارغة",
                message = "تصفح المطاعم والصيدليات والمتاجر وأضف ما يعجبك لتطلبه فالسريع!",
                icon = Icons.Default.ShoppingCart,
                actionText = "تصفح الآن",
                onActionClick = onBack,
                modifier = modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(SurfaceBackground),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Partner Banner (Enforces Single Partner constraint visibility)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(partner.logoEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "طلب موحد من: ${partner.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "توصيل سريع خلال ${partner.deliveryTimeMinutes} دقيقة تقريبًا 🛵",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Items list
                item {
                    Text(
                        text = "الأصناف المختارة (${cartItems.values.sum()}):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(cartItems.entries.toList()) { (product, qty) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(product.imageEmoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                val opt = cartOptions[product.id]
                                if (!opt.isNullOrEmpty()) {
                                    Text(opt, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Text("${product.price.toInt()} ج.م", style = MaterialTheme.typography.labelMedium, color = BrandPrimary, fontWeight = FontWeight.Bold)
                            }

                            // Stepper
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceBackground)
                                    .padding(2.dp)
                            ) {
                                IconButton(
                                    onClick = { onUpdateQuantity(product, -1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (qty == 1) Icons.Default.Delete else Icons.Default.Remove,
                                        contentDescription = "تقليل",
                                        tint = if (qty == 1) StatusRed else TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text("$qty", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(
                                    onClick = { onUpdateQuantity(product, 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Coupon code box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("كوبون الخصم:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppInput(
                                    value = couponCodeInput,
                                    onValueChange = { couponCodeInput = it },
                                    label = "كود الخصم",
                                    placeholder = "مثال: SARIEE30",
                                    modifier = Modifier.weight(1f),
                                    testTag = "cart_coupon_input"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                AppButton(
                                    text = "تطبيق",
                                    onClick = { onApplyCoupon(couponCodeInput) },
                                    testTag = "cart_apply_coupon_btn"
                                )
                            }
                            if (appliedCoupon != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "تم تطبيق كود ${appliedCoupon.code} (خصم ${appliedCoupon.discountPercent}%) ✅",
                                    color = StatusGreen,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Delivery Address Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = BrandPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("عنوان التوصيل:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            addresses.forEach { addr ->
                                val text = "${addr.label}: ${addr.area} - ${addr.street} (${addr.building})"
                                val isSelected = selectedAddressText == text
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAddressText = text }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedAddressText = text },
                                        colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(addr.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("${addr.area} - ${addr.street}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            AppInput(
                                value = orderNotes,
                                onValueChange = { orderNotes = it },
                                label = "ملاحظات إضافية للتوصيل (اختياري)",
                                placeholder = "مثال: رن الجرس، الطلب عند البواب...",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Payment Method Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("طريقة الدفع:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            // COD Option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPaymentMethod = PaymentMethod.CASH_ON_DELIVERY }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY,
                                    onClick = { selectedPaymentMethod = PaymentMethod.CASH_ON_DELIVERY },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("💵 الدفع عند الاستلام (كاش)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }

                            // Bank / Electronic Transfer Option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPaymentMethod = PaymentMethod.BANK_TRANSFER }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == PaymentMethod.BANK_TRANSFER,
                                    onClick = { selectedPaymentMethod = PaymentMethod.BANK_TRANSFER },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("🏦 تحويل إلكتروني (InstaPay / محفظة إلكترونية)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }

                            // If Bank Transfer, show transfer instructions & receipt upload simulation
                            if (selectedPaymentMethod == PaymentMethod.BANK_TRANSFER) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBackground),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("معلومات التحويل:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                        Text("• رقم المحفظة / انستاباي: 01012345678", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                        Text("• عنوان انستاباي: falsaree3@instapay", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        AppInput(
                                            value = transferReceiptNote,
                                            onValueChange = { transferReceiptNote = it },
                                            label = "رقم العملية المرجعي أو تفاصيل الإيصال",
                                            placeholder = "مثال: تم التحويل من رقم 010... رقم مرجعي 1234",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isReceiptSimulatedAttached) "تم إرفاق صورة الإيصال بنجاح 📎" else "إرفاق صورة إشعار التحويل:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isReceiptSimulatedAttached) StatusGreen else TextSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            TextButton(onClick = { isReceiptSimulatedAttached = !isReceiptSimulatedAttached }) {
                                                Text(if (isReceiptSimulatedAttached) "تغيير" else "إرفاق صورة 📸", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Cost Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("تفاصيل الفاتورة:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("المجموع الفرعي:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("${subtotal.toInt()} ج.م", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("رسوم التوصيل:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("${deliveryFee.toInt()} ج.م", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            if (discountAmount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("الخصم المطبق:", style = MaterialTheme.typography.bodySmall, color = StatusGreen)
                                    Text("-${discountAmount.toInt()} ج.م", style = MaterialTheme.typography.bodySmall, color = StatusGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = SurfaceBorder)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الإجمالي المستحق:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${total.toInt()} ج.م", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
