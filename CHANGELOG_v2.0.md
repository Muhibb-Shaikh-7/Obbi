# Changelog - Obbi 2.0

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [2.0.0] - TBD

### 🎉 Major Features

#### Enhanced Editor Experience

- **Added** Persistent formatting toolbar in edit mode
- **Added** Haptic feedback for all toolbar actions
- **Added** Keyboard shortcuts (Ctrl+B for bold, Ctrl+I for italic, etc.)
- **Enhanced** Markdown guide with tabbed categories and examples
- **Added** Visual auto-save indicator showing save state
- **Improved** Undo/Redo stack with better state management

#### Interactive Preview Mode

- **Added** Clickable checkboxes in preview - tap to toggle state
- **Added** Clickable wiki-style links ([[Note Title]]) - tap to navigate
- **Added** Clickable standard markdown links
- **Improved** Preview rendering with better theme support
- **Added** Smooth animation when toggling between edit and preview
- **Fixed** Preview scrolling position preservation

#### Advanced Organization

- **Added** Nested folder support (unlimited depth)
- **Added** Hierarchical folder picker with expand/collapse
- **Added** Breadcrumb navigation showing current folder path
- **Added** Drag-and-drop note reordering within folders
- **Added** Custom sort order that persists across sessions
- **Added** Ability to create subfolders from folder picker

#### UX Polish & Animations

- **Added** Rich empty state screens with helpful guidance
- **Added** Smooth transitions between screens
- **Added** Animated multi-select mode activation
- **Improved** FAB behavior with expanded menu
- **Enhanced** Context menu with icons and better organization
- **Added** Visual feedback for drag-and-drop operations
- **Improved** Loading states throughout the app

### 🔧 Technical Improvements

#### Database

- **Added** `sortOrder` field to Note entity for custom ordering
- **Migrated** Database from version 2 to version 3
- **Improved** Query performance with better indexing
- **Added** Support for folder hierarchy queries

#### Architecture

- **Added** `CheckboxParser` utility for markdown checkbox parsing
- **Added** `InteractiveMarkdownView` composable
- **Added** `HierarchicalFolderPicker` composable
- **Added** `DraggableNoteList` composable
- **Added** `AutoSaveIndicator` composable
- **Added** `EnhancedEmptyState` composable
- **Added** `FolderBreadcrumb` composable
- **Enhanced** `NoteRepository` with new methods:
    - `reorderNotes()` - Persist custom note order
    - `findNoteByTitle()` - Quick note lookup for navigation
- **Enhanced** `NotesViewModel` with folder tree building logic
- **Enhanced** `NoteDetailViewModel` with checkbox and link interaction

#### Testing

- **Added** Unit tests for `CheckboxParser` (100% coverage)
- **Added** Unit tests for `NoteRepository` (80%+ coverage)
- **Added** Integration tests for folder hierarchy
- **Added** Compose UI tests for key user flows
- **Improved** Test infrastructure and utilities

#### Performance

- **Added** Paging support for large note lists (500+ notes)
- **Optimized** Folder tree building algorithm
- **Reduced** Memory usage in note list rendering
- **Improved** Scroll performance with better LazyColumn configuration
- **Optimized** Database queries for better response times

### 🐛 Bug Fixes

- **Fixed** Auto-save indicator not hiding after save complete
- **Fixed** Multi-select mode persisting after navigation
- **Fixed** Empty folder state not showing correctly
- **Fixed** Drag gesture interfering with scroll in note list
- **Fixed** Theme colors not updating in preview mode
- **Fixed** Cursor position jumping when toolbar action applied
- **Fixed** Folder picker showing incorrect selection state
- **Fixed** Breadcrumb overflow on small screens
- **Fixed** Memory leak in markdown preview rendering

### 🔒 Security

- **Maintained** AES-256-GCM encryption for notes
- **Maintained** Android Keystore integration
- **Verified** No network dependencies added
- **Ensured** All data remains local and private

### ♿ Accessibility

- **Improved** Content descriptions for all new UI elements
- **Added** Semantic labels for toolbar actions
- **Enhanced** Touch target sizes for better usability
- **Tested** TalkBack compatibility for new features
- **Added** High contrast support for all new components

### 📚 Documentation

- **Added** Comprehensive upgrade plan (OBBI_UPGRADE_PLAN.md)
- **Added** Quick start implementation guide
- **Added** Architecture diagrams for new components
- **Updated** README with new feature descriptions
- **Added** Code examples for common use cases
- **Updated** User guide with interactive preview instructions

### 🔄 Changed

- **Changed** Notes list default sorting to include custom order
- **Changed** Folder creation flow to support parent folder selection
- **Changed** Multi-select mode activation (now with animation)
- **Changed** Empty state design throughout app
- **Updated** Material3 theme consistency

### ⚠️ Breaking Changes

**None!** This is a fully backward-compatible upgrade.

- All existing notes remain readable
- Folder structure preserved
- Links and tags unchanged
- Encryption keys remain valid
- No data loss on upgrade

### 📦 Dependencies

#### Added

- `androidx.paging:paging-runtime:3.2.1`
- `androidx.paging:paging-compose:3.2.1`

#### Updated

- None (all existing dependencies remain at current versions)

### 🗑️ Deprecated

**None.** All existing features remain supported.

### 🚫 Removed

**None.** This is an additive release only.

---

## [1.0.0] - Previous Release

### Features

- Core note CRUD operations
- Markdown editor and rendering
- Bidirectional linking
- Graph visualization
- Folder organization (flat)
- Tag system with auto-detection
- Full-text search
- Note encryption (AES-256-GCM)
- Pin and favorite notes
- Undo/Redo support
- Auto-save functionality
- Multi-select mode
- Formatting toolbar
- Preview toggle
- Import/export notes
- Dark and light themes

---

## Migration Guide

### From v1.x to v2.0

**Database Migration:**
The database will automatically migrate from version 2 to 3 on first launch. This adds the
`sortOrder` field to notes.

**No User Action Required:**

- All existing notes remain unchanged
- Folders continue to work (now with nesting support!)
- Tags and links are preserved
- Encrypted notes remain encrypted with same keys

**New Features to Explore:**

1. **Interactive Preview:** Tap the eye icon in editor, then tap checkboxes or links
2. **Nested Folders:** Long-press a folder to create subfolders
3. **Drag Reordering:** Long-press and drag notes to reorder
4. **Keyboard Shortcuts:** Use Ctrl+B, Ctrl+I in editor on devices with keyboards

**Rollback:**
If you need to rollback to v1.x, export your notes first (backup recommended), then:

1. Uninstall Obbi v2.0
2. Install Obbi v1.x
3. Import notes from backup

---

## Known Issues

_To be filled in during development and testing_

### In Progress

- [ ] Drag-and-drop animation polish
- [ ] Nested folder performance with 100+ folders
- [ ] Interactive preview on very large notes (10,000+ words)

### Planned Fixes

- None currently

---

## Contributors

- [Your Name] - Lead Developer
- [Contributors from community]
- Special thanks to testers and early adopters!

---

## Release Notes

### What's New in 2.0

Obbi 2.0 is a major upgrade that brings Obsidian-class features to your offline note-taking
experience:

**🖊️ Professional Editor**
The editor now feels like a premium writing app with always-visible formatting tools, keyboard
shortcuts, and clear auto-save feedback.

**👁️ Living Preview**
Preview mode is no longer static—tap checkboxes to toggle tasks and links to jump between notes, all
without leaving preview mode.

**📁 Unlimited Organization**
Create folders within folders, organize complex projects with ease, and navigate with breadcrumbs
showing exactly where you are.

**✨ Polished Experience**
Smooth animations, helpful empty states, and consistent Material3 design make Obbi a joy to use
every day.

**🔒 Still 100% Private**
Everything stays offline, encrypted notes remain secure, and your data never leaves your device.

---

## Upgrade Instructions

### For Users

1. Backup your notes (Settings → Export → Export All)
2. Update Obbi through your app store or side-load the new APK
3. Open app - migration happens automatically
4. Explore new features!

### For Developers

1. Pull latest code from `main` branch
2. Review `OBBI_UPGRADE_PLAN.md` for architecture details
3. Run `./gradlew clean build` to ensure everything compiles
4. Run `./gradlew test` to verify tests pass
5. Deploy to test devices

---

## Support

If you encounter issues after upgrading:

1. Check the [Troubleshooting Guide](TROUBLESHOOTING.md)
2. Search [GitHub Issues](https://github.com/yourusername/obby/issues)
3. Create a new issue with:
    - Device model and Android version
    - Obbi version (Settings → About)
    - Steps to reproduce the issue
    - Logcat output (if available)

---

**Last Updated:** TBD  
**Status:** In Development  
**Target Release:** Week 8 of implementation timeline
