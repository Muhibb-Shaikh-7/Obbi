package com.example.obby.util

import android.content.Context
import com.example.obby.data.local.entity.Note
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object FileExporter {

    fun exportNoteToMarkdown(note: Note, directory: File): File {
        val fileName = sanitizeFileName(note.title) + ".md"
        val file = File(directory, fileName)

        val metadata = buildString {
            appendLine("---")
            appendLine("title: ${note.title}")
            appendLine("created: ${note.createdAt}")
            appendLine("modified: ${note.modifiedAt}")
            if (note.isPinned) appendLine("pinned: true")
            if (note.isFavorite) appendLine("favorite: true")
            appendLine("---")
            appendLine()
        }

        file.writeText(metadata + note.content)
        return file
    }

    fun exportAllNotesAsZip(notes: List<Note>, outputFile: File) {
        ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
            notes.forEach { note ->
                val content = buildString {
                    appendLine("---")
                    appendLine("title: ${note.title}")
                    appendLine("created: ${note.createdAt}")
                    appendLine("modified: ${note.modifiedAt}")
                    appendLine("---")
                    appendLine()
                    append(note.content)
                }

                val fileName = sanitizeFileName(note.title) + ".md"
                val entry = ZipEntry(fileName)
                zipOut.putNextEntry(entry)
                zipOut.write(content.toByteArray())
                zipOut.closeEntry()
            }
        }
    }

    fun importMarkdownFile(file: File): Pair<String, String> {
        val content = file.readText()
        val title = extractTitleFromMarkdown(content, file.nameWithoutExtension)
        val cleanContent = removeMetadataFromMarkdown(content)
        return Pair(title, cleanContent)
    }

    fun importNotesFromZip(zipFile: File): List<Pair<String, String>> {
        val notes = mutableListOf<Pair<String, String>>()

        ZipInputStream(FileInputStream(zipFile)).use { zipIn ->
            var entry: ZipEntry? = zipIn.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".md")) {
                    val content = zipIn.readBytes().toString(Charsets.UTF_8)
                    val title =
                        extractTitleFromMarkdown(content, File(entry.name).nameWithoutExtension)
                    val cleanContent = removeMetadataFromMarkdown(content)
                    notes.add(Pair(title, cleanContent))
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }

        return notes
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9.-]"), "_")
            .take(100)
    }

    private fun extractTitleFromMarkdown(content: String, fallback: String): String {
        val titleRegex = """^---\s*\ntitle:\s*(.+)\s*\n""".toRegex(RegexOption.MULTILINE)
        return titleRegex.find(content)?.groupValues?.get(1)?.trim() ?: fallback
    }

    private fun removeMetadataFromMarkdown(content: String): String {
        val metadataRegex = """^---\s*\n.*?\n---\s*\n+""".toRegex(RegexOption.DOT_MATCHES_ALL)
        return content.replace(metadataRegex, "")
    }
}
