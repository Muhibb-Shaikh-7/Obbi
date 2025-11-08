package com.example.obby.ui.viewmodel

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.obby.data.local.entity.Note
import com.example.obby.data.local.entity.Tag
import com.example.obby.data.repository.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Stack

class NoteDetailViewModel(
    private val repository: NoteRepository,
    private val noteId: Long
) : ViewModel() {

    companion object {
        private const val TAG = "NoteDetailViewModel"
        private const val AUTO_SAVE_DELAY = 2000L // 2 seconds
    }

    private val _editMode = MutableStateFlow(false)
    val editMode: StateFlow<Boolean> = _editMode.asStateFlow()

    private val _showMarkdownPreview = MutableStateFlow(false)
    val showMarkdownPreview: StateFlow<Boolean> = _showMarkdownPreview.asStateFlow()

    // Text selection state
    private val _textSelection = MutableStateFlow(TextRange(0))
    val textSelection: StateFlow<TextRange> = _textSelection.asStateFlow()

    // Undo/Redo stacks
    private val undoStack = Stack<NoteState>()
    private val redoStack = Stack<NoteState>()
    private var currentState: NoteState? = null

    // Auto-save
    private var autoSaveJob: Job? = null
    private val _pendingChanges = MutableStateFlow(false)
    val pendingChanges: StateFlow<Boolean> = _pendingChanges.asStateFlow()

    // Snackbar messages
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    val note: StateFlow<Note?> = repository.getNoteById(noteId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val linkedNotes: StateFlow<List<Note>> = repository.getLinkedNotes(noteId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val backlinks: StateFlow<List<Note>> = repository.getBacklinks(noteId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tags: StateFlow<List<Tag>> = repository.getTagsForNote(noteId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Initialize undo stack with current note
        viewModelScope.launch {
            note.filterNotNull().first().let { initialNote ->
                currentState = NoteState(initialNote.title, initialNote.content)
            }
        }
    }

    fun toggleEditMode() {
        _editMode.value = !_editMode.value
        if (!_editMode.value) {
            // Save when exiting edit mode
            saveNow()
        }
    }

    fun toggleMarkdownPreview() {
        _showMarkdownPreview.value = !_showMarkdownPreview.value
    }

    fun updateTextSelection(selection: TextRange) {
        _textSelection.value = selection
    }

    fun onContentChange(title: String, content: String) {
        // Save current state to undo stack if it's different
        currentState?.let { current ->
            if (current.title != title || current.content != content) {
                undoStack.push(current)
                redoStack.clear() // Clear redo stack on new changes
            }
        }

        currentState = NoteState(title, content)
        _pendingChanges.value = true

        // Schedule auto-save
        scheduleAutoSave(title, content)
    }

    private fun scheduleAutoSave(title: String, content: String) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(AUTO_SAVE_DELAY)
            saveNote(title, content)
        }
    }

    private suspend fun saveNote(title: String, content: String) {
        try {
            note.value?.let { currentNote ->
                repository.updateNote(
                    currentNote.copy(
                        title = title,
                        content = content
                    )
                )
                _pendingChanges.value = false
                Log.d(TAG, "Note auto-saved")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error auto-saving note", e)
            _snackbarMessage.emit("Failed to save note")
        }
    }

    fun saveNow() {
        currentState?.let { state ->
            viewModelScope.launch {
                saveNote(state.title, state.content)
                _snackbarMessage.emit("Note saved")
            }
        }
    }

    fun undo(): Pair<String, String>? {
        if (undoStack.isNotEmpty()) {
            currentState?.let { redoStack.push(it) }
            val previousState = undoStack.pop()
            currentState = previousState
            scheduleAutoSave(previousState.title, previousState.content)
            return previousState.title to previousState.content
        }
        return null
    }

    fun redo(): Pair<String, String>? {
        if (redoStack.isNotEmpty()) {
            currentState?.let { undoStack.push(it) }
            val nextState = redoStack.pop()
            currentState = nextState
            scheduleAutoSave(nextState.title, nextState.content)
            return nextState.title to nextState.content
        }
        return null
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun updateNoteContent(content: String) {
        viewModelScope.launch {
            try {
                note.value?.let { currentNote ->
                    repository.updateNote(currentNote.copy(content = content))
                    _snackbarMessage.emit("Note updated")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating content", e)
                _snackbarMessage.emit("Failed to update note")
            }
        }
    }

    fun updateNoteTitle(title: String) {
        viewModelScope.launch {
            try {
                note.value?.let { currentNote ->
                    repository.updateNote(currentNote.copy(title = title))
                    _snackbarMessage.emit("Note updated")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating title", e)
                _snackbarMessage.emit("Failed to update note")
            }
        }
    }

    fun togglePin() {
        viewModelScope.launch {
            try {
                repository.togglePinNote(noteId)
                val isPinned = note.value?.isPinned == true
                _snackbarMessage.emit(if (!isPinned) "Note pinned" else "Note unpinned")
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling pin", e)
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                repository.toggleFavoriteNote(noteId)
                val isFavorite = note.value?.isFavorite == true
                _snackbarMessage.emit(if (!isFavorite) "Added to favorites" else "Removed from favorites")
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite", e)
            }
        }
    }

    fun encryptNote(password: String) {
        viewModelScope.launch {
            try {
                repository.encryptNote(noteId, password)
                _snackbarMessage.emit("Note encrypted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error encrypting note", e)
                _snackbarMessage.emit("Failed to encrypt note: ${e.message}")
            }
        }
    }

    fun decryptNote(password: String) {
        viewModelScope.launch {
            try {
                repository.decryptNote(noteId, password)
                _snackbarMessage.emit("Note decrypted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error decrypting note", e)
                _snackbarMessage.emit("Incorrect password or corrupted data")
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            try {
                note.value?.let { repository.deleteNote(it) }
                _snackbarMessage.emit("Note deleted")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting note", e)
                _snackbarMessage.emit("Failed to delete note")
            }
        }
    }

    fun duplicateNote() {
        viewModelScope.launch {
            try {
                note.value?.let { currentNote ->
                    repository.insertNote(
                        currentNote.copy(
                            id = 0, // New ID will be generated
                            title = "${currentNote.title} (Copy)",
                            createdAt = System.currentTimeMillis(),
                            modifiedAt = System.currentTimeMillis()
                        )
                    )
                    _snackbarMessage.emit("Note duplicated")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error duplicating note", e)
                _snackbarMessage.emit("Failed to duplicate note")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }

    private data class NoteState(
        val title: String,
        val content: String
    )
}
