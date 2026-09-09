package com.example.data.repository


import android.content.Context
import com.example.core.model.AuthState
import com.example.core.model.UserRole
import com.example.core.model.UserSession
import com.example.data.local.FalsareeDao
import com.example.data.local.UserEntity
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Local implementation of AuthRepository backed by Room database.
 * Supports persistent local sessions, real user creation, and isolated development role switching.
 */
class LocalAuthRepository(
    context: Context,
    private val dao: FalsareeDao
) : AuthRepository {

    private val sessionStore = LocalSessionStore(context)

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun login(identifier: String, password: String): Result<UserSession> = withContext(Dispatchers.IO) {
        val normalizedIdentifier = identifier.trim()
        if (normalizedIdentifier.isBlank() || password.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("رقم الهاتف أو البريد الإلكتروني وكلمة المرور مطلوبان"))
        }
        val existingUser = dao.getUserByPhone(normalizedIdentifier)
            ?: dao.getUserByEmail(normalizedIdentifier)
        if (existingUser != null && !verifyPassword(password, existingUser.passwordSalt, existingUser.passwordHash)) {
            return@withContext Result.failure(IllegalArgumentException("بيانات تسجيل الدخول غير صحيحة"))
        }
        if (existingUser != null && !existingUser.isActive) {
            return@withContext Result.failure(IllegalStateException(
                if (existingUser.activationStatus == "PENDING_ACTIVATION")
                    "تم إنشاء الحساب وبانتظار تفعيل الإدارة"
                else "الحساب غير نشط. تواصل مع الإدارة"
            ))
        }
        val session = existingUser?.let {
            UserSession(
                userId = it.id,
                name = it.name,
                phone = it.phone,
                email = it.email,
                role = it.role,
                associatedCustomerId = if (it.role == UserRole.CUSTOMER) it.id else null,
                associatedDriverId = it.associatedDriverId,
                associatedPartnerId = it.associatedPartnerId
            )
        } ?: return@withContext Result.failure(
            IllegalArgumentException("لا يوجد حساب مسجل بهذا الرقم")
        )

        persistAuthenticatedSession(session)
        Result.success(session)
    }

    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            val normalizedName = name.trim()
            val normalizedPhone = phone.trim()
            val normalizedEmail = email.trim()
            if (normalizedName.isBlank() || normalizedPhone.isBlank() || password.length < 8) {
                return@withContext Result.failure(IllegalArgumentException("الاسم ورقم الهاتف وكلمة مرور من 8 أحرف على الأقل مطلوبة"))
            }
            if (dao.getUserByPhone(normalizedPhone) != null ||
                (normalizedEmail.isNotBlank() && dao.getUserByEmail(normalizedEmail) != null)) {
                return@withContext Result.failure(
                    IllegalArgumentException("يوجد حساب مسجل بهذا الهاتف أو البريد الإلكتروني")
                )
            }
            if (role == UserRole.DRIVER) {
                val driverProfile = dao.getDriverByPhone(normalizedPhone)
                    ?: return@withContext Result.failure(
                        IllegalStateException("رقم الهاتف غير مسجل لدى الإدارة كمندوب. تواصل مع الإدارة أولاً")
                    )
            }
            val salt = generateSalt()
            val userId = dao.insertUser(
                UserEntity(
                    name = normalizedName,
                    phone = normalizedPhone,
                    email = normalizedEmail,
                    passwordHash = hashPassword(password, salt),
                    passwordSalt = salt,
                    role = role,
                    isActive = role != UserRole.DRIVER,
                    activationStatus = if (role == UserRole.DRIVER) "PENDING_ACTIVATION" else "ACTIVE"
                )
            )

            val session = UserSession(
                userId = userId,
                name = normalizedName,
                phone = normalizedPhone,
                email = normalizedEmail,
                role = role,
                associatedCustomerId = if (role == UserRole.CUSTOMER) userId else null
            )

            persistAuthenticatedSession(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sessionStore.clear()
        _currentSession.value = null
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun switchDevelopmentRole(role: UserRole): UserSession {
        check(BuildConfig.DEBUG) { "تبديل الأدوار متاح في نسخة التطوير فقط" }
        val current = requireNotNull(_currentSession.value) { "يجب تسجيل الدخول أولاً" }
        val updated = current.copy(
            role = role,
            associatedCustomerId = if (role == UserRole.CUSTOMER) current.userId else current.associatedCustomerId
        )

        persistAuthenticatedSession(updated)
        return updated
    }

    override suspend fun restoreSession(): UserSession? = withContext(Dispatchers.IO) {
        val restored = sessionStore.restore()
        if (restored == null) {
            _currentSession.value = null
            _authState.value = AuthState.Unauthenticated
            return@withContext null
        }

        val user = dao.getUserById(restored.userId)
        if (user == null) {
            sessionStore.clear()
            _currentSession.value = null
            _authState.value = AuthState.Unauthenticated
            return@withContext null
        }

        val session = restored.copy(
            name = user.name,
            phone = user.phone,
            email = user.email,
            role = user.role,
            associatedCustomerId = if (user.role == UserRole.CUSTOMER) user.id else null,
            associatedDriverId = user.associatedDriverId,
            associatedPartnerId = user.associatedPartnerId
        )
        persistAuthenticatedSession(session)
        session
    }

    private fun persistAuthenticatedSession(session: UserSession) {
        sessionStore.save(session)
        _currentSession.value = session
        _authState.value = AuthState.Authenticated(session)
    }

    private fun generateSalt(): String = ByteArray(16).also(SecureRandom()::nextBytes)
        .let(Base64.getEncoder()::encodeToString)

    private fun hashPassword(password: String, salt: String): String = MessageDigest.getInstance("SHA-256")
        .digest((salt + password).toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean =
        MessageDigest.isEqual(hashPassword(password, salt).toByteArray(), expectedHash.toByteArray())
}
