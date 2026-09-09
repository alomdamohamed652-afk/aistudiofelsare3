package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.core.model.*
import com.example.data.local.*
import com.example.data.repository.FalsareeRepository
import com.example.data.repository.LocalAuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FalsareeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FalsareeDatabase.getDatabase(application)
    val repository = FalsareeRepository(database.dao())
    val authRepository = LocalAuthRepository(database.dao())

    val currentSession: StateFlow<UserSession?> = authRepository.currentSession
    val currentRole: StateFlow<UserRole> = currentSession
        .map { it?.role ?: UserRole.CUSTOMER }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserRole.CUSTOMER)
    val isUserLoggedIn: StateFlow<Boolean> = currentSession
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val currentCustomerId: StateFlow<Long?> = currentSession
        .map { it?.associatedCustomerId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()
    private val _customerSelectedTab = MutableStateFlow(0)
    val customerSelectedTab: StateFlow<Int> = _customerSelectedTab.asStateFlow()
    private val _driverSelectedTab = MutableStateFlow(0)
    val driverSelectedTab: StateFlow<Int> = _driverSelectedTab.asStateFlow()
    private val _selectedPartner = MutableStateFlow<PartnerEntity?>(null)
    val selectedPartner: StateFlow<PartnerEntity?> = _selectedPartner.asStateFlow()
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()
    private val _trackedOrderId = MutableStateFlow<Long?>(null)
    val trackedOrderId: StateFlow<Long?> = _trackedOrderId.asStateFlow()
    private val _reviewingOrderId = MutableStateFlow<Long?>(null)
    val reviewingOrderId: StateFlow<Long?> = _reviewingOrderId.asStateFlow()
    private val _activePartnerId = MutableStateFlow<Long?>(null)
    val activePartnerId: StateFlow<Long?> = _activePartnerId.asStateFlow()
    private val _activeDriverId = MutableStateFlow<Long?>(null)
    val activeDriverId: StateFlow<Long?> = _activeDriverId.asStateFlow()

    private val _cartPartner = MutableStateFlow<PartnerEntity?>(null)
    val cartPartner: StateFlow<PartnerEntity?> = _cartPartner.asStateFlow()
    private val _cartItems = MutableStateFlow<Map<ProductEntity, Int>>(emptyMap())
    val cartItems: StateFlow<Map<ProductEntity, Int>> = _cartItems.asStateFlow()
    private val _cartOptions = MutableStateFlow<Map<Long, String>>(emptyMap())
    val cartOptions: StateFlow<Map<Long, String>> = _cartOptions.asStateFlow()
    private val _appliedCoupon = MutableStateFlow<CouponEntity?>(null)
    val appliedCoupon: StateFlow<CouponEntity?> = _appliedCoupon.asStateFlow()
    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _selectedCategoryFilter = MutableStateFlow<PartnerType?>(null)
    val selectedCategoryFilter: StateFlow<PartnerType?> = _selectedCategoryFilter.asStateFlow()
    private val _onlyOpenFilter = MutableStateFlow(false)
    val onlyOpenFilter: StateFlow<Boolean> = _onlyOpenFilter.asStateFlow()
    private val _alertMessage = MutableStateFlow<String?>(null)
    val alertMessage: StateFlow<String?> = _alertMessage.asStateFlow()

    val favoritePartnerIds: StateFlow<List<Long>> = currentCustomerId
        .flatMapLatest { it?.let(repository::getFavoritePartnerIdsForCustomer) ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customerOrders: StateFlow<List<OrderEntity>> = currentCustomerId
        .flatMapLatest { it?.let(repository::getCustomerOrders) ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customerAddresses: StateFlow<List<CustomerAddressEntity>> = currentCustomerId
        .flatMapLatest { it?.let(repository::getAddressesForCustomer) ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customerTickets: StateFlow<List<SupportTicketEntity>> = currentCustomerId
        .flatMapLatest { it?.let(repository::getTicketsForCustomer) ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val driverPayoutRequests: StateFlow<List<DriverPayoutRequestEntity>> = currentSession
        .flatMapLatest { it?.associatedDriverId?.let(repository::getDriverPayoutRequests) ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val driverShiftAssignments: StateFlow<List<DriverShiftAssignmentEntity>> = repository.driverShiftAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val driverPerformance: StateFlow<DriverPerformanceEntity?> = currentSession
        .flatMapLatest { it?.associatedDriverId?.let(repository::observeDriverPerformance) ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val activeDriverOfferEvents: StateFlow<List<DriverDispatchEventEntity>> = currentSession
        .flatMapLatest { it?.associatedDriverId?.let(repository::activeDriverOfferEvents) ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { repository.seedInitialDataIfEmpty() }
        viewModelScope.launch {
            currentSession.collect { session ->
                _activeDriverId.value = session?.associatedDriverId
                _activePartnerId.value = session?.associatedPartnerId
            }
        }
    }

    fun clearAlert() { _alertMessage.value = null }
    fun setAlert(msg: String) { _alertMessage.value = msg }

    fun switchRole(role: UserRole) {
        if (!BuildConfig.DEBUG) {
            _alertMessage.value = "تبديل الأدوار متاح في نسخة التطوير فقط"
            return
        }
        viewModelScope.launch {
            runCatching { authRepository.switchDevelopmentRole(role) }
                .onSuccess { session ->
                    _activeDriverId.value = session.associatedDriverId
                    _activePartnerId.value = session.associatedPartnerId
                }
                .onFailure { error ->
                    _alertMessage.value = error.message ?: "تعذر تبديل الدور"
                }
        }
    }

    fun setCustomerTab(tabIndex: Int) { _customerSelectedTab.value = tabIndex }
    fun setDriverTab(tabIndex: Int) { _driverSelectedTab.value = tabIndex }
    fun selectPartner(partner: PartnerEntity?) { _selectedPartner.value = partner }
    fun selectProduct(product: ProductEntity?) { _selectedProduct.value = product }

    fun trackOrder(orderId: Long?) {
        if (orderId == null || customerOrders.value.any { it.id == orderId }) {
            _trackedOrderId.value = orderId
        } else {
            _alertMessage.value = "لا يمكنك عرض طلب مستخدم آخر"
        }
    }

    fun setActivePartnerId(id: Long) {
        if (!BuildConfig.DEBUG) {
            _alertMessage.value = "لا يمكن تغيير هوية الشريك خارج وضع الاختبار"
            return
        }
        _activePartnerId.value = id
    }

    fun login(identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _alertMessage.value = "أدخل رقم الهاتف أو البريد الإلكتروني وكلمة المرور"
            return
        }
        viewModelScope.launch {
            authRepository.login(identifier.trim(), password)
                .onSuccess { session ->
                    _customerSelectedTab.value = 0
                    _activeDriverId.value = session.associatedDriverId
                    _activePartnerId.value = session.associatedPartnerId
                }
                .onFailure { error ->
                    _alertMessage.value = error.message ?: "تعذر تسجيل الدخول"
                }
        }
    }

    fun register(name: String, phone: String, email: String, password: String, confirmation: String) {
        if (password != confirmation) {
            _alertMessage.value = "كلمتا المرور غير متطابقتين"
            return
        }
        viewModelScope.launch {
            authRepository.register(
                name = name.trim(),
                phone = phone.trim(),
                email = email.trim(),
                password = password,
                role = UserRole.CUSTOMER
            ).onFailure { error ->
                _alertMessage.value = error.message ?: "تعذر إنشاء الحساب"
            }
        }
    }

    fun completeOnboarding() { _isOnboardingCompleted.value = true }

    fun addToCart(partner: PartnerEntity, product: ProductEntity, quantity: Int, optionsSummary: String = "") {
        val currentPartner = _cartPartner.value
        if (currentPartner != null && currentPartner.id != partner.id) {
            _alertMessage.value = "لا يمكن الطلب من شريكين مختلفين في نفس السلة. تم إفراغ السلة السابقة وبدء سلة جديدة من ${partner.name}."
            _cartPartner.value = partner
            _cartItems.value = mapOf(product to quantity)
            _cartOptions.value = mapOf(product.id to optionsSummary)
            _appliedCoupon.value = null
            _discountAmount.value = 0.0
            return
        }
        _cartPartner.value = partner
        val currentMap = _cartItems.value.toMutableMap()
        currentMap[product] = (currentMap[product] ?: 0) + quantity
        _cartItems.value = currentMap
        if (optionsSummary.isNotEmpty()) {
            val optMap = _cartOptions.value.toMutableMap()
            optMap[product.id] = optionsSummary
            _cartOptions.value = optMap
        }
        _alertMessage.value = "تمت إضافة ${product.name} إلى السلة ⚡"
    }

    fun updateCartItemQuantity(product: ProductEntity, delta: Int) {
        val currentMap = _cartItems.value.toMutableMap()
        val currentQty = currentMap[product] ?: return
        val newQty = currentQty + delta
        if (newQty <= 0) {
            currentMap.remove(product)
            val optMap = _cartOptions.value.toMutableMap()
            optMap.remove(product.id)
            _cartOptions.value = optMap
        } else {
            currentMap[product] = newQty
        }
        _cartItems.value = currentMap
        if (currentMap.isEmpty()) {
            _cartPartner.value = null
            _appliedCoupon.value = null
            _discountAmount.value = 0.0
        }
    }

    fun clearCart() {
        _cartPartner.value = null
        _cartItems.value = emptyMap()
        _cartOptions.value = emptyMap()
        _appliedCoupon.value = null
        _discountAmount.value = 0.0
    }

    fun applyCoupon(code: String) {
        viewModelScope.launch {
            val subtotal = _cartItems.value.entries.sumOf { it.key.price * it.value }
            repository.validateCoupon(code, subtotal)
                .onSuccess { (coupon, discount) ->
                    _appliedCoupon.value = coupon
                    _discountAmount.value = discount
                    _alertMessage.value = "تم تطبيق كود ${coupon.code} بنجاح! خصم ${discount.toInt()} ج.م ⚡"
                }
                .onFailure { error -> _alertMessage.value = error.message ?: "فشل تطبيق الكوبون" }
        }
    }

    fun placeOrder(
        customerName: String = "عميل فالسريع",
        customerPhone: String = "01000000000",
        deliveryAddress: String,
        customerNotes: String = "",
        paymentMethod: PaymentMethod,
        transferReceiptNote: String = ""
    ) {
        val partner = _cartPartner.value ?: return
        val itemsList = _cartItems.value.map { it.key to it.value }
        val optionsSummary = _cartOptions.value.values.joinToString(", ")
        viewModelScope.launch {
            val customerId = currentSession.value?.associatedCustomerId
            if (customerId == null) {
                _alertMessage.value = "يجب تسجيل الدخول لإرسال الطلب"
                return@launch
            }
            repository.placeOrder(
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                partner = partner,
                items = itemsList,
                optionsNotes = optionsSummary,
                deliveryAddress = deliveryAddress,
                customerNotes = customerNotes,
                paymentMethod = paymentMethod,
                appliedDiscount = _discountAmount.value,
                transferReceiptNote = transferReceiptNote
            ).onSuccess { orderId ->
                clearCart()
                _trackedOrderId.value = orderId
                _customerSelectedTab.value = 2
                _selectedPartner.value = null
                _alertMessage.value = "تم تأكيد طلبك بنجاح! يوصلك فالسريع ⚡"
            }.onFailure { error -> _alertMessage.value = error.message ?: "حدث خطأ أثناء إرسال الطلب" }
        }
    }

    fun setDriverAvailability(driverId: Long, status: DriverStatus) {
        viewModelScope.launch {
            repository.setDriverAvailability(driverId, status)
                .onFailure { _alertMessage.value = it.message ?: "تعذر تغيير حالة المندوب" }
        }
    }

    fun acceptDriverOffer(orderId: Long, driverId: Long) {
        viewModelScope.launch {
            repository.acceptDriverOffer(orderId, driverId)
                .onFailure { _alertMessage.value = it.message ?: "تعذر قبول الطلب" }
        }
    }

    fun rejectDriverOffer(orderId: Long, driverId: Long, reason: String, shiftName: String) {
        viewModelScope.launch {
            if (reason.isBlank()) {
                _alertMessage.value = "يجب اختيار سبب الرفض"
                return@launch
            }
            repository.rejectDriverOffer(orderId, driverId, reason, shiftName)
                .onFailure { _alertMessage.value = it.message ?: "تعذر رفض الطلب" }
        }
    }

    fun timeoutDriverOffer(orderId: Long, driverId: Long, shiftName: String) {
        viewModelScope.launch {
            repository.timeoutDriverOffer(orderId, driverId, shiftName)
                .onFailure { _alertMessage.value = it.message ?: "تعذر معالجة انتهاء مهلة العرض" }
        }
    }

    fun updateDriverDeliveryStatus(
        order: OrderEntity,
        driver: DriverProfileEntity?,
        status: DeliveryStatus,
        reason: String
    ) {
        val sessionDriverId = currentSession.value?.associatedDriverId
        if (driver == null || sessionDriverId != driver.id) {
            _alertMessage.value = "لا توجد صلاحية لتنفيذ هذا الإجراء"
            return
        }
        viewModelScope.launch {
            repository.updateDeliveryStatus(
                order.id,
                status,
                driver.id,
                driver.name,
                driver.name,
                "المندوب",
                reason
            ).onFailure { _alertMessage.value = it.message ?: "تعذر تحديث حالة التوصيل" }
        }
    }

    fun toggleFavorite(partnerId: Long) {
        viewModelScope.launch {
            val customerId = currentSession.value?.associatedCustomerId
            if (customerId == null) {
                _alertMessage.value = "يجب تسجيل الدخول لإدارة المفضلة"
                return@launch
            }
            val isFav = favoritePartnerIds.value.contains(partnerId)
            repository.toggleFavorite(partnerId, isFav, customerId)
            _alertMessage.value = if (isFav) "تمت الإزالة من المفضلة" else "تمت الإضافة إلى المفضلة ❤️"
        }
    }

    fun openReviewDialog(orderId: Long) {
        if (customerOrders.value.any { it.id == orderId }) {
            _reviewingOrderId.value = orderId
        } else {
            _alertMessage.value = "لا يمكنك تقييم طلب مستخدم آخر"
        }
    }

    fun closeReviewDialog() { _reviewingOrderId.value = null }

    fun cancelCustomerOrder(orderId: Long) {
        viewModelScope.launch {
            if (customerOrders.value.none { it.id == orderId }) {
                _alertMessage.value = "لا يمكنك إلغاء طلب مستخدم آخر"
                return@launch
            }
            repository.updateOrderStatus(
                orderId = orderId,
                newStatus = OrderStatus.CANCELLED,
                actor = currentSession.value?.name ?: "العميل",
                actorRole = "العميل",
                reason = "إلغاء بناء على رغبة العميل"
            ).onFailure { _alertMessage.value = it.message ?: "تعذر إلغاء الطلب" }
        }
    }

    fun submitReview(orderId: Long, partnerRating: Int, driverRating: Int, notes: String) {
        viewModelScope.launch {
            val customerId = currentSession.value?.associatedCustomerId
            if (customerId == null) {
                _alertMessage.value = "يجب تسجيل الدخول لإرسال التقييم"
                return@launch
            }
            repository.submitReview(orderId, partnerRating, driverRating, notes, customerId)
                .onSuccess {
                    _reviewingOrderId.value = null
                    _alertMessage.value = "شكراً لتقييمك! نسعد بخدمتك دائماً ⚡"
                }
                .onFailure { _alertMessage.value = it.message ?: "تعذر إرسال التقييم" }
        }
    }

    fun addAddress(address: CustomerAddressEntity) {
        viewModelScope.launch {
            val customerId = currentSession.value?.associatedCustomerId
            if (customerId == null) {
                _alertMessage.value = "يجب تسجيل الدخول لإضافة عنوان"
                return@launch
            }
            repository.addAddress(address.copy(id = 0L, customerId = customerId))
            _alertMessage.value = "تم حفظ العنوان بنجاح"
        }
    }

    fun deleteAddress(address: CustomerAddressEntity) {
        viewModelScope.launch {
            val customerId = currentSession.value?.associatedCustomerId
            if (customerId == null || address.customerId != customerId) {
                _alertMessage.value = "لا يمكنك حذف هذا العنوان"
                return@launch
            }
            repository.deleteAddress(address)
        }
    }

    fun createSupportTicket(ticket: SupportTicketEntity) {
        viewModelScope.launch {
            val session = currentSession.value
            val customerId = session?.associatedCustomerId
            if (session == null || customerId == null) {
                _alertMessage.value = "يجب تسجيل الدخول لإنشاء تذكرة دعم"
                return@launch
            }
            if (ticket.orderId != null && customerOrders.value.none { it.id == ticket.orderId }) {
                _alertMessage.value = "لا يمكنك إنشاء تذكرة لطلب مستخدم آخر"
                return@launch
            }
            repository.createTicket(ticket.copy(id = 0L, customerId = customerId, customerName = session.name))
            _alertMessage.value = "تم إرسال تذكرة الدعم بنجاح"
        }
    }

    fun requestDriverPayout(amount: Double) {
        viewModelScope.launch {
            val driverId = currentSession.value?.associatedDriverId
            if (driverId == null) {
                _alertMessage.value = "لا توجد هوية مندوب مرتبطة بهذه الجلسة"
                return@launch
            }
            repository.requestDriverPayout(driverId, amount)
            _alertMessage.value = "تم إرسال طلب سحب ${amount.toInt()} جنيه بنجاح للإدارة 💵"
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            clearCart()
            _trackedOrderId.value = null
            _reviewingOrderId.value = null
            _selectedPartner.value = null
            _selectedProduct.value = null
            _customerSelectedTab.value = 0
            _driverSelectedTab.value = 0
            _activeDriverId.value = null
            _activePartnerId.value = null
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategoryFilter(type: PartnerType?) { _selectedCategoryFilter.value = type }
    fun toggleOnlyOpenFilter() { _onlyOpenFilter.value = !_onlyOpenFilter.value }
}
