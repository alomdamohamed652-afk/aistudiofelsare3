package com.example.data.local

import androidx.room.TypeConverter
import com.example.core.model.*

class FalsareeTypeConverters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name
    @TypeConverter
    fun toUserRole(value: String): UserRole = try { UserRole.valueOf(value) } catch (e: Exception) { UserRole.CUSTOMER }

    @TypeConverter
    fun fromPartnerType(value: PartnerType): String = value.name
    @TypeConverter
    fun toPartnerType(value: String): PartnerType = try { PartnerType.valueOf(value) } catch (e: Exception) { PartnerType.RESTAURANT }

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name
    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = try { OrderStatus.valueOf(value) } catch (e: Exception) { OrderStatus.CREATED }

    @TypeConverter
    fun fromDeliveryStatus(value: DeliveryStatus): String = value.name
    @TypeConverter
    fun toDeliveryStatus(value: String): DeliveryStatus = try { DeliveryStatus.valueOf(value) } catch (e: Exception) { DeliveryStatus.WAITING_FOR_DRIVER }

    @TypeConverter
    fun fromDispatchMode(value: DispatchMode): String = value.name
    @TypeConverter
    fun toDispatchMode(value: String): DispatchMode = try { DispatchMode.valueOf(value) } catch (e: Exception) { DispatchMode.OPEN_DISPATCH }

    @TypeConverter
    fun fromApprovalWorkflow(value: ApprovalWorkflow): String = value.name
    @TypeConverter
    fun toApprovalWorkflow(value: String): ApprovalWorkflow = try { ApprovalWorkflow.valueOf(value) } catch (e: Exception) { ApprovalWorkflow.PARTNER }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name
    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = try { PaymentMethod.valueOf(value) } catch (e: Exception) { PaymentMethod.CASH_ON_DELIVERY }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name
    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = try { PaymentStatus.valueOf(value) } catch (e: Exception) { PaymentStatus.PENDING }

    @TypeConverter
    fun fromDriverStatus(value: DriverStatus): String = value.name
    @TypeConverter
    fun toDriverStatus(value: String): DriverStatus = try { DriverStatus.valueOf(value) } catch (e: Exception) { DriverStatus.AVAILABLE }

    @TypeConverter
    fun fromProductStatus(value: ProductStatus): String = value.name
    @TypeConverter
    fun toProductStatus(value: String): ProductStatus = try { ProductStatus.valueOf(value) } catch (e: Exception) { ProductStatus.AVAILABLE }

    @TypeConverter
    fun fromHomeSectionType(value: HomeSectionType): String = value.name
    @TypeConverter
    fun toHomeSectionType(value: String): HomeSectionType = try { HomeSectionType.valueOf(value) } catch (e: Exception) { HomeSectionType.CUSTOM }

    @TypeConverter
    fun fromNotificationCategory(value: NotificationCategory): String = value.name
    @TypeConverter
    fun toNotificationCategory(value: String): NotificationCategory = try { NotificationCategory.valueOf(value) } catch (e: Exception) { NotificationCategory.ORDER }

    @TypeConverter
    fun fromTicketCategory(value: TicketCategory): String = value.name
    @TypeConverter
    fun toTicketCategory(value: String): TicketCategory = try { TicketCategory.valueOf(value) } catch (e: Exception) { TicketCategory.OTHER }

    @TypeConverter
    fun fromTicketStatus(value: TicketStatus): String = value.name
    @TypeConverter
    fun toTicketStatus(value: String): TicketStatus = try { TicketStatus.valueOf(value) } catch (e: Exception) { TicketStatus.OPEN }
}
