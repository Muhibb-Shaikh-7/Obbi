# Obbi Comprehensive Upgrade Plan

## Transforming Obbi into an Obsidian-Class Knowledge Base

**Version:** 2.0  
**Date:** 2025  
**Goal:** Elevate Obbi from a solid note-taking app to a powerful, polished, fully offline Markdown
knowledge-base application with Obsidian-level features and UX.

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current State Analysis](#current-state-analysis)
3. [Upgrade Strategy](#upgrade-strategy)
4. [Phase 1: Enhanced Editing Experience](#phase-1-enhanced-editing-experience)
5. [Phase 2: Interactive Preview & UX Polish](#phase-2-interactive-preview--ux-polish)
6. [Phase 3: Advanced Organization](#phase-3-advanced-organization)
7. [Phase 4: Testing & Optimization](#phase-4-testing--optimization)
8. [Architecture Updates](#architecture-updates)
9. [Implementation Guide](#implementation-guide)
10. [UI/UX Wireframes](#uiux-wireframes)
11. [Library Recommendations](#library-recommendations)
12. [Testing Strategy](#testing-strategy)
13. [Migration & Compatibility](#migration--compatibility)
14. [Timeline & Milestones](#timeline--milestones)

---

## Executive Summary

This upgrade plan transforms Obbi into a premium, Obsidian-inspired offline note-taking application
while **retaining 100% of existing features**. The focus is on:

- **Enhanced editor** with persistent formatting toolbar and markdown shortcuts
- **Interactive preview mode** with clickable links and checkbox toggles
- **Improved multi-select & long-press context menus** for power users
- **Nested folders** and **drag-and-drop** for advanced organization
- **Smooth animations**, auto-save indicators, and Material3 polish
- **Full offline functionality** with no compromises to security/encryption

**Key Principle:** Additive enhancement—no feature removal, only improvements and additions.

---

## Current State Analysis

### ✅ Existing Strengths

| Feature | Status | Quality |
|---------|--------|---------|
| Core note CRUD | ✅ Complete | Excellent |
| Markdown rendering (Markwon) | ✅ Complete | Very Good |
| Bidirectional linking | ✅ Complete | Good |
| Graph visualization | ✅ Complete | Good |
| Folder organization | ✅ Complete | Good (flat only) |
| Tag system (auto-detect) | ✅ Complete | Excellent |
| Search functionality | ✅ Complete | Good |
| Encryption (AES-256-GCM) | ✅ Complete | Excellent |
| Undo/Redo | ✅ Complete | Good |
| Auto-save | ✅ Complete | Good |
| Multi-select mode | ✅ Complete | Basic |
| Formatting toolbar | ✅ Complete | Good |
| Preview toggle | ✅ Complete | Basic |

### 🔄 Areas for Enhancement

| Feature | Current State | Target State |
|---------|---------------|--------------|
| Toolbar visibility | Hidden when not editing | Persistent in edit mode |
| Preview interactions | Static render | Interactive (checkboxes, links) |
| Context menus | Basic three-dot menu | Rich long-press with haptics |
| Multi-select | Basic delete/move | Enhanced with animations |
| Folders | Flat structure | Nested/hierarchical |
| Note ordering | Timestamp-based | Custom drag-and-drop |
| Auto-save feedback | Small spinner | Subtle status indicator |
| Empty states | Basic text | Rich illustrations |
| Animations | Minimal | Smooth, polished |
| Markdown help | Dialog | Interactive guide + tooltips |

### 📊 Technical Debt & Opportunities

- **Performance:** Large note lists could benefit from paging
- **Testing:** Limited unit/UI test coverage
- **Accessibility:** No TalkBack optimization
- **Drag-drop:** Not implemented for note reordering
- **Interactive preview:** Checkbox toggles not yet implemented

---

## Upgrade Strategy

### Design Principles

1. **Non-Breaking:** All existing data structures remain compatible
2. **Incremental:** Ship features in phases for testing and feedback
3. **Offline-First:** No network dependencies added
4. **Privacy-Preserving:** Encryption and local storage unchanged
5. **Material3-Native:** Consistent with Android design guidelines
6. **Performance-Conscious:** Optimize for 1000+ notes

### Architecture Approach

```
┌─────────────────────────────────────────────────────┐
│                 Presentation Layer                   │
│  ┌──────────────────────────────────────────────┐  │
│  │  Enhanced Screens (backward compatible)      │  │
│  │  - NoteDetailScreen (+ interactive preview)  │  │
│  │  - NotesListScreen (+ drag-drop)             │  │
│  │  - FolderTreeView (new, hierarchical)        │  │
│  └──────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────┐  │
│  │  New UI Components                           │  │
│  │  - InteractiveMarkdownPreview                │  │
│  │  - EnhancedContextMenu                       │  │
│  │  - DraggableNoteList                         │  │
│  │  - NestedFolderPicker                        │  │
│  │  - AutoSaveIndicator                         │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────┐
│                 Domain Layer (NEW)                   │
│  ┌──────────────────────────────────────────────┐  │
│  │  Use Cases                                   │  │
│  │  - ToggleCheckboxUseCase                     │  │
│  │  - NavigateToLinkedNoteUseCase               │  │
│  │  - ReorderNotesUseCase                       │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                          │
┌─────────────────────────────────────────────────────┐
│              Data Layer (EXTENDED)                   │
│  ┌──────────────────────────────────────────────┐  │
│  │  Enhanced Entities                           │  │
│  │  - Note (+ sortOrder field)                  │  │
│  │  - Folder (existing parentFolderId is OK!)   │  │
│  └──────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────┐  │
│  │  New Utilities                               │  │
│  │  - CheckboxParser (find & toggle in MD)      │  │
│  │  - LinkNavigator (extract click targets)     │  │
│  │  - NoteReorderer (persist custom order)      │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

---

## Phase 1: Enhanced Editing Experience

**Duration:** 2 weeks  
**Priority:** HIGH  
**Goal:** Make the editor feel professional and powerful

### 1.1 Persistent Formatting Toolbar

**Current:** Toolbar exists but behavior can be improved  
**Enhancement:** Always visible in edit mode, hidden in preview/view mode

#### Changes to `NoteDetailScreen.kt`

The toolbar is already conditionally shown. No major changes needed, but we'll enhance the behavior:

```kotlin
// Already implemented correctly:
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
                viewModel.onContentChange(titleFieldValue.text, newText)
                viewModel.updateTextSelection(newSelection)
            }
        )
    }
}
```

**Enhancement:** Add haptic feedback and animation

```kotlin
// In MarkdownToolbar.kt - enhance IconButton clicks
val haptic = LocalHapticFeedback.current

IconButton(onClick = { 
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    onAction(MarkdownActionType.Bold) 
}) {
    Icon(Icons.Default.FormatBold, "Bold")
}
```

### 1.2 Keyboard Shortcuts & Gesture Support

**Implementation:** Add a `KeyboardShortcutHandler` composable

Create `app/src/main/java/com/example/obby/ui/components/KeyboardShortcutHandler.kt`:

```kotlin
package com.example.obby.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager

data class KeyboardShortcut(
    val key: Key,
    val ctrl: Boolean = false,
    val shift: Boolean = false,
    val alt: Boolean = false,
    val action: () -> Unit
)

@Composable
fun KeyboardShortcutProvider(
    shortcuts: List<KeyboardShortcut>,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.onPreviewKeyEvent { keyEvent ->
            if (keyEvent.type == KeyEventType.KeyDown) {
                shortcuts.find { shortcut ->
                    shortcut.key == keyEvent.key &&
                    shortcut.ctrl == keyEvent.isCtrlPressed &&
                    shortcut.shift == keyEvent.isShiftPressed &&
                    shortcut.alt == keyEvent.isAltPressed
                }?.let { 
                    it.action()
                    return@onPreviewKeyEvent true
                }
            }
            false
        }
    ) {
        content()
    }
}

// Usage in NoteDetailScreen:
// Wrap the content with:
KeyboardShortcutProvider(
    shortcuts = listOf(
        KeyboardShortcut(Key.B, ctrl = true) { /* Bold */ },
        KeyboardShortcut(Key.I, ctrl = true) { /* Italic */ },
        KeyboardShortcut(Key.K, ctrl = true) { /* Insert link */ },
        // ... more shortcuts
    )
) {
    // existing screen content
}
```

### 1.3 Enhanced Markdown Guide

**Current:** Basic cheat sheet dialog  
**Enhancement:** Interactive with syntax highlighting and examples

Update `MarkdownCheatSheet` in `MarkdownToolbar.kt`:

```kotlin
@Composable
fun MarkdownCheatSheet(onDismiss: () -> Unit) {
    var selectedCategory by remember { mutableStateOf(MarkdownCategory.TEXT) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Markdown Guide") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Category tabs
                ScrollableTabRow(selectedTabIndex = selectedCategory.ordinal) {
                    MarkdownCategory.values().forEach { category ->
                        Tab(
                            selected = category == selectedCategory,
                            onClick = { selectedCategory = category },
                            text = { Text(category.displayName) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content for selected category
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedCategory) {
                        MarkdownCategory.TEXT -> TextFormattingExamples()
                        MarkdownCategory.HEADINGS -> HeadingExamples()
                        MarkdownCategory.LISTS -> ListExamples()
                        MarkdownCategory.LINKS -> LinkExamples()
                        MarkdownCategory.CODE -> CodeExamples()
                        MarkdownCategory.ADVANCED -> AdvancedExamples()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

enum class MarkdownCategory(val displayName: String) {
    TEXT("Text"),
    HEADINGS("Headings"),
    LISTS("Lists"),
    LINKS("Links"),
    CODE("Code"),
    ADVANCED("Advanced")
}

@Composable
private fun TextFormattingExamples() {
    ExampleCard(
        title = "Bold",
        syntax = "**bold text** or __bold text__",
        preview = "Makes text bold"
    )
    ExampleCard(
        title = "Italic",
        syntax = "*italic text* or _italic text_",
        preview = "Makes text italic"
    )
    ExampleCard(
        title = "Strikethrough",
        syntax = "~~strikethrough~~",
        preview = "Strikes through text"
    )
}

@Composable
private fun ExampleCard(title: String, syntax: String, preview: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = syntax,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.shapes.small
                    )
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Result: $preview",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

---

## Phase 2: Interactive Preview & UX Polish

**Duration:** 2-3 weeks  
**Priority:** HIGH  
**Goal:** Make preview mode interactive and delightful

### 2.1 Interactive Markdown Preview

**Current:** Static TextView with Markwon rendering  
**Enhancement:** Clickable links + toggleable checkboxes

Create `app/src/main/java/com/example/obby/ui/components/InteractiveMarkdownView.kt`:

```kotlin
package com.example.obby.ui.components

import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.obby.util.CheckboxParser
import io.noties.markwon.*
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import java.util.regex.Pattern

@Composable
fun InteractiveMarkdownView(
    markdown: String,
    onCheckboxToggle: (Int, Boolean) -> Unit,
    onWikiLinkClick: (String) -> Unit,
    onUrlClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    
    val textColor = if (isDarkTheme) {
        android.graphics.Color.parseColor("#E8E6E3")
    } else {
        android.graphics.Color.parseColor("#1C1B1F")
    }
    
    val linkColor = if (isDarkTheme) {
        android.graphics.Color.parseColor("#9C89B8")
    } else {
        android.graphics.Color.parseColor("#6750A4")
    }
    
    val markwon = remember(isDarkTheme) {
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(WikiLinkPlugin(onWikiLinkClick))
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .linkColor(linkColor)
                        .headingTextSizeMultipliers(floatArrayOf(2f, 1.5f, 1.17f, 1f, 0.83f, 0.67f))
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
                setLineSpacing(4f, 1.3f)
                movementMethod = LinkMovementMethod.getInstance()
                
                // Enable checkbox interaction
                setOnTouchListener { v, event ->
                    // Custom touch handling for checkboxes
                    false
                }
            }
        },
        update = { textView ->
            try {
                val spanned = markwon.toMarkdown(markdown)
                
                // Add checkbox click handlers
                val checkboxes = CheckboxParser.findCheckboxes(markdown)
                // Apply checkbox spans that can be clicked
                
                textView.text = spanned
                textView.setTextColor(textColor)
            } catch (e: Exception) {
                textView.text = markdown
            }
        }
    )
}

// Wiki-link plugin for Markwon
class WikiLinkPlugin(
    private val onLinkClick: (String) -> Unit
) : AbstractMarkwonPlugin() {
    override fun processMarkdown(markdown: String): String {
        // Replace [[Note Title]] with markdown links
        val pattern = Pattern.compile("\\[\\[([^\\]]+)\\]\\]")
        val matcher = pattern.matcher(markdown)
        val result = StringBuffer()
        
        while (matcher.find()) {
            val linkText = matcher.group(1)
            val parts = linkText.split("|")
            val target = parts[0]
            val display = parts.getOrNull(1) ?: target
            
            // Replace with [display](wiki://target)
            matcher.appendReplacement(result, "[$display](wiki://$target)")
        }
        matcher.appendTail(result)
        
        return result.toString()
    }
    
    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
        builder.setFactory(Link::class.java) { configuration, props ->
            val destination = props.get(CoreProps.LINK_DESTINATION) ?: return@setFactory null
            
            if (destination.startsWith("wiki://")) {
                val noteTitle = destination.removePrefix("wiki://")
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onLinkClick(noteTitle)
                    }
                }
            } else {
                // Default link behavior
                null
            }
        }
    }
}
```

Create `app/src/main/java/com/example/obby/util/CheckboxParser.kt`:

```kotlin
package com.example.obby.util

import java.util.regex.Pattern

data class CheckboxMatch(
    val index: Int,
    val lineStart: Int,
    val lineEnd: Int,
    val isChecked: Boolean,
    val text: String
)

object CheckboxParser {
    
    private val CHECKBOX_PATTERN = Pattern.compile("^(\\s*)- \\[([ x])\\](.*)$", Pattern.MULTILINE)
    
    fun findCheckboxes(markdown: String): List<CheckboxMatch> {
        val matches = mutableListOf<CheckboxMatch>()
        val matcher = CHECKBOX_PATTERN.matcher(markdown)
        var index = 0
        
        while (matcher.find()) {
            val lineStart = matcher.start()
            val lineEnd = matcher.end()
            val isChecked = matcher.group(2) == "x"
            val text = matcher.group(3)?.trim() ?: ""
            
            matches.add(
                CheckboxMatch(
                    index = index++,
                    lineStart = lineStart,
                    lineEnd = lineEnd,
                    isChecked = isChecked,
                    text = text
                )
            )
        }
        
        return matches
    }
    
    fun toggleCheckbox(markdown: String, checkboxIndex: Int): String {
        val checkboxes = findCheckboxes(markdown)
        
        if (checkboxIndex >= checkboxes.size) return markdown
        
        val checkbox = checkboxes[checkboxIndex]
        val newState = if (checkbox.isChecked) " " else "x"
        
        return StringBuilder(markdown).apply {
            // Find the position of the checkbox marker
            val checkboxMarkerPos = checkbox.lineStart + markdown.substring(checkbox.lineStart).indexOf('[') + 1
            setCharAt(checkboxMarkerPos, newState[0])
        }.toString()
    }
    
    fun toggleCheckboxAtPosition(markdown: String, cursorPosition: Int): String? {
        val checkboxes = findCheckboxes(markdown)
        
        // Find checkbox containing cursor position
        val checkbox = checkboxes.find { cursorPosition in it.lineStart..it.lineEnd }
            ?: return null
        
        return toggleCheckbox(markdown, checkbox.index)
    }
}
```

### 2.2 Update NoteDetailViewModel for Interactive Preview

Add to `NoteDetailViewModel.kt`:

```kotlin
fun toggleCheckbox(checkboxIndex: Int) {
    viewModelScope.launch {
        note.value?.let { currentNote ->
            val newContent = CheckboxParser.toggleCheckbox(currentNote.content, checkboxIndex)
            repository.updateNote(currentNote.copy(content = newContent))
            _snackbarMessage.emit("Checkbox toggled")
        }
    }
}

fun navigateToLinkedNote(noteTitle: String): Long? {
    // Find note by title and return its ID
    // This would need a new repository method
    viewModelScope.launch {
        try {
            val targetNote = repository.findNoteByTitle(noteTitle)
            if (targetNote != null) {
                return@launch targetNote.id
            } else {
                _snackbarMessage.emit("Note '$noteTitle' not found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to linked note", e)
        }
    }
    return null
}
```

Add to `NoteRepository.kt`:

```kotlin
suspend fun findNoteByTitle(title: String): Note? {
    return noteDao.getNoteByTitle(title)
}
```

### 2.3 Auto-Save Indicator Enhancement

Create `app/src/main/java/com/example/obby/ui/components/AutoSaveIndicator.kt`:

```kotlin
package com.example.obby.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class SaveState {
    SAVED,
    SAVING,
    UNSAVED,
    ERROR
}

@Composable
fun AutoSaveIndicator(
    saveState: SaveState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = saveState != SaveState.SAVED,
        enter = fadeIn() + expandHorizontally(),
        exit = fadeOut() + shrinkHorizontally(),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            when (saveState) {
                SaveState.SAVING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        "Saving...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SaveState.UNSAVED -> {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Unsaved changes",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        "Unsaved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                SaveState.ERROR -> {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Save error",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }
        }
    }
}

// Update NoteDetailScreen to use:
topBar = {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (editMode) {
                    BasicTextField(...)
                } else {
                    Text(note?.title ?: "")
                }
                AutoSaveIndicator(
                    saveState = when {
                        pendingChanges -> SaveState.SAVING
                        else -> SaveState.SAVED
                    }
                )
            }
        },
        // ...
    )
}
```

### 2.4 Enhanced Empty States

Create `app/src/main/java/com/example/obby/ui/components/EmptyStateViews.kt`:

```kotlin
package com.example.obby.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class EmptyStateType {
    NO_NOTES,
    NO_SEARCH_RESULTS,
    EMPTY_FOLDER,
    NO_TAGS,
    NO_LINKED_NOTES,
    NO_BACKLINKS
}

@Composable
fun EnhancedEmptyState(
    type: EmptyStateType,
    query: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (type) {
                EmptyStateType.NO_NOTES -> {
                    Icon(
                        Icons.Default.NoteAdd,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No notes yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap the + button to create your first note",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                EmptyStateType.NO_SEARCH_RESULTS -> {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No results found",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    if (query != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No notes match \"$query\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                EmptyStateType.EMPTY_FOLDER -> {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Empty folder",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Create a note or move existing notes here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                EmptyStateType.NO_LINKED_NOTES -> {
                    Icon(
                        Icons.Default.LinkOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No linked notes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Use [[Note Title]] to link to other notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                EmptyStateType.NO_BACKLINKS -> {
                    Icon(
                        Icons.Default.LinkOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No backlinks",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "No other notes link to this one yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                else -> {}
            }
            
            if (onAction != null) {
                Spacer(modifier = Modifier.height(24.dp))
                FilledTonalButton(onClick = onAction) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Note")
                }
            }
        }
    }
}
```

---

## Phase 3: Advanced Organization

**Duration:** 3 weeks  
**Priority:** MEDIUM-HIGH  
**Goal:** Nested folders, drag-drop, advanced list management

### 3.1 Nested Folders Support

**Good News:** The `Folder` entity already has `parentFolderId: Long?` field! Just need UI.

#### Update NotesViewModel for Hierarchical Folders

Add to `NotesViewModel.kt`:

```kotlin
// Get folder hierarchy (root → children)
fun getFolderHierarchy(): StateFlow<List<FolderNode>> {
    return folders.map { allFolders ->
        buildFolderTree(allFolders)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

private fun buildFolderTree(folders: List<Folder>): List<FolderNode> {
    val folderMap = folders.associateBy { it.id }
    val rootFolders = folders.filter { it.parentFolderId == null }
    
    fun buildNode(folder: Folder, depth: Int = 0): FolderNode {
        val children = folders
            .filter { it.parentFolderId == folder.id }
            .map { buildNode(it, depth + 1) }
        
        return FolderNode(
            folder = folder,
            children = children,
            depth = depth,
            isExpanded = true // or track in state
        )
    }
    
    return rootFolders.map { buildNode(it) }
}

data class FolderNode(
    val folder: Folder,
    val children: List<FolderNode>,
    val depth: Int,
    val isExpanded: Boolean
)

suspend fun createSubfolder(name: String, parentId: Long?) {
    try {
        val folder = Folder(
            name = name,
            parentFolderId = parentId,
            createdAt = System.currentTimeMillis()
        )
        repository.insertFolder(folder)
        _actionMessage.emit("Subfolder created")
    } catch (e: Exception) {
        Log.e(TAG, "Error creating subfolder", e)
        _actionMessage.emit("Failed to create subfolder")
    }
}
```

#### Create Hierarchical Folder Picker

Create `app/src/main/java/com/example/obby/ui/components/HierarchicalFolderPicker.kt`:

```kotlin
package com.example.obby.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.obby.data.local.entity.Folder
import com.example.obby.ui.viewmodel.NotesViewModel.FolderNode

@Composable
fun HierarchicalFolderPicker(
    folderTree: List<FolderNode>,
    currentFolderId: Long?,
    onFolderSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedFolders by remember { mutableStateOf(setOf<Long>()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move to Folder") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                // Root option
                item {
                    FolderPickerItem(
                        folder = null,
                        depth = 0,
                        isSelected = currentFolderId == null,
                        isExpanded = false,
                        hasChildren = false,
                        onToggleExpand = {},
                        onClick = { onFolderSelected(null) }
                    )
                    Divider()
                }
                
                // Recursive folder tree
                items(folderTree) { node ->
                    FolderTreeItem(
                        node = node,
                        currentFolderId = currentFolderId,
                        expandedFolders = expandedFolders,
                        onToggleExpand = { folderId ->
                            expandedFolders = if (expandedFolders.contains(folderId)) {
                                expandedFolders - folderId
                            } else {
                                expandedFolders + folderId
                            }
                        },
                        onSelect = onFolderSelected
                    )
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

@Composable
private fun FolderTreeItem(
    node: FolderNode,
    currentFolderId: Long?,
    expandedFolders: Set<Long>,
    onToggleExpand: (Long) -> Unit,
    onSelect: (Long) -> Unit
) {
    val isExpanded = expandedFolders.contains(node.folder.id)
    val hasChildren = node.children.isNotEmpty()
    
    Column {
        FolderPickerItem(
            folder = node.folder,
            depth = node.depth,
            isSelected = node.folder.id == currentFolderId,
            isExpanded = isExpanded,
            hasChildren = hasChildren,
            onToggleExpand = { onToggleExpand(node.folder.id) },
            onClick = { onSelect(node.folder.id) }
        )
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                node.children.forEach { childNode ->
                    FolderTreeItem(
                        node = childNode,
                        currentFolderId = currentFolderId,
                        expandedFolders = expandedFolders,
                        onToggleExpand = onToggleExpand,
                        onSelect = onSelect
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderPickerItem(
    folder: Folder?,
    depth: Int,
    isSelected: Boolean,
    isExpanded: Boolean,
    hasChildren: Boolean,
    onToggleExpand: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                start = (16 + depth * 24).dp,
                top = 12.dp,
                bottom = 12.dp,
                end = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasChildren) {
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        } else if (depth > 0) {
            Spacer(modifier = Modifier.width(28.dp))
        }
        
        Icon(
            if (folder == null) Icons.Default.Home else Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = folder?.name ?: "Root",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}
```

### 3.2 Drag-and-Drop Note Reordering

**Database Update:** Add `sortOrder` field to `Note` entity

Update `app/src/main/java/com/example/obby/data/local/entity/Note.kt`:

```kotlin
@Entity(
    tableName = "notes",
    indices = [Index(value = ["title"], unique = false)]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val folderId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isEncrypted: Boolean = false,
    val sortOrder: Int = 0  // NEW FIELD
)
```

**Migration:** Create migration file

Create `app/src/main/java/com/example/obby/data/local/migrations/Migration2to3.kt`:

```kotlin
package com.example.obby.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add sortOrder column to notes table
        database.execSQL("ALTER TABLE notes ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
        
        // Initialize sortOrder based on creation time
        database.execSQL("""
            UPDATE notes SET sortOrder = 
            (SELECT COUNT(*) FROM notes n2 WHERE n2.createdAt <= notes.createdAt)
        """)
    }
}
```

Update `ObbyDatabase.kt`:

```kotlin
@Database(
    entities = [Note::class, Folder::class, Tag::class, NoteTagCrossRef::class, NoteLink::class],
    version = 3,  // Increment version
    exportSchema = true
)
abstract class ObbyDatabase : RoomDatabase() {
    // ...
    
    companion object {
        @Volatile
        private var INSTANCE: ObbyDatabase? = null
        
        fun getDatabase(context: Context): ObbyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ObbyDatabase::class.java,
                    "obby_database"
                )
                    .addMigrations(MIGRATION_2_3)  // Add migration
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**Implement Drag-and-Drop**

Create `app/src/main/java/com/example/obby/ui/components/DraggableNoteList.kt`:

```kotlin
package com.example.obby.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.obby.data.local.entity.Note

@Composable
fun DraggableNoteList(
    notes: List<Note>,
    isDragEnabled: Boolean,
    onNoteClick: (Note) -> Unit,
    onNotesReordered: (List<Note>) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (Note, Boolean, () -> Unit) -> Unit
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    
    val notesState = remember(notes) { notes.toMutableStateList() }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = notesState,
            key = { _, note -> note.id }
        ) { index, note ->
            val isDragging = draggedIndex == index
            val elevation by animateDpAsState(
                targetValue = if (isDragging) 8.dp else 0.dp,
                label = "elevation"
            )
            
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        if (isDragging) {
                            translationY = dragOffset.y
                            alpha = 0.8f
                        }
                    }
                    .then(
                        if (isDragEnabled) {
                            Modifier.pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedIndex = index
                                        dragOffset = Offset.Zero
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount
                                        
                                        // Calculate target index based on drag position
                                        val itemHeight = 80.dp.toPx() // Approximate
                                        val offsetItems = (dragOffset.y / itemHeight).toInt()
                                        targetIndex = (index + offsetItems).coerceIn(0, notesState.size - 1)
                                    },
                                    onDragEnd = {
                                        draggedIndex?.let { fromIndex ->
                                            targetIndex?.let { toIndex ->
                                                if (fromIndex != toIndex) {
                                                    val item = notesState.removeAt(fromIndex)
                                                    notesState.add(toIndex, item)
                                                    onNotesReordered(notesState.toList())
                                                }
                                            }
                                        }
                                        draggedIndex = null
                                        targetIndex = null
                                        dragOffset = Offset.Zero
                                    },
                                    onDragCancel = {
                                        draggedIndex = null
                                        targetIndex = null
                                        dragOffset = Offset.Zero
                                    }
                                )
                            }
                        } else Modifier
                    )
            ) {
                itemContent(note, isDragging) {
                    if (!isDragging) onNoteClick(note)
                }
            }
        }
    }
}
```

Add reordering logic to `NoteRepository.kt`:

```kotlin
suspend fun reorderNotes(notes: List<Note>) {
    notes.forEachIndexed { index, note ->
        noteDao.updateNote(note.copy(sortOrder = index))
    }
}
```

Update `NoteDao.kt` to sort by `sortOrder`:

```kotlin
@Query("SELECT * FROM notes ORDER BY isPinned DESC, sortOrder ASC, modifiedAt DESC")
fun getAllNotes(): Flow<List<Note>>
```

### 3.3 Breadcrumb Navigation for Nested Folders

Create `app/src/main/java/com/example/obby/ui/components/FolderBreadcrumb.kt`:

```kotlin
package com.example.obby.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.obby.data.local.entity.Folder

@Composable
fun FolderBreadcrumb(
    currentFolderPath: List<Folder>,
    onFolderClick: (Folder?) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Root/Home
            TextButton(
                onClick = { onFolderClick(null) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("All Notes", style = MaterialTheme.typography.bodySmall)
            }
            
            // Breadcrumb trail
            currentFolderPath.forEach { folder ->
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                TextButton(
                    onClick = { onFolderClick(folder) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(folder.name, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
```

---

## Phase 4: Testing & Optimization

**Duration:** 1-2 weeks  
**Priority:** HIGH  
**Goal:** Ensure reliability and performance

### 4.1 Unit Tests

Create test files:

**`NoteRepositoryTest.kt`:**

```kotlin
package com.example.obby.data.repository

import com.example.obby.data.local.dao.*
import com.example.obby.data.local.entity.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class NoteRepositoryTest {
    
    @Mock lateinit var noteDao: NoteDao
    @Mock lateinit var folderDao: FolderDao
    @Mock lateinit var tagDao: TagDao
    @Mock lateinit var noteLinkDao: NoteLinkDao
    
    private lateinit var repository: NoteRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = NoteRepository(noteDao, folderDao, tagDao, noteLinkDao)
    }
    
    @Test
    fun `insertNote creates links when wiki-style links present`() = runTest {
        // Given
        val note = Note(
            id = 0,
            title = "Test Note",
            content = "See [[Another Note]] for details"
        )
        
        val targetNote = Note(
            id = 2,
            title = "Another Note",
            content = "Content"
        )
        
        `when`(noteDao.insertNote(any())).thenReturn(1L)
        `when`(noteDao.getNoteByTitle("Another Note")).thenReturn(targetNote)
        
        // When
        repository.insertNote(note)
        
        // Then
        verify(noteLinkDao).insertLink(
            argThat { link ->
                link.sourceNoteId == 1L && link.targetNoteId == 2L
            }
        )
    }
    
    @Test
    fun `updateNote re-parses links and tags`() = runTest {
        // Test link/tag re-parsing on update
        // ...
    }
    
    @Test
    fun `generateNoteGraph creates correct node structure`() = runTest {
        // Test graph generation
        // ...
    }
}
```

**`CheckboxParserTest.kt`:**

```kotlin
package com.example.obby.util

import org.junit.Assert.*
import org.junit.Test

class CheckboxParserTest {
    
    @Test
    fun `findCheckboxes detects all checkboxes`() {
        val markdown = """
            # Todo List
            - [ ] Unchecked item
            - [x] Checked item
            - [ ] Another unchecked
        """.trimIndent()
        
        val checkboxes = CheckboxParser.findCheckboxes(markdown)
        
        assertEquals(3, checkboxes.size)
        assertFalse(checkboxes[0].isChecked)
        assertTrue(checkboxes[1].isChecked)
        assertFalse(checkboxes[2].isChecked)
    }
    
    @Test
    fun `toggleCheckbox changes state correctly`() {
        val markdown = "- [ ] Task"
        val toggled = CheckboxParser.toggleCheckbox(markdown, 0)
        
        assertTrue(toggled.contains("- [x] Task"))
    }
    
    @Test
    fun `toggleCheckbox handles multiple checkboxes`() {
        val markdown = """
            - [ ] First
            - [x] Second
            - [ ] Third
        """.trimIndent()
        
        val toggled = CheckboxParser.toggleCheckbox(markdown, 1)
        
        assertTrue(toggled.contains("- [ ] Second"))
    }
}
```

### 4.2 Compose UI Tests

**`NoteDetailScreenTest.kt`:**

```kotlin
package com.example.obby.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class NoteDetailScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `toolbar is visible in edit mode`() {
        // Setup
        composeTestRule.setContent {
            // NoteDetailScreen with edit mode = true
        }
        
        // Assert toolbar is displayed
        composeTestRule.onNodeWithContentDescription("Bold").assertIsDisplayed()
    }
    
    @Test
    fun `toolbar is hidden in preview mode`() {
        // Setup
        composeTestRule.setContent {
            // NoteDetailScreen with preview mode = true
        }
        
        // Assert toolbar is not displayed
        composeTestRule.onNodeWithContentDescription("Bold").assertDoesNotExist()
    }
    
    @Test
    fun `undo button is enabled after text change`() {
        // Test undo functionality
    }
}
```

### 4.3 Performance Optimization

**Implement Paging for Large Note Lists:**

Add to `build.gradle.kts`:

```kotlin
dependencies {
    implementation("androidx.paging:paging-runtime:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")
}
```

Update `NoteDao.kt`:

```kotlin
import androidx.paging.PagingSource

@Query("SELECT * FROM notes ORDER BY isPinned DESC, sortOrder ASC, modifiedAt DESC")
fun getAllNotesPaged(): PagingSource<Int, Note>
```

Update `NoteRepository.kt`:

```kotlin
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData

fun getAllNotesPaged(): Flow<PagingData<Note>> {
    return Pager(
        config = PagingConfig(
            pageSize = 50,
            enablePlaceholders = false,
            prefetchDistance = 10
        ),
        pagingSourceFactory = { noteDao.getAllNotesPaged() }
    ).flow
}
```

---

## Architecture Updates

### Updated Architecture Diagram

```
┌───────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                         │
├───────────────────────────────────────────────────────────────────┤
│  Screens                                                           │
│  ├─ NotesListScreen (enhanced with drag-drop)                     │
│  ├─ NoteDetailScreen (interactive preview + toolbar)              │
│  ├─ GraphScreen (existing)                                        │
│  └─ FolderTreeScreen (NEW - hierarchical navigation)              │
│                                                                    │
│  Components                                                        │
│  ├─ MarkdownToolbar (existing, enhanced)                          │
│  ├─ InteractiveMarkdownView (NEW)                                 │
│  ├─ DraggableNoteList (NEW)                                       │
│  ├─ HierarchicalFolderPicker (NEW)                                │
│  ├─ AutoSaveIndicator (NEW)                                       │
│  ├─ EnhancedEmptyState (NEW)                                      │
│  └─ FolderBreadcrumb (NEW)                                        │
│                                                                    │
│  ViewModels (State Management)                                    │
│  ├─ NotesViewModel (enhanced for nested folders)                  │
│  ├─ NoteDetailViewModel (enhanced for interactive preview)        │
│  └─ GraphViewModel (existing)                                     │
└───────────────────────────────────────────────────────────────────┘
                                  │
┌───────────────────────────────────────────────────────────────────┐
│                          DOMAIN LAYER (NEW)                        │
├───────────────────────────────────────────────────────────────────┤
│  Models                                                            │
│  ├─ GraphNode, GraphEdge (existing)                               │
│  └─ FolderNode (NEW - tree structure)                             │
│                                                                    │
│  Use Cases (optional, for complex logic)                          │
│  ├─ ToggleCheckboxUseCase                                         │
│  └─ NavigateToLinkedNoteUseCase                                   │
└───────────────────────────────────────────────────────────────────┘
                                  │
┌───────────────────────────────────────────────────────────────────┐
│                          DATA LAYER                                │
├───────────────────────────────────────────────────────────────────┤
│  Repository (Single Source of Truth)                              │
│  └─ NoteRepository (enhanced)                                     │
│     ├─ insertNote() - creates links/tags                          │
│     ├─ updateNote() - re-parses links/tags                        │
│     ├─ reorderNotes() (NEW)                                       │
│     └─ findNoteByTitle() (NEW)                                    │
│                                                                    │
│  Local Database (Room)                                            │
│  ├─ ObbyDatabase (v3 with migration)                              │
│  ├─ DAOs                                                           │
│  │  ├─ NoteDao (updated for sortOrder)                            │
│  │  ├─ FolderDao (existing, supports parentFolderId)              │
│  │  ├─ TagDao (existing)                                          │
│  │  └─ NoteLinkDao (existing)                                     │
│  │                                                                 │
│  └─ Entities                                                       │
│     ├─ Note (+ sortOrder field)                                   │
│     ├─ Folder (existing, parentFolderId already present!)         │
│     ├─ Tag (existing)                                              │
│     ├─ NoteLink (existing)                                         │
│     └─ NoteTagCrossRef (existing)                                 │
│                                                                    │
│  Utilities                                                         │
│  ├─ MarkdownLinkParser (existing)                                 │
│  ├─ CheckboxParser (NEW)                                          │
│  ├─ EncryptionManager (existing)                                  │
│  └─ FileExporter (existing)                                       │
└───────────────────────────────────────────────────────────────────┘
```

---

## Implementation Guide

### Step-by-Step Implementation Order

#### Week 1-2: Foundation & Enhanced Editor

1. **Day 1-2:** Database migration
    - Add `sortOrder` field to `Note`
    - Create and test migration
    - Update DAOs

2. **Day 3-4:** Toolbar enhancements
    - Add haptic feedback to toolbar buttons
    - Implement keyboard shortcuts
    - Test on physical device

3. **Day 5-7:** Enhanced Markdown guide
    - Create tabbed guide interface
    - Add example cards with syntax highlighting
    - Integrate into toolbar

4. **Day 8-10:** Auto-save indicator
    - Create `AutoSaveIndicator` component
    - Integrate with `NoteDetailViewModel`
    - Add save state tracking

#### Week 3-4: Interactive Preview

1. **Day 11-13:** Checkbox parser
    - Implement `CheckboxParser` utility
    - Write comprehensive unit tests
    - Test with various markdown formats

2. **Day 14-16:** Interactive markdown view
    - Create `InteractiveMarkdownView` component
    - Implement checkbox toggle in preview
    - Add click handlers for wiki-links

3. **Day 17-19:** Link navigation
    - Update `NoteRepository` with `findNoteByTitle()`
    - Implement navigation from preview links
    - Test deep linking

4. **Day 20-21:** Preview polish
    - Add animations for checkbox toggles
    - Implement scroll position preservation
    - Test with long documents

#### Week 5-6: Advanced Organization

1. **Day 22-24:** Hierarchical folder UI
    - Create `HierarchicalFolderPicker`
    - Build folder tree in `NotesViewModel`
    - Add expand/collapse animations

2. **Day 25-27:** Breadcrumb navigation
    - Create `FolderBreadcrumb` component
    - Implement folder path tracking
    - Add navigation from breadcrumbs

3. **Day 28-30:** Drag-and-drop
    - Create `DraggableNoteList` component
    - Implement reordering logic
    - Persist order with `sortOrder` field

4. **Day 31-33:** Subfolder creation
    - Add "New Subfolder" option in folder picker
    - Update folder creation dialog
    - Test nested folder operations

#### Week 7: Polish & Empty States

1. **Day 34-36:** Enhanced empty states
    - Create `EnhancedEmptyState` component
    - Add illustrations and guidance
    - Implement for all list views

2. **Day 37-39:** Animation polish
    - Add smooth transitions between screens
    - Implement list item animations
    - Polish drag-and-drop feedback

3. **Day 40-42:** Multi-select enhancements
    - Add animated selection mode
    - Implement batch operations progress
    - Test with large selections

#### Week 8: Testing & Optimization

1. **Day 43-45:** Unit tests
    - Write tests for `CheckboxParser`
    - Test `NoteRepository` logic
    - Test folder tree building

2. **Day 46-48:** UI tests
    - Write Compose UI tests
    - Test user flows
    - Test edge cases

3. **Day 49-51:** Performance optimization
    - Implement paging for large lists
    - Profile and optimize hot paths
    - Test with 1000+ notes

4. **Day 52-54:** Bug fixes & polish
    - Address test failures
    - Fix edge cases
    - Final UX polish

---

## UI/UX Wireframes

### 1. Enhanced Editor Screen

```
┌─────────────────────────────────────────┐
│  ←  [ Note Title          ]  ↶ ↷ 👁️ ✓ ⋮ │  <- Top bar
├─────────────────────────────────────────┤
│                                         │
│  # Heading                              │  <- Edit area
│                                         │     (BasicTextField)
│  - [ ] Task 1                           │
│  - [x] Task 2                           │
│                                         │
│  See [[Another Note]] for more info.    │
│                                         │
│  ▓                                      │  <- Cursor
│                                         │
├─────────────────────────────────────────┤
│ [B] [I] [~] [#▾] • 1. ☐ " ` </> 🔗 ?  │  <- Toolbar (persistent)
└─────────────────────────────────────────┘
```

### 2. Interactive Preview Mode

```
┌─────────────────────────────────────────┐
│  ←  [ Note Title          ]  ✏️ ⋮       │  <- Edit icon (not preview)
├─────────────────────────────────────────┤
│                                         │
│  # Heading                              │  <- Rendered markdown
│                                         │     (Markwon)
│  ☐ Task 1     <- clickable              │
│  ☑ Task 2     <- clickable              │
│                                         │
│  See Another Note for more info.        │
│       ^^^^^^^^^                         │
│       (clickable link, jumps to note)   │
│                                         │
│  ┌───────────────────────────┐         │
│  │ Linked Notes              │         │
│  │ • Project Ideas           │         │
│  └───────────────────────────┘         │
│                                         │
│  ┌───────────────────────────┐         │
│  │ Backlinks (2)             │         │
│  │ • Meeting Notes           │         │
│  │ • Weekly Review           │         │
│  └───────────────────────────┘         │
└─────────────────────────────────────────┘
(No toolbar in preview mode)
```

### 3. Notes List with Multi-Select

```
┌─────────────────────────────────────────┐
│  ✕  3 selected       [⚇] [→] [🗑️]      │  <- Multi-select bar
├─────────────────────────────────────────┤
│  🔍 Search notes...                  🕸  │
├─────────────────────────────────────────┤
│  ┌─────────────────────────────────┐   │
│  │ ☑ 📌 Project Ideas         ⋮   │   │  <- Selected
│  │   Some content preview...       │   │
│  │   📁 Work · Dec 5               │   │
│  └─────────────────────────────────┘   │
│  ┌─────────────────────────────────┐   │
│  │ ☑ Meeting Notes            ⋮   │   │  <- Selected
│  │   Discussed quarterly goals...  │   │
│  │   📁 Work · Dec 4               │   │
│  └─────────────────────────────────┘   │
│  ┌─────────────────────────────────┐   │
│  │ ☐ Shopping List            ⋮   │   │  <- Not selected
│  │   - Milk - Eggs - Bread         │   │
│  │   Dec 3                         │   │
│  └─────────────────────────────────┘   │
│                                         │
│                              [+]        │  <- FAB hidden in multi-select
└─────────────────────────────────────────┘
```

### 4. Hierarchical Folder Picker

```
┌─────────────────────────────────────────┐
│  Move to Folder                         │
├─────────────────────────────────────────┤
│  🏠 Root                                 │  <- Always shown
│  ──────────────────────────────────────  │
│  ▼ 📁 Work              <- Expanded     │
│      ▼ 📁 Projects                      │
│          📁 Client A                    │
│          📁 Client B   <- Selected      │
│      ▶ 📁 Archive                       │
│  ▼ 📁 Personal                          │
│      📁 Journal                         │
│      📁 Ideas                           │
│  📁 Reading                             │
│                                         │
│                            [Cancel]     │
└─────────────────────────────────────────┘
```

### 5. Enhanced Empty State

```
┌─────────────────────────────────────────┐
│  ☰  Obby                          🕸    │
├─────────────────────────────────────────┤
│                                         │
│                                         │
│             ┌───────────┐              │
│             │    📝     │              │  <- Large icon
│             └───────────┘              │
│                                         │
│          No notes yet                   │  <- Headline
│                                         │
│     Tap + to create your first note     │  <- Subtext
│                                         │
│                                         │
│                                         │
│                              [+]        │  <- FAB
└─────────────────────────────────────────┘
```

### 6. Context Menu (Long-Press)

```
┌─────────────────────────────────────────┐
│  Note Title                             │
│  ┌───────────────────────────────────┐ │
│  │ 📌 Pin                            │ │
│  │ ⭐ Add to favorites               │ │
│  │ ──────────────────────────────────│ │
│  │ ✏️ Rename                          │ │
│  │ 📋 Duplicate                      │ │
│  │ 📁 Move to folder                 │ │
│  │ 🔗 Share                          │ │
│  │ ──────────────────────────────────│ │
│  │ 🗑️ Delete                         │ │  <- Red text
│  └───────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

---

## Library Recommendations

### Essential Libraries (Already in Use)

| Library | Version | Purpose |
|---------|---------|---------|
| Markwon | 4.6.2 | Markdown rendering ✅ |
| Room | 2.6.1 | Database ✅ |
| Compose | 2024.09.00 | UI framework ✅ |
| Navigation | 2.8.5 | Screen navigation ✅ |

### Recommended Additions

| Library | Version | Purpose | Priority |
|---------|---------|---------|----------|
| Paging 3 | 3.2.1 | Large list performance | HIGH |
| Accompanist | 0.32.0 | Advanced Compose utilities | MEDIUM |
| Coil | 2.5.0 | Image loading (future attachments) | LOW |
| JUnit | 4.13.2 | Unit testing ✅ | HIGH |
| Mockito | 5.6.0 | Mocking for tests | HIGH |
| Turbine | 1.0.0 | Flow testing | MEDIUM |

### Optional Libraries

| Library | Purpose | Note |
|---------|---------|------|
| LeakCanary | Memory leak detection | Debug builds only |
| Timber | Enhanced logging | Replace Log.d/e |
| Detekt | Code quality | CI/CD integration |

---

## Testing Strategy

### Test Coverage Goals

| Layer | Target Coverage | Priority |
|-------|----------------|----------|
| Utilities | 90%+ | HIGH |
| Repository | 80%+ | HIGH |
| ViewModel | 70%+ | MEDIUM |
| UI Components | 50%+ | MEDIUM |
| Screens | 30%+ | LOW |

### Test Types

#### 1. Unit Tests

**Files to Test:**

- `CheckboxParser` - 100% coverage
- `MarkdownLinkParser` - 90%+
- `EncryptionManager` - Critical paths
- `NoteRepository` - All public methods
- ViewModels - State transitions

**Example Test Structure:**

```kotlin
class CheckboxParserTest {
    @Test fun `findCheckboxes detects all formats`()
    @Test fun `toggleCheckbox changes state`()
    @Test fun `toggleCheckbox handles edge cases`()
    @Test fun `toggleCheckboxAtPosition finds correct checkbox`()
}
```

#### 2. Integration Tests

**Scenarios:**

- Note creation with link/tag parsing
- Folder hierarchy building
- Note reordering persistence
- Encryption/decryption round-trip

#### 3. UI Tests (Compose)

**Scenarios:**

- Toolbar visibility in edit/preview modes
- Multi-select mode activation
- Drag-and-drop reordering
- Empty state display

#### 4. Manual Testing Checklist

- [ ] Create note with 10,000+ characters
- [ ] Test with 500+ notes in database
- [ ] Verify encryption with incorrect password
- [ ] Test nested folders 5+ levels deep
- [ ] Drag-and-drop across large distances
- [ ] Multi-select 100+ notes and move
- [ ] Toggle checkbox in preview mode
- [ ] Click wiki-link to non-existent note
- [ ] Test on various screen sizes (tablet, phone)
- [ ] Test with accessibility services (TalkBack)

---

## Migration & Compatibility

### Database Migration Plan

#### Version 2 → 3 Migration

**Changes:**

- Add `sortOrder` column to `notes` table
- Initialize `sortOrder` based on creation time

**Migration Code:** (See Phase 3 section above)

**Validation:**

1. Back up database before migration
2. Test migration on copy of production data
3. Verify all notes retain correct data
4. Check foreign key integrity

### Backward Compatibility

**Guarantees:**

- ✅ All existing notes remain readable
- ✅ Folder structure preserved (parentFolderId already exists!)
- ✅ Links and tags unchanged
- ✅ Encryption keys remain valid
- ✅ No data loss on upgrade

**User Experience:**

- Seamless upgrade (no user action required)
- Existing folders work as root-level folders
- Notes default to chronological order (sortOrder = 0)
- All features accessible immediately

### Rollback Plan

If critical issues arise:

1. **Database:** Keep v2 schema backup
2. **APK:** Provide v1.x download link
3. **Data:** Export notes before upgrade (optional)

---

## Timeline & Milestones

### 8-Week Implementation Timeline

```
┌─────────────────────────────────────────────────────────────────┐
│                     OBBI 2.0 IMPLEMENTATION                      │
└─────────────────────────────────────────────────────────────────┘

Week 1-2: PHASE 1 - Enhanced Editor
├─ Database migration (sortOrder field)
├─ Toolbar haptic feedback & keyboard shortcuts
├─ Enhanced Markdown guide (tabbed interface)
└─ Auto-save indicator component
   ✅ Milestone: Professional editor experience

Week 3-4: PHASE 2 - Interactive Preview (Part 1)
├─ CheckboxParser utility + unit tests
├─ InteractiveMarkdownView component
├─ Checkbox toggle in preview mode
└─ Wiki-link click navigation
   ✅ Milestone: Preview mode is interactive

Week 5-6: PHASE 3 - Advanced Organization
├─ Hierarchical folder picker UI
├─ Breadcrumb navigation component
├─ Drag-and-drop note reordering
└─ Subfolder creation workflow
   ✅ Milestone: Power-user organization tools

Week 7: PHASE 4 - Polish & Empty States
├─ Enhanced empty state components
├─ Animation polish (transitions, drag feedback)
├─ Multi-select mode enhancements
└─ Accessibility improvements
   ✅ Milestone: Delightful UX

Week 8: PHASE 5 - Testing & Optimization
├─ Unit tests (80%+ coverage on utilities)
├─ Compose UI tests (key user flows)
├─ Performance optimization (paging, profiling)
└─ Bug fixes and final polish
   ✅ Milestone: Production-ready release

┌─────────────────────────────────────────────────────────────────┐
│                       🚀 RELEASE: OBBI 2.0                       │
│          "Obsidian-Class Knowledge Base, Fully Offline"          │
└─────────────────────────────────────────────────────────────────┘
```

### Release Checklist

#### Pre-Release (Week 8)

- [ ] All migrations tested
- [ ] Unit test coverage > 70%
- [ ] UI tests passing
- [ ] Performance profiling complete
- [ ] Accessibility audit
- [ ] Documentation updated
- [ ] CHANGELOG.md created

#### Release Day

- [ ] Version bump to 2.0.0
- [ ] Create Git tag
- [ ] Build signed APK
- [ ] Upload to GitHub Releases
- [ ] Update README with new features
- [ ] Announce on community channels

#### Post-Release (Week 9+)

- [ ] Monitor crash reports
- [ ] Gather user feedback
- [ ] Prioritize bug fixes
- [ ] Plan Phase 6 (future enhancements)

---

## Success Metrics

### Quantitative Goals

| Metric | Current | Target | Measure |
|--------|---------|--------|---------|
| Editor Actions | 3 clicks to format | 1 click | Toolbar efficiency |
| Preview Interaction | 0 (static) | 100% interactive | Checkbox/link clicks |
| Folder Depth | 1 level (flat) | 10+ levels | Nested folders |
| Note Reordering | Manual only | Drag-and-drop | UX improvement |
| Auto-save Delay | 2s (invisible) | 2s with indicator | User feedback |
| Empty State Value | Basic text | Actionable guidance | User engagement |
| Test Coverage | ~20% | 70%+ | Code reliability |

### Qualitative Goals

- **Editor feels powerful:** Users can format quickly without context-switching
- **Preview feels alive:** Interactive elements respond to taps
- **Organization feels natural:** Folder hierarchy is intuitive
- **Performance feels snappy:** No lag with 1000+ notes
- **App feels polished:** Smooth animations, clear feedback

---

## Future Enhancements (Phase 6+)

### Post-2.0 Roadmap

**Phase 6: Advanced Markdown (2-3 weeks)**

- LaTeX math rendering (KaTeX)
- Mermaid diagram support
- Custom CSS themes for preview
- Table editor UI

**Phase 7: Attachments & Media (3-4 weeks)**

- Image attachments
- Audio recordings
- File attachments (PDF, etc.)
- Gallery view

**Phase 8: Power User Features (2-3 weeks)**

- Note templates
- Keyboard shortcuts overlay
- Vim mode (optional)
- Split-screen editing

**Phase 9: Widgets & Shortcuts (1-2 weeks)**

- Home screen widget
- Quick note tile
- Share to Obbi

**Phase 10: Sync & Backup (4-5 weeks)**

- Local network sync (no cloud!)
- Encrypted backup/restore
- WebDAV support (optional)
- Git-based sync (optional)

---

## Conclusion

This comprehensive upgrade plan transforms Obbi from a solid note-taking app into a **premium,
Obsidian-class knowledge base** while maintaining its core values:

✅ **100% offline** - No network dependencies  
✅ **Privacy-first** - Encryption and local storage  
✅ **Additive enhancement** - No feature removal  
✅ **Material3-native** - Modern Android design  
✅ **Performance-conscious** - Scales to 1000+ notes

The 8-week timeline is aggressive but achievable with focused development. Each phase delivers
tangible user value and can be shipped incrementally for early feedback.

**Next Steps:**

1. Review and approve this plan
2. Set up project board (GitHub Projects or Trello)
3. Begin Week 1 implementation (database migration)
4. Schedule weekly progress reviews
5. Prepare test devices and accounts

Let's build something amazing! 🚀

---

**Document Version:** 1.0  
**Last Updated:** 2025  
**Status:** Ready for Implementation
