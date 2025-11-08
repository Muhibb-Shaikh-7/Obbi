# Obby - Offline Note-Taking App

<div align="center">
  <h3>A fully offline, privacy-focused note-taking app inspired by Obsidian</h3>
  <p>Built with Kotlin, Jetpack Compose, and Room Database</p>
</div>

---

## 📱 Overview

Obby is a powerful, offline-first note-taking application for Android that brings the best features
of Obsidian to mobile. Create, organize, and link your notes with Markdown support, bidirectional
linking, and graph visualization—all stored securely on your device with no internet connection
required.

## ✨ Features

### Core Features

- **📝 Markdown Editor** - Full Markdown support with live preview
- **🔗 Bidirectional Links** - Connect notes with `[[Note Title]]` syntax
- **🔙 Backlink Viewer** - See all notes that reference the current note
- **📊 Graph View** - Visualize connections between notes
- **📁 Folder Organization** - Organize notes in a hierarchical folder structure
- **🏷️ Tag System** - Auto-detect tags with `#tag` syntax
- **🔍 Full-Text Search** - Quickly find notes by title or content
- **📌 Pin & Favorite** - Mark important notes for quick access
- **🔐 Local Encryption** - Encrypt sensitive notes using Android Keystore
- **📤 Import/Export** - Backup and restore notes as Markdown files
- **🌓 Dark & Light Themes** - Automatic theme switching with Material You support

### Privacy & Security

- **100% Offline** - Works without any internet connection
- **Local Storage** - All data stored on your device
- **End-to-End Encryption** - Optional AES-256 encryption for individual notes
- **No Analytics** - Zero tracking or data collection
- **No Ads** - Clean, distraction-free experience
- **Password-Based Encryption** - Protect sensitive notes with strong encryption

## 🚀 Quick Start

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/obby.git
   cd obby
   ```

2. **Open in Android Studio:**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Build and Run:**
   - Connect an Android device or start an emulator
   - Click the "Run" button (▶️) or press `Shift+F10`

### Requirements

- **Android Studio** Hedgehog or newer
- **Android SDK** 33+
- **Java** 11+
- **Kotlin** 2.0.21

## 📖 User Guide

### Creating Notes

#### Quick Note

1. Tap the **floating action button (FAB)** with the `+` icon
2. Enter a title
3. Tap "Create"
4. Start writing!

#### With Folder

1. Open the **drawer** (tap menu icon)
2. Select a **folder**
3. Tap the **FAB**
4. Your note is automatically placed in that folder

#### Checklist

1. Tap and hold the **FAB** to expand the menu
2. Tap the **checkbox icon**
3. Enter a title
4. Your note will have a pre-formatted checklist template

### Editing Notes

#### Entering Edit Mode

- Tap the **edit icon** (✏️) in the top bar
- Or tap on a note to view, then tap edit

#### Using the Markdown Toolbar

The toolbar appears at the bottom when editing:

| Icon    | Action           | Shortcut       |
|---------|------------------|----------------|
| **B**   | Bold             | `**text**`     |
| **I**   | Italic           | `*text*`       |
| **~**   | Strikethrough    | `~~text~~`     |
| **#**   | Headings (H1-H6) | `# Heading`    |
| **•**   | Bullet List      | `- Item`       |
| **1.**  | Numbered List    | `1. Item`      |
| **☐**   | Checklist        | `- [ ] Task`   |
| **"**   | Quote            | `> Quote`      |
| **`**   | Inline Code      | `` `code` ``   |
| **</>** | Code Block       | ` ```code``` ` |
| **🔗**  | Link             | `[text](url)`  |
| **📊**  | Table            | Auto-inserted  |
| **─**   | Horizontal Rule  | `---`          |
| **?**   | Help             | Markdown guide |

#### Preview Mode

- Tap the **preview icon** (👁️) to see rendered Markdown
- Tap **edit icon** to return to editing
- Preview supports interactive checklists!

#### Auto-Save

- Content **automatically saves** 2 seconds after you stop typing
- No need to press a save button
- Look for the **progress indicator** in the title bar

#### Undo/Redo

- Tap the **undo arrow** (↶) to undo changes
- Tap the **redo arrow** (↷) to redo changes
- Works for both title and content

### Managing Notes

#### Long-Press Context Menu

1. **Long press** on any note in the list
2. A context menu appears with options:
   - **Pin/Unpin** - Keep note at the top
   - **Favorite** - Add to favorites collection
   - **Rename** - Change the note title
   - **Duplicate** - Create a copy
   - **Move to Folder** - Select a destination folder
   - **Share** - Export as plain text
   - **Delete** - Remove the note (with confirmation)

#### Multi-Select Mode

1. **Long press** on any note to enter multi-select mode
2. **Tap** additional notes to select them
3. Use the **action bar** at the top:
   - **Select All** - Select every note in the current view
   - **Move** - Move all selected notes to a folder
   - **Delete** - Remove all selected notes
4. Tap **X** to exit multi-select mode

#### Three-Dot Menu

- Tap the **⋮** icon on any note card
- Access the same actions as long-press menu
- Alternative to long-press for easier one-handed use

### Organizing with Folders

#### Creating Folders

1. Tap the **FAB** and select **folder icon**
2. Or use the **+** button in the drawer next to "Folders"
3. Enter a folder name
4. Tap "Create"

#### Moving Notes

1. Long-press or tap **⋮** on a note
2. Select **"Move to folder"**
3. Choose a destination folder or "Root"
4. The note is instantly moved

#### Viewing Folders

1. Open the **drawer**
2. Tap on any **folder name**
3. Only notes in that folder are shown
4. Tap **"All Notes"** to clear the filter

### Using Tags

#### Adding Tags

- Simply type `#tagname` anywhere in your note content
- Tags are **automatically extracted** and indexed
- Example: `This note is about #android #development`

#### Viewing Tags

1. Open the **drawer**
2. Scroll to the **"Tags"** section
3. Tap a tag to see all notes with that tag

### Linking Notes

#### Creating Links

- Use wiki-style syntax: `[[Note Title]]`
- Example: `See also [[Project Ideas]]`
- Links are automatically created if the note exists

#### Following Links

- Tap a link in **preview mode** to jump to that note
- Links are highlighted in blue

#### Viewing Backlinks

- Open any note
- Scroll to the bottom
- See **"Backlinks"** section showing notes that link to this one

### Searching

1. Use the **search bar** at the top of the notes list
2. Type to search **titles and content**
3. Results appear in real-time
4. Tap **X** to clear the search

### Graph View

1. Tap the **graph icon** (🕸️) in the top bar
2. See a visual representation of note connections
3. Notes are nodes, links are edges
4. Tap any node to jump to that note

### Note Encryption

#### Encrypting a Note

1. Open any note
2. Tap the **⋮** menu in the top bar
3. Select **"Encrypt"**
4. Enter a **strong password** (minimum 1 character, but use a strong password!)
5. Tap **"Encrypt"**
6. The note content is now encrypted and shows a 🔒 icon
7. ⚠️ **Remember your password!** There is no recovery option

#### Decrypting a Note

1. Open an encrypted note (shows 🔒 icon in the list and cannot be edited)
2. Tap the **⋮** menu
3. Select **"Decrypt"**
4. Enter the **same password** you used to encrypt
5. Tap **"Decrypt"**
6. If the password is correct, the note is decrypted
7. If incorrect, you'll see an error message

#### Encryption Technical Details

- **Algorithm**: AES-256-GCM (military-grade authenticated encryption)
- **Key Derivation**: PBKDF2 with HMAC-SHA256, 65,536 iterations
- **Salt**: 32-byte random salt per note (prevents rainbow table attacks)
- **IV**: 12-byte random initialization vector per encryption
- **Password Required**: Each note can have a different password
- **No Recovery**: If you forget the password, the note **cannot be recovered**
- **Offline**: All encryption happens locally on your device

#### Important Security Notes

⚠️ **Warning**:

- **Use a strong, unique password** for important notes (12+ characters recommended)
- There is **no "forgot password"** or recovery mechanism
- **Encrypted notes cannot be searched** until decrypted
- **The password is never stored**—only you know it
- **Encrypted notes cannot be edited** until decrypted
- **Links and tags** in encrypted notes are not indexed

💡 **Best Practices**:

- Use a password manager to generate and store strong passwords
- Don't encrypt notes unless necessary (adds friction to editing)
- Consider using folders for organization instead of encryption for all notes
- Test the password immediately after encrypting to ensure you remember it

---

## 🎨 Markdown Guide

### Text Formatting

```markdown
**Bold text**
*Italic text*
~~Strikethrough text~~
`Inline code`
```

### Headings

```markdown
# Heading 1
## Heading 2
### Heading 3
#### Heading 4
##### Heading 5
###### Heading 6
```

### Lists

```markdown
- Bullet point
- Another point
  - Nested point

1. First item
2. Second item
3. Third item

- [ ] Unchecked task
- [x] Checked task
```

### Links

```markdown
[Link text](https://example.com)
[[Wiki-style link to another note]]
```

### Code

````markdown
Inline `code` in text

```kotlin
// Code block with syntax highlighting
fun hello() {
    println("Hello, World!")
}
```
````

### Quotes

```markdown
> This is a quote
> It can span multiple lines
```

### Tables

```markdown
| Column 1 | Column 2 | Column 3 |
|----------|----------|----------|
| Data 1   | Data 2   | Data 3   |
| More     | Data     | Here     |
```

### Horizontal Rule

```markdown
---
```

---

## 🏗️ Architecture

Obby follows **Clean Architecture** principles with **MVVM** pattern:

```
UI Layer (Compose)
    ↓
ViewModel Layer (State Management)
    ↓
Repository Layer (Business Logic)
    ↓
Data Layer (Room Database)
```

### Tech Stack

- **UI**: Jetpack Compose, Material 3
- **Architecture**: MVVM, Clean Architecture
- **Database**: Room (SQLite)
- **Async**: Kotlin Coroutines, Flow
- **Navigation**: Navigation Compose
- **Markdown**: Markwon (rendering), custom toolbar
- **Security**: Android Keystore, Security Crypto
- **Storage**: Local files + Room database

### Key Components

- **NotesViewModel** - Manages note list, search, multi-select
- **NoteDetailViewModel** - Handles editing, auto-save, undo/redo
- **NoteRepository** - Coordinates data operations
- **ObbyDatabase** - Room database with DAOs
- **MarkdownToolbar** - Custom formatting toolbar
- **MarkdownFormatter** - Syntax insertion logic

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

### Getting Started

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use **meaningful variable names**
- Add **comments** for complex logic
- Write **composable functions** for reusable UI
- Keep **ViewModels** thin and testable

### Testing

- Add **unit tests** for ViewModels and repositories
- Test **edge cases** (empty states, errors, etc.)
- Verify **UI behavior** with Compose tests

---

## 🐛 Troubleshooting

### App Won't Build

**Solution:**

1. Clean project: `Build → Clean Project`
2. Invalidate caches: `File → Invalidate Caches → Invalidate and Restart`
3. Sync Gradle: `File → Sync Project with Gradle Files`
4. Check SDK installation in `Tools → SDK Manager`

### Notes Not Saving

**Possible Causes:**

- Not in edit mode (tap the edit icon)
- Auto-save delay hasn't passed (wait 2 seconds)
- Database error (check Logcat)

**Solution:**

- Manually save with the checkmark icon
- Restart the app
- Check device storage space

### Slow Performance

**Tips:**

- Reduce number of notes (archive old ones)
- Clear search filters
- Avoid extremely large notes (>50,000 characters)
- Close and reopen the app

### Markdown Not Rendering

**Solution:**

- Toggle preview mode off and back on
- Check Markdown syntax (use the **?** help button)
- Ensure note is not encrypted

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

### Libraries

- [Markwon](https://github.com/noties/Markwon) - Excellent Markdown rendering
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Room](https://developer.android.com/jetpack/androidx/releases/room) - Robust database

### Inspiration

- [Obsidian](https://obsidian.md/) - Connected thought organization
- [Notion](https://www.notion.so/) - Flexible note-taking
- [Bear](https://bear.app/) - Beautiful Markdown editor

---

## 🗺️ Roadmap

### Phase 1: Core Features

- [x] Basic note CRUD operations
- [x] Folder organization
- [x] Tag system
- [x] Markdown rendering
- [x] Search functionality
- [x] Note linking

### Phase 2: Enhanced Editing

- [x] Markdown toolbar
- [x] Live preview toggle
- [x] Auto-save
- [x] Undo/Redo
- [x] Long-press actions
- [x] Multi-select mode

### Phase 3: Advanced Features

- [ ] Nested folders
- [ ] Note templates
- [ ] Custom themes
- [ ] Export to PDF/HTML
- [ ] Image attachments
- [ ] Voice notes
- [ ] Widget support
- [ ] Backup/restore

### Phase 4: Power User

- [ ] Keyboard shortcuts
- [ ] Vim mode
- [ ] Split-screen editing
- [ ] Note versioning
- [ ] Advanced search (regex)
- [ ] Custom CSS for preview
- [ ] LaTeX math support
- [ ] Mermaid diagrams

---

## 📞 Support

### Documentation

- **User Guide**: This README
- **Architecture**: [ENHANCEMENTS_IMPLEMENTATION.md](ENHANCEMENTS_IMPLEMENTATION.md)
- **Development**: [DEVELOPMENT.md](DEVELOPMENT.md)
- **Troubleshooting**: Check issues or Logcat

### Community

- **Issues**: [GitHub Issues](https://github.com/yourusername/obby/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/obby/discussions)

---

<div align="center">

**Built with ❤️ using Kotlin and Jetpack Compose**

⭐ **Star this repo if you find it useful!** ⭐

</div>

