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
 * This is a prototype auth layer; production authentication should use Supabase Auth.
 */
class LocalAuthRepository(
    private val dao: FalsareeDao
) : AuthRepository {

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun login(identifier: String, password: String): Result<UserSession> = withContext(Dispatchers.IO) {
        val normalizedIdentifier = identifier.trim()
        if (normalizedIdentifier.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("رقم الهاتف أو البريد الإلكتروني مطلوب"))
        }
        if (password.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("كلمة المرور مطلوبة"))
        }

        val lookupEmail = normalizedIdentifier.lowercase()
        val existingUser = dao.getUserByIdentifier(normalizedIdentifier, lookupEmail)
            ?: return@withContext Result.failure(IllegalArgumentException("رقم الهاتف أو البريد الإلكتروني غير مسجل"))

        if (existingUser.passwordHash.isBlank() || existingUser.passwordHash != hashPassword(password)) {
            return@withContext Result.failure(IllegalArgumentException("رقم الهاتف أو البريد الإلكتروني أو كلمة المرور غير صحيحة"))
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
        password: String,
        confirmPassword: String
    ): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            val normalizedName = name.trim()
            val normalizedPhone = phone.trim()
            val normalizedEmail = email.trim().lowercase()

            if (normalizedName.length < 2) {
                return@withContext Result.failure(IllegalArgumentException("الاسم يجب أن يحتوي على حرفين على الأقل"))
            }
            if (!normalizedPhone.matches(Regex("^01[0-9]{9}$"))) {
                return@withContext Result.failure(IllegalArgumentException("رقم الهاتف غير صالح"))
            }
            if (normalizedEmail.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) {
                return@withContext Result.failure(IllegalArgumentException("البريد الإلكتروني غير صالح"))
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
            if (normalizedEmail.isNotBlank() && dao.getUserByIdentifier("", normalizedEmail) != null) {
                return@withContext Result.failure(IllegalArgumentException("يوجد حساب مسجل بهذا البريد الإلكتروني"))
            }

            val userId = dao.insertUser(
                UserEntity(
                    name = normalizedName,
                    phone = normalizedPhone,
                    email = normalizedEmail,
                    passwordHash = hashPassword(password),
                    role = UserRole.CUSTOMER
                )
            )

            val session = UserSession(
                userId = userId,
                name = normalizedName,
                phone = normalizedPhone,
                email = normalizedEmail,
                role = UserRole.CUSTOMER,
                associatedCustomerId = userId
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

    /** Development-only role switching. Never callable in release builds. */
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

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
