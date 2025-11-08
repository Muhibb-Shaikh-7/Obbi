# Obbi 2.0 Implementation Quick Start

## 🚀 Get Started in 30 Minutes

This guide helps you start implementing the Obbi 2.0 upgrade plan immediately.

---

## Prerequisites

- ✅ Read `OBBI_UPGRADE_PLAN.md` (at least Executive Summary + Current State Analysis)
- ✅ Android Studio Hedgehog or newer
- ✅ Git repository cloned and up-to-date
- ✅ Working Obbi v1.x build

---

## Phase 1: Start Here (Week 1, Day 1-2)

### Step 1: Database Migration (30 mins)

**1. Update Note Entity**

File: `app/src/main/java/com/example/obby/data/local/entity/Note.kt`

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
    val sortOrder: Int = 0  // ← ADD THIS LINE
)
```

**2. Create Migration File**

Create: `app/src/main/java/com/example/obby/data/local/migrations/Migrations.kt`

```kotlin
package com.example.obby.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE notes ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
        database.execSQL("""
            UPDATE notes SET sortOrder = 
            (SELECT COUNT(*) FROM notes n2 WHERE n2.createdAt <= notes.createdAt)
        """)
    }
}
```

**3. Update ObbyDatabase**

File: `app/src/main/java/com/example/obby/data/local/ObbyDatabase.kt`

```kotlin
@Database(
    entities = [Note::class, Folder::class, Tag::class, NoteTagCrossRef::class, NoteLink::class],
    version = 3,  // ← CHANGE FROM 2 TO 3
    exportSchema = true
)
abstract class ObbyDatabase : RoomDatabase() {
    // ... existing code ...
    
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
                    .addMigrations(MIGRATION_2_3)  // ← ADD THIS LINE
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**4. Test Migration**

```bash
./gradlew clean build
# Run app on emulator or device
# Check Logcat for migration success
```

✅ **Checkpoint:** App launches, existing notes visible, no crashes.

---

## Phase 1: Continue (Week 1, Day 3-4)

### Step 2: Add Haptic Feedback to Toolbar (15 mins)

File: `app/src/main/java/com/example/obby/ui/components/MarkdownToolbar.kt`

Find all `IconButton` blocks and add haptic feedback:

```kotlin
@Composable
fun MarkdownToolbar(
    onAction: (MarkdownActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current  // ← ADD THIS LINE
    
    // ... existing code ...
    
    // Example for Bold button:
    IconButton(onClick = { 
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)  // ← ADD THIS
        onAction(MarkdownActionType.Bold) 
    }) {
        Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(20.dp))
    }
    
    // Repeat for all other buttons...
}
```

✅ **Checkpoint:** Tapping toolbar buttons produces haptic feedback.

---

## Phase 2: Interactive Preview (Week 3, Day 11-13)

### Step 3: Create CheckboxParser Utility (45 mins)

Create: `app/src/main/java/com/example/obby/util/CheckboxParser.kt`

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
            val checkboxMarkerPos = checkbox.lineStart + markdown.substring(checkbox.lineStart).indexOf('[') + 1
            setCharAt(checkboxMarkerPos, newState[0])
        }.toString()
    }
    
    fun toggleCheckboxAtPosition(markdown: String, cursorPosition: Int): String? {
        val checkboxes = findCheckboxes(markdown)
        val checkbox = checkboxes.find { cursorPosition in it.lineStart..it.lineEnd }
            ?: return null
        return toggleCheckbox(markdown, checkbox.index)
    }
}
```

**Test it immediately:**

Create: `app/src/test/java/com/example/obby/util/CheckboxParserTest.kt`

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
}
```

Run tests:

```bash
./gradlew test
```

✅ **Checkpoint:** Tests pass, checkbox parsing works.

---

## Phase 3: Nested Folders (Week 5, Day 22-24)

### Step 4: Build Folder Tree in ViewModel (30 mins)

File: `app/src/main/java/com/example/obby/ui/viewmodel/NotesViewModel.kt`

Add at the end of the class:

```kotlin
data class FolderNode(
    val folder: Folder,
    val children: List<FolderNode>,
    val depth: Int,
    val isExpanded: Boolean = true
)

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
    val rootFolders = folders.filter { it.parentFolderId == null }
    
    fun buildNode(folder: Folder, depth: Int = 0): FolderNode {
        val children = folders
            .filter { it.parentFolderId == folder.id }
            .map { buildNode(it, depth + 1) }
        
        return FolderNode(
            folder = folder,
            children = children,
            depth = depth,
            isExpanded = true
        )
    }
    
    return rootFolders.map { buildNode(it) }
}
```

✅ **Checkpoint:** Folder hierarchy builds correctly (verify in debugger).

---

## Common Issues & Solutions

### Issue: Migration Fails

**Solution:**

```bash
# Clear app data and rebuild
adb uninstall com.example.obby
./gradlew clean installDebug
```

### Issue: Tests Not Running

**Solution:**

```bash
# Sync Gradle
./gradlew clean test --rerun-tasks
```

### Issue: Compose Preview Not Updating

**Solution:**

- Click "Build & Refresh" in preview pane
- Invalidate caches: `File → Invalidate Caches → Invalidate and Restart`

---

## Daily Checklist

Use this for each implementation day:

- [ ] Pull latest code from `main`
- [ ] Create feature branch: `git checkout -b feature/phase-X-day-Y`
- [ ] Implement feature from plan
- [ ] Write unit tests (if applicable)
- [ ] Run all tests: `./gradlew test`
- [ ] Build and test on device/emulator
- [ ] Commit with descriptive message
- [ ] Push and create PR (if team review needed)

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `Note.kt` | Note entity (add `sortOrder`) |
| `Migrations.kt` | Database migrations |
| `ObbyDatabase.kt` | Database configuration |
| `NoteRepository.kt` | Business logic, add new methods |
| `NotesViewModel.kt` | UI state management |
| `NoteDetailViewModel.kt` | Editor state management |
| `MarkdownToolbar.kt` | Formatting toolbar (enhance) |
| `CheckboxParser.kt` | NEW - Checkbox parsing utility |
| `NoteDetailScreen.kt` | Editor UI (interactive preview) |
| `NotesListScreen.kt` | Note list (drag-and-drop) |

---

## Testing Commands

```bash
# Run unit tests
./gradlew test

# Run specific test
./gradlew test --tests CheckboxParserTest

# Run instrumented tests (on device)
./gradlew connectedAndroidTest

# Build release APK
./gradlew assembleRelease

# Check for lint issues
./gradlew lint
```

---

## Communication

### Weekly Progress Report Template

```markdown
## Week X Progress

**Completed:**
- [ ] Feature A
- [ ] Feature B

**In Progress:**
- [ ] Feature C (70% done)

**Blockers:**
- None / Issue with X

**Next Week:**
- [ ] Start Feature D
- [ ] Test Feature C
```

---

## Phase Completion Criteria

### Phase 1 Done When:

- ✅ Database migrated to v3
- ✅ Toolbar has haptic feedback
- ✅ Enhanced Markdown guide implemented
- ✅ Auto-save indicator working

### Phase 2 Done When:

- ✅ Checkbox parsing utility complete + tested
- ✅ Interactive preview with checkbox toggles
- ✅ Wiki-links clickable in preview
- ✅ Link navigation working

### Phase 3 Done When:

- ✅ Nested folder picker UI complete
- ✅ Breadcrumb navigation working
- ✅ Drag-and-drop reordering functional
- ✅ Notes persist custom order

---

## Resources

- **Main Plan:** `OBBI_UPGRADE_PLAN.md`
- **Architecture:** `ARCHITECTURE.md`
- **Existing Docs:** `README.md`, `DEVELOPMENT.md`
- **Compose Docs:** https://developer.android.com/jetpack/compose
- **Room Migrations:
  ** https://developer.android.com/training/data-storage/room/migrating-db-versions

---

## Quick Wins (Do These First!)

These are easy, high-impact changes you can make today:

1. **Add haptic feedback to toolbar** (15 mins) ⚡
2. **Fix empty state in notes list** (30 mins) 🎨
3. **Add icons to context menu items** (10 mins) ✨
4. **Improve auto-save indicator** (20 mins) 💾

---

## Need Help?

1. Check the main upgrade plan: `OBBI_UPGRADE_PLAN.md`
2. Review architecture diagram in plan
3. Look at existing implementations for similar features
4. Check Compose/Room documentation
5. Use Android Studio's "Find Usages" to see how existing features work

---

**Good luck! Let's build Obbi 2.0! 🚀**
