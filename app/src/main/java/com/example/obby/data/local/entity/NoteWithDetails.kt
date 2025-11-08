package com.example.obby.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NoteWithDetails(
    @Embedded val note: Note,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>,

    @Relation(
        parentColumn = "folderId",
        entityColumn = "id"
    )
    val folder: Folder?
)

data class NoteWithLinks(
    @Embedded val note: Note,

    @Relation(
        parentColumn = "id",
        entityColumn = "sourceNoteId"
    )
    val outgoingLinks: List<NoteLink>,

    @Relation(
        parentColumn = "id",
        entityColumn = "targetNoteId"
    )
    val incomingLinks: List<NoteLink>
)
