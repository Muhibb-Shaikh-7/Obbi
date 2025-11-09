package com.example.obby.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.hiddenNotesDataStore: DataStore<Preferences> by preferencesDataStore(name = "hidden_notes_prefs")

/**
 * Manager for hidden notes functionality with various unlock mechanisms
 */
class HiddenNotesManager(private val context: Context) {

    private val EMOJI_PIN_KEY = stringPreferencesKey("emoji_pin_hash")
    private val UNLOCK_METHOD_KEY = stringPreferencesKey("unlock_method")
    private val IS_UNLOCKED_KEY = booleanPreferencesKey("is_unlocked")
    private val CATEGORY_ALIAS_KEY = stringPreferencesKey("category_alias")

    enum class UnlockMethod {
        EMOJI_PIN,      // Default: 4-emoji sequence
        KNOCK_CODE,     // 2x2 grid tap sequence
        LONG_PRESS,     // Long-press with haptic
        NONE            // No hidden notes configured
    }

    /**
     * Check if hidden notes feature is configured
     */
    val isConfigured: Flow<Boolean> = context.hiddenNotesDataStore.data.map { preferences ->
        preferences[EMOJI_PIN_KEY] != null
    }

    /**
     * Check if hidden notes are currently unlocked
     */
    val isUnlocked: Flow<Boolean> = context.hiddenNotesDataStore.data.map { preferences ->
        preferences[IS_UNLOCKED_KEY] ?: false
    }

    /**
     * Get the current unlock method
     */
    val unlockMethod: Flow<UnlockMethod> = context.hiddenNotesDataStore.data.map { preferences ->
        val method = preferences[UNLOCK_METHOD_KEY] ?: UnlockMethod.NONE.name
        try {
            UnlockMethod.valueOf(method)
        } catch (e: IllegalArgumentException) {
            UnlockMethod.NONE
        }
    }

    /**
     * Get the category alias for disguised notes
     */
    val categoryAlias: Flow<String> = context.hiddenNotesDataStore.data.map { preferences ->
        preferences[CATEGORY_ALIAS_KEY] ?: "Recipes"
    }

    /**
     * Set up hidden notes with emoji PIN
     */
    suspend fun setupEmojiPin(emojiSequence: String, alias: String = "Recipes") {
        val hash = hashPin(emojiSequence)
        context.hiddenNotesDataStore.edit { preferences ->
            preferences[EMOJI_PIN_KEY] = hash
            preferences[UNLOCK_METHOD_KEY] = UnlockMethod.EMOJI_PIN.name
            preferences[CATEGORY_ALIAS_KEY] = alias
            preferences[IS_UNLOCKED_KEY] = false
        }
    }

    /**
     * Verify emoji PIN and unlock if correct
     */
    suspend fun verifyAndUnlock(emojiSequence: String): Boolean {
        val hash = hashPin(emojiSequence)
        val dataStore = context.hiddenNotesDataStore

        var isValid = false
        dataStore.data.collect { preferences ->
            val storedHash = preferences[EMOJI_PIN_KEY]
            isValid = storedHash == hash
        }

        if (isValid) {
            dataStore.edit { preferences ->
                preferences[IS_UNLOCKED_KEY] = true
            }
        }

        return isValid
    }

    /**
     * Lock hidden notes (quick hide)
     */
    suspend fun lock() {
        context.hiddenNotesDataStore.edit { preferences ->
            preferences[IS_UNLOCKED_KEY] = false
        }
    }

    /**
     * Reset hidden notes configuration
     */
    suspend fun reset() {
        context.hiddenNotesDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Change the category alias
     */
    suspend fun updateCategoryAlias(newAlias: String) {
        context.hiddenNotesDataStore.edit { preferences ->
            preferences[CATEGORY_ALIAS_KEY] = newAlias
        }
    }

    /**
     * Hash the PIN for secure storage
     */
    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        /**
         * Predefined category aliases for plausible deniability
         */
        val CATEGORY_ALIASES = listOf(
            "Recipes",
            "Shopping Lists",
            "Book Notes",
            "Movie Reviews",
            "Travel Plans",
            "Gift Ideas",
            "Fitness Goals"
        )

        /**
         * Popular emoji options for PIN
         */
        val EMOJI_OPTIONS = listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
            "🙂", "🙃", "😉", "😊", "😇", "🥰", "😍", "🤩",
            "😘", "😗", "☺️", "😚", "😙", "🥲", "😋", "😛",
            "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
            "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄",
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
            "🔥", "⭐", "✨", "🎉", "🎊", "🎁", "🏆", "🚀"
        )
    }
}
