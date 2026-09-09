package com.example.data.repository

import android.content.Context
import com.example.core.model.UserRole
import com.example.core.model.UserSession

/**
 * Persists only the non-sensitive session identity needed to restore a local
 * prototype session after an app restart. Passwords are never stored here.
 *
 * This is intentionally a local implementation; a production backend should
 * replace it with a server-issued, securely stored access token.
 */
class LocalSessionStore(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun save(session: UserSession) {
        preferences.edit()
            .putLong(KEY_USER_ID, session.userId)
            .putString(KEY_NAME, session.name)
            .putString(KEY_PHONE, session.phone)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_ROLE, session.role.name)
            .putLong(KEY_CUSTOMER_ID, session.associatedCustomerId ?: NO_ID)
            .putLong(KEY_DRIVER_ID, session.associatedDriverId ?: NO_ID)
            .putLong(KEY_PARTNER_ID, session.associatedPartnerId ?: NO_ID)
            .putString(KEY_TOKEN, session.token)
            .apply()
    }

    fun restore(): UserSession? {
        if (!preferences.contains(KEY_USER_ID)) return null

        val roleName = preferences.getString(KEY_ROLE, null) ?: return null
        val role = runCatching { UserRole.valueOf(roleName) }.getOrNull() ?: return null
        val userId = preferences.getLong(KEY_USER_ID, NO_ID)
        if (userId == NO_ID) return null

        return UserSession(
            userId = userId,
            name = preferences.getString(KEY_NAME, "") ?: "",
            phone = preferences.getString(KEY_PHONE, "") ?: "",
            email = preferences.getString(KEY_EMAIL, "") ?: "",
            role = role,
            associatedCustomerId = preferences.optionalId(KEY_CUSTOMER_ID),
            associatedDriverId = preferences.optionalId(KEY_DRIVER_ID),
            associatedPartnerId = preferences.optionalId(KEY_PARTNER_ID),
            token = preferences.getString(KEY_TOKEN, "") ?: ""
        )
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun android.content.SharedPreferences.optionalId(key: String): Long? =
        getLong(key, NO_ID).takeIf { it != NO_ID }

    private companion object {
        const val PREFERENCES_NAME = "falsaree_session"
        const val NO_ID = -1L
        const val KEY_USER_ID = "user_id"
        const val KEY_NAME = "name"
        const val KEY_PHONE = "phone"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
        const val KEY_CUSTOMER_ID = "customer_id"
        const val KEY_DRIVER_ID = "driver_id"
        const val KEY_PARTNER_ID = "partner_id"
        const val KEY_TOKEN = "token"
    }
}
