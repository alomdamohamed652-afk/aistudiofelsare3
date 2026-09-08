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

    // Current authenticated session — drives all user-specific data
    val currentSession: StateFlow<UserSession?> = authRepository.currentSession

    // --- Active App Navigation & Role State ---
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(true)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    // Customer Navigation Tab (0: Home, 1: Explore, 2: Orders, 3: Favorites, 4: Profile)
    private val _customerSelectedTab = MutableStateFlow(0)
    val customerSelectedTab: StateFlow<Int> = _customerSelectedTab.asStateFlow()

    // Driver Navigation Tab (0: Home, 1: Orders, 2: Wallet, 3: Profile)
    private val _driverSelectedTab = MutableStateFlow(0)
    val driverSelectedTab: StateFlow<Int> = _driverSelectedTab.asStateFlow()

    // Selected Partner Detail View
    private val _selectedPartner = MutableStateFlow<PartnerEntity?>(null)
    val selectedPartner: StateFlow<PartnerEntity?> = _selectedPartner.asStateFlow()

    // Selected Product for Options Sheet
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    // Currently Tracked Order
    private val _trackedOrderId = MutableStateFlow<Long?>(null)
    val trackedOrderId: StateFlow<Long?> = _trackedOrderId.asStateFlow()

    // Order to review
    private val _reviewingOrderId = MutableStateFlow<Long?>(null)
    val reviewingOrderId: StateFlow<Long?> = _reviewingOrderId.asStateFlow()

    // Favorites
    val favoritePartnerIds: StateFlow<List<Long>> = repository.favoritePartnerIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Driver Payouts
    val driverPayoutRequests: StateFlow<List<com.example.data.local.DriverPayoutRequestEntity>> = repository.driverPayoutRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Partner ID — sourced from session when available, overridable by admin/dev tools
    private val _activePartnerId = MutableStateFlow<Long>(1L)
    val activePartnerId: StateFlow<Long> = _activePartnerId.asStateFlow()

    // Active Driver ID — sourced from session when available; dev-mode role switch falls back to 1L
    private val _activeDriverId = MutableStateFlow<Long>(1L)
    val activeDriverId: StateFlow<Long> = _activeDriverId.asStateFlow()

    // --- Cart State (Single Partner Rule Enforced) ---
    private val _cartPartner = MutableStateFlow<PartnerEntity?>(null)
    val cartPartner: StateFlow<PartnerEntity?> = _cartPartner.asStateFlow()

    private val _cartItems = MutableStateFlow<Map<ProductEntity, Int>>(emptyMap())
    val cartItems: StateFlow<Map<ProductEntity, Int>> = _cartItems.asStateFlow()

    private val _cartOptions = MutableStateFlow<Map<Long, String>>(emptyMap()) // productId to options text
    val cartOptions: StateFlow<Map<Long, String>> = _cartOptions.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<CouponEntity?>(null)
    val appliedCoupon: StateFlow<CouponEntity?> = _appliedCoupon.asStateFlow()

    private val _discountAmount = MutableStateFlow(0.0)
    val discountAmount: StateFlow<Double> = _discountAmount.asStateFlow()

    // Search Query & Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<PartnerType?>(null)
    val selectedCategoryFilter: StateFlow<PartnerType?> = _selectedCategoryFilter.asStateFlow()

    private val _onlyOpenFilter = MutableStateFlow(false)
    val onlyOpenFilter: StateFlow<Boolean> = _onlyOpenFilter.asStateFlow()

    // Toast/Alert Message
    private val _alertMessage = MutableStateFlow<String?>(null)
    val alertMessage: StateFlow<String?> = _alertMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // --- Role Switching (dev tool — routes through auth for session consistency) ---
    fun switchRole(role: UserRole) {
        _currentRole.value = role
        viewModelScope.launch {
            val updatedSession = authRepository.switchDevelopmentRole(role)
            updatedSession.associatedDriverId?.let { _activeDriverId.value = it }
            updatedSession.associatedPartnerId?.let { _activePartnerId.value = it }
        }
    }

    fun setCustomerTab(tabIndex: Int) {
        _customerSelectedTab.value = tabIndex
        if (tabIndex != 1 && _selectedPartner.value != null) {
            // keep partner or back
        }
    }

    fun selectPartner(partner: PartnerEntity?) {
        _selectedPartner.value = partner
    }

    fun selectProduct(product: ProductEntity?) {
        _selectedProduct.value = product
    }

    fun trackOrder(orderId: Long?) {
        _trackedOrderId.value = orderId
    }

    fun setActivePartnerId(id: Long) {
        _activePartnerId.value = id
    }

    fun clearAlert() {
        _alertMessage.value = null
    }

    fun setAlert(msg: String) {
        _alertMessage.value = msg
    }

    fun completeOnboarding() {
        _isOnboardingCompleted.value = true
    }

    // --- Cart Management ---
    fun addToCart(partner: PartnerEntity, product: ProductEntity, quantity: Int, optionsSummary: String = "") {
        val currentPartner = _cartPartner.value
        if (currentPartner != null && currentPartner.id != partner.id) {
            // Violation of Single-Partner Rule
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
        val currentQty = currentMap[product] ?: 0
        currentMap[product] = currentQty + quantity
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
            val result = repository.validateCoupon(code, subtotal)
            result.onSuccess { (coupon, discount) ->
                _appliedCoupon.value = coupon
                _discountAmount.value = discount
                _alertMessage.value = "تم تطبيق كود ${coupon.code} بنجاح! خصم ${discount.toInt()} ج.م ⚡"
            }.onFailure { error ->
                _alertMessage.value = error.message ?: "فشل تطبيق الكوبون"
            }
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
        val itemsList = _cartItems.value.map { Pair(it.key, it.value) }
        val optionsSummary = _cartOptions.value.values.joinToString(", ")

        viewModelScope.launch {
            val customerId = currentSession.value?.associatedCustomerId ?: 1L
            val res = repository.placeOrder(
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
            )
            res.onSuccess { orderId ->
                clearCart()
                _trackedOrderId.value = orderId
                _customerSelectedTab.value = 2 // Go to Orders tab
                _selectedPartner.value = null
                _alertMessage.value = "تم تأكيد طلبك بنجاح! يوصلك فالسريع ⚡"
            }.onFailure { error ->
                _alertMessage.value = error.message ?: "حدث خطأ أثناء إرسال الطلب"
            }
        }
    }

    fun setDriverTab(tabIndex: Int) {
        _driverSelectedTab.value = tabIndex
    }

    fun toggleFavorite(partnerId: Long) {
        viewModelScope.launch {
            val customerId = currentSession.value?.associatedCustomerId ?: 1L
            val isFav = favoritePartnerIds.value.contains(partnerId)
            repository.toggleFavorite(partnerId, isFav, customerId)
            _alertMessage.value = if (isFav) "تمت الإزالة من المفضلة" else "تمت الإضافة إلى المفضلة ❤️"
        }
    }

    fun openReviewDialog(orderId: Long) {
        _reviewingOrderId.value = orderId
    }

    fun closeReviewDialog() {
        _reviewingOrderId.value = null
    }

    fun submitReview(orderId: Long, partnerRating: Int, driverRating: Int, notes: String) {
        viewModelScope.launch {
            val customerId = currentSession.value?.associatedCustomerId ?: 1L
            repository.submitReview(orderId, partnerRating, driverRating, notes, customerId)
            _reviewingOrderId.value = null
            _alertMessage.value = "شكراً لتقييمك! نسعد بخدمتك دائماً ⚡"
        }
    }

    fun requestDriverPayout(driverId: Long, amount: Double) {
        viewModelScope.launch {
            repository.requestDriverPayout(driverId, amount)
            _alertMessage.value = "تم إرسال طلب سحب ${amount.toInt()} جنيه بنجاح للإدارة 💵"
        }
    }

    // --- Search & Filters ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(type: PartnerType?) {
        _selectedCategoryFilter.value = type
    }

    fun toggleOnlyOpenFilter() {
        _onlyOpenFilter.value = !_onlyOpenFilter.value
    }
}
