package com.example.obby.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager for private folder state and operations
 *
 * Coordinates between PasswordRepository and UI state
 */
class PrivateFolderManager(context: Context) {

    private val passwordRepo = PasswordRepository(context)

    private val _state = MutableStateFlow<PrivateFolderState>(PrivateFolderState.Uninitialized)
    val state: StateFlow<PrivateFolderState> = _state.asStateFlow()

    /**
     * Initialize the manager and determine current state
     */
    suspend fun initialize() {
        val hasPassword = passwordRepo.hasPassword()
        _state.value = if (hasPassword) {
            PrivateFolderState.Locked
        } else {
            PrivateFolderState.Uninitialized
        }
    }

    /**
     * Create a new password for the private folder
     *
     * @param password The password to set
     * @param confirmPassword Confirmation password (must match)
     * @return Result with success boolean or error message
     */
    suspend fun createPassword(
        password: String,
        confirmPassword: String
    ): Result<Boolean> {
        if (password != confirmPassword) {
            return Result.failure(Exception("Passwords do not match"))
        }

        if (password.length < 8) {
            return Result.failure(Exception("Password must be at least 8 characters"))
        }

        val success = passwordRepo.setPassword(password.toCharArray())
        if (success) {
            _state.value = PrivateFolderState.Unlocked
            return Result.success(true)
        }

        return Result.failure(Exception("Failed to set password"))
    }

    /**
     * Attempt to unlock the private folder
     *
     * @param password The password attempt
     * @return Result with success boolean or error message
     */
    suspend fun unlock(password: String): Result<Boolean> {
        // Check if locked out
        if (passwordRepo.isLockedOut()) {
            val remaining = passwordRepo.getLockoutRemainingMs()
            val seconds = (remaining / 1000).toInt()
            _state.value = PrivateFolderState.LockedOut(
                until = System.currentTimeMillis() + remaining,
                attemptCount = passwordRepo.getFailedAttempts()
            )
            return Result.failure(Exception("Locked out. Try again in $seconds seconds."))
        }

        // Verify password
        val isCorrect = passwordRepo.verifyPassword(password.toCharArray())

        if (isCorrect) {
            passwordRepo.resetAttempts()
            _state.value = PrivateFolderState.Unlocked
            return Result.success(true)
        } else {
            passwordRepo.recordFailedAttempt()
            val attempts = passwordRepo.getFailedAttempts()

            // Check if this caused a lockout
            if (passwordRepo.isLockedOut()) {
                val remaining = passwordRepo.getLockoutRemainingMs()
                _state.value = PrivateFolderState.LockedOut(
                    until = System.currentTimeMillis() + remaining,
                    attemptCount = attempts
                )
                return Result.failure(Exception("Too many failed attempts. Locked out."))
            }

            _state.value = PrivateFolderState.Unlocking(
                failedAttempts = attempts,
                lockedOutUntil = null
            )
            return Result.failure(Exception("Incorrect password. $attempts failed attempt(s)."))
        }
    }

    /**
     * Lock the private folder
     */
    fun lock() {
        _state.value = PrivateFolderState.Locked
    }

    /**
     * Change the password
     *
     * @param oldPassword Current password
     * @param newPassword New password
     * @param confirmPassword Confirmation of new password
     * @return Result with success boolean or error message
     */
    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Boolean> {
        if (newPassword != confirmPassword) {
            return Result.failure(Exception("New passwords do not match"))
        }

        if (newPassword.length < 8) {
            return Result.failure(Exception("Password must be at least 8 characters"))
        }

        val success = passwordRepo.changePassword(
            oldPassword.toCharArray(),
            newPassword.toCharArray()
        )

        if (success) {
            return Result.success(true)
        }

        return Result.failure(Exception("Incorrect old password"))
    }

    /**
     * Remove the password (make private notes accessible without password)
     *
     * @param password Current password for confirmation
     * @return Result with success boolean or error message
     */
    suspend fun removePassword(password: String): Result<Boolean> {
        val isCorrect = passwordRepo.verifyPassword(password.toCharArray())
        if (!isCorrect) {
            return Result.failure(Exception("Incorrect password"))
        }

        passwordRepo.clearAll()
        _state.value = PrivateFolderState.Uninitialized
        return Result.success(true)
    }

    /**
     * Remove a specific note from private status (requires password confirmation)
     *
     * @param noteId The ID of the note to remove from private
     * @param password Password for confirmation
     * @return Result with success boolean or error message
     */
    suspend fun removeNoteFromPrivate(noteId: Long, password: String): Result<Boolean> {
        val isCorrect = passwordRepo.verifyPassword(password.toCharArray())
        if (!isCorrect) {
            return Result.failure(Exception("Incorrect password"))
        }

        // Password is correct, note will be unmarked in the UI layer
        return Result.success(true)
    }

    /**
     * Generate a recovery phrase
     *
     * @return The recovery phrase (12 words)
     */
    suspend fun generateRecoveryPhrase(): String {
        return passwordRepo.generateRecoveryPhrase()
    }

    /**
     * Check if a recovery phrase exists
     */
    suspend fun hasRecoveryPhrase(): Boolean {
        return passwordRepo.hasRecoveryPhrase()
    }

    /**
     * Recover access using recovery phrase
     *
     * @param recoveryPhrase The recovery phrase
     * @param newPassword New password to set
     * @param confirmPassword Confirmation of new password
     * @return Result with success boolean or error message
     */
    suspend fun recoverWithPhrase(
        recoveryPhrase: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Boolean> {
        if (newPassword != confirmPassword) {
            return Result.failure(Exception("Passwords do not match"))
        }

        if (newPassword.length < 8) {
            return Result.failure(Exception("Password must be at least 8 characters"))
        }

        val success = passwordRepo.recoverWithPhrase(
            recoveryPhrase.trim(),
            newPassword.toCharArray()
        )

        if (success) {
            _state.value = PrivateFolderState.Unlocked
            return Result.success(true)
        }

        return Result.failure(Exception("Invalid recovery phrase"))
    }

    /**
     * Check if the folder is currently unlocked
     */
    fun isUnlocked(): Boolean {
        return _state.value is PrivateFolderState.Unlocked
    }

    /**
     * Reset the entire private folder system (WARNING: irreversible!)
     */
    suspend fun resetAll() {
        passwordRepo.clearAll()
        _state.value = PrivateFolderState.Uninitialized
    }
}

// Backward compatibility alias
@Deprecated("Use PrivateFolderManager instead", ReplaceWith("PrivateFolderManager"))
typealias HiddenFolderManager = PrivateFolderManager
