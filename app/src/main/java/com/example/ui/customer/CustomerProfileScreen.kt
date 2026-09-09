package com.example.ui.customer

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
import com.example.core.model.TicketCategory
import com.example.core.model.UserRole
import com.example.data.local.CouponEntity
import com.example.data.local.CustomerAddressEntity
import com.example.data.local.SupportTicketEntity
import com.example.core.model.UserSession

@Composable
fun CustomerProfileScreen(
    addresses: List<CustomerAddressEntity>,
    coupons: List<CouponEntity>,
    tickets: List<SupportTicketEntity>,
    currentSession: UserSession?,
    onLogout: () -> Unit,
    onAddAddress: (CustomerAddressEntity) -> Unit,
    onDeleteAddress: (CustomerAddressEntity) -> Unit,
    onCreateTicket: (SupportTicketEntity) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showCreateTicketDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(BrandPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(currentSession?.name ?: "المستخدم", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(currentSession?.phone.orEmpty(), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        if (!currentSession?.email.isNullOrBlank()) {
                            Text(currentSession?.email.orEmpty(), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StatusGreenLight
                    ) {
                        Text(
                            text = "عميل نشط ⚡",
                            color = StatusGreen,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل الخروج")
            }
        }

        // Saved Addresses Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = BrandPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("العناوين المسجلة", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showAddAddressDialog = true }) {
                            Text("+ إضافة عنوان", color = BrandPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    addresses.forEach { addr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(addr.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    if (addr.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        StatusBadge(text = "افتراضي", backgroundColor = StatusBlueLight, textColor = StatusBlue)
                                    }
                                }
                                Text("${addr.area} - ${addr.street}، ${addr.building}، ${addr.floor}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            IconButton(onClick = { onDeleteAddress(addr) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = StatusRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Coupons Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎟️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("كوبونات الخصم المتاحة", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    coupons.filter { it.active }.forEach { coupon ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceBackground),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(coupon.code, fontWeight = FontWeight.ExtraBold, color = BrandPrimary, style = MaterialTheme.typography.titleSmall)
                                    Text("خصم ${coupon.discountPercent}% على الطلبات فوق ${coupon.minOrder.toInt()} ج.م", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                StatusBadge(text = "فعال ✅", backgroundColor = StatusGreenLight, textColor = StatusGreen)
                            }
                        }
                    }
                }
            }
        }

        // Support Tickets Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.SupportAgent, contentDescription = null, tint = BrandPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تذاكر الدعم الفني", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showCreateTicketDialog = true }) {
                            Text("+ تذكرة جديدة", color = BrandPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (tickets.isEmpty()) {
                        Text("لا توجد تذاكر دعم سابقة.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    } else {
                        tickets.forEach { ticket ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceBackground),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("#${ticket.ticketNumber} • ${ticket.subject}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(ticket.category.titleArabic, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    StatusBadge(
                                        text = ticket.status.titleArabic,
                                        backgroundColor = if (ticket.status.name == "RESOLVED") StatusGreenLight else StatusYellowLight,
                                        textColor = if (ticket.status.name == "RESOLVED") StatusGreen else StatusYellow
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Switch Role Portal (Admin, Driver, Partner, Customer)
        if (BuildConfig.DEBUG) item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSecondaryLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "التبديل بين واجهات النظام:",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppButton(
                            text = "👑 الإدارة",
                            onClick = { onSwitchRole(UserRole.ADMIN) },
                            modifier = Modifier.weight(1f),
                            containerColor = Color(0xFF6366F1),
                            testTag = "profile_switch_admin"
                        )
                        AppButton(
                            text = "🛵 المندوب",
                            onClick = { onSwitchRole(UserRole.DRIVER) },
                            modifier = Modifier.weight(1f),
                            containerColor = StatusGreen,
                            testTag = "profile_switch_driver"
                        )
                        AppButton(
                            text = "🏪 الشريك",
                            onClick = { onSwitchRole(UserRole.PARTNER) },
                            modifier = Modifier.weight(1f),
                            containerColor = StatusYellow,
                            testTag = "profile_switch_partner"
                        )
                    }
                }
            }
        }


    }


    // Add Address Dialog
    if (showAddAddressDialog) {
        var label by remember { mutableStateOf("المنزل") }
        var area by remember { mutableStateOf("المهندسين") }
        var street by remember { mutableStateOf("") }
        var building by remember { mutableStateOf("") }
        var floor by remember { mutableStateOf("") }
        var apartment by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddAddressDialog = false },
            title = { Text("إضافة عنوان جديد 📍", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppInput(value = label, onValueChange = { label = it }, label = "تسمية العنوان (المنزل، العمل...)")
                    AppInput(value = area, onValueChange = { area = it }, label = "المنطقة / الحي")
                    AppInput(value = street, onValueChange = { street = it }, label = "اسم الشارع")
                    AppInput(value = building, onValueChange = { building = it }, label = "رقم أو اسم العمارة")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppInput(value = floor, onValueChange = { floor = it }, label = "الدور", modifier = Modifier.weight(1f))
                        AppInput(value = apartment, onValueChange = { apartment = it }, label = "الشقة", modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                AppButton(
                    text = "حفظ العنوان",
                    onClick = {
                        if (street.isNotBlank()) {
                            onAddAddress(
                                CustomerAddressEntity(
                                    customerId = 0L,
                                    label = label,
                                    area = area,
                                    street = street,
                                    building = building,
                                    floor = floor,
                                    apartment = apartment
                                )
                            )
                            showAddAddressDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddAddressDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Create Support Ticket Dialog
    if (showCreateTicketDialog) {
        var subject by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(TicketCategory.ORDER_PROBLEM) }

        AlertDialog(
            onDismissRequest = { showCreateTicketDialog = false },
            title = { Text("فتح تذكرة دعم جديدة 🎫", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppInput(value = subject, onValueChange = { subject = it }, label = "عنوان المشكلة")
                    AppInput(
                        value = message,
                        onValueChange = { message = it },
                        label = "تفاصيل المشكلة",
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                AppButton(
                    text = "إرسال التذكرة",
                    onClick = {
                        if (subject.isNotBlank() && message.isNotBlank()) {
                            onCreateTicket(
                                SupportTicketEntity(
                                    customerId = 0L,
                                    ticketNumber = "TK-${(100..999).random()}",
                                    customerName = "",
                                    category = category,
                                    subject = subject,
                                    message = message
                                )
                            )
                            showCreateTicketDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showCreateTicketDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
