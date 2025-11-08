package com.example.obby.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.obby.data.repository.NoteRepository
import com.example.obby.util.BackupManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: NoteRepository,
    private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    sealed class BackupState {
        object Idle : BackupState()
        data class Loading(val message: String) : BackupState()
        data class Success(val message: String) : BackupState()
        data class Error(val message: String) : BackupState()
    }

    suspend fun createBackup(destinationUri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading("Creating backup...")

            try {
                // Get all notes
                val notes = repository.getAllNotes().first()

                // Create backup
                val result = BackupManager.createBackup(context, notes, destinationUri)

                if (result.success) {
                    _backupState.value = BackupState.Success(result.message)
                    Log.d(TAG, "Backup created successfully")
                } else {
                    _backupState.value = BackupState.Error(result.message)
                    Log.e(TAG, "Backup failed: ${result.message}")
                }

                // Reset state after delay
                kotlinx.coroutines.delay(3000)
                _backupState.value = BackupState.Idle
            } catch (e: Exception) {
                Log.e(TAG, "Error creating backup", e)
                _backupState.value = BackupState.Error("Failed to create backup: ${e.message}")
                kotlinx.coroutines.delay(3000)
                _backupState.value = BackupState.Idle
            }
        }
    }

    suspend fun restoreBackup(backupUri: Uri) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading("Restoring backup...")

            try {
                val result = BackupManager.restoreBackup(context, backupUri)

                if (result.success) {
                    _backupState.value = BackupState.Success(result.message)
                    Log.d(TAG, "Backup restored successfully")
                } else {
                    _backupState.value = BackupState.Error(result.message)
                    Log.e(TAG, "Restore failed: ${result.message}")
                }

                // Keep success/error state for longer to allow app restart
                kotlinx.coroutines.delay(2000)
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring backup", e)
                _backupState.value = BackupState.Error("Failed to restore backup: ${e.message}")
                kotlinx.coroutines.delay(3000)
                _backupState.value = BackupState.Idle
            }
        }
    }
}
