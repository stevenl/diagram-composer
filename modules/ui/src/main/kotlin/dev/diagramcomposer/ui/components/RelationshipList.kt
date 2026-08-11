package dev.diagramcomposer.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.diagramcomposer.ui.state.RelationshipRow

/**
 * List view of a diagram's relationships (docs/implementation-plan.md
 * Milestone 7 task 3). Milestone 8 task 4 adds a "Remove" action per row,
 * invoking [onRemoveRelationship].
 */
@Composable
fun RelationshipList(
    rows: List<RelationshipRow>,
    onRemoveRelationship: (RelationshipRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(rows, key = { it.id.value }) { row ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(row.describe())
                TextButton(onClick = { onRemoveRelationship(row) }) { Text("Remove") }
            }
        }
    }
}

private fun RelationshipRow.describe(): String =
    buildString {
        append(sourceName)
        append(" -> ")
        append(targetName)
        description?.let { append(": $it") }
    }
