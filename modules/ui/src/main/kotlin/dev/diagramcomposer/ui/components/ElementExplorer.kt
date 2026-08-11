package dev.diagramcomposer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.diagramcomposer.core.model.Entity
import dev.diagramcomposer.ui.state.ElementTreeNode

private const val INDENT_PER_DEPTH_DP = 16

/**
 * Tree/list view of a diagram's elements, grouped by boundary
 * (docs/implementation-plan.md Milestone 7 task 2). Nested boundaries are
 * indented one level deeper than their parent, matching [ElementTreeNode]'s
 * recursive shape.
 *
 * Milestone 8 adds editing: clicking an entity row invokes [onEditEntity]
 * (task 2, "edit entity properties"), and each entity row offers a "Remove"
 * action invoking [onRemoveEntity] (task 4). Boundaries have no edit/remove
 * action yet — `AddBoundaryCommand`/an "edit boundary" command aren't in
 * this milestone's scope (docs/implementation-plan.md Milestone 8's task
 * list only covers entities and relationships).
 */
@Composable
fun ElementExplorer(
    nodes: List<ElementTreeNode>,
    onEditEntity: (Entity) -> Unit,
    onRemoveEntity: (Entity) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        renderNodes(nodes, depth = 0, onEditEntity, onRemoveEntity)
    }
}

private fun LazyListScope.renderNodes(
    nodes: List<ElementTreeNode>,
    depth: Int,
    onEditEntity: (Entity) -> Unit,
    onRemoveEntity: (Entity) -> Unit,
) {
    nodes.forEach { node ->
        when (node) {
            is ElementTreeNode.EntityNode ->
                item { EntityRow(node.entity, depth, onEditEntity, onRemoveEntity) }
            is ElementTreeNode.BoundaryNode -> {
                item { BoundaryRow(node.boundary.name, depth) }
                renderNodes(node.children, depth + 1, onEditEntity, onRemoveEntity)
            }
        }
    }
}

@Composable
private fun EntityRow(
    entity: Entity,
    depth: Int,
    onEditEntity: (Entity) -> Unit,
    onRemoveEntity: (Entity) -> Unit,
) {
    Row(modifier = Modifier.padding(start = (depth * INDENT_PER_DEPTH_DP).dp)) {
        Text(
            text = "${entity.name} (${entity.type.name.lowercase()})",
            modifier =
                Modifier
                    .clickable { onEditEntity(entity) }
                    .padding(top = 2.dp, bottom = 2.dp),
        )
        TextButton(onClick = { onRemoveEntity(entity) }) { Text("Remove") }
    }
}

@Composable
private fun BoundaryRow(
    name: String,
    depth: Int,
) {
    Text(
        text = name,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = (depth * INDENT_PER_DEPTH_DP).dp, top = 8.dp, bottom = 2.dp),
    )
}
