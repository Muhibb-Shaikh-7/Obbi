package com.example.obby.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.obby.data.repository.NoteRepository
import com.example.obby.domain.model.GraphEdge
import com.example.obby.domain.model.GraphNode
import com.example.obby.ui.viewmodel.GraphViewModel
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    repository: NoteRepository,
    onNodeClick: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel = remember { GraphViewModel(repository) }
    val graph by viewModel.graph.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedNodeId by viewModel.selectedNodeId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Graph View") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadGraph() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                graph == null || graph!!.nodes.isEmpty() -> {
                    EmptyGraphState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    GraphCanvas(
                        nodes = graph!!.nodes,
                        edges = graph!!.edges,
                        selectedNodeId = selectedNodeId,
                        onNodeClick = { nodeId ->
                            viewModel.selectNode(nodeId)
                        },
                        onNodeDoubleClick = onNodeClick,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Show selected node info
                    selectedNodeId?.let { nodeId ->
                        val selectedNode = graph!!.nodes.find { it.id == nodeId }
                        selectedNode?.let { node ->
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                                    .fillMaxWidth(0.9f),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = node.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${node.connections} connections",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { onNodeClick(node.id) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Open Note")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GraphCanvas(
    nodes: List<GraphNode>,
    edges: List<GraphEdge>,
    selectedNodeId: Long?,
    onNodeClick: (Long) -> Unit,
    onNodeDoubleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    var lastClickTime by remember { mutableStateOf(0L) }
    var lastClickedNodeId by remember { mutableStateOf<Long?>(null) }

    val density = LocalDensity.current
    val nodeRadius = with(density) { 24.dp.toPx() }

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                // Find clicked node
                val clickedNode = nodes.find { node ->
                    val dx = (size.width / 2 + node.x - offset.x)
                    val dy = (size.height / 2 + node.y - offset.y)
                    sqrt(dx * dx + dy * dy) <= nodeRadius
                }

                clickedNode?.let { node ->
                    val currentTime = System.currentTimeMillis()
                    if (node.id == lastClickedNodeId && currentTime - lastClickTime < 500) {
                        // Double click
                        onNodeDoubleClick(node.id)
                    } else {
                        // Single click
                        onNodeClick(node.id)
                        lastClickedNodeId = node.id
                        lastClickTime = currentTime
                    }
                }
            }
        }
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Draw edges
        edges.forEach { edge ->
            val sourceNode = nodes.find { it.id == edge.sourceId }
            val targetNode = nodes.find { it.id == edge.targetId }

            if (sourceNode != null && targetNode != null) {
                drawLine(
                    color = surfaceVariantColor,
                    start = Offset(centerX + sourceNode.x, centerY + sourceNode.y),
                    end = Offset(centerX + targetNode.x, centerY + targetNode.y),
                    strokeWidth = 2f
                )
            }
        }

        // Draw nodes
        nodes.forEach { node ->
            val isSelected = node.id == selectedNodeId
            val nodeColor = if (isSelected) secondaryColor else primaryColor
            val nodeSize = if (isSelected) nodeRadius * 1.2f else nodeRadius

            drawCircle(
                color = nodeColor,
                radius = nodeSize,
                center = Offset(centerX + node.x, centerY + node.y)
            )

            // Draw node label
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 32f
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                val text = if (node.title.length > 10) {
                    node.title.take(8) + "..."
                } else {
                    node.title
                }

                drawText(
                    text,
                    centerX + node.x,
                    centerY + node.y + nodeRadius + 40f,
                    paint
                )
            }
        }
    }
}

@Composable
fun EmptyGraphState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No connections yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create links between notes to see the graph",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
