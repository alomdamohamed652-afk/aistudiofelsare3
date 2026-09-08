package com.example.data.repository

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
 * Passwords are hashed locally for the prototype; production authentication will move to Supabase Auth.
 */
class LocalAuthRepository(
    private val dao: FalsareeDao
) : AuthRepository {

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun login(phone: String, role: UserRole): Result<UserSession> = withContext(Dispatchers.IO) {
        val (normalizedPhone, password) = unpackLoginPayload(phone)
        if (normalizedPhone.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("رقم الهاتف مطلوب"))
        }
        if (password.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("كلمة المرور مطلوبة"))
        }

        val existingUser = dao.getUserByPhone(normalizedPhone)
            ?: return@withContext Result.failure(IllegalArgumentException("لا يوجد حساب مسجل بهذا الرقم"))

        if (existingUser.passwordHash.isBlank() || existingUser.passwordHash != hashPassword(password)) {
            return@withContext Result.failure(IllegalArgumentException("رقم الهاتف أو كلمة المرور غير صحيحة"))
        }

        val session = existingUser.toSession()
        _currentSession.value = session
        _authState.value = AuthState.Authenticated(session)
        Result.success(session)
    }

    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        role: UserRole
    ): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            val normalizedName = name.trim()
            val normalizedPhone = phone.trim()
            val (normalizedEmail, password, confirmPassword) = unpackRegistrationPayload(email)

            if (normalizedName.length < 2) {
                return@withContext Result.failure(IllegalArgumentException("الاسم يجب أن يحتوي على حرفين على الأقل"))
            }
            if (!normalizedPhone.matches(Regex("^01[0-9]{9}$"))) {
                return@withContext Result.failure(IllegalArgumentException("رقم الهاتف غير صالح"))
            }
            if (password.length < 6) {
                return@withContext Result.failure(IllegalArgumentException("كلمة المرور يجب أن تكون 6 أحرف أو أكثر"))
            }
            if (password != confirmPassword) {
                return@withContext Result.failure(IllegalArgumentException("كلمتا المرور غير متطابقتين"))
            }
            if (dao.getUserByPhone(normalizedPhone) != null) {
                return@withContext Result.failure(IllegalArgumentException("يوجد حساب مسجل بهذا الرقم"))
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

            _currentSession.value = session
            _authState.value = AuthState.Authenticated(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        _currentSession.value = null
        _authState.value = AuthState.Unauthenticated
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

    override suspend fun restoreSession(): UserSession? = _currentSession.value

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

    /**
     * Compatibility payload used by the existing callback signature. The payload never reaches the database;
     * only the extracted password hash is stored.
     */
    private fun unpackLoginPayload(payload: String): Pair<String, String> {
        val separator = payload.indexOf(CREDENTIAL_SEPARATOR)
        if (separator < 0) return payload.trim() to ""
        return payload.substring(0, separator).trim() to payload.substring(separator + 1)
    }

    private fun unpackRegistrationPayload(payload: String): Triple<String, String, String> {
        val separator = payload.indexOf(CREDENTIAL_SEPARATOR)
        if (separator < 0) return payload.trim() to "" to ""
        val email = payload.substring(0, separator)
        val remaining = payload.substring(separator + 1)
        val second = remaining.indexOf(CREDENTIAL_SEPARATOR)
        if (second < 0) return email to remaining to ""
        return Triple(email, remaining.substring(0, second), remaining.substring(second + 1))
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val CREDENTIAL_SEPARATOR = "\u001F"
    }
}
