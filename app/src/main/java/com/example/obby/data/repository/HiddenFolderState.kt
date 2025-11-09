package com.example.obby.data.repository

/**
 * Represents the current state of the hidden folder system
 */
sealed interface HiddenFolderState {
    /**
     * No password has been set up yet
     */
    object Uninitialized : HiddenFolderState

    /**
     * Password is set but folder is currently locked
     */
    object Locked : HiddenFolderState

    /**
     * User is attempting to unlock (tracks failed attempts)
     */
    data class Unlocking(
        val failedAttempts: Int,
        val lockedOutUntil: Long? = null
    ) : HiddenFolderState

    /**
     * Folder is unlocked and accessible
     */
    object Unlocked : HiddenFolderState

    /**
     * Temporarily locked out due to too many failed attempts
     */
    data class LockedOut(
        val until: Long,
        val attemptCount: Int
    ) : HiddenFolderState
}
