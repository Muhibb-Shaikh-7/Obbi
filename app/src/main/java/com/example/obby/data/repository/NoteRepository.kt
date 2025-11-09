package com.example.obby.data.repository

import android.util.Log
import com.example.obby.data.local.dao.*
import com.example.obby.data.local.entity.*
import com.example.obby.domain.model.GraphEdge
import com.example.obby.domain.model.GraphNode
import com.example.obby.domain.model.NoteGraph
import com.example.obby.util.EncryptionManager
import com.example.obby.util.MarkdownLinkParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.math.cos
import kotlin.math.sin

class NoteRepository(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao,
    private val tagDao: TagDao,
    private val noteLinkDao: NoteLinkDao
) {

    // Note operations
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    /**
     * Get all notes excluding private ones (for main view)
     */
    fun getAllNotesExcludingPrivate(): Flow<List<Note>> =
        noteDao.getAllNotes().map { notes -> notes.filter { !it.isActuallyPrivate } }

    /**
     * Get only private notes (when unlocked)
     */
    fun getPrivateNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { notes -> notes.filter { it.isActuallyPrivate } }

    fun getNoteById(noteId: Long): Flow<Note?> = noteDao.getNoteByIdFlow(noteId)

    fun getNoteWithDetails(noteId: Long): Flow<NoteWithDetails?> =
        noteDao.getNoteWithDetailsFlow(noteId)

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    fun getNotesByFolder(folderId: Long): Flow<List<Note>> = noteDao.getNotesByFolder(folderId)

    fun getNotesWithoutFolder(): Flow<List<Note>> = noteDao.getNotesWithoutFolder()

    fun getPinnedNotes(): Flow<List<Note>> = noteDao.getPinnedNotes()

    fun getFavoriteNotes(): Flow<List<Note>> = noteDao.getFavoriteNotes()

    suspend fun insertNote(note: Note): Long {
        return try {
            Log.d("NoteRepository", "Inserting note: ${note.title}")
            val noteId = noteDao.insertNote(note)
            Log.d("NoteRepository", "Note inserted with ID: $noteId")

            try {
                updateLinksFromContent(noteId, note.content)
            } catch (e: Exception) {
                Log.e("NoteRepository", "Error updating links", e)
            }

            try {
                updateTagsFromContent(noteId, note.content)
            } catch (e: Exception) {
                Log.e("NoteRepository", "Error updating tags", e)
            }

            noteId
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error inserting note", e)
            throw e
        }
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.copy(modifiedAt = System.currentTimeMillis()))
        try {
            updateLinksFromContent(note.id, note.content)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error updating links", e)
        }
        try {
            updateTagsFromContent(note.id, note.content)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error updating tags", e)
        }
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun togglePinNote(noteId: Long) {
        val note = noteDao.getNoteById(noteId) ?: return
        noteDao.updateNote(note.copy(isPinned = !note.isPinned))
    }

    suspend fun toggleFavoriteNote(noteId: Long) {
        val note = noteDao.getNoteById(noteId) ?: return
        noteDao.updateNote(note.copy(isFavorite = !note.isFavorite))
    }

    /**
     * Toggle private status of a note
     */
    suspend fun togglePrivateNote(noteId: Long, categoryAlias: String = "Recipes") {
        val note = noteDao.getNoteById(noteId) ?: return
        noteDao.updateNote(
            note.copy(
                isPrivate = !note.isActuallyPrivate,
                isHidden = false, // Clear old field
                hiddenCategoryAlias = if (!note.isActuallyPrivate) categoryAlias else null
            )
        )
    }

    /**
     * Mark multiple notes as private at once
     */
    suspend fun markNotesAsPrivate(noteIds: List<Long>, categoryAlias: String = "Recipes") {
        noteIds.forEach { noteId ->
            val note = noteDao.getNoteById(noteId) ?: return@forEach
            if (!note.isActuallyPrivate) {
                noteDao.updateNote(
                    note.copy(
                        isPrivate = true,
                        isHidden = false,
                        hiddenCategoryAlias = categoryAlias
                    )
                )
            }
        }
    }

    /**
     * Mark multiple notes as not private at once
     */
    suspend fun unmarkNotesAsPrivate(noteIds: List<Long>) {
        noteIds.forEach { noteId ->
            val note = noteDao.getNoteById(noteId) ?: return@forEach
            if (note.isActuallyPrivate) {
                noteDao.updateNote(
                    note.copy(
                        isPrivate = false,
                        isHidden = false,
                        hiddenCategoryAlias = null
                    )
                )
            }
        }
    }

    suspend fun encryptNote(noteId: Long, password: String) {
        val note = noteDao.getNoteById(noteId) ?: return
        if (!note.isEncrypted) {
            val encryptedContent = EncryptionManager.encrypt(note.content, password)
            noteDao.updateNote(note.copy(content = encryptedContent, isEncrypted = true))
        }
    }

    suspend fun decryptNote(noteId: Long, password: String) {
        val note = noteDao.getNoteById(noteId) ?: return
        if (note.isEncrypted) {
            val decryptedContent = EncryptionManager.decrypt(note.content, password)
            noteDao.updateNote(note.copy(content = decryptedContent, isEncrypted = false))
        }
    }

    // Folder operations
    fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders()

    fun getRootFolders(): Flow<List<Folder>> = folderDao.getRootFolders()

    suspend fun insertFolder(folder: Folder): Long = folderDao.insertFolder(folder)

    suspend fun updateFolder(folder: Folder) = folderDao.updateFolder(folder)

    suspend fun deleteFolder(folder: Folder) = folderDao.deleteFolder(folder)

    // Tag operations
    fun getAllTags(): Flow<List<Tag>> = tagDao.getAllTags()

    fun getTagsForNote(noteId: Long): Flow<List<Tag>> = tagDao.getTagsForNote(noteId)

    fun getNotesForTag(tagId: Long): Flow<List<Note>> = tagDao.getNotesForTag(tagId)

    suspend fun insertTag(tag: Tag): Long = tagDao.insertTag(tag)

    suspend fun deleteTag(tag: Tag) = tagDao.deleteTag(tag)

    suspend fun addTagToNote(noteId: Long, tagId: Long) {
        tagDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
    }

    suspend fun removeTagFromNote(noteId: Long, tagId: Long) {
        tagDao.deleteNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
    }

    // Link operations
    fun getLinkedNotes(noteId: Long): Flow<List<Note>> = noteLinkDao.getLinkedNotes(noteId)

    fun getBacklinks(noteId: Long): Flow<List<Note>> = noteLinkDao.getBacklinks(noteId)

    suspend fun createLink(sourceNoteId: Long, targetNoteId: Long, linkText: String) {
        val existingLink = noteLinkDao.getLink(sourceNoteId, targetNoteId)
        if (existingLink == null) {
            noteLinkDao.insertLink(
                NoteLink(
                    sourceNoteId = sourceNoteId,
                    targetNoteId = targetNoteId,
                    linkText = linkText
                )
            )
        }
    }

    suspend fun deleteLink(sourceNoteId: Long, targetNoteId: Long) {
        val link = noteLinkDao.getLink(sourceNoteId, targetNoteId)
        link?.let { noteLinkDao.deleteLink(it) }
    }

    // Graph operations
    suspend fun generateNoteGraph(): NoteGraph {
        val notes = noteDao.getAllNotes().first()
        val links = noteLinkDao.getAllLinks().first()

        val nodes = notes.mapIndexed { index, note ->
            val angle = (2 * Math.PI * index) / notes.size
            val radius = 300f
            GraphNode(
                id = note.id,
                title = note.title,
                x = (radius * cos(angle)).toFloat(),
                y = (radius * sin(angle)).toFloat(),
                connections = links.count { it.sourceNoteId == note.id || it.targetNoteId == note.id }
            )
        }

        val edges = links.map { link ->
            GraphEdge(
                sourceId = link.sourceNoteId,
                targetId = link.targetNoteId,
                linkText = link.linkText
            )
        }

        return NoteGraph(nodes, edges)
    }

    // Private helper functions
    private suspend fun updateLinksFromContent(noteId: Long, content: String) {
        // Delete existing outgoing links
        noteLinkDao.deleteOutgoingLinks(noteId)

        // Parse links from content
        val links = MarkdownLinkParser.parseAllLinks(content)

        // Create new links
        links.forEach { parsedLink ->
            val targetNote = noteDao.getNoteByTitle(parsedLink.linkText)
            if (targetNote != null) {
                createLink(noteId, targetNote.id, parsedLink.displayText)
            }
        }
    }

    private suspend fun updateTagsFromContent(noteId: Long, content: String) {
        // Delete existing tags
        tagDao.deleteAllTagsForNote(noteId)

        // Extract tags from content
        val tagNames = MarkdownLinkParser.extractTags(content)

        // Add tags
        tagNames.forEach { tagName ->
            var tag = tagDao.getTagByName(tagName)
            if (tag == null) {
                val tagId = tagDao.insertTag(Tag(name = tagName))
                tag = tagDao.getTagById(tagId)
            }
            tag?.let { addTagToNote(noteId, it.id) }
        }
    }
}
