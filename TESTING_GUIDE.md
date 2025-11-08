# Testing Guide for Obby App

## 🧪 Quick Testing Checklist

### Part 1: Toolbar Position ✅

#### Test 1: Toolbar Visibility

1. Open the app
2. Create a new note or open existing note
3. Tap Edit button (pencil icon)
4. **Expected:** Markdown toolbar appears at the TOP of the screen, below the title bar
5. **Expected:** All formatting buttons visible and scrollable

#### Test 2: Toolbar Functionality

1. In edit mode, tap each toolbar button:
    - **[B]** Bold → Should wrap text in `**text**`
    - **[I]** Italic → Should wrap text in `*text*`
    - **[S]** Strikethrough → Should wrap text in `~~text~~`
    - **[H▼]** Heading dropdown → Should show 6 heading levels
    - **[•]** Bullet list → Should add `- ` prefix
    - **[1.]** Numbered list → Should add `1. ` prefix
    - **[☑]** Checklist → Should add `- [ ] ` prefix
    - **["]** Quote → Should add `> ` prefix
    - **[`]** Inline code → Should wrap in backticks
    - **[</>]** Code block → Should wrap in triple backticks
    - **[🔗]** Link → Should create `[text](url)` format
    - **[⊞]** Table → Should insert table template
    - **[─]** Divider → Should insert `---`
    - **[?]** Help → Should open cheat sheet dialog
2. **Expected:** All buttons work correctly

#### Test 3: Keyboard Interaction

1. In edit mode, tap in the text editor
2. On-screen keyboard should appear
3. **Expected:** Toolbar remains visible at the top (doesn't hide)
4. **Expected:** Editor content scrolls behind the toolbar
5. Type some text
6. **Expected:** Text appears normally, no overlap with toolbar

---

### Part 2: Backup Functionality 💾

#### Test 4: Access Settings

1. Open the app (notes list screen)
2. Tap hamburger menu icon (top-left)
3. Scroll down in the drawer
4. **Expected:** See "Settings" section at the bottom
5. Tap "Settings"
6. **Expected:** Settings screen opens with "Backup & Restore" section

#### Test 5: Create Backup

1. In Settings, tap "Backup Notes"
2. **Expected:** System folder picker dialog opens
3. Navigate to a folder (e.g., Downloads)
4. Tap "Use This Folder" or equivalent
5. **Expected:** Loading indicator shows "Creating backup..."
6. Wait for completion
7. **Expected:** Success message: "Backup saved successfully to obby_backup_[timestamp].zip"
8. Navigate to the selected folder using file manager
9. **Expected:** ZIP file exists with correct timestamp

#### Test 6: Verify Backup Contents

1. Find the backup ZIP file
2. Extract it (using file manager or computer)
3. **Expected:** See two folders:
    - `notes/` containing `.md` files (one per note)
    - `database/` containing `obby_database.db` and related files
4. Open one markdown file
5. **Expected:** Should contain metadata header and note content

#### Test 7: Backup with No Notes

1. Delete all notes from the app
2. Go to Settings → Backup Notes
3. Create a backup
4. **Expected:** Backup succeeds but `notes/` folder is empty
5. **Expected:** Database files still present in `database/` folder

#### Test 8: Backup Error Handling

1. Try to backup to a read-only location (if possible)
2. **Expected:** Error message: "Cannot write to selected location"
3. Cancel the folder picker
4. **Expected:** No error, returns to settings

---

### Part 3: Restore Functionality 🔄

#### Test 9: Restore from Backup

1. Create some test notes (e.g., "Test Note 1", "Test Note 2")
2. Create a backup
3. Delete one of the test notes
4. Go to Settings → Restore Backup
5. **Expected:** File picker opens
6. Select the backup ZIP file
7. **Expected:** Confirmation dialog appears with warning
8. Tap "Restore"
9. **Expected:** Loading indicator shows "Restoring backup..."
10. **Expected:** App restarts automatically
11. Check notes list
12. **Expected:** Deleted note is restored

#### Test 10: Restore Confirmation Dialog

1. Go to Settings → Restore Backup
2. Select a backup file
3. **Expected:** Dialog shows:
    - Title: "Restore Backup?"
    - Warning text about data replacement
    - Two buttons: "Cancel" and "Restore"
4. Tap "Cancel"
5. **Expected:** Dialog dismisses, no changes made

#### Test 11: Restore Invalid File

1. Create a text file (not a ZIP) named `fake_backup.zip`
2. Try to restore it
3. **Expected:** Error message: "Failed to restore backup: [error details]"
4. **Expected:** App remains stable, no crash

#### Test 12: Restore Old Backup

1. Create backup with some notes
2. Add more notes
3. Restore the old backup
4. **Expected:** App reverts to the state when backup was created
5. **Expected:** New notes (after backup) are gone
6. **Expected:** Old notes are present

---

### Part 4: Edge Cases & Error Handling 🔍

#### Test 13: Multiple Rapid Backups

1. Tap "Backup Notes"
2. Select folder
3. Immediately tap "Backup Notes" again while first backup is processing
4. **Expected:** Second backup button is disabled (grayed out)
5. **Expected:** Cannot start second backup until first completes

#### Test 14: Large Backup (Performance)

1. Create 100+ notes with long content
2. Create a backup
3. **Expected:** Backup completes without crash
4. **Expected:** Loading indicator shows progress
5. Check backup file size
6. **Expected:** Reasonable size based on content

#### Test 15: Special Characters in Note Titles

1. Create notes with titles containing:
    - `Test/Note` (slash)
    - `Test\Note` (backslash)
    - `Test:Note` (colon)
    - `Test|Note` (pipe)
2. Create backup
3. **Expected:** Backup succeeds
4. Open ZIP file
5. **Expected:** Filenames are sanitized (special chars replaced with underscores)

#### Test 16: Encrypted Notes

1. Create a note
2. Encrypt it with a password
3. Create backup
4. Restore backup
5. **Expected:** Note is still encrypted after restore
6. **Expected:** Can decrypt with same password

#### Test 17: Network Interruption (Should Not Matter)

1. Enable airplane mode
2. Create backup
3. **Expected:** Backup works normally (it's all local)
4. Restore backup
5. **Expected:** Restore works normally

#### Test 18: Low Storage Space

1. Create backup with device nearly full
2. **Expected:** Either succeeds or shows clear error
3. **Expected:** App doesn't crash

---

### Part 5: UI/UX Testing 🎨

#### Test 19: Material 3 Design Consistency

1. Check all new screens
2. **Expected:** Colors match app theme
3. **Expected:** Typography is consistent
4. **Expected:** Icons follow Material Design guidelines
5. **Expected:** Touch targets are at least 48dp

#### Test 20: Dark Mode (If Supported)

1. Switch device to dark mode
2. Open Settings
3. **Expected:** Settings screen uses dark theme
4. Open editor
5. **Expected:** Toolbar uses dark theme
6. Create backup/restore
7. **Expected:** All dialogs use dark theme

#### Test 21: Orientation Change

1. Open Settings
2. Rotate device
3. **Expected:** Layout adjusts correctly
4. Start backup, rotate during process
5. **Expected:** Operation continues, UI adapts

#### Test 22: Accessibility

1. Enable TalkBack
2. Navigate to Settings
3. **Expected:** All buttons have content descriptions
4. **Expected:** Can navigate with TalkBack gestures
5. **Expected:** Announces backup progress

---

### Part 6: Integration Testing 🔗

#### Test 23: Backup → Restore → Edit

1. Create note "Original Note"
2. Backup
3. Edit note to "Modified Note"
4. Restore backup
5. **Expected:** Note content is "Original Note"
6. Edit note again
7. **Expected:** Can edit normally

#### Test 24: Multiple Devices (Same Backup)

1. Create backup on Device A
2. Transfer ZIP file to Device B
3. Install Obby on Device B
4. Restore backup on Device B
5. **Expected:** All notes appear on Device B
6. **Expected:** All metadata preserved (folders, tags, etc.)

#### Test 25: Folder Structure Preservation

1. Create folder "Work"
2. Create notes in "Work" folder
3. Create backup
4. Delete folder
5. Restore backup
6. **Expected:** "Work" folder restored
7. **Expected:** Notes are in correct folder

#### Test 26: Tag Preservation

1. Create note with tags (#important, #work)
2. Create backup
3. Remove tags
4. Restore backup
5. **Expected:** Tags restored correctly

---

## 📊 Test Results Template

```
╔══════════════════════════════════════════════════════════╗
║  OBBY APP - TEST RESULTS                                 ║
╠══════════════════════════════════════════════════════════╣
║  Date: _____________                                     ║
║  Tester: ___________                                     ║
║  Device: ___________                                     ║
║  Android Version: ___                                    ║
╠══════════════════════════════════════════════════════════╣
║  TOOLBAR TESTS                                           ║
║  ☐ Test 1: Toolbar Visibility                            ║
║  ☐ Test 2: Toolbar Functionality                         ║
║  ☐ Test 3: Keyboard Interaction                          ║
╠══════════════════════════════════════════════════════════╣
║  BACKUP TESTS                                            ║
║  ☐ Test 4: Access Settings                               ║
║  ☐ Test 5: Create Backup                                 ║
║  ☐ Test 6: Verify Backup Contents                        ║
║  ☐ Test 7: Backup with No Notes                          ║
║  ☐ Test 8: Backup Error Handling                         ║
╠══════════════════════════════════════════════════════════╣
║  RESTORE TESTS                                           ║
║  ☐ Test 9: Restore from Backup                           ║
║  ☐ Test 10: Restore Confirmation Dialog                  ║
║  ☐ Test 11: Restore Invalid File                         ║
║  ☐ Test 12: Restore Old Backup                           ║
╠══════════════════════════════════════════════════════════╣
║  EDGE CASES                                              ║
║  ☐ Test 13: Multiple Rapid Backups                       ║
║  ☐ Test 14: Large Backup                                 ║
║  ☐ Test 15: Special Characters                           ║
║  ☐ Test 16: Encrypted Notes                              ║
║  ☐ Test 17: Network Interruption                         ║
║  ☐ Test 18: Low Storage Space                            ║
╠══════════════════════════════════════════════════════════╣
║  UI/UX TESTS                                             ║
║  ☐ Test 19: Material 3 Design                            ║
║  ☐ Test 20: Dark Mode                                    ║
║  ☐ Test 21: Orientation Change                           ║
║  ☐ Test 22: Accessibility                                ║
╠══════════════════════════════════════════════════════════╣
║  INTEGRATION TESTS                                       ║
║  ☐ Test 23: Backup → Restore → Edit                      ║
║  ☐ Test 24: Multiple Devices                             ║
║  ☐ Test 25: Folder Structure                             ║
║  ☐ Test 26: Tag Preservation                             ║
╠══════════════════════════════════════════════════════════╣
║  OVERALL RESULT:  ☐ PASS  ☐ FAIL                         ║
║                                                          ║
║  Notes: ____________________________________________     ║
║  ____________________________________________________     ║
║  ____________________________________________________     ║
╚══════════════════════════════════════════════════════════╝
```

---

## 🚀 Quick Smoke Test (5 Minutes)

For rapid verification, run these critical tests:

1. ✅ Open editor → Toolbar at top
2. ✅ Tap Bold button → Text wraps correctly
3. ✅ Open Settings from drawer
4. ✅ Create backup → Success message
5. ✅ Restore backup → Confirmation dialog → App restarts
6. ✅ Check notes list → All notes present

If all 6 pass → **Core functionality working!** ✅

---

## 🐛 Bug Reporting Template

If you find a bug, please report using this format:

```markdown
### Bug Report

**Title:** [Short description]

**Steps to Reproduce:**
1. 
2. 
3. 

**Expected Behavior:**
[What should happen]

**Actual Behavior:**
[What actually happens]

**Device Info:**
- Device Model: 
- Android Version: 
- App Version: 

**Screenshots/Logs:**
[Attach if available]

**Severity:** [ ] Critical / [ ] High / [ ] Medium / [ ] Low
```

---

## ✅ Sign-Off Criteria

The implementation is ready for production when:

- [ ] All 26 tests pass
- [ ] No critical or high severity bugs
- [ ] Backup files can be created and opened
- [ ] Restore successfully reverts app state
- [ ] UI is responsive and follows Material 3
- [ ] No crashes or ANRs (Application Not Responding)
- [ ] Works on Android 13+ (minSdk 33)
- [ ] Performance is acceptable (<5 seconds for typical backup)

---

Happy Testing! 🎉
