package com.example.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FalsareeDesignSystemDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .testTag("design_system_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = FalsareeGray50)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(FalsareeBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatColorFill,
                                contentDescription = null,
                                tint = FalsareeBluePrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "دليل نظام التصميم (فالسريع)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = FalsareeGray900
                            )
                            Text(
                                text = "الألوان الستة • خطوط Cairo & IBM Plex • اتجاه RTL",
                                style = MaterialTheme.typography.labelSmall,
                                color = FalsareeGray600
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = FalsareeGray600)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = FalsareeGray200)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Color Palette Section
                    item {
                        Text(
                            text = "🎨 لوحة الألوان الرئيسية (The 6 Color Palettes)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FalsareeGray900
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Green
                        PaletteCard(
                            title = "🟢 الأخضر (Green Palette)",
                            description = "مخصص لحالات النجاح، إتمام التوصيل، اتصال الكابتن، والأرباح",
                            swatches = listOf(
                                SwatchInfo("الرئيسي", "#22C55E", FalsareeGreenPrimary, Color.White),
                                SwatchInfo("الداكن", "#16A34A", FalsareeGreenDark, Color.White),
                                SwatchInfo("الفاتح", "#DCFCE7", FalsareeGreenLight, FalsareeGreenDark),
                                SwatchInfo("السطح", "#F0FDF4", FalsareeGreenSurface, FalsareeGreenDark)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Yellow
                        PaletteCard(
                            title = "🟡 الأصفر (Yellow Palette)",
                            description = "مخصص للتقييمات، طلبات قيد التحضير، وتنبيهات الكابتن",
                            swatches = listOf(
                                SwatchInfo("الرئيسي", "#F59E0B", FalsareeYellowPrimary, Color.White),
                                SwatchInfo("الداكن", "#D97706", FalsareeYellowDark, Color.White),
                                SwatchInfo("الفاتح", "#FEF3C7", FalsareeYellowLight, FalsareeYellowDark),
                                SwatchInfo("الإنجاز", "#FFB703", FalsareeYellowAccent, Color.Black)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Orange
                        PaletteCard(
                            title = "🟠 البرتقالي (Orange Palette)",
                            description = "هوية المندوب، سرعة التوصيل، رادار الطلبات المباشر",
                            swatches = listOf(
                                SwatchInfo("الرئيسي", "#FF6B00", FalsareeOrangePrimary, Color.White),
                                SwatchInfo("الداكن", "#E05D00", FalsareeOrangeDark, Color.White),
                                SwatchInfo("الفاتح", "#FFEDD5", FalsareeOrangeLight, FalsareeOrangeDark),
                                SwatchInfo("السطح", "#FFF7ED", FalsareeOrangeSurface, FalsareeOrangeDark)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Blue
                        PaletteCard(
                            title = "🔵 الأزرق (Blue Palette)",
                            description = "هوية العميل، التصفح، الخرائط، والكحلي الداكن الفاخر",
                            swatches = listOf(
                                SwatchInfo("الرئيسي", "#0085FF", FalsareeBluePrimary, Color.White),
                                SwatchInfo("الداكن", "#0066CC", FalsareeBlueDark, Color.White),
                                SwatchInfo("الكحلي", "#0A1A2F", FalsareeBlueNavy, Color.White),
                                SwatchInfo("السماوي", "#00D1FF", FalsareeBlueCyan, Color.Black)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Red
                        PaletteCard(
                            title = "🔴 الأحمر (Red Palette)",
                            description = "مخصص للتنبيهات الحرجة، إلغاء الطلبات، وإشعارات الخطأ",
                            swatches = listOf(
                                SwatchInfo("الرئيسي", "#EF4444", FalsareeRedPrimary, Color.White),
                                SwatchInfo("الداكن", "#DC2626", FalsareeRedDark, Color.White),
                                SwatchInfo("الفاتح", "#FEE2E2", FalsareeRedLight, FalsareeRedDark),
                                SwatchInfo("السطح", "#FEF2F2", FalsareeRedSurface, FalsareeRedDark)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Gray
                        PaletteCard(
                            title = "⚪ الرمادي والمحايد (Gray & Neutral Palette)",
                            description = "للأسطح، الحدود، التباين، والنصوص الرئيسية والفرعية",
                            swatches = listOf(
                                SwatchInfo("Gray-50", "#F8FAFC", FalsareeGray50, FalsareeGray900),
                                SwatchInfo("Gray-100", "#F1F5F9", FalsareeGray100, FalsareeGray900),
                                SwatchInfo("Gray-200", "#E2E8F0", FalsareeGray200, FalsareeGray900),
                                SwatchInfo("Gray-400", "#94A3B8", FalsareeGray400, Color.White),
                                SwatchInfo("Gray-600", "#64748B", FalsareeGray600, Color.White),
                                SwatchInfo("Gray-900", "#0F172A", FalsareeGray900, Color.White)
                            )
                        )
                    }

                    // 2. Arabic Typography Section
                    item {
                        Text(
                            text = "✍️ الخطوط والطباعة العربية (Cairo & IBM Plex Sans Arabic)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FalsareeGray900
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FalsareeGray200)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Cairo (خط العناوين والواجهات البارزة)",
                                    fontWeight = FontWeight.Bold,
                                    color = FalsareeBluePrimary,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "فالسريع: طلبك في الطريق بسرعة البرق ⚡",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FalsareeGray900
                                )
                                Text(
                                    text = "وجبات ساخنة، مقاضي سريعة، وأدوية موثوقة في دقائق معدودة.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = FalsareeGray800
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Divider(color = FalsareeGray200)
                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "IBM Plex Sans Arabic (خط الفقرات وتفاصيل البنود)",
                                    fontWeight = FontWeight.Bold,
                                    color = FalsareeOrangePrimary,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "تم اعتماد خط IBM Plex Sans Arabic لقراءة مريحة وأعلى درجات الوضوح على شاشات الهواتف المحمولة لجميع النصوص وتفاصيل الفاتورة وقوائم الأسعار.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = FalsareeGray600
                                )
                            }
                        }
                    }

                    // 3. RTL Support Section
                    item {
                        Text(
                            text = "🌍 توافق ودعم اللغة العربية (Full RTL Integration)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FalsareeGray900
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FalsareeGray200)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "محاذاة العناصر واتجاه الشاشات:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = FalsareeGray900
                                    )
                                    StatusBadge(
                                        text = "RTL مفعل بالكامل 🟢",
                                        backgroundColor = FalsareeGreenLight,
                                        textColor = FalsareeGreenDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• دعم كامل لـ `LayoutDirection.Rtl`.\n• استخدام الأيقونات المعكوسة تلقائياً `Icons.AutoMirrored` لحركات الرجوع والملاحة.\n• محاذاة تلقائية للنصوص والأسعار (ج.م) جهة اليمين بطريقة متسقة.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FalsareeGray600
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class SwatchInfo(
    val name: String,
    val hex: String,
    val color: Color,
    val textColor: Color
)

@Composable
private fun PaletteCard(
    title: String,
    description: String,
    swatches: List<SwatchInfo>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, FalsareeGray200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = FalsareeGray900
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = FalsareeGray600
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                swatches.forEach { sw ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(sw.color)
                            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = sw.name,
                                color = sw.textColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = sw.hex,
                                color = sw.textColor.copy(alpha = 0.85f),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
