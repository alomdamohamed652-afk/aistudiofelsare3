package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.model.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val passwordHash: String,
    val passwordSalt: String,
    val role: UserRole = UserRole.CUSTOMER,
    val associatedDriverId: Long? = null,
    val associatedPartnerId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "partners")
data class PartnerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: PartnerType,
    val rating: Double,
    val isOpen: Boolean = true,
    val distanceKm: Double,
    val deliveryTimeMinutes: Int,
    val deliveryFee: Double,
    val address: String,
    val logoEmoji: String,
    val workingHours: String = "9:00 ص - 12:00 م",
    val approvalWorkflow: ApprovalWorkflow = ApprovalWorkflow.PARTNER,
    val activeOrdersCount: Int = 0
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partnerId: Long,
    val category: String,
    val name: String,
    val description: String,
    val price: Double,
    val status: ProductStatus = ProductStatus.AVAILABLE,
    val imageEmoji: String,
    val sizesString: String = "صغير:0.0,وسط:20.0,كبير:40.0", // options
    val addonsString: String = "جبنة زيادة:15.0,صوص خاص:10.0,مخلل:5.0"
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    val partnerId: Long,
    val partnerName: String,
    val partnerType: PartnerType,
    val driverId: Long? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val orderStatus: OrderStatus = OrderStatus.CREATED,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.WAITING_FOR_DRIVER,
    val dispatchMode: DispatchMode = DispatchMode.OPEN_DISPATCH,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val transferReceiptNote: String = "",
    val deliveryAddress: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double = 0.0,
    val total: Double,
    val customerNotes: String = "",
    val estimatedPrepMinutes: Int = 20,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val optionsSummary: String = "",
    val totalPrice: Double
)

@Entity(tableName = "order_activity_logs")
data class OrderActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val timeFormatted: String,
    val actor: String,
    val actorRole: String,
    val action: String,
    val oldValue: String,
    val newValue: String,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "onboarding_pages")
data class OnboardingPageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sortOrder: Int,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val active: Boolean = true
)

@Entity(tableName = "home_sections")
data class HomeSectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: HomeSectionType,
    val displayType: String = "HORIZONTAL_CARDS",
    val limitCount: Int = 10,
    val sortOrder: Int,
    val active: Boolean = true
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val discountPercent: Int,
    val minOrder: Double,
    val active: Boolean = true,
    val usageCount: Int = 0
)

@Entity(tableName = "drivers")
data class DriverProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val vehicle: String,
    val status: DriverStatus = DriverStatus.AVAILABLE,
    val workingArea: String = "القاهرة والجيزة",
    val currentOrderId: Long? = null,
    val todayEarnings: Double = 180.0,
    val totalEarnings: Double = 2450.0,
    val rating: Double = 4.9,
    val completedOrdersCount: Int = 42
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val ticketNumber: String,
    val customerName: String,
    val orderId: Long? = null,
    val category: TicketCategory = TicketCategory.ORDER_PROBLEM,
    val subject: String,
    val message: String,
    val status: TicketStatus = TicketStatus.OPEN,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customer_addresses")
data class CustomerAddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val label: String, // منزل، عمل، آخر
    val area: String,
    val street: String,
    val building: String,
    val floor: String,
    val apartment: String,
    val notes: String = "",
    val isDefault: Boolean = false
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetUserId: Long? = null,
    val targetRole: UserRole = UserRole.CUSTOMER,
    val category: NotificationCategory = NotificationCategory.ORDER,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val relatedOrderId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val onboardingEnabled: Boolean = true,
    val defaultApproval: ApprovalWorkflow = ApprovalWorkflow.PARTNER,
    val defaultDispatchMode: DispatchMode = DispatchMode.OPEN_DISPATCH,
    val dispatchTriggerTiming: String = "عند بدء التجهيز"
)

@Entity(tableName = "favorite_partners")
data class FavoritePartnerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val partnerId: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_reviews")
data class OrderReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val orderId: Long,
    val partnerRating: Int = 5,
    val driverRating: Int = 5,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "driver_payout_requests")
data class DriverPayoutRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val driverId: Long,
    val amount: Double,
    val status: String = "PENDING", // PENDING, APPROVED, TRANSFERRED
    val createdAt: Long = System.currentTimeMillis()
) {
    val statusArabic: String
        get() = when (status) {
            "APPROVED" -> "تمت الموافقة 🟢"
            "REJECTED" -> "مرفوض 🔴"
            else -> "قيد المراجعة ⏳"
        }
}
