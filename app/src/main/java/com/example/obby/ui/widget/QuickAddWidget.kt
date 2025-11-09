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
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.obby.MainActivity

/**
 * Compact 1x1 widget for quick note creation
 */
class QuickAddWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            QuickAddContent(context)
        }
    }

    @Composable
    private fun QuickAddContent(context: Context) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
                .background(GlanceTheme.colors.primaryContainer)
                .clickable {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("action", "create_note")
                    }
                    context.startActivity(intent)
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "Add Note",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
