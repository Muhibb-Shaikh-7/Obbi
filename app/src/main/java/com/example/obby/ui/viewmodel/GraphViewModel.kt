package com.example.obby.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.obby.data.repository.NoteRepository
import com.example.obby.domain.model.NoteGraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GraphViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _graph = MutableStateFlow<NoteGraph?>(null)
    val graph: StateFlow<NoteGraph?> = _graph.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedNodeId = MutableStateFlow<Long?>(null)
    val selectedNodeId: StateFlow<Long?> = _selectedNodeId.asStateFlow()

    init {
        loadGraph()
    }

    fun loadGraph() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val noteGraph = repository.generateNoteGraph()
                _graph.value = noteGraph
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectNode(nodeId: Long?) {
        _selectedNodeId.value = nodeId
    }
}
