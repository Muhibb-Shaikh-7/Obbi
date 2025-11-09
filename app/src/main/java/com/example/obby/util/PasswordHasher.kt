package com.example.obby.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Secure password hashing utility using PBKDF2WithHmacSHA256
 *
 * This class provides cryptographically secure password hashing and verification
 * using industry-standard algorithms and parameters.
 */
object PasswordHasher {
    private const val ITERATIONS = 150_000 // High iteration count for security
    private const val KEY_LENGTH = 256 // bits
    private const val SALT_LENGTH = 32 // bytes
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    /**
     * Generate a cryptographically secure random salt
     */
    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    /**
     * Hash a password with the given salt
     *
     * @param password The password to hash (will be cleared after use)
     * @param salt The salt to use for hashing
     * @return The hashed password as a byte array
     */
    fun hash(password: CharArray, salt: ByteArray): ByteArray {
        return try {
            val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            factory.generateSecret(spec).encoded
        } finally {
            // Clear password from memory for security
            password.fill('\u0000')
        }
    }

    /**
     * Verify a password against a stored hash
     *
     * @param password The password to verify (will be cleared after use)
     * @param salt The salt used for the stored hash
     * @param expectedHash The expected hash to compare against
     * @return true if the password matches, false otherwise
     */
    fun verify(password: CharArray, salt: ByteArray, expectedHash: ByteArray): Boolean {
        return try {
            val actualHash = hash(password.copyOf(), salt)
            MessageDigest.isEqual(actualHash, expectedHash)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Encode bytes to Base64 string for storage
     */
    fun encodeToString(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Decode Base64 string to bytes
     */
    fun decodeFromString(encoded: String): ByteArray {
        return Base64.decode(encoded, Base64.NO_WRAP)
    }

    /**
     * Check if a password meets minimum complexity requirements
     *
     * @param password The password to check
     * @return Pair of (isValid, errorMessage)
     */
    fun checkPasswordComplexity(password: String): Pair<Boolean, String?> {
        return when {
            password.length < 8 -> false to "Password must be at least 8 characters"
            password.all { it.isDigit() } -> false to "Password cannot be all numbers"
            password.all { it.isLetter() && it.isLowerCase() } -> false to "Password should include uppercase, numbers, or symbols"
            else -> true to null
        }
    }

    /**
     * Calculate password strength (0-100)
     */
    fun calculatePasswordStrength(password: String): Int {
        var strength = 0

        // Length score (up to 40 points)
        strength += (password.length.coerceAtMost(20) * 2)

        // Character variety (up to 60 points)
        if (password.any { it.isLowerCase() }) strength += 10
        if (password.any { it.isUpperCase() }) strength += 15
        if (password.any { it.isDigit() }) strength += 15
        if (password.any { !it.isLetterOrDigit() }) strength += 20

        return strength.coerceIn(0, 100)
    }
}
