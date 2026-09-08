package com.example.core.model

/**
 * Represents an authenticated user session in the application.
 * Replaces hardcoded user/driver/partner assumptions.
 */
data class UserSession(
    val userId: Long,
    val name: String,
    val phone: String,
    val email: String = "",
    val role: UserRole,
    val associatedCustomerId: Long? = if (role == UserRole.CUSTOMER) userId else null,
    val associatedDriverId: Long? = null,
    val associatedPartnerId: Long? = null,
    val token: String = "session_${userId}_${System.currentTimeMillis()}"
)

/**
 * State representing authentication status.
 */
sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Authenticated(val session: UserSession) : AuthState
    object Unauthenticated : AuthState
    data class Error(val message: String) : AuthState
}
