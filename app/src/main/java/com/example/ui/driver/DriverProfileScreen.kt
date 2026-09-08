package com.example.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.DriverProfileEntity

@Composable
fun DriverProfileScreen(
    driver: DriverProfileEntity?,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoAcceptOrders by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "حساب الكابتن 👤",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "الملف الشخصي، بيانات المركبة، وإعدادات العمل",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Card with Dark Navy Canvas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DriverDarkCard)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(DriverPrimaryOrange.copy(alpha = 0.2f))
                                .border(2.dp, DriverPrimaryOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛵", fontSize = 36.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = driver?.name ?: "محمد علي كابتن فالسريع",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = driver?.phone ?: "01098765432",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusGreenLight
                            ) {
                                Text(
                                    text = "حساب موثق ومعتمد ✅",
                                    color = StatusGreen,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusYellowLight
                            ) {
                                Text(
                                    text = "⭐ ${driver?.rating ?: 4.9} تقييم ممتاز",
                                    color = StatusYellow,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Vehicle Information Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "بيانات المركبة والرخصة 🛵",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نوع المركبة:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(driver?.vehicle ?: "دراجة نارية (موتوسيكل)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("رقم اللوحة المعدنية:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("س د ر 1234", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("رخصة القيادة:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("سارية حتى 2028 🟢", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = StatusGreen)
                        }
                    }
                }
            }

            // Performance KPIs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "مؤشرات الأداء 📈",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricCard(
                                title = "الطلبات المكتملة",
                                value = "${driver?.completedOrdersCount ?: 42}",
                                icon = Icons.Default.CheckCircle,
                                color = StatusGreen,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            MetricCard(
                                title = "نسبة القبول",
                                value = "98%",
                                icon = Icons.Default.ThumbUp,
                                color = DriverPrimaryOrange,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            MetricCard(
                                title = "الالتزام بالوقت",
                                value = "99%",
                                icon = Icons.Default.Speed,
                                color = DriverAccentYellow,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Preferences Switches
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "تفضيلات التطبيق",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("إشعارات الطلبات العاجلة 🔔", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                                Text("تنبيه صوتي واهتزاز فوري عند توفر طلب", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DriverPrimaryOrange, checkedTrackColor = DriverPrimaryOrange.copy(alpha = 0.5f))
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("القبول التلقائي للطلبات ⚡", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                                Text("قبول الطلبات القريبة تلقائياً دون انتظار", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Switch(
                                checked = autoAcceptOrders,
                                onCheckedChange = { autoAcceptOrders = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DriverPrimaryOrange, checkedTrackColor = DriverPrimaryOrange.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }

            // Switch to Customer Role Button
            item {
                OutlinedButton(
                    onClick = onSwitchRole,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("driver_switch_customer_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CustomerPrimaryBlue)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = CustomerPrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("التبديل إلى واجهة العميل 👤", color = CustomerPrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
