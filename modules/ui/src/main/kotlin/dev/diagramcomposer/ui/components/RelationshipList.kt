package dev.diagramcomposer.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.diagramcomposer.ui.state.RelationshipRow

/**
 * Read-only list view of a diagram's relationships
 * (docs/implementation-plan.md Milestone 7 task 3).
 */
@Composable
fun relationshipList(
    rows: List<RelationshipRow>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(rows, key = { it.id.value }) { row ->
            Text(
                text = row.describe(),
                modifier = Modifier.padding(vertical = 4.dp),
            )
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
