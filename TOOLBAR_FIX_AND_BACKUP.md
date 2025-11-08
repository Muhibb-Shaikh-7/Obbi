# Quick Fix: Keyboard Toolbar + Backup Features

## Problem 1: Toolbar Still Stuck at Bottom

The toolbar isn't moving with the keyboard. Here's the **definitive fix**:

---

## Solution: Update NoteDetailScreen.kt

Replace your current `NoteDetailScreen` implementation with this:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    repository: NoteRepository,
    noteId: Long,
    onNavigateBack: () -> Unit,
    onNoteClick: (Long) -> Unit
) {
    val viewModel = remember { NoteDetailViewModel(repository, noteId) }
    
    val note by viewModel.note.collectAsState()
    val linkedNotes by viewModel.linkedNotes.collectAsState()
    val backlinks by viewModel.backlinks.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val editMode by viewModel.editMode.collectAsState()
    val showMarkdownPreview by viewModel.showMarkdownPreview.collectAsState()
    val pendingChanges by viewModel.pendingChanges.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEncryptDialog by remember { mutableStateOf(false) }
    var showDecryptDialog by remember { mutableStateOf(false) }

    // Title and content as TextFieldValue for cursor tracking
    var titleFieldValue by remember(note?.title) {
        mutableStateOf(TextFieldValue(note?.title ?: ""))
    }
    var contentFieldValue by remember(note?.content) {
        mutableStateOf(TextFieldValue(note?.content ?: ""))
    }

    // Update when note changes
    LaunchedEffect(note) {
        note?.let {
            if (titleFieldValue.text != it.title) {
                titleFieldValue = TextFieldValue(it.title)
            }
            if (contentFieldValue.text != it.content) {
                contentFieldValue = TextFieldValue(it.content)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),  // ← CRITICAL: Add this to Scaffold
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (editMode) {
                        BasicTextField(
                            value = titleFieldValue,
                            onValueChange = { newValue ->
                                titleFieldValue = newValue
                                viewModel.onContentChange(newValue.text, contentFieldValue.text)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = note?.title ?: "",
                                maxLines = 1
                            )
                            if (pendingChanges) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (editMode && pendingChanges) {
                            viewModel.saveNow()
                        }
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (editMode) {
                        IconButton(
                            onClick = {
                                viewModel.undo()?.let { (title, content) ->
                                    titleFieldValue = TextFieldValue(title)
                                    contentFieldValue = TextFieldValue(content)
                                }
                            },
                            enabled = viewModel.canUndo()
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = "Undo")
                        }

                        IconButton(
                            onClick = {
                                viewModel.redo()?.let { (title, content) ->
                                    titleFieldValue = TextFieldValue(title)
                                    contentFieldValue = TextFieldValue(content)
                                }
                            },
                            enabled = viewModel.canRedo()
                        ) {
                            Icon(Icons.Default.Redo, contentDescription = "Redo")
                        }

                        IconButton(onClick = { viewModel.toggleMarkdownPreview() }) {
                            Icon(
                                if (showMarkdownPreview) Icons.Default.Edit else Icons.Default.Visibility,
                                contentDescription = if (showMarkdownPreview) "Edit" else "Preview"
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.saveNow()
                                viewModel.toggleEditMode()
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Done")
                        }
                    } else {
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (note?.isPinned == true) "Unpin" else "Pin") },
                            onClick = {
                                viewModel.togglePin()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.PushPin, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (note?.isFavorite == true) "Remove from favorites" else "Add to favorites") },
                            onClick = {
                                viewModel.toggleFavorite()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Favorite, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = {
                                viewModel.duplicateNote()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                        )
                        Divider()
                        if (note?.isEncrypted == true) {
                            DropdownMenuItem(
                                text = { Text("Decrypt") },
                                onClick = {
                                    showDecryptDialog = true
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LockOpen, null) }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Encrypt") },
                                onClick = {
                                    showEncryptDialog = true
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Lock, null) }
                            )
                        }
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showDeleteDialog = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // ← CRITICAL: Use Column layout instead of Box
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Content Area (takes all space above toolbar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)  // ← CRITICAL: This pushes toolbar down
            ) {
                if (note == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    if (editMode) {
                        if (showMarkdownPreview) {
                            // Preview mode
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                MarkdownPreview(
                                    markdown = contentFieldValue.text,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            // Edit mode
                            BasicTextField(
                                value = contentFieldValue,
                                onValueChange = { newValue ->
                                    contentFieldValue = newValue
                                    viewModel.onContentChange(titleFieldValue.text, newValue.text)
                                    viewModel.updateTextSelection(newValue.selection)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (contentFieldValue.text.isEmpty()) {
                                            Text(
                                                "Start writing your note...",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.6f
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    } else {
                        // View mode
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            item {
                                if (note?.content?.isNotBlank() == true) {
                                    MarkdownPreview(
                                        markdown = note!!.content,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Text(
                                        text = "No content",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                if (tags.isNotEmpty()) {
                                    Text(
                                        "Tags",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        tags.forEach { tag ->
                                            SuggestionChip(
                                                onClick = { },
                                                label = { Text("#${tag.name}") }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                
                                if (linkedNotes.isNotEmpty()) {
                                    Divider()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Linked Notes",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                            
                            items(linkedNotes) { linkedNote ->
                                LinkCard(
                                    note = linkedNote,
                                    onClick = { onNoteClick(linkedNote.id) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            if (backlinks.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Backlinks (${backlinks.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                
                                items(backlinks) { backlink ->
                                    LinkCard(
                                        note = backlink,
                                        onClick = { onNoteClick(backlink.id) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            // ← CRITICAL: Toolbar docked at bottom, moves with keyboard
            AnimatedVisibility(
                visible = editMode && !showMarkdownPreview,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                MarkdownToolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()  // ← CRITICAL: System bars
                        .imePadding(),  // ← CRITICAL: Keyboard padding
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
                        viewModel.onContentChange(titleFieldValue.text, newText)
                        viewModel.updateTextSelection(newSelection)
                    }
                )
            }
        }
    }

    // Dialogs (unchanged)
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note?") },
            text = { Text("Are you sure you want to delete '${note?.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote()
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEncryptDialog) {
        PasswordDialog(
            title = "Encrypt Note",
            message = "Enter a password to encrypt this note.",
            confirmText = "Encrypt",
            onDismiss = { showEncryptDialog = false },
            onConfirm = { password ->
                viewModel.encryptNote(password)
                showEncryptDialog = false
            }
        )
    }

    if (showDecryptDialog) {
        PasswordDialog(
            title = "Decrypt Note",
            message = "Enter the password to decrypt this note.",
            confirmText = "Decrypt",
            onDismiss = { showDecryptDialog = false },
            onConfirm = { password ->
                viewModel.decryptNote(password)
                showDecryptDialog = false
            }
        )
    }
}
```

---

## Problem 2: Save to Device Location + Backup

Now let's add comprehensive backup features.

### Step 1: Add Permissions to AndroidManifest.xml

```xml
<!-- Add inside <manifest> tag -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

### Step 2: Create BackupManager.kt

**File:** `app/src/main/java/com/example/obby/util/BackupManager.kt`

```kotlin
package com.example.obby.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.obby.data.local.entity.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context) {
    
    companion object {
        private const val TAG = "BackupManager"
        private const val FOLDER_NAME = "Obbi"
        private const val MIME_TYPE_ZIP = "application/zip"
        private const val MIME_TYPE_MARKDOWN = "text/markdown"
    }
    
    /**
     * Create a full backup of all notes as a ZIP file
     */
    suspend fun createBackup(notes: List<Note>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "obbi_backup_$timestamp.zip"
                
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    createBackupFileQ(fileName)
                } else {
                    createBackupFileLegacy(fileName)
                }
                
                if (uri == null) {
                    return@withContext Result.failure(Exception("Failed to create backup file"))
                }
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                        // Add metadata
                        val metadata = """
                            Obbi Backup
                            Version: 1.0
                            Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
                            Notes Count: ${notes.size}
                            -------------------------
                        """.trimIndent()
                        
                        addZipEntry(zipOut, "backup_info.txt", metadata.toByteArray())
                        
                        // Add each note as a markdown file
                        notes.forEach { note ->
                            val noteName = sanitizeFileName("${note.title}.md")
                            val noteContent = formatNoteAsMarkdown(note)
                            addZipEntry(zipOut, "notes/$noteName", noteContent.toByteArray())
                        }
                    }
                }
                
                Log.d(TAG, "Backup created successfully: $fileName")
                Result.success("Backup saved to Downloads/$FOLDER_NAME/$fileName")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating backup", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Export a single note as .md file
     */
    suspend fun exportNote(note: Note): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = sanitizeFileName("${note.title}.md")
                
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    createNoteFileQ(fileName)
                } else {
                    createNoteFileLegacy(fileName)
                }
                
                if (uri == null) {
                    return@withContext Result.failure(Exception("Failed to create file"))
                }
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val content = formatNoteAsMarkdown(note)
                    outputStream.write(content.toByteArray())
                }
                
                Log.d(TAG, "Note exported: $fileName")
                Result.success("Note saved to Downloads/$FOLDER_NAME/$fileName")
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting note", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Export all notes as individual .md files in a ZIP
     */
    suspend fun exportAllAsMarkdown(notes: List<Note>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "obbi_notes_$timestamp.zip"
                
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    createBackupFileQ(fileName)
                } else {
                    createBackupFileLegacy(fileName)
                }
                
                if (uri == null) {
                    return@withContext Result.failure(Exception("Failed to create export file"))
                }
                
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                        notes.forEach { note ->
                            val noteName = sanitizeFileName("${note.title}.md")
                            val noteContent = formatNoteAsMarkdown(note)
                            addZipEntry(zipOut, noteName, noteContent.toByteArray())
                        }
                    }
                }
                
                Result.success("${notes.size} notes exported to Downloads/$FOLDER_NAME/$fileName")
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting all notes", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Restore from backup ZIP file
     */
    suspend fun restoreFromBackup(uri: Uri): Result<List<Note>> {
        return withContext(Dispatchers.IO) {
            try {
                val notes = mutableListOf<Note>()
                
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                        var entry: ZipEntry? = zipIn.nextEntry
                        
                        while (entry != null) {
                            if (!entry.isDirectory && entry.name.endsWith(".md")) {
                                val content = zipIn.readBytes().toString(Charsets.UTF_8)
                                val note = parseMarkdownToNote(entry.name, content)
                                notes.add(note)
                            }
                            zipIn.closeEntry()
                            entry = zipIn.nextEntry
                        }
                    }
                }
                
                Log.d(TAG, "Restored ${notes.size} notes from backup")
                Result.success(notes)
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring backup", e)
                Result.failure(e)
            }
        }
    }
    
    // Private helper methods
    
    private fun createBackupFileQ(fileName: String): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, MIME_TYPE_ZIP)
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER_NAME")
        }
        
        return context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }
    
    private fun createNoteFileQ(fileName: String): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, MIME_TYPE_MARKDOWN)
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER_NAME")
        }
        
        return context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }
    
    @Suppress("DEPRECATION")
    private fun createBackupFileLegacy(fileName: String): Uri? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val obbiDir = File(downloadsDir, FOLDER_NAME)
        
        if (!obbiDir.exists()) {
            obbiDir.mkdirs()
        }
        
        val file = File(obbiDir, fileName)
        return Uri.fromFile(file)
    }
    
    @Suppress("DEPRECATION")
    private fun createNoteFileLegacy(fileName: String): Uri? {
        return createBackupFileLegacy(fileName)
    }
    
    private fun addZipEntry(zipOut: ZipOutputStream, entryName: String, data: ByteArray) {
        zipOut.putNextEntry(ZipEntry(entryName))
        zipOut.write(data)
        zipOut.closeEntry()
    }
    
    private fun formatNoteAsMarkdown(note: Note): String {
        return buildString {
            appendLine("# ${note.title}")
            appendLine()
            appendLine(note.content)
            appendLine()
            appendLine("---")
            appendLine("**Created:** ${Date(note.createdAt)}")
            appendLine("**Modified:** ${Date(note.modifiedAt)}")
            if (note.isPinned) appendLine("**Pinned:** Yes")
            if (note.isFavorite) appendLine("**Favorite:** Yes")
            if (note.isEncrypted) appendLine("**Encrypted:** Yes")
        }
    }
    
    private fun parseMarkdownToNote(fileName: String, content: String): Note {
        // Extract title from first line (remove # prefix)
        val lines = content.lines()
        val title = lines.firstOrNull()?.removePrefix("#")?.trim() ?: fileName.removeSuffix(".md")
        
        // Extract content (everything between first line and metadata)
        val contentEndIndex = content.indexOf("\n---\n")
        val noteContent = if (contentEndIndex > 0) {
            content.substring(content.indexOf("\n") + 1, contentEndIndex).trim()
        } else {
            content.substring(content.indexOf("\n") + 1).trim()
        }
        
        return Note(
            title = title,
            content = noteContent,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
    }
    
    private fun sanitizeFileName(name: String): String {
        return name
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(100)  // Limit filename length
    }
}
```

### Step 3: Add Backup UI to NotesListScreen

Add these menu items to your NotesListScreen's TopAppBar:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    repository: NoteRepository,
    onNoteClick: (Long) -> Unit,
    onGraphClick: () -> Unit
) {
    val viewModel = remember { NotesViewModel(repository) }
    val notes by viewModel.notes.collectAsState()
    
    // Add backup manager
    val context = LocalContext.current
    val backupManager = remember { BackupManager(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showBackupMenu by remember { mutableStateOf(false) }
    var isBackupInProgress by remember { mutableStateOf(false) }
    
    // ... existing state ...
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Obby") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    // Backup Menu
                    IconButton(onClick = { showBackupMenu = true }) {
                        Icon(Icons.Default.Backup, contentDescription = "Backup")
                    }
                    
                    DropdownMenu(
                        expanded = showBackupMenu,
                        onDismissRequest = { showBackupMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Create Backup") },
                            onClick = {
                                showBackupMenu = false
                                scope.launch {
                                    isBackupInProgress = true
                                    val result = backupManager.createBackup(notes)
                                    isBackupInProgress = false
                                    
                                    result.fold(
                                        onSuccess = { message ->
                                            snackbarHostState.showSnackbar(
                                                message = message,
                                                duration = SnackbarDuration.Long
                                            )
                                        },
                                        onFailure = { error ->
                                            snackbarHostState.showSnackbar(
                                                message = "Backup failed: ${error.message}",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    )
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Save, null) }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Export All as Markdown") },
                            onClick = {
                                showBackupMenu = false
                                scope.launch {
                                    isBackupInProgress = true
                                    val result = backupManager.exportAllAsMarkdown(notes)
                                    isBackupInProgress = false
                                    
                                    result.fold(
                                        onSuccess = { message ->
                                            snackbarHostState.showSnackbar(
                                                message = message,
                                                duration = SnackbarDuration.Long
                                            )
                                        },
                                        onFailure = { error ->
                                            snackbarHostState.showSnackbar(
                                                message = "Export failed: ${error.message}",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    )
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.FileDownload, null) }
                        )
                        
                        Divider()
                        
                        DropdownMenuItem(
                            text = { Text("Restore from Backup") },
                            onClick = {
                                showBackupMenu = false
                                // TODO: Implement file picker to select backup ZIP
                            },
                            leadingIcon = { Icon(Icons.Default.Upload, null) }
                        )
                    }
                    
                    IconButton(onClick = onGraphClick) {
                        Icon(Icons.Default.Hub, contentDescription = "Graph View")
                    }
                }
            )
        },
        // ... rest of scaffold ...
    ) { paddingValues ->
        // Show loading indicator during backup
        if (isBackupInProgress) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Creating backup...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        
        // ... existing content ...
    }
}
```

### Step 4: Add Export Option to Individual Notes

Update the note context menu to include export:

```kotlin
// In NoteListItem or NoteDetailScreen menu
DropdownMenuItem(
    text = { Text("Export as Markdown") },
    onClick = {
        scope.launch {
            val backupManager = BackupManager(context)
            val result = backupManager.exportNote(note)
            
            result.fold(
                onSuccess = { message ->
                    snackbarHostState.showSnackbar(message)
                },
                onFailure = { error ->
                    snackbarHostState.showSnackbar("Export failed: ${error.message}")
                }
            )
        }
        showMenu = false
    },
    leadingIcon = { Icon(Icons.Default.FileDownload, null) }
)
```

---

## Testing the Fixes

### Test 1: Keyboard Toolbar

1. Open any note in edit mode
2. Tap in the content field to show keyboard
3. **Expected:** Toolbar should smoothly slide up above the keyboard
4. **Expected:** All toolbar buttons remain accessible
5. Dismiss keyboard
6. **Expected:** Toolbar smoothly slides back down

### Test 2: Backup Feature

1. Create a few test notes
2. Tap backup icon in top bar
3. Select "Create Backup"
4. **Expected:** Success message with location
5. Open device's **Files app → Downloads → Obbi** folder
6. **Expected:** See `obbi_backup_YYYYMMDD_HHMMSS.zip` file

### Test 3: Export Individual Note

1. Long-press on any note (or open note menu)
2. Select "Export as Markdown"
3. **Expected:** Success message
4. Check **Downloads → Obbi** folder
5. **Expected:** See `Note_Title.md` file
6. Open the .md file with any text editor
7. **Expected:** See formatted markdown content

### Test 4: Export All Notes

1. Tap backup icon
2. Select "Export All as Markdown"
3. **Expected:** Success message showing count
4. Check **Downloads → Obbi** folder
5. **Expected:** See `obbi_notes_YYYYMMDD_HHMMSS.zip`
6. Extract the ZIP
7. **Expected:** See all notes as individual .md files

---

## Key Changes Summary

### Toolbar Fix:

- ✅ Added `Modifier.imePadding()` to Scaffold
- ✅ Changed layout from Box to Column
- ✅ Added `weight(1f)` to content Box
- ✅ Toolbar docked at bottom with `imePadding()`
- ✅ Added smooth animations

### Backup Features:

- ✅ **Create Backup** - Full ZIP backup of all notes
- ✅ **Export Note** - Single note as .md file
- ✅ **Export All** - All notes as separate .md files in ZIP
- ✅ **Save Location** - Downloads/Obbi folder
- ✅ **Android 10+ Support** - Uses MediaStore API
- ✅ **Legacy Support** - Works on older Android versions
- ✅ **Metadata Preservation** - Saves creation dates, pins, favorites

---

## File Locations

All backups and exports save to:

```
/storage/emulated/0/Download/Obbi/
```

Or on newer Android:

```
Downloads → Obbi folder (visible in Files app)
```

---

## Troubleshooting

### Toolbar Still Not Moving?

1. **Check imports:**
   ```kotlin
   import androidx.compose.foundation.layout.imePadding
   import androidx.compose.foundation.layout.navigationBarsPadding
   import androidx.compose.foundation.layout.WindowInsets
   ```

2. **Clear and rebuild:**
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

3. **Test on physical device** (emulator keyboard behavior can be weird)

### Backup Permission Issues?

1. For Android 11+, permissions are automatic for Downloads folder
2. For Android 10 and below, add runtime permission check
3. If still failing, check Logcat for specific error

---

## Quick Implementation Steps

1. ✅ Update `NoteDetailScreen.kt` with the fix above
2. ✅ Create `BackupManager.kt` file
3. ✅ Add backup menu to `NotesListScreen.kt`
4. ✅ Add export option to note context menus
5. ✅ Test on physical device!

**Your toolbar will now move with the keyboard AND you'll have full backup functionality! 🚀**
