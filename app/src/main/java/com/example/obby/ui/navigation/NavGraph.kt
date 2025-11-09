package com.example.obby.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.obby.data.repository.NoteRepository
import com.example.obby.ui.screens.GraphScreen
import com.example.obby.ui.screens.NoteDetailScreen
import com.example.obby.ui.screens.NotesListScreen
import com.example.obby.ui.screens.SettingsScreen
import com.example.obby.ui.screens.SplashScreen
import com.example.obby.ui.screens.HiddenNotesScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object NotesList : Screen("notes_list")
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Long) = "note_detail/$noteId"
    }

    object Graph : Screen("graph")
    object Settings : Screen("settings")
    object HiddenNotes : Screen("hidden_notes")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: NoteRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.NotesList.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.NotesList.route) {
            NotesListScreen(
                repository = repository,
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                },
                onGraphClick = {
                    navController.navigate(Screen.Graph.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onHiddenNotesClick = {
                    navController.navigate(Screen.HiddenNotes.route)
                }
            )
        }

        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: return@composable
            NoteDetailScreen(
                repository = repository,
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() },
                onNoteClick = { linkedNoteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(linkedNoteId))
                }
            )
        }

        composable(Screen.Graph.route) {
            GraphScreen(
                repository = repository,
                onNodeClick = { nodeId ->
                    navController.navigate(Screen.NoteDetail.createRoute(nodeId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HiddenNotes.route) {
            HiddenNotesScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                }
            )
        }
    }
}
