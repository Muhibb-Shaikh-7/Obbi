# Obby Markdown & Linking Guide

Quick reference for writing notes in Obby.

---

## 📝 Basic Markdown Syntax

### Headings

```markdown
# Heading 1
## Heading 2
### Heading 3
#### Heading 4
##### Heading 5
###### Heading 6
```

### Text Formatting

```markdown
**Bold text**
*Italic text*
***Bold and italic***
~~Strikethrough~~
`Inline code`
```

### Lists

**Unordered Lists:**

```markdown
- Item 1
- Item 2
  - Nested item
  - Another nested item
- Item 3
```

**Ordered Lists:**

```markdown
1. First item
2. Second item
3. Third item
   1. Nested numbered item
```

**Task Lists:**

```markdown
- [ ] Uncompleted task
- [x] Completed task
- [ ] Another task
```

### Links

**Standard Links:**

```markdown
[Link text](https://example.com)
[Link with title](https://example.com "Title text")
```

**Automatic Links:**

```markdown
<https://example.com>
<email@example.com>
```

### Images

```markdown
![Alt text](image-url.jpg)
![Alt text](image-url.jpg "Image title")
```

### Blockquotes

```markdown
> This is a blockquote
> 
> It can span multiple lines

> Nested blockquotes
>> Are also supported
```

### Code Blocks

**Inline Code:**

```markdown
Use `code` in text
```

**Code Blocks:**

````markdown
```
Code block without syntax highlighting
```

```kotlin
fun example() {
    println("Kotlin code with syntax highlighting")
}
```

```python
def example():
    print("Python code")
```
````

### Horizontal Rules

```markdown
---
***
___
```

### Tables

```markdown
| Header 1 | Header 2 | Header 3 |
|----------|----------|----------|
| Cell 1   | Cell 2   | Cell 3   |
| Cell 4   | Cell 5   | Cell 6   |

| Left | Center | Right |
|:-----|:------:|------:|
| L1   | C1     | R1    |
| L2   | C2     | R2    |
```

---

## 🔗 Obby-Specific Features

### Wiki-Style Links

**Basic Link:**

```markdown
[[Note Title]]
```

Creates a link to a note with the title "Note Title". The link text will be "Note Title".

**Link with Custom Display Text:**

```markdown
[[Note Title|Custom Display Text]]
```

Links to "Note Title" but displays "Custom Display Text".

**Examples:**

```markdown
This note references [[Project Ideas]].

Read more about [[Kotlin Programming|Kotlin here]].

Check out [[Meeting Notes 2024-01-15]].
```

### Auto-Detection

- Links are automatically detected when you save a note
- Bidirectional relationships are created automatically
- If the target note doesn't exist yet, the link is dormant until you create it

### Tags

**Inline Tags:**

```markdown
#tag
#multi-word-tag
#category/subcategory
```

**Tags in Context:**

```markdown
This is a note about #productivity and #time-management.

Topics: #android #kotlin #jetpack-compose

Project: #work/project-alpha
```

**Tag Rules:**

- Must start with `#`
- Can contain letters, numbers, hyphens, underscores
- No spaces (use hyphens instead)
- Case-sensitive

### Backlinks

Backlinks are automatically tracked:

- When Note A links to Note B, Note B shows Note A in its backlinks
- Backlinks appear at the bottom of each note
- Click a backlink to navigate to the referencing note

---

## 💡 Writing Tips

### Organizing Notes

**By Folders:**

```
Personal/
├── Journal/
│   ├── 2024-01-15.md
│   └── 2024-01-16.md
└── Ideas/
    └── App Ideas.md

Work/
├── Projects/
│   └── Project Alpha.md
└── Meetings/
    └── Weekly Sync.md
```

**By Tags:**

```markdown
Use tags for cross-cutting concerns:
- #todo for tasks
- #important for priority items
- #review for items needing follow-up
- #archive for completed items
```

### Note Templates

**Daily Note:**

```markdown
# 2024-01-15

## Tasks
- [ ] Task 1
- [ ] Task 2

## Notes
- 

## Links
- [[Yesterday's Note]]
- [[Tomorrow's Note]]

#daily #journal
```

**Meeting Note:**

```markdown
# Meeting: [Topic]

**Date:** 2024-01-15
**Attendees:** [[Person 1]], [[Person 2]]

## Agenda
1. Item 1
2. Item 2

## Discussion


## Action Items
- [ ] [[Person 1]]: Action 1
- [ ] [[Person 2]]: Action 2

## Next Meeting
[[Next Meeting Date]]

#meeting #work
```

**Project Note:**

```markdown
# Project: [Name]

## Overview


## Goals
- Goal 1
- Goal 2

## Resources
- [[Related Document 1]]
- [[Related Document 2]]

## Timeline
- [ ] Phase 1: Description
- [ ] Phase 2: Description

## Notes


#project #active
```

### Linking Strategies

**MOC (Map of Content):**

```markdown
# Android Development MOC

## Languages
- [[Kotlin Basics]]
- [[Java Interop]]

## UI Frameworks
- [[Jetpack Compose]]
- [[View System]]

## Architecture
- [[MVVM Pattern]]
- [[Clean Architecture]]

## Libraries
- [[Room Database]]
- [[Retrofit]]
- [[Coroutines]]

#moc #android
```

**Zettelkasten Method:**

```markdown
# Note ID: 202401151430

Main idea in one sentence.

Detailed explanation...

## Related Notes
- [[202401141200]] - Previous thought
- [[202401151500]] - Next thought
- [[202401101000]] - Contradicting view

## Sources
- Source 1
- Source 2

#zettelkasten #permanent-note
```

---

## 🎨 Formatting Best Practices

### Readability

- Use headings to structure content
- Keep paragraphs short (3-5 lines)
- Use lists for clarity
- Add whitespace between sections

### Consistency

- Choose one heading style and stick with it
- Use consistent date formats (YYYY-MM-DD recommended)
- Tag consistently (#work vs #Work)

### Performance

- Break very long notes into smaller ones
- Link related notes instead of duplicating content
- Use folders for broad categories, tags for specifics

---

## 🔍 Search Tips

### Search Operators (Future Enhancement)

While not yet implemented, these patterns work well in titles:

```
[TODO] Task description
[DRAFT] Document name
[DONE] Completed item
```

### Current Search

- Searches both titles and content
- Case-insensitive
- Partial matches supported

**Examples:**

- Search "kotlin" finds "Kotlin Programming" and notes containing "kotlin"
- Search "2024-01" finds all notes from January 2024
- Search "#todo" finds all notes with todo tags

---

## 📊 Graph View Tips

### Building Your Graph

- Create a central MOC (Map of Content) note
- Link related concepts
- Use bidirectional links for stronger connections
- Tag clusters of related notes

### Navigation

- **Tap** a node to select it
- **Double-tap** to open the note
- View connection count in node info

### Visualization

- Nodes sized by connection count (coming soon)
- Color coding by folder/tag (coming soon)
- Force-directed layout (coming soon)

---

## 🔐 Encryption Notes

### When to Encrypt

- Personal sensitive information
- Financial data
- Passwords (use a password manager instead!)
- Private thoughts

### Encryption Limitations

- Encrypted notes cannot be searched
- Links in encrypted notes are not detected
- Tags in encrypted notes are not indexed
- Must decrypt to view/edit

### Best Practice

- Keep most notes unencrypted for full functionality
- Encrypt only truly sensitive content
- Remember: all data stays on your device anyway

---

## 🚀 Keyboard Shortcuts (Future)

*Coming in a future update:*

- `Ctrl/Cmd + N`: New note
- `Ctrl/Cmd + S`: Save note
- `Ctrl/Cmd + F`: Search
- `Ctrl/Cmd + B`: Bold
- `Ctrl/Cmd + I`: Italic
- `Ctrl/Cmd + K`: Insert link

---

## 📚 Additional Resources

### Markdown

- [Markdown Guide](https://www.markdownguide.org/)
- [CommonMark Spec](https://commonmark.org/)

### Note-Taking Methods

- [Zettelkasten Method](https://zettelkasten.de/)
- [PARA Method](https://fortelabs.co/blog/para/)
- [Getting Things Done (GTD)](https://gettingthingsdone.com/)

### Obsidian Resources

- [Obsidian Help](https://help.obsidian.md/)
- [Linking Your Thinking](https://www.linkingyourthinking.com/)

---

## 🆘 Troubleshooting

**Links Not Working?**

- Ensure target note exists with exact title
- Check for extra spaces or special characters
- Links are case-sensitive

**Tags Not Showing?**

- Make sure tag starts with `#`
- No spaces in tag names
- Save note to trigger tag extraction

**Graph Empty?**

- Create notes with [[links]] between them
- Links need to point to existing notes
- Refresh graph view

---

*Happy note-taking! 📝*
