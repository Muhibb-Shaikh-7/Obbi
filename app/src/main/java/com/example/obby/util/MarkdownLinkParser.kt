package com.example.obby.util

object MarkdownLinkParser {

    // Matches [[Note Title]] or [[Note Title|Display Text]]
    private val wikiLinkRegex = """\[\[([^\]|]+)(?:\|([^\]]+))?\]\]""".toRegex()

    // Matches [Display Text](note-title) markdown links
    private val markdownLinkRegex = """\[([^\]]+)\]\(([^)]+)\)""".toRegex()

    data class ParsedLink(
        val linkText: String,
        val displayText: String,
        val startIndex: Int,
        val endIndex: Int
    )

    fun parseWikiLinks(content: String): List<ParsedLink> {
        return wikiLinkRegex.findAll(content).map { match ->
            val noteTitle = match.groupValues[1].trim()
            val displayText = match.groupValues.getOrNull(2)?.trim() ?: noteTitle
            ParsedLink(
                linkText = noteTitle,
                displayText = displayText,
                startIndex = match.range.first,
                endIndex = match.range.last + 1
            )
        }.toList()
    }

    fun parseMarkdownLinks(content: String): List<ParsedLink> {
        return markdownLinkRegex.findAll(content).map { match ->
            val displayText = match.groupValues[1].trim()
            val noteTitle = match.groupValues[2].trim()
            ParsedLink(
                linkText = noteTitle,
                displayText = displayText,
                startIndex = match.range.first,
                endIndex = match.range.last + 1
            )
        }.toList()
    }

    fun parseAllLinks(content: String): List<ParsedLink> {
        return parseWikiLinks(content) + parseMarkdownLinks(content)
    }

    fun extractTags(content: String): List<String> {
        val tagRegex = """(?:^|\s)#([a-zA-Z0-9_-]+)""".toRegex()
        return tagRegex.findAll(content)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }
}
