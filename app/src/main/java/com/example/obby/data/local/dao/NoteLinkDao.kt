package com.example.obby.data.local.dao

import androidx.room.*
import com.example.obby.data.local.entity.Note
import com.example.obby.data.local.entity.NoteLink
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteLinkDao {

    @Query("SELECT * FROM note_links WHERE sourceNoteId = :noteId")
    fun getOutgoingLinks(noteId: Long): Flow<List<NoteLink>>

    @Query("SELECT * FROM note_links WHERE targetNoteId = :noteId")
    fun getIncomingLinks(noteId: Long): Flow<List<NoteLink>>

    @Query(
        """
        SELECT notes.* FROM notes
        INNER JOIN note_links ON notes.id = note_links.targetNoteId
        WHERE note_links.sourceNoteId = :noteId
        ORDER BY notes.title ASC
    """
    )
    fun getLinkedNotes(noteId: Long): Flow<List<Note>>

    @Query(
        """
        SELECT notes.* FROM notes
        INNER JOIN note_links ON notes.id = note_links.sourceNoteId
        WHERE note_links.targetNoteId = :noteId
        ORDER BY notes.title ASC
    """
    )
    fun getBacklinks(noteId: Long): Flow<List<Note>>

    @Query(
        """
        SELECT * FROM note_links 
        WHERE sourceNoteId = :sourceId AND targetNoteId = :targetId
        LIMIT 1
    """
    )
    suspend fun getLink(sourceId: Long, targetId: Long): NoteLink?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: NoteLink): Long

    @Delete
    suspend fun deleteLink(link: NoteLink)

    @Query("DELETE FROM note_links WHERE sourceNoteId = :noteId")
    suspend fun deleteOutgoingLinks(noteId: Long)

    @Query("DELETE FROM note_links WHERE targetNoteId = :noteId")
    suspend fun deleteIncomingLinks(noteId: Long)

    @Query("SELECT * FROM note_links")
    fun getAllLinks(): Flow<List<NoteLink>>
}
