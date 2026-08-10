package dev.diagramcomposer.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.ui.state.ElementTreeNode

private const val INDENT_PER_DEPTH_DP = 16

/**
 * Read-only tree/list view of a diagram's elements, grouped by boundary
 * (docs/implementation-plan.md Milestone 7 task 2). Nested boundaries are
 * indented one level deeper than their parent, matching [ElementTreeNode]'s
 * recursive shape.
 */
@Composable
fun elementExplorer(
    nodes: List<ElementTreeNode>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        renderNodes(nodes, depth = 0)
    }
}

private fun LazyListScope.renderNodes(
    nodes: List<ElementTreeNode>,
    depth: Int,
) {
    nodes.forEach { node ->
        when (node) {
            is ElementTreeNode.EntityNode -> item { entityRow(node.entity, depth) }
            is ElementTreeNode.BoundaryNode -> {
                item { boundaryRow(node.boundary.name, depth) }
                renderNodes(node.children, depth + 1)
            }
        }
    }
}

@Composable
private fun entityRow(
    entity: Entity,
    depth: Int,
) {
    Text(
        text = "${entity.name} (${entity.type.name.lowercase()})",
        modifier = Modifier.padding(start = (depth * INDENT_PER_DEPTH_DP).dp, top = 2.dp, bottom = 2.dp),
    )
}

@Composable
private fun boundaryRow(
    name: String,
    depth: Int,
) {
    Text(
        text = name,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = (depth * INDENT_PER_DEPTH_DP).dp, top = 8.dp, bottom = 2.dp),
    )
}
