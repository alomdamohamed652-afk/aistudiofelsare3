package com.example.core.model

import androidx.compose.ui.graphics.Color
import com.example.core.designsystem.*

enum class UserRole(val titleArabic: String, val iconEmoji: String) {
    CUSTOMER("تطبيق العميل", "👤"),
    ADMIN("لوحة الإدارة", "👑"),
    DRIVER("تطبيق المندوب", "🛵"),
    PARTNER("تطبيق الشريك", "🏪")
}

enum class PartnerType(val titleArabic: String, val iconEmoji: String) {
    RESTAURANT("مطعم", "🍔"),
    PHARMACY("صيدلية", "💊"),
    CAFE("كافيه", "☕"),
    STORE("متجر", "🛒")
}

enum class OrderStatus(val titleArabic: String) {
    CREATED("تم إنشاء الطلب"),
    PENDING_REVIEW("قيد المراجعة"),
    APPROVED("تم اعتماد الطلب"),
    PREPARING("قيد التجهيز"),
    READY_FOR_PICKUP("جاهز للاستلام"),
    PICKED_UP("تم استلام الطلب"),
    DELIVERED("تم التسليم"),
    REJECTED("تم رفض الطلب"),
    CANCELLED("تم إلغاء الطلب");

    fun getBadgeColor(): Pair<Color, Color> {
        return when (this) {
            CREATED -> Pair(StatusBlueLight, StatusBlue)
            PENDING_REVIEW -> Pair(StatusYellowLight, StatusYellow)
            APPROVED -> Pair(StatusBlueLight, StatusBlue)
            PREPARING -> Pair(StatusOrangeLight, StatusOrange)
            READY_FOR_PICKUP -> Pair(StatusGreenLight, StatusGreen)
            PICKED_UP -> Pair(StatusBlueLight, StatusBlue)
            DELIVERED -> Pair(StatusGreenLight, StatusGreen)
            REJECTED, CANCELLED -> Pair(StatusRedLight, StatusRed)
        }
    }
}

enum class DeliveryStatus(val titleArabic: String) {
    NOT_REQUIRED("غير مطلوب"),
    WAITING_FOR_DRIVER("بانتظار مندوب"),
    DRIVER_ASSIGNED("تم تعيين مندوب"),
    DRIVER_TO_PICKUP("المندوب في الطريق للاستلام"),
    PICKED_UP("تم الاستلام من الشريك"),
    OUT_FOR_DELIVERY("الطلب في الطريق إليك 🛵"),
    DELIVERED("تم التسليم للعميل 🟢");

    fun getBadgeColor(): Pair<Color, Color> {
        return when (this) {
            NOT_REQUIRED -> Pair(StatusGrayLight, StatusGray)
            WAITING_FOR_DRIVER -> Pair(StatusYellowLight, StatusYellow)
            DRIVER_ASSIGNED -> Pair(StatusBlueLight, StatusBlue)
            DRIVER_TO_PICKUP -> Pair(StatusOrangeLight, StatusOrange)
            PICKED_UP -> Pair(StatusOrangeLight, StatusOrange)
            OUT_FOR_DELIVERY -> Pair(StatusBlueLight, StatusBlue)
            DELIVERED -> Pair(StatusGreenLight, StatusGreen)
        }
    }
}

enum class DispatchMode(val titleArabic: String, val descriptionArabic: String) {
    SEQUENTIAL("توزيع بالتتابع", "يُعرض الطلب على المندوبين المؤهلين واحدًا تلو الآخر حسب الدور"),
    HYBRID("توزيع هجين", "يبدأ بالتتابع ثم يتحول إلى العرض الجماعي عند عدم وجود مندوب مؤهل"),
    BROADCAST("عرض جماعي", "يظهر الطلب لجميع المندوبين المؤهلين وأول قبول صحيح يفوز بالتعيين")
}

enum class ApprovalWorkflow(val titleArabic: String) {
    ADMIN("اعتماد من الإدارة"),
    PARTNER("اعتماد من الشريك"),
    AUTOMATIC("اعتماد تلقائي فوري")
}

enum class PaymentMethod(val titleArabic: String, val iconEmoji: String) {
    CASH_ON_DELIVERY("الدفع عند الاستلام", "💵"),
    BANK_TRANSFER("تحويل إلكتروني (فودافون كاش / انستاباي)", "🏦")
}

enum class PaymentStatus(val titleArabic: String) {
    PENDING("في انتظار الدفع"),
    PENDING_VERIFICATION("قيد مراجعة التحويل"),
    VERIFIED("تم تأكيد الدفع"),
    REJECTED("تم رفض التحويل")
}

enum class DriverStatus(val titleArabic: String) {
    AVAILABLE("متاح للطلبات"),
    BUSY("في مهمة توصيل"),
    OFFLINE("غير متاح"),
    SUSPENDED("موقوف مؤقتًا")
}

enum class ProductStatus(val titleArabic: String) {
    AVAILABLE("متاح للطلب"),
    TEMPORARILY_UNAVAILABLE("غير متاح مؤقتًا"),
    DISABLED("معطل إداريًا")
}

enum class HomeSectionType(val titleArabic: String) {
    BANNERS("عروض وبانرات متحركة"),
    CATEGORIES("تصنيفات رئيسية"),
    OFFERS("عروض وخصومات مميزة"),
    NEARBY_PARTNERS("أماكن قريبة منك"),
    TOP_RATED("الأعلى تقييمًا"),
    NEW_PARTNERS("انضموا حديثًا"),
    FEATURED_PRODUCTS("أكثر الأصناف طلبًا"),
    CUSTOM("قسم مخصص")
}

enum class NotificationCategory(val titleArabic: String, val iconEmoji: String) {
    ORDER("تحديثات الطلبات", "📦"),
    PAYMENT("المدفوعات", "💰"),
    SYSTEM("النظام والإدارة", "⚙️"),
    ACTION_REQUIRED("إجراء مطلوب", "🚨")
}

enum class TicketCategory(val titleArabic: String) {
    ORDER_PROBLEM("مشكلة في الطلب"),
    PAYMENT_PROBLEM("مشكلة في الدفع"),
    ACCOUNT_PROBLEM("مشكلة في الحساب"),
    OTHER("أخرى")
}

enum class TicketStatus(val titleArabic: String) {
    OPEN("قيد المتابعة"),
    RESOLVED("تم الحل"),
    CLOSED("مغلقة")
}
