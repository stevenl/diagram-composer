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
 * callbacks themselves so [Toolbar] stays under detekt's `LongParameterList`
 * threshold (6).
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
 * The toolbar's callbacks, bundled for the same reason as [ToolbarState].
 */
data class ToolbarActions(
    val onAddEntity: () -> Unit,
    val onAddRelationship: () -> Unit,
    val onUndo: () -> Unit,
    val onRedo: () -> Unit,
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
    actions: ToolbarActions,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(vertical = BUTTON_SPACING)) {
        TextButton(onClick = actions.onAddEntity) { Text("Add Entity") }
        TextButton(onClick = actions.onAddRelationship, enabled = state.canAddRelationship) {
            Text("Add Relationship")
        }
        TextButton(onClick = actions.onUndo, enabled = state.canUndo) { Text("Undo") }
        TextButton(onClick = actions.onRedo, enabled = state.canRedo) { Text("Redo") }
    }
}
