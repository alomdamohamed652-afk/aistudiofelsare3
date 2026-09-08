package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    val currentRole: StateFlow<UserRole> = currentSession.map { it?.role ?: UserRole.CUSTOMER }.stateIn(viewModelScope, SharingStarted.Eagerly, UserRole.CUSTOMER)
    val isUserLoggedIn: StateFlow<Boolean> = currentSession.map { it != null }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val currentCustomerId: StateFlow<Long?> = currentSession.map { it?.associatedCustomerId }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

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

    val favoritePartnerIds: StateFlow<List<Long>> = currentCustomerId
        .flatMapLatest { customerId -> customerId?.let(repository::getFavoritePartnerIdsForCustomer) ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customerOrders: StateFlow<List<OrderEntity>> = currentCustomerId
        .flatMapLatest { customerId -> customerId?.let(repository::getCustomerOrders) ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customerAddresses: StateFlow<List<CustomerAddressEntity>> = currentCustomerId
        .flatMapLatest { customerId -> customerId?.let(repository::getAddressesForCustomer) ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customerTickets: StateFlow<List<SupportTicketEntity>> = currentCustomerId
        .flatMapLatest { customerId -> customerId?.let(repository::getTicketsForCustomer) ?: flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val driverPayoutRequests: StateFlow<List<DriverPayoutRequestEntity>> = repository.driverPayoutRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activePartnerId = MutableStateFlow(0L)
    val activePartnerId: StateFlow<Long> = _activePartnerId.asStateFlow()
    private val _activeDriverId = MutableStateFlow(0L)
    val activeDriverId: StateFlow<Long> = _activeDriverId.asStateFlow()

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

    init { viewModelScope.launch { repository.seedInitialDataIfEmpty() } }

    /** Debug-only helper. Release builds cannot change the authenticated role. */
    fun switchRole(role: UserRole) {
        viewModelScope.launch {
            runCatching { authRepository.switchDevelopmentRole(role) }
                .onFailure { _alertMessage.value = it.message ?: "تبديل الأدوار غير متاح" }
        }
    }

    fun setCustomerTab(tabIndex: Int) { _customerSelectedTab.value = tabIndex }
    fun selectPartner(partner: PartnerEntity?) { _selectedPartner.value = partner }
    fun selectProduct(product: ProductEntity?) { _selectedProduct.value = product }

    fun trackOrder(orderId: Long?) {
        if (orderId == null) { _trackedOrderId.value = null; return }
        val customerId = currentCustomerId.value
        if (customerId == null) { _alertMessage.value = "يجب تسجيل الدخول لعرض الطلب"; return }
        viewModelScope.launch {
            val owned = repository.getCustomerOrders(customerId).firstOrNull()?.any { it.id == orderId } == true
            if (owned) _trackedOrderId.value = orderId else _alertMessage.value = "لا يمكنك عرض هذا الطلب"
        }
    }

    fun setActivePartnerId(id: Long) { if (id > 0L) _activePartnerId.value = id }

    fun login(identifier: String, password: String) {
        if (identifier.isBlank()) { _alertMessage.value = "أدخل رقم الهاتف أو البريد الإلكتروني"; return }
        if (password.isBlank()) { _alertMessage.value = "أدخل كلمة المرور"; return }
        viewModelScope.launch {
            authRepository.login(identifier.trim(), password)
                .onSuccess { session ->
                    _customerSelectedTab.value = 0
                    session.associatedDriverId?.let { _activeDriverId.value = it }
                    session.associatedPartnerId?.let { _activePartnerId.value = it }
                }
                .onFailure { error -> _alertMessage.value = error.message ?: "تعذر تسجيل الدخول" }
        }
    }

    fun register(name: String, phone: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            authRepository.register(name, phone, email, password, confirmPassword)
                .onFailure { error -> _alertMessage.value = error.message ?: "تعذر إنشاء الحساب" }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            clearCart()
            _trackedOrderId.value = null
            _reviewingOrderId.value = null
            _activeDriverId.value = 0L
            _activePartnerId.value = 0L
        }
    }

    fun clearAlert() { _alertMessage.value = null }
    fun setAlert(msg: String) { _alertMessage.value = msg }
    fun completeOnboarding() { _isOnboardingCompleted.value = true }

    fun addToCart(partner: PartnerEntity, product: ProductEntity, quantity: Int, optionsSummary: String = "") {
        if (currentRole.value != UserRole.CUSTOMER) { _alertMessage.value = "إضافة المنتجات للسلة متاحة للعميل فقط"; return }
        if (quantity <= 0) { _alertMessage.value = "الكمية غير صالحة"; return }
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
        _cartItems.value = _cartItems.value.toMutableMap().also { it[product] = (it[product] ?: 0) + quantity }
        if (optionsSummary.isNotBlank()) _cartOptions.value = _cartOptions.value.toMutableMap().also { it[product.id] = optionsSummary }
        _alertMessage.value = "تمت إضافة ${product.name} إلى السلة ⚡"
    }

    fun updateCartItemQuantity(product: ProductEntity, delta: Int) {
        val items = _cartItems.value.toMutableMap()
        val qty = items[product] ?: return
        if (qty + delta <= 0) {
            items.remove(product)
            _cartOptions.value = _cartOptions.value.toMutableMap().also { it.remove(product.id) }
        } else items[product] = qty + delta
        _cartItems.value = items
        if (items.isEmpty()) { _cartPartner.value = null; _appliedCoupon.value = null; _discountAmount.value = 0.0 }
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

    fun placeOrder(deliveryAddress: String, customerNotes: String = "", paymentMethod: PaymentMethod, transferReceiptNote: String = "") {
        val session = currentSession.value
        val customerId = session?.associatedCustomerId
        val partner = _cartPartner.value
        if (currentRole.value != UserRole.CUSTOMER || session == null || customerId == null) { _alertMessage.value = "يجب تسجيل الدخول بحساب عميل لإرسال الطلب"; return }
        if (partner == null || _cartItems.value.isEmpty()) { _alertMessage.value = "السلة فارغة"; return }
        if (deliveryAddress.isBlank()) { _alertMessage.value = "اختر عنوان التوصيل"; return }
        val itemsList = _cartItems.value.map { it.key to it.value }
        val optionsSummary = _cartOptions.value.values.filter { it.isNotBlank() }.joinToString(", ")
        viewModelScope.launch {
            repository.placeOrder(customerId, session.name, session.phone, partner, itemsList, optionsSummary, deliveryAddress, customerNotes, paymentMethod, _discountAmount.value, transferReceiptNote)
                .onSuccess { orderId ->
                    clearCart(); _trackedOrderId.value = orderId; _customerSelectedTab.value = 3; _selectedPartner.value = null
                    _alertMessage.value = "تم تأكيد طلبك بنجاح! يوصلك فالسريع ⚡"
                }
                .onFailure { error -> _alertMessage.value = error.message ?: "حدث خطأ أثناء إرسال الطلب" }
        }
    }

    fun toggleFavorite(partnerId: Long) {
        viewModelScope.launch {
            val customerId = currentCustomerId.value ?: run { _alertMessage.value = "يجب تسجيل الدخول لإدارة المفضلة"; return@launch }
            val isFav = favoritePartnerIds.value.contains(partnerId)
            repository.toggleFavorite(partnerId, isFav, customerId)
            _alertMessage.value = if (isFav) "تمت الإزالة من المفضلة" else "تمت الإضافة إلى المفضلة ❤️"
        }
    }

    fun openReviewDialog(orderId: Long) {
        val customerId = currentCustomerId.value ?: run { _alertMessage.value = "يجب تسجيل الدخول لإرسال التقييم"; return }
        viewModelScope.launch {
            val order = repository.getCustomerOrders(customerId).firstOrNull()?.firstOrNull { it.id == orderId }
            if (order == null || order.orderStatus != OrderStatus.DELIVERED) _alertMessage.value = "لا يمكن تقييم هذا الطلب"
            else _reviewingOrderId.value = orderId
        }
    }

    fun closeReviewDialog() { _reviewingOrderId.value = null }

    fun submitReview(orderId: Long, partnerRating: Int, driverRating: Int, notes: String) {
        viewModelScope.launch {
            val customerId = currentCustomerId.value ?: run { _alertMessage.value = "يجب تسجيل الدخول لإرسال التقييم"; return@launch }
            val owned = repository.getCustomerOrders(customerId).firstOrNull()?.firstOrNull { it.id == orderId }
            if (owned == null || owned.orderStatus != OrderStatus.DELIVERED) { _alertMessage.value = "لا يمكنك تقييم هذا الطلب"; return@launch }
            runCatching { repository.submitReview(orderId, partnerRating, driverRating, notes, customerId) }
                .onSuccess { _reviewingOrderId.value = null; _alertMessage.value = "شكراً لتقييمك! نسعد بخدمتك دائماً ⚡" }
                .onFailure { error -> _alertMessage.value = error.message ?: "تعذر إرسال التقييم" }
        }
    }

    fun cancelCustomerOrder(orderId: Long) {
        viewModelScope.launch {
            val customerId = currentCustomerId.value ?: run { _alertMessage.value = "يجب تسجيل الدخول لإلغاء الطلب"; return@launch }
            val order = repository.getCustomerOrders(customerId).firstOrNull()?.firstOrNull { it.id == orderId }
            if (order == null) { _alertMessage.value = "لا يمكنك إلغاء هذا الطلب"; return@launch }
            if (order.orderStatus in setOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.REJECTED)) { _alertMessage.value = "لا يمكن إلغاء هذا الطلب الآن"; return@launch }
            repository.updateOrderStatus(orderId, OrderStatus.CANCELLED, currentSession.value?.name ?: "العميل", "العميل", "إلغاء بناء على رغبة العميل")
                .onSuccess { _trackedOrderId.value = null; _alertMessage.value = "تم إلغاء الطلب بنجاح" }
                .onFailure { error -> _alertMessage.value = error.message ?: "تعذر إلغاء الطلب" }
        }
    }

    fun addAddress(address: CustomerAddressEntity) {
        viewModelScope.launch {
            val customerId = currentCustomerId.value ?: run { _alertMessage.value = "يجب تسجيل الدخول لإضافة عنوان"; return@launch }
            repository.addAddress(address.copy(id = 0L, customerId = customerId))
            _alertMessage.value = "تم حفظ العنوان بنجاح"
        }
    }

    fun deleteAddress(address: CustomerAddressEntity) {
        viewModelScope.launch {
            val customerId = currentCustomerId.value
            if (customerId == null || address.customerId != customerId) { _alertMessage.value = "لا يمكنك حذف هذا العنوان"; return@launch }
            repository.deleteAddress(address)
        }
    }

    fun createSupportTicket(ticket: SupportTicketEntity) {
        viewModelScope.launch {
            val session = currentSession.value
            val customerId = session?.associatedCustomerId
            if (session == null || customerId == null) { _alertMessage.value = "يجب تسجيل الدخول لإنشاء تذكرة دعم"; return@launch }
            repository.createTicket(ticket.copy(id = 0L, customerId = customerId, customerName = session.name))
            _alertMessage.value = "تم إرسال تذكرة الدعم بنجاح"
        }
    }

    fun requestDriverPayout(amount: Double) {
        viewModelScope.launch {
            val driverId = currentSession.value?.associatedDriverId
            if (currentRole.value != UserRole.DRIVER || driverId == null) { _alertMessage.value = "لا يوجد حساب مندوب مرتبط بالجلسة الحالية"; return@launch }
            if (amount <= 0) { _alertMessage.value = "قيمة السحب غير صالحة"; return@launch }
            repository.requestDriverPayout(driverId, amount)
            _alertMessage.value = "تم إرسال طلب سحب ${amount.toInt()} جنيه بنجاح للإدارة 💵"
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategoryFilter(type: PartnerType?) { _selectedCategoryFilter.value = type }
    fun toggleOnlyOpenFilter() { _onlyOpenFilter.value = !_onlyOpenFilter.value }
}
