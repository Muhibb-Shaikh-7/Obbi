package com.example.obby.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.obby.data.local.entity.Note
import com.example.obby.data.repository.HiddenFolderState
import com.example.obby.data.repository.PrivateFolderManager
import com.example.obby.data.repository.PrivateFolderState
import com.example.obby.data.repository.NoteRepository
import com.example.obby.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenNotesScreen(
    repository: NoteRepository,
    onNavigateBack: () -> Unit,
    onNoteClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val privateFolderManager = remember { PrivateFolderManager(context) }
    val folderState by privateFolderManager.state.collectAsState()

    // Hidden notes
    val hiddenNotes = remember { mutableStateListOf<Note>() }

    // Dialog states
    var showUnlockDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showRecoveryPhraseDialog by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var showLockedOutDialog by remember { mutableStateOf(false) }
    var showRemoveFromPrivateDialog by remember { mutableStateOf(false) }
    var selectedNoteToRemove by remember { mutableStateOf<Note?>(null) }
    var recoveryPhrase by remember { mutableStateOf("") }
    var lockoutSeconds by remember { mutableStateOf(0) }
    var failedAttempts by remember { mutableStateOf(0) }

    // Auto-lock timer
    var autoLockJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Initialize manager
    LaunchedEffect(Unit) {
        privateFolderManager.initialize()
    }

    // Load hidden notes when unlocked
    LaunchedEffect(folderState) {
        val currentState = folderState
        when (currentState) {
            PrivateFolderState.Unlocked -> {
                // Load hidden notes
                repository.getAllNotes().collect { allNotes ->
                    hiddenNotes.clear()
                    hiddenNotes.addAll(allNotes.filter { it.isActuallyPrivate })
                }

                // Start auto-lock timer (2 minutes)
                autoLockJob?.cancel()
                autoLockJob = launch {
                    delay(2 * 60 * 1000L)
                    privateFolderManager.lock()
                    snackbarHostState.showSnackbar("Private locked due to inactivity")
                }
            }

            PrivateFolderState.Uninitialized -> {
                // No password set - user needs to go to Settings
                // Don't show any dialog, just display message in UI
            }

            PrivateFolderState.Locked -> {
                // Show unlock dialog when locked
                showUnlockDialog = true
                autoLockJob?.cancel()
            }

            is PrivateFolderState.Unlocking -> {
                failedAttempts = currentState.failedAttempts
            }

            is PrivateFolderState.LockedOut -> {
                lockoutSeconds = ((currentState.until - System.currentTimeMillis()) / 1000).toInt()
                failedAttempts = currentState.attemptCount
                showLockedOutDialog = true
                showUnlockDialog = false
            }
        }
    }

    // Countdown for lockout
    LaunchedEffect(showLockedOutDialog) {
        if (showLockedOutDialog && lockoutSeconds > 0) {
            while (lockoutSeconds > 0) {
                delay(1000)
                lockoutSeconds--
            }
            showLockedOutDialog = false
            privateFolderManager.initialize()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Private") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (folderState is PrivateFolderState.Unlocked) {
                        // Lock button
                        IconButton(
                            onClick = {
                                privateFolderManager.lock()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Private locked")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Lock, "Lock")
                        }

                        // Menu
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Change Password") },
                                onClick = {
                                    showChangePasswordDialog = true
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Key, null) }
                            )

                            DropdownMenuItem(
                                text = { Text("Setup Recovery Phrase") },
                                onClick = {
                                    scope.launch {
                                        recoveryPhrase =
                                            privateFolderManager.generateRecoveryPhrase()
                                        showRecoveryPhraseDialog = true
                                        showMenu = false
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Security, null) }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (folderState) {
                PrivateFolderState.Unlocked -> {
                    if (hiddenNotes.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No Private Notes",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Long-press notes in the main list and select \"Mark as Private\" to add them here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Show hidden notes
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Will auto-lock after 2 minutes of inactivity",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                "Tap the lock icon to manually lock",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(hiddenNotes) { note ->
                                var showNoteMenu by remember { mutableStateOf(false) }

                                Card(
                                    onClick = { onNoteClick(note.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                note.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Lock,
                                                    contentDescription = "Private",
                                                    modifier = Modifier.size(20.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )

                                                Box {
                                                    IconButton(onClick = { showNoteMenu = true }) {
                                                        Icon(
                                                            Icons.Default.MoreVert,
                                                            contentDescription = "More options",
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }

                                                    DropdownMenu(
                                                        expanded = showNoteMenu,
                                                        onDismissRequest = { showNoteMenu = false }
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Remove from Private") },
                                                            onClick = {
                                                                selectedNoteToRemove = note
                                                                showRemoveFromPrivateDialog = true
                                                                showNoteMenu = false
                                                            },
                                                            leadingIcon = {
                                                                Icon(
                                                                    Icons.Default.LockOpen,
                                                                    contentDescription = null
                                                                )
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (note.content.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                note.content.take(100),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                PrivateFolderState.Uninitialized -> {
                    // Show message to set password in Settings
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Private Password Required",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Please set a password in Settings to use Private notes",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 1.0
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateBack
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Go to Settings")
                        }
                    }
                }

                else -> {
                    // Show loading or locked state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // Dialogs
    if (showUnlockDialog && folderState !is PrivateFolderState.LockedOut && folderState !is PrivateFolderState.Uninitialized) {
        UnlockHiddenFolderDialog(
            onDismiss = {
                showUnlockDialog = false
                onNavigateBack()
            },
            onUnlock = { password ->
                scope.launch {
                    val result = privateFolderManager.unlock(password)
                    result.onSuccess {
                        showUnlockDialog = false
                        snackbarHostState.showSnackbar("Private unlocked")
                    }.onFailure { error ->
                        snackbarHostState.showSnackbar(
                            error.message ?: "Failed to unlock"
                        )
                    }
                }
            },
            failedAttempts = failedAttempts,
            onForgotPassword = {
                showUnlockDialog = false
                showRecoveryDialog = true
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onChange = { oldPassword, newPassword, confirmPassword ->
                scope.launch {
                    val result = privateFolderManager.changePassword(
                        oldPassword,
                        newPassword,
                        confirmPassword
                    )
                    result.onSuccess {
                        showChangePasswordDialog = false
                        snackbarHostState.showSnackbar("Password changed successfully")
                    }.onFailure { error ->
                        snackbarHostState.showSnackbar(
                            error.message ?: "Failed to change password"
                        )
                    }
                }
            }
        )
    }

    if (showRecoveryPhraseDialog) {
        RecoveryPhraseDialog(
            recoveryPhrase = recoveryPhrase,
            onDismiss = {
                showRecoveryPhraseDialog = false
                recoveryPhrase = ""
            },
            onConfirm = {
                showRecoveryPhraseDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Recovery phrase saved")
                }
                recoveryPhrase = ""
            }
        )
    }

    if (showRecoveryDialog) {
        RecoveryDialog(
            onDismiss = { showRecoveryDialog = false },
            onRecover = { phrase, newPassword, confirmPassword ->
                scope.launch {
                    val result = privateFolderManager.recoverWithPhrase(
                        phrase,
                        newPassword,
                        confirmPassword
                    )
                    result.onSuccess {
                        showRecoveryDialog = false
                        snackbarHostState.showSnackbar("Password recovered successfully")
                    }.onFailure { error ->
                        snackbarHostState.showSnackbar(
                            error.message ?: "Recovery failed"
                        )
                    }
                }
            }
        )
    }

    if (showLockedOutDialog) {
        LockedOutDialog(
            remainingSeconds = lockoutSeconds,
            attemptCount = failedAttempts,
            onDismiss = {
                showLockedOutDialog = false
                if (lockoutSeconds <= 0) {
                    showUnlockDialog = true
                }
            }
        )
    }

    if (showRemoveFromPrivateDialog && selectedNoteToRemove != null) {
        RemoveFromPrivateDialog(
            noteTitle = selectedNoteToRemove!!.title,
            onDismiss = {
                showRemoveFromPrivateDialog = false
                selectedNoteToRemove = null
            },
            onRemove = { password ->
                scope.launch {
                    val result = privateFolderManager.removeNoteFromPrivate(
                        selectedNoteToRemove!!.id,
                        password
                    )
                    result.onSuccess {
                        // Password verified, now remove from private
                        repository.togglePrivateNote(selectedNoteToRemove!!.id)
                        showRemoveFromPrivateDialog = false
                        selectedNoteToRemove = null
                        snackbarHostState.showSnackbar("Note removed from Private")
                    }.onFailure { error ->
                        snackbarHostState.showSnackbar(
                            error.message ?: "Incorrect password"
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun RemoveFromPrivateDialog(
    noteTitle: String,
    onDismiss: () -> Unit,
    onRemove: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Remove from Private") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Are you sure you want to remove \"$noteTitle\" from Private?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    "This note will become visible in the main notes list.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "Enter your password to confirm:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (password.isNotEmpty()) onRemove(password) },
                enabled = password.isNotEmpty()
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
