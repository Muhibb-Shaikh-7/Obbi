package com.example.obby

import com.example.obby.util.PasswordHasher
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for PasswordHasher
 *
 * Verifies correct password hashing, verification, and security properties
 */
class PasswordHasherTest {

    @Test
    fun `generateSalt creates 32-byte salt`() {
        val salt = PasswordHasher.generateSalt()
        assertEquals(32, salt.size)
    }

    @Test
    fun `generateSalt creates unique salts`() {
        val salt1 = PasswordHasher.generateSalt()
        val salt2 = PasswordHasher.generateSalt()

        assertFalse("Salts should be unique", salt1.contentEquals(salt2))
    }

    @Test
    fun `hash produces consistent output for same input`() {
        val password = "TestPassword123!".toCharArray()
        val salt = PasswordHasher.generateSalt()

        val hash1 = PasswordHasher.hash(password.copyOf(), salt)
        val hash2 = PasswordHasher.hash(password.copyOf(), salt)

        assertArrayEquals("Same password and salt should produce same hash", hash1, hash2)
    }

    @Test
    fun `hash produces different output for different salts`() {
        val password = "TestPassword123!".toCharArray()
        val salt1 = PasswordHasher.generateSalt()
        val salt2 = PasswordHasher.generateSalt()

        val hash1 = PasswordHasher.hash(password.copyOf(), salt1)
        val hash2 = PasswordHasher.hash(password.copyOf(), salt2)

        assertFalse(
            "Different salts should produce different hashes",
            hash1.contentEquals(hash2)
        )
    }

    @Test
    fun `hash produces different output for different passwords`() {
        val password1 = "TestPassword1".toCharArray()
        val password2 = "TestPassword2".toCharArray()
        val salt = PasswordHasher.generateSalt()

        val hash1 = PasswordHasher.hash(password1, salt)
        val hash2 = PasswordHasher.hash(password2, salt)

        assertFalse(
            "Different passwords should produce different hashes",
            hash1.contentEquals(hash2)
        )
    }

    @Test
    fun `verify returns true for correct password`() {
        val password = "CorrectPassword123!".toCharArray()
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password.copyOf(), salt)

        val result = PasswordHasher.verify(password.copyOf(), salt, hash)

        assertTrue("Correct password should verify", result)
    }

    @Test
    fun `verify returns false for incorrect password`() {
        val correctPassword = "CorrectPassword123!".toCharArray()
        val wrongPassword = "WrongPassword456!".toCharArray()
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(correctPassword, salt)

        val result = PasswordHasher.verify(wrongPassword, salt, hash)

        assertFalse("Incorrect password should not verify", result)
    }

    @Test
    fun `verify returns false for incorrect salt`() {
        val password = "TestPassword123!".toCharArray()
        val salt1 = PasswordHasher.generateSalt()
        val salt2 = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password.copyOf(), salt1)

        val result = PasswordHasher.verify(password.copyOf(), salt2, hash)

        assertFalse("Wrong salt should not verify", result)
    }

    @Test
    fun `encodeToString and decodeFromString are inverses`() {
        val original = ByteArray(32) { it.toByte() }

        val encoded = PasswordHasher.encodeToString(original)
        val decoded = PasswordHasher.decodeFromString(encoded)

        assertArrayEquals("Encode then decode should return original", original, decoded)
    }

    @Test
    fun `checkPasswordComplexity rejects short passwords`() {
        val shortPassword = "Short1!"

        val (isValid, errorMessage) = PasswordHasher.checkPasswordComplexity(shortPassword)

        assertFalse("Short password should be rejected", isValid)
        assertNotNull("Should have error message", errorMessage)
        assertTrue(
            "Error should mention length",
            errorMessage!!.contains("8 characters", ignoreCase = true)
        )
    }

    @Test
    fun `checkPasswordComplexity rejects all-digit passwords`() {
        val allDigits = "12345678"

        val (isValid, errorMessage) = PasswordHasher.checkPasswordComplexity(allDigits)

        assertFalse("All-digit password should be rejected", isValid)
        assertNotNull("Should have error message", errorMessage)
        assertTrue(
            "Error should mention digits",
            errorMessage!!.contains("all numbers", ignoreCase = true)
        )
    }

    @Test
    fun `checkPasswordComplexity rejects all-lowercase passwords`() {
        val allLowercase = "abcdefgh"

        val (isValid, errorMessage) = PasswordHasher.checkPasswordComplexity(allLowercase)

        assertFalse("All-lowercase password should be rejected", isValid)
        assertNotNull("Should have error message", errorMessage)
    }

    @Test
    fun `checkPasswordComplexity accepts strong passwords`() {
        val strongPasswords = listOf(
            "MyPassword123!",
            "Str0ng!Pass",
            "C0mpl3x@Pwd",
            "SecureP@ssw0rd"
        )

        strongPasswords.forEach { password ->
            val (isValid, errorMessage) = PasswordHasher.checkPasswordComplexity(password)
            assertTrue("Strong password should be accepted: $password", isValid)
            assertNull("Should have no error message", errorMessage)
        }
    }

    @Test
    fun `calculatePasswordStrength returns 0 for empty password`() {
        val strength = PasswordHasher.calculatePasswordStrength("")
        assertEquals(0, strength)
    }

    @Test
    fun `calculatePasswordStrength increases with length`() {
        val short = PasswordHasher.calculatePasswordStrength("Pass1!")
        val medium = PasswordHasher.calculatePasswordStrength("Password1!")
        val long = PasswordHasher.calculatePasswordStrength("LongPassword123!")

        assertTrue("Longer password should be stronger", short < medium)
        assertTrue("Longer password should be stronger", medium < long)
    }

    @Test
    fun `calculatePasswordStrength increases with character variety`() {
        val onlyLower = PasswordHasher.calculatePasswordStrength("abcdefgh")
        val withUpper = PasswordHasher.calculatePasswordStrength("abcdEFGH")
        val withDigit = PasswordHasher.calculatePasswordStrength("abcd1234")
        val withSymbol = PasswordHasher.calculatePasswordStrength("abcd!@#$")
        val allTypes = PasswordHasher.calculatePasswordStrength("Abc123!@")

        assertTrue("Adding uppercase should increase strength", withUpper > onlyLower)
        assertTrue("Adding digits should increase strength", withDigit > onlyLower)
        assertTrue("Adding symbols should increase strength", withSymbol > onlyLower)
        assertTrue(
            "All character types should be strongest",
            allTypes > maxOf(withUpper, withDigit, withSymbol)
        )
    }

    @Test
    fun `calculatePasswordStrength returns value between 0 and 100`() {
        val passwords = listOf(
            "",
            "weak",
            "Medium1!",
            "VeryStr0ng!P@ssw0rd",
            "x".repeat(100)
        )

        passwords.forEach { password ->
            val strength = PasswordHasher.calculatePasswordStrength(password)
            assertTrue("Strength should be >= 0: $password", strength >= 0)
            assertTrue("Strength should be <= 100: $password", strength <= 100)
        }
    }

    @Test
    fun `hash output is 32 bytes (256 bits)`() {
        val password = "TestPassword123!".toCharArray()
        val salt = PasswordHasher.generateSalt()

        val hash = PasswordHasher.hash(password, salt)

        assertEquals("Hash should be 32 bytes (256 bits)", 32, hash.size)
    }

    @Test
    fun `verify handles exceptions gracefully`() {
        val password = "Test123!".toCharArray()
        val invalidSalt = ByteArray(0) // Invalid salt
        val hash = ByteArray(32)

        // Should not throw exception
        val result = PasswordHasher.verify(password, invalidSalt, hash)

        assertFalse("Invalid input should return false", result)
    }

    @Test
    fun `password variations produce different hashes`() {
        val salt = PasswordHasher.generateSalt()

        // Similar passwords should produce different hashes
        val variations = listOf(
            "Password123!",
            "password123!",
            "PASSWORD123!",
            "Password123",
            "Password1234!"
        )

        val hashes = variations.map { password ->
            PasswordHasher.hash(password.toCharArray(), salt)
        }

        // All hashes should be unique
        for (i in hashes.indices) {
            for (j in i + 1 until hashes.size) {
                assertFalse(
                    "Password variation '${variations[i]}' vs '${variations[j]}' " +
                            "should produce different hashes",
                    hashes[i].contentEquals(hashes[j])
                )
            }
        }
    }
}
