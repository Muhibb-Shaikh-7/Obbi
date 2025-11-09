package com.example.obby.ui.screens

import android.content.Intent
import android.widget.TextView
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.obby.data.local.entity.Folder
import com.example.obby.data.local.entity.Note
import com.example.obby.data.repository.NoteRepository
import com.example.obby.ui.viewmodel.NotesViewModel
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    repository: NoteRepository,
    onNoteClick: (Long) -> Unit,
    onGraphClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val viewModel = remember { NotesViewModel(repository) }
    val notes by viewModel.notes.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedNotes by viewModel.selectedNotes.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showCreateNoteDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showCreateChecklistDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showMoveToFolderDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }

    // Listen to action messages
    LaunchedEffect(Unit) {
        viewModel.actionMessage.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    folders = folders,
                    tags = tags,
                    onFolderClick = { folderId ->
                        viewModel.selectFolder(folderId)
                        scope.launch { drawerState.close() }
                    },
                    onTagClick = { tagId ->
                        viewModel.selectTag(tagId)
                        scope.launch { drawerState.close() }
                    },
                    onViewModeChange = { mode ->
                        viewModel.setViewMode(mode)
                        scope.launch { drawerState.close() }
                    },
                    onCreateFolder = { showCreateFolderDialog = true },
                    onSettingsClick = { onSettingsClick() }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                AnimatedVisibility(
                    visible = isMultiSelectMode,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    MultiSelectTopBar(
                        selectedCount = selectedNotes.size,
                        onClearSelection = { viewModel.clearSelection() },
                        onSelectAll = { viewModel.selectAllNotes() },
                        onDelete = { showDeleteConfirmation = true },
                        onMove = { showMoveToFolderDialog = true }
                    )
                }

                AnimatedVisibility(
                    visible = !isMultiSelectMode,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    TopAppBar(
                        title = { Text("Obby") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = onGraphClick) {
                                Icon(Icons.Default.Hub, contentDescription = "Graph View")
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !isMultiSelectMode,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    EnhancedFAB(
                        expanded = showFabMenu,
                        onToggle = { showFabMenu = !showFabMenu },
                        onCreateNote = { showCreateNoteDialog = true },
                        onCreateFolder = { showCreateFolderDialog = true },
                        onCreateChecklist = { showCreateChecklistDialog = true }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                
                if (notes.isEmpty()) {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteListItem(
                                note = note,
                                isSelected = selectedNotes.contains(note.id),
                                isMultiSelectMode = isMultiSelectMode,
                                onClick = {
                                    if (isMultiSelectMode) {
                                        viewModel.toggleNoteSelection(note.id)
                                    } else {
                                        onNoteClick(note.id)
                                    }
                                },
                                onLongClick = {
                                    if (!isMultiSelectMode) {
                                        viewModel.toggleMultiSelectMode()
                                        viewModel.toggleNoteSelection(note.id)
                                    }
                                },
                                onTogglePin = { viewModel.togglePinNote(note.id) },
                                onToggleFavorite = { viewModel.toggleFavoriteNote(note.id) },
                                onRename = { newTitle -> viewModel.renameNote(note, newTitle) },
                                onDuplicate = { viewModel.duplicateNote(note) },
                                onMove = { folderId -> viewModel.moveNoteToFolder(note, folderId) },
                                onShare = {
                                    viewModel.getShareableContent(note.id)?.let { content ->
                                        shareNote(context, content)
                                    }
                                },
                                onDelete = { viewModel.deleteNote(note) },
                                folders = folders
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCreateNoteDialog) {
        CreateNoteDialog(
            title = "Create Note",
            onDismiss = { showCreateNoteDialog = false },
            onCreate = { title ->
                viewModel.createNote(title)
                showCreateNoteDialog = false
            }
        )
    }

    if (showCreateChecklistDialog) {
        CreateNoteDialog(
            title = "Create Checklist",
            onDismiss = { showCreateChecklistDialog = false },
            onCreate = { title ->
                viewModel.createChecklist(title)
                showCreateChecklistDialog = false
            }
        )
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                viewModel.createFolder(name)
                showCreateFolderDialog = false
            }
        )
    }

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            count = selectedNotes.size,
            onDismiss = { showDeleteConfirmation = false },
            onConfirm = {
                viewModel.deleteSelectedNotes()
                showDeleteConfirmation = false
            }
        )
    }

    if (showMoveToFolderDialog) {
        MoveToFolderDialog(
            folders = folders,
            onDismiss = { showMoveToFolderDialog = false },
            onFolderSelected = { folderId ->
                if (isMultiSelectMode) {
                    viewModel.moveSelectedNotesToFolder(folderId)
                }
                showMoveToFolderDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "Clear selection")
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = "Select all")
            }
            IconButton(onClick = onMove) {
                Icon(Icons.Default.DriveFileMove, contentDescription = "Move")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )
}

@Composable
fun EnhancedFAB(
    expanded: Boolean,
    onToggle: () -> Unit,
    onCreateNote: () -> Unit,
    onCreateFolder: () -> Unit,
    onCreateChecklist: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        onCreateChecklist()
                        onToggle()
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(Icons.Default.CheckBox, contentDescription = "New Checklist")
                }

                SmallFloatingActionButton(
                    onClick = {
                        onCreateFolder()
                        onToggle()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder")
                }

                SmallFloatingActionButton(
                    onClick = {
                        onCreateNote()
                        onToggle()
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = "New Note")
                }
            }
        }

        FloatingActionButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            }
        ) {
            Icon(
                if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Create"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    folders: List<Folder>,
    modifier: Modifier = Modifier
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = spring(),
        label = "elevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    )
                } else Modifier
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation,
            pressedElevation = 4.dp,
            hoveredElevation = 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else
                Color(0xFF2D3748) // Dark card background
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title and Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMultiSelectMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.05f
                        ),
                        color = Color(0xFFE2E8F0), // Light gray text
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isMultiSelectMode) {
                    Box {
                        IconButton(onClick = { showContextMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = Color(0xFFA0AEC0) // Medium gray for icon
                            )
                        }

                        NoteContextMenu(
                            expanded = showContextMenu,
                            note = note,
                            onDismiss = { showContextMenu = false },
                            onPin = {
                                onTogglePin()
                                showContextMenu = false
                            },
                            onFavorite = {
                                onToggleFavorite()
                                showContextMenu = false
                            },
                            onRename = {
                                showRenameDialog = true
                                showContextMenu = false
                            },
                            onDuplicate = {
                                onDuplicate()
                                showContextMenu = false
                            },
                            onMove = {
                                showMoveDialog = true
                                showContextMenu = false
                            },
                            onShare = {
                                onShare()
                                showContextMenu = false
                            },
                            onDelete = {
                                showDeleteDialog = true
                                showContextMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Content",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFA0AEC0), // Medium gray label
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Content preview in a box styled like an input field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF1A202C), // Darker background for content
                            shape = MaterialTheme.shapes.small
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF4A5568), // Border color
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(10.dp)
                ) {
                    if (note.content.isNotBlank()) {
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0), // Light gray text
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "No content",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF718096), // Dimmer gray for placeholder
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Date and badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badges and folder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (note.isPinned) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (note.isFavorite) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (note.isEncrypted) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    if (note.folderId != null) {
                        val folderName = folders.find { it.id == note.folderId }?.name
                        if (folderName != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFF718096)
                                )
                                Text(
                                    text = folderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF718096)
                                )
                            }
                        }
                    }
                }

                // Date
                Text(
                    text = dateFormat.format(Date(note.modifiedAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF718096) // Gray text for date
                )
            }
        }
    }

    // Dialogs for individual note actions
    if (showRenameDialog) {
        RenameNoteDialog(
            currentTitle = note.title,
            onDismiss = { showRenameDialog = false },
            onRename = { newTitle ->
                onRename(newTitle)
                showRenameDialog = false
            }
        )
    }

    if (showMoveDialog) {
        MoveToFolderDialog(
            folders = folders,
            currentFolderId = note.folderId,
            onDismiss = { showMoveDialog = false },
            onFolderSelected = { folderId ->
                onMove(folderId)
                showMoveDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            noteTitle = note.title,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            }
        )
    }
}

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
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(if (note.isPinned) "Unpin" else "Pin") },
            onClick = onPin,
            leadingIcon = { Icon(Icons.Default.PushPin, null) }
        )
        DropdownMenuItem(
            text = { Text(if (note.isFavorite) "Remove from favorites" else "Add to favorites") },
            onClick = onFavorite,
            leadingIcon = { Icon(Icons.Default.Favorite, null) }
        )
        Divider()
        DropdownMenuItem(
            text = { Text("Rename") },
            onClick = onRename,
            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, null) }
        )
        DropdownMenuItem(
            text = { Text("Duplicate") },
            onClick = onDuplicate,
            leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
        )
        DropdownMenuItem(
            text = { Text("Move to folder") },
            onClick = onMove,
            leadingIcon = { Icon(Icons.Default.DriveFileMove, null) }
        )
        DropdownMenuItem(
            text = { Text("Share") },
            onClick = onShare,
            leadingIcon = { Icon(Icons.Default.Share, null) }
        )
        Divider()
        DropdownMenuItem(
            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
            onClick = onDelete,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search notes...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

// ... existing code ...

@Composable
fun DrawerContent(
    folders: List<Folder>,
    tags: List<com.example.obby.data.local.entity.Tag>,
    onFolderClick: (Long?) -> Unit,
    onTagClick: (Long?) -> Unit,
    onViewModeChange: (NotesViewModel.ViewMode) -> Unit,
    onCreateFolder: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Obby",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Divider()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Views", style = MaterialTheme.typography.titleSmall)
        
        NavigationDrawerItem(
            label = { Text("All Notes") },
            selected = false,
            onClick = { onViewModeChange(NotesViewModel.ViewMode.ALL) },
            icon = { Icon(Icons.Default.Home, null) }
        )
        NavigationDrawerItem(
            label = { Text("Pinned") },
            selected = false,
            onClick = { onViewModeChange(NotesViewModel.ViewMode.PINNED) },
            icon = { Icon(Icons.Default.Star, null) }
        )
        NavigationDrawerItem(
            label = { Text("Favorites") },
            selected = false,
            onClick = { onViewModeChange(NotesViewModel.ViewMode.FAVORITES) },
            icon = { Icon(Icons.Default.Favorite, null) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Folders", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = onCreateFolder) {
                Icon(Icons.Default.Add, contentDescription = "Create Folder", modifier = Modifier.size(20.dp))
            }
        }
        
        folders.forEach { folder ->
            NavigationDrawerItem(
                label = { Text(folder.name) },
                selected = false,
                onClick = { onFolderClick(folder.id) },
                icon = { Icon(Icons.Default.Folder, null) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Tags", style = MaterialTheme.typography.titleSmall)
        
        tags.forEach { tag ->
            NavigationDrawerItem(
                label = { Text("#${tag.name}") },
                selected = false,
                onClick = { onTagClick(tag.id) },
                icon = { Icon(Icons.Default.Tag, null) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Settings", style = MaterialTheme.typography.titleSmall)

        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Default.Settings, null) }
        )
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No notes yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap + to create your first note",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CreateNoteDialog(
    title: String = "Create Note",
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var noteTitle by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TextField(
                value = noteTitle,
                onValueChange = { noteTitle = it },
                label = { Text("Note Title") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (noteTitle.isNotBlank()) onCreate(noteTitle) },
                enabled = noteTitle.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Folder") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Folder Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameNoteDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var newTitle by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Note") },
        text = {
            TextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("Note Title") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (newTitle.isNotBlank()) onRename(newTitle) },
                enabled = newTitle.isNotBlank() && newTitle != currentTitle
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    noteTitle: String? = null,
    count: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val message = when {
        noteTitle != null -> "Are you sure you want to delete '$noteTitle'? This action cannot be undone."
        count != null -> "Are you sure you want to delete $count note(s)? This action cannot be undone."
        else -> "Are you sure you want to delete this note? This action cannot be undone."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Note${if (count != null && count > 1) "s" else ""}?") },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MoveToFolderDialog(
    folders: List<Folder>,
    currentFolderId: Long? = null,
    onDismiss: () -> Unit,
    onFolderSelected: (Long?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move to Folder") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Root folder option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFolderSelected(null) }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Root",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (currentFolderId == null)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                Divider()

                // Folder list
                folders.forEach { folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFolderSelected(folder.id) }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            folder.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (folder.id == currentFolderId)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper function for sharing notes
private fun shareNote(context: android.content.Context, content: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, content)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Note"))
}

@Composable
fun MarkdownListPreview(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    val textColor = if (isDarkTheme) {
        android.graphics.Color.parseColor("#E8E6E3")
    } else {
        android.graphics.Color.parseColor("#1C1B1F")
    }

    val markwon = remember(isDarkTheme) {
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .build()
    }

    AndroidView(
        modifier = modifier.heightIn(max = 48.dp),
        factory = { ctx ->
            TextView(ctx).apply {
                this.textSize = 14f
                setTextColor(textColor)
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(0, 0, 0, 0)
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            markwon.setMarkdown(textView, markdown)
        }
    )
}
