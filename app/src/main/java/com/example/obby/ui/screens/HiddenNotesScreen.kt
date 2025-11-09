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
import androidx.compose.ui.unit.dp
import com.example.obby.data.local.entity.Note
import com.example.obby.data.repository.HiddenFolderManager
import com.example.obby.data.repository.HiddenFolderState
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

    val hiddenFolderManager = remember { HiddenFolderManager(context) }
    val folderState by hiddenFolderManager.state.collectAsState()

    // Hidden notes
    val hiddenNotes = remember { mutableStateListOf<Note>() }

    // Dialog states
    var showCreatePasswordDialog by remember { mutableStateOf(false) }
    var showUnlockDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showRecoveryPhraseDialog by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var showLockedOutDialog by remember { mutableStateOf(false) }
    var recoveryPhrase by remember { mutableStateOf("") }
    var lockoutSeconds by remember { mutableStateOf(0) }
    var failedAttempts by remember { mutableStateOf(0) }

    // Auto-lock timer
    var autoLockJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Initialize manager
    LaunchedEffect(Unit) {
        hiddenFolderManager.initialize()
    }

    // Load hidden notes when unlocked
    LaunchedEffect(folderState) {
        when (folderState) {
            is HiddenFolderState.Unlocked -> {
                // Load hidden notes
                repository.getAllNotes().collect { allNotes ->
                    hiddenNotes.clear()
                    hiddenNotes.addAll(allNotes.filter { it.isHidden })
                }

                // Start auto-lock timer (2 minutes)
                autoLockJob?.cancel()
                autoLockJob = launch {
                    delay(2 * 60 * 1000L)
                    hiddenFolderManager.lock()
                    snackbarHostState.showSnackbar("Hidden folder locked due to inactivity")
                }
            }

            is HiddenFolderState.Uninitialized -> {
                showCreatePasswordDialog = true
            }

            is HiddenFolderState.Locked -> {
                showUnlockDialog = true
                autoLockJob?.cancel()
            }

            is HiddenFolderState.Unlocking -> {
                failedAttempts = (folderState as HiddenFolderState.Unlocking).failedAttempts
            }

            is HiddenFolderState.LockedOut -> {
                val lockedState = folderState as HiddenFolderState.LockedOut
                lockoutSeconds = ((lockedState.until - System.currentTimeMillis()) / 1000).toInt()
                failedAttempts = lockedState.attemptCount
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
            hiddenFolderManager.initialize()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Hidden Folder") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (folderState is HiddenFolderState.Unlocked) {
                        // Lock button
                        IconButton(
                            onClick = {
                                hiddenFolderManager.lock()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Hidden folder locked")
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
                                            hiddenFolderManager.generateRecoveryPhrase()
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
                is HiddenFolderState.Unlocked -> {
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
                                Icons.Default.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No Hidden Notes",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Long-press notes in the main list and select \"Hide\" to add them here",
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
                                                "Folder will auto-lock after 2 minutes of inactivity",
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
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                note.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                Icons.Default.VisibilityOff,
                                                contentDescription = "Hidden",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
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
    if (showCreatePasswordDialog) {
        CreatePasswordDialog(
            onDismiss = {
                showCreatePasswordDialog = false
                onNavigateBack()
            },
            onCreate = { password, confirmPassword ->
                scope.launch {
                    val result = hiddenFolderManager.createPassword(password, confirmPassword)
                    result.onSuccess {
                        showCreatePasswordDialog = false
                        snackbarHostState.showSnackbar("Password created successfully")
                    }.onFailure { error ->
                        snackbarHostState.showSnackbar(
                            error.message ?: "Failed to create password"
                        )
                    }
                }
            },
            onSetupRecoveryPhrase = {
                scope.launch {
                    recoveryPhrase = hiddenFolderManager.generateRecoveryPhrase()
                    showRecoveryPhraseDialog = true
                }
            }
        )
    }

    if (showUnlockDialog && folderState !is HiddenFolderState.LockedOut) {
        UnlockHiddenFolderDialog(
            onDismiss = {
                showUnlockDialog = false
                onNavigateBack()
            },
            onUnlock = { password ->
                scope.launch {
                    val result = hiddenFolderManager.unlock(password)
                    result.onSuccess {
                        showUnlockDialog = false
                        snackbarHostState.showSnackbar("Hidden folder unlocked")
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
                    val result = hiddenFolderManager.changePassword(
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
                    val result = hiddenFolderManager.recoverWithPhrase(
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
}
