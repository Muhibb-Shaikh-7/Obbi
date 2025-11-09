package com.example.obby.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "notes",
    indices = [Index(value = ["title"], unique = false)]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val folderId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isEncrypted: Boolean = false,
    @Deprecated("Use isPrivate instead", ReplaceWith("isPrivate"))
    val isHidden: Boolean = false,
    val isPrivate: Boolean = false,
    val hiddenCategoryAlias: String? = null,
    val encryptedContentIv: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Note

        if (id != other.id) return false
        if (title != other.title) return false
        if (content != other.content) return false
        if (folderId != other.folderId) return false
        if (createdAt != other.createdAt) return false
        if (modifiedAt != other.modifiedAt) return false
        if (isPinned != other.isPinned) return false
        if (isFavorite != other.isFavorite) return false
        if (isEncrypted != other.isEncrypted) return false
        if (isHidden != other.isHidden) return false
        if (isPrivate != other.isPrivate) return false
        if (hiddenCategoryAlias != other.hiddenCategoryAlias) return false
        if (encryptedContentIv != null) {
            if (other.encryptedContentIv == null) return false
            if (!encryptedContentIv.contentEquals(other.encryptedContentIv)) return false
        } else if (other.encryptedContentIv != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + (folderId?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + modifiedAt.hashCode()
        result = 31 * result + isPinned.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + isEncrypted.hashCode()
        result = 31 * result + isHidden.hashCode()
        result = 31 * result + isPrivate.hashCode()
        result = 31 * result + (hiddenCategoryAlias?.hashCode() ?: 0)
        result = 31 * result + (encryptedContentIv?.contentHashCode() ?: 0)
        return result
    }

    // Convenience property: a note is considered private if either isPrivate or isHidden is true
    // This provides backward compatibility during transition
    val isActuallyPrivate: Boolean
        get() = isPrivate || isHidden
}
