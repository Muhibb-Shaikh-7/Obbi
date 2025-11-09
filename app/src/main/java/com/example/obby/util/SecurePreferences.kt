package com.example.obby.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for sensitive data using Android's EncryptedSharedPreferences
 *
 * All data stored through this class is encrypted at rest using AES256_GCM
 */
class SecurePreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "hidden_folder_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Store a byte array securely (Base64 encoded)
     */
    fun putBytes(key: String, value: ByteArray) {
        preferences.edit()
            .putString(key, PasswordHasher.encodeToString(value))
            .apply()
    }

    /**
     * Retrieve a byte array
     */
    fun getBytes(key: String): ByteArray? {
        return preferences.getString(key, null)?.let {
            PasswordHasher.decodeFromString(it)
        }
    }

    /**
     * Store a string securely
     */
    fun putString(key: String, value: String) {
        preferences.edit()
            .putString(key, value)
            .apply()
    }

    /**
     * Retrieve a string
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return preferences.getString(key, defaultValue)
    }

    /**
     * Store an integer
     */
    fun putInt(key: String, value: Int) {
        preferences.edit()
            .putInt(key, value)
            .apply()
    }

    /**
     * Retrieve an integer
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return preferences.getInt(key, defaultValue)
    }

    /**
     * Store a long
     */
    fun putLong(key: String, value: Long) {
        preferences.edit()
            .putLong(key, value)
            .apply()
    }

    /**
     * Retrieve a long
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return preferences.getLong(key, defaultValue)
    }

    /**
     * Store a boolean
     */
    fun putBoolean(key: String, value: Boolean) {
        preferences.edit()
            .putBoolean(key, value)
            .apply()
    }

    /**
     * Retrieve a boolean
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    /**
     * Check if a key exists
     */
    fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    /**
     * Remove a key
     */
    fun remove(key: String) {
        preferences.edit()
            .remove(key)
            .apply()
    }

    /**
     * Clear all data
     */
    fun clear() {
        preferences.edit()
            .clear()
            .apply()
    }
}
