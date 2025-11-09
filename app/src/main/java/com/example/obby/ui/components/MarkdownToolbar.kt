package com.example.obby.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

data class MarkdownAction(
    val icon: ImageVector,
    val label: String,
    val action: MarkdownActionType
)

sealed class MarkdownActionType {
    object Bold : MarkdownActionType()
    object Italic : MarkdownActionType()
    object Strikethrough : MarkdownActionType()
    object Code : MarkdownActionType()
    data class Heading(val level: Int) : MarkdownActionType()
    object BulletList : MarkdownActionType()
    object NumberedList : MarkdownActionType()
    object Checklist : MarkdownActionType()
    object Quote : MarkdownActionType()
    object Link : MarkdownActionType()
    object CodeBlock : MarkdownActionType()
    object Table : MarkdownActionType()
    object HorizontalRule : MarkdownActionType()
    object Help : MarkdownActionType()
}

@Composable
fun MarkdownToolbar(
    onAction: (MarkdownActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    var showHeadingMenu by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bold
            IconButton(onClick = { onAction(MarkdownActionType.Bold) }) {
                Icon(
                    Icons.Default.FormatBold,
                    contentDescription = "Bold",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Italic
            IconButton(onClick = { onAction(MarkdownActionType.Italic) }) {
                Icon(
                    Icons.Default.FormatItalic,
                    contentDescription = "Italic",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Strikethrough
            IconButton(onClick = { onAction(MarkdownActionType.Strikethrough) }) {
                Icon(
                    Icons.Default.FormatStrikethrough,
                    contentDescription = "Strikethrough",
                    modifier = Modifier.size(20.dp)
                )
            }

            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
            )

            // Headings menu
            Box {
                IconButton(onClick = { showHeadingMenu = true }) {
                    Icon(
                        Icons.Default.Title,
                        contentDescription = "Heading",
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showHeadingMenu,
                    onDismissRequest = { showHeadingMenu = false }
                ) {
                    (1..6).forEach { level ->
                        DropdownMenuItem(
                            text = { Text("Heading $level") },
                            onClick = {
                                onAction(MarkdownActionType.Heading(level))
                                showHeadingMenu = false
                            }
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
            )

            // Lists
            IconButton(onClick = { onAction(MarkdownActionType.BulletList) }) {
                Icon(
                    Icons.Default.FormatListBulleted,
                    contentDescription = "Bullet List",
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = { onAction(MarkdownActionType.NumberedList) }) {
                Icon(
                    Icons.Default.FormatListNumbered,
                    contentDescription = "Numbered List",
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = { onAction(MarkdownActionType.Checklist) }) {
                Icon(
                    Icons.Default.CheckBox,
                    contentDescription = "Checklist",
                    modifier = Modifier.size(20.dp)
                )
            }

            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
            )

            // Quote
            IconButton(onClick = { onAction(MarkdownActionType.Quote) }) {
                Icon(
                    Icons.Default.FormatQuote,
                    contentDescription = "Quote",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Code
            IconButton(onClick = { onAction(MarkdownActionType.Code) }) {
                Icon(
                    Icons.Default.Code,
                    contentDescription = "Inline Code",
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = { onAction(MarkdownActionType.CodeBlock) }) {
                Icon(
                    Icons.Default.DataObject,
                    contentDescription = "Code Block",
                    modifier = Modifier.size(20.dp)
                )
            }

            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
            )

            // Link
            IconButton(onClick = { onAction(MarkdownActionType.Link) }) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = "Link",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Table
            IconButton(onClick = { onAction(MarkdownActionType.Table) }) {
                Icon(
                    Icons.Default.TableChart,
                    contentDescription = "Table",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Horizontal Rule
            IconButton(onClick = { onAction(MarkdownActionType.HorizontalRule) }) {
                Icon(
                    Icons.Default.HorizontalRule,
                    contentDescription = "Horizontal Rule",
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Help
            IconButton(onClick = { showHelpDialog = true }) {
                Icon(
                    Icons.Default.Help,
                    contentDescription = "Markdown Help",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showHelpDialog) {
        MarkdownCheatSheet(onDismiss = { showHelpDialog = false })
    }
}

@Composable
fun MarkdownCheatSheet(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Markdown Cheat Sheet") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                CheatSheetItem("Bold", "**text** or __text__")
                CheatSheetItem("Italic", "*text* or _text_")
                CheatSheetItem("Strikethrough", "~~text~~")
                CheatSheetItem("Heading 1", "# Heading")
                CheatSheetItem("Heading 2", "## Heading")
                CheatSheetItem("Bullet List", "- Item or * Item")
                CheatSheetItem("Numbered List", "1. Item")
                CheatSheetItem("Checklist", "- [ ] Unchecked\n- [x] Checked")
                CheatSheetItem("Quote", "> Quote text")
                CheatSheetItem("Inline Code", "`code`")
                CheatSheetItem("Code Block", "```\ncode\n```")
                CheatSheetItem("Link", "[text](url)")
                CheatSheetItem("Wiki Link", "[[Note Title]]")
                CheatSheetItem("Image", "![alt](url)")
                CheatSheetItem("Table", "| Col 1 | Col 2 |\n|-------|-------|\n| Data  | Data  |")
                CheatSheetItem("Horizontal Rule", "---")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
private fun CheatSheetItem(title: String, syntax: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = syntax,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

// Markdown formatting helper functions
object MarkdownFormatter {

    fun applyFormat(
        currentText: String,
        selection: TextRange,
        actionType: MarkdownActionType
    ): Pair<String, TextRange> {
        val start = selection.start
        val end = selection.end
        val selectedText = currentText.substring(start, end)

        return when (actionType) {
            is MarkdownActionType.Bold -> {
                wrapText(currentText, start, end, "**", "**")
            }

            is MarkdownActionType.Italic -> {
                wrapText(currentText, start, end, "*", "*")
            }

            is MarkdownActionType.Strikethrough -> {
                wrapText(currentText, start, end, "~~", "~~")
            }

            is MarkdownActionType.Code -> {
                wrapText(currentText, start, end, "`", "`")
            }

            is MarkdownActionType.Heading -> {
                val prefix = "#".repeat(actionType.level) + " "
                insertAtLineStart(currentText, start, prefix)
            }

            is MarkdownActionType.BulletList -> {
                insertAtLineStart(currentText, start, "- ")
            }

            is MarkdownActionType.NumberedList -> {
                insertAtLineStart(currentText, start, "1. ")
            }

            is MarkdownActionType.Checklist -> {
                insertAtLineStart(currentText, start, "- [ ] ")
            }

            is MarkdownActionType.Quote -> {
                insertAtLineStart(currentText, start, "> ")
            }

            is MarkdownActionType.Link -> {
                if (selectedText.isEmpty()) {
                    val linkText = "[Link Text](url)"
                    val newText =
                        currentText.substring(0, start) + linkText + currentText.substring(end)
                    newText to TextRange(start + 1, start + 9) // Select "Link Text"
                } else {
                    val linkText = "[$selectedText](url)"
                    val newText =
                        currentText.substring(0, start) + linkText + currentText.substring(end)
                    newText to TextRange(
                        start + selectedText.length + 3,
                        start + selectedText.length + 6
                    ) // Select "url"
                }
            }

            is MarkdownActionType.CodeBlock -> {
                val codeBlock = if (selectedText.isEmpty()) {
                    "```\ncode\n```"
                } else {
                    "```\n$selectedText\n```"
                }
                val newText =
                    currentText.substring(0, start) + codeBlock + currentText.substring(end)
                newText to TextRange(
                    start + 4,
                    start + 4 + if (selectedText.isEmpty()) 4 else selectedText.length
                )
            }

            is MarkdownActionType.Table -> {
                val table =
                    "| Column 1 | Column 2 |\n|----------|----------|\n| Data 1   | Data 2   |"
                val newText = currentText.substring(0, start) + table + currentText.substring(end)
                newText to TextRange(start + table.length)
            }

            is MarkdownActionType.HorizontalRule -> {
                val rule = "\n---\n"
                val newText = currentText.substring(0, start) + rule + currentText.substring(end)
                newText to TextRange(start + rule.length)
            }

            else -> currentText to selection
        }
    }

    /**
     * Handle Enter key press with Markdown auto-continuation
     *
     * @param currentText The current text content
     * @param cursorPosition The current cursor position
     * @return Pair of new text and new cursor position
     */
    fun handleEnterKey(
        currentText: String,
        cursorPosition: Int
    ): Pair<String, Int> {
        // Find the start and end of the current line
        val lineStart = currentText.lastIndexOf('\n', cursorPosition - 1) + 1
        val lineEnd = currentText.indexOf('\n', cursorPosition).let {
            if (it == -1) currentText.length else it
        }
        val currentLine = currentText.substring(lineStart, lineEnd)

        // Get indentation
        val indent = currentLine.takeWhile { it == ' ' || it == '\t' }
        val contentAfterIndent = currentLine.substring(indent.length)

        // Check for various list patterns
        val nextLinePrefix = getNextLinePrefix(contentAfterIndent, indent)

        if (nextLinePrefix == null) {
            // No special handling, just insert newline
            val newText = currentText.substring(0, cursorPosition) + "\n" + currentText.substring(
                cursorPosition
            )
            return newText to (cursorPosition + 1)
        } else if (nextLinePrefix.isEmpty()) {
            // Empty list item - remove the prefix and insert plain newline
            // Find the position of the list marker
            val contentStartInLine = lineStart + indent.length
            val contentEndInLine = when {
                contentAfterIndent.startsWith("- [ ] ") || contentAfterIndent.startsWith("- [x] ") -> contentStartInLine + 6
                contentAfterIndent.startsWith("- ") || contentAfterIndent.startsWith("* ") -> contentStartInLine + 2
                else -> {
                    val numMatch = Regex("""^(\d+)\.\s""").find(contentAfterIndent)
                    if (numMatch != null) contentStartInLine + numMatch.value.length else contentStartInLine
                }
            }

            // Remove the list marker and insert newline
            val newText = currentText.substring(0, contentStartInLine) +
                    currentText.substring(contentEndInLine)
            return newText to contentStartInLine
        } else {
            // Insert newline with continuation prefix
            val newText = currentText.substring(
                0,
                cursorPosition
            ) + "\n" + nextLinePrefix + currentText.substring(cursorPosition)
            return newText to (cursorPosition + 1 + nextLinePrefix.length)
        }
    }

    /**
     * Determine the prefix for the next line based on the current line content
     *
     * @param contentAfterIndent The current line content after indentation
     * @param indent The indentation string
     * @return The prefix for next line, null if no special handling, empty string to exit list
     */
    private fun getNextLinePrefix(contentAfterIndent: String, indent: String): String? {
        // Check for empty prefixed lines (exit list)
        when {
            contentAfterIndent == "- " || contentAfterIndent == "* " -> return ""
            contentAfterIndent.equals("- [ ] ", ignoreCase = true) ||
                    contentAfterIndent.equals("- [x] ", ignoreCase = true) -> return ""
        }

        val numberMatch = Regex("""^(\d+)\.\s$""").matchEntire(contentAfterIndent)
        if (numberMatch != null) return ""

        // Check for checkbox continuation
        val checkboxMatch = Regex("""^-\s\[([ xX])\]\s""").find(contentAfterIndent)
        if (checkboxMatch != null) {
            return "$indent- [ ] "
        }

        // Check for bullet list continuation
        if (contentAfterIndent.startsWith("- ")) {
            return "$indent- "
        }
        if (contentAfterIndent.startsWith("* ")) {
            return "$indent* "
        }

        // Check for numbered list continuation
        val numberedMatch = Regex("""^(\d+)\.\s""").find(contentAfterIndent)
        if (numberedMatch != null) {
            val currentNumber = numberedMatch.groupValues[1].toInt()
            return "$indent${currentNumber + 1}. "
        }

        // No special pattern found
        return null
    }

    private fun wrapText(
        text: String,
        start: Int,
        end: Int,
        prefix: String,
        suffix: String
    ): Pair<String, TextRange> {
        val before = text.substring(0, start)
        val selected = text.substring(start, end)
        val after = text.substring(end)

        val newText = "$before$prefix$selected$suffix$after"
        val newSelection = if (selected.isEmpty()) {
            TextRange(start + prefix.length)
        } else {
            TextRange(start + prefix.length, end + prefix.length)
        }

        return newText to newSelection
    }

    private fun insertAtLineStart(
        text: String,
        cursorPosition: Int,
        prefix: String
    ): Pair<String, TextRange> {
        // Find the start of the current line
        val lineStart = text.lastIndexOf('\n', cursorPosition - 1) + 1

        val before = text.substring(0, lineStart)
        val after = text.substring(lineStart)

        val newText = "$before$prefix$after"
        val newCursorPosition = lineStart + prefix.length + (cursorPosition - lineStart)

        return newText to TextRange(newCursorPosition)
    }
}
