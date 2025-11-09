package com.example.obby.ui.screens

import android.util.Log
import android.widget.TextView
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.obby.data.local.entity.Note
import com.example.obby.data.repository.NoteRepository
import com.example.obby.ui.components.MarkdownFormatter
import com.example.obby.ui.components.MarkdownToolbar
import com.example.obby.ui.viewmodel.NoteDetailViewModel
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    repository: NoteRepository,
    noteId: Long,
    onNavigateBack: () -> Unit,
    onNoteClick: (Long) -> Unit
) {
    Log.d("NoteDetailScreen", "Opening note with ID: $noteId")

    val viewModel = remember {
        Log.d("NoteDetailScreen", "Creating ViewModel for note: $noteId")
        NoteDetailViewModel(repository, noteId)
    }

    val note by viewModel.note.collectAsState()
    val linkedNotes by viewModel.linkedNotes.collectAsState()
    val backlinks by viewModel.backlinks.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val editMode by viewModel.editMode.collectAsState()
    val showMarkdownPreview by viewModel.showMarkdownPreview.collectAsState()
    val pendingChanges by viewModel.pendingChanges.collectAsState()

    LaunchedEffect(note) {
        Log.d("NoteDetailScreen", "Note loaded: ${note?.title}")
    }

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEncryptDialog by remember { mutableStateOf(false) }
    var showDecryptDialog by remember { mutableStateOf(false) }

    // Use TextFieldValue for cursor tracking
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

    // Snackbar
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
                        // Undo/Redo
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

                        // Preview toggle
                        IconButton(onClick = { viewModel.toggleMarkdownPreview() }) {
                            Icon(
                                if (showMarkdownPreview) Icons.Default.Edit else Icons.Default.Visibility,
                                contentDescription = if (showMarkdownPreview) "Edit" else "Preview"
                            )
                        }

                        // Done editing
                        IconButton(
                            onClick = {
                                viewModel.saveNow()
                                viewModel.toggleEditMode()
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Done")
                        }
                    } else {
                        // Edit button
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }

                    // More menu
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show toolbar at the top in edit mode and not in preview
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
                        viewModel.onContentChange(titleFieldValue.text, newText)
                        viewModel.updateTextSelection(newSelection)
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (note == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    if (editMode) {
                        if (showMarkdownPreview) {
                            // Preview mode in editor
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
                                        modifier = Modifier.fillMaxWidth(),
                                        onCheckboxToggled = { updatedMarkdown ->
                                            viewModel.updateContent(updatedMarkdown)
                                        }
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
        }
    }

    // Delete confirmation dialog
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

    // Encrypt dialog
    if (showEncryptDialog) {
        PasswordDialog(
            title = "Encrypt Note",
            message = "Enter a password to encrypt this note. You will need this password to decrypt it later.",
            confirmText = "Encrypt",
            onDismiss = { showEncryptDialog = false },
            onConfirm = { password ->
                viewModel.encryptNote(password)
                showEncryptDialog = false
            }
        )
    }

    // Decrypt dialog
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

@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier,
    onCheckboxToggled: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    
    // Define colors based on theme
    val textColor = if (isDarkTheme) {
        android.graphics.Color.parseColor("#E8E6E3") // Light text for dark theme
    } else {
        android.graphics.Color.parseColor("#1C1B1F") // Dark text for light theme
    }
    
    val headingColor = if (isDarkTheme) {
        android.graphics.Color.parseColor("#F0EDE6") // Brighter for headings
    } else {
        android.graphics.Color.parseColor("#000000")
    }
    
    val codeBackgroundColor = if (isDarkTheme) {
        android.graphics.Color.parseColor("#2A2A2A")
    } else {
        android.graphics.Color.parseColor("#F5F5F5")
    }
    
    val linkColor = if (isDarkTheme) {
        android.graphics.Color.parseColor("#9C89B8") // Purple for links
    } else {
        android.graphics.Color.parseColor("#6750A4")
    }

    val markwon = remember(isDarkTheme, onCheckboxToggled) {
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .headingTextSizeMultipliers(floatArrayOf(2f, 1.5f, 1.17f, 1f, 0.83f, 0.67f))
                        .headingTypeface(android.graphics.Typeface.DEFAULT_BOLD)
                        .linkColor(linkColor)
                        .codeTextColor(textColor)
                        .codeBackgroundColor(codeBackgroundColor)
                        .codeBlockTextColor(textColor)
                        .codeBlockBackgroundColor(codeBackgroundColor)
                        .blockQuoteColor(linkColor)
                        .listItemColor(textColor)
                }

                // Add interactive checkbox support if callback is provided
                override fun configureSpansFactory(builder: io.noties.markwon.MarkwonSpansFactory.Builder) {
                    if (onCheckboxToggled != null) {
                        // Get the original TaskListItem factory
                        val originalFactory =
                            builder.getFactory(io.noties.markwon.ext.tasklist.TaskListItem::class.java)

                        if (originalFactory != null) {
                            builder.setFactory(io.noties.markwon.ext.tasklist.TaskListItem::class.java) { configuration, props ->
                                val span = originalFactory.getSpans(configuration, props)

                                if (span is io.noties.markwon.ext.tasklist.TaskListSpan) {
                                    // Return array of spans: original span + clickable span with larger touch target
                                    arrayOf(
                                        span,
                                        object : android.text.style.ClickableSpan() {
                                            override fun onClick(widget: android.view.View) {
                                                // Toggle visual state
                                                span.isDone = !span.isDone

                                                // Invalidate widget to show change
                                                widget.invalidate()

                                                // Update the markdown content
                                                if (widget is TextView) {
                                                    val text = widget.text
                                                    if (text is android.text.Spanned) {
                                                        // Find the checkbox position and toggle it
                                                        val updatedMarkdown =
                                                            toggleCheckboxInMarkdown(
                                                                markdown,
                                                                text,
                                                                span
                                                            )
                                                        onCheckboxToggled?.invoke(updatedMarkdown)
                                                    }
                                                }
                                            }

                                            override fun updateDrawState(ds: android.text.TextPaint) {
                                                // No-op: don't change appearance (don't make it look like a link)
                                            }
                                        }
                                    )
                                } else {
                                    span
                                }
                            }
                        }
                    }
                }
            })
            .build()
    }
    
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                setTextIsSelectable(true)
                setTextColor(textColor)
                textSize = 16f
                setLineSpacing(4f, 1.3f) // Extra space, multiplier
                // Increase touch padding for better checkbox accessibility
                setPadding(0, 8, 0, 8) // Add vertical padding for easier tapping
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
                // Increase minimum height to ensure 48dp touch target
                minHeight = (48 * resources.displayMetrics.density).toInt()
                // Set movement method to enable clickable spans
                if (onCheckboxToggled != null) {
                    movementMethod = android.text.method.LinkMovementMethod.getInstance()
                    // Improve touch slop for easier checkbox clicking
                    isClickable = true
                    isFocusable = true
                }
            }
        },
        update = { textView ->
            try {
                textView.setTextColor(textColor)
                markwon.setMarkdown(textView, markdown)
            } catch (e: Exception) {
                textView.text = markdown
                textView.setTextColor(textColor)
            }
        }
    )
}

// Helper function to toggle checkbox in markdown text
private fun toggleCheckboxInMarkdown(
    markdown: String,
    renderedText: android.text.Spanned,
    clickedSpan: io.noties.markwon.ext.tasklist.TaskListSpan
): String {
    // Find all checkboxes in the markdown with their positions
    val checkboxPattern = Regex("- \\[([ xX])\\]")
    val matches = checkboxPattern.findAll(markdown).toList()

    // Find all TaskListSpan instances in the rendered text
    val spans = renderedText.getSpans(
        0,
        renderedText.length,
        io.noties.markwon.ext.tasklist.TaskListSpan::class.java
    )

    // Find the index of the clicked span by finding its text position in the rendered content
    // This is more robust than using array index comparison
    val clickedSpanStart = renderedText.getSpanStart(clickedSpan)
    val clickedSpanEnd = renderedText.getSpanEnd(clickedSpan)

    // Count how many TaskListSpan instances appear before this one
    var spanIndex = 0
    for (span in spans) {
        val spanStart = renderedText.getSpanStart(span)
        val spanEnd = renderedText.getSpanEnd(span)

        if (spanStart == clickedSpanStart && spanEnd == clickedSpanEnd && span === clickedSpan) {
            // Found the exact span
            break
        }

        if (spanStart < clickedSpanStart) {
            spanIndex++
        }
    }

    if (spanIndex >= 0 && spanIndex < matches.size) {
        val match = matches[spanIndex]
        val currentState = match.groupValues[1]
        val newState = if (currentState == " ") "x" else " "

        // Replace the checkbox at the specific position
        val before = markdown.substring(0, match.range.first)
        val after = markdown.substring(match.range.last + 1)
        return before + "- [$newState]" + after
    }

    return markdown
}

@Composable
fun LinkCard(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleSmall
            )
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content.take(100),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

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
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (showPassword)
                        androidx.compose.ui.text.input.VisualTransformation.None
                    else
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
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
            TextButton(
                onClick = { if (password.isNotBlank()) onConfirm(password) },
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
