package com.example.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

// ============================================================================
// 🎨 FALSAREE COLOR PALETTE (لوحة ألوان فالسريع المعتمدة)
// ============================================================================

/**
 * 🟢 الأخضر (Green Palette)
 * مخصص للحالات الإيجابية: نجاح الطلب، اتصال الكابتن، الرصيد المتاح، الأرباح، التأكيدات.
 */
val FalsareeGreenPrimary = Color(0xFF22C55E) // أخضر فالسريع الزاهي #22C55E
val FalsareeGreenDark = Color(0xFF16A34A)    // أخضر داكن للأزرار #16A34A
val FalsareeGreenLight = Color(0xFFDCFCE7)   // خلفية شارات الحالة الفاتحة #DCFCE7
val FalsareeGreenSurface = Color(0xFFF0FDF4) // خلفية سطح لطيفة #F0FDF4

/**
 * 🟡 الأصفر (Yellow Palette)
 * مخصص لحالات الانتظار: النجوم والتقييمات، التحضير، التنبيهات الموقوتة، إنجازات الكباتن.
 */
val FalsareeYellowPrimary = Color(0xFFF59E0B) // أصفر العنبر #F59E0B
val FalsareeYellowDark = Color(0xFFD97706)    // أصفر داكن #D97706
val FalsareeYellowLight = Color(0xFFFEF3C7)   // خلفية الشارات المعلقة #FEF3C7
val FalsareeYellowAccent = Color(0xFFFFB703)  // أصفر الإنجاز والأرباح #FFB703

/**
 * 🟠 البرتقالي (Orange Palette)
 * هوية المندوب وسرعة التوصيل (العلامة الفارقة): جاري التوصيل، رادار الطلبات، أزرار السحب للتأكيد.
 */
val FalsareeOrangePrimary = Color(0xFFFF6B00) // برتقالي فالسريع المتميز #FF6B00
val FalsareeOrangeDark = Color(0xFFE05D00)    // برتقالي داكن للتفاعل #E05D00
val FalsareeOrangeLight = Color(0xFFFFEDD5)   // خلفية الشارات البرتقالية #FFEDD5
val FalsareeOrangeSurface = Color(0xFFFFF7ED) // أسطح العروض الترويجية #FFF7ED

/**
 * 🔵 الأزرق (Blue Palette)
 * هوية العميل والواجهات الرئيسية: البحث، الملاحة، بطاقات المتاجر، الكحلي العميق.
 */
val FalsareeBluePrimary = Color(0xFF0085FF) // أزرق فالسريع الرئيسي للعملاء #0085FF
val FalsareeBlueDark = Color(0xFF0066CC)    // أزرق للتفاعل #0066CC
val FalsareeBlueLight = Color(0xFFE0F2FE)   // خلفية شارات المعلومات #E0F2FE
val FalsareeBlueNavy = Color(0xFF0A1A2F)    // كحلي داكن فاخر #0A1A2F
val FalsareeBlueCyan = Color(0xFF00D1FF)    // أزرق سماوي مشرق #00D1FF

/**
 * 🔴 الأحمر (Red Palette)
 * مخصص للتنبيهات الحرجة: إلغاء الطلب، الأخطاء، المبالغ المدينة، التنبيهات العاجلة.
 */
val FalsareeRedPrimary = Color(0xFFEF4444) // أحمر التحذير #EF4444
val FalsareeRedDark = Color(0xFFDC2626)    // أحمر داكن #DC2626
val FalsareeRedLight = Color(0xFFFEE2E2)   // خلفية شارات الخطأ والرفض #FEE2E2
val FalsareeRedSurface = Color(0xFFFEF2F2) // خلفية مربعات الخطأ #FEF2F2

/**
 * ⚪ الرمادي والأسطح المحايدة (Gray & Neutral Palette)
 * مخصص للأسطح، الفواصل، النصوص الأساسية والثانوية، والعناصر غير النشطة.
 */
val FalsareeGray50 = Color(0xFFF8FAFC)  // خلفية الشاشات الرئيسية
val FalsareeGray100 = Color(0xFFF1F5F9) // خلفية الحقول والبطاقات الخفيفة
val FalsareeGray200 = Color(0xFFE2E8F0) // خطوط الحدود والفواصل الهادئة
val FalsareeGray300 = Color(0xFFCBD5E1) // الحدود عند التركيز
val FalsareeGray400 = Color(0xFF94A3B8) // الأيقونات المعطلة وتلميحات النصوص
val FalsareeGray600 = Color(0xFF64748B) // النصوص الثانوية والوصف
val FalsareeGray800 = Color(0xFF1E293B) // العناوين الفرعية الداكنة
val FalsareeGray900 = Color(0xFF0F172A) // النص الرئيسي الداكن عالي التباين

/**
 * بيانات كاملة لسمات ألوان فالسريع لسهولة الوصول إليها عبر التركيب (CompositionLocal).
 */
@Immutable
data class FalsareeColorTokens(
    // 🟢 الأخضر
    val greenPrimary: Color = FalsareeGreenPrimary,
    val greenDark: Color = FalsareeGreenDark,
    val greenLight: Color = FalsareeGreenLight,
    val greenSurface: Color = FalsareeGreenSurface,

    // 🟡 الأصفر
    val yellowPrimary: Color = FalsareeYellowPrimary,
    val yellowDark: Color = FalsareeYellowDark,
    val yellowLight: Color = FalsareeYellowLight,
    val yellowAccent: Color = FalsareeYellowAccent,

    // 🟠 البرتقالي
    val orangePrimary: Color = FalsareeOrangePrimary,
    val orangeDark: Color = FalsareeOrangeDark,
    val orangeLight: Color = FalsareeOrangeLight,
    val orangeSurface: Color = FalsareeOrangeSurface,

    // 🔵 الأزرق
    val bluePrimary: Color = FalsareeBluePrimary,
    val blueDark: Color = FalsareeBlueDark,
    val blueLight: Color = FalsareeBlueLight,
    val blueNavy: Color = FalsareeBlueNavy,
    val blueCyan: Color = FalsareeBlueCyan,

    // 🔴 الأحمر
    val redPrimary: Color = FalsareeRedPrimary,
    val redDark: Color = FalsareeRedDark,
    val redLight: Color = FalsareeRedLight,
    val redSurface: Color = FalsareeRedSurface,

    // ⚪ الرمادي والمحايد
    val gray50: Color = FalsareeGray50,
    val gray100: Color = FalsareeGray100,
    val gray200: Color = FalsareeGray200,
    val gray400: Color = FalsareeGray400,
    val gray600: Color = FalsareeGray600,
    val gray900: Color = FalsareeGray900,

    // أسطح عامة
    val background: Color = FalsareeGray50,
    val surface: Color = Color.White,
    val surfaceCard: Color = Color.White,
    val border: Color = FalsareeGray200,
    val textPrimary: Color = FalsareeGray900,
    val textSecondary: Color = FalsareeGray600,
    val textMuted: Color = FalsareeGray400
)

val LocalFalsareeColors = staticCompositionLocalOf { FalsareeColorTokens() }

// ============================================================================
// ✍️ ARABIC TYPOGRAPHY (CAIRO & IBM PLEX SANS ARABIC مع دعم RTL)
// ============================================================================

/**
 * مزود خطوط جوجل الرسمي (Google Fonts Provider) لتنزيل خطي Cairo و IBM Plex Sans Arabic.
 */
val GoogleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val CairoGoogleFont = GoogleFont("Cairo")
private val IBMPlexSansArabicGoogleFont = GoogleFont("IBM Plex Sans Arabic")

/**
 * عائلة خط Cairo الرسمية - ممتازة للعناوين والأرقام والواجهات الحديثة.
 */
val CairoFontFamily: FontFamily = FontFamily(
    Font(googleFont = CairoGoogleFont, fontProvider = GoogleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = CairoGoogleFont, fontProvider = GoogleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = CairoGoogleFont, fontProvider = GoogleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = CairoGoogleFont, fontProvider = GoogleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = CairoGoogleFont, fontProvider = GoogleFontProvider, weight = FontWeight.ExtraBold)
)

/**
 * عائلة خط IBM Plex Sans Arabic الرسمية - فائقة الوضوح والقراءة للنصوص والفقرات وقوائم الأسعار.
 */
val IBMPlexSansArabicFontFamily: FontFamily = FontFamily(
    Font(googleFont = IBMPlexSansArabicGoogleFont, fontProvider = GoogleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = IBMPlexSansArabicGoogleFont, fontProvider = GoogleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = IBMPlexSansArabicGoogleFont, fontProvider = GoogleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = IBMPlexSansArabicGoogleFont, fontProvider = GoogleFontProvider, weight = FontWeight.Bold)
)

/**
 * خط فالسريع العربي الأساسي (Cairo مع خط احتياطي SansSerif لضمان الأمان الكامل دون اتصال)
 */
val FalsareeDisplayFont: FontFamily = CairoFontFamily
val FalsareeBodyFont: FontFamily = IBMPlexSansArabicFontFamily

/**
 * تكوين نمط النص العربي الموحد لضمان اتجاه الكتابة من اليمين لليسار (RTL) وتباعد أسطر ملائم للرسم العربي.
 */
private fun createArabicTextStyle(
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    fontSize: Float,
    lineHeight: Float,
    color: Color = FalsareeGray900
): TextStyle = TextStyle(
    fontFamily = fontFamily,
    fontWeight = fontWeight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
    color = color,
    textDirection = TextDirection.Rtl
)

/**
 * نظام الطباعة الكامل لتطبيق فالسريع، مع ضبط مقاييس الخطوط وارتفاع الأسطر لتناسب الخطوط العربية (Cairo و IBM Plex).
 */
val FalsareeArabicTypography = Typography(
    // العناوين الضخمة والبارزة (Display & Hero)
    displayLarge = createArabicTextStyle(FalsareeDisplayFont, FontWeight.ExtraBold, 32f, 42f),
    displayMedium = createArabicTextStyle(FalsareeDisplayFont, FontWeight.Bold, 28f, 38f),
    displaySmall = createArabicTextStyle(FalsareeDisplayFont, FontWeight.Bold, 24f, 34f),

    // عناوين الشاشات والأقسام الرئيسية
    headlineLarge = createArabicTextStyle(FalsareeDisplayFont, FontWeight.Bold, 22f, 30f),
    headlineMedium = createArabicTextStyle(FalsareeDisplayFont, FontWeight.Bold, 20f, 28f),
    headlineSmall = createArabicTextStyle(FalsareeDisplayFont, FontWeight.SemiBold, 18f, 26f),

    // عناوين البطاقات والبنود
    titleLarge = createArabicTextStyle(FalsareeDisplayFont, FontWeight.Bold, 17f, 24f),
    titleMedium = createArabicTextStyle(FalsareeDisplayFont, FontWeight.SemiBold, 15f, 22f),
    titleSmall = createArabicTextStyle(FalsareeDisplayFont, FontWeight.Medium, 14f, 20f),

    // نصوص الفقرات والشروحات (تعتمد على IBM Plex Sans Arabic لنقاء القراءة)
    bodyLarge = createArabicTextStyle(FalsareeBodyFont, FontWeight.Normal, 15f, 24f),
    bodyMedium = createArabicTextStyle(FalsareeBodyFont, FontWeight.Normal, 13f, 20f, FalsareeGray600),
    bodySmall = createArabicTextStyle(FalsareeBodyFont, FontWeight.Normal, 12f, 18f, FalsareeGray600),

    // الأزرار والشارات التوضيحية
    labelLarge = createArabicTextStyle(FalsareeDisplayFont, FontWeight.Bold, 14f, 20f),
    labelMedium = createArabicTextStyle(FalsareeDisplayFont, FontWeight.SemiBold, 12f, 17f),
    labelSmall = createArabicTextStyle(FalsareeDisplayFont, FontWeight.Medium, 10f, 15f)
)

// ============================================================================
// 📐 SHAPES, SPACING & ELEVATION (الأشكال والمسافات والظلال)
// ============================================================================

val FalsareeShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

object FalsareeSpacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 28.dp
    val xxxl: Dp = 36.dp
}

object FalsareeElevation {
    val none: Dp = 0.dp
    val low: Dp = 2.dp
    val card: Dp = 4.dp
    val modal: Dp = 8.dp
    val stickyHeader: Dp = 12.dp
}

// ============================================================================
// 🌍 RTL & THEME PROVIDER (مقدم نظام التصميم ودعم اللغة العربية)
// ============================================================================

/**
 * مغلف الدعم الكامل للغة العربية واتجاه اليمين لليسار (RTL).
 */
@Composable
fun FalsareeRtlProvider(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        content = content
    )
}

/**
 * كائن مركزي للوصول السريع لرموز نظام التصميم:
 * `FalsareeDesign.colors.greenPrimary`
 * `FalsareeDesign.typography.titleLarge`
 * `FalsareeDesign.spacing.lg`
 */
object FalsareeDesign {
    val colors: FalsareeColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalFalsareeColors.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val spacing: FalsareeSpacing = FalsareeSpacing
    val elevation: FalsareeElevation = FalsareeElevation
    val shapes: Shapes = FalsareeShapes
}
