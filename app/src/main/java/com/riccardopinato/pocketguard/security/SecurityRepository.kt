package com.riccardopinato.pocketguard.security

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.securityDataStore by preferencesDataStore(
    name = "pocket_guard_security"
)

class SecurityRepository(
    private val context: Context
) {
    private val saltKey = stringPreferencesKey("pin_salt")
    private val verifierKey = stringPreferencesKey("pin_verifier")

    val hasPinFlow: Flow<Boolean> =
        context.securityDataStore.data.map {
            !it[saltKey].isNullOrBlank() &&
                !it[verifierKey].isNullOrBlank()
        }

    suspend fun savePin(
        saltBase64: String,
        verifierBase64: String
    ) {
        context.securityDataStore.edit {
            it[saltKey] = saltBase64
            it[verifierKey] = verifierBase64
        }
    }

    suspend fun getPinMaterial(): Pair<String, String>? {
        val prefs = context.securityDataStore.data.first()
        val salt = prefs[saltKey]
        val verifier = prefs[verifierKey]

        return if (!salt.isNullOrBlank() && !verifier.isNullOrBlank()) {
            salt to verifier
        } else {
            null
        }
    }

    suspend fun clearPin() {
        context.securityDataStore.edit {
            it.remove(saltKey)
            it.remove(verifierKey)
        }
    }
}
