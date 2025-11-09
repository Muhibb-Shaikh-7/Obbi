package com.example.obby.data.repository

import android.content.Context
import com.example.obby.util.PasswordHasher
import com.example.obby.util.SecurePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing hidden folder password authentication
 *
 * Provides secure password storage, verification, attempt tracking, and lockout logic
 */
class PasswordRepository(context: Context) {

    private val securePrefs = SecurePreferences(context)

    companion object {
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_PASSWORD_SALT = "password_salt"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"
        private const val KEY_RECOVERY_PHRASE_HASH = "recovery_phrase_hash"
        private const val KEY_RECOVERY_PHRASE_SALT = "recovery_phrase_salt"

        // Lockout policy
        private const val MAX_ATTEMPTS_BEFORE_LOCKOUT = 5
        private const val LOCKOUT_DURATION_MS = 2 * 60 * 1000L // 2 minutes
        private const val LOCKOUT_MULTIPLIER = 2 // Exponential backoff multiplier
    }

    /**
     * Check if a password has been set
     */
    suspend fun hasPassword(): Boolean = withContext(Dispatchers.IO) {
        securePrefs.contains(KEY_PASSWORD_HASH)
    }

    /**
     * Set a new password
     *
     * @param password The password to set (will be cleared after use)
     * @return true if successful
     */
    suspend fun setPassword(password: CharArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hash(password, salt)

            securePrefs.putBytes(KEY_PASSWORD_SALT, salt)
            securePrefs.putBytes(KEY_PASSWORD_HASH, hash)

            // Reset attempts on password change
            resetAttempts()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verify a password attempt
     *
     * @param password The password to verify (will be cleared after use)
     * @return true if the password is correct
     */
    suspend fun verifyPassword(password: CharArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val salt = securePrefs.getBytes(KEY_PASSWORD_SALT) ?: return@withContext false
            val hash = securePrefs.getBytes(KEY_PASSWORD_HASH) ?: return@withContext false

            PasswordHasher.verify(password, salt, hash)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Change the password (requires old password verification)
     *
     * @param oldPassword The current password
     * @param newPassword The new password to set
     * @return true if successful
     */
    suspend fun changePassword(oldPassword: CharArray, newPassword: CharArray): Boolean =
        withContext(Dispatchers.IO) {
            if (!verifyPassword(oldPassword.copyOf())) {
                return@withContext false
            }

            setPassword(newPassword)
        }

    /**
     * Record a failed authentication attempt
     */
    suspend fun recordFailedAttempt() = withContext(Dispatchers.IO) {
        val currentAttempts = getFailedAttempts()
        val newAttempts = currentAttempts + 1

        securePrefs.putInt(KEY_FAILED_ATTEMPTS, newAttempts)

        // Calculate lockout if needed
        if (newAttempts >= MAX_ATTEMPTS_BEFORE_LOCKOUT) {
            val lockoutMultiplier = (newAttempts / MAX_ATTEMPTS_BEFORE_LOCKOUT)
            val lockoutDuration = LOCKOUT_DURATION_MS *
                    (LOCKOUT_MULTIPLIER.toLong().shl(lockoutMultiplier - 1))
            val lockoutUntil = System.currentTimeMillis() + lockoutDuration

            securePrefs.putLong(KEY_LOCKOUT_UNTIL, lockoutUntil)
        }
    }

    /**
     * Reset failed attempts counter
     */
    suspend fun resetAttempts() = withContext(Dispatchers.IO) {
        securePrefs.putInt(KEY_FAILED_ATTEMPTS, 0)
        securePrefs.remove(KEY_LOCKOUT_UNTIL)
    }

    /**
     * Get current number of failed attempts
     */
    suspend fun getFailedAttempts(): Int = withContext(Dispatchers.IO) {
        securePrefs.getInt(KEY_FAILED_ATTEMPTS, 0)
    }

    /**
     * Check if currently locked out
     */
    suspend fun isLockedOut(): Boolean = withContext(Dispatchers.IO) {
        val lockoutUntil = securePrefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        if (lockoutUntil == 0L) return@withContext false

        val now = System.currentTimeMillis()
        if (now >= lockoutUntil) {
            // Lockout period expired, clear it
            securePrefs.remove(KEY_LOCKOUT_UNTIL)
            false
        } else {
            true
        }
    }

    /**
     * Get remaining lockout time in milliseconds
     */
    suspend fun getLockoutRemainingMs(): Long = withContext(Dispatchers.IO) {
        val lockoutUntil = securePrefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        if (lockoutUntil == 0L) return@withContext 0L

        val remaining = lockoutUntil - System.currentTimeMillis()
        if (remaining < 0) 0L else remaining
    }

    /**
     * Generate and store a recovery phrase
     *
     * @return The recovery phrase as a string (12 words)
     */
    suspend fun generateRecoveryPhrase(): String = withContext(Dispatchers.IO) {
        val words = RECOVERY_WORDS.shuffled().take(12)
        val phrase = words.joinToString(" ")

        // Store hashed recovery phrase
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(phrase.toCharArray(), salt)

        securePrefs.putBytes(KEY_RECOVERY_PHRASE_SALT, salt)
        securePrefs.putBytes(KEY_RECOVERY_PHRASE_HASH, hash)

        phrase
    }

    /**
     * Verify a recovery phrase and reset the password if correct
     *
     * @param recoveryPhrase The recovery phrase to verify
     * @param newPassword The new password to set if recovery is successful
     * @return true if recovery was successful
     */
    suspend fun recoverWithPhrase(recoveryPhrase: String, newPassword: CharArray): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val salt = securePrefs.getBytes(KEY_RECOVERY_PHRASE_SALT)
                    ?: return@withContext false
                val hash = securePrefs.getBytes(KEY_RECOVERY_PHRASE_HASH)
                    ?: return@withContext false

                if (!PasswordHasher.verify(recoveryPhrase.toCharArray(), salt, hash)) {
                    return@withContext false
                }

                // Recovery phrase is correct, set new password
                setPassword(newPassword)
                true
            } catch (e: Exception) {
                false
            }
        }

    /**
     * Check if a recovery phrase has been set up
     */
    suspend fun hasRecoveryPhrase(): Boolean = withContext(Dispatchers.IO) {
        securePrefs.contains(KEY_RECOVERY_PHRASE_HASH)
    }

    /**
     * Clear all password data (use with caution!)
     */
    suspend fun clearAll() = withContext(Dispatchers.IO) {
        securePrefs.clear()
    }

    // Recovery phrase word list (subset of BIP39 for demonstration)
    private val RECOVERY_WORDS = listOf(
        "abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract",
        "absurd", "abuse", "access", "accident", "account", "accuse", "achieve", "acid",
        "acoustic", "acquire", "across", "act", "action", "actor", "actress", "actual",
        "adapt", "add", "addict", "address", "adjust", "admit", "adult", "advance",
        "advice", "aerobic", "affair", "afford", "afraid", "again", "age", "agent",
        "agree", "ahead", "aim", "air", "airport", "aisle", "alarm", "album",
        "alcohol", "alert", "alien", "all", "alley", "allow", "almost", "alone",
        "alpha", "already", "also", "alter", "always", "amateur", "amazing", "among",
        "amount", "amused", "analyst", "anchor", "ancient", "anger", "angle", "angry",
        "animal", "ankle", "announce", "annual", "another", "answer", "antenna", "antique",
        "anxiety", "any", "apart", "apology", "appear", "apple", "approve", "april",
        "arch", "arctic", "area", "arena", "argue", "arm", "armed", "armor",
        "army", "around", "arrange", "arrest", "arrive", "arrow", "art", "artefact",
        "artist", "artwork", "ask", "aspect", "assault", "asset", "assist", "assume",
        "asthma", "athlete", "atom", "attack", "attend", "attitude", "attract", "auction",
        "audit", "august", "aunt", "author", "auto", "autumn", "average", "avocado",
        "avoid", "awake", "aware", "away", "awesome", "awful", "awkward", "axis"
    )
}
