package dev.diagramcomposer.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val BUTTON_SPACING = 4.dp

/**
 * Which toolbar actions are currently available, decoupled from the
 * callbacks themselves so [Toolbar] stays under a 6-parameter signature
 * (AGENTS.md "Kotlin Guidelines" / detekt's `LongParameterList`).
 *
 * [canAddRelationship] is `false` when the diagram has no entities yet,
 * since [AddRelationshipDialog] requires at least one to pick a
 * source/target from.
 */
data class ToolbarState(
    val canAddRelationship: Boolean,
    val canUndo: Boolean,
    val canRedo: Boolean,
)

/**
 * Diagram Composer toolbar (docs/implementation-plan.md Milestone 8 tasks
 * 1, 3, 5; docs/engineering.md §8's `Toolbar` role; docs/ui.md §4). Unlike
 * docs/ui.md §4.2's per-entity-type PlantUML C4 toolbar, this exposes a
 * single "Add Entity" action that opens [AddEntityDialog] (which itself
 * collects the type) — the PlantUML C4 adapter's specific vocabulary living
 * in the toolbar directly would be language-specific UI logic, which
 * docs/architecture.md §5 reserves for adapters, not `ui`.
 */
@Composable
fun Toolbar(
    state: ToolbarState,
    onAddEntity: () -> Unit,
    onAddRelationship: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(vertical = BUTTON_SPACING)) {
        TextButton(onClick = onAddEntity) { Text("Add Entity") }
        TextButton(onClick = onAddRelationship, enabled = state.canAddRelationship) { Text("Add Relationship") }
        TextButton(onClick = onUndo, enabled = state.canUndo) { Text("Undo") }
        TextButton(onClick = onRedo, enabled = state.canRedo) { Text("Redo") }
    }
}
