package com.example.obby.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.obby.data.local.entity.Folder
import com.example.obby.data.local.entity.Note
import com.example.obby.data.local.entity.Tag
import com.example.obby.data.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFolder = MutableStateFlow<Long?>(null)
    val selectedFolder: StateFlow<Long?> = _selectedFolder.asStateFlow()

    private val _selectedTag = MutableStateFlow<Long?>(null)
    val selectedTag: StateFlow<Long?> = _selectedTag.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.ALL)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // Multi-select state
    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    private val _selectedNotes = MutableStateFlow<Set<Long>>(emptySet())
    val selectedNotes: StateFlow<Set<Long>> = _selectedNotes.asStateFlow()

    // Action feedback
    private val _actionMessage = MutableSharedFlow<String>()
    val actionMessage: SharedFlow<String> = _actionMessage.asSharedFlow()

    val notes: StateFlow<List<Note>> = combine(
        searchQuery,
        selectedFolder,
        selectedTag,
        viewMode
    ) { query, folderId, tagId, mode ->
        when {
            query.isNotEmpty() -> repository.searchNotes(query)
            tagId != null -> repository.getNotesForTag(tagId)
            folderId != null -> repository.getNotesByFolder(folderId)
            mode == ViewMode.PINNED -> repository.getPinnedNotes()
            mode == ViewMode.FAVORITES -> repository.getFavoriteNotes()
            mode == ViewMode.NO_FOLDER -> repository.getNotesWithoutFolder()
            else -> repository.getAllNotes()
        }
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val folders: StateFlow<List<Folder>> = repository.getAllFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tags: StateFlow<List<Tag>> = repository.getAllTags()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectFolder(folderId: Long?) {
        _selectedFolder.value = folderId
        _selectedTag.value = null
    }

    fun selectTag(tagId: Long?) {
        _selectedTag.value = tagId
        _selectedFolder.value = null
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
        _selectedFolder.value = null
        _selectedTag.value = null
    }

    // Multi-select operations
    fun toggleMultiSelectMode() {
        _isMultiSelectMode.value = !_isMultiSelectMode.value
        if (!_isMultiSelectMode.value) {
            _selectedNotes.value = emptySet()
        }
    }

    fun toggleNoteSelection(noteId: Long) {
        _selectedNotes.value = if (_selectedNotes.value.contains(noteId)) {
            _selectedNotes.value - noteId
        } else {
            _selectedNotes.value + noteId
        }

        // Exit multi-select if no notes selected
        if (_selectedNotes.value.isEmpty() && _isMultiSelectMode.value) {
            _isMultiSelectMode.value = false
        }
    }

    fun selectAllNotes() {
        _selectedNotes.value = notes.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedNotes.value = emptySet()
        _isMultiSelectMode.value = false
    }

    // Note operations
    fun createNote(title: String, content: String = "", folderId: Long? = null) {
        Log.d("NotesViewModel", "createNote called with title: $title")
        viewModelScope.launch {
            try {
                val noteId = repository.insertNote(
                    Note(
                        title = title,
                        content = content,
                        folderId = folderId
                    )
                )
                Log.d("NotesViewModel", "Note created successfully with ID: $noteId")
                _actionMessage.emit("Note created")
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error creating note", e)
                _actionMessage.emit("Failed to create note")
            }
        }
    }

    fun createChecklist(title: String, folderId: Long? = null) {
        viewModelScope.launch {
            try {
                val checklistContent = "# $title\n\n- [ ] Task 1\n- [ ] Task 2\n- [ ] Task 3"
                repository.insertNote(
                    Note(
                        title = title,
                        content = checklistContent,
                        folderId = folderId
                    )
                )
                _actionMessage.emit("Checklist created")
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error creating checklist", e)
                _actionMessage.emit("Failed to create checklist")
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.deleteNote(note)
                _actionMessage.emit("Note deleted")
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error deleting note", e)
                _actionMessage.emit("Failed to delete note")
            }
        }
    }

    fun deleteSelectedNotes() {
        viewModelScope.launch {
            try {
                val notesToDelete = notes.value.filter { it.id in _selectedNotes.value }
                notesToDelete.forEach { repository.deleteNote(it) }
                _actionMessage.emit("${notesToDelete.size} note(s) deleted")
                clearSelection()
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error deleting notes", e)
                _actionMessage.emit("Failed to delete notes")
            }
        }
    }

    fun duplicateNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.insertNote(
                    note.copy(
                        id = 0,
                        title = "${note.title} (Copy)",
                        createdAt = System.currentTimeMillis(),
                        modifiedAt = System.currentTimeMillis()
                    )
                )
                _actionMessage.emit("Note duplicated")
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error duplicating note", e)
                _actionMessage.emit("Failed to duplicate note")
            }
        }
    }

    fun renameNote(note: Note, newTitle: String) {
        viewModelScope.launch {
            try {
                repository.updateNote(note.copy(title = newTitle))
                _actionMessage.emit("Note renamed")
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error renaming note", e)
                _actionMessage.emit("Failed to rename note")
            }
        }
    }

    fun moveNoteToFolder(note: Note, folderId: Long?) {
        viewModelScope.launch {
            try {
                repository.updateNote(note.copy(folderId = folderId))
                val folderName = if (folderId == null) {
                    "root"
                } else {
                    folders.value.find { it.id == folderId }?.name ?: "folder"
                }
                _actionMessage.emit("Note moved to $folderName")
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error moving note", e)
                _actionMessage.emit("Failed to move note")
            }
        }
    }

    fun moveSelectedNotesToFolder(folderId: Long?) {
        viewModelScope.launch {
            try {
                val notesToMove = notes.value.filter { it.id in _selectedNotes.value }
                notesToMove.forEach { note ->
                    repository.updateNote(note.copy(folderId = folderId))
                }
                val folderName = if (folderId == null) {
                    "root"
                } else {
                    folders.value.find { it.id == folderId }?.name ?: "folder"
                }
                _actionMessage.emit("${notesToMove.size} note(s) moved to $folderName")
                clearSelection()
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error moving notes", e)
                _actionMessage.emit("Failed to move notes")
            }
        }
    }

    fun togglePinNote(noteId: Long) {
        viewModelScope.launch {
            try {
                repository.togglePinNote(noteId)
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error toggling pin", e)
            }
        }
    }

    fun toggleFavoriteNote(noteId: Long) {
        viewModelScope.launch {
            try {
                repository.toggleFavoriteNote(noteId)
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error toggling favorite", e)
            }
        }
    }

    fun createFolder(name: String, parentFolderId: Long? = null) {
        viewModelScope.launch {
            try {
                repository.insertFolder(
                    Folder(name = name, parentFolderId = parentFolderId)
                )
                _actionMessage.emit("Folder created")
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error creating folder", e)
                _actionMessage.emit("Failed to create folder")
            }
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            try {
                repository.deleteFolder(folder)
                _actionMessage.emit("Folder deleted")
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Error deleting folder", e)
                _actionMessage.emit("Failed to delete folder")
            }
        }
    }

    fun getShareableContent(noteId: Long): String? {
        return notes.value.find { it.id == noteId }?.let { note ->
            "${note.title}\n\n${note.content}"
        }
    }

    enum class ViewMode {
        ALL, PINNED, FAVORITES, NO_FOLDER
    }
}
