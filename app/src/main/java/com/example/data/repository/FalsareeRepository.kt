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

    private class NoEligibleDriverException(message: String) : IllegalStateException(message)

    private fun isActiveDriverAccount(user: UserEntity?): Boolean =
        user != null && user.role == UserRole.DRIVER && user.isActive && user.activationStatus == "ACTIVE"

    // --- Flows ---
    val allPartners: Flow<List<PartnerEntity>> = dao.getAllPartners()
    fun getPartnersByType(type: PartnerType): Flow<List<PartnerEntity>> = dao.getPartnersByType(type)
    fun getProductsForPartner(partnerId: Long): Flow<List<ProductEntity>> = dao.getProductsForPartner(partnerId)
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()

    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    fun getCustomerOrders(customerId: Long): Flow<List<OrderEntity>> = dao.getOrdersForCustomer(customerId)
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
    val driverPayoutRequests: Flow<List<DriverPayoutRequestEntity>> = dao.getAllPayoutRequests()
    val driverShiftAssignments: Flow<List<DriverShiftAssignmentEntity>> = dao.getAllDriverShiftAssignments()

    /**
     * Hybrid dispatch foundation: the next eligible driver is selected by the
     * configured shift queue. Busy, inactive, or forced-break drivers are skipped.
     */
    suspend fun getNextEligibleDriver(
        shiftName: String,
        excludedDriverIds: Set<Long> = emptySet()
    ): DriverProfileEntity? = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        normalizeExpiredDriverBreaks(now)
        val assignments = dao.getActiveDriverShiftAssignments().firstOrNull()
            .orEmpty()
            .filter { it.shiftName == shiftName && it.forcedBreakUntil <= now && it.driverId !in excludedDriverIds }
            .sortedBy { it.queuePosition }
        for (assignment in assignments) {
            val driver = dao.getDriverById(assignment.driverId) ?: continue
            val user = dao.getUserByAssociatedDriverId(driver.id)
            if (isActiveDriverAccount(user) && driver.status == DriverStatus.AVAILABLE && driver.currentOrderId == null) return@withContext driver
        }
        null
    }

    suspend fun assignDriverToShift(driverId: Long, shiftName: String, queuePosition: Int) = withContext(Dispatchers.IO) {
        val existing = dao.getDriverShiftAssignment(driverId)
        val assignment = DriverShiftAssignmentEntity(
            id = existing?.id ?: 0L,
            driverId = driverId,
            shiftName = shiftName,
            queuePosition = queuePosition,
            active = true,
            forcedBreakUntil = existing?.forcedBreakUntil ?: 0L,
            statusBeforeBreak = existing?.statusBeforeBreak
        )
        if (existing == null) dao.insertDriverShiftAssignment(assignment) else dao.updateDriverShiftAssignment(assignment)
    }

    suspend fun moveDriverInShift(driverId: Long, shiftName: String, queuePosition: Int) =
        assignDriverToShift(driverId, shiftName, queuePosition)

    suspend fun reorderShift(shiftName: String, orderedDriverIds: List<Long>) = withContext(Dispatchers.IO) {
        orderedDriverIds.distinct().forEachIndexed { index, driverId ->
            assignDriverToShift(driverId, shiftName, index + 1)
        }
    }

    suspend fun setDriverShiftActive(driverId: Long, active: Boolean) = withContext(Dispatchers.IO) {
        dao.getDriverShiftAssignment(driverId)?.let { assignment ->
            dao.updateDriverShiftAssignment(assignment.copy(active = active, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun restoreDriverFromForcedBreak(driverId: Long) = withContext(Dispatchers.IO) {
        val assignment = dao.getDriverShiftAssignment(driverId)
        val previousStatus = assignment?.statusBeforeBreak
        assignment?.let {
            dao.updateDriverShiftAssignment(
                it.copy(
                    forcedBreakUntil = 0L,
                    statusBeforeBreak = null,
                    active = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        dao.getDriverById(driverId)?.let { driver ->
            if (driver.status == DriverStatus.BREAK) {
                dao.updateDriver(driver.copy(status = previousStatus ?: DriverStatus.AVAILABLE))
            }
        }
    }

    private suspend fun normalizeExpiredDriverBreaks(now: Long = System.currentTimeMillis()) {
        dao.getExpiredForcedBreakAssignments(now).forEach { assignment ->
            val driver = dao.getDriverById(assignment.driverId)
            val restoredStatus = assignment.statusBeforeBreak ?: DriverStatus.AVAILABLE
            dao.updateDriverShiftAssignment(
                assignment.copy(
                    forcedBreakUntil = 0L,
                    statusBeforeBreak = null,
                    updatedAt = now
                )
            )
            if (driver?.status == DriverStatus.BREAK) {
                dao.updateDriver(driver.copy(status = restoredStatus))
            }
        }
    }

    suspend fun putDriverOnForcedBreak(driverId: Long, minutes: Int) = withContext(Dispatchers.IO) {
        val assignment = dao.getDriverShiftAssignment(driverId)
            ?: return@withContext
        dao.updateDriverShiftAssignment(
            assignment.copy(
                forcedBreakUntil = System.currentTimeMillis() + minutes.coerceAtLeast(1) * 60_000L,
                statusBeforeBreak = dao.getDriverById(driverId)?.status ?: assignment.statusBeforeBreak,
                updatedAt = System.currentTimeMillis()
            )
        )
        dao.getDriverById(driverId)?.let { driver ->
            dao.updateDriver(driver.copy(status = DriverStatus.BREAK))
        }
    }

    suspend fun recordDriverDispatchEvent(orderId: Long, driverId: Long, eventType: String, reason: String = "", expiresAt: Long = 0L) =
        withContext(Dispatchers.IO) {
            dao.insertDriverDispatchEvent(
                DriverDispatchEventEntity(orderId = orderId, driverId = driverId, eventType = eventType, reason = reason, expiresAt = expiresAt)
            )
        }

    suspend fun timeoutDriverOffer(orderId: Long, driverId: Long, shiftName: String): Result<DriverProfileEntity> =
        withContext(Dispatchers.IO) {
            val order = dao.getOrderById(orderId)
                ?: return@withContext Result.failure(Exception("الطلب غير موجود"))
            if (order.driverId != null) {
                return@withContext Result.failure(IllegalStateException("الطلب تم تعيينه بالفعل"))
            }
            val offer = dao.getLatestDriverOffer(orderId, driverId)
                ?: return@withContext Result.failure(IllegalStateException("لا يوجد عرض لهذا المندوب"))
            if (offer.expiresAt <= 0L || System.currentTimeMillis() <= offer.expiresAt) {
                return@withContext Result.failure(IllegalStateException("مهلة العرض لم تنته بعد"))
            }
            if (offer.eventType == "OFFERED") {
                val latest = dao.getLatestOrderOffer(orderId)
                if (latest?.id != offer.id) {
                    return@withContext Result.failure(IllegalStateException("تم تجاوز العرض بعرض أحدث"))
                }
            }
            recordDriverDispatchEvent(orderId, driverId, "TIMEOUT", "انتهت مهلة قبول الطلب")
            updateDriverPerformance(driverId, "TIMEOUT")
            applyAutomaticPenaltyIfNeeded(orderId, driverId)
            if (offer.eventType == "OFFERED") {
                offerOrderToNextDriver(orderId, shiftName)
            } else {
                Result.success(dao.getDriverById(driverId) ?: return@withContext Result.failure(IllegalStateException("المندوب غير موجود")))
            }
        }


    fun getAddressesForCustomer(customerId: Long): Flow<List<CustomerAddressEntity>> = dao.getAddressesForCustomer(customerId)

    suspend fun toggleFavorite(partnerId: Long, isFav: Boolean, customerId: Long) = withContext(Dispatchers.IO) {
        if (isFav) {
            dao.removeFavoriteForCustomer(partnerId, customerId)
        } else {
            dao.addFavorite(FavoritePartnerEntity(partnerId = partnerId, customerId = customerId))
        }
    }

    suspend fun submitReview(orderId: Long, partnerRating: Int, driverRating: Int, notes: String, customerId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val order = dao.getOrderById(orderId)
            ?: return@withContext Result.failure(IllegalArgumentException("الطلب غير موجود"))
        if (order.customerId != customerId) {
            return@withContext Result.failure(SecurityException("لا يمكنك تقييم طلب مستخدم آخر"))
        }
        dao.insertReview(
            OrderReviewEntity(
                customerId = customerId,
                orderId = orderId,
                partnerRating = partnerRating,
                driverRating = driverRating,
                notes = notes
            )
        )
        Result.success(Unit)
    }

    suspend fun requestDriverPayout(driverId: Long, amount: Double) = withContext(Dispatchers.IO) {
        dao.insertPayoutRequest(
            DriverPayoutRequestEntity(
                driverId = driverId,
                amount = amount
            )
        )
    }

    fun getDriverPayoutRequests(driverId: Long): Flow<List<DriverPayoutRequestEntity>> =
        dao.getPayoutRequestsForDriver(driverId)

    // --- Order Operations ---

    suspend fun placeOrder(
        customerId: Long,
        customerName: String,
        customerPhone: String,
        partner: PartnerEntity,
        items: List<Triple<ProductEntity, Int, String>>, // product, quantity, selected options
        deliveryAddress: String,
        customerNotes: String,
        paymentMethod: PaymentMethod,
        appliedDiscount: Double = 0.0,
        transferReceiptNote: String = "",
        transferReceiptUri: String = ""
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (items.isEmpty()) return@withContext Result.failure(IllegalArgumentException("السلة فارغة"))

            val subtotal = items.sumOf { it.first.price * it.second }
            val total = (subtotal + partner.deliveryFee - appliedDiscount).coerceAtLeast(0.0)
            val orderNum = "#FS-${(1000..9999).random()}"

            val initialOrderStatus = when (partner.approvalWorkflow) {
                ApprovalWorkflow.AUTOMATIC -> OrderStatus.APPROVED
                else -> OrderStatus.PENDING_REVIEW
            }

            val paymentStatus = if (paymentMethod == PaymentMethod.BANK_TRANSFER) {
                PaymentStatus.PENDING_VERIFICATION
            } else {
                PaymentStatus.PENDING
            }

            val order = OrderEntity(
                orderNumber = orderNum,
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                partnerId = partner.id,
                partnerName = partner.name,
                partnerType = partner.type,
                orderStatus = initialOrderStatus,
                deliveryStatus = DeliveryStatus.WAITING_FOR_DRIVER,
                paymentMethod = paymentMethod,
                paymentStatus = paymentStatus,
                transferReceiptNote = transferReceiptNote,
                transferReceiptUri = transferReceiptUri,
                deliveryAddress = deliveryAddress,
                subtotal = subtotal,
                deliveryFee = partner.deliveryFee,
                discount = appliedDiscount,
                total = total,
                customerNotes = customerNotes,
                estimatedPrepMinutes = 20
            )

            val orderId = dao.insertOrder(order)

            val orderItems = items.map { (prod, qty, optionsSummary) ->
                OrderItemEntity(
                    orderId = orderId,
                    productId = prod.id,
                    productName = prod.name,
                    quantity = qty,
                    unitPrice = prod.price,
                    optionsSummary = optionsSummary,
                    totalPrice = prod.price * qty
                )
            }
            dao.insertOrderItems(orderItems)

            // Log activity
            dao.insertActivityLog(
                OrderActivityLogEntity(
                    orderId = orderId,
                    timeFormatted = timeFormat.format(Date()),
                    actor = customerName,
                    actorRole = "العميل",
                    action = "ORDER_CREATED",
                    oldValue = "NONE",
                    newValue = initialOrderStatus.name,
                    reason = "تم إنشاء الطلب واختيار الدفع: ${paymentMethod.titleArabic}"
                )
            )

            // Notify only the authenticated account linked to this partner.
            dao.getUserByAssociatedPartnerId(partner.id)?.let { partnerUser ->
                dao.insertNotification(
                    NotificationEntity(
                        targetUserId = partnerUser.id,
                        targetRole = UserRole.PARTNER,
                        category = NotificationCategory.ORDER,
                        title = "طلب جديد $orderNum",
                        message = "طلب جديد من $customerName بإجمالي ${total.toInt()} ج.م",
                        relatedOrderId = orderId
                    )
                )
            }
            dao.insertNotification(
                NotificationEntity(
                    targetRole = UserRole.ADMIN,
                    category = NotificationCategory.ORDER,
                    title = "طلب جديد في النظام $orderNum",
                    message = "تم إنشاء طلب جديد لدى ${partner.name}",
                    relatedOrderId = orderId
                )
            )

            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(
        orderId: Long,
        newStatus: OrderStatus,
        actor: String,
        actorRole: String,
        reason: String,
        isForceOverride: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val current = dao.getOrderById(orderId) ?: return@withContext Result.failure(Exception("الطلب غير موجود"))

        // Terminal protection: Cannot transition if already in a terminal state
        if (current.orderStatus in setOf(OrderStatus.DELIVERED, OrderStatus.REJECTED, OrderStatus.CANCELLED)) {
            return@withContext Result.failure(
                IllegalStateException("لا يمكن تعديل حالة طلب منتهي أو ملغي أو مرفوض")
            )
        }

        if (!isForceOverride && !OrderEngine.isValidOrderStatusTransition(current.orderStatus, newStatus)) {
            return@withContext Result.failure(
                IllegalStateException("غير مسموح بالانتقال من ${current.orderStatus.titleArabic} إلى ${newStatus.titleArabic}")
            )
        }

        // If order is cancelled or rejected, release assigned driver if any
        if (newStatus in setOf(OrderStatus.CANCELLED, OrderStatus.REJECTED)) {
            current.driverId?.let { dId ->
                val assignedDriver = dao.getDriverById(dId)
                if (assignedDriver != null) {
                    dao.updateDriver(
                        assignedDriver.copy(
                            status = DriverStatus.AVAILABLE,
                            currentOrderId = null
                        )
                    )
                }
            }
        }

        val updated = current.copy(
            orderStatus = newStatus,
            deliveryStatus = if (newStatus in setOf(OrderStatus.CANCELLED, OrderStatus.REJECTED)) DeliveryStatus.NOT_REQUIRED else current.deliveryStatus,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateOrder(updated)

        dao.insertActivityLog(
            OrderActivityLogEntity(
                orderId = orderId,
                timeFormatted = timeFormat.format(Date()),
                actor = actor,
                actorRole = actorRole,
                action = "ORDER_STATUS_CHANGED",
                oldValue = current.orderStatus.name,
                newValue = newStatus.name,
                reason = reason.ifBlank { "تحديث مرحلة الطلب إلى ${newStatus.titleArabic}" }
            )
        )

        // Customer notification
        dao.insertNotification(
            NotificationEntity(
                targetUserId = current.customerId,
                targetRole = UserRole.CUSTOMER,
                category = NotificationCategory.ORDER,
                title = "تحديث لطلبك ${current.orderNumber}",
                message = "${newStatus.titleArabic}: ${OrderEngine.getHumanizedTrackingMessage(newStatus, updated.deliveryStatus)}",
                relatedOrderId = orderId
            )
        )

        Result.success(Unit)
    }

    suspend fun updateDeliveryStatus(
        orderId: Long,
        newStatus: DeliveryStatus,
        driverId: Long? = null,
        driverName: String? = null,
        actor: String,
        actorRole: String,
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val current = dao.getOrderById(orderId) ?: return@withContext Result.failure(Exception("الطلب غير موجود"))

        // Terminal protection: Cannot update delivery status for cancelled/rejected or delivered orders
        if (current.orderStatus in setOf(OrderStatus.CANCELLED, OrderStatus.REJECTED)) {
            return@withContext Result.failure(
                IllegalStateException("لا يمكن تحديث حالة توصيل لطلب ملغي أو مرفوض")
            )
        }
        if (current.deliveryStatus in setOf(DeliveryStatus.DELIVERED, DeliveryStatus.NOT_REQUIRED)) {
            return@withContext Result.failure(
                IllegalStateException("حالة التوصيل الحالية منتهية ولا يمكن تعديلها")
            )
        }

        // Validate delivery status transition rules
        if (!OrderEngine.isValidDeliveryStatusTransition(current.deliveryStatus, newStatus)) {
            return@withContext Result.failure(
                IllegalStateException("غير مسموح بانتقال التوصيل من ${current.deliveryStatus.titleArabic} إلى ${newStatus.titleArabic}")
            )
        }

        var updated = current.copy(
            deliveryStatus = newStatus,
            driverId = driverId ?: current.driverId,
            driverName = driverName ?: current.driverName,
            updatedAt = System.currentTimeMillis()
        )

        // Keep orderStatus and paymentStatus in sync if delivered or picked up
        if (newStatus in setOf(DeliveryStatus.PICKED_UP, DeliveryStatus.OUT_FOR_DELIVERY) && current.orderStatus in setOf(OrderStatus.APPROVED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP)) {
            updated = updated.copy(orderStatus = OrderStatus.PICKED_UP)
        } else if (newStatus == DeliveryStatus.DELIVERED) {
            updated = updated.copy(
                orderStatus = OrderStatus.DELIVERED,
                paymentStatus = if (current.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) PaymentStatus.VERIFIED else current.paymentStatus
            )
            // Credit driver earnings and release driver
            val dId = driverId ?: current.driverId
            if (dId != null) {
                val driver = dao.getDriverById(dId)
                if (driver != null) {
                    dao.updateDriver(
                        driver.copy(
                            status = DriverStatus.AVAILABLE,
                            todayEarnings = driver.todayEarnings + current.deliveryFee,
                            totalEarnings = driver.totalEarnings + current.deliveryFee,
                            completedOrdersCount = driver.completedOrdersCount + 1,
                            currentOrderId = null
                        )
                    )
                }
            }
        }

        dao.updateOrder(updated)

        dao.insertActivityLog(
            OrderActivityLogEntity(
                orderId = orderId,
                timeFormatted = timeFormat.format(Date()),
                actor = actor,
                actorRole = actorRole,
                action = "DELIVERY_STATUS_CHANGED",
                oldValue = current.deliveryStatus.name,
                newValue = newStatus.name,
                reason = reason.ifBlank { "تحديث حالة التوصيل: ${newStatus.titleArabic}" }
            )
        )

        dao.insertNotification(
            NotificationEntity(
                targetUserId = current.customerId,
                targetRole = UserRole.CUSTOMER,
                category = NotificationCategory.ORDER,
                title = "تحديث توصيل طلبك ${current.orderNumber}",
                message = "${newStatus.titleArabic}",
                relatedOrderId = orderId
            )
        )

        Result.success(Unit)
    }

    /**
     * Sequential dispatch foundation: the first eligible driver receives an offer.
     * Final assignment happens only after that driver accepts.
     */
    suspend fun offerOrderToNextDriver(orderId: Long, shiftName: String): Result<DriverProfileEntity> =
        withContext(Dispatchers.IO) {
            val order = dao.getOrderById(orderId)
                ?: return@withContext Result.failure(Exception("الطلب غير موجود"))
            val excluded = dao.getSequentiallyProcessedDriverIds(orderId).toSet()
            val driver = getNextEligibleDriver(shiftName, excluded)
                ?: return@withContext Result.failure(NoEligibleDriverException("لا يوجد مندوب مؤهل جديد حاليًا"))
            val timeout = (dao.getSettings().firstOrNull()?.driverOfferTimeoutSeconds ?: 30).coerceAtLeast(5)
            recordDriverDispatchEvent(orderId, driver.id, "OFFERED", "Sequential queue", System.currentTimeMillis() + timeout * 1000L)
            dao.getUserByAssociatedDriverId(driver.id)?.let { user ->
                dao.insertNotification(NotificationEntity(
                    targetUserId = user.id,
                    targetRole = UserRole.DRIVER,
                    category = NotificationCategory.ORDER,
                    title = "عرض طلب جديد",
                    message = "لديك طلب جديد " + order.orderNumber + ". يرجى القبول أو الرفض.",
                    relatedOrderId = orderId
                ))
            }
            Result.success(driver)
        }

    private suspend fun rotateQueueAfterAcceptance(shiftName: String, acceptedDriverId: Long) {
        val queue = dao.getActiveDriverShiftAssignmentsSnapshot(shiftName)
        val accepted = queue.firstOrNull { it.driverId == acceptedDriverId } ?: return
        val rotated = queue.filter { it.driverId != acceptedDriverId } + accepted
        val now = System.currentTimeMillis()
        rotated.forEachIndexed { index, item ->
            dao.updateDriverShiftAssignment(item.copy(queuePosition = index + 1, updatedAt = now))
        }
    }

    private suspend fun updateDriverPerformance(driverId: Long, eventType: String) {
        val current = dao.getDriverPerformance(driverId) ?: DriverPerformanceEntity(driverId = driverId)
        val next = when (eventType) {
            "ACCEPTED" -> current.copy(
                totalAccepted = current.totalAccepted + 1,
                consecutiveRejects = 0,
                consecutiveTimeouts = 0,
                updatedAt = System.currentTimeMillis()
            )
            "REJECTED" -> current.copy(
                totalRejected = current.totalRejected + 1,
                consecutiveRejects = current.consecutiveRejects + 1,
                consecutiveTimeouts = 0,
                updatedAt = System.currentTimeMillis()
            )
            "TIMEOUT" -> current.copy(
                totalTimeouts = current.totalTimeouts + 1,
                consecutiveTimeouts = current.consecutiveTimeouts + 1,
                consecutiveRejects = 0,
                updatedAt = System.currentTimeMillis()
            )
            else -> current
        }
        dao.upsertDriverPerformance(next)
    }

    private suspend fun applyAutomaticPenaltyIfNeeded(orderId: Long, driverId: Long) {
        val settings = dao.getSettings().firstOrNull() ?: AppSettingsEntity()
        val performance = dao.getDriverPerformance(driverId) ?: return
        val rejectLimit = settings.maxRejectsBeforeBreak.coerceAtLeast(1)
        val timeoutLimit = settings.maxTimeoutsBeforeBreak.coerceAtLeast(1)
        val exceededRejects = performance.consecutiveRejects >= rejectLimit
        val exceededTimeouts = performance.consecutiveTimeouts >= timeoutLimit
        if (!exceededRejects && !exceededTimeouts) return

        putDriverOnForcedBreak(driverId, settings.automaticPenaltyBreakMinutes)
        val reason = if (exceededRejects) {
            "تجاوز حد الرفضات المتتالية"
        } else {
            "تجاوز حد انتهاء مهلة العروض المتتالية"
        }
        dao.upsertDriverPerformance(
            performance.copy(
                consecutiveRejects = 0,
                consecutiveTimeouts = 0,
                lastPenaltyAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        recordDriverDispatchEvent(orderId, driverId, "PENALTY_BREAK", reason)
    }

    private suspend fun isDriverEligibleForDispatch(
        driverId: Long,
        shiftName: String,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        val assignment = dao.getDriverShiftAssignment(driverId) ?: return false
        if (!assignment.active || assignment.shiftName != shiftName) return false
        if (assignment.forcedBreakUntil > now) return false
        val driver = dao.getDriverById(driverId) ?: return false
        return driver.status == DriverStatus.AVAILABLE && driver.currentOrderId == null
    }

    private suspend fun validateActiveOffer(
        orderId: Long,
        driverId: Long,
        now: Long = System.currentTimeMillis()
    ): Result<DriverDispatchEventEntity> {
        val latestDriverEvent = dao.getLatestDriverEvent(orderId, driverId)
            ?: return Result.failure(IllegalStateException("لا يوجد عرض نشط لهذا المندوب"))
        if (latestDriverEvent.eventType in setOf("ACCEPTED", "REJECTED", "TIMEOUT")) {
            return Result.failure(IllegalStateException("تم التعامل مع هذا العرض بالفعل"))
        }
        val offer = dao.getLatestDriverOffer(orderId, driverId)
            ?: return Result.failure(IllegalStateException("لا يوجد عرض نشط لهذا المندوب"))
        if (offer.id != latestDriverEvent.id) {
            return Result.failure(IllegalStateException("العرض لم يعد نشطًا"))
        }
        if (offer.expiresAt <= 0L || now > offer.expiresAt) {
            return Result.failure(IllegalStateException("انتهت مهلة قبول الطلب"))
        }
        val latestOrderOffer = dao.getLatestOrderOffer(orderId)
            ?: return Result.failure(IllegalStateException("لا يوجد عرض نشط للطلب"))
        if (offer.id != latestOrderOffer.id && offer.eventType == "OFFERED") {
            return Result.failure(IllegalStateException("تم تجاوز هذا العرض بعرض أحدث"))
        }
        return Result.success(offer)
    }

    suspend fun acceptDriverOffer(orderId: Long, driverId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val order = dao.getOrderById(orderId)
            ?: return@withContext Result.failure(Exception("الطلب غير موجود"))
        if (order.driverId != null) {
            return@withContext Result.failure(IllegalStateException("تم قبول الطلب بواسطة مندوب آخر"))
        }
        val assignment = dao.getDriverShiftAssignment(driverId)
            ?: return@withContext Result.failure(IllegalStateException("المندوب غير مسجل في شيفت"))
        val offerValidation = validateActiveOffer(orderId, driverId)
        if (offerValidation.isFailure) return@withContext Result.failure(offerValidation.exceptionOrNull()!!)
        if (!isDriverEligibleForDispatch(driverId, assignment.shiftName)) {
            return@withContext Result.failure(IllegalStateException("المندوب غير مؤهل لقبول الطلب حاليًا"))
        }
        val driver = dao.getDriverById(driverId)
            ?: return@withContext Result.failure(Exception("المندوب غير موجود"))

        val claimed = dao.claimUnassignedOrder(
            orderId = orderId,
            driverId = driver.id,
            driverName = driver.name,
            driverPhone = driver.phone,
            updatedAt = System.currentTimeMillis()
        )
        if (claimed != 1) {
            return@withContext Result.failure(IllegalStateException("تم قبول الطلب بواسطة مندوب آخر أو لم يعد متاحًا"))
        }

        dao.updateDriver(driver.copy(status = DriverStatus.BUSY, currentOrderId = orderId))
        recordDriverDispatchEvent(orderId, driverId, "ACCEPTED")
        updateDriverPerformance(driverId, "ACCEPTED")
        rotateQueueAfterAcceptance(assignment.shiftName, driverId)
        recordDriverDispatchEvent(orderId, driverId, "QUEUE_ROTATED", "انتقل المندوب إلى نهاية الدور")

        dao.insertActivityLog(
            OrderActivityLogEntity(
                orderId = orderId,
                timeFormatted = timeFormat.format(Date()),
                actor = driver.name,
                actorRole = "المندوب",
                action = "DRIVER_ACCEPTED_OFFER",
                oldValue = "WAITING_FOR_DRIVER",
                newValue = "DRIVER_ASSIGNED",
                reason = "قبل المندوب العرض وتم تعيين الطلب ذريًا"
            )
        )
        Result.success(Unit)
    }

    suspend fun rejectDriverOffer(orderId: Long, driverId: Long, reason: String, shiftName: String): Result<DriverProfileEntity> =
        withContext(Dispatchers.IO) {
            if (reason.isBlank()) return@withContext Result.failure(IllegalArgumentException("سبب الرفض مطلوب"))
            val offerValidation = validateActiveOffer(orderId, driverId)
            if (offerValidation.isFailure) return@withContext Result.failure(offerValidation.exceptionOrNull()!!)
            if (!isDriverEligibleForDispatch(driverId, shiftName)) {
                return@withContext Result.failure(IllegalStateException("المندوب غير مؤهل للتعامل مع العرض"))
            }
            val offerType = offerValidation.getOrThrow().eventType
            recordDriverDispatchEvent(orderId, driverId, "REJECTED", reason)
            updateDriverPerformance(driverId, "REJECTED")
            applyAutomaticPenaltyIfNeeded(orderId, driverId)
            if (offerType == "OFFERED") {
                offerOrderToNextDriver(orderId, shiftName)
            } else {
                Result.success(dao.getDriverById(driverId) ?: return@withContext Result.failure(IllegalStateException("المندوب غير موجود")))
            }
        }

    suspend fun dispatchOrder(orderId: Long, shiftName: String): Result<String> = withContext(Dispatchers.IO) {
        val order = dao.getOrderById(orderId)
            ?: return@withContext Result.failure(IllegalArgumentException("الطلب غير موجود"))
        if (order.driverId != null || order.deliveryStatus != DeliveryStatus.WAITING_FOR_DRIVER) {
            return@withContext Result.failure(IllegalStateException("الطلب غير متاح للتوزيع"))
        }
        when (order.dispatchMode) {
            DispatchMode.SEQUENTIAL -> {
                val result = offerOrderToNextDriver(orderId, shiftName)
                result.map { "SEQUENTIAL" }
            }
            DispatchMode.BROADCAST -> {
                val result = broadcastOrderToEligibleDrivers(orderId, shiftName)
                result.map { "BROADCAST" }
            }
            DispatchMode.HYBRID -> {
                val result = offerOrderToNextDriver(orderId, shiftName)
                if (result.isSuccess) {
                    Result.success("SEQUENTIAL")
                } else {
                    val sequentialError = result.exceptionOrNull()
                    if (sequentialError !is NoEligibleDriverException) {
                        Result.failure(sequentialError ?: IllegalStateException("فشل التوزيع بالتتابع"))
                    } else {
                        val broadcast = broadcastOrderToEligibleDrivers(orderId, shiftName)
                        if (broadcast.isSuccess) Result.success("BROADCAST")
                        else Result.failure(
                            broadcast.exceptionOrNull()
                                ?: sequentialError
                        )
                    }
                }
            }
        }
    }

    suspend fun broadcastOrderToEligibleDrivers(orderId: Long, shiftName: String): Result<Int> =
        withContext(Dispatchers.IO) {
            val order = dao.getOrderById(orderId)
                ?: return@withContext Result.failure(Exception("الطلب غير موجود"))
            if (order.driverId != null || order.deliveryStatus != DeliveryStatus.WAITING_FOR_DRIVER) {
                return@withContext Result.failure(IllegalStateException("الطلب غير متاح للتوزيع"))
            }
            val eligible = getEligibleDrivers(shiftName)
            if (eligible.isEmpty()) return@withContext Result.failure(NoEligibleDriverException("لا يوجد مندوب متاح"))
            val timeout = (dao.getSettings().firstOrNull()?.driverOfferTimeoutSeconds ?: 30).coerceAtLeast(5)
            val expiresAt = System.currentTimeMillis() + timeout * 1000L
            eligible.forEach { driver ->
                recordDriverDispatchEvent(orderId, driver.id, "BROADCAST_OFFER", "Broadcast dispatch", expiresAt)
                dao.getUserByAssociatedDriverId(driver.id)?.let { user ->
                    dao.insertNotification(NotificationEntity(
                        targetUserId = user.id,
                        targetRole = UserRole.DRIVER,
                        category = NotificationCategory.ORDER,
                        title = "طلب متاح للجميع",
                        message = "الطلب " + order.orderNumber + " متاح، أول قبول صحيح يحصل على التعيين.",
                        relatedOrderId = orderId
                    ))
                }
            }
            Result.success(eligible.size)
        }

    private suspend fun getEligibleDrivers(shiftName: String): List<DriverProfileEntity> {
        val now = System.currentTimeMillis()
        normalizeExpiredDriverBreaks(now)
        val assignments = dao.getActiveDriverShiftAssignmentsSnapshot(shiftName)
        return assignments.sortedBy { it.queuePosition }.mapNotNull { assignment ->
            if (assignment.forcedBreakUntil > now) null
            else dao.getDriverById(assignment.driverId)?.takeIf {
                it.status == DriverStatus.AVAILABLE && it.currentOrderId == null
            }
        }
    }

    suspend fun assignDriverToOrder(
        orderId: Long,
        driver: DriverProfileEntity,
        actor: String,
        actorRole: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (driver.status != DriverStatus.AVAILABLE || driver.currentOrderId != null) {
            return@withContext Result.failure(IllegalStateException("المندوب غير متاح"))
        }
        val claimed = dao.claimUnassignedOrder(
            orderId, driver.id, driver.name, driver.phone, System.currentTimeMillis()
        )
        if (claimed != 1) {
            return@withContext Result.failure(IllegalStateException("تعذر تعيين المندوب: الطلب لم يعد متاحًا"))
        }
        dao.updateDriver(driver.copy(status = DriverStatus.BUSY, currentOrderId = orderId))
        Result.success(Unit)
    }

    suspend fun reviewBankTransfer(
        orderId: Long,
        isApproved: Boolean,
        reason: String,
        actor: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val current = dao.getOrderById(orderId) ?: return@withContext Result.failure(Exception("الطلب غير موجود"))

        val newPaymentStatus = if (isApproved) PaymentStatus.VERIFIED else PaymentStatus.REJECTED
        val updated = current.copy(
            paymentStatus = newPaymentStatus,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateOrder(updated)

        dao.insertActivityLog(
            OrderActivityLogEntity(
                orderId = orderId,
                timeFormatted = timeFormat.format(Date()),
                actor = actor,
                actorRole = "الإدارة",
                action = "PAYMENT_REVIEW",
                oldValue = current.paymentStatus.name,
                newValue = newPaymentStatus.name,
                reason = reason
            )
        )

        dao.insertNotification(
            NotificationEntity(
                targetUserId = current.customerId,
                targetRole = UserRole.CUSTOMER,
                category = NotificationCategory.PAYMENT,
                title = if (isApproved) "تم اعتماد تحويلك المالي ✅" else "تم رفض إيصال التحويل ❌",
                message = if (isApproved) "تم تأكيد سداد طلبك ${current.orderNumber}" else "سبب الرفض: $reason",
                relatedOrderId = orderId
            )
        )

        Result.success(Unit)
    }

    // --- Onboarding Management ---
    suspend fun saveOnboardingPage(page: OnboardingPageEntity) = withContext(Dispatchers.IO) {
        if (page.id == 0L) {
            dao.insertOnboardingPages(listOf(page))
        } else {
            dao.updateOnboardingPage(page)
        }
    }

    suspend fun deleteOnboardingPage(page: OnboardingPageEntity) = withContext(Dispatchers.IO) {
        dao.deleteOnboardingPage(page)
    }

    suspend fun setOnboardingEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        val settings = dao.getSettings().firstOrNull() ?: AppSettingsEntity()
        dao.insertSettings(settings.copy(onboardingEnabled = enabled))
    }

    suspend fun saveAppSettings(settings: AppSettingsEntity) = withContext(Dispatchers.IO) {
        dao.insertSettings(settings.copy(id = 1))
    }

    // --- Home Builder Management ---
    suspend fun saveHomeSection(section: HomeSectionEntity) = withContext(Dispatchers.IO) {
        if (section.id == 0L) {
            dao.insertHomeSections(listOf(section))
        } else {
            dao.updateHomeSection(section)
        }
    }

    suspend fun deleteHomeSection(section: HomeSectionEntity) = withContext(Dispatchers.IO) {
        dao.deleteHomeSection(section)
    }

    // --- Partner & Product Operations ---
    suspend fun setPartnerOpenStatus(partnerId: Long, isOpen: Boolean) = withContext(Dispatchers.IO) {
        val partner = dao.getPartnerById(partnerId) ?: return@withContext
        dao.updatePartner(partner.copy(isOpen = isOpen))
    }

    suspend fun saveProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        if (product.id == 0L) {
            dao.insertProduct(product)
        } else {
            dao.updateProduct(product)
        }
    }

    suspend fun updateProductStatus(productId: Long, status: ProductStatus) = withContext(Dispatchers.IO) {
        val products = dao.getAllProducts().firstOrNull() ?: return@withContext
        val product = products.find { it.id == productId } ?: return@withContext
        dao.updateProduct(product.copy(status = status))
    }

    // --- Driver Profile Operations ---
    suspend fun addDriver(driver: DriverProfileEntity): Long = withContext(Dispatchers.IO) {
        dao.insertDriver(driver)
    }

    /**
     * Admin-controlled driver account provisioning.
     * A driver cannot self-create an application account; the admin creates
     * the driver profile and optionally provisions the linked login identity.
     */
    suspend fun provisionDriverAccount(
        driver: DriverProfileEntity,
        accountName: String,
        email: String,
        passwordHash: String,
        passwordSalt: String
    ): Long = withContext(Dispatchers.IO) {
        val driverId = if (driver.id == 0L) dao.insertDriver(driver) else {
            dao.updateDriver(driver)
            driver.id
        }
        val existing = dao.getUserByAssociatedDriverId(driverId)
        val user = UserEntity(
            id = existing?.id ?: 0,
            name = accountName,
            phone = driver.phone,
            email = email,
            passwordHash = passwordHash,
            passwordSalt = passwordSalt,
            role = UserRole.DRIVER,
            associatedDriverId = driverId,
            isActive = existing?.isActive ?: false,
            activationStatus = existing?.activationStatus ?: "PENDING_APPROVAL",
            activationReason = existing?.activationReason ?: ""
        )
        dao.insertUser(user)
        driverId
    }

    suspend fun updateDriverProfile(driver: DriverProfileEntity) = withContext(Dispatchers.IO) {
        dao.updateDriver(driver)
    }

    suspend fun deleteDriverProfile(driver: DriverProfileEntity) = withContext(Dispatchers.IO) {
        dao.deleteDriver(driver)
    }

    suspend fun approveDriverAccount(userId: Long) = withContext(Dispatchers.IO) {
        dao.updateUserActivation(userId, true, "ACTIVE", "")
    }

    suspend fun rejectDriverAccount(userId: Long, reason: String) = withContext(Dispatchers.IO) {
        dao.updateUserActivation(userId, false, "REJECTED", reason)
    }

    suspend fun suspendDriverAccount(userId: Long, reason: String) = withContext(Dispatchers.IO) {
        val user = dao.getUserById(userId) ?: return@withContext
        dao.updateUserActivation(userId, false, "SUSPENDED", reason)
        user.associatedDriverId?.let { driverId ->
            dao.getDriverById(driverId)?.let { driver ->
                dao.updateDriver(driver.copy(status = DriverStatus.OFFLINE))
            }
        }
    }

    suspend fun setAccountActivation(userId: Long, active: Boolean, reason: String = "") = withContext(Dispatchers.IO) {
        if (active) approveDriverAccount(userId) else suspendDriverAccount(userId, reason)
    }

    suspend fun setDriverAvailability(driverId: Long, status: DriverStatus): Result<Unit> = withContext(Dispatchers.IO) {
        val driver = dao.getDriverById(driverId)
            ?: return@withContext Result.failure(IllegalArgumentException("المندوب غير موجود"))
        val user = dao.getUserByAssociatedDriverId(driverId)
        if (!isActiveDriverAccount(user)) {
            return@withContext Result.failure(IllegalStateException("حساب المندوب غير مفعل"))
        }
        if (status !in setOf(DriverStatus.OFFLINE, DriverStatus.AVAILABLE)) {
            return@withContext Result.failure(IllegalArgumentException("لا يمكن تغيير حالة العمل إلى هذه الحالة يدويًا"))
        }
        if (driver.status == DriverStatus.BUSY && status == DriverStatus.OFFLINE) {
            return@withContext Result.failure(IllegalStateException("لا يمكن فصل المندوب أثناء وجود طلب نشط"))
        }
        dao.updateDriver(driver.copy(status = status))
        Result.success(Unit)
    }

    // --- Coupon Operations ---
    suspend fun validateCoupon(code: String, subtotal: Double): Result<Pair<CouponEntity, Double>> = withContext(Dispatchers.IO) {
        val coupon = dao.getCouponByCode(code.trim().uppercase())
            ?: return@withContext Result.failure(Exception("الكود غير صالح أو منتهي الصلاحية"))

        if (subtotal < coupon.minOrder) {
            return@withContext Result.failure(Exception("الحد الأدنى للطلب لاستخدام الكوبون هو ${coupon.minOrder.toInt()} ج.م"))
        }

        val discountAmount = (subtotal * coupon.discountPercent / 100.0)
        Result.success(Pair(coupon, discountAmount))
    }

    suspend fun saveCoupon(coupon: CouponEntity) = withContext(Dispatchers.IO) {
        if (coupon.id == 0L) dao.insertCoupon(coupon) else dao.updateCoupon(coupon)
    }

    // --- Support Tickets ---
    suspend fun createTicket(ticket: SupportTicketEntity): Long = withContext(Dispatchers.IO) {
        val ticketId = dao.insertTicket(ticket)
        dao.insertNotification(
            NotificationEntity(
                targetRole = UserRole.ADMIN,
                category = NotificationCategory.ACTION_REQUIRED,
                title = "تذكرة دعم جديدة #${ticket.ticketNumber}",
                message = "${ticket.customerName}: ${ticket.subject}"
            )
        )
        ticketId
    }

    suspend fun updateTicketStatus(ticketId: Long, status: TicketStatus) = withContext(Dispatchers.IO) {
        val tickets = dao.getAllTickets().firstOrNull() ?: return@withContext
        val ticket = tickets.find { it.id == ticketId } ?: return@withContext
        dao.updateTicket(ticket.copy(status = status))
    }

    // --- Saved Addresses ---
    suspend fun addAddress(address: CustomerAddressEntity) = withContext(Dispatchers.IO) {
        dao.insertAddress(address)
    }

    suspend fun deleteAddress(address: CustomerAddressEntity) = withContext(Dispatchers.IO) {
        dao.deleteAddress(address)
    }

    // --- Notifications ---
    suspend fun markAllNotificationsRead(role: UserRole) = withContext(Dispatchers.IO) {
        dao.markAllNotificationsAsRead(role)
    }

    suspend fun markAllNotificationsReadForUser(role: UserRole, userId: Long) = withContext(Dispatchers.IO) {
        dao.markAllNotificationsAsReadForUser(role, userId)
    }

    // --- Initial Seeding ---
    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val partners = dao.getAllPartners().firstOrNull()
        if (!partners.isNullOrEmpty()) return@withContext

        // 1. App Settings
        dao.insertSettings(
            AppSettingsEntity(
                id = 1,
                onboardingEnabled = true,
                defaultApproval = ApprovalWorkflow.PARTNER,
                defaultDispatchMode = DispatchMode.SEQUENTIAL,
                dispatchTriggerTiming = "عند بدء التجهيز"
            )
        )

        // Sample records need a real owner, but this seed-only account must also satisfy
        // the credential schema. These values are intentionally non-empty and are not
        // presented as application login credentials.
        val sampleCustomerId = dao.insertUser(
            UserEntity(
                name = "عمرو إبراهيم",
                phone = "01011122233",
                passwordHash = "6de6eb033759a8a9a255d64386afc169c03ce46f1156907b7e7043c6b98c522d",
                passwordSalt = "falsaree_seed_v1"
            )
        )

        // 2. Onboarding Pages (Fully admin-configurable)
        dao.insertOnboardingPages(
            listOf(
                OnboardingPageEntity(
                    sortOrder = 1,
                    title = "اطلب كل اللي تحتاجه",
                    description = "من مطاعم وصيدليات وكافيهات ومتاجر بقالة، كل احتياجاتك في مكان واحد وبأعلى سرعة.",
                    iconEmoji = "⚡",
                    active = true
                ),
                OnboardingPageEntity(
                    sortOrder = 2,
                    title = "اختار عنوانك بسهولة",
                    description = "حدد موقع بيتك أو شغلك وخلي طلبك يوصلك لباب البيت بأمان وبدون أي عناء.",
                    iconEmoji = "📍",
                    active = true
                ),
                OnboardingPageEntity(
                    sortOrder = 3,
                    title = "طلبك في الطريق",
                    description = "تابع مسار مندوبك وحالة طلبك لحظة بلحظة مع إشعارات مباشرة وشفافة.",
                    iconEmoji = "🛵",
                    active = true
                ),
                OnboardingPageEntity(
                    sortOrder = 4,
                    title = "فالسريع",
                    description = "اطلب.. يوصلك فالسريع ⚡\nأسرع خدمة توصيل في مصر بضغطة واحدة.",
                    iconEmoji = "🔥",
                    active = true
                )
            )
        )

        // 3. Home Builder Sections
        dao.insertHomeSections(
            listOf(
                HomeSectionEntity(
                    title = "العروض والبانرات",
                    type = HomeSectionType.BANNERS,
                    sortOrder = 1,
                    limitCount = 5,
                    active = true
                ),
                HomeSectionEntity(
                    title = "التصنيفات الرئيسية",
                    type = HomeSectionType.CATEGORIES,
                    sortOrder = 2,
                    limitCount = 4,
                    active = true
                ),
                HomeSectionEntity(
                    title = "أقوى العروض الحصرية",
                    type = HomeSectionType.OFFERS,
                    sortOrder = 3,
                    limitCount = 6,
                    active = true
                ),
                HomeSectionEntity(
                    title = "أماكن قريبة منك",
                    type = HomeSectionType.NEARBY_PARTNERS,
                    sortOrder = 4,
                    limitCount = 8,
                    active = true
                ),
                HomeSectionEntity(
                    title = "الأعلى تقييمًا ⭐",
                    type = HomeSectionType.TOP_RATED,
                    sortOrder = 5,
                    limitCount = 6,
                    active = true
                ),
                HomeSectionEntity(
                    title = "أكثر الأصناف طلبًا 🔥",
                    type = HomeSectionType.FEATURED_PRODUCTS,
                    sortOrder = 6,
                    limitCount = 8,
                    active = true
                )
            )
        )

        // 4. Partners
        val p1 = PartnerEntity(
            name = "بيتزا تايم - المهندسين",
            type = PartnerType.RESTAURANT,
            rating = 4.8,
            isOpen = true,
            distanceKm = 1.8,
            deliveryTimeMinutes = 25,
            deliveryFee = 25.0,
            address = "شارع جامعة الدول، المهندسين، الجيزة",
            logoEmoji = "🍕",
            approvalWorkflow = ApprovalWorkflow.PARTNER
        )
        val p2 = PartnerEntity(
            name = "كرم الشام - الدقي",
            type = PartnerType.RESTAURANT,
            rating = 4.9,
            isOpen = true,
            distanceKm = 2.4,
            deliveryTimeMinutes = 30,
            deliveryFee = 20.0,
            address = "ميدان المساحة، الدقي، الجيزة",
            logoEmoji = "🌯",
            approvalWorkflow = ApprovalWorkflow.AUTOMATIC
        )
        val p3 = PartnerEntity(
            name = "صيدلية العزبي - الزمالك",
            type = PartnerType.PHARMACY,
            rating = 4.9,
            isOpen = true,
            distanceKm = 1.2,
            deliveryTimeMinutes = 20,
            deliveryFee = 15.0,
            address = "شارع 26 يوليو، الزمالك، القاهرة",
            logoEmoji = "💊",
            approvalWorkflow = ApprovalWorkflow.PARTNER
        )
        val p4 = PartnerEntity(
            name = "صيدلية سيف - المعادي",
            type = PartnerType.PHARMACY,
            rating = 4.7,
            isOpen = true,
            distanceKm = 3.5,
            deliveryTimeMinutes = 35,
            deliveryFee = 25.0,
            address = "شارع النصر، المعادي، القاهرة",
            logoEmoji = "🩺",
            approvalWorkflow = ApprovalWorkflow.ADMIN
        )
        val p5 = PartnerEntity(
            name = "كافيين لاب - الزمالك",
            type = PartnerType.CAFE,
            rating = 4.8,
            isOpen = true,
            distanceKm = 1.5,
            deliveryTimeMinutes = 20,
            deliveryFee = 18.0,
            address = "شارع حسن صبري، الزمالك، القاهرة",
            logoEmoji = "☕",
            approvalWorkflow = ApprovalWorkflow.AUTOMATIC
        )
        val p6 = PartnerEntity(
            name = "سوبرماركت سعودي - الشيخ زايد",
            type = PartnerType.STORE,
            rating = 4.9,
            isOpen = true,
            distanceKm = 4.0,
            deliveryTimeMinutes = 40,
            deliveryFee = 30.0,
            address = "بالم هيلز، الشيخ زايد، 6 أكتوبر",
            logoEmoji = "🛒",
            approvalWorkflow = ApprovalWorkflow.PARTNER
        )

        dao.insertPartners(listOf(p1, p2, p3, p4, p5, p6))

        // Get inserted partner IDs
        val insertedPartners = dao.getAllPartners().firstOrNull() ?: emptyList()
        val pizzaPartner = insertedPartners.find { it.type == PartnerType.RESTAURANT } ?: p1
        val pharmacyPartner = insertedPartners.find { it.type == PartnerType.PHARMACY } ?: p3
        val cafePartner = insertedPartners.find { it.type == PartnerType.CAFE } ?: p5
        val storePartner = insertedPartners.find { it.type == PartnerType.STORE } ?: p6

        // 5. Products
        val products = listOf(
            ProductEntity(
                partnerId = pizzaPartner.id,
                category = "بيتزا إيطالي",
                name = "بيتزا سوبر سوبريم",
                description = "موتزاريلا طبيعية، سلامي، هوت دوج، فلفل أخضر، زيتون، وصوص طماطم رائع.",
                price = 145.0,
                imageEmoji = "🍕",
                sizesString = "صغير:0.0,وسط:35.0,كبير:70.0",
                addonsString = "إكسترا جبنة:25.0,مشروم:15.0,هلابينو حار:10.0"
            ),
            ProductEntity(
                partnerId = pizzaPartner.id,
                category = "بيتزا إيطالي",
                name = "بيتزا رانش دجاج باربيكيو",
                description = "قطع صدور دجاج مشوية، صوص رانش فاخر، صوص باربيكيو مدخن وجبنة شيدر.",
                price = 165.0,
                imageEmoji = "🍗",
                sizesString = "وسط:0.0,عائلي:50.0",
                addonsString = "صوص رانش إضافي:15.0,بطاطس ويدجز:30.0"
            ),
            ProductEntity(
                partnerId = pizzaPartner.id,
                category = "مقبلات ومشروبات",
                name = "أجنحة دجاج بافلو حارة",
                description = "6 قطع أجنحة دجاج مقرمشة مع صوص البافلو وغموس البلو تشيز.",
                price = 95.0,
                imageEmoji = "🍗",
                sizesString = "6 قطع:0.0,12 قطعة:75.0"
            ),
            ProductEntity(
                partnerId = pharmacyPartner.id,
                category = "أدوية ومسكنات",
                name = "بانادول إكسترا 500 ملجم",
                description = "مسكن فعال للصداع وخافض للحرارة مع كافيين سريع الامتصاص.",
                price = 52.0,
                imageEmoji = "💊"
            ),
            ProductEntity(
                partnerId = pharmacyPartner.id,
                category = "فيتامينات ومكملات",
                name = "فيتامين سي 1000 + زنك فوار",
                description = "أقراص فوارة بطعم البرتقال لتعزيز المناعة وصحة الجسم.",
                price = 85.0,
                imageEmoji = "🍊"
            ),
            ProductEntity(
                partnerId = cafePartner.id,
                category = "قهوة ساخنة",
                name = "سبانش لاتيه فاخر",
                description = "إسبريسو غني مع حليب مبخر وصوص الحليب المكثف المحلى الفاخر.",
                price = 78.0,
                imageEmoji = "☕",
                sizesString = "عادي:0.0,مزدوج:20.0"
            ),
            ProductEntity(
                partnerId = storePartner.id,
                category = "ألبان ومخبوزات",
                name = "حليب كامل الدسم المراعي 1 لتر",
                description = "حليب طبيعي 100% معقم وطازج.",
                price = 44.0,
                imageEmoji = "🥛"
            )
        )
        dao.insertProducts(products)

        // 6. Drivers
        val drivers = listOf(
            DriverProfileEntity(
                name = "أحمد محمود",
                phone = "01098765432",
                vehicle = "موتوسيكل هوندا 150cc",
                status = DriverStatus.AVAILABLE,
                workingArea = "المهندسين والدقي",
                todayEarnings = 220.0,
                totalEarnings = 3400.0,
                rating = 4.9,
                completedOrdersCount = 68
            ),
            DriverProfileEntity(
                name = "محمد عادل",
                phone = "01123456789",
                vehicle = "سكوتر فيسبا إيطالي",
                status = DriverStatus.AVAILABLE,
                workingArea = "الزمالك ووسط البلد",
                todayEarnings = 150.0,
                totalEarnings = 1900.0,
                rating = 4.8,
                completedOrdersCount = 35
            )
        )
        dao.insertDrivers(drivers)

        // 7. Coupons
        dao.insertCoupons(
            listOf(
                CouponEntity(
                    code = "SARIEE30",
                    discountPercent = 30,
                    minOrder = 100.0,
                    active = true,
                    usageCount = 142
                ),
                CouponEntity(
                    code = "EGYPT10",
                    discountPercent = 10,
                    minOrder = 50.0,
                    active = true,
                    usageCount = 310
                )
            )
        )

        // 8. Saved Addresses
        dao.insertAddresses(
            listOf(
                CustomerAddressEntity(
                    customerId = sampleCustomerId,
                    label = "المنزل",
                    area = "المهندسين",
                    street = "شارع سوريا متفرع من مصدق",
                    building = "عمارة 14",
                    floor = "الدور الرابع",
                    apartment = "شقة 402",
                    notes = "رن الجرس أو اتصل بالموبايل",
                    isDefault = true
                ),
                CustomerAddressEntity(
                    customerId = sampleCustomerId,
                    label = "العمل",
                    area = "الدقي",
                    street = "شارع مصدق الرئيسي",
                    building = "برج الأطباء",
                    floor = "الدور الثامن",
                    apartment = "مكتب 8B",
                    notes = "اترك الطلب للاستقبال",
                    isDefault = false
                )
            )
        )

        // 9. Sample Initial Orders to populate all roles immediately
        val initialOrder1 = OrderEntity(
            orderNumber = "#FS-1024",
            customerId = sampleCustomerId,
            customerName = "عمرو إبراهيم",
            customerPhone = "01011122233",
            partnerId = pizzaPartner.id,
            partnerName = pizzaPartner.name,
            partnerType = pizzaPartner.type,
            orderStatus = OrderStatus.PREPARING,
            deliveryStatus = DeliveryStatus.WAITING_FOR_DRIVER,
            dispatchMode = DispatchMode.SEQUENTIAL,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            paymentStatus = PaymentStatus.PENDING,
            deliveryAddress = "شارع سوريا، عمارة 14، المهندسين",
            subtotal = 145.0,
            deliveryFee = 25.0,
            discount = 0.0,
            total = 170.0,
            customerNotes = "بدون بصل لو سمحت",
            estimatedPrepMinutes = 15
        )

        val initialOrder2 = OrderEntity(
            orderNumber = "#FS-1025",
            customerId = sampleCustomerId,
            customerName = "سارة أحمد",
            customerPhone = "01233344455",
            partnerId = pharmacyPartner.id,
            partnerName = pharmacyPartner.name,
            partnerType = pharmacyPartner.type,
            orderStatus = OrderStatus.PENDING_REVIEW,
            deliveryStatus = DeliveryStatus.WAITING_FOR_DRIVER,
            dispatchMode = DispatchMode.SEQUENTIAL,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            paymentStatus = PaymentStatus.PENDING_VERIFICATION,
            transferReceiptNote = "تم تحويل 152 ج.م عبر انستاباي رقم مرجعي 987412",
            deliveryAddress = "شارع 26 يوليو، الزمالك",
            subtotal = 137.0,
            deliveryFee = 15.0,
            discount = 0.0,
            total = 152.0,
            customerNotes = "الروشتة مرفقة"
        )

        val o1Id = dao.insertOrder(initialOrder1)
        val o2Id = dao.insertOrder(initialOrder2)

        dao.insertActivityLog(
            OrderActivityLogEntity(
                orderId = o1Id,
                timeFormatted = "11:20",
                actor = "بيتزا تايم",
                actorRole = "الشريك",
                action = "START_PREPARING",
                oldValue = OrderStatus.APPROVED.name,
                newValue = OrderStatus.PREPARING.name,
                reason = "بدأ إعداد العجينة والمكونات بالمطبخ"
            )
        )

        dao.insertActivityLog(
            OrderActivityLogEntity(
                orderId = o2Id,
                timeFormatted = "11:25",
                actor = "سارة أحمد",
                actorRole = "العميل",
                action = "TRANSFER_UPLOADED",
                oldValue = PaymentStatus.PENDING.name,
                newValue = PaymentStatus.PENDING_VERIFICATION.name,
                reason = "رفع بيانات تحويل انستاباي"
            )
        )
    }
}
