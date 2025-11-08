package com.example.obby.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EncryptionManager {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    private const val SALT_LENGTH = 32
    private const val KEY_LENGTH = 256
    private const val ITERATION_COUNT = 65536

    /**
     * Encrypts plaintext with a password
     * @param plaintext The text to encrypt
     * @param password The password to use for encryption
     * @return Base64-encoded string containing salt + IV + encrypted data
     */
    fun encrypt(plaintext: String, password: String): String {
        // Generate random salt
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        // Derive key from password
        val secretKey = deriveKeyFromPassword(password, salt)

        // Initialize cipher
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        // Get IV
        val iv = cipher.iv

        // Encrypt
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Combine: salt + IV + encrypted data
        val combined = salt + iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts ciphertext with a password
     * @param encryptedData Base64-encoded string containing salt + IV + encrypted data
     * @param password The password to use for decryption
     * @return Decrypted plaintext
     * @throws Exception if password is incorrect or data is corrupted
     */
    fun decrypt(encryptedData: String, password: String): String {
        val combined = Base64.decode(encryptedData, Base64.NO_WRAP)

        // Extract salt, IV, and encrypted data
        val salt = combined.copyOfRange(0, SALT_LENGTH)
        val iv = combined.copyOfRange(SALT_LENGTH, SALT_LENGTH + GCM_IV_LENGTH)
        val encryptedBytes = combined.copyOfRange(
            SALT_LENGTH + GCM_IV_LENGTH,
            combined.size
        )

        // Derive key from password
        val secretKey = deriveKeyFromPassword(password, salt)

        // Initialize cipher
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        // Decrypt
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Derives a cryptographic key from a password using PBKDF2
     */
    private fun deriveKeyFromPassword(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Validates if a password can decrypt the given encrypted data
     * @return true if password is correct, false otherwise
     */
    fun isPasswordCorrect(encryptedData: String, password: String): Boolean {
        return try {
            decrypt(encryptedData, password)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if encryption is available on this device
     */
    fun isEncryptionAvailable(): Boolean {
        return try {
            SecretKeyFactory.getInstance(KEY_ALGORITHM)
            true
        } catch (e: Exception) {
            false
        }
    }
}
