package com.example.data.repository

import com.example.core.model.AuthState
import com.example.core.model.UserRole
import com.example.core.model.UserSession
import kotlinx.coroutines.flow.StateFlow

/**
 * Authentication/session abstraction.
 * Production authentication will be provided by Supabase Auth.
 */
interface AuthRepository {
    val authState: StateFlow<AuthState>
    val currentSession: StateFlow<UserSession?>

    suspend fun login(identifier: String, password: String): Result<UserSession>
    suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<UserSession>
    suspend fun logout()
    suspend fun switchDevelopmentRole(role: UserRole): UserSession
    suspend fun restoreSession(): UserSession?
}
