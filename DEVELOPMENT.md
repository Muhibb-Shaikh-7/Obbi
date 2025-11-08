# Obby - Development Guide

## 🚀 Quick Start for Developers

### Prerequisites

- **Android Studio**: Hedgehog or newer
- **JDK**: 11 or higher
- **Android SDK**: 33+ (min), 36 (target)
- **Kotlin**: 2.0.21

### Setup

```bash
git clone https://github.com/yourusername/obby.git
cd obby
./gradlew build
```

---

## 📋 Recent Enhancements Quick Reference

### 1. Enhanced NotesViewModel

**Location**: `ui/viewmodel/NotesViewModel.kt`

**New Features Added**:

```kotlin
// Multi-select state
val isMultiSelectMode: StateFlow<Boolean>
val selectedNotes: StateFlow<Set<Long>>

// Action feedback
val actionMessage: SharedFlow<String>

// Functions
fun toggleMultiSelectMode()
fun toggleNoteSelection(noteId: Long)
fun selectAllNotes()
fun clearSelection()
fun deleteSelectedNotes()
fun renameNote(note: Note, newTitle: String)
fun duplicateNote(note: Note)
fun moveNoteToFolder(note: Note, folderId: Long?)
fun moveSelectedNotesToFolder(folderId: Long?)
fun createChecklist(title: String, folderId: Long? = null)
fun getShareableContent(noteId: Long): String?
```

**Usage Example**:

```kotlin
// In Composable
val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
val selectedNotes by viewModel.selectedNotes.collectAsState()

// Listen to action messages
LaunchedEffect(Unit) {
    viewModel.actionMessage.collect { message ->
        snackbarHostState.showSnackbar(message)
    }
}

// Toggle multi-select
viewModel.toggleMultiSelectMode()

// Select a note
viewModel.toggleNoteSelection(noteId)

// Batch operations
viewModel.deleteSelectedNotes()
viewModel.moveSelectedNotesToFolder(folderId)
```

---

### 2. Enhanced NoteDetailViewModel

**Location**: `ui/viewmodel/NoteDetailViewModel.kt`

**Existing Features** (already implemented):

```kotlin
// Auto-save
const val AUTO_SAVE_DELAY = 2000L
fun onContentChange(title: String, content: String)
fun saveNow()
val pendingChanges: StateFlow<Boolean>

// Undo/Redo
fun undo(): Pair<String, String>?
fun redo(): Pair<String, String>?
fun canUndo(): Boolean
fun canRedo(): Boolean

// Markdown preview
val showMarkdownPreview: StateFlow<Boolean>
fun toggleMarkdownPreview()

// Text selection
fun updateTextSelection(selection: TextRange)

// Snackbar messages
val snackbarMessage: SharedFlow<String>
```

---

### 3. Markdown Toolbar Component

**Location**: `ui/components/MarkdownToolbar.kt`

**Key Components**:

```kotlin
// Sealed class for actions
sealed class MarkdownActionType {
    object Bold : MarkdownActionType()
    object Italic : MarkdownActionType()
    object Strikethrough : MarkdownActionType()
    data class Heading(val level: Int) : MarkdownActionType()
    object Checklist : MarkdownActionType()
    // ... etc
}

// Main composable
@Composable
fun MarkdownToolbar(
    onAction: (MarkdownActionType) -> Unit
)

// Formatter object
object MarkdownFormatter {
    fun applyFormat(
        currentText: String,
        selection: TextRange,
        actionType: MarkdownActionType
    ): Pair<String, TextRange>
}
```

**Integration Example**:

```kotlin
var contentFieldValue by remember { 
    mutableStateOf(TextFieldValue(note.content)) 
}

Scaffold(
    bottomBar = {
        if (editMode && !showMarkdownPreview) {
            MarkdownToolbar(
                onAction = { action ->
                    val (newText, newSelection) = MarkdownFormatter.applyFormat(
                        currentText = contentFieldValue.text,
                        selection = contentFieldValue.selection,
                        actionType = action
                    )
                    contentFieldValue = TextFieldValue(
                        text = newText,
                        selection = newSelection
                    )
                    viewModel.onContentChange(titleValue.text, newText)
                }
            )
        }
    }
) {
    BasicTextField(
        value = contentFieldValue,
        onValueChange = { newValue ->
            contentFieldValue = newValue
            viewModel.onContentChange(title, newValue.text)
            viewModel.updateTextSelection(newValue.selection)
        }
    )
}
```

---

### 4. Enhanced NotesListScreen

**Location**: `ui/screens/NotesListScreen.kt`

**New Components**:

```kotlin
// Multi-select top bar
@Composable
fun MultiSelectTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit
)

// Enhanced FAB
@Composable
fun EnhancedFAB(
    expanded: Boolean,
    onToggle: () -> Unit,
    onCreateNote: () -> Unit,
    onCreateFolder: () -> Unit,
    onCreateChecklist: () -> Unit
)

// Note list item with long-press
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListItem(
    note: Note,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: (String) -> Unit,
    onDuplicate: () -> Unit,
    onMove: (Long?) -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    folders: List<Folder>
)

// Context menu
@Composable
fun NoteContextMenu(
    expanded: Boolean,
    note: Note,
    onDismiss: () -> Unit,
    onPin: () -> Unit,
    onFavorite: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onMove: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
)
```

**New Dialogs**:

```kotlin
@Composable
fun RenameNoteDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
)

@Composable
fun DeleteConfirmationDialog(
    noteTitle: String? = null,
    count: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
)

@Composable
fun MoveToFolderDialog(
    folders: List<Folder>,
    currentFolderId: Long? = null,
    onDismiss: () -> Unit,
    onFolderSelected: (Long?) -> Unit
)
```

---

## 🏗️ Architecture Overview

### MVVM Pattern

```
UI (Compose) → ViewModel (StateFlow) → Repository → DAO → Database
```

### State Management

```kotlin
// Use StateFlow for UI state
private val _state = MutableStateFlow(initialValue)
val state: StateFlow<Type> = _state.asStateFlow()

// Use SharedFlow for one-time events
private val _event = MutableSharedFlow<Type>()
val event: SharedFlow<Type> = _event.asSharedFlow()

// Collect in Composable
val state by viewModel.state.collectAsState()

LaunchedEffect(Unit) {
    viewModel.event.collect { event ->
        // Handle event
    }
}
```

---

## Table of Contents

1. [Setup](#setup)
2. [Project Structure](#project-structure)
3. [Code Style](#code-style)
4. [Adding Features](#adding-features)
5. [Testing](#testing)
6. [Common Tasks](#common-tasks)
7. [Debugging](#debugging)

---

## Setup

### Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 11 or later
- **Android SDK**: API 33+ (minimum), API 36 (target)
- **Kotlin**: 2.0.21

### Initial Setup

1. **Clone and Open**
   ```bash
   git clone <repository-url>
   cd obby
   ```

2. **Sync Gradle**
    - Open in Android Studio
    - Wait for Gradle sync to complete
    - Resolve any dependency issues

3. **Run on Emulator**
    - Create AVD with API 33+
    - Click Run (Shift+F10)

4. **Enable Developer Options** (Physical Device)
    - Settings → About Phone → Tap Build Number 7 times
    - Enable USB Debugging

---

## Project Structure

```
app/src/main/java/com/example/obby/
├── data/
│   ├── local/
│   │   ├── entity/          # Room entities (database tables)
│   │   │   ├── Note.kt
│   │   │   ├── Folder.kt
│   │   │   ├── Tag.kt
│   │   │   ├── NoteTagCrossRef.kt
│   │   │   ├── NoteLink.kt
│   │   │   └── NoteWithDetails.kt
│   │   ├── dao/             # Data Access Objects
│   │   │   ├── NoteDao.kt
│   │   │   ├── FolderDao.kt
│   │   │   ├── TagDao.kt
│   │   │   └── NoteLinkDao.kt
│   │   └── ObbyDatabase.kt  # Room database definition
│   └── repository/
│       └── NoteRepository.kt # Single source of truth
├── domain/
│   └── model/               # Domain models
│       └── NoteGraph.kt
├── ui/
│   ├── screens/             # Composable screens
│   │   ├── NotesListScreen.kt
│   │   ├── NoteDetailScreen.kt
│   │   └── GraphScreen.kt
│   ├── viewmodel/           # ViewModels
│   │   ├── NotesViewModel.kt
│   │   ├── NoteDetailViewModel.kt
│   │   └── GraphViewModel.kt
│   ├── navigation/
│   │   └── NavGraph.kt      # Navigation setup
│   └── theme/               # Material 3 theming
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/                    # Utility classes
│   ├── MarkdownLinkParser.kt
│   ├── EncryptionManager.kt
│   └── FileExporter.kt
└── MainActivity.kt          # Entry point
```

---

## Code Style

### Kotlin Conventions

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// Good: Clear naming, concise
fun getNoteById(noteId: Long): Flow<Note?> = noteDao.getNoteByIdFlow(noteId)

// Bad: Unclear naming, verbose
fun getTheNoteUsingTheIdParameter(id: Long): Flow<Note?> {
    return noteDao.getNoteByIdFlow(id)
}
```

### Compose Best Practices

**1. Extract Reusable Components**

```kotlin
// Good: Reusable, testable
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.clickable(onClick = onClick)) {
        // ...
    }
}

// Bad: Inline, not reusable
@Composable
fun NotesList() {
    LazyColumn {
        items(notes) { note ->
            Card(modifier = Modifier.clickable { /* ... */ }) {
                // Inline implementation
            }
        }
    }
}
```

**2. State Hoisting**

```kotlin
// Good: State hoisted to parent
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(value = query, onValueChange = onQueryChange)
}

// Bad: State managed internally
@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") }
    TextField(value = query, onValueChange = { query = it })
}
```

**3. Use `remember` for Expensive Computations**

```kotlin
@Composable
fun ExpensiveComponent(data: List<Item>) {
    // Good: Cached result
    val processedData = remember(data) {
        data.map { processItem(it) }
    }
    
    // Bad: Recomputes on every recomposition
    val processedData = data.map { processItem(it) }
}
```

### Room Best Practices

**1. Use Flow for Reactive Queries**

```kotlin
// Good: Reactive, updates automatically
@Query("SELECT * FROM notes")
fun getAllNotes(): Flow<List<Note>>

// Avoid: Requires manual refresh
@Query("SELECT * FROM notes")
suspend fun getAllNotes(): List<Note>
```

**2. Use Transactions for Multiple Operations**

```kotlin
@Transaction
suspend fun insertNoteWithTags(note: Note, tags: List<Tag>) {
    val noteId = insertNote(note)
    tags.forEach { tag ->
        insertNoteTagCrossRef(NoteTagCrossRef(noteId, tag.id))
    }
}
```

**3. Index Frequently Queried Columns**

```kotlin
@Entity(
    tableName = "notes",
    indices = [Index(value = ["title"])]  // Fast title searches
)
data class Note(...)
```

---

## Adding Features

### Example: Adding a "Archive Note" Feature

#### 1. Update Entity

```kotlin
// data/local/entity/Note.kt
@Entity(tableName = "notes")
data class Note(
    // ... existing fields
    val isArchived: Boolean = false  // Add new field
)
```

#### 2. Update Database Version

```kotlin
// data/local/ObbyDatabase.kt
@Database(
    entities = [...],
    version = 2,  // Increment version
    exportSchema = true
)
abstract class ObbyDatabase : RoomDatabase() {
    // Add migration
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}
```

#### 3. Add DAO Method

```kotlin
// data/local/dao/NoteDao.kt
@Query("UPDATE notes SET isArchived = :archived WHERE id = :noteId")
suspend fun setArchived(noteId: Long, archived: Boolean)

@Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY modifiedAt DESC")
fun getArchivedNotes(): Flow<List<Note>>
```

#### 4. Add Repository Method

```kotlin
// data/repository/NoteRepository.kt
suspend fun archiveNote(noteId: Long) {
    noteDao.setArchived(noteId, true)
}

fun getArchivedNotes(): Flow<List<Note>> = noteDao.getArchivedNotes()
```

#### 5. Update ViewModel

```kotlin
// ui/viewmodel/NotesViewModel.kt
fun archiveNote(noteId: Long) {
    viewModelScope.launch {
        repository.archiveNote(noteId)
    }
}
```

#### 6. Add UI Action

```kotlin
// ui/screens/NotesListScreen.kt
DropdownMenuItem(
    text = { Text("Archive") },
    onClick = {
        viewModel.archiveNote(note.id)
        showMenu = false
    },
    leadingIcon = { Icon(Icons.Default.Archive, null) }
)
```

---

## Testing

### Unit Tests

**Example: Repository Test**

```kotlin
class NoteRepositoryTest {
    private lateinit var repository: NoteRepository
    private lateinit var database: ObbyDatabase
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ObbyDatabase::class.java
        ).build()
        
        repository = NoteRepository(
            noteDao = database.noteDao(),
            // ... other DAOs
        )
    }
    
    @Test
    fun `insertNote creates note and links`() = runBlocking {
        val note = Note(title = "Test", content = "Link to [[Another Note]]")
        val noteId = repository.insertNote(note)
        
        assertThat(noteId).isGreaterThan(0)
        // Verify links were created
    }
    
    @After
    fun tearDown() {
        database.close()
    }
}
```

**Example: ViewModel Test**

```kotlin
class NotesViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var viewModel: NotesViewModel
    private lateinit var repository: NoteRepository
    
    @Before
    fun setup() {
        repository = mockk()
        viewModel = NotesViewModel(repository)
    }
    
    @Test
    fun `createNote calls repository`() = runBlocking {
        every { repository.insertNote(any()) } returns 1L
        
        viewModel.createNote("Test Note")
        
        verify { repository.insertNote(match { it.title == "Test Note" }) }
    }
}
```

### UI Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class NotesListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun clickingFabShowsDialog() {
        composeTestRule.setContent {
            NotesListScreen(...)
        }
        
        composeTestRule.onNodeWithContentDescription("New Note").performClick()
        composeTestRule.onNodeWithText("Create Note").assertIsDisplayed()
    }
}
```

---

## Common Tasks

### Adding a New Screen

1. **Create Screen Composable**
   ```kotlin
   // ui/screens/NewScreen.kt
   @Composable
   fun NewScreen(
       repository: NoteRepository,
       onNavigateBack: () -> Unit
   ) {
       Scaffold(
           topBar = { /* ... */ }
       ) { paddingValues ->
           // Content
       }
   }
   ```

2. **Add to Navigation**
   ```kotlin
   // ui/navigation/NavGraph.kt
   sealed class Screen(val route: String) {
       // ... existing screens
       object NewScreen : Screen("new_screen")
   }
   
   composable(Screen.NewScreen.route) {
       NewScreen(repository, onNavigateBack = { navController.popBackStack() })
   }
   ```

### Adding a New Database Table

1. **Create Entity**
2. **Create DAO**
3. **Add to Database**
4. **Increment version**
5. **Add migration**
6. **Update Repository**

### Implementing Search

```kotlin
// 1. Add search state to ViewModel
private val _searchQuery = MutableStateFlow("")
val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

// 2. Combine with data flow
val filteredNotes = combine(allNotes, searchQuery) { notes, query ->
    if (query.isEmpty()) notes
    else notes.filter { it.title.contains(query, ignoreCase = true) }
}

// 3. Add search UI
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        // ...
    )
}
```

---

## Debugging

### Common Issues

**1. Room Database Issues**

```kotlin
// Enable SQL logging
Room.databaseBuilder(context, ObbyDatabase::class.java, "obby_database")
    .setQueryCallback({ sqlQuery, bindArgs ->
        Log.d("RoomDB", "SQL Query: $sqlQuery SQL Args: $bindArgs")
    }, Executors.newSingleThreadExecutor())
    .build()
```

**2. Flow Not Updating**

```kotlin
// Ensure Flow is collected in correct lifecycle scope
LaunchedEffect(Unit) {
    viewModel.notes.collect { notes ->
        // Handle updates
    }
}

// Or use collectAsState in Compose
val notes by viewModel.notes.collectAsState()
```

**3. Compose Recomposition Issues**

```kotlin
// Use derivedStateOf for computed values
val filteredNotes by remember {
    derivedStateOf {
        notes.filter { it.isPinned }
    }
}
```

### Logging Best Practices

```kotlin
// Use Timber or Android Log
import android.util.Log

private const val TAG = "NotesViewModel"

fun logDebug(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(TAG, message)
    }
}
```

### Performance Profiling

1. **Android Studio Profiler**
    - View → Tool Windows → Profiler
    - Monitor CPU, Memory, Network

2. **Layout Inspector**
    - View → Tool Windows → Layout Inspector
    - Inspect Compose hierarchy

3. **Database Inspector**
    - View → Tool Windows → App Inspection
    - View Room database contents

---

## Best Practices Summary

1. **Use dependency injection** (or factory pattern for simplicity)
2. **Follow MVVM strictly** - no business logic in UI
3. **Use Flow for reactive data**
4. **Keep ViewModels lifecycle-aware**
5. **Write unit tests for business logic**
6. **Use Compose preview for rapid UI iteration**
7. **Handle errors gracefully** with sealed classes or Result types
8. **Document complex logic** with comments
9. **Keep composables small and focused**
10. **Use Material 3 components** for consistency

---

## Resources

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose Guide](https://developer.android.com/jetpack/compose)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture)
- [Material Design 3](https://m3.material.io/)

---

## Getting Help

- **Issues**: Open a GitHub issue
- **Discussions**: Use GitHub Discussions
- **Code Review**: Submit a draft PR for feedback

---

Happy coding! 🚀
