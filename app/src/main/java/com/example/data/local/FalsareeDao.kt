package com.example.data.local

import androidx.room.*
import com.example.core.model.PartnerType
import com.example.core.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface FalsareeDao {

    // --- Partners ---
    @Query("SELECT * FROM partners ORDER BY rating DESC")
    fun getAllPartners(): Flow<List<PartnerEntity>>

    @Query("SELECT * FROM partners WHERE type = :type ORDER BY rating DESC")
    fun getPartnersByType(type: PartnerType): Flow<List<PartnerEntity>>

    @Query("SELECT * FROM partners WHERE id = :id")
    suspend fun getPartnerById(id: Long): PartnerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: PartnerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartners(partners: List<PartnerEntity>)

    @Update
    suspend fun updatePartner(partner: PartnerEntity)

    // --- Products ---
    @Query("SELECT * FROM products WHERE partnerId = :partnerId")
    fun getProductsForPartner(partnerId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getOrdersForCustomer(customerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE partnerId = :partnerId ORDER BY createdAt DESC")
    fun getOrdersForPartner(partnerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE driverId = :driverId AND deliveryStatus NOT IN ('DELIVERED', 'NOT_REQUIRED') AND orderStatus NOT IN ('CANCELLED', 'REJECTED') ORDER BY createdAt DESC LIMIT 1")
    fun getActiveOrderForDriver(driverId: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE driverId IS NULL AND deliveryStatus = 'WAITING_FOR_DRIVER' AND orderStatus NOT IN ('CANCELLED', 'REJECTED') ORDER BY createdAt ASC")
    fun getOpenOrdersForDrivers(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderFlow(orderId: Long): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    // --- Order Items ---
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getItemsForOrder(orderId: Long): Flow<List<OrderItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    // --- Activity Logs ---
    @Query("SELECT * FROM order_activity_logs WHERE orderId = :orderId ORDER BY createdAt DESC")
    fun getLogsForOrder(orderId: Long): Flow<List<OrderActivityLogEntity>>

    @Query("SELECT * FROM order_activity_logs ORDER BY createdAt DESC LIMIT 50")
    fun getRecentActivityLogs(): Flow<List<OrderActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: OrderActivityLogEntity)

    // --- Onboarding Pages ---
    @Query("SELECT * FROM onboarding_pages WHERE active = 1 ORDER BY sortOrder ASC")
    fun getActiveOnboardingPages(): Flow<List<OnboardingPageEntity>>

    @Query("SELECT * FROM onboarding_pages ORDER BY sortOrder ASC")
    fun getAllOnboardingPages(): Flow<List<OnboardingPageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnboardingPages(pages: List<OnboardingPageEntity>)

    @Update
    suspend fun updateOnboardingPage(page: OnboardingPageEntity)

    @Delete
    suspend fun deleteOnboardingPage(page: OnboardingPageEntity)

    // --- Home Sections ---
    @Query("SELECT * FROM home_sections WHERE active = 1 ORDER BY sortOrder ASC")
    fun getActiveHomeSections(): Flow<List<HomeSectionEntity>>

    @Query("SELECT * FROM home_sections ORDER BY sortOrder ASC")
    fun getAllHomeSections(): Flow<List<HomeSectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeSections(sections: List<HomeSectionEntity>)

    @Update
    suspend fun updateHomeSection(section: HomeSectionEntity)

    @Delete
    suspend fun deleteHomeSection(section: HomeSectionEntity)

    // --- Coupons ---
    @Query("SELECT * FROM coupons ORDER BY id DESC")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code AND active = 1 LIMIT 1")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<CouponEntity>)

    @Update
    suspend fun updateCoupon(coupon: CouponEntity)

    // --- Drivers ---
    @Query("SELECT * FROM drivers ORDER BY id ASC")
    fun getAllDrivers(): Flow<List<DriverProfileEntity>>

    @Query("SELECT * FROM drivers WHERE id = :id")
    suspend fun getDriverById(id: Long): DriverProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<DriverProfileEntity>)

    @Update
    suspend fun updateDriver(driver: DriverProfileEntity)

    // --- Support Tickets ---
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity): Long

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)

    // --- Customer Addresses ---
    @Query("SELECT * FROM customer_addresses ORDER BY isDefault DESC, id ASC")
    fun getAllAddresses(): Flow<List<CustomerAddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: CustomerAddressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddresses(addresses: List<CustomerAddressEntity>)

    @Delete
    suspend fun deleteAddress(address: CustomerAddressEntity)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE targetRole = :role ORDER BY createdAt DESC")
    fun getNotificationsForRole(role: UserRole): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE targetRole = :role AND isRead = 0")
    fun getUnreadNotificationsCount(role: UserRole): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE targetRole = :role")
    suspend fun markAllNotificationsAsRead(role: UserRole)

    // --- App Settings ---
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettingsEntity)

    @Update
    suspend fun updateSettings(settings: AppSettingsEntity)

    // --- Favorites ---
    @Query("SELECT partnerId FROM favorite_partners")
    fun getFavoritePartnerIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoritePartnerEntity)

    @Query("DELETE FROM favorite_partners WHERE partnerId = :partnerId")
    suspend fun removeFavorite(partnerId: Long)

    // --- Order Reviews ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: OrderReviewEntity): Long

    @Query("SELECT * FROM order_reviews WHERE orderId = :orderId LIMIT 1")
    suspend fun getReviewForOrder(orderId: Long): OrderReviewEntity?

    // --- Driver Payout Requests ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayoutRequest(payout: DriverPayoutRequestEntity): Long

    @Query("SELECT * FROM driver_payout_requests WHERE driverId = :driverId ORDER BY createdAt DESC")
    fun getPayoutRequestsForDriver(driverId: Long): Flow<List<DriverPayoutRequestEntity>>

    @Query("SELECT * FROM driver_payout_requests ORDER BY createdAt DESC")
    fun getAllPayoutRequests(): Flow<List<DriverPayoutRequestEntity>>
}

