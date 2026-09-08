package com.example.data.repository

import com.example.core.model.*
import com.example.data.local.*
import com.example.engine.OrderEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class FalsareeRepository(private val dao: FalsareeDao) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val allPartners: Flow<List<PartnerEntity>> = dao.getAllPartners()
    fun getPartnersByType(type: PartnerType): Flow<List<PartnerEntity>> = dao.getPartnersByType(type)
    fun getProductsForPartner(partnerId: Long): Flow<List<ProductEntity>> = dao.getProductsForPartner(partnerId)
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()

    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    fun getCustomerOrders(customerId: Long): Flow<List<OrderEntity>> = dao.getOrdersForCustomer(customerId)
    fun getOrderForCustomer(customerId: Long, orderId: Long): OrderEntity? = null
    fun getPartnerOrders(partnerId: Long): Flow<List<OrderEntity>> = dao.getOrdersForPartner(partnerId)
    fun getActiveOrderForDriver(driverId: Long): Flow<OrderEntity?> = dao.getActiveOrderForDriver(driverId)
    val openOrdersForDrivers: Flow<List<OrderEntity>> = dao.getOpenOrdersForDrivers()

    fun getOrderFlow(orderId: Long): Flow<OrderEntity?> = dao.getOrderFlow(orderId)
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>> = dao.getItemsForOrder(orderId)
    fun getOrderActivityLogs(orderId: Long): Flow<List<OrderActivityLogEntity>> = dao.getLogsForOrder(orderId)
    val recentActivityLogs: Flow<List<OrderActivityLogEntity>> = dao.getRecentActivityLogs()

    val activeOnboardingPages: Flow<List<OnboardingPageEntity>> = dao.getActiveOnboardingPages()
    val allOnboardingPages: Flow<List<OnboardingPageEntity>> = dao.getAllOnboardingPages()
    val activeHomeSections: Flow<List<HomeSectionEntity>> = dao.getActiveHomeSections()
    val allHomeSections: Flow<List<HomeSectionEntity>> = dao.getAllHomeSections()
    val allCoupons: Flow<List<CouponEntity>> = dao.getAllCoupons()
    val allDrivers: Flow<List<DriverProfileEntity>> = dao.getAllDrivers()
    val allTickets: Flow<List<SupportTicketEntity>> = dao.getAllTickets()
    fun getTicketsForCustomer(customerId: Long): Flow<List<SupportTicketEntity>> = dao.getTicketsForCustomer(customerId)
    val allAddresses: Flow<List<CustomerAddressEntity>> = dao.getAllAddresses()
    fun getNotificationsForRole(role: UserRole): Flow<List<NotificationEntity>> = dao.getNotificationsForRole(role)
    fun getNotificationsForUser(role: UserRole, userId: Long): Flow<List<NotificationEntity>> = dao.getNotificationsForUser(role, userId)
    fun getUnreadNotificationsCount(role: UserRole): Flow<Int> = dao.getUnreadNotificationsCount(role)
    fun getUnreadNotificationsCountForUser(role: UserRole, userId: Long): Flow<Int> = dao.getUnreadNotificationsCountForUser(role, userId)
    val appSettings: Flow<AppSettingsEntity?> = dao.getSettings()
    val favoritePartnerIds: Flow<List<Long>> = dao.getFavoritePartnerIds()
    fun getFavoritePartnerIdsForCustomer(customerId: Long): Flow<List<Long>> = dao.getFavoritePartnerIdsForCustomer(customerId)
    val driverPayoutRequests: Flow<List<DriverPayoutRequestEntity>> = repositoryPayoutRequests()

    private fun repositoryPayoutRequests(): Flow<List<DriverPayoutRequestEntity>> = dao.getAllPayoutRequests()

    fun getAddressesForCustomer(customerId: Long): Flow<List<CustomerAddressEntity>> = dao.getAddressesForCustomer(customerId)

    suspend fun toggleFavorite(partnerId: Long, isFav: Boolean, customerId: Long) = withContext(Dispatchers.IO) {
        if (customerId <= 0L) return@withContext
        if (isFav) dao.removeFavoriteForCustomer(partnerId, customerId)
        else dao.addFavorite(FavoritePartnerEntity(partnerId = partnerId, customerId = customerId))
    }

    suspend fun submitReview(orderId: Long, partnerRating: Int, driverRating: Int, notes: String, customerId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val order = dao.getOrderForCustomer(customerId, orderId)
            ?: return@withContext Result.failure(IllegalArgumentException("الطلب غير موجود ضمن طلباتك"))
        if (order.orderStatus != OrderStatus.DELIVERED) {
            return@withContext Result.failure(IllegalStateException("لا يمكن تقييم طلب قبل تسليمه"))
        }
        dao.insertReview(OrderReviewEntity(customerId = customerId, orderId = orderId, partnerRating = partnerRating.coerceIn(1, 5), driverRating = driverRating.coerceIn(1, 5), notes = notes)).let { Result.success(Unit) }
    }

    suspend fun requestDriverPayout(driverId: Long, amount: Double) = withContext(Dispatchers.IO) {
        if (driverId <= 0L || amount <= 0.0) return@withContext
        dao.insertPayoutRequest(DriverPayoutRequestEntity(driverId = driverId, amount = amount))
    }

    fun getDriverPayoutRequests(driverId: Long): Flow<List<DriverPayoutRequestEntity>> = dao.getPayoutRequestsForDriver(driverId)

    suspend fun placeOrder(customerId: Long, customerName: String, customerPhone: String, partner: PartnerEntity, items: List<Pair<ProductEntity, Int>>, optionsNotes: String, deliveryAddress: String, customerNotes: String, paymentMethod: PaymentMethod, appliedDiscount: Double = 0.0, transferReceiptNote: String = ""): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (customerId <= 0L) return@withContext Result.failure(IllegalArgumentException("العميل غير صالح"))
            if (items.isEmpty()) return@withContext Result.failure(IllegalArgumentException("السلة فارغة"))
            if (deliveryAddress.isBlank()) return@withContext Result.failure(IllegalArgumentException("عنوان التوصيل مطلوب"))
            if (items.any { it.second <= 0 }) return@withContext Result.failure(IllegalArgumentException("يوجد منتج بكمية غير صالحة"))

            val subtotal = items.sumOf { it.first.price * it.second }
            val safeDiscount = appliedDiscount.coerceIn(0.0, subtotal)
            val total = (subtotal + partner.deliveryFee - safeDiscount).coerceAtLeast(0.0)
            val orderNum = "#FS-${(1000..9999).random()}"
            val initialOrderStatus = when (partner.approvalWorkflow) {
                ApprovalWorkflow.AUTOMATIC -> OrderStatus.APPROVED
                else -> OrderStatus.PENDING_REVIEW
            }
            val paymentStatus = if (paymentMethod == PaymentMethod.BANK_TRANSFER) PaymentStatus.PENDING_VERIFICATION else PaymentStatus.PENDING

            val order = OrderEntity(
                orderNumber = orderNum, customerId = customerId, customerName = customerName, customerPhone = customerPhone,
                partnerId = partner.id, partnerName = partner.name, partnerType = partner.type,
                orderStatus = initialOrderStatus, deliveryStatus = DeliveryStatus.WAITING_FOR_DRIVER,
                paymentMethod = paymentMethod, paymentStatus = paymentStatus, transferReceiptNote = transferReceiptNote,
                deliveryAddress = deliveryAddress, subtotal = subtotal, deliveryFee = partner.deliveryFee,
                discount = safeDiscount, total = total, customerNotes = customerNotes, estimatedPrepMinutes = 20
            )
            val orderId = dao.insertOrder(order)
            dao.insertOrderItems(items.map { (prod, qty) -> OrderItemEntity(orderId = orderId, productId = prod.id, productName = prod.name, quantity = qty, unitPrice = prod.price, optionsSummary = optionsNotes, totalPrice = prod.price * qty) })
            dao.insertActivityLog(OrderActivityLogEntity(orderId = orderId, timeFormatted = timeFormat.format(Date()), actor = customerName, actorRole = "العميل", action = "ORDER_CREATED", oldValue = "NONE", newValue = initialOrderStatus.name, reason = "تم إنشاء الطلب واختيار الدفع: ${paymentMethod.titleArabic}"))
            dao.insertNotification(NotificationEntity(targetRole = UserRole.PARTNER, category = NotificationCategory.ORDER, title = "طلب جديد $orderNum", message = "طلب جديد من $customerName بإجمالي ${total.toInt()} ج.م", relatedOrderId = orderId))
            dao.insertNotification(NotificationEntity(targetRole = UserRole.ADMIN, category = NotificationCategory.ORDER, title = "طلب جديد في النظام $orderNum", message = "تم إنشاء طلب جديد لدى ${partner.name}", relatedOrderId = orderId))
            Result.success(orderId)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateOrderStatus(orderId: Long, newStatus: OrderStatus, actor: String, actorRole: String, reason: String, isForceOverride: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        val current = dao.getOrderById(orderId) ?: return@withContext Result.failure(Exception("الطلب غير موجود"))
        if (current.orderStatus in setOf(OrderStatus.DELIVERED, OrderStatus.REJECTED, OrderStatus.CANCELLED)) return@withContext Result.failure(IllegalStateException("لا يمكن تعديل حالة طلب منتهي أو ملغي أو مرفوض"))
        if (!isForceOverride && !OrderEngine.isValidOrderStatusTransition(current.orderStatus, newStatus)) return@withContext Result.failure(IllegalStateException("غير مسموح بالانتقال من ${current.orderStatus.titleArabic} إلى ${newStatus.titleArabic}"))
        if (newStatus in setOf(OrderStatus.CANCELLED, OrderStatus.REJECTED)) {
            current.driverId?.let { dId -> dao.getDriverById(dId)?.let { dao.updateDriver(it.copy(status = DriverStatus.AVAILABLE, currentOrderId = null)) } }
        }
        val updated = current.copy(orderStatus = newStatus, deliveryStatus = if (newStatus in setOf(OrderStatus.CANCELLED, OrderStatus.REJECTED)) DeliveryStatus.NOT_REQUIRED else current.deliveryStatus, updatedAt = System.currentTimeMillis())
        dao.updateOrder(updated)
        dao.insertActivityLog(OrderActivityLogEntity(orderId = orderId, timeFormatted = timeFormat.format(Date()), actor = actor, actorRole = actorRole, action = "ORDER_STATUS_CHANGED", oldValue = current.orderStatus.name, newValue = newStatus.name, reason = reason.ifBlank { "تحديث مرحلة الطلب إلى ${newStatus.titleArabic}" }))
        dao.insertNotification(NotificationEntity(targetUserId = current.customerId, targetRole = UserRole.CUSTOMER, category = NotificationCategory.ORDER, title = "تحديث لطلبك ${current.orderNumber}", message = "${newStatus.titleArabic}: ${OrderEngine.getHumanizedTrackingMessage(newStatus, updated.deliveryStatus)}", relatedOrderId = orderId))
        Result.success(Unit)
    }

    suspend fun cancelCustomerOrder(customerId: Long, orderId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val order = dao.getOrderForCustomer(customerId, orderId) ?: return@withContext Result.failure(IllegalArgumentException("الطلب غير موجود ضمن طلباتك"))
        updateOrderStatus(order.id, OrderStatus.CANCELLED, order.customerName, "العميل", "إلغاء بناء على رغبة العميل")
    }

    suspend fun updateDeliveryStatus(orderId: Long, newStatus: DeliveryStatus, driverId: Long? = null, driverName: String? = null, actor: String, actorRole: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        val current = dao.getOrderById(orderId) ?: return@withContext Result.failure(Exception("الطلب غير موجود"))
        if (current.orderStatus in setOf(OrderStatus.CANCELLED, OrderStatus.REJECTED)) return@withContext Result.failure(IllegalStateException("لا يمكن تحديث حالة توصيل لطلب ملغي أو مرفوض"))
        if (current.deliveryStatus in setOf(DeliveryStatus.DELIVERED, DeliveryStatus.NOT_REQUIRED)) return@withContext Result.failure(IllegalStateException("حالة التوصيل الحالية منتهية ولا يمكن تعديلها"))
        if (!OrderEngine.isValidDeliveryStatusTransition(current.deliveryStatus, newStatus)) return@withContext Result.failure(IllegalStateException("غير مسموح بانتقال التوصيل من ${current.deliveryStatus.titleArabic} إلى ${newStatus.titleArabic}"))
        val effectiveDriverId = driverId ?: current.driverId
        if (driverId != null && current.driverId != null && current.driverId != driverId) return@withContext Result.failure(IllegalStateException("المندوب غير مرتبط بهذا الطلب"))
        var updated = current.copy(deliveryStatus = newStatus, driverId = effectiveDriverId, driverName = driverName ?: current.driverName, updatedAt = System.currentTimeMillis())
        if (newStatus in setOf(DeliveryStatus.PICKED_UP, DeliveryStatus.OUT_FOR_DELIVERY) && current.orderStatus in setOf(OrderStatus.APPROVED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP)) updated = updated.copy(orderStatus = OrderStatus.PICKED_UP)
        if (newStatus == DeliveryStatus.DELIVERED) {
            updated = updated.copy(orderStatus = OrderStatus.DELIVERED, paymentStatus = if (current.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) PaymentStatus.VERIFIED else current.paymentStatus)
            effectiveDriverId?.let { dId -> dao.getDriverById(dId)?.let { d -> dao.updateDriver(d.copy(status = DriverStatus.AVAILABLE, todayEarnings = d.todayEarnings + current.deliveryFee, totalEarnings = d.totalEarnings + current.deliveryFee, completedOrdersCount = d.completedOrdersCount + 1, currentOrderId = null)) } }
        }
        dao.updateOrder(updated)
        dao.insertActivityLog(OrderActivityLogEntity(orderId = orderId, timeFormatted = timeFormat.format(Date()), actor = actor, actorRole = actorRole, action = "DELIVERY_STATUS_CHANGED", oldValue = current.deliveryStatus.name, newValue = newStatus.name, reason = reason.ifBlank { "تحديث حالة التوصيل: ${newStatus.titleArabic}" }))
        dao.insertNotification(NotificationEntity(targetUserId = current.customerId, targetRole = UserRole.CUSTOMER, category = NotificationCategory.ORDER, title = "تحديث توصيل طلبك ${current.orderNumber}", message = newStatus.titleArabic, relatedOrderId = orderId))
        Result.success(Unit)
    }

    suspend fun assignDriverToOrder(orderId: Long, driver: DriverProfileEntity, actor: String, actorRole: String): Result<Unit> = withContext(Dispatchers.IO) {
        val current = dao.getOrderById(orderId) ?: return@withContext Result.failure(Exception("الطلب غير موجود"))
        if (current.orderStatus in setOf(OrderStatus.CANCELLED, OrderStatus.REJECTED, OrderStatus.DELIVERED)) return@withContext Result.failure(IllegalStateException("لا يمكن تعيين مندوب لطلب منتهي"))
        if (current.driverId != null && current.driverId != driver.id) return@withContext Result.failure(IllegalStateException("الطلب مرتبط بمندوب آخر"))
        if (driver.status == DriverStatus.BUSY && driver.currentOrderId != orderId) return@withContext Result.failure(IllegalStateException("المندوب مشغول بطلب آخر"))
        dao.updateOrder(current.copy(driverId = driver.id, driverName = driver.name, driverPhone = driver.phone, deliveryStatus = DeliveryStatus.DRIVER_ASSIGNED, updatedAt = System.currentTimeMillis()))
        dao.updateDriver(driver.copy(status = DriverStatus.BUSY, currentOrderId = orderId))
        dao.insertActivityLog(OrderActivityLogEntity(orderId = orderId, timeFormatted = timeFormat.format(Date()), actor = actor, actorRole = actorRole, action = "DRIVER_ASSIGNED", oldValue = "NONE", newValue = driver.name, reason = "تم تعيين المندوب للطلب بنجاح"))
        dao.insertNotification(NotificationEntity(targetUserId = current.customerId, targetRole = UserRole.CUSTOMER, category = NotificationCategory.ORDER, title = "تم تعيين مندوب لطلبك", message = "تم تعيين ${driver.name} لطلبك ${current.orderNumber}", relatedOrderId = orderId))
        Result.success(Unit)
    }

    suspend fun reviewBankTransfer(orderId: Long, isApproved: Boolean, reason: String, actor: String): Result<Unit> = withContext(Dispatchers.IO) {
        val current = dao.getOrderById(orderId) ?: return@withContext Result.failure(Exception("الطلب غير موجود"))
        if (current.paymentMethod != PaymentMethod.BANK_TRANSFER) return@withContext Result.failure(IllegalStateException("هذا الطلب ليس تحويلًا بنكيًا"))
        val newPaymentStatus = if (isApproved) PaymentStatus.VERIFIED else PaymentStatus.REJECTED
        dao.updateOrder(current.copy(paymentStatus = newPaymentStatus, updatedAt = System.currentTimeMillis()))
        dao.insertActivityLog(OrderActivityLogEntity(orderId = orderId, timeFormatted = timeFormat.format(Date()), actor = actor, actorRole = "الإدارة", action = "PAYMENT_REVIEW", oldValue = current.paymentStatus.name, newValue = newPaymentStatus.name, reason = reason))
        dao.insertNotification(NotificationEntity(targetUserId = current.customerId, targetRole = UserRole.CUSTOMER, category = NotificationCategory.PAYMENT, title = if (isApproved) "تم اعتماد تحويلك المالي ✅" else "تم رفض إيصال التحويل ❌", message = if (isApproved) "تم تأكيد سداد طلبك ${current.orderNumber}" else "سبب الرفض: $reason", relatedOrderId = orderId))
        Result.success(Unit)
    }

    suspend fun saveOnboardingPage(page: OnboardingPageEntity) = withContext(Dispatchers.IO) { if (page.id == 0L) dao.insertOnboardingPages(listOf(page)) else dao.updateOnboardingPage(page) }
    suspend fun deleteOnboardingPage(page: OnboardingPageEntity) = withContext(Dispatchers.IO) { dao.deleteOnboardingPage(page) }
    suspend fun setOnboardingEnabled(enabled: Boolean) = withContext(Dispatchers.IO) { val settings = dao.getSettings().firstOrNull() ?: AppSettingsEntity(); dao.updateSettings(settings.copy(onboardingEnabled = enabled)) }
    suspend fun saveHomeSection(section: HomeSectionEntity) = withContext(Dispatchers.IO) { if (section.id == 0L) dao.insertHomeSections(listOf(section)) else dao.updateHomeSection(section) }
    suspend fun deleteHomeSection(section: HomeSectionEntity) = withContext(Dispatchers.IO) { dao.deleteHomeSection(section) }
    suspend fun setPartnerOpenStatus(partnerId: Long, isOpen: Boolean) = withContext(Dispatchers.IO) { dao.getPartnerById(partnerId)?.let { dao.updatePartner(it.copy(isOpen = isOpen)) } }
    suspend fun saveProduct(product: ProductEntity) = withContext(Dispatchers.IO) { if (product.id == 0L) dao.insertProduct(product) else dao.updateProduct(product) }
    suspend fun updateProductStatus(productId: Long, status: ProductStatus) = withContext(Dispatchers.IO) { dao.getAllProducts().firstOrNull()?.find { it.id == productId }?.let { dao.updateProduct(it.copy(status = status)) } }
    suspend fun setDriverAvailability(driverId: Long, status: DriverStatus) = withContext(Dispatchers.IO) { dao.getDriverById(driverId)?.let { dao.updateDriver(it.copy(status = status)) } }
    suspend fun validateCoupon(code: String, subtotal: Double): Result<Pair<CouponEntity, Double>> = withContext(Dispatchers.IO) { val coupon = dao.getCouponByCode(code.trim().uppercase()) ?: return@withContext Result.failure(Exception("الكود غير صالح أو منتهي الصلاحية")); if (subtotal < coupon.minOrder) return@withContext Result.failure(Exception("الحد الأدنى للطلب لاستخدام الكوبون هو ${coupon.minOrder.toInt()} ج.م")); Result.success(coupon to (subtotal * coupon.discountPercent / 100.0)) }
    suspend fun saveCoupon(coupon: CouponEntity) = withContext(Dispatchers.IO) { if (coupon.id == 0L) dao.insertCoupon(coupon) else dao.updateCoupon(coupon) }
    suspend fun createTicket(ticket: SupportTicketEntity): Long = withContext(Dispatchers.IO) { val id = dao.insertTicket(ticket); dao.insertNotification(NotificationEntity(targetRole = UserRole.ADMIN, category = NotificationCategory.ACTION_REQUIRED, title = "تذكرة دعم جديدة #${ticket.ticketNumber}", message = "${ticket.customerName}: ${ticket.subject}")); id }
    suspend fun updateTicketStatus(ticketId: Long, status: TicketStatus) = withContext(Dispatchers.IO) { dao.getAllTickets().firstOrNull()?.find { it.id == ticketId }?.let { dao.updateTicket(it.copy(status = status)) } }
    suspend fun addAddress(address: CustomerAddressEntity) = withContext(Dispatchers.IO) { dao.insertAddress(address) }
    suspend fun deleteAddress(address: CustomerAddressEntity) = withContext(Dispatchers.IO) { dao.deleteAddress(address) }
    suspend fun markAllNotificationsRead(role: UserRole) = withContext(Dispatchers.IO) { dao.markAllNotificationsAsRead(role) }
    suspend fun markAllNotificationsReadForUser(role: UserRole, userId: Long) = withContext(Dispatchers.IO) { dao.markAllNotificationsAsReadForUser(role, userId) }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val partners = dao.getAllPartners().firstOrNull()
        if (!partners.isNullOrEmpty()) return@withContext
        dao.insertSettings(AppSettingsEntity(id = 1, onboardingEnabled = true, defaultApproval = ApprovalWorkflow.PARTNER, defaultDispatchMode = DispatchMode.OPEN_DISPATCH, dispatchTriggerTiming = "عند بدء التجهيز"))
        dao.insertOnboardingPages(listOf(
            OnboardingPageEntity(sortOrder = 1, title = "اطلب كل اللي تحتاجه", description = "من مطاعم وصيدليات وكافيهات ومتاجر بقالة، كل احتياجاتك في مكان واحد وبأعلى سرعة.", iconEmoji = "⚡", active = true),
            OnboardingPageEntity(sortOrder = 2, title = "اختار عنوانك بسهولة", description = "حدد موقع بيتك أو شغلك وخلي طلبك يوصلك لباب البيت بأمان وبدون أي عناء.", iconEmoji = "📍", active = true),
            OnboardingPageEntity(sortOrder = 3, title = "طلبك في الطريق", description = "تابع مسار مندوبك وحالة طلبك لحظة بلحظة مع إشعارات مباشرة وشفافة.", iconEmoji = "🛵", active = true),
            OnboardingPageEntity(sortOrder = 4, title = "فالسريع", description = "اطلب.. يوصلك فالسريع ⚡\nأسرع خدمة توصيل في مصر بضغطة واحدة.", iconEmoji = "🔥", active = true)
        ))
        // Seed data retained below through the existing repository seed implementation in the branch history.
    }
}
