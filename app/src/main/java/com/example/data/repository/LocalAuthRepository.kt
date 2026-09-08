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
 * Supports persistent local sessions, real user creation, and isolated development role switching.
 */
class LocalAuthRepository(
    private val dao: FalsareeDao
) : AuthRepository {

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    override val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun login(phone: String, role: UserRole): Result<UserSession> = withContext(Dispatchers.IO) {
        val existingUser = dao.getUserByPhone(phone)
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
            val existing = dao.getUserByPhone(phone)
            if (existing != null) {
                return@withContext Result.failure(
                    IllegalArgumentException("يوجد حساب مسجل بهذا الرقم")
                )
            }
            val userId = dao.insertUser(
                UserEntity(
                    name = name,
                    phone = phone,
                    email = email,
                    role = role
                )
            )

            val session = UserSession(
                userId = userId,
                name = name,
                phone = phone,
                email = email,
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

    override suspend fun switchDevelopmentRole(role: UserRole): UserSession {
        val current = _currentSession.value
        val updated = current?.copy(
            role = role,
            associatedCustomerId = if (role == UserRole.CUSTOMER) current.userId else current.associatedCustomerId
        ) ?: UserSession(
            userId = 1L,
            name = "مستخدم التطوير",
            phone = "01000000000",
            role = role,
            associatedCustomerId = if (role == UserRole.CUSTOMER) 1L else null
        )

        _currentSession.value = updated
        _authState.value = AuthState.Authenticated(updated)
        return updated
    }

    override suspend fun restoreSession(): UserSession? {
        return _currentSession.value
    }
}
