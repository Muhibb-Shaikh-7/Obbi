# Troubleshooting Guide

## App Crashes When Opening a Note

**Fixed in latest version!** If you're experiencing crashes:

### Symptoms

- App crashes immediately when tapping on a note
- Error related to `AndroidView`, `TextView`, or nested scrolling

### Root Cause

- Nested scrolling conflict between `LazyColumn` and `verticalScroll` modifiers
- Markdown rendering issues

### Solution

The code has been updated to:

1. Remove `verticalScroll` from the AndroidView inside LazyColumn
2. Wrap the preview in a separate scrollable Column for edit mode
3. Add proper layout params to the TextView
4. Add try-catch for markdown parsing errors

**If you still see crashes:**

1. Check Logcat for the exact error
2. Look for messages with tag `NoteDetailScreen`
3. Try clearing app data and rebuilding

---

## Notes Not Being Created

If notes aren't appearing after you create them, follow these steps:

### 1. Check Logcat

Open **Logcat** in Android Studio and filter by:

- Tag: `NotesViewModel` or `NoteRepository`
- Log level: Debug and Error

Look for these messages:

```
D/NotesViewModel: createNote called with title: [your note title]
D/NoteRepository: Inserting note: [your note title]
D/NoteRepository: Note inserted with ID: [number]
D/NotesViewModel: Note created successfully with ID: [number]
```

**If you don't see these logs:**

- The dialog isn't calling `onCreate`
- Check that you're entering a title and clicking "Create"

**If you see error logs:**

- Look at the error message and stack trace
- Common issues are listed below

---

### 2. Common Issues & Solutions

#### Issue: "Cannot access database on the main thread"

**Solution:** This shouldn't happen with the current code, but if it does:

```kotlin
// The code already uses viewModelScope.launch { }
// which runs on a background thread
```

#### Issue: Database not initialized

**Error:** `IllegalStateException: Cannot access database`

**Solution:** Check that `MainActivity` properly initializes the database:

```kotlin
// In MainActivity.onCreate()
val database = ObbyDatabase.getDatabase(applicationContext)
val repository = NoteRepository(...)
```

#### Issue: Foreign key constraint failed

**Error:** `SQLiteConstraintException: FOREIGN KEY constraint failed`

**Cause:** Trying to assign a note to a non-existent folder

**Solution:** Either:

1. Don't set `folderId` when creating the note
2. Create the folder first, then assign notes to it

#### Issue: Notes created but not visible

**Possible causes:**

1. **Filter active:** Check if you have a search query, folder, or tag filter active
2. **Flow not collected:** The UI might not be observing the StateFlow

**Solution:**

```kotlin
// In NotesListScreen, verify:
val notes by viewModel.notes.collectAsState()

// Check current filters:
val searchQuery by viewModel.searchQuery.collectAsState()
Log.d("Debug", "Search query: $searchQuery")
Log.d("Debug", "Notes count: ${notes.size}")
```

---

### 3. Manual Database Inspection

**Using Database Inspector:**

1. Run the app in debug mode
2. View → Tool Windows → App Inspection
3. Select "Database Inspector"
4. Expand "obby_database"
5. Click on "notes" table
6. Verify if notes are being inserted

**Expected data:**

- `id`: Auto-generated number
- `title`: Your note title
- `content`: Empty or your content
- `createdAt`, `modifiedAt`: Timestamps
- `folderId`: NULL or folder ID

---

### 4. Debug Flow Collection

Add this to `NotesListScreen` to debug:

```kotlin
LaunchedEffect(notes) {
    Log.d("NotesListScreen", "Notes updated: ${notes.size} notes")
    notes.forEach { note ->
        Log.d("NotesListScreen", "- Note: ${note.title} (ID: ${note.id})")
    }
}
```

---

### 5. Test Note Creation Programmatically

Add this to `MainActivity.onCreate()` after repository initialization:

```kotlin
// Test note creation
lifecycleScope.launch {
    try {
        val noteId = repository.insertNote(
            Note(title = "Test Note", content = "This is a test")
        )
        Log.d("MainActivity", "Test note created with ID: $noteId")
    } catch (e: Exception) {
        Log.e("MainActivity", "Failed to create test note", e)
    }
}
```

Don't forget to import: `androidx.lifecycle.lifecycleScope`

---

### 6. Clear App Data

If all else fails, clear the app data:

**Option A: Via Android Studio**

1. Run → Stop app
2. Tools → Device Manager
3. Find your app → Right-click → "Uninstall"
4. Run app again

**Option B: On Device**

1. Settings → Apps → Obby
2. Storage → Clear Data
3. Relaunch app

---

## Other Common Issues

### Graph View is Empty

**Symptoms:** Graph screen shows "No connections yet"

**Causes:**

1. No notes with links exist
2. Links syntax incorrect

**Solution:**

1. Create at least 2 notes
2. In one note, add: `[[Other Note Title]]`
3. Save the note
4. Check graph view

---

### Search Not Working

**Symptoms:** Search returns no results even though notes exist

**Possible causes:**

1. Notes are encrypted (encrypted notes can't be searched)
2. Query is case-sensitive (it shouldn't be)

**Solution:**

```kotlin
// Verify in NoteDao.kt:
@Query("""
    SELECT * FROM notes 
    WHERE title LIKE '%' || :query || '%' 
    OR content LIKE '%' || :query || '%'
    ORDER BY modifiedAt DESC
""")
fun searchNotes(query: String): Flow<List<Note>>
```

---

### Tags Not Showing

**Symptoms:** Adding `#tag` doesn't create tags

**Solution:**

1. Save the note (tags extracted on save)
2. Check that tag syntax is correct: `#tagname` (no spaces)
3. Open drawer to verify tags appear

---

### Backlinks Not Showing

**Symptoms:** Links work but backlinks section is empty

**Debug:**

```kotlin
// Check in NoteDetailScreen
LaunchedEffect(backlinks) {
    Log.d("NoteDetail", "Backlinks count: ${backlinks.size}")
}
```

**Verify:**

- Note A has `[[Note B]]`
- Note B should show Note A in backlinks

---

### App Crashes on Startup

**Common causes:**

1. **Room database migration issue**
    - Clear app data
    - Or add migration in `ObbyDatabase`

2. **Dependency conflict**
    - Check Gradle sync completed
    - Run: `./gradlew clean build`

3. **Missing permissions** (shouldn't happen, but check)
   ```xml
   <!-- AndroidManifest.xml should NOT have INTERNET permission -->
   ```

---

## Getting More Help

1. **Check Logcat** - Most issues show clear error messages
2. **Enable verbose logging** - Set log level to Verbose
3. **Test incrementally** - Create one note, verify it appears, then continue
4. **Check Database Inspector** - Verify data is actually being saved

---

## Reporting Bugs

When reporting issues, include:

1. **Logcat output** (with tags: NotesViewModel, NoteRepository)
2. **Steps to reproduce**
3. **Expected vs actual behavior**
4. **Device/emulator info** (API level, device name)
5. **App version** (check `versionName` in `build.gradle.kts`)

---

## Debug Checklist

- [ ] Gradle sync successful
- [ ] App builds without errors
- [ ] Logcat shows note creation logs
- [ ] Database Inspector shows notes table
- [ ] No search/filter active
- [ ] Flow is being collected (`collectAsState()`)
- [ ] No exceptions in Logcat
- [ ] Database file exists (check via Device File Explorer)

---

**If you've tried all these steps and still have issues, create a GitHub issue with the debug
information!**
