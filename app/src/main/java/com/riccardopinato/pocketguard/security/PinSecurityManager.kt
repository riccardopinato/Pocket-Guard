package com.riccardopinato.pocketguard.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PinSecurityManager(
    private val repository: SecurityRepository
) {
    suspend fun setPin(pin: String): Boolean {
        if (!PIN_REGEX.matches(pin)) return false

        val salt = ByteArray(16).also {
            SecureRandom().nextBytes(it)
        }

        val verifier = buildVerifier(pin, salt)

        repository.savePin(
            Base64.encodeToString(salt, Base64.NO_WRAP),
            Base64.encodeToString(verifier, Base64.NO_WRAP)
        )
        return true
    }

    suspend fun verify(pin: String): Boolean {
        if (!PIN_REGEX.matches(pin)) return false

        val material = repository.getPinMaterial() ?: return false
        val salt = Base64.decode(material.first, Base64.NO_WRAP)
        val stored = Base64.decode(material.second, Base64.NO_WRAP)
        val calculated = buildVerifier(pin, salt)

        return constantTimeEquals(stored, calculated)
    }

    private fun buildVerifier(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(
            pin.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            256
        )

        val derived =
            SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded

        spec.clearPassword()

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(getOrCreateKeystoreKey())
        return mac.doFinal(derived)
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore =
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_HMAC_SHA256,
            "AndroidKeyStore"
        )

        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).build()
        )

        return generator.generateKey()
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }

    private companion object {
        val PIN_REGEX = Regex("^\\d{4,6}$")
        const val KEY_ALIAS = "pocket_guard_pin_hmac"
        const val PBKDF2_ITERATIONS = 120_000
    }
}
