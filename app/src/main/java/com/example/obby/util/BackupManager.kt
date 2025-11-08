package com.example.obby.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.example.obby.data.local.entity.Note
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {
    private const val TAG = "BackupManager"
    private const val BACKUP_PREFIX = "obby_backup_"
    private const val DATABASE_NAME = "obby_database"

    data class BackupResult(
        val success: Boolean,
        val message: String,
        val backupUri: Uri? = null
    )

    /**
     * Create a backup ZIP file containing all notes as markdown files and the database
     */
    suspend fun createBackup(
        context: Context,
        notes: List<Note>,
        destinationUri: Uri
    ): BackupResult {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "${BACKUP_PREFIX}${timestamp}.zip"

            // Get the DocumentFile for the destination directory
            val destinationDir = DocumentFile.fromTreeUri(context, destinationUri)
            if (destinationDir == null || !destinationDir.canWrite()) {
                return BackupResult(false, "Cannot write to selected location")
            }

            // Create the backup file
            val backupFile = destinationDir.createFile("application/zip", backupFileName)
            if (backupFile == null) {
                return BackupResult(false, "Failed to create backup file")
            }

            val outputStream = context.contentResolver.openOutputStream(backupFile.uri)
            if (outputStream == null) {
                return BackupResult(false, "Failed to open output stream")
            }

            ZipOutputStream(outputStream).use { zipOut ->
                // Add notes as markdown files
                notes.forEach { note ->
                    val content = buildString {
                        appendLine("---")
                        appendLine("id: ${note.id}")
                        appendLine("title: ${note.title}")
                        appendLine("created: ${note.createdAt}")
                        appendLine("modified: ${note.modifiedAt}")
                        appendLine("pinned: ${note.isPinned}")
                        appendLine("favorite: ${note.isFavorite}")
                        appendLine("encrypted: ${note.isEncrypted}")
                        note.folderId?.let { appendLine("folderId: $it") }
                        appendLine("---")
                        appendLine()
                        append(note.content)
                    }

                    val fileName = sanitizeFileName(note.title) + ".md"
                    val entry = ZipEntry("notes/$fileName")
                    zipOut.putNextEntry(entry)
                    zipOut.write(content.toByteArray())
                    zipOut.closeEntry()
                }

                // Add database file
                val dbPath = context.getDatabasePath(DATABASE_NAME)
                if (dbPath.exists()) {
                    FileInputStream(dbPath).use { fis ->
                        val entry = ZipEntry("database/$DATABASE_NAME.db")
                        zipOut.putNextEntry(entry)
                        fis.copyTo(zipOut)
                        zipOut.closeEntry()
                    }
                }

                // Add database WAL file if exists
                val walPath = File(dbPath.path + "-wal")
                if (walPath.exists()) {
                    FileInputStream(walPath).use { fis ->
                        val entry = ZipEntry("database/$DATABASE_NAME.db-wal")
                        zipOut.putNextEntry(entry)
                        fis.copyTo(zipOut)
                        zipOut.closeEntry()
                    }
                }

                // Add database SHM file if exists
                val shmPath = File(dbPath.path + "-shm")
                if (shmPath.exists()) {
                    FileInputStream(shmPath).use { fis ->
                        val entry = ZipEntry("database/$DATABASE_NAME.db-shm")
                        zipOut.putNextEntry(entry)
                        fis.copyTo(zipOut)
                        zipOut.closeEntry()
                    }
                }
            }

            Log.d(TAG, "Backup created successfully: ${backupFile.uri}")
            BackupResult(
                success = true,
                message = "Backup saved successfully to $backupFileName",
                backupUri = backupFile.uri
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating backup", e)
            BackupResult(false, "Failed to create backup: ${e.message}")
        }
    }

    /**
     * Restore notes from a backup ZIP file
     */
    suspend fun restoreBackup(
        context: Context,
        backupUri: Uri
    ): BackupResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(backupUri)
            if (inputStream == null) {
                return BackupResult(false, "Failed to open backup file")
            }

            val restoredNotes = mutableListOf<Pair<String, String>>()
            var databaseRestored = false

            ZipInputStream(inputStream).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                while (entry != null) {
                    when {
                        entry.name.startsWith("notes/") && entry.name.endsWith(".md") -> {
                            // Read note content
                            val content = zipIn.readBytes().toString(Charsets.UTF_8)
                            val (title, noteContent) = parseMarkdownFile(content)
                            restoredNotes.add(Pair(title, noteContent))
                        }

                        entry.name == "database/$DATABASE_NAME.db" -> {
                            // Restore database
                            val dbPath = context.getDatabasePath(DATABASE_NAME)
                            FileOutputStream(dbPath).use { fos ->
                                zipIn.copyTo(fos)
                            }
                            databaseRestored = true
                        }

                        entry.name == "database/$DATABASE_NAME.db-wal" -> {
                            val dbPath = context.getDatabasePath(DATABASE_NAME)
                            val walPath = File(dbPath.path + "-wal")
                            FileOutputStream(walPath).use { fos ->
                                zipIn.copyTo(fos)
                            }
                        }

                        entry.name == "database/$DATABASE_NAME.db-shm" -> {
                            val dbPath = context.getDatabasePath(DATABASE_NAME)
                            val shmPath = File(dbPath.path + "-shm")
                            FileOutputStream(shmPath).use { fos ->
                                zipIn.copyTo(fos)
                            }
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            val message = buildString {
                if (databaseRestored) {
                    append("Database restored successfully. ")
                }
                if (restoredNotes.isNotEmpty()) {
                    append("${restoredNotes.size} note(s) found in backup.")
                }
            }

            Log.d(TAG, "Backup restored successfully")
            BackupResult(
                success = true,
                message = if (message.isNotBlank()) message else "Backup restored successfully"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring backup", e)
            BackupResult(false, "Failed to restore backup: ${e.message}")
        }
    }

    /**
     * Export individual note to markdown file
     */
    fun exportNoteToUri(
        context: Context,
        note: Note,
        destinationUri: Uri
    ): BackupResult {
        return try {
            val content = buildString {
                appendLine("---")
                appendLine("title: ${note.title}")
                appendLine("created: ${note.createdAt}")
                appendLine("modified: ${note.modifiedAt}")
                appendLine("---")
                appendLine()
                append(note.content)
            }

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            BackupResult(true, "Note exported successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting note", e)
            BackupResult(false, "Failed to export note: ${e.message}")
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9.-_]"), "_")
            .take(100)
    }

    private fun parseMarkdownFile(content: String): Pair<String, String> {
        val metadataRegex = """^---\s*\n(.*?)\n---\s*\n+""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val match = metadataRegex.find(content)

        val title = if (match != null) {
            val metadata = match.groupValues[1]
            val titleRegex = """title:\s*(.+)""".toRegex()
            titleRegex.find(metadata)?.groupValues?.get(1)?.trim() ?: "Untitled"
        } else {
            "Untitled"
        }

        val noteContent = content.replace(metadataRegex, "")

        return Pair(title, noteContent)
    }
}
