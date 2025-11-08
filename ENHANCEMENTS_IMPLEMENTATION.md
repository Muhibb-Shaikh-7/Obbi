# Obby - Enhanced Markdown Note-Taking App

## 🎉 Major Enhancements Implemented

This document outlines all the major improvements made to transform Obby from a basic note-taking
app into a polished, fully-featured offline Markdown editor.

---

## 📋 Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Core Features Implemented](#core-features-implemented)
3. [Markdown Editor Features](#markdown-editor-features)
4. [Note Management](#note-management)
5. [UI/UX Improvements](#ui-ux-improvements)
6. [Technical Implementation Details](#technical-implementation-details)
7. [Usage Guide](#usage-guide)

---

## Architecture Overview

### MVVM Architecture with Clean Code Principles

```
┌─────────────────────────────────────────────────────────┐
│                     UI Layer (Compose)                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ NotesListScreen│ │NoteDetailScreen│ │ GraphScreen │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
│         │                  │                  │           │
│  ┌──────▼──────────────────▼──────────────────▼───────┐ │
│  │           ViewModel Layer                            │ │
│  │  - NotesViewModel (list, multi-select, actions)     │ │
│  │  - NoteDetailViewModel (editing, undo/redo, save)   │ │
│  │  - GraphViewModel (visualization)                    │ │
│  └──────────────────────┬───────────────────────────────┘ │
└─────────────────────────┼───────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────┐
│                  Repository Layer                        │
│  ┌────────────────────────────────────────────────────┐ │
│  │ NoteRepository (business logic, data operations)   │ │
│  └──────────────────────┬─────────────────────────────┘ │
└─────────────────────────┼───────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────┐
│                   Data Layer (Room)                      │
│  ┌───────────┐  ┌───────────┐  ┌────────────┐          │
│  │  NoteDao  │  │ FolderDao │  │  TagDao    │          │
│  └───────────┘  └───────────┘  └────────────┘          │
│  ┌───────────────────────────────────────────┐          │
│  │   ObbyDatabase (SQLite + Room)            │          │
│  └───────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────┘
```

---

## Core Features Implemented

### ✅ 1. Enhanced Markdown Toolbar

**Location:** `ui/components/MarkdownToolbar.kt`

#### Features:

- **Formatting Buttons**: Bold, Italic, Strikethrough, Code, Quote
- **Headings**: H1-H6 via dropdown menu
- **Lists**: Bullet list, numbered list, checklist
- **Advanced**: Code blocks, links, tables, horizontal rules
- **Help**: Built-in Markdown cheat sheet popup
- **Smart Cursor Positioning**: Inserts syntax at cursor location
- **Text Selection Support**: Wraps selected text with formatting

#### Implementation:
```kotlin
@Composable
fun MarkdownToolbar(
    onAction: (MarkdownActionType) -> Unit
)
```

The toolbar uses `MarkdownFormatter` object to apply formatting:

- Preserves cursor position
- Handles text selection intelligently
- Supports undo/redo through ViewModel

---

### ✅ 2. Markdown Preview Toggle

**Location:** `ui/screens/NoteDetailScreen.kt`

#### Features:

- **Live Preview**: Real-time Markdown rendering using Markwon
- **Toggle Button**: Switch between edit and preview modes
- **Checklist Support**: Interactive checkboxes in preview
- **Code Highlighting**: Syntax highlighting for code blocks
- **Table Support**: Renders Markdown tables properly

#### Preview Plugins Enabled:

- `StrikethroughPlugin` - ~~strikethrough text~~
- `TablePlugin` - Markdown tables
- `TaskListPlugin` - Interactive checklists
- `SyntaxHighlightPlugin` - Code syntax highlighting

---

### ✅ 3. Auto-Save System

**Location:** `ui/viewmodel/NoteDetailViewModel.kt`

#### Features:

- **Debounced Saving**: 2-second delay after typing stops
- **Visual Indicator**: Progress spinner shows pending changes
- **Manual Save**: "Done" button for immediate save
- **Background Processing**: Non-blocking auto-save
- **Error Handling**: Toast notifications on save failure

#### Implementation:
```kotlin
private const val AUTO_SAVE_DELAY = 2000L // 2 seconds

private fun scheduleAutoSave(title: String, content: String) {
    autoSaveJob?.cancel()
    autoSaveJob = viewModelScope.launch {
        delay(AUTO_SAVE_DELAY)
        saveNote(title, content)
    }
}
```

---

### ✅ 4. Undo/Redo System

**Location:** `ui/viewmodel/NoteDetailViewModel.kt`

#### Features:

- **Stack-Based**: Uses Stack<NoteState> for history
- **Visual Feedback**: Undo/Redo buttons enabled/disabled appropriately
- **State Tracking**: Captures both title and content changes
- **Efficient**: Only stores changed states

#### Usage:
```kotlin
// Undo/Redo buttons in NoteDetailScreen
IconButton(
    onClick = { viewModel.undo() },
    enabled = viewModel.canUndo()
) {
    Icon(Icons.Default.Undo, "Undo")
}
```

---

## Note Management

### ✅ 5. Long-Press Context Menu

**Location:** `ui/screens/NotesListScreen.kt`

#### Features:

- **Haptic Feedback**: Vibration on long press
- **Context Actions**:
    - Pin/Unpin
    - Add to Favorites
    - Rename
    - Duplicate
    - Move to Folder
    - Share
    - Delete
- **Visual Menu**: Dropdown with icons

#### Implementation:
```kotlin
@OptIn(ExperimentalFoundationApi::class)
modifier.combinedClickable(
    onClick = onClick,
    onLongClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onLongClick()
    }
)
```

---

### ✅ 6. Multi-Select Mode

**Location:** `ui/viewmodel/NotesViewModel.kt`, `ui/screens/NotesListScreen.kt`

#### Features:

- **Batch Operations**: Select multiple notes
- **Visual Selection**: Blue border on selected notes
- **Custom Top Bar**: Shows count and actions
- **Actions**:
    - Select All
    - Delete Selected
    - Move Selected to Folder
- **Smart Exit**: Clears selection when empty

#### State Management:

```kotlin
// ViewModel state
private val _isMultiSelectMode = MutableStateFlow(false)
private val _selectedNotes = MutableStateFlow<Set<Long>>(emptySet())

// Enter multi-select mode
fun toggleMultiSelectMode()
fun toggleNoteSelection(noteId: Long)
fun selectAllNotes()
fun clearSelection()
```

---

### ✅ 7. Move to Folder Dialog

**Location:** `ui/screens/NotesListScreen.kt`

#### Features:

- **Visual Folder List**: All folders with icons
- **Root Option**: Move to root (no folder)
- **Current Indicator**: Highlights current folder
- **Batch Move**: Works with multi-select

#### Dialog Component:
```kotlin
@Composable
fun MoveToFolderDialog(
    folders: List<Folder>,
    currentFolderId: Long? = null,
    onFolderSelected: (Long?) -> Unit
)
```

---

### ✅ 8. Rename Note Feature

**Location:** `ui/screens/NotesListScreen.kt`

#### Features:

- **Inline Dialog**: TextField with current title
- **Validation**: Prevents empty titles
- **Change Detection**: Only enables rename if title changed
- **Instant Update**: Changes reflected immediately

---

### ✅ 9. Duplicate Note

**Location:** `ui/viewmodel/NotesViewModel.kt`

#### Features:

- **Smart Naming**: Adds "(Copy)" suffix
- **Full Clone**: Copies title, content, and folder
- **New Timestamps**: Fresh creation/modification times
- **Toast Feedback**: Confirms duplication

---

### ✅ 10. Share Note

**Location:** `ui/screens/NotesListScreen.kt`

#### Features:

- **Android Share Sheet**: Native sharing interface
- **Format**: "Title\n\nContent"
- **Multiple Apps**: Share to any app that accepts text
- **Quick Access**: Available in context menu

#### Implementation:
```kotlin
private fun shareNote(context: Context, content: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, content)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Note"))
}
```

---

### ✅ 11. Password-Based Note Encryption ⭐

**Location:** `util/EncryptionManager.kt`, `ui/screens/NoteDetailScreen.kt`

#### Features:

- **Strong Encryption**: AES-256-GCM with authenticated encryption
- **Password Required**: User must enter password to encrypt/decrypt
- **Key Derivation**: PBKDF2 with 65,536 iterations
- **Salt & IV**: Random salt and IV for each encryption
- **Password Validation**: Shows error if incorrect password
- **Visual Indicators**: 🔒 icon on encrypted notes
- **No Recovery**: Passwords are never stored

#### Implementation:

**EncryptionManager.kt**:

```kotlin
object EncryptionManager {
    // Encrypts with user password
    fun encrypt(plaintext: String, password: String): String {
        // Generate random salt
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        
        // Derive key from password using PBKDF2
        val secretKey = deriveKeyFromPassword(password, salt)
        
        // Encrypt with AES-256-GCM
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray())
        
        // Combine: salt + IV + encrypted data
        val combined = salt + iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }
    
    // Decrypts with user password
    fun decrypt(encryptedData: String, password: String): String {
        val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
        
        // Extract salt, IV, and encrypted data
        val salt = combined.copyOfRange(0, SALT_LENGTH)
        val iv = combined.copyOfRange(SALT_LENGTH, SALT_LENGTH + GCM_IV_LENGTH)
        val encryptedBytes = combined.copyOfRange(SALT_LENGTH + GCM_IV_LENGTH, combined.size)
        
        // Derive key from password
        val secretKey = deriveKeyFromPassword(password, salt)
        
        // Decrypt
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        
        return String(decryptedBytes, Charsets.UTF_8)
    }
    
    private fun deriveKeyFromPassword(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}
```

**PasswordDialog Component**:
```kotlin
@Composable
fun PasswordDialog(
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    AlertDialog(
        title = { Text(title) },
        text = {
            Column {
                Text(message)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (showPassword) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.Visibility 
                                else Icons.Default.VisibilityOff,
                                "Toggle password visibility"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(password) },
                enabled = password.isNotBlank()
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

#### Usage Flow:

**Encrypting a Note**:

1. User opens note detail screen
2. Taps ⋮ menu → "Encrypt"
3. Password dialog appears
4. User enters password
5. `viewModel.encryptNote(password)` is called
6. Repository encrypts content with password
7. Note saved with `isEncrypted = true`
8. Success snackbar shown

**Decrypting a Note**:

1. User opens encrypted note (🔒 icon shown)
2. Taps ⋮ menu → "Decrypt"
3. Password dialog appears
4. User enters password
5. `viewModel.decryptNote(password)` is called
6. If password correct: note decrypted and saved
7. If password wrong: error snackbar "Incorrect password or corrupted data"

#### Security Features:

**Cryptographic Strength**:

- **AES-256**: Symmetric encryption with 256-bit key
- **GCM Mode**: Authenticated encryption (prevents tampering)
- **PBKDF2**: Password-based key derivation with 65,536 iterations
- **Random Salt**: 32 bytes, unique per encryption
- **Random IV**: 12 bytes, unique per encryption

**Attack Resistance**:

- **Brute Force**: 65,536 iterations slow down password guessing
- **Rainbow Tables**: Salt prevents pre-computed attack tables
- **Tampering**: GCM authentication detects any modification
- **Replay**: Each encryption has unique salt and IV

**Best Practices**:

- Passwords never stored in plain text
- Each note can have different password
- No password hints or recovery mechanism
- Encrypted data includes authentication tag
- Base64 encoding for safe database storage

#### Error Handling:

```kotlin
fun decryptNote(password: String) {
    viewModelScope.launch {
        try {
            repository.decryptNote(noteId, password)
            _snackbarMessage.emit("Note decrypted successfully")
        } catch (e: Exception) {
            // Wrong password throws exception during decryption
            _snackbarMessage.emit("Incorrect password or corrupted data")
        }
    }
}
```

---

## UI/UX Improvements

### ✅ 12. Smooth Animations

**Implemented Throughout the App:**

#### Top Bar Transitions:

```kotlin
AnimatedVisibility(
    visible = isMultiSelectMode,
    enter = slideInVertically() + fadeIn(),
    exit = slideOutVertically() + fadeOut()
) {
    MultiSelectTopBar(...)
}
```

#### Card Elevation Animation:
```kotlin
val elevation by animateDpAsState(
    targetValue = if (isSelected) 8.dp else 2.dp,
    animationSpec = spring()
)
```

#### FAB Menu Expansion:
```kotlin
AnimatedVisibility(
    visible = expanded,
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut()
)
```

---

### ✅ 13. Visual Selection Indicators

**Location:** `ui/screens/NotesListScreen.kt`

#### Features:

- **Checkbox**: Shows in multi-select mode
- **Blue Border**: 2dp primary color border
- **Elevated Card**: 8dp elevation when selected
- **Spring Animation**: Smooth elevation transition

---

### ✅ 14. Enhanced Note Cards

**Location:** `ui/screens/NotesListScreen.kt`

#### Visual Indicators:

- **Pin Icon**: 📌 for pinned notes
- **Favorite Icon**: ❤️ for favorites
- **Lock Icon**: 🔒 for encrypted notes
- **Folder Chip**: Shows folder name
- **Date Stamp**: Last modified date
- **Content Preview**: First 2 lines of note

---

### ✅ 15. Snackbar Notifications

**Location:** Throughout ViewModels

#### Feedback for Actions:

- Note created/deleted/moved/renamed
- Folder created/deleted
- Batch operations completed
- Save confirmations
- Error messages

#### Implementation:

```kotlin
// ViewModel
private val _actionMessage = MutableSharedFlow<String>()
val actionMessage: SharedFlow<String> = _actionMessage

viewModelScope.launch {
    _actionMessage.emit("Note deleted")
}

// UI
LaunchedEffect(Unit) {
    viewModel.actionMessage.collect { message ->
        snackbarHostState.showSnackbar(message)
    }
}
```

---

## Technical Implementation Details

### State Management

#### NotesViewModel State:
```kotlin
// Search and filtering
val searchQuery: StateFlow<String>
val selectedFolder: StateFlow<Long?>
val selectedTag: StateFlow<Long?>
val viewMode: StateFlow<ViewMode>

// Multi-select
val isMultiSelectMode: StateFlow<Boolean>
val selectedNotes: StateFlow<Set<Long>>

// Data
val notes: StateFlow<List<Note>>
val folders: StateFlow<List<Folder>>
val tags: StateFlow<List<Tag>>
```

#### NoteDetailViewModel State:
```kotlin
val note: StateFlow<Note?>
val editMode: StateFlow<Boolean>
val showMarkdownPreview: StateFlow<Boolean>
val pendingChanges: StateFlow<Boolean>
val textSelection: StateFlow<TextRange>
val linkedNotes: StateFlow<List<Note>>
val backlinks: StateFlow<List<Note>>
val tags: StateFlow<List<Tag>>
```

---

### Offline-First Architecture

#### Key Principles:

1. **All data stored locally** in Room database
2. **No network calls** - fully offline
3. **Efficient queries** with Flow for reactive updates
4. **Background operations** with coroutines
5. **Atomic transactions** for data consistency

#### Room Database Schema:
```
Notes (id, title, content, folderId, isPinned, isFavorite, isEncrypted, ...)
Folders (id, name, parentFolderId, ...)
Tags (id, name, color, ...)
NoteTagCrossRef (noteId, tagId)
NoteLinks (sourceNoteId, targetNoteId, linkText)
```

---

### Performance Optimizations

1. **LazyColumn**: Efficient list rendering with key-based recycling
2. **StateFlow**: Reactive state with backpressure handling
3. **Debounced Auto-Save**: Reduces database writes
4. **Minimal Recompositions**: Targeted state updates
5. **Coroutine Cancellation**: Proper cleanup in ViewModels

---

## Usage Guide

### Creating Notes

1. **Quick Note**: Tap FAB → Enter title
2. **With Folder**: Create from folder → Select folder first
3. **Checklist**: Tap FAB menu → Checklist icon

### Editing Notes

1. **Enter Edit Mode**: Tap edit icon in top bar
2. **Format Text**: Use bottom toolbar
3. **Preview**: Tap preview icon
4. **Auto-Save**: Content saves after 2 seconds
5. **Manual Save**: Tap checkmark icon

### Managing Notes

#### Single Note Actions:

- **Long Press** → Context menu
- Or tap **⋮** icon → Context menu

#### Batch Operations:

1. **Long press** any note → Multi-select mode
2. **Tap notes** to select
3. Use **top bar buttons** for batch actions

### Organizing with Folders

1. **Create Folder**: FAB menu → Folder icon
2. **Move Note**: Context menu → Move to folder
3. **View Folder**: Drawer → Select folder
4. **Nested Folders**: Not yet supported (coming soon)

### Searching

- Use **search bar** at top of list
- Searches in titles and content
- Real-time results

### Markdown Shortcuts

#### Toolbar Quick Actions:

- **B**: Bold
- **I**: Italic
- **~**: Strikethrough
- **#**: Heading
- **[**: Link
- **`**: Code

#### Keyboard Shortcuts (in planning):

- `Ctrl+B`: Bold
- `Ctrl+I`: Italic
- `Ctrl+Z`: Undo
- `Ctrl+Y`: Redo

---

## Future Enhancements (Roadmap)

### Phase 2 - Advanced Features:

- [ ] Nested folders
- [ ] Note templates
- [ ] Custom themes
- [ ] Export to PDF/HTML
- [ ] Image attachments
- [ ] Voice notes
- [ ] Widget support
- [ ] Backup/restore

### Phase 3 - Power User Features:

- [ ] Keyboard shortcuts
- [ ] Vim mode
- [ ] Split screen editing
- [ ] Note versioning
- [ ] Advanced search (regex, filters)
- [ ] Custom CSS for preview
- [ ] LaTeX math support
- [ ] Mermaid diagrams

---

## Libraries Used

### Core:

- **Jetpack Compose**: UI framework
- **Room**: Local database
- **Coroutines**: Asynchronous programming
- **Navigation Compose**: Screen navigation

### Markdown:

- **Markwon**: Markdown rendering
    - `markwon-core`: Base rendering
    - `markwon-ext-strikethrough`: ~~Strikethrough~~
    - `markwon-ext-tables`: Tables
    - `markwon-ext-tasklist`: Checklists
    - `markwon-syntax-highlight`: Code highlighting

### Security:

- **Security Crypto**: Note encryption (Android Keystore)
- **DataStore**: Preferences storage

---

## Testing Checklist

### ✅ Note Operations:

- [x] Create note
- [x] Edit note with auto-save
- [x] Delete note
- [x] Rename note
- [x] Duplicate note
- [x] Move note to folder
- [x] Share note
- [x] Pin/favorite note

### ✅ Multi-Select:

- [x] Enter multi-select mode
- [x] Select multiple notes
- [x] Select all
- [x] Batch delete
- [x] Batch move
- [x] Exit multi-select

### ✅ Markdown:

- [x] Bold, italic, strikethrough
- [x] Headings (H1-H6)
- [x] Lists (bullet, numbered, checklist)
- [x] Code blocks
- [x] Links
- [x] Tables
- [x] Preview mode
- [x] Undo/redo

### ✅ UI/UX:

- [x] Smooth animations
- [x] Haptic feedback
- [x] Snackbar notifications
- [x] Dark theme
- [x] Responsive layout

---

## Troubleshooting

### Auto-Save Not Working

- Check if edit mode is active
- Verify 2-second delay has passed
- Check for error toasts

### Multi-Select Issues

- Try clearing selection
- Restart multi-select mode
- Check if notes are visible in current filter

### Markdown Not Rendering

- Toggle preview mode off/on
- Check Markwon syntax
- Verify content is not encrypted

### Performance Issues

- Reduce number of notes (archive old ones)
- Clear search query
- Restart app

---

## Contributing

See `DEVELOPMENT.md` for:

- Code style guidelines
- Architecture patterns
- Testing procedures
- Pull request process

---

## License

MIT License - See LICENSE file

---

**Obby** - Your offline-first Markdown companion 📝
