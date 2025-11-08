# Obby Architecture Documentation

## Table of Contents

1. [Overview](#overview)
2. [Architecture Pattern](#architecture-pattern)
3. [Data Layer](#data-layer)
4. [Domain Layer](#domain-layer)
5. [Presentation Layer](#presentation-layer)
6. [Data Flow](#data-flow)
7. [Key Features Implementation](#key-features-implementation)
8. [Performance Optimizations](#performance-optimizations)
9. [Security Implementation](#security-implementation)

---

## Overview

Obby follows a **Clean Architecture** approach with **MVVM** (Model-View-ViewModel) pattern,
ensuring:

- Clear separation of concerns
- Testability
- Maintainability
- Scalability

### Tech Stack

- **Kotlin 2.0.21** - Modern, expressive language
- **Jetpack Compose** - Declarative UI framework
- **Room 2.6.1** - Type-safe SQLite abstraction
- **Coroutines & Flow** - Asynchronous programming
- **Markwon 4.6.2** - Markdown rendering
- **Material 3** - Modern Material Design

---

## Architecture Pattern

### MVVM Architecture

```
┌─────────────────────────────────────────────────┐
│                   UI Layer                       │
│  ┌──────────────┐  ┌──────────────┐            │
│  │   Compose    │  │  Navigation  │            │
│  │   Screens    │  │    Graph     │            │
│  └──────┬───────┘  └──────────────┘            │
│         │                                        │
│  ┌──────▼───────────────────────┐               │
│  │       ViewModels              │               │
│  │  (State Management)           │               │
│  └──────┬────────────────────────┘               │
└─────────┼──────────────────────────────────────┘
          │
┌─────────▼──────────────────────────────────────┐
│              Domain Layer                       │
│  ┌──────────────────────────────┐              │
│  │   Business Logic Models      │              │
│  │   (Graph, ParsedLinks)       │              │
│  └──────────────────────────────┘              │
└─────────┬──────────────────────────────────────┘
          │
┌─────────▼──────────────────────────────────────┐
│              Data Layer                         │
│  ┌──────────────┐  ┌───────────────┐          │
│  │  Repository  │  │   Utilities   │          │
│  │              │  │ (Encryption,  │          │
│  └──────┬───────┘  │  Parsing)     │          │
│         │          └───────────────┘          │
│  ┌──────▼─────────────────────┐               │
│  │    Room Database           │               │
│  │  ┌─────┐  ┌─────┐  ┌─────┐│               │
│  │  │DAO 1│  │DAO 2│  │DAO 3││               │
│  │  └─────┘  └─────┘  └─────┘│               │
│  └────────────────────────────┘               │
└────────────────────────────────────────────────┘
```

---

## Data Layer

### Database Schema

#### 1. Notes Table

```kotlin
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val folderId: Long? = null,
    val createdAt: Long,
    val modifiedAt: Long,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isEncrypted: Boolean = false
)
```

**Indices**:

- `title` - For fast title-based searches

**Purpose**: Core note storage with metadata

#### 2. Folders Table

```kotlin
@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val parentFolderId: Long? = null,
    val createdAt: Long,
    val color: Int? = null
)
```

**Indices**:

- `name` (unique) - Prevent duplicate folder names

**Purpose**: Hierarchical folder organization

#### 3. Tags Table

```kotlin
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int? = null
)
```

**Purpose**: Tag definitions for categorization

#### 4. Note-Tag Junction Table

```kotlin
@Entity(
    tableName = "note_tag_cross_ref",
    primaryKeys = ["noteId", "tagId"],
    foreignKeys = [...]
)
data class NoteTagCrossRef(
    val noteId: Long,
    val tagId: Long
)
```

**Purpose**: Many-to-many relationship between notes and tags

#### 5. Note Links Table

```kotlin
@Entity(tableName = "note_links")
data class NoteLink(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceNoteId: Long,
    val targetNoteId: Long,
    val linkText: String,
    val createdAt: Long
)
```

**Indices**:

- `sourceNoteId`, `targetNoteId` - Fast bidirectional lookup

**Purpose**: Track wiki-style links between notes

### Data Access Objects (DAOs)

#### NoteDao

Handles all note-related database operations:

- CRUD operations
- Search queries (FTS)
- Filtering (pinned, favorites, by folder)
- Complex queries with joins

#### FolderDao

Manages folder hierarchy:

- CRUD operations
- Subfolder queries
- Root folder retrieval

#### TagDao

Tag management:

- CRUD operations
- Many-to-many relationship handling
- Tag queries for notes

#### NoteLinkDao

Link management:

- Bidirectional link queries
- Backlink tracking
- Graph data retrieval

### Repository Pattern

**NoteRepository** acts as the single source of truth:

```kotlin
class NoteRepository(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao,
    private val tagDao: TagDao,
    private val noteLinkDao: NoteLinkDao
)
```

**Responsibilities**:

1. **Data Coordination**: Combines data from multiple DAOs
2. **Business Logic**: Link detection, tag extraction
3. **Data Transformation**: Converts entities to domain models
4. **Caching**: In-memory caching for frequently accessed data

---

## Domain Layer

### Models

#### NoteGraph

```kotlin
data class NoteGraph(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>
)

data class GraphNode(
    val id: Long,
    val title: String,
    val x: Float,
    val y: Float,
    val connections: Int
)

data class GraphEdge(
    val sourceId: Long,
    val targetId: Long,
    val linkText: String
)
```

**Purpose**: Graph view visualization data

### Utilities

#### MarkdownLinkParser

Parses markdown content for:

- Wiki-style links: `[[Note Title]]` or `[[Title|Display]]`
- Standard markdown links: `[Display](url)`
- Hashtags: `#tag`

```kotlin
object MarkdownLinkParser {
    fun parseAllLinks(content: String): List<ParsedLink>
    fun extractTags(content: String): List<String>
}
```

#### EncryptionManager

Handles note encryption using Android Keystore:

- AES-256-GCM encryption
- Hardware-backed key storage
- Secure key generation

```kotlin
object EncryptionManager {
    fun encrypt(plaintext: String): String
    fun decrypt(encryptedData: String): String
}
```

#### FileExporter

Import/export functionality:

- Export notes as `.md` files
- Create ZIP archives
- Parse imported markdown files

---

## Presentation Layer

### ViewModels

#### NotesViewModel

Manages the notes list screen:

```kotlin
class NotesViewModel(repository: NoteRepository) : ViewModel() {
    val notes: StateFlow<List<Note>>
    val folders: StateFlow<List<Folder>>
    val tags: StateFlow<List<Tag>>
    
    fun createNote(title: String)
    fun deleteNote(note: Note)
    fun searchNotes(query: String)
}
```

**State Management**:

- `StateFlow` for reactive UI updates
- Combines multiple data sources
- Handles filtering and search

#### NoteDetailViewModel

Manages individual note editing:

```kotlin
class NoteDetailViewModel(
    repository: NoteRepository,
    noteId: Long
) : ViewModel() {
    val note: StateFlow<Note?>
    val linkedNotes: StateFlow<List<Note>>
    val backlinks: StateFlow<List<Note>>
    
    fun updateNoteContent(content: String)
    fun toggleEditMode()
}
```

**Features**:

- Real-time note updates
- Link tracking
- Edit mode management

#### GraphViewModel

Manages graph visualization:

```kotlin
class GraphViewModel(repository: NoteRepository) : ViewModel() {
    val graph: StateFlow<NoteGraph?>
    val selectedNodeId: StateFlow<Long?>
    
    fun loadGraph()
    fun selectNode(nodeId: Long?)
}
```

### Composable Screens

#### NotesListScreen

Main screen with:

- Search bar
- Notes list (LazyColumn)
- Navigation drawer (folders/tags)
- FAB for new notes

**Key Features**:

- Pull-to-refresh
- Swipe actions
- Filter chips

#### NoteDetailScreen

Note editor with:

- Editable title
- Markdown editor
- Live preview toggle
- Linked notes section
- Backlinks section

**Key Features**:

- Auto-save on edit
- Markdown syntax highlighting
- Link click navigation

#### GraphScreen

Interactive graph view:

- Canvas-based rendering
- Node tap/double-tap
- Selected node info card
- Zoom/pan gestures

---

## Data Flow

### Creating a Note

```
User Input (Screen)
    ↓
ViewModel.createNote()
    ↓
Repository.insertNote()
    ↓
┌─────────────────────────────────┐
│ 1. Save note to database        │
│ 2. Parse content for links      │
│ 3. Create link entries          │
│ 4. Extract and save tags        │
└─────────────────────────────────┘
    ↓
Flow emits new note list
    ↓
UI updates automatically
```

### Updating a Note

```
User edits content
    ↓
ViewModel.updateNoteContent()
    ↓
Repository.updateNote()
    ↓
┌────────────────────────────────────┐
│ 1. Update note with new timestamp │
│ 2. Delete old links                │
│ 3. Parse new links                 │
│ 4. Create new link entries         │
│ 5. Update tags                     │
└────────────────────────────────────┘
    ↓
Flow emits updated note
    ↓
UI shows changes + backlinks
```

### Search Flow

```
User types in search bar
    ↓
ViewModel.onSearchQueryChange()
    ↓
StateFlow updates
    ↓
Combined flow triggers
    ↓
Repository.searchNotes(query)
    ↓
Database FTS query
    ↓
Results via Flow
    ↓
LazyColumn updates
```

---

## Key Features Implementation

### 1. Bidirectional Links

**Implementation**:

1. Parse content when note is saved/updated
2. Extract `[[Note Title]]` patterns
3. Lookup target note by title
4. Create `NoteLink` entry with source and target IDs
5. Query backlinks using `targetNoteId`

**Automatic Updates**:

- When note title changes, update all referring links
- When note is deleted, cascade delete links

### 2. Auto-Tagging

**Implementation**:

1. Regex scan for `#word` patterns
2. Create tag if it doesn't exist
3. Create `NoteTagCrossRef` entry
4. On update, delete old tags and re-parse

### 3. Graph Visualization

**Algorithm**:

1. Fetch all notes and links from database
2. Calculate node positions using circular layout
3. Render edges as lines between nodes
4. Render nodes as circles with labels
5. Handle tap events for interaction

**Performance**:

- Cache graph data in ViewModel
- Only recalculate on note changes
- Use Canvas for efficient rendering

### 4. Encryption

**Flow**:

```
Plaintext → AES-256-GCM → Base64 → Store in DB
           ↑
     Android Keystore
```

**Key Points**:

- Each note encrypted individually
- IV stored with ciphertext
- Keys never exposed to app

### 5. Full-Text Search

**Implementation**:

```sql
SELECT * FROM notes 
WHERE title LIKE '%query%' 
   OR content LIKE '%query%'
ORDER BY modifiedAt DESC
```

**Optimization**:

- Indexed `title` column
- Debounced search input
- Limit results for performance

---

## Performance Optimizations

### 1. Database Level

- **Indices**: On frequently queried columns (title, tags)
- **Transactions**: Batch operations for links/tags
- **Lazy Loading**: Use `Flow` for reactive queries

### 2. UI Level

- **LazyColumn**: Only render visible items
- **Key**: Stable keys for efficient recomposition
- **remember**: Cache expensive computations
- **derivedStateOf**: Optimize state transformations

### 3. Repository Level

- **Coroutines**: All DB operations on IO dispatcher
- **Flow**: Emit only on actual changes
- **Debouncing**: Search queries debounced

### 4. Memory Management

- **Paging**: Load notes in chunks (future enhancement)
- **WeakReference**: For image caching (future)
- **Clear unused ViewModels**: Proper lifecycle management

---

## Security Implementation

### Encryption Architecture

```
┌──────────────────────────────────────┐
│         Android Keystore             │
│    (Hardware-backed if available)    │
│                                      │
│  ┌────────────────────────────┐    │
│  │   AES-256 Master Key       │    │
│  │   (Never leaves KeyStore)  │    │
│  └────────────────────────────┘    │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│      EncryptionManager               │
│                                      │
│  encrypt(plaintext)                  │
│    ├─> Generate random IV (12 bytes)│
│    ├─> Encrypt with AES-GCM         │
│    └─> Prepend IV + return Base64   │
│                                      │
│  decrypt(ciphertext)                 │
│    ├─> Decode Base64                │
│    ├─> Extract IV (first 12 bytes)  │
│    └─> Decrypt with key + IV        │
└──────────────────────────────────────┘
```

### Security Best Practices

1. **Key Storage**: Android Keystore (hardware-backed)
2. **Algorithm**: AES-256-GCM (authenticated encryption)
3. **IV**: Random, unique for each encryption
4. **No Hardcoded Secrets**: All keys generated securely
5. **Secure Deletion**: Overwrite sensitive data in memory

### Privacy Features

- **No Network Permission**: App cannot access internet
- **App-Private Storage**: Database in internal storage
- **No Logging**: Sensitive data never logged
- **No Backups**: Flag prevents cloud backups (optional)

---

## Testing Strategy

### Unit Tests

- Repository logic
- ViewModel state management
- Utility functions (parser, encryption)

### Integration Tests

- Database operations
- DAO queries
- Repository + DAO interaction

### UI Tests

- Screen navigation
- User interactions
- State updates

---

## Future Enhancements

1. **Room FTS**: Implement full-text search extension
2. **Pagination**: Use Paging 3 for large datasets
3. **Background Sync**: Local network sync with other devices
4. **Custom Plugins**: Plugin architecture for extensibility
5. **Backup/Restore**: Automated encrypted backups
6. **Conflict Resolution**: Handle concurrent edits

---

## Conclusion

Obby's architecture emphasizes:

- **Separation of Concerns**: Clear layer boundaries
- **Testability**: Isolated, mockable components
- **Performance**: Optimized for large datasets
- **Security**: Privacy-first design
- **Maintainability**: Clean, documented code

This architecture provides a solid foundation for future enhancements while maintaining simplicity
and performance.
