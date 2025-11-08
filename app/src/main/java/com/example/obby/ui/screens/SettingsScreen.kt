package com.example.obby.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.obby.data.repository.NoteRepository
import com.example.obby.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: NoteRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel(repository, context) }
    val scope = rememberCoroutineScope()

    val backupState by viewModel.backupState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var selectedBackupUri by remember { mutableStateOf<Uri?>(null) }

    // Backup folder picker launcher
    val backupFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                viewModel.createBackup(it)
            }
        }
    }

    // Restore file picker launcher
    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedBackupUri = it
            showRestoreConfirmDialog = true
        }
    }

    // Show snackbar for messages
    LaunchedEffect(backupState) {
        if (backupState is SettingsViewModel.BackupState.Success) {
            snackbarHostState.showSnackbar(
                message = (backupState as SettingsViewModel.BackupState.Success).message,
                duration = SnackbarDuration.Long
            )
        } else if (backupState is SettingsViewModel.BackupState.Error) {
            snackbarHostState.showSnackbar(
                message = (backupState as SettingsViewModel.BackupState.Error).message,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Backup & Restore Section
            item {
                Text(
                    text = "Backup & Restore",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Backup,
                    title = "Backup Notes",
                    subtitle = "Export all notes and database to a ZIP file",
                    onClick = {
                        backupFolderLauncher.launch(null)
                    },
                    enabled = backupState !is SettingsViewModel.BackupState.Loading
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.RestorePage,
                    title = "Restore Backup",
                    subtitle = "Import notes from a backup ZIP file",
                    onClick = {
                        restoreFileLauncher.launch(arrayOf("application/zip"))
                    },
                    enabled = backupState !is SettingsViewModel.BackupState.Loading
                )
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // App Info Section
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "Obby v1.0.0",
                    onClick = {}
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Description,
                    title = "Markdown Guide",
                    subtitle = "Learn how to format your notes",
                    onClick = {}
                )
            }
        }

        // Loading indicator
        if (backupState is SettingsViewModel.BackupState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (backupState as SettingsViewModel.BackupState.Loading).message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // Restore confirmation dialog
    if (showRestoreConfirmDialog && selectedBackupUri != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                selectedBackupUri = null
            },
            title = { Text("Restore Backup?") },
            text = {
                Text("This will replace your current database with the backup. The app will restart after restore. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.restoreBackup(selectedBackupUri!!)
                            showRestoreConfirmDialog = false

                            // Request app restart after successful restore
                            if (backupState is SettingsViewModel.BackupState.Success) {
                                // Restart the app
                                val intent = (context as? Activity)?.intent
                                (context as? Activity)?.finish()
                                context.startActivity(intent)
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreConfirmDialog = false
                    selectedBackupUri = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                ),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.38f
                    )
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
