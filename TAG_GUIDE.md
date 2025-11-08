# 📌 Obby Tag System Guide

## How to Add Tags to Your Notes

Tags in Obby are **automatically extracted** from your note content using hashtags. No manual
tagging UI needed—just type and go!

---

## ✍️ **Method: Inline Hashtags (Auto-Extraction)**

### **How It Works**:

1. Open any note in edit mode
2. Type `#tagname` anywhere in your content
3. Tags are **automatically extracted** and indexed
4. Tags appear in the drawer menu instantly

### **Examples**:

#### **Simple Tags**:

```markdown
# My Weekend Plans

Going hiking this weekend #outdoor #exercise #nature
```

**Result**: Creates 3 tags: `outdoor`, `exercise`, `nature`

---

#### **Project Organization**:

```markdown
# Android App Ideas

Build a note-taking app using #kotlin #android #jetpackcompose

Features to implement:
- Markdown support #markdown
- Offline-first #database #room
- Beautiful UI #materialdesign #ui
```

**Result**: Creates 7 tags: `kotlin`, `android`, `jetpackcompose`, `markdown`, `database`, `room`,
`materialdesign`, `ui`

---

#### **Multiple Tags in One Line**:

```markdown
This note covers #programming #tutorial #beginner topics
```

**Result**: All 3 tags are extracted

---

#### **Tags in Lists**:

```markdown
## Technologies to Learn

- #python - For data science
- #rust - For systems programming  
- #go - For backend services
```

**Result**: Creates 3 separate tags

---

#### **Tags in Different Sections**:

```markdown
# Book Notes

## Chapter 1 #introduction
Key concepts about #philosophy and #ethics

## Chapter 2 #advanced  
Deep dive into #theory and #practice
```

**Result**: 6 tags from different parts of the note

---

## 🔍 **Viewing Your Tags**

### **1. In the Drawer Menu**:

1. Open the **drawer** (tap ☰ menu icon)
2. Scroll to the **"Tags"** section
3. See all your tags listed with # prefix

### **2. Tag Format in Drawer**:

```
Tags
  #android
  #kotlin
  #productivity
  #tutorial
```

---

## 📂 **Using Tags to Filter Notes**

### **Find Notes by Tag**:

1. Open the **drawer** menu
2. Scroll to **"Tags"** section
3. **Tap any tag** (e.g., `#kotlin`)
4. View **all notes** containing that tag
5. Tap **"All Notes"** to clear filter

### **Example Workflow**:

```
Step 1: Tap #kotlin in drawer
Step 2: See all notes with #kotlin tag
Step 3: Edit/view any note
Step 4: Back to list → still filtered by #kotlin
Step 5: Open drawer → Tap "All Notes" to show all
```

---

## 🎯 **Tag Best Practices**

### ✅ **DO**:

- Use **lowercase** for consistency: `#android` not `#Android`
- Keep tags **short**: `#ui` not `#userinterface`
- Be **specific**: `#jetpackcompose` not `#compose`
- Use **compound words**: `#mobiledevelopment` or `#mobile-dev`
- Tag early and often

### ❌ **DON'T**:

- Avoid spaces: `#mobile dev` → Only `#mobile` is tagged
- No special characters: `#c++` → Won't work properly
- Don't over-tag: 5-10 tags per note is plenty

---

## 🏷️ **Smart Tagging Strategies**

### **1. Category Tags**:

```markdown
#work #personal #learning #ideas #project
```

Broad categories for organizing notes

### **2. Technology Tags**:

```markdown
#kotlin #python #javascript #react #android #ios
```

Programming languages and frameworks

### **3. Status Tags**:

```markdown
#todo #inprogress #done #review #archived
```

Track project status

### **4. Content Type Tags**:

```markdown
#tutorial #reference #notes #cheatsheet #summary
```

Identify note types

### **5. Topic Tags**:

```markdown
#algorithms #design #architecture #testing #deployment
```

Specific subject areas

---

## 📝 **Tag Examples for Different Use Cases**

### **Student Notes**:

```markdown
# Computer Science Lecture Notes

Today's topic: Data Structures #cs101 #datastructures #lecture

Key concepts:
- Arrays and Lists #arrays
- Trees and Graphs #trees #graphs #algorithms
- Time Complexity #bigO #performance

#school #university #fall2024
```

### **Work Project**:

```markdown
# Mobile App Redesign

Client wants modern UI #client #design #ui

Technologies:
- Kotlin #kotlin #android
- Jetpack Compose #compose #declarativeui
- Room Database #room #database

Deadline: Next week #urgent #deadline
```

### **Recipe Notes**:

```markdown
# Grandma's Pasta Recipe

Best Italian pasta ever! #recipe #italian #cooking

Ingredients #ingredients:
- Tomatoes #vegetables
- Basil #herbs
- Garlic #aromatics

#family #dinner #homemade
```

### **Book Summary**:

```markdown
# "Clean Code" Summary

Great book on programming #books #programming #reading

Key takeaways #notes:
- Functions should be small #functions #refactoring
- Names should be meaningful #naming #bestpractices
- Comments are a code smell #comments #documentation

#softwareengineering #career #2024reading
```

---

## 🔗 **Tags vs. Folders vs. Links**

| Feature | Best For | Example |
|---------|----------|---------|
| **Tags** | Cross-cutting topics | `#android`, `#tutorial` |
| **Folders** | Hierarchical organization | `Projects/Mobile/Android` |
| **Links** | Related notes | `[[Related Note]]` |

### **Combined Example**:

```markdown
Folder: Work/Projects/Mobile
File: android-app-notes.md

# Android App Development Notes

This relates to [[Project Timeline]] and [[API Documentation]]

Working on #android #mobile #kotlin features

Using #jetpackcompose for #ui and #room for #database
```

**Result**:

- **Folder**: Organized under Work/Projects/Mobile
- **Links**: Connected to 2 other notes
- **Tags**: 6 searchable tags

---

## 💡 **Advanced Tag Techniques**

### **1. Tag Hierarchies (Manual)**:

```markdown
#project #project-mobile #project-mobile-android
```

Create your own hierarchy with prefixes

### **2. Date Tags**:

```markdown
#2024 #q1-2024 #january2024 #week1
```

Time-based organization

### **3. Priority Tags**:

```markdown
#p1 #p2 #p3 #critical #important #low-priority
```

Indicate importance

### **4. Context Tags**:

```markdown
#home #office #online #meeting #review
```

Where/when to act on notes

---

## 🔎 **Finding Tags in Your Notes**

### **Quick Search**:

1. Use the **search bar** at top
2. Type `#tagname` in search
3. Finds all notes with that tag

### **Example Searches**:

- `#android` - All Android notes
- `tutorial` - Notes with "tutorial" in text OR tag
- `#kotlin #android` - Notes with both tags

---

## 📊 **Tag Management Tips**

### **Keep Tags Organized**:

1. **Review periodically** - Delete unused tags
2. **Standardize naming** - Decide on conventions
3. **Merge similar tags** - Use search & replace
4. **Document your system** - Keep a "Tags Guide" note

### **Example Tag Convention Note**:

```markdown
# My Tagging System

## Categories
- #work, #personal, #learning

## Technologies  
- #kotlin, #python, #javascript

## Status
- #todo, #inprogress, #done

## Always use lowercase
## Keep tags short (1-2 words)
## Review tags monthly
```

---

## 🎨 **Future Tag Features (Coming Soon)**

While the current system is fully functional, here are planned enhancements:

- [ ] **Tag chips** on note cards (visual preview)
- [ ] **Tag colors** for visual organization
- [ ] **Tag renaming** (updates all notes)
- [ ] **Tag merging** (combine similar tags)
- [ ] **Tag statistics** (usage count)
- [ ] **Popular tags** widget
- [ ] **Tag autocomplete** when typing #
- [ ] **Tag suggestions** based on content

---

## ❓ **Troubleshooting**

### **Tags Not Appearing?**

✅ Make sure you're using `#` before the word
✅ Tag should be one word (no spaces)
✅ Note must be saved (auto-saves after 2 seconds)
✅ Check drawer → Tags section

### **Can't Find Tag in Drawer?**

✅ Ensure at least one note has that tag
✅ Tag is automatically created when note is saved
✅ Try closing and reopening the drawer

### **Tag Filtering Not Working?**

✅ Make sure you tapped the tag in drawer
✅ Check if search bar is empty
✅ Try tapping "All Notes" first, then the tag again

---

## 📚 **Quick Reference**

### **Add Tags**:

```
Type #tagname in note content
```

### **View Tags**:

```
Open drawer → Scroll to "Tags" section
```

### **Filter by Tag**:

```
Drawer → Tags → Tap any tag
```

### **Clear Tag Filter**:

```
Drawer → Tap "All Notes"
```

### **Tag Format**:

```
✅ #android
✅ #kotlin-programming
✅ #ui_design
❌ #my tag (has space)
❌ #c++ (special chars)
```

---

## 🎓 **Example: Building a Personal Knowledge Base**

```markdown
# Setting Up My PKM System

Creating a Personal Knowledge Management system #pkm #productivity #organization

## Categories #planning
- Work notes #work
- Personal projects #personal
- Learning resources #learning
- Reference materials #reference

## Tagging Strategy #strategy
- Use technology tags: #android #kotlin #python
- Add status: #todo #inprogress #done
- Include type: #tutorial #notes #ideas
- Time context: #2024 #q1

## Example Note Structure #template
```

# Note Title

Content with #relevant #tags

Related: [[Other Note]], [[Another Note]]

```

This system will help organize all my notes #system #workflow
```

**Result**: 20+ tags automatically created, note connected to PKM system

---

## 💡 **Pro Tips**

1. **Start with broad tags**, refine later
2. **Use tags AND folders** for maximum flexibility
3. **Tag at the end** of writing to summarize content
4. **Review your tags weekly** to maintain consistency
5. **Create a "tag legend" note** for reference
6. **Export notes** with tags for backup (tags preserved in text)

---

**Remember**: Tags in Obby are **automatic, flexible, and powerful**. Just type `#anything` and
start organizing! 🚀

---

**Need help?** Check the main README.md or open an issue on GitHub.
