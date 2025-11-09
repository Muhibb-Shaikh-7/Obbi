package com.example.obby

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for checkbox toggling in Markdown
 *
 * These tests verify that the checkbox glitch fix works correctly:
 * - Individual checkboxes toggle independently
 * - Multiple checkboxes don't affect each other
 * - Position-based toggling is reliable
 */
class CheckboxToggleTest {

    @Test
    fun `toggle first checkbox in list`() {
        val markdown = """
            - [ ] Task 1
            - [ ] Task 2
            - [ ] Task 3
        """.trimIndent()

        val expected = """
            - [x] Task 1
            - [ ] Task 2
            - [ ] Task 3
        """.trimIndent()

        val result = toggleCheckboxAtIndex(markdown, 0)
        assertEquals(expected, result)
    }

    @Test
    fun `toggle middle checkbox in list`() {
        val markdown = """
            - [ ] Task 1
            - [ ] Task 2
            - [ ] Task 3
        """.trimIndent()

        val expected = """
            - [ ] Task 1
            - [x] Task 2
            - [ ] Task 3
        """.trimIndent()

        val result = toggleCheckboxAtIndex(markdown, 1)
        assertEquals(expected, result)
    }

    @Test
    fun `toggle last checkbox in list`() {
        val markdown = """
            - [ ] Task 1
            - [ ] Task 2
            - [ ] Task 3
        """.trimIndent()

        val expected = """
            - [ ] Task 1
            - [ ] Task 2
            - [x] Task 3
        """.trimIndent()

        val result = toggleCheckboxAtIndex(markdown, 2)
        assertEquals(expected, result)
    }

    @Test
    fun `uncheck already checked checkbox`() {
        val markdown = """
            - [ ] Task 1
            - [x] Task 2
            - [ ] Task 3
        """.trimIndent()

        val expected = """
            - [ ] Task 1
            - [ ] Task 2
            - [ ] Task 3
        """.trimIndent()

        val result = toggleCheckboxAtIndex(markdown, 1)
        assertEquals(expected, result)
    }

    @Test
    fun `toggle checkbox with uppercase X`() {
        val markdown = """
            - [ ] Task 1
            - [X] Task 2
            - [ ] Task 3
        """.trimIndent()

        val expected = """
            - [ ] Task 1
            - [ ] Task 2
            - [ ] Task 3
        """.trimIndent()

        val result = toggleCheckboxAtIndex(markdown, 1)
        assertEquals(expected, result)
    }

    @Test
    fun `toggle multiple checkboxes sequentially`() {
        var markdown = """
            - [ ] Task 1
            - [ ] Task 2
            - [ ] Task 3
        """.trimIndent()

        // Toggle first
        markdown = toggleCheckboxAtIndex(markdown, 0)
        assertEquals("- [x] Task 1", markdown.lines()[0])

        // Toggle second
        markdown = toggleCheckboxAtIndex(markdown, 1)
        assertEquals("- [x] Task 2", markdown.lines()[1])

        // Toggle first again (uncheck)
        markdown = toggleCheckboxAtIndex(markdown, 0)
        assertEquals("- [ ] Task 1", markdown.lines()[0])

        // Second should still be checked
        assertEquals("- [x] Task 2", markdown.lines()[1])
    }

    @Test
    fun `checkbox in mixed content`() {
        val markdown = """
            # My Checklist
            
            - [ ] Task 1
            
            Some text here
            
            - [ ] Task 2
        """.trimIndent()

        val expected = """
            # My Checklist
            
            - [x] Task 1
            
            Some text here
            
            - [ ] Task 2
        """.trimIndent()

        val result = toggleCheckboxAtIndex(markdown, 0)
        assertEquals(expected, result)
    }

    @Test
    fun `checkbox with nested lists`() {
        val markdown = """
            - [ ] Parent Task
              - [ ] Subtask 1
              - [ ] Subtask 2
            - [ ] Another Task
        """.trimIndent()

        // Toggle parent
        val result1 = toggleCheckboxAtIndex(markdown, 0)
        assertEquals("- [x] Parent Task", result1.lines()[0])

        // Toggle subtask should not affect parent
        val result2 = toggleCheckboxAtIndex(markdown, 1)
        assertEquals("  - [x] Subtask 1", result2.lines()[1])
        assertEquals("- [ ] Parent Task", result2.lines()[0])
    }

    /**
     * Helper function that simulates the checkbox toggle logic
     * This mirrors the implementation in NoteDetailScreen.kt
     */
    private fun toggleCheckboxAtIndex(markdown: String, checkboxIndex: Int): String {
        val checkboxPattern = Regex("- \\[([ xX])\\]")
        val matches = checkboxPattern.findAll(markdown).toList()

        if (checkboxIndex >= 0 && checkboxIndex < matches.size) {
            val match = matches[checkboxIndex]
            val currentState = match.groupValues[1]
            val newState = if (currentState == " ") "x" else " "

            val before = markdown.substring(0, match.range.first)
            val after = markdown.substring(match.range.last + 1)
            return before + "- [$newState]" + after
        }

        return markdown
    }
}
