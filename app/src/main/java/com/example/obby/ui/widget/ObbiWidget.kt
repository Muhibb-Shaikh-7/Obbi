package com.example.obby.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.obby.MainActivity
import com.example.obby.data.local.ObbyDatabase
import com.example.obby.data.local.entity.Note
import com.example.obby.data.repository.NoteRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

/**
 * Obbi widget showing recent notes, quick add button, and checklist progress
 */
class ObbiWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = ObbyDatabase.getDatabase(context)
        val repository = NoteRepository(
            noteDao = database.noteDao(),
            folderDao = database.folderDao(),
            tagDao = database.tagDao(),
            noteLinkDao = database.noteLinkDao()
        )

        // Fetch data before providing content
        val allNotes = repository.getAllNotesExcludingHidden().first()

        // Get pinned notes first, then recent notes
        val notes = (allNotes.filter { it.isPinned } + allNotes.filterNot { it.isPinned })
            .take(5) // Show only 5 most recent notes

        // Calculate checklist progress
        val checklistNotes = allNotes.filter { it.content.contains("- [") }
        val totalTasks = checklistNotes.sumOf { note ->
            note.content.count { it == '[' }
        }
        val completedTasks = checklistNotes.sumOf { note ->
            "- \\[x\\]".toRegex(RegexOption.IGNORE_CASE).findAll(note.content).count()
        }
        val progressPercent = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0

        provideContent {
            WidgetContent(
                context = context,
                notes = notes,
                totalTasks = totalTasks,
                completedTasks = completedTasks,
                progressPercent = progressPercent
            )
        }
    }

    @Composable
    private fun WidgetContent(
        context: Context,
        notes: List<Note>,
        totalTasks: Int,
        completedTasks: Int,
        progressPercent: Int
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .background(GlanceTheme.colors.background)
        ) {
            // Header with progress
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.Start,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Obbi",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onBackground
                        )
                    )
                    if (totalTasks > 0) {
                        Text(
                            text = "$completedTasks/$totalTasks tasks • $progressPercent%",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    }
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "+ Add",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.primary
                    ),
                    modifier = GlanceModifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .background(GlanceTheme.colors.primaryContainer)
                        .clickable {
                            val intent = Intent(context, MainActivity::class.java).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra("action", "create_note")
                            }
                            context.startActivity(intent)
                        }
                )
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Notes list
            if (notes.isEmpty()) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "No notes yet",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "Tap + Add to create one",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            } else {
                notes.forEach { note ->
                    NoteItem(context, note)
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }
            }
        }
    }

    @Composable
    private fun NoteItem(context: Context, note: Note) {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(GlanceTheme.colors.surface)
                .clickable {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("note_id", note.id)
                    }
                    context.startActivity(intent)
                }
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.Start,
                verticalAlignment = Alignment.Vertical.Top
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Row(
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                        horizontalAlignment = Alignment.Horizontal.Start
                    ) {
                        Text(
                            text = note.title,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = GlanceTheme.colors.onSurface
                            ),
                            maxLines = 1
                        )
                        if (note.isPinned) {
                            Spacer(modifier = GlanceModifier.width(4.dp))
                            Text(
                                text = "📌",
                                style = TextStyle(fontSize = 10.sp)
                            )
                        }
                    }
                    if (note.content.isNotBlank()) {
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        // Clean markdown formatting for preview
                        val preview = note.content
                            .replace(Regex("^#{1,6}\\s"), "")
                            .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
                            .replace(Regex("\\*(.+?)\\*"), "$1")
                            .take(50)
                        Text(
                            text = preview,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            ),
                            maxLines = 2
                        )
                    }
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = dateFormat.format(Date(note.modifiedAt)),
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }
    }
}
