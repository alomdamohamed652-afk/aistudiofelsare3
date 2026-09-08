package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.core.model.AuthState
import com.example.core.model.UserRole
import com.example.core.model.UserSession
import com.example.data.local.FalsareeDao
import com.example.data.local.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * Local authentication implementation used until Supabase Auth is integrated.
 * Passwords are stored as SHA-256 hashes for this prototype; production credentials
 * must be delegated to Supabase Auth rather than stored in the Room users table.
 */
class LocalAuthRepository(
    private val dao: FalsareeDao,
    context: Context
) : AuthRepository {

    private val preferences = context.applicationContext.getSharedPreferences(
        "falsaree_auth",
        Context.MODE_PRIVATE
    )

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun login(phone: String, password: String): Result<UserSession> = withContext(Dispatchers.IO) {
        val normalizedPhone = normalizePhone(phone)
        if (normalizedPhone.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("رقم الهاتف مطلوب"))
        }
        if (password.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("كلمة المرور مطلوبة"))
        }

        val existingUser = dao.getUserByPhone(normalizedPhone)
            ?: return@withContext Result.failure(IllegalArgumentException("لا يوجد حساب مسجل بهذا الرقم"))

        if (existingUser.passwordHash.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("هذا الحساب يحتاج إلى إعادة إنشاء كلمة المرور قبل تسجيل الدخول")
            )
        }

        if (existingUser.passwordHash != hashPassword(password)) {
            return@withContext Result.failure(IllegalArgumentException("رقم الهاتف أو كلمة المرور غير صحيحة"))
        }

        val session = existingUser.toSession()
        setAuthenticatedSession(session)
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
            val normalizedPhone = normalizePhone(phone)
            val normalizedEmail = email.trim()

            validateRegistration(normalizedName, normalizedPhone, password)?.let {
                return@withContext Result.failure(IllegalArgumentException(it))
            }

            if (dao.getUserByPhone(normalizedPhone) != null) {
                return@withContext Result.failure(
                    IllegalArgumentException("يوجد حساب مسجل بهذا الرقم")
                )
            }

            val userId = dao.insertUser(
                UserEntity(
                    name = normalizedName,
                    phone = normalizedPhone,
                    email = normalizedEmail,
                    passwordHash = hashPassword(password),
                    role = role
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

            setAuthenticatedSession(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        _currentSession.value = null
        _authState.value = AuthState.Unauthenticated
        preferences.edit().remove(KEY_SESSION_USER_ID).apply()
    }

    /** Development-only role switching. Production builds cannot elevate roles. */
    override suspend fun switchDevelopmentRole(role: UserRole): UserSession {
        if (!BuildConfig.DEBUG) {
            throw IllegalStateException("تبديل الأدوار غير متاح في نسخة الإنتاج")
        }

        val current = _currentSession.value
            ?: throw IllegalStateException("لا توجد جلسة مستخدم نشطة")

        val updated = current.copy(role = role)
        _currentSession.value = updated
        _authState.value = AuthState.Authenticated(updated)
        return updated
    }

    override suspend fun restoreSession(): UserSession? = withContext(Dispatchers.IO) {
        val userId = preferences.getLong(KEY_SESSION_USER_ID, -1L)
        if (userId <= 0L) {
            return@withContext null
        }

        val user = dao.getUserById(userId) ?: run {
            preferences.edit().remove(KEY_SESSION_USER_ID).apply()
            return@withContext null
        }

        val session = user.toSession()
        _currentSession.value = session
        _authState.value = AuthState.Authenticated(session)
        session
    }

    private fun setAuthenticatedSession(session: UserSession) {
        _currentSession.value = session
        _authState.value = AuthState.Authenticated(session)
        preferences.edit().putLong(KEY_SESSION_USER_ID, session.userId).apply()
    }

    private fun UserEntity.toSession(): UserSession = UserSession(
        userId = id,
        name = name,
        phone = phone,
        email = email,
        role = role,
        associatedCustomerId = if (role == UserRole.CUSTOMER) id else null,
        associatedDriverId = associatedDriverId,
        associatedPartnerId = associatedPartnerId
    )

    private fun normalizePhone(phone: String): String =
        phone.trim().replace(" ", "").replace("-", "")

    private fun validateRegistration(name: String, phone: String, password: String): String? {
        if (name.length < 2) return "الاسم يجب أن يحتوي على حرفين على الأقل"
        if (!phone.matches(Regex("^01[0-9]{9}$"))) return "رقم الهاتف غير صالح"
        if (password.length < 6) return "كلمة المرور يجب أن تكون 6 أحرف أو أكثر"
        return null
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val KEY_SESSION_USER_ID = "session_user_id"
    }
}
