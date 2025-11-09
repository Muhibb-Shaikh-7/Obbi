# Implementation Summary: "Hidden" to "Private" Refactoring

## Overview

This document tracks the implementation of renaming "Hidden Notes" to "Private Notes" and adding
password-based security with improved Markdown editor UX.

## ✅ Completed Tasks

### 1. Strings and Resources

- **File**: `app/src/main/res/values/strings.xml`
- Renamed all "Hidden" references to "Private"
- Added new password-related strings
- Maintained backward compatibility aliases
- Added migration strings

### 2. Database Schema

- **File**: `app/src/main/java/com/example/obby/data/local/ObbyDatabase.kt`
- Added `MIGRATION_2_3` to add `isPrivate` column
- Updated database version from 2 to 3
- Preserved `isHidden` column for backward compatibility

### 3. Data Model

- **File**: `app/src/main/java/com/example/obby/data/local/entity/Note.kt`
- Added `isPrivate` property
- Deprecated `isHidden` property with replacement annotation
- Added `isActuallyPrivate` convenience property for transition period
- Updated `equals()` and `hashCode()` methods

### 4. Repository Layer

- **File**: `app/src/main/java/com/example/obby/data/repository/NoteRepository.kt`
- Renamed `getAllNotesExcludingHidden()` → `getAllNotesExcludingPrivate()`
- Renamed `getHiddenNotes()` → `getPrivateNotes()`
- Renamed `toggleHideNote()` → `togglePrivateNote()`
- Renamed `hideNotes()` → `markNotesAsPrivate()`
- Renamed `unhideNotes()` → `unmarkNotesAsPrivate()`
- Updated all filter logic to use `isActuallyPrivate`

### 5. State Management

- **File**: `app/src/main/java/com/example/obby/data/repository/HiddenFolderState.kt`
- Renamed `HiddenFolderState` → `PrivateFolderState`
- Added backward compatibility alias
- Updated all documentation

### 6. Private Folder Manager

- **File**: `app/src/main/java/com/example/obby/data/repository/HiddenFolderManager.kt`
- Renamed `HiddenFolderManager` → `PrivateFolderManager`
- Added `removePassword()` method
- Updated all state transitions to use `PrivateFolderState`
- Added backward compatibility alias

### 7. Settings UI

- **File**: `app/src/main/java/com/example/obby/ui/screens/SettingsScreen.kt`
- Added "Privacy" section at the top of settings
- Implemented "Set Private Password" option (when no password exists)
- Implemented "Change Private Password" option (when password exists)
- Implemented "Remove Private Password" option with confirmation dialog
- Integrated `PrivateFolderManager` for password operations

### 8. UI Components - Dialogs

- **File**: `app/src/main/java/com/example/obby/ui/components/HiddenFolderDialogs.kt`
- Updated `CreatePasswordDialog` title: "Create Private Password"
- Updated `UnlockHiddenFolderDialog` title: "Unlock Private"
- Updated all dialog messages to use "private" terminology

### 9. Markdown Editor Enhancement

- **File**: `app/src/main/java/com/example/obby/ui/components/MarkdownToolbar.kt`
- Added `handleEnterKey()` method to `MarkdownFormatter`
- Implemented auto-continuation for:
    - Bullet lists (`- ` and `* `)
    - Numbered lists (auto-increments number)
    - Checkboxes (`- [ ]` and `- [x]`)
- Implemented exit-list behavior (Enter on empty list item removes prefix)
- Maintains indentation levels

## ⚠️ Pending Tasks

### 1. Integrate Enter Key Handling in NoteDetailScreen

- **File**: `app/src/main/java/com/example/obby/ui/screens/NoteDetailScreen.kt`
- **Task**: Connect `MarkdownFormatter.handleEnterKey()` to the text field
- **Challenge**: Compose BasicTextField doesn't have direct keyboard event interception
- **Solutions**:
    - Option A: Use `KeyboardActions` with custom IME action
    - Option B: Monitor text changes and detect "\n" insertion, then apply transformation
    - Option C: Use `Modifier.onKeyEvent()` (may not work with soft keyboard)

### 2. Increase Checkbox Size in Preview Mode

- **File**: `app/src/main/java/com/example/obby/ui/screens/NoteDetailScreen.kt` (MarkdownPreview
  function)
- **Task**: Modify the TaskListPlugin configuration to increase checkbox rendering size
- **Requirements**:
    - Minimum 48dp touch target
    - Scale checkbox drawable
    - Maintain proper alignment with text

### 3. Update NotesListScreen

- **File**: `app/src/main/java/com/example/obby/ui/screens/NotesListScreen.kt`
- **Tasks**:
    - Update "Hidden Folder" → "Private" in drawer
    - Update context menu "Hide"/"Unhide" → "Mark as Private"/"Remove from Private"
    - Update icon from `Icons.Default.VisibilityOff` to `Icons.Default.Lock` where appropriate
    - Update `onHiddenNotesClick` → `onPrivateNotesClick` callback names

### 4. Rename and Update HiddenNotesScreen

- **File**: `app/src/main/java/com/example/obby/ui/screens/HiddenNotesScreen.kt`
- **Tasks**:
    - Rename file to `PrivateNotesScreen.kt`
    - Rename function `HiddenNotesScreen` → `PrivateNotesScreen`
    - Update all UI text: "Hidden Folder" → "Private"
    - Replace `HiddenFolderManager` with `PrivateFolderManager`
    - Update state checks

### 5. Update Navigation

- **File**: `app/src/main/java/com/example/obby/ui/navigation/NavGraph.kt`
- **Tasks**:
    - Rename `Screen.HiddenNotes` → `Screen.PrivateNotes`
    - Update route from "hidden_notes" → "private_notes"
    - Update `HiddenNotesScreen` → `PrivateNotesScreen` composable call

### 6. Update NotesViewModel

- **File**: `app/src/main/java/com/example/obby/ui/viewmodel/NotesViewModel.kt`
- **Tasks**:
    - Update filter logic to use `isActuallyPrivate`
    - Rename methods: `hideSelectedNotes()` → `markSelectedNotesAsPrivate()`
    - Rename methods: `unhideSelectedNotes()` → `unmarkSelectedNotesAsPrivate()`
    - Update `toggleHideNote()` → `togglePrivateNote()`

### 7. Emoji PIN Migration

- **File**: Create `app/src/main/java/com/example/obby/util/MigrationHelper.kt`
- **Tasks**:
    - Detect if old emoji PIN system (`HiddenNotesManager`) was configured
    - Show one-time migration dialog on app start
    - Prompt user to set new password
    - Clear old emoji PIN data after migration
    - **Integration point**: `MainActivity` or splash screen

### 8. Widget Updates

- **Files**: `app/src/main/java/com/example/obby/ui/widget/*`
- **Tasks**:
    - Ensure widgets don't leak private note content when locked
    - Update any "Hidden" references to "Private" in widget code

### 9. Testing

- **Files**: `app/src/test/java/com/example/obby/*`, `app/src/androidTest/java/com/example/obby/*`
- **Tasks**:
    - Update `HiddenNotesTest.kt` → `PrivateNotesTest.kt`
    - Update test cases to use `isPrivate` instead of `isHidden`
    - Add tests for password set/change/remove flows
    - Add tests for Markdown auto-continuation
    - Add tests for checkbox size/tap target
    - Test migration from emoji PIN

### 10. Documentation Updates

- **Files**: `README.md`, `SECURITY.md`, `MARKDOWN_GUIDE.md`, etc.
- **Tasks**:
    - Replace "Hidden Notes" with "Private Notes" throughout docs
    - Update security section with password-based authentication details
    - Document Markdown auto-continuation feature
    - Update user guide and screenshots

## 🔧 Technical Notes

### Backward Compatibility Strategy

- The `isHidden` column remains in the database during transition
- `isActuallyPrivate` property checks both `isHidden` and `isPrivate`
- When updating notes, always set `isHidden = false` and use `isPrivate` going forward
- Future migration (v4) can drop the `isHidden` column after transition period

### Password Security Implementation

- Uses `PasswordRepository` with PBKDF2-HMAC-SHA256
- Salt is generated per device
- Password hash and salt stored in `SecurePreferences` (encrypted)
- Lockout policy: 5 failed attempts = 2 minute lockout with exponential backoff
- Recovery phrase option available (BIP39-like word list)

### Markdown Auto-Continuation Logic

- Implemented in `MarkdownFormatter.handleEnterKey()`
- Patterns supported:
    - `- ` or `* ` → continues bullet list
    - `1. ` → continues numbered list with incremented number
    - `- [ ] ` or `- [x] ` → continues checkbox list with unchecked box
- Empty list item (just the prefix) → exits list and removes prefix
- Maintains indentation (spaces/tabs)

## 🎯 Next Steps Priority

1. **HIGH**: Integrate Enter key handling in NoteDetailScreen (critical UX feature)
2. **HIGH**: Update NotesListScreen UI terminology and callbacks
3. **HIGH**: Rename HiddenNotesScreen to PrivateNotesScreen
4. **MEDIUM**: Update Navigation routes
5. **MEDIUM**: Implement emoji PIN migration flow
6. **MEDIUM**: Increase checkbox size in preview
7. **LOW**: Update tests
8. **LOW**: Update documentation

## 📝 Known Issues

1. **Enter key handling**: Compose BasicTextField doesn't expose direct keyboard events - need
   workaround
2. **Checkbox size**: Markwon library uses Android TextView - need custom span drawable sizing
3. **Migration timing**: Need to decide if migration prompt shows on splash screen or first private
   notes access

## 🔍 Files Requiring Attention

Priority files to update next:

1. `app/src/main/java/com/example/obby/ui/screens/NoteDetailScreen.kt`
2. `app/src/main/java/com/example/obby/ui/screens/NotesListScreen.kt`
3. `app/src/main/java/com/example/obby/ui/screens/HiddenNotesScreen.kt` (rename to
   PrivateNotesScreen.kt)
4. `app/src/main/java/com/example/obby/ui/navigation/NavGraph.kt`
5. `app/src/main/java/com/example/obby/ui/viewmodel/NotesViewModel.kt`

---

**Last Updated**: January 2025
**Status**: ~60% Complete
**Estimated Completion**: Requires 4-6 more hours of focused development
