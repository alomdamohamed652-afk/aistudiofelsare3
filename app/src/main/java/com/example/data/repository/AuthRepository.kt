package com.example.data.repository

import com.example.core.model.AuthState
import com.example.core.model.UserRole
import com.example.core.model.UserSession
import kotlinx.coroutines.flow.StateFlow

/**
 * Authentication/session abstraction. The current UI keeps the original callback contract;
 * the local repository extracts credentials from the submission payload until Supabase Auth
 * replaces this prototype implementation.
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
