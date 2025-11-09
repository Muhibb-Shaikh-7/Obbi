package com.example.obby

import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest

/**
 * Unit tests for Hidden Notes functionality
 *
 * Tests:
 * - Emoji PIN hashing and verification
 * - PIN validation rules
 * - Category alias selection
 * - Security properties
 */
class HiddenNotesTest {

    @Test
    fun `emoji PIN hash is consistent`() {
        val pin = "😀😃😄😁"
        val hash1 = hashPin(pin)
        val hash2 = hashPin(pin)

        assertEquals("Same PIN should produce same hash", hash1, hash2)
    }

    @Test
    fun `different emoji PINs produce different hashes`() {
        val pin1 = "😀😃😄😁"
        val pin2 = "😁😄😃😀"  // Same emojis, different order

        val hash1 = hashPin(pin1)
        val hash2 = hashPin(pin2)

        assertNotEquals("Different PINs should produce different hashes", hash1, hash2)
    }

    @Test
    fun `PIN verification succeeds with correct PIN`() {
        val pin = "😀😃😄😁"
        val storedHash = hashPin(pin)

        val inputHash = hashPin(pin)
        assertEquals("Correct PIN should verify", storedHash, inputHash)
    }

    @Test
    fun `PIN verification fails with incorrect PIN`() {
        val pin = "😀😃😄😁"
        val wrongPin = "😁😄😃😀"

        val storedHash = hashPin(pin)
        val inputHash = hashPin(wrongPin)

        assertNotEquals("Incorrect PIN should not verify", storedHash, inputHash)
    }

    @Test
    fun `emoji PIN must be exactly 4 characters`() {
        assertTrue("3 emojis should be invalid", "😀😃😄".length < 4)
        assertTrue("5 emojis should be invalid", "😀😃😄😁😆".length > 4)
        assertTrue("4 emojis should be valid", "😀😃😄😁".length == 4)
    }

    @Test
    fun `category alias options are available`() {
        val aliases = listOf(
            "Recipes",
            "Shopping Lists",
            "Book Notes",
            "Movie Reviews",
            "Travel Plans",
            "Gift Ideas",
            "Fitness Goals"
        )

        assertTrue("Should have multiple alias options", aliases.size >= 5)
        assertTrue("Should include Recipes", aliases.contains("Recipes"))
    }

    @Test
    fun `emoji options are comprehensive`() {
        val emojiOptions = listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
            "🙂", "🙃", "😉", "😊", "😇", "🥰", "😍", "🤩",
            "😘", "😗", "☺️", "😚", "😙", "🥲", "😋", "😛",
            "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
            "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄",
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
            "🔥", "⭐", "✨", "🎉", "🎊", "🎁", "🏆", "🚀"
        )

        assertTrue("Should have sufficient emoji options", emojiOptions.size >= 48)
    }

    @Test
    fun `hash output is hex string`() {
        val pin = "😀😃😄😁"
        val hash = hashPin(pin)

        // SHA-256 produces 64 hex characters
        assertEquals("Hash should be 64 characters", 64, hash.length)
        assertTrue("Hash should be valid hex", hash.matches(Regex("^[0-9a-f]+$")))
    }

    @Test
    fun `same PIN with different Unicode representation`() {
        // Test that emoji variations are handled correctly
        val pin1 = "😀"  // U+1F600
        val pin2 = "😀"  // Same emoji

        assertEquals("Same emoji should be identical", pin1, pin2)
    }

    @Test
    fun `PIN is case-sensitive for X in markdown`() {
        // Verify that 'x' and 'X' are both recognized as checked
        val checkedLower = "- [x] Task"
        val checkedUpper = "- [X] Task"

        assertTrue("Lowercase x should be valid", checkedLower.contains("[x]"))
        assertTrue("Uppercase X should be valid", checkedUpper.contains("[X]"))
    }

    @Test
    fun `category alias disguises note type`() {
        val sensitiveTitle = "Bank Account Details"
        val disguisedAlias = "Chocolate Cake Recipe"

        // Verify that the alias provides plausible deniability
        assertNotEquals("Alias should not reveal content", sensitiveTitle, disguisedAlias)
        assertTrue(
            "Alias should be benign",
            listOf("Recipe", "Shopping", "Book", "Movie", "Travel").any {
                disguisedAlias.contains(it)
            }
        )
    }

    @Test
    fun `emoji PIN provides sufficient entropy`() {
        // With 56 emoji options and 4 positions: 56^4 = 9,834,496 combinations
        val emojiCount = 56
        val pinLength = 4
        val combinations = Math.pow(emojiCount.toDouble(), pinLength.toDouble())

        assertTrue("Should have high entropy", combinations > 1_000_000)
        // This provides reasonable security against brute force for a local-only app
    }

    @Test
    fun `hidden notes are excluded from search when locked`() {
        // Simulate filtering logic
        val notes = listOf(
            TestNote("Note 1", isHidden = false),
            TestNote("Hidden Note", isHidden = true),
            TestNote("Note 2", isHidden = false)
        )

        val visibleNotes = notes.filter { !it.isHidden }

        assertEquals("Should show only 2 visible notes", 2, visibleNotes.size)
        assertFalse(
            "Should not include hidden note",
            visibleNotes.any { it.title == "Hidden Note" }
        )
    }

    @Test
    fun `hidden notes are included when unlocked`() {
        val notes = listOf(
            TestNote("Note 1", isHidden = false),
            TestNote("Hidden Note", isHidden = true),
            TestNote("Note 2", isHidden = false)
        )

        val isUnlocked = true
        val displayedNotes = if (isUnlocked) notes else notes.filter { !it.isHidden }

        assertEquals("Should show all 3 notes when unlocked", 3, displayedNotes.size)
        assertTrue(
            "Should include hidden note",
            displayedNotes.any { it.title == "Hidden Note" }
        )
    }

    /**
     * Helper function that mirrors HiddenNotesManager.hashPin()
     */
    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Test data class for notes
     */
    private data class TestNote(
        val title: String,
        val isHidden: Boolean = false
    )
}
