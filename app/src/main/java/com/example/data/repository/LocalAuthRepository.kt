package com.example.data.repository

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

/**
 * Local implementation of AuthRepository backed by Room database.
 * Authentication is local-only until the production Supabase auth layer is integrated.
 */
class LocalAuthRepository(
    private val dao: FalsareeDao
) : AuthRepository {

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun login(phone: String, role: UserRole): Result<UserSession> = withContext(Dispatchers.IO) {
        val normalizedPhone = phone.trim()
        if (normalizedPhone.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("رقم الهاتف مطلوب"))
        }

        val existingUser = dao.getUserByPhone(normalizedPhone)
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
            val normalizedEmail = email.trim()

            if (normalizedName.isBlank() || normalizedPhone.isBlank()) {
                return@withContext Result.failure(
                    IllegalArgumentException("الاسم ورقم الهاتف مطلوبان")
                )
            }

            val existing = dao.getUserByPhone(normalizedPhone)
            if (existing != null) {
                return@withContext Result.failure(
                    IllegalArgumentException("يوجد حساب مسجل بهذا الرقم")
                )
            }

            val userId = dao.insertUser(
                UserEntity(
                    name = normalizedName,
                    phone = normalizedPhone,
                    email = normalizedEmail,
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

    /**
     * Kept for compatibility with the existing development UI, but it can no longer
     * elevate or change an authenticated user's role. Production role must come from
     * the persisted account/session.
     */
    override suspend fun switchDevelopmentRole(role: UserRole): UserSession {
        val current = _currentSession.value
            ?: throw IllegalStateException("لا توجد جلسة مستخدم نشطة")

        if (current.role != role) {
            return current
        }

        return current
    }

    override suspend fun restoreSession(): UserSession? {
        // Persistent session restoration will be handled by the production auth layer.
        return _currentSession.value
    }
}
