package com.example.obby.domain.model

data class GraphNode(
    val id: Long,
    val title: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val connections: Int = 0
)

data class GraphEdge(
    val sourceId: Long,
    val targetId: Long,
    val linkText: String
)

data class NoteGraph(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>
)
