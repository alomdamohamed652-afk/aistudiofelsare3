package com.example.data.repository

import com.example.core.model.AuthState
import com.example.core.model.UserRole
import com.example.core.model.UserSession
import kotlinx.coroutines.flow.StateFlow

/**
 * Clean abstraction for User Authentication and Session Management.
 * Separates local prototype behavior from future remote/cloud backend implementations.
 */
interface AuthRepository {
    val authState: StateFlow<AuthState>
    val currentSession: StateFlow<UserSession?>

    suspend fun login(phone: String, role: UserRole): Result<UserSession>
    suspend fun register(name: String, phone: String, email: String, role: UserRole): Result<UserSession>
    suspend fun logout()
    suspend fun switchDevelopmentRole(role: UserRole): UserSession
    suspend fun restoreSession(): UserSession?
}
