package dev.diagramcomposer.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.diagramcomposer.ui.components.elementExplorer
import dev.diagramcomposer.ui.components.relationshipList
import dev.diagramcomposer.ui.state.DiagramViewModel

private const val CONTENT_PADDING_DP = 16
private val CONTENT_PADDING = CONTENT_PADDING_DP.dp

/**
 * Root composable for the Diagram Composer UI
 * (docs/implementation-plan.md Milestone 7): a read-only, side-by-side view
 * of [viewModel]'s element tree and relationship list.
 *
 * No editing, selection, or canvas rendering yet — those land in later
 * milestones (docs/engineering.md §8's `Toolbar`/`Canvas`/`PropertiesPanel`
 * aren't part of this UI yet; only its `Explorer` role is covered here).
 */
@Composable
fun diagramComposerApp(viewModel: DiagramViewModel) {
    Row(modifier = Modifier.fillMaxSize().padding(CONTENT_PADDING)) {
        elementExplorer(
            nodes = viewModel.elementTree,
            modifier = Modifier.weight(1f),
        )
        relationshipList(
            rows = viewModel.relationshipRows,
            modifier = Modifier.weight(1f),
        )
    }
}
