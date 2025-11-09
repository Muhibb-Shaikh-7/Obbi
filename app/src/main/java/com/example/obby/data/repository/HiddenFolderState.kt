package com.example.obby.data.repository

/**
 * Represents the current state of the private folder system
 */
sealed interface PrivateFolderState {
    /**
     * No password has been set up yet
     */
    object Uninitialized : PrivateFolderState

    /**
     * Password is set but folder is currently locked
     */
    object Locked : PrivateFolderState

    /**
     * User is attempting to unlock (tracks failed attempts)
     */
    data class Unlocking(
        val failedAttempts: Int,
        val lockedOutUntil: Long? = null
    ) : PrivateFolderState

    /**
     * Folder is unlocked and accessible
     */
    object Unlocked : PrivateFolderState

    /**
     * Temporarily locked out due to too many failed attempts
     */
    data class LockedOut(
        val until: Long,
        val attemptCount: Int
    ) : PrivateFolderState
}

// Backward compatibility alias
@Deprecated("Use PrivateFolderState instead", ReplaceWith("PrivateFolderState"))
typealias HiddenFolderState = PrivateFolderState
