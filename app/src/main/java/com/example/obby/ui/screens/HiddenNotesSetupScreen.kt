package com.example.obby.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.obby.util.HiddenNotesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenNotesSetupScreen(
    onNavigateBack: () -> Unit,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val hiddenNotesManager = remember { HiddenNotesManager(context) }
    val scope = rememberCoroutineScope()

    var selectedEmojis by remember { mutableStateOf("") }
    var categoryAlias by remember { mutableStateOf("Recipes") }
    var showAliasDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Up Hidden Notes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (selectedEmojis.length == 4) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    hiddenNotesManager.setupEmojiPin(selectedEmojis, categoryAlias)
                                    onSetupComplete()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Check, "Complete setup")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Your Emoji PIN",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Choose 4 emojis as your secret PIN",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // PIN Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (index < selectedEmojis.length) {
                                Text(
                                    text = selectedEmojis[index].toString(),
                                    style = MaterialTheme.typography.displaySmall
                                )
                            } else {
                                Text(
                                    text = "○",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                        alpha = 0.3f
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Backspace button
            if (selectedEmojis.isNotEmpty()) {
                FilledTonalButton(
                    onClick = {
                        selectedEmojis = selectedEmojis.dropLast(1)
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Backspace, "Delete", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove Last")
                }
            }

            // Category Alias Setting
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { showAliasDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Category Disguise",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Hidden notes will appear as: $categoryAlias",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Emoji Grid
            Text(
                text = "Choose Emojis",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(HiddenNotesManager.EMOJI_OPTIONS) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(enabled = selectedEmojis.length < 4) {
                                selectedEmojis += emoji
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Category Alias Dialog
    if (showAliasDialog) {
        AlertDialog(
            onDismissRequest = { showAliasDialog = false },
            title = { Text("Choose Category Disguise") },
            text = {
                Column {
                    Text(
                        text = "Hidden notes will use this category name for plausible deniability",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    HiddenNotesManager.CATEGORY_ALIASES.forEach { alias ->
                        TextButton(
                            onClick = {
                                categoryAlias = alias
                                showAliasDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = alias,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAliasDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HiddenNotesUnlockDialog(
    onDismiss: () -> Unit,
    onUnlockSuccess: () -> Unit
) {
    val context = LocalContext.current
    val hiddenNotesManager = remember { HiddenNotesManager(context) }
    val scope = rememberCoroutineScope()

    var selectedEmojis by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unlock Hidden Notes") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter your 4-emoji PIN",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // PIN Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (index < selectedEmojis.length) {
                                Text(
                                    text = selectedEmojis[index].toString(),
                                    style = MaterialTheme.typography.displaySmall
                                )
                            } else {
                                Text(
                                    text = "○",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }

                if (isError) {
                    Text(
                        text = "Incorrect PIN",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Backspace
                if (selectedEmojis.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = {
                            selectedEmojis = selectedEmojis.dropLast(1)
                            isError = false
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.Backspace, "Delete")
                    }
                }

                // Emoji Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(HiddenNotesManager.EMOJI_OPTIONS) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(enabled = selectedEmojis.length < 4) {
                                    val newPin = selectedEmojis + emoji
                                    selectedEmojis = newPin

                                    if (newPin.length == 4) {
                                        scope.launch {
                                            val isValid = hiddenNotesManager.verifyAndUnlock(newPin)
                                            if (isValid) {
                                                onUnlockSuccess()
                                            } else {
                                                isError = true
                                                selectedEmojis = ""
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                        }
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
