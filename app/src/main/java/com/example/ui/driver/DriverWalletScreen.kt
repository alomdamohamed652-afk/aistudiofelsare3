package com.example.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.designsystem.*
import com.example.data.local.DriverPayoutRequestEntity
import com.example.data.local.DriverProfileEntity

@Composable
fun DriverWalletScreen(
    driver: DriverProfileEntity?,
    payoutRequests: List<DriverPayoutRequestEntity>,
    onRequestPayout: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPayoutDialog by remember { mutableStateOf(false) }

    val todayEarnings = driver?.todayEarnings ?: 180.0
    val totalEarnings = driver?.totalEarnings ?: 2450.0
    val availableBalance = totalEarnings.coerceAtLeast(350.0) // demo balance available for payout

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "المحفظة والأرباح 💰",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "متابعة مستحقاتك وأرباح التوصيل وطلبات السحب",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Balance Card (Driver Palette: Dark Navy & Orange Accent)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("driver_wallet_balance_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DriverDarkCard)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الرصيد القابل للسحب",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${availableBalance.toInt()} ج.م",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(DriverPrimaryOrange.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💵", fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("دخل اليوم", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                        Text("${todayEarnings.toInt()} ج.م", color = DriverAccentYellow, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Column {
                        Text("إجمالي الأرباح", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                        Text("${totalEarnings.toInt()} ج.م", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Column {
                        Text("نقدية محصّلة (كاش)", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                        Text("420 ج.م", color = StatusGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { showPayoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("request_payout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DriverPrimaryOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("طلب سحب أرباح", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "سجل المعاملات والتحويلات 📋",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (payoutRequests.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💳", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد طلبات سحب سابقة",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "يتم تحويل الأرباح عبر فودافون كاش، إنستاباي، أو الحساب البنكي فور طلبها",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(payoutRequests) { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(DriverPrimaryOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Paid,
                                        contentDescription = null,
                                        tint = DriverPrimaryOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "طلب سحب ${req.amount.toInt()} ج.م",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "طلب رقم #${req.id}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            StatusBadge(
                                text = req.statusArabic,
                                backgroundColor = if (req.status == "APPROVED") StatusGreenLight else StatusOrangeLight,
                                textColor = if (req.status == "APPROVED") StatusGreen else StatusOrange
                            )
                        }
                    }
                }
            }
        }
    }

    // Payout Request Dialog
    if (showPayoutDialog) {
        DriverPayoutDialog(
            availableBalance = availableBalance,
            onDismiss = { showPayoutDialog = false },
            onSubmit = { amount ->
                onRequestPayout(amount)
                showPayoutDialog = false
            }
        )
    }
}

@Composable
fun DriverPayoutDialog(
    availableBalance: Double,
    onDismiss: () -> Unit,
    onSubmit: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf(availableBalance.toInt().toString()) }
    var selectedMethod by remember { mutableStateOf("إنستاباي / محفظة إلكترونية") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "طلب سحب أرباح 💵",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "الرصيد المتاح حالياً: ${availableBalance.toInt()} ج.م",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DriverPrimaryOrange,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("المبلغ المطلوب سحبه (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "طريقة التحويل:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                val methods = listOf("فودافون كاش / أورانج / اتصالات", "إنستاباي InstaPay", "تحويل بنكي")
                methods.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            colors = RadioButtonDefaults.colors(selectedColor = DriverPrimaryOrange)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(method, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0 && amount <= availableBalance) {
                            onSubmit(amount)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DriverPrimaryOrange),
                    shape = RoundedCornerShape(12.dp),
                    enabled = (amountText.toDoubleOrNull() ?: 0.0) in 1.0..availableBalance
                ) {
                    Text("تأكيد طلب السحب", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
